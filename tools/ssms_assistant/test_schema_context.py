import subprocess
import unittest
from unittest.mock import patch

import schema_context


_CATALOG = "\ufeffLIVE_DATABASE|[ExistingDb]\nOBJECT|USER_TABLE|[dbo].[ExistingTable]\nSCHEMA_COMPLETE\n"


class TestSchemaContext(unittest.TestCase):
    def setUp(self):
        self.addCleanup(patch.stopall)
        patch.dict(schema_context.os.environ, {}, clear=True).start()
        patch.object(schema_context, "_schema_cache", {}).start()
        patch.object(schema_context, "_last_schema_error", "").start()
        patch.object(schema_context.shutil, "which", return_value="sqlcmd.exe").start()
        self.run = patch.object(schema_context.subprocess, "run").start()
        self.run.return_value = subprocess.CompletedProcess([], 0, _CATALOG, "")

    def test_reads_existing_database_with_integrated_auth_and_caches_success(self):
        first = schema_context.load_schema_context()
        self.assertIn("VERIFIED LIVE", first)
        self.assertIn("[ExistingDb]", first)
        self.assertNotIn("SCHEMA_COMPLETE", first)
        self.assertEqual("", schema_context.get_last_schema_error())
        self.assertEqual(first, schema_context.load_schema_context())
        self.run.assert_called_once()
        args, kwargs = self.run.call_args
        command = args[0]
        self.assertIn("-E", command)
        self.assertEqual(command[command.index("-d") + 1], "QuanLyNhanSuTienLuong")
        self.assertEqual(command[command.index("-S") + 1], "localhost")
        self.assertLessEqual(kwargs["timeout"], 5)
        self.assertFalse(kwargs["shell"])
        self.assertEqual(kwargs["creationflags"], getattr(subprocess, "CREATE_NO_WINDOW", 0))
        query = command[command.index("-Q") + 1]
        self.assertNotRegex(query, r"(?i)\b(?:CREATE|ALTER|DROP|INSERT|UPDATE|DELETE|EXEC|USE)\b")
        self.assertNotRegex(query, r"(?i)\bFROM\s+dbo\.")
        self.assertEqual(query.count("SELECT "), query.count("MAX_GRANT_PERCENT = 1"))
        self.assertEqual(query.count("SELECT "), query.count("MAXDOP 1"))
        self.assertIn("SCHEMA_COMPLETE", query)

    def test_environment_target_has_separate_cache(self):
        schema_context.load_schema_context()
        with patch.dict(schema_context.os.environ, {"SSMS_SQL_SERVER": "my-server\\instance",
                                                    "SSMS_SQL_DATABASE": "Existing Database"}):
            schema_context.load_schema_context()
        self.assertEqual(self.run.call_count, 2)
        command = self.run.call_args.args[0]
        self.assertEqual(command[command.index("-S") + 1], "my-server\\instance")
        self.assertEqual(command[command.index("-d") + 1], "Existing Database")

    def test_explicit_target_overrides_environment(self):
        with patch.dict(schema_context.os.environ, {"SSMS_SQL_SERVER": "environment-server",
                                                    "SSMS_SQL_DATABASE": "EnvironmentDb"}):
            context = schema_context.load_schema_context(server="active-server", database="ActiveDb")
        command = self.run.call_args.args[0]
        self.assertEqual(command[command.index("-S") + 1], "active-server")
        self.assertEqual(command[command.index("-d") + 1], "ActiveDb")
        self.assertIn("Target server: active-server; target database: ActiveDb.", context)

    def test_invalid_target_is_not_passed_to_process(self):
        self.assertIsNone(schema_context.load_schema_context(database="Bad\nDatabase"))
        self.assertIn("không hợp lệ", schema_context.get_last_schema_error())
        self.run.assert_not_called()

    def test_expired_success_is_refreshed(self):
        with patch.object(schema_context.time, "monotonic", return_value=0):
            schema_context.load_schema_context()
        with patch.object(schema_context.time, "monotonic", return_value=61):
            schema_context.load_schema_context()
        self.assertEqual(self.run.call_count, 2)

    def test_failures_return_none_and_are_not_cached(self):
        for failure in (OSError("not available"), subprocess.TimeoutExpired("sqlcmd", 5)):
            with self.subTest(failure=failure):
                self.run.side_effect = failure
                self.assertIsNone(schema_context.load_schema_context())
                self.assertTrue(schema_context.get_last_schema_error())
        self.run.side_effect = None
        self.assertIsNotNone(schema_context.load_schema_context())
        self.assertEqual("", schema_context.get_last_schema_error())

    def test_unsuccessful_or_incomplete_catalog_is_not_accepted(self):
        for code, stdout in ((1, _CATALOG), (0, "error text"),
                             (0, "LIVE_DATABASE|[empty]"),
                             (0, "LIVE_DATABASE|[empty]\nSCHEMA_COMPLETE\n")):
            with self.subTest(code=code, stdout=stdout):
                self.run.return_value = subprocess.CompletedProcess([], code, stdout, "")
                self.assertIsNone(schema_context.load_schema_context())

    def test_missing_sqlcmd_fails_gracefully(self):
        schema_context.shutil.which.return_value = None
        self.assertIsNone(schema_context.load_schema_context())
        self.assertIn("sqlcmd", schema_context.get_last_schema_error())
        self.run.assert_not_called()

    def test_partial_success_is_not_cached_even_when_sqlcmd_returns_zero(self):
        partial = _CATALOG.replace("SCHEMA_COMPLETE\n", "")
        self.run.return_value = subprocess.CompletedProcess([], 0, partial, "")
        self.assertIsNone(schema_context.load_schema_context())
        self.assertIn("chưa đầy đủ", schema_context.get_last_schema_error())
        self.assertFalse(schema_context._schema_cache)
        self.run.return_value = subprocess.CompletedProcess([], 0, _CATALOG, "")
        self.assertIsNotNone(schema_context.load_schema_context())
        self.assertEqual(2, self.run.call_count)

    def test_sqlcmd_internal_timeout_error_is_rejected_despite_zero_exit_code(self):
        self.run.return_value = subprocess.CompletedProcess(
            [], 0, _CATALOG,
            "Sqlcmd: Error: Internal error at ReadAndHandleColumnData\nTimeout expired\n",
        )
        self.assertIsNone(schema_context.load_schema_context())
        self.assertIn("thời gian chờ", schema_context.get_last_schema_error())
        self.assertFalse(schema_context._schema_cache)

    def test_connection_errors_are_explained_without_echoing_raw_output(self):
        for error, detail in (
            ("Login failed for user private-name with token-secret", "quyền"),
            ("TCP Provider: connection refused token-secret", "Không kết nối"),
        ):
            with self.subTest(detail=detail):
                self.run.return_value = subprocess.CompletedProcess([], 1, "", error)
                self.assertIsNone(schema_context.load_schema_context())
                self.assertIn(detail, schema_context.get_last_schema_error())
                self.assertNotIn("private-name", schema_context.get_last_schema_error())
                self.assertNotIn("token-secret", schema_context.get_last_schema_error())

    def test_completed_metadata_can_include_a_benign_warning(self):
        self.run.return_value = subprocess.CompletedProcess([], 0, _CATALOG, "Warning: informational notice\n")
        self.assertIsNotNone(schema_context.load_schema_context())
        self.assertEqual("", schema_context.get_last_schema_error())


if __name__ == "__main__":
    unittest.main()
