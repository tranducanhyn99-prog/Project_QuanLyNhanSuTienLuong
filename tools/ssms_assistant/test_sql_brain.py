import re
import unittest
from unittest.mock import patch

from sql_brain import generate_sql


class TestSqlBrain(unittest.TestCase):
    def setUp(self):
        # Keep every test independent of local credentials and network access.
        ai_patch = patch("sql_brain._call_gemini_fallback", return_value=None)
        self.ai = ai_patch.start()
        self.addCleanup(ai_patch.stop)

    def assert_uses_current_database(self, sql):
        self.assertNotRegex(sql, r"(?im)^\s*USE\s+")
        self.assertNotRegex(sql, r"(?im)^\s*GO(?:\s+\d+)?\s*;?\s*$")

    def assert_uses_existing_objects(self, sql):
        self.assertNotRegex(sql, r"(?i)\bCREATE\s+(?:OR\s+ALTER\s+)?(?:DATABASE|TABLE|TRIGGER)\b")
        self.assertNotRegex(sql, r"(?i)\bINSERT\s+(?:INTO\s+)?dbo\.NHANVIEN\b")
        self.assertNotRegex(sql, r"\b100[89]\b")
        self.assertRegex(sql, r"(?is)\bSELECT\b.*\bFROM\s+dbo\.")

    def test_dynamic_grant_sp_to_role(self):
        sql = generate_sql("-- #cấp quyền chốt lương cho hr")
        self.assertRegex(sql, r"grant execute on (?:object::)?dbo\.sp_ChotBangLuong to \[?role_HRManager\]?")
        self.assertNotRegex(sql, r"(?i)\bREVOKE\b")

    def test_dynamic_deny_table_to_role(self):
        sql = generate_sql("-- #cấm kế toán sửa xóa bảng nhân viên")
        self.assertIn("deny", sql)
        self.assertIn("dbo.NHANVIEN", sql)
        self.assertIn("role_PayrollOfficer", sql)

    def test_dynamic_revoke_permission(self):
        sql = generate_sql("-- #thu hoi quyen select tren bangluong cua nhan vien")
        self.assertRegex(sql, r"revoke select on (?:object::)?dbo\.BANGLUONG from \[?role_Employee\]?")

    def test_create_login_and_role(self):
        sql = generate_sql("-- #tạo login test_gv và gán vào role hr")
        self.assertIn("create login [test_gv]", sql)
        self.assertIn("create user [test_gv] for login [test_gv]", sql)
        self.assertIn("alter role role_HRManager add member [test_gv]", sql)

    def test_rbac_sections(self):
        for prompt, role in (
            ("-- #test quyền hr", "role_HRManager"),
            ("-- #test quyền kế toán", "role_PayrollOfficer"),
            ("-- #test quyền nhân viên", "role_Employee"),
        ):
            with self.subTest(prompt=prompt):
                sql = generate_sql(prompt)
                self.assertIn(role, sql)
                self.assertIn("sys.database_role_members", sql)
                self.assertRegex(sql, r"(?i)EXECUTE AS (?:USER|LOGIN)")
                self.assertRegex(sql, r"(?i)HAS_PERMS_BY_NAME")
                self.assertRegex(sql, r"(?i)REVERT;")
                self.assertNotRegex(sql, r"(?i)\bCREATE\s+(?:LOGIN|USER)\b")

    def test_rls_view_and_da_chot_modification(self):
        sql = generate_sql("-- #test rls phiếu lương cá nhân")
        self.assertIn("vw_PhieuLuongCaNhan", sql)
        self.assertIn("MaNV", sql)
        self.assertIn("ThucNhan", sql)
        self.assertNotRegex(sql, r"(?i)\bCREATE\s+(?:LOGIN|USER|VIEW)\b")
        self.assertNotRegex(sql, r"(?i)\b(?:DECLARE|THROW)\b")

        sql_mod = generate_sql("-- #sửa view rls chỉ xem kỳ đã chốt")
        self.assertRegex(sql_mod, r"(?:create or )?alter view dbo\.vw_PhieuLuongCaNhan")
        self.assertIn("TrangThaiBangLuong = 'DA_CHOT'", sql_mod)

    def test_rls_test_for_closed_period_does_not_alter_view(self):
        for clean_comments in (False, True):
            with self.subTest(clean_comments=clean_comments):
                sql = generate_sql("-- #test rls phiếu lương cá nhân kỳ đã chốt", clean_comments=clean_comments)
                self.assertIn("dbo.vw_PhieuLuongCaNhan", sql)
                self.assertNotRegex(sql, r"(?i)\b(?:CREATE|ALTER)\s+(?:OR\s+ALTER\s+)?VIEW\b")
                self.assert_uses_current_database(sql)

    def test_sp_chot_and_mo_luong(self):
        for prompt, procedure in (
            ("-- #chốt bảng lương 3742", "sp_ChotBangLuong"),
            ("-- #mở lại kỳ lương 3742", "sp_HuyChotBangLuong"),
        ):
            with self.subTest(prompt=prompt):
                sql = generate_sql(prompt, clean_comments=True)
                self.assertIn("exec dbo." + procedure, sql)
                self.assertRegex(sql, r"\b3742\b")
                self.assertNotRegex(sql, r"\b100[89]\b")

    def test_close_and_reopen_honor_requested_period(self):
        for prompt in ("-- chốt bảng lương tháng 9 năm 2026", "-- mở lại kỳ lương tháng 9/2026"):
            with self.subTest(prompt=prompt):
                sql = generate_sql(prompt, clean_comments=True)
                self.assertIn("bl.Thang = 9", sql)
                self.assertIn("bl.Nam = 2026", sql)

    def test_top_actual_pay_requires_ranking_not_function_demo(self):
        sql = generate_sql("-- tìm 3 nhân viên thực nhận cao nhất", clean_comments=True)
        self.assertIn("top (3) with ties", sql)
        self.assertIn("order by ThucNhan desc", sql)
        self.assertNotIn("dbo.fn_TinhThucNhan(", sql)

    def test_payroll_without_id_selects_an_existing_period(self):
        for prompt, procedure in (
            ("-- #chốt bảng lương", "sp_ChotBangLuong"),
            ("-- #mở lại kỳ lương", "sp_HuyChotBangLuong"),
        ):
            with self.subTest(prompt=prompt):
                sql = generate_sql(prompt, clean_comments=True)
                self.assertIn("exec dbo." + procedure, sql)
                self.assertRegex(sql, r"(?is)\bSELECT\b.*\bFROM\s+dbo\.BANGLUONG\b")
                self.assertNotRegex(sql, r"\b100[89]\b")
                self.assertNotRegex(sql, r"(?i)\bINSERT\s+(?:INTO\s+)?dbo\.BANGLUONG\b")
        self.assertNotIn("sp_HuyChotBangLuong", generate_sql("-- #chốt bảng lương"))

    def test_triggers_tv5(self):
        self.assertIn("dbo.CHAMCONG", generate_sql("-- #test trigger chấm công kỳ đã chốt"))
        self.assertIn("dbo.PHUCAPNHANVIEN", generate_sql("-- #test trigger phụ cấp"))
        self.assertIn("dbo.KHAUTRUNHANVIEN", generate_sql("-- #test trigger khấu trừ"))
        self.assertIn("dbo.CHITIETBANGLUONG", generate_sql("-- #test trigger sửa chi tiết lương"))
        self.assertIn("delete from dbo.NHANVIEN", generate_sql("-- #test trigger xóa nhân viên"))
        self.assertIn("disable trigger", generate_sql("-- #tắt trigger chấm công"))

    def test_payroll_header_trigger_uses_existing_closed_row_and_rolls_back(self):
        sql = generate_sql("-- #test trigger bảng lương", clean_comments=True)
        self.assert_uses_existing_objects(sql)
        self.assert_uses_current_database(sql)
        self.assertIn("trg_BangLuong_KhongSuaKhiDaChot", sql)
        self.assertIn("sys.triggers", sql)
        self.assertRegex(sql, r"(?is)\bSELECT\b.*FROM dbo\.BANGLUONG\b.*TrangThai\s*=\s*'DA_CHOT'")
        self.assertIn("update dbo.BANGLUONG", sql)
        self.assertIn("rollback transaction", sql)
        self.assertNotIn("sp_ChotBangLuong", sql)
        self.assertNotIn("sp_HuyChotBangLuong", sql)
        self.assertNotIn("dbo.CHAMCONG", sql)
        self.assertNotRegex(sql, r"(?i)\b(?:ENABLE|DISABLE)\s+TRIGGER\b")

    def test_function_thuc_nhan_and_clamp_zero(self):
        sql = generate_sql("-- #test hàm tính thực nhận")
        self.assertIn("dbo.fn_TinhThucNhan", sql)
        sql_clamp = generate_sql("-- #sửa hàm thực nhận âm thì về 0")
        self.assertRegex(sql_clamp, r"(?:create or )?alter function dbo\.fn_TinhThucNhan")
        self.assertIn("when @KetQua < 0 then 0", sql_clamp)

    def test_negative_net_pay_test_does_not_implicitly_alter_function(self):
        for clean_comments in (False, True):
            with self.subTest(clean_comments=clean_comments):
                sql = generate_sql("-- #test hàm thực nhận âm thì về 0", clean_comments=clean_comments)
                self.assertIn("dbo.fn_TinhThucNhan", sql)
                self.assertNotRegex(sql, r"(?i)\b(?:CREATE|ALTER)\s+(?:OR\s+ALTER\s+)?FUNCTION\b")
                self.assert_uses_current_database(sql)

    def test_index_benchmark_and_10k(self):
        for prompt in ("-- #đo hiệu năng index", "-- #benchmark 10000 dòng bảng tạm"):
            with self.subTest(prompt=prompt):
                sql = generate_sql(prompt, clean_comments=True)
                self.assertIn("dbo.NHANVIEN", sql)
                self.assertIn("statistics io on", sql)
                self.assert_uses_existing_objects(sql)
                self.assertNotIn("#BenchmarkNhanVien", sql)

    def test_concurrency_and_blocking(self):
        self.assertIn("with (updlock, holdlock)", generate_sql("-- #concurrency cửa sổ 1"))
        self.assertIn("sys.dm_exec_requests", generate_sql("-- #xem session nào đang block"))

    def test_constraints_and_queries(self):
        self.assertIn("CHK_BANGLUONG_Thang", generate_sql("-- #test check tháng 13"))
        self.assertIn("group by TenPB", generate_sql("-- #thống kê quỹ lương theo phòng ban"))
        self.assertIn("top (1) with ties", generate_sql("-- #tìm nhân viên lương cao nhất"))
        self.assertIn("sp_helptext", generate_sql("-- #xem code sp_ChotBangLuong"))
        self.assertIn("revert;", generate_sql("-- #reset"))

    def test_analytical_queries_honor_explicit_period(self):
        for prompt, expected_operation in (
            ("-- #tìm 3 nhân viên lương cao nhất tháng 9 năm 2026", "top (3)"),
            ("-- #thống kê quỹ lương theo phòng ban tháng 9 năm 2026", "group by TenPB"),
        ):
            with self.subTest(prompt=prompt):
                sql = generate_sql(prompt, clean_comments=True)
                self.assertIn(expected_operation, sql)
                self.assertRegex(sql, r"\bThang\s*=\s*9\b")
                self.assertRegex(sql, r"\bNam\s*=\s*2026\b")
                self.assert_uses_existing_objects(sql)
                self.assertNotRegex(sql, r"(?i)\b(?:DECLARE|THROW)\b")

    def test_explicit_payroll_calculation_uses_existing_period_without_setup(self):
        sql = generate_sql("-- #chạy sp_TinhBangLuongThang tháng 10 năm 2026", clean_comments=True)
        self.assert_uses_existing_objects(sql)
        self.assert_uses_current_database(sql)
        self.assertRegex(sql, r"(?is)\bSELECT\b.*FROM dbo\.BANGLUONG\b")
        self.assertRegex(sql, r"bl\.Thang\s*=\s*10\b")
        self.assertRegex(sql, r"bl\.Nam\s*=\s*2026\b")
        self.assertIn("exec dbo.sp_TinhBangLuongThang", sql)
        self.assertNotRegex(sql, r"(?i)\bINSERT\s+(?:INTO\s+)?dbo\.")
        self.assertNotRegex(sql, r"(?i)\bCREATE\b")
        self.assertNotRegex(generate_sql("-- #tv4", clean_comments=True), r"(?i)\bEXEC(?:UTE)?\b")

    def test_offline_outputs_omit_use_and_unnecessary_go(self):
        prompts = (
            "-- #metadata",
            "-- #xem code sp_ChotBangLuong",
            "-- #thống kê quỹ lương theo phòng ban",
            "-- #test quyền hr",
            "-- #test trigger chấm công kỳ đã chốt",
            "-- #tv1",
            "-- #tv2",
            "-- #tv3",
            "-- #tv4",
            "-- #chốt bảng lương",
            "-- #concurrency cửa sổ 1",
        )
        for clean_comments in (False, True):
            for prompt in prompts:
                with self.subTest(prompt=prompt, clean_comments=clean_comments):
                    self.assert_uses_current_database(generate_sql(prompt, clean_comments=clean_comments))

    def test_default_trigger_tests_use_existing_rows_without_preparing_schema(self):
        for prompt in (
            "-- #test trigger",
            "-- #test trigger chấm công kỳ đã chốt",
            "-- #test trigger phụ cấp",
            "-- #test trigger khấu trừ",
            "-- #test trigger sửa chi tiết lương",
            "-- #test trigger xóa nhân viên",
        ):
            with self.subTest(prompt=prompt):
                sql = generate_sql(prompt, clean_comments=True)
                self.assert_uses_existing_objects(sql)
                self.assertNotRegex(sql, r"(?i)\b(?:ENABLE|DISABLE)\s+TRIGGER\b")
                self.assertNotIn("sp_ChotBangLuong", sql)

    def test_module_helpers_query_existing_data(self):
        for module in ("tv1", "tv2", "tv3", "tv4"):
            with self.subTest(module=module):
                sql = generate_sql("-- #" + module, clean_comments=True)
                self.assert_uses_existing_objects(sql)
                self.assertNotRegex(sql, r"(?i)\b(?:DECLARE|THROW)\b")

    def test_ai_outputs_omit_use_and_unnecessary_go_in_both_routes(self):
        self.ai.return_value = (
            "USE [QuanLyNhanSuTienLuong];\nGO\n"
            "SELECT MaNV FROM dbo.NHANVIEN;\nGO\n"
            "EXEC dbo.sp_LayTaiKhoanHienTai;\nGO"
        )
        for force_ai in (False, True):
            for clean_comments in (False, True):
                with self.subTest(force_ai=force_ai, clean_comments=clean_comments):
                    sql = generate_sql("-- #yêu cầu chưa có mẫu cục bộ", force_ai=force_ai, clean_comments=clean_comments)
                    self.assert_uses_current_database(sql)
                    self.assertIn("select MaNV from dbo.NHANVIEN;", sql)
                    self.assertIn("exec dbo.sp_LayTaiKhoanHienTai;", sql)

    def test_context_cleanup_preserves_string_literals_in_both_comment_modes(self):
        literal_select = (
            "SELECT N'-- giữ nguyên /* trong chuỗi */' AS LiteralText,\n"
            "       N'it''s -- literal' AS EscapedText,\n"
            "       N'first line\nGO\nUSE [AnotherDatabase];\n/* literal */\n-- literal\nlast line' AS MultilineText;"
        )
        expected_select = (
            "select N'-- giữ nguyên /* trong chuỗi */' as LiteralText,\n"
            "       N'it''s -- literal' as EscapedText,\n"
            "       N'first line\nGO\nUSE [AnotherDatabase];\n/* literal */\n-- literal\nlast line' as MultilineText;"
        )
        self.ai.return_value = (
            "USE [QuanLyNhanSuTienLuong];\nGO\n"
            "-- comment to remove\n"
            "/* block comment to remove\nGO\nUSE [AnotherDatabase];\n*/\n"
            + literal_select + " -- trailing comment to remove\nGO"
        )
        for clean_comments in (False, True):
            with self.subTest(clean_comments=clean_comments):
                sql = generate_sql("-- #viết truy vấn chuỗi", force_ai=True, clean_comments=clean_comments)
                self.assertIn(expected_select, sql)
                if clean_comments:
                    self.assertNotIn("comment to remove", sql)
                self.assertFalse(sql.lstrip().upper().startswith("USE "))

    def test_comment_cleanup_keeps_tokens_separated(self):
        self.ai.return_value = "SELECT/* explanation */MaNV FROM dbo.NHANVIEN; -- explanation"
        sql = generate_sql("-- #viết truy vấn nhân viên", force_ai=True, clean_comments=True)
        self.assertRegex(sql, r"select\s+MaNV\s+from")
        self.assertNotIn("explanation", sql)

    def test_comments_are_preserved_when_cleanup_is_disabled(self):
        comment = "/* preserved block\nUSE [AnotherDatabase];\nGO\n*/"
        self.ai.return_value = (
            "USE [QuanLyNhanSuTienLuong];\nGO\n"
            "-- preserved line comment\n" + comment + "\nSELECT 1;\nGO"
        )
        sql = generate_sql("-- #viết truy vấn", force_ai=True, clean_comments=False)
        self.assertIn("-- preserved line comment", sql)
        self.assertIn(comment, sql)
        self.assertIn("select 1;", sql)
        self.assertFalse(sql.lstrip().upper().startswith("USE "))

    def test_cleanup_preserves_quoted_identifiers_and_nested_comments(self):
        query = 'SELECT [-- weird]]name], [/* column */], "-- another column" FROM dbo.NHANVIEN;'
        expected_query = 'select [-- weird]]name], [/* column */], "-- another column" from dbo.NHANVIEN;'
        self.ai.return_value = "/* outer /* nested */ comment */\n" + query
        for clean_comments in (False, True):
            with self.subTest(clean_comments=clean_comments):
                sql = generate_sql("-- #đọc cột có tên đặc biệt", force_ai=True, clean_comments=clean_comments)
                self.assertIn(expected_query, sql)
                if clean_comments:
                    self.assertNotIn("nested", sql)
                    self.assertNotIn("outer", sql)
                else:
                    self.assertIn("/* outer /* nested */ comment */", sql)

    def test_repeat_go_is_preserved(self):
        self.ai.return_value = "SELECT 1;\nGO 2\nSELECT 2;\nGO"
        for clean_comments in (False, True):
            with self.subTest(clean_comments=clean_comments):
                sql = generate_sql("-- #chạy truy vấn hai lần", force_ai=True, clean_comments=clean_comments)
                self.assertRegex(sql, r"(?im)^GO 2$")
                self.assertIn("select 1;", sql)
                self.assertIn("select 2;", sql)

    def test_variable_scope_go_is_preserved(self):
        for first_declaration, expected_declaration in (
            ("DECLARE @MaNV int = 1;", "declare @MaNV int = 1;"),
            ("DECLARE @Other int = 0, @MaNV int = 1;", "declare @Other int = 0, @MaNV int = 1;"),
        ):
            self.ai.return_value = first_declaration + "\nSELECT @MaNV;\nGO\nDECLARE @MaNV int = 2;\nSELECT @MaNV;\nGO"
            with self.subTest(first_declaration=first_declaration):
                sql = generate_sql("-- #đọc dữ liệu qua hai batch", force_ai=True)
                batches = [part.strip() for part in re.split(r"(?im)^\s*GO\s*$", sql) if part.strip()]
                self.assertEqual(len(batches), 2)
                self.assertIn(expected_declaration, batches[0])
                self.assertIn("declare @MaNV int = 2;", batches[1])

    def test_explicit_module_ddl_keeps_required_batch_boundaries(self):
        for module_ddl, expected_ddl in (
            (
                "CREATE OR ALTER VIEW dbo.ExistingView AS SELECT 1 AS Value;",
                "create or alter view dbo.ExistingView as select 1 as Value;",
            ),
            (
                "CREATE OR ALTER FUNCTION dbo.ExistingFunction() RETURNS int AS BEGIN RETURN 1; END;",
                "create or alter function dbo.ExistingFunction() returns int as begin return 1; end;",
            ),
        ):
            self.ai.return_value = (
                "USE [QuanLyNhanSuTienLuong];\nGO\n"
                "SELECT N'before definition';\nGO\n"
                + module_ddl + "\nGO\nSELECT N'after definition';\nGO"
            )
            for clean_comments in (False, True):
                with self.subTest(module_ddl=module_ddl, clean_comments=clean_comments):
                    sql = generate_sql("-- #sửa định nghĩa đối tượng", force_ai=True, clean_comments=clean_comments)
                    self.assertNotRegex(sql, r"(?im)^\s*USE\s+")
                    batches = [part.strip() for part in re.split(r"(?im)^\s*GO\s*$", sql) if part.strip()]
                    self.assertEqual(len(batches), 3)
                    self.assertIn("before definition", batches[0])
                    self.assertEqual(expected_ddl, batches[1])
                    self.assertIn("after definition", batches[2])

    def test_force_ai_failure_does_not_silently_run_an_offline_query(self):
        for clean_comments in (False, True):
            with self.subTest(clean_comments=clean_comments):
                self.ai.reset_mock()
                sql = generate_sql("-- #tìm nhân viên lương cao nhất", force_ai=True, clean_comments=clean_comments)
                self.ai.assert_called_once()
                self.assertTrue(sql.lstrip().startswith("--"), sql)
                self.assertRegex(sql, r"(?i)API|Gemini|key")
                self.assertNotRegex(sql, r"(?im)^\s*(SELECT|EXEC|CREATE|USE)\b")

    def test_ai_schema_creation_is_rejected_for_existing_data_requests(self):
        for generated_sql in (
            "CREATE DATABASE PayrollDemo;",
            "CREATE TABLE dbo.FakeEmployees (MaNV int); SELECT * FROM dbo.FakeEmployees;",
            "CREATE TRIGGER dbo.FakeTrigger ON dbo.NHANVIEN AFTER UPDATE AS SELECT 1;",
            "SELECT * INTO #FakeEmployees FROM dbo.NHANVIEN;",
        ):
            self.ai.return_value = generated_sql
            for clean_comments in (False, True):
                with self.subTest(generated_sql=generated_sql, clean_comments=clean_comments):
                    sql = generate_sql("-- #kiểm tra dữ liệu nhân viên có sẵn", force_ai=True, clean_comments=clean_comments)
                    self.assertTrue(sql.lstrip().startswith("--"), sql)
                    self.assertNotRegex(sql, r"(?im)^\s*(CREATE|ALTER|SELECT|USE)\b")

    def test_data_changes_and_report_requests_do_not_authorize_schema_creation(self):
        self.ai.return_value = "CREATE TABLE dbo.FakeEmployees (MaNV int); SELECT * FROM dbo.FakeEmployees;"
        for prompt in (
            "-- #sửa lương cơ bản của nhân viên 3742 thành 15000000",
            "-- #tạo báo cáo thống kê quỹ lương theo phòng ban",
        ):
            with self.subTest(prompt=prompt):
                sql = generate_sql(prompt, force_ai=True)
                self.assertTrue(sql.lstrip().startswith("--"), sql)
                self.assertNotRegex(sql, r"(?im)^\s*(CREATE|ALTER|SELECT|USE)\b")

    def test_ai_explicit_definition_change_is_allowed(self):
        self.ai.return_value = "CREATE TABLE dbo.RequestedReport (MaNV int);"
        sql = generate_sql("-- #tạo bảng dbo.RequestedReport gồm cột MaNV int", force_ai=True)
        self.assertIn("create table dbo.RequestedReport", sql)
        self.assert_uses_current_database(sql)

    def test_ai_creation_words_inside_literals_are_not_rejected(self):
        self.ai.return_value = "SELECT N'CREATE TABLE dbo.Example (ID int);' AS ScriptText;"
        sql = generate_sql("-- #hiển thị chuỗi văn bản", force_ai=True)
        self.assertIn("select N'CREATE TABLE dbo.Example (ID int);' as ScriptText;", sql)


if __name__ == "__main__":
    unittest.main()
