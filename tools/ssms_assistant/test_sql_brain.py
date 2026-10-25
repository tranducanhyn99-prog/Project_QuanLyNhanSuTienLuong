import unittest
from sql_brain import generate_sql


class TestSqlBrain(unittest.TestCase):
    def test_dynamic_grant_sp_to_role(self):
        sql = generate_sql("-- #cấp quyền chốt lương cho hr")
        self.assertIn("GRANT EXECUTE ON dbo.sp_ChotBangLuong TO role_HRManager", sql)
        self.assertIn("REVOKE EXECUTE ON dbo.sp_ChotBangLuong FROM role_HRManager", sql)

    def test_dynamic_deny_table_to_role(self):
        sql = generate_sql("-- #cấm kế toán sửa xóa bảng nhân viên")
        self.assertIn("DENY", sql)
        self.assertIn("dbo.NHANVIEN", sql)
        self.assertIn("role_PayrollOfficer", sql)

    def test_dynamic_revoke_permission(self):
        sql = generate_sql("-- #thu hoi quyen select tren bangluong cua nhan vien")
        self.assertIn("REVOKE SELECT ON dbo.BANGLUONG FROM role_Employee", sql)

    def test_create_login_and_role(self):
        sql = generate_sql("-- #tạo login test_gv và gán vào role hr")
        self.assertIn("CREATE LOGIN [test_gv]", sql)
        self.assertIn("CREATE USER [test_gv] FOR LOGIN [test_gv]", sql)
        self.assertIn("ALTER ROLE role_HRManager ADD MEMBER [test_gv]", sql)

    def test_rbac_sections(self):
        self.assertIn("EXECUTE AS LOGIN = 'gui_hr'", generate_sql("-- #test quyền hr"))
        self.assertIn("EXECUTE AS LOGIN = 'gui_payroll'", generate_sql("-- #test quyền kế toán"))
        self.assertIn("EXECUTE AS LOGIN = 'gui_a'", generate_sql("-- #test quyền nhân viên"))

    def test_rls_view_and_da_chot_modification(self):
        sql = generate_sql("-- #test rls phiếu lương cá nhân")
        self.assertIn("vw_PhieuLuongCaNhan", sql)
        self.assertIn("gui_a", sql)
        self.assertIn("gui_b", sql)

        sql_mod = generate_sql("-- #sửa view rls chỉ xem kỳ đã chốt")
        self.assertIn("CREATE OR ALTER VIEW dbo.vw_PhieuLuongCaNhan", sql_mod)
        self.assertIn("TrangThaiBangLuong = 'DA_CHOT'", sql_mod)

    def test_sp_chot_and_mo_luong(self):
        sql = generate_sql("-- #chốt bảng lương 1009")
        self.assertIn("EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009", sql)
        sql_reopen = generate_sql("-- #mở lại kỳ lương")
        self.assertIn("EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = 1009", sql_reopen)

    def test_triggers_tv5(self):
        self.assertIn("dbo.CHAMCONG", generate_sql("-- #test trigger chấm công kỳ đã chốt"))
        self.assertIn("dbo.PHUCAPNHANVIEN", generate_sql("-- #test trigger phụ cấp"))
        self.assertIn("dbo.KHAUTRUNHANVIEN", generate_sql("-- #test trigger khấu trừ"))
        self.assertIn("dbo.CHITIETBANGLUONG", generate_sql("-- #test trigger sửa chi tiết lương"))
        self.assertIn("DELETE FROM dbo.NHANVIEN", generate_sql("-- #test trigger xóa nhân viên"))
        self.assertIn("DISABLE TRIGGER", generate_sql("-- #tắt trigger chấm công"))

    def test_function_thuc_nhan_and_clamp_zero(self):
        sql = generate_sql("-- #test hàm tính thực nhận")
        self.assertIn("dbo.fn_TinhThucNhan", sql)
        sql_clamp = generate_sql("-- #sửa hàm thực nhận âm thì về 0")
        self.assertIn("CREATE OR ALTER FUNCTION dbo.fn_TinhThucNhan", sql_clamp)
        self.assertIn("WHEN @KetQua < 0 THEN 0", sql_clamp)

    def test_index_benchmark_and_10k(self):
        sql = generate_sql("-- #đo hiệu năng index")
        self.assertIn("IX_NHANVIEN_MaPB_MaCV", sql)
        sql_10k = generate_sql("-- #benchmark 10000 dòng bảng tạm")
        self.assertIn("#BenchmarkNhanVien", sql_10k)

    def test_concurrency_and_blocking(self):
        self.assertIn("WITH (UPDLOCK, HOLDLOCK)", generate_sql("-- #concurrency cửa sổ 1"))
        self.assertIn("sys.dm_exec_requests", generate_sql("-- #xem session nào đang block"))

    def test_constraints_and_queries(self):
        self.assertIn("CHK_BANGLUONG_Thang", generate_sql("-- #test check tháng 13"))
        self.assertIn("GROUP BY TenPB", generate_sql("-- #thống kê quỹ lương theo phòng ban"))
        self.assertIn("TOP (1) WITH TIES", generate_sql("-- #tìm nhân viên lương cao nhất"))
        self.assertIn("sp_helptext", generate_sql("-- #xem code sp_ChotBangLuong"))
        self.assertIn("REVERT;", generate_sql("-- #reset"))


if __name__ == "__main__":
    unittest.main()
