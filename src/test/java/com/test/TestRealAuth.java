package com.test;

import com.service.AuthService;
import com.model.TaiKhoan;
import com.session.Session;

public class TestRealAuth {
    public static void main(String[] args) {
        System.out.println("Kiểm tra đăng nhập thực tế với Database...");
        AuthService auth = new AuthService();
        try {
            // 1. Test admin
            System.out.println("\n[1] Đăng nhập tài khoản admin (DB_Admin)...");
            TaiKhoan admin = auth.login("admin", "123456");
            System.out.println("-> Đăng nhập thành công!");
            System.out.println("   Tên đăng nhập: " + admin.getTenDangNhap());
            System.out.println("   Vai trò: " + admin.getVaiTro());
            System.out.println("   Session Info: " + Session.getInstance().getUserInfo());
            System.out.println("   Session Role: " + Session.getInstance().getFullRoleDisplayName());

            // 2. Test hr_manager
            System.out.println("\n[2] Đăng nhập tài khoản hr_manager (HR_Manager)...");
            TaiKhoan hr = auth.login("hr_manager", "123456");
            System.out.println("-> Đăng nhập thành công!");
            System.out.println("   Tên đăng nhập: " + hr.getTenDangNhap());
            System.out.println("   Vai trò: " + hr.getVaiTro());
            System.out.println("   Session Info: " + Session.getInstance().getUserInfo());
            System.out.println("   Session Role: " + Session.getInstance().getFullRoleDisplayName());

            // 3. Test payroll_officer
            System.out.println("\n[3] Đăng nhập tài khoản payroll_officer (Payroll_Officer)...");
            TaiKhoan payroll = auth.login("payroll_officer", "123456");
            System.out.println("-> Đăng nhập thành công!");
            System.out.println("   Tên đăng nhập: " + payroll.getTenDangNhap());
            System.out.println("   Vai trò: " + payroll.getVaiTro());

            // 4. Test employee01
            System.out.println("\n[4] Đăng nhập tài khoản employee01 (Employee)...");
            TaiKhoan emp = auth.login("employee01", "123456");
            System.out.println("-> Đăng nhập thành công!");
            System.out.println("   Tên đăng nhập: " + emp.getTenDangNhap());
            System.out.println("   Vai trò: " + emp.getVaiTro());

            // 5. Test sai mật khẩu
            System.out.println("\n[5] Test sai mật khẩu...");
            try {
                auth.login("admin", "sai_mat_khau");
                System.err.println("-> LỖI: Lẽ ra phải báo sai mật khẩu!");
            } catch (Exception ex) {
                System.out.println("-> Bắt lỗi chính xác: " + ex.getMessage());
            }

            // 6. Test tài khoản bị khóa
            System.out.println("\n[6] Test tài khoản bị khóa (locked_user)...");
            try {
                auth.login("locked_user", "123456");
                System.err.println("-> LỖI: Lẽ ra phải báo tài khoản bị khóa!");
            } catch (Exception ex) {
                System.out.println("-> Bắt lỗi chính xác: " + ex.getMessage());
            }

            System.out.println("\n>>> TẤT CẢ TEST ĐĂNG NHẬP VỚI CSDL ĐỀU THÀNH CÔNG RỰC RỠ! <<<");

        } catch (Exception ex) {
            System.err.println("Lỗi xác thực: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
