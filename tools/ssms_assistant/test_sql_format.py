import re
import unittest

from sql_format import prepare_sql


class TestSqlFormat(unittest.TestCase):
    def test_keywords_are_lowercase_while_names_and_status_values_are_exact(self):
        source = (
            "SeLeCt TOP (3) MaNV, HoTen, ThucNhan\n"
            "FROM dbo.vw_BangLuongChiTiet\n"
            "WHERE TrangThaiBangLuong = 'DA_CHOT'\n"
            "ORDER BY ThucNhan DESC;"
        )
        expected = (
            "select top (3) MaNV, HoTen, ThucNhan\n"
            "from dbo.vw_BangLuongChiTiet\n"
            "where TrangThaiBangLuong = 'DA_CHOT'\n"
            "order by ThucNhan desc;"
        )
        self.assertEqual(expected, prepare_sql(source))

    def test_literals_quoted_identifiers_and_nested_comments_keep_exact_case(self):
        comment = "-- SELECT KEEP Comment\n/* OUTER /* INNER UPDATE */ END */\n"
        source_query = (
            "SELECT N'Tiếng Việt SELECT -- /* không đổi */', 'It''s MiXeD',\n"
            '       [SeLeCt]]Name], "FrOm""Name" FROM dbo.NHANVIEN;'
        )
        expected_query = (
            "select N'Tiếng Việt SELECT -- /* không đổi */', 'It''s MiXeD',\n"
            '       [SeLeCt]]Name], "FrOm""Name" from dbo.NHANVIEN;'
        )
        self.assertEqual(comment + expected_query, prepare_sql(comment + source_query))
        cleaned = prepare_sql(comment + source_query, clean_comments=True)
        self.assertIn(expected_query, cleaned)
        self.assertNotIn("KEEP Comment", cleaned)
        self.assertNotIn("OUTER", cleaned)

    def test_variable_and_qualified_identifier_names_that_resemble_keywords_are_preserved(self):
        source = (
            "DECLARE @SELECT int = 1;\n"
            "SELECT @SELECT, dbo.SELECT, Report.UPDATE, [WHERE], \"ORDER\"\n"
            "FROM #SELECT;"
        )
        expected = (
            "declare @SELECT int = 1;\n"
            "select @SELECT, dbo.SELECT, Report.UPDATE, [WHERE], \"ORDER\"\n"
            "from #SELECT;"
        )
        self.assertEqual(expected, prepare_sql(source))

    def test_function_name_formatting_does_not_change_columns_or_aliases(self):
        source = "SELECT COUNT(*) AS Count, Date, SUM(Amount) AS Sum FROM dbo.Report;"
        sql = prepare_sql(source)
        self.assertRegex(
            sql,
            r"^select (?i:COUNT)\(\*\) as Count, Date, (?i:SUM)\(Amount\) as Sum from dbo\.Report;$",
        )

    def test_dynamic_sql_literal_keeps_original_sql_bytes(self):
        dynamic_literal = "N'SELECT MaNV FROM dbo.NHANVIEN WHERE TrangThai = ''DANG_LAM_VIEC'';'"
        source = "DECLARE @Sql nvarchar(max) = " + dynamic_literal + ";\nEXEC sys.sp_executesql @Sql;"
        expected = "declare @Sql nvarchar(max) = " + dynamic_literal + ";\nexec sys.sp_executesql @Sql;"
        self.assertEqual(expected, prepare_sql(source))

    def test_contextual_words_used_as_columns_and_aliases_keep_their_case(self):
        self.assertEqual(
            "select Role, Output, Login, Password from dbo.Report;",
            prepare_sql("SELECT Role, Output, Login, Password FROM dbo.Report;"),
        )
        self.assertEqual(
            "select 1 as Role, 2 as Password, 3 as Output, 4 as Login;",
            prepare_sql("SELECT 1 AS Role, 2 AS Password, 3 AS Output, 4 AS Login;"),
        )

    def test_declaration_and_cast_types_do_not_change_columns_in_expressions(self):
        source = "DECLARE @x DATE = (SELECT @v + Date FROM dbo.Report);"
        self.assertEqual("declare @x date = (select @v + Date from dbo.Report);", prepare_sql(source))
        self.assertEqual(
            "select cast(Date as date) as Date, convert(decimal(18,2), Amount) as Amount;",
            prepare_sql("SELECT CAST(Date AS DATE) AS Date, CONVERT(DECIMAL(18,2), Amount) AS Amount;"),
        )
        self.assertEqual("declare @Sql nvarchar(max);", prepare_sql("DECLARE @Sql NVARCHAR(MAX);"))

    def test_cleanup_removes_context_commands_and_keeps_lowercase_module_boundaries(self):
        source = (
            "USE [ExistingDatabase];\nGO\nSELECT 1;\nGO\n"
            "ALTER VIEW dbo.MixedView AS SELECT N'GO' AS Marker;\nGO\nSELECT 2;\nGO"
        )
        sql = prepare_sql(source)
        self.assertNotRegex(sql, r"(?im)^\s*USE\b")
        self.assertNotRegex(sql, r"(?m)^\s*GO\b")
        batches = [part.strip() for part in re.split(r"(?m)^\s*go\s*$", sql) if part.strip()]
        self.assertEqual([
            "select 1;",
            "alter view dbo.MixedView as select N'GO' as Marker;",
            "select 2;",
        ], batches)

    def test_multiline_literal_is_not_treated_as_executable_keywords(self):
        literal = "N'FIRST\nGO\nUSE [OtherDatabase];\nSELECT FROM WHERE\nLAST'"
        self.assertEqual("select " + literal + " as TextValue;", prepare_sql("SELECT " + literal + " AS TextValue;"))


if __name__ == "__main__":
    unittest.main()
