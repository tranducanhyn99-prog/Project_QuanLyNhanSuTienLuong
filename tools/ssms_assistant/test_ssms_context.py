import os
import unittest

if os.name == "nt":
    from ssms_daemon import parse_ssms_connection


@unittest.skipUnless(os.name == "nt", "The SSMS daemon uses Windows APIs")
class TestSsmsConnectionTitle(unittest.TestCase):
    def test_current_local_query_connection(self):
        title = (
            "SQLQuery1.sql - (local).QuanLyNhanSuTienLuong "
            "(VINHNGUYEN\\quangvinh (72))* - Microsoft SQL Server Management Studio"
        )
        self.assertEqual(parse_ssms_connection(title), ("(local)", "QuanLyNhanSuTienLuong"))

    def test_repeated_actual_document_title_does_not_become_server_name(self):
        document_title = (
            "SQLQuery1.sql - (local).QuanLyNhanSuTienLuong "
            "(VINHNGUYEN\\quangvinh (72))*"
        )
        title = document_title + " - " + document_title + " - Microsoft SQL Server Management Studio"
        self.assertEqual(parse_ssms_connection(title), ("(local)", "QuanLyNhanSuTienLuong"))

    def test_named_servers_ports_and_dotted_hosts(self):
        for server in ("localhost", "127.0.0.1", r"HOST\SQLEXPRESS",
                       r"(localdb)\MSSQLLocalDB", "sql.example.test", "tcp:sql.example.test,1433"):
            with self.subTest(server=server):
                title = f"SQLQuery2.sql - {server}.ExistingDb (db_user (51)) - Microsoft SQL Server Management Studio"
                self.assertEqual(parse_ssms_connection(title), (server, "ExistingDb"))

    def test_saved_script_filenames_and_database_with_spaces(self):
        for filename in ("04_Module_TinhLuong_TV4.sql", r"D:\Payroll Scripts\October - report.sql", "REPORT.SQL"):
            with self.subTest(filename=filename):
                title = f"{filename} - SERVER.Payroll Database (DOMAIN\\user.name (14))* - Microsoft SQL Server Management Studio"
                self.assertEqual(parse_ssms_connection(title), ("SERVER", "Payroll Database"))

    def test_unconnected_or_other_application_titles_are_rejected(self):
        for title in ("", "Microsoft SQL Server Management Studio",
                      "SQLQuery1.sql - not connected - Microsoft SQL Server Management Studio",
                      "04_Module_TinhLuong_TV4.sql - Visual Studio Code", "PowerShell"):
            with self.subTest(title=title):
                self.assertIsNone(parse_ssms_connection(title))


if __name__ == "__main__":
    unittest.main()
