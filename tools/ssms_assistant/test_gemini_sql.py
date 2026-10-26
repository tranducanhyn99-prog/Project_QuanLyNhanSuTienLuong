import io
import json
import os
import unittest
import urllib.error
from unittest.mock import MagicMock, patch

import sql_brain


class TestGeminiSql(unittest.TestCase):
    TEST_KEY = "unit-test-key-never-use-for-api"
    LIVE_SCHEMA = (
        "LIVE_DATABASE|[ExistingPayroll]\n"
        "OBJECT|USER_TABLE|[hr].[ExistingEmployees]\n"
        "COLUMN|[hr].[ExistingEmployees]|[EmployeeId]|int NOT NULL\n"
    )

    def setUp(self):
        target = dict(sql_brain._active_sql_target)
        error = sql_brain._last_ai_error
        self.addCleanup(self.restore_sql_state, target, error)
        sql_brain._active_sql_target.clear()
        sql_brain._last_ai_error = ""

        patches = (
            patch.dict(os.environ, {}, clear=True),
            patch("sql_brain._load_api_key", return_value=self.TEST_KEY),
            patch("sql_brain.load_schema_context", return_value=self.LIVE_SCHEMA),
            patch("sql_brain.get_last_schema_error", return_value=""),
            patch("sql_brain.urllib.request.urlopen"),
        )
        started = []
        for test_patch in patches:
            started.append(test_patch.start())
            self.addCleanup(test_patch.stop)
        _, self.load_key, self.load_schema, self.schema_error, self.urlopen = started
        self.response = MagicMock()
        self.urlopen.return_value.__enter__.return_value = self.response
        self.set_response({
            "candidates": [{"content": {"parts": [{"text": "SELECT EmployeeId FROM hr.ExistingEmployees;"}]}}]
        })

    @staticmethod
    def restore_sql_state(target, error):
        sql_brain._active_sql_target.clear()
        sql_brain._active_sql_target.update(target)
        sql_brain._last_ai_error = error

    def set_response(self, response_data):
        self.response.read.return_value = json.dumps(response_data).encode("utf-8")

    def sent_request(self):
        return self.urlopen.call_args.args[0]

    def test_request_uses_live_metadata_and_key_header(self):
        prompt = "liệt kê mã nhân viên đang có"
        sql = sql_brain._call_gemini_fallback(prompt)
        self.assertEqual("SELECT EmployeeId FROM hr.ExistingEmployees;", sql)
        self.load_schema.assert_called_once_with()
        self.urlopen.assert_called_once()

        request = self.sent_request()
        body = json.loads(request.data.decode("utf-8"))
        prompt_text = "\n".join(part["text"] for part in body["contents"][0]["parts"])
        self.assertIn(self.LIVE_SCHEMA, prompt_text)
        self.assertIn(prompt, prompt_text)
        self.assertNotIn("MaBangLuong 1008", prompt_text)
        headers = {name.lower(): value for name, value in request.header_items()}
        self.assertEqual(self.TEST_KEY, headers["x-goog-api-key"])
        self.assertNotIn(self.TEST_KEY, request.full_url)
        self.assertNotIn("?key=", request.full_url)
        self.assertNotIn("thinkingBudget", body)
        self.assertNotIn("thinkingConfig", body)
        self.assertNotIn("thinkingBudget", json.dumps(body))

    def test_active_ssms_target_is_passed_to_metadata_reader(self):
        sql_brain.set_database_context(r"WORKSTATION\SQLEXPRESS", "ExistingPayroll")
        sql_brain._call_gemini_fallback("đọc danh sách nhân viên")
        self.load_schema.assert_called_once_with(
            server=r"WORKSTATION\SQLEXPRESS", database="ExistingPayroll",
        )

        self.load_schema.reset_mock()
        sql_brain.set_database_context("AnotherServer", "AnotherExistingDatabase")
        sql_brain._call_gemini_fallback("đọc danh sách nhân viên")
        self.load_schema.assert_called_once_with(
            server="AnotherServer", database="AnotherExistingDatabase",
        )

    def test_unavailable_metadata_stops_before_api_and_explains_recovery(self):
        self.load_schema.return_value = ""
        sql = sql_brain.generate_sql("-- #tìm nhân viên có lương cao nhất", force_ai=True)
        self.urlopen.assert_not_called()
        self.assertTrue(sql.lstrip().startswith("--"), sql)
        self.assertIn("schema", sql.lower())
        self.assertIn("sqlcmd", sql.lower())
        self.assertIn("SSMS_SQL_DATABASE", sql)
        self.assertNotRegex(sql, r"(?im)^\s*(SELECT|EXEC|CREATE|USE)\b")

    def test_missing_key_does_not_read_metadata_or_call_api(self):
        self.load_key.return_value = ""
        sql = sql_brain.generate_sql("-- #đọc nhân viên", force_ai=True)
        self.load_schema.assert_not_called()
        self.urlopen.assert_not_called()
        self.assertIn("GEMINI_API_KEY", sql)
        self.assertTrue(sql.lstrip().startswith("--"), sql)

    def test_metadata_failure_reports_specific_cause_without_calling_api(self):
        self.load_schema.return_value = None
        self.schema_error.return_value = "SQL Server đọc metadata quá thời gian chờ."
        sql = sql_brain.generate_sql(
            "-- viết code thực hiện lấy danh sách 3 người có tiền lương cao nhất", force_ai=True,
        )
        self.assertIn(self.schema_error.return_value, sql)
        self.urlopen.assert_not_called()

    def test_response_joins_all_sql_parts_and_omits_thoughts(self):
        self.set_response({
            "candidates": [{"content": {"parts": [
                {"text": "internal reasoning must not become SQL", "thought": True},
                {"text": "```sql\nSELECT"},
                {"functionCall": {"name": "ignored"}},
                {"text": "EmployeeId"},
                {"text": "FROM hr.ExistingEmployees;\n```"},
            ]}}],
        })
        sql = sql_brain._call_gemini_fallback("đọc mã nhân viên")
        self.assertEqual("SELECT\nEmployeeId\nFROM hr.ExistingEmployees;", sql)
        self.assertNotIn("reasoning", sql)
        self.assertNotIn("```", sql)

    def test_gemini_model_environment_override_is_used(self):
        os.environ["GEMINI_MODEL"] = "gemini-unit-test-1.0"
        self.assertIsNotNone(sql_brain._call_gemini_fallback("đọc mã nhân viên"))
        self.urlopen.assert_called_once()
        self.assertTrue(self.sent_request().full_url.endswith("/models/gemini-unit-test-1.0:generateContent"))

    def test_invalid_model_override_is_rejected_without_network(self):
        for model in ("../other-model", "models/gemini-test", "gemini-test?key=secret", "model with spaces"):
            with self.subTest(model=model):
                os.environ["GEMINI_MODEL"] = model
                self.assertIsNone(sql_brain._call_gemini_fallback("đọc mã nhân viên"))
                self.urlopen.assert_not_called()
                self.assertIn("GEMINI_MODEL", sql_brain._last_ai_error)
                self.assertNotIn(model, sql_brain._last_ai_error)

    def test_rate_limit_error_is_actionable_and_does_not_expose_key(self):
        self.urlopen.side_effect = urllib.error.HTTPError(
            "https://example.invalid/?key=" + self.TEST_KEY,
            429,
            "quota exhausted for key " + self.TEST_KEY,
            hdrs=None,
            fp=io.BytesIO(self.TEST_KEY.encode("utf-8")),
        )
        sql = sql_brain.generate_sql("-- #tìm nhân viên có lương cao nhất", force_ai=True, clean_comments=True)
        self.assertTrue(sql.lstrip().startswith("--"), sql)
        self.assertIn("429", sql)
        self.assertRegex(sql, r"hạn mức|giới hạn")
        self.assertNotIn(self.TEST_KEY, sql)
        self.assertNotIn(self.TEST_KEY, sql_brain._last_ai_error)
        self.assertNotRegex(sql, r"(?im)^\s*(SELECT|EXEC|CREATE|USE)\b")

    def test_empty_or_malformed_response_does_not_become_an_offline_query(self):
        for response_data in (
            {},
            {"candidates": []},
            {"candidates": [{"content": {"parts": [{"thought": True, "text": "reasoning only"}]}}]},
        ):
            with self.subTest(response_data=response_data):
                self.set_response(response_data)
                sql = sql_brain.generate_sql("-- #tìm nhân viên có lương cao nhất", force_ai=True)
                self.assertTrue(sql.lstrip().startswith("--"), sql)
                self.assertIn("Gemini", sql)
                self.assertNotIn("reasoning only", sql)
                self.assertNotRegex(sql, r"(?im)^\s*(SELECT|EXEC|CREATE|USE)\b")


if __name__ == "__main__":
    unittest.main()
