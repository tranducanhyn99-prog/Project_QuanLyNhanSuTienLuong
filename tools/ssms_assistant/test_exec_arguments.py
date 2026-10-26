import re
import unittest
from unittest.mock import patch

from sql_brain import generate_sql


class TestExecArguments(unittest.TestCase):
    REPORTED_SQL = (
        "exec dbo.sp_HuyChotBangLuong @MaBangLuong = "
        "(select top 1 MaBangLuong from BANGLUONG where TrangThai = 'DA_CHOT');"
    )

    def setUp(self):
        ai_patch = patch("sql_brain._call_gemini_fallback", return_value=None)
        self.ai = ai_patch.start()
        self.addCleanup(ai_patch.stop)

    def assert_variable_selected_before_exec(self, sql, procedure="sp_HuyChotBangLuong"):
        declaration = re.search(r"\bdeclare\s+(@\w+)\s+int\b", sql)
        self.assertIsNotNone(declaration, sql)
        variable = declaration.group(1)
        assignment = re.search(
            r"\bselect\s+(?:top\s*\(?\s*\d+\s*\)?\s+)?"
            + re.escape(variable) + r"\s*=",
            sql,
        )
        self.assertIsNotNone(assignment, sql)
        invocation = re.search(
            r"\bexec\s+dbo\." + re.escape(procedure)
            + r"\s+@MaBangLuong\s*=\s*" + re.escape(variable) + r"\b",
            sql,
        )
        self.assertIsNotNone(invocation, sql)
        self.assertLess(declaration.start(), assignment.start())
        self.assertLess(assignment.start(), invocation.start())
        self.assertNotRegex(sql, r"(?i)\bexec\s+dbo\.\w+\s+@MaBangLuong\s*=\s*\(\s*select\b")

    def assert_uses_existing_closed_payroll(self, sql):
        self.assertRegex(sql, r"\bfrom\s+(?:dbo\.)?BANGLUONG\b")
        self.assertIn("'DA_CHOT'", sql)
        self.assertNotRegex(sql, r"(?i)\b(?:create|insert)\b")
        self.assertNotRegex(sql, r"\b100[89]\b")
        self.assertNotRegex(sql, r"(?im)^\s*(?:use|go)\b")
        self.assertNotIn("sp_ChotBangLuong", sql)

    def test_reopening_synonyms_select_closed_existing_payroll_offline(self):
        for prompt in (
            "-- #mở khóa bảng lương đã chốt",
            "-- #mở khoá bảng lương đã chốt",
            "-- #bỏ chốt bảng lương",
        ):
            for clean_comments in (False, True):
                with self.subTest(prompt=prompt, clean_comments=clean_comments):
                    sql = generate_sql(prompt, clean_comments=clean_comments)
                    self.assert_variable_selected_before_exec(sql)
                    self.assert_uses_existing_closed_payroll(sql)
                    self.ai.assert_not_called()

    def test_reopening_synonym_honors_explicit_payroll_id(self):
        sql = generate_sql("-- #mở khóa bảng lương 3742 đã chốt", clean_comments=True)
        self.assert_variable_selected_before_exec(sql)
        self.assertRegex(sql, r"\bMaBangLuong\s*=\s*3742\b")
        self.assertNotRegex(sql, r"\b100[89]\b")
        self.ai.assert_not_called()

    def test_reported_ai_subquery_argument_is_assigned_before_exec(self):
        self.ai.return_value = self.REPORTED_SQL
        for clean_comments in (False, True):
            with self.subTest(clean_comments=clean_comments):
                sql = generate_sql("-- #mở khóa bảng lương đã chốt", force_ai=True, clean_comments=clean_comments)
                self.assert_variable_selected_before_exec(sql)
                self.assert_uses_existing_closed_payroll(sql)

    def test_invalid_exec_text_inside_literals_and_comments_is_untouched(self):
        comment = "-- " + self.REPORTED_SQL + "\n/* " + self.REPORTED_SQL + " */\n"
        literal = "N'" + self.REPORTED_SQL.replace("'", "''") + "'"
        self.ai.return_value = comment + "select " + literal + " as ExampleText;"
        sql = generate_sql("-- #hiển thị đoạn SQL dưới dạng văn bản", force_ai=True, clean_comments=False)
        self.assertEqual(self.ai.return_value, sql)
        self.assertNotRegex(sql, r"(?im)^\s*declare\b")

    def test_valid_variable_exec_is_unchanged(self):
        valid_sql = (
            "declare @MaBangLuong int;\n"
            "select top (1) @MaBangLuong = MaBangLuong\n"
            "from dbo.BANGLUONG\n"
            "where TrangThai = 'DA_CHOT'\n"
            "order by Nam desc, Thang desc;\n"
            "exec dbo.sp_HuyChotBangLuong @MaBangLuong = @MaBangLuong;"
        )
        self.ai.return_value = valid_sql
        for clean_comments in (False, True):
            with self.subTest(clean_comments=clean_comments):
                sql = generate_sql("-- #mở khóa bảng lương đã chốt", force_ai=True, clean_comments=clean_comments)
                self.assertEqual(valid_sql, sql)


if __name__ == "__main__":
    unittest.main()
