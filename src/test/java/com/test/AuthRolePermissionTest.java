package com.test;

import com.model.TaiKhoan;
import com.session.Session;
import com.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * AuthRolePermissionTest – Bộ kiểm thử phân quyền và Session cho 4 vai trò:
 * 1. DB_Admin
 * 2. HR_Manager
 * 3. Payroll_Officer
 * 4. Employee
 *
 * Kiểm tra các tiêu chí:
 * - TaiKhoan đầy đủ 5 getters: MaTK, MaNV, TenDangNhap, VaiTro, HoTenNV
 * - Session lưu trữ chính xác thông tin đăng nhập từ TaiKhoan
 * - MainFrame hiển thị đúng Tên đăng nhập / Người dùng và Vai trò (không còn "Khách")
 * - Phân quyền hiển thị MenuBar theo ma trận phân quyền TV5
 */
public class AuthRolePermissionTest {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ PHÂN QUYỀN VÀ SESSION (TV5)");
        System.out.println("==============================================================");

        try {
            testTaiKhoanGetters();
            testSessionLoginTaiKhoan();
            testRoleAdmin();
            testRoleHRManager();
            testRolePayrollOfficer();
            testRoleEmployee();
            testLogout();

            System.out.println("==============================================================");
            System.out.println(String.format("   KẾT QUẢ: %d/%d testcase ĐÃ ĐẠT (PASSED)", passedTests, totalTests));
            System.out.println("==============================================================");

            if (passedTests == totalTests) {
                System.out.println(">>> TẤT CẢ KIỂM THỬ PHÂN QUYỀN VÀ SESSION ĐỀU THÀNH CÔNG! <<<");
                System.exit(0);
            } else {
                System.err.println(">>> CÓ KIỂM THỬ THẤT BẠI! <<<");
                System.exit(1);
            }
        } catch (Exception ex) {
            System.err.println("Lỗi không mong muốn trong quá trình kiểm thử: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertTrue(String message, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + message);
        } else {
            System.err.println("  [FAIL] " + message);
        }
    }

    private static void assertEquals(String message, Object expected, Object actual) {
        totalTests++;
        if (expected == null && actual == null) {
            passedTests++;
            System.out.println("  [PASS] " + message);
        } else if (expected != null && expected.equals(actual)) {
            passedTests++;
            System.out.println("  [PASS] " + message);
        } else {
            System.err.println(String.format("  [FAIL] %s - Kì vọng: '%s', Thực tế: '%s'", message, expected, actual));
        }
    }

    // ─── 1. Kiểm tra TaiKhoan getters ────────────────────────────────
    private static void testTaiKhoanGetters() {
        System.out.println("\n--- 1. Kiểm tra TaiKhoan getters ---");
        TaiKhoan tk = new TaiKhoan(10, 100, "test_user", "HR_Manager", "Nguyễn Văn Test", "HOAT_DONG");

        assertEquals("TaiKhoan.getMaTK()", 10, tk.getMaTK());
        assertEquals("TaiKhoan.getMaNV()", 100, tk.getMaNV());
        assertEquals("TaiKhoan.getTenDangNhap()", "test_user", tk.getTenDangNhap());
        assertEquals("TaiKhoan.getVaiTro()", "HR_Manager", tk.getVaiTro());
        assertEquals("TaiKhoan.getHoTenNV()", "Nguyễn Văn Test", tk.getHoTenNV());
        assertTrue("TaiKhoan.isActive()", tk.isActive());
        assertTrue("TaiKhoan.isLocked()", !tk.isLocked());
    }

    // ─── 2. Kiểm tra Session login bằng TaiKhoan ─────────────────────
    private static void testSessionLoginTaiKhoan() {
        System.out.println("\n--- 2. Kiểm tra Session.login(TaiKhoan) ---");
        TaiKhoan tk = new TaiKhoan(1, 1, "test_admin", "DB_Admin", null, "HOAT_DONG");
        Session session = Session.getInstance();
        session.login(tk);

        assertTrue("Session.isLoggedIn() phải là true", session.isLoggedIn());
        assertEquals("Session.getMaTK()", 1, session.getMaTK());
        assertEquals("Session.getTenDangNhap()", "test_admin", session.getTenDangNhap());
        assertEquals("Session.getVaiTro()", "DB_Admin", session.getVaiTro());
        assertEquals("Session.getUserInfo() khi chưa có họ tên NV", "test_admin", session.getUserInfo());
        assertEquals("Session.getVaiTroDisplayName()", "Quản trị viên", session.getVaiTroDisplayName());
        assertEquals("Session.getFullRoleDisplayName()", "Quản trị viên (DB_Admin)", session.getFullRoleDisplayName());
    }

    // ─── 3. Kiểm tra Role: DB_Admin ─────────────────────────────────
    private static void testRoleAdmin() {
        System.out.println("\n--- 3. Kiểm tra Role: DB_Admin ---");
        TaiKhoan admin = new TaiKhoan(1, -1, "admin", "DB_Admin", null, "HOAT_DONG");
        Session session = Session.getInstance();
        session.login(admin);

        MainFrame mainFrame = new MainFrame();

        // Kiểm tra thông tin người dùng và vai trò
        assertEquals("MainFrame User Label", "Người dùng: admin", mainFrame.getLblStatusUser().getText());
        assertEquals("MainFrame Role Label", "Vai trò: Quản trị viên (DB_Admin)", mainFrame.getLblStatusRole().getText());
        assertTrue("MainFrame Title chứa admin", mainFrame.getTitle().contains("admin"));
        assertTrue("MainFrame Title chứa Quản trị viên (DB_Admin)", mainFrame.getTitle().contains("Quản trị viên (DB_Admin)"));
        assertTrue("MainFrame Title KHÔNG chứa 'Khách'", !mainFrame.getTitle().contains("Khách"));

        // Kiểm tra Menu hiển thị cho DB_Admin: Toàn quyền (7 menu đều hiển thị)
        assertTrue("Menu Nhân viên phải hiển thị cho DB_Admin", mainFrame.getMenuNhanVien().isVisible());
        assertTrue("Menu Danh mục phải hiển thị cho DB_Admin", mainFrame.getMenuDanhMuc().isVisible());
        assertTrue("Menu Chấm công phải hiển thị cho DB_Admin", mainFrame.getMenuChamCong().isVisible());
        assertTrue("Menu Phụ cấp / Khấu trừ phải hiển thị cho DB_Admin", mainFrame.getMenuPhuCapKhauTru().isVisible());
        assertTrue("Menu Lương phải hiển thị cho DB_Admin", mainFrame.getMenuLuong().isVisible());
        assertTrue("Menu Báo cáo phải hiển thị cho DB_Admin", mainFrame.getMenuBaoCao().isVisible());
        assertTrue("Menu Quản trị phải hiển thị cho DB_Admin", mainFrame.getMenuQuanTri().isVisible());

        mainFrame.dispose();
    }

    // ─── 4. Kiểm tra Role: HR_Manager ────────────────────────────────
    private static void testRoleHRManager() {
        System.out.println("\n--- 4. Kiểm tra Role: HR_Manager ---");
        TaiKhoan hr = new TaiKhoan(2, 1, "hr_manager", "HR_Manager", "Nguyễn Văn HR", "HOAT_DONG");
        Session session = Session.getInstance();
        session.login(hr);

        MainFrame mainFrame = new MainFrame();

        // Kiểm tra thông tin hiển thị
        assertEquals("MainFrame User Label", "Người dùng: Nguyễn Văn HR (hr_manager)", mainFrame.getLblStatusUser().getText());
        assertEquals("MainFrame Role Label", "Vai trò: Quản lý nhân sự (HR_Manager)", mainFrame.getLblStatusRole().getText());
        assertTrue("MainFrame Title chứa tên và username", mainFrame.getTitle().contains("Nguyễn Văn HR (hr_manager)"));
        assertTrue("MainFrame Title chứa Quản lý nhân sự (HR_Manager)", mainFrame.getTitle().contains("Quản lý nhân sự (HR_Manager)"));
        assertTrue("MainFrame Title KHÔNG chứa 'Khách'", !mainFrame.getTitle().contains("Khách"));

        // Kiểm tra Menu: Thấy Nhân viên, Danh mục, Chấm công, Phụ cấp, Báo cáo; Ẩn Lương và Quản trị
        assertTrue("Menu Nhân viên phải hiển thị cho HR_Manager", mainFrame.getMenuNhanVien().isVisible());
        assertTrue("Menu Danh mục phải hiển thị cho HR_Manager", mainFrame.getMenuDanhMuc().isVisible());
        assertTrue("Menu Chấm công phải hiển thị cho HR_Manager", mainFrame.getMenuChamCong().isVisible());
        assertTrue("Menu Phụ cấp / Khấu trừ phải hiển thị cho HR_Manager", mainFrame.getMenuPhuCapKhauTru().isVisible());
        assertTrue("Menu Báo cáo phải hiển thị cho HR_Manager", mainFrame.getMenuBaoCao().isVisible());
        assertTrue("Menu Lương PHẢI ẨN đối với HR_Manager", !mainFrame.getMenuLuong().isVisible());
        assertTrue("Menu Quản trị PHẢI ẨN đối với HR_Manager", !mainFrame.getMenuQuanTri().isVisible());

        mainFrame.dispose();
    }

    // ─── 5. Kiểm tra Role: Payroll_Officer ───────────────────────────
    private static void testRolePayrollOfficer() {
        System.out.println("\n--- 5. Kiểm tra Role: Payroll_Officer ---");
        TaiKhoan payroll = new TaiKhoan(3, 2, "payroll_officer", "Payroll_Officer", "Trần Thị Payroll", "HOAT_DONG");
        Session session = Session.getInstance();
        session.login(payroll);

        MainFrame mainFrame = new MainFrame();

        // Kiểm tra thông tin hiển thị
        assertEquals("MainFrame User Label", "Người dùng: Trần Thị Payroll (payroll_officer)", mainFrame.getLblStatusUser().getText());
        assertEquals("MainFrame Role Label", "Vai trò: Nhân viên kế toán lương (Payroll_Officer)", mainFrame.getLblStatusRole().getText());
        assertTrue("MainFrame Title chứa tên và username", mainFrame.getTitle().contains("Trần Thị Payroll (payroll_officer)"));
        assertTrue("MainFrame Title chứa vai trò kế toán lương", mainFrame.getTitle().contains("Nhân viên kế toán lương (Payroll_Officer)"));
        assertTrue("MainFrame Title KHÔNG chứa 'Khách'", !mainFrame.getTitle().contains("Khách"));

        // Kiểm tra Menu: Thấy Chấm công (chỉ xem), Phụ cấp, Lương, Báo cáo; Ẩn Nhân viên, Danh mục, Quản trị
        assertTrue("Menu Nhân viên PHẢI ẨN đối với Payroll_Officer", !mainFrame.getMenuNhanVien().isVisible());
        assertTrue("Menu Danh mục PHẢI ẨN đối với Payroll_Officer", !mainFrame.getMenuDanhMuc().isVisible());
        assertTrue("Menu Chấm công phải hiển thị cho Payroll_Officer", mainFrame.getMenuChamCong().isVisible());
        assertTrue("Nhập chấm công (item 0) PHẢI DISABLE đối với Payroll_Officer", !mainFrame.getMenuChamCong().getItem(0).isEnabled());
        assertTrue("Menu Phụ cấp / Khấu trừ phải hiển thị cho Payroll_Officer", mainFrame.getMenuPhuCapKhauTru().isVisible());
        assertTrue("Menu Lương phải hiển thị cho Payroll_Officer", mainFrame.getMenuLuong().isVisible());
        assertTrue("Menu Báo cáo phải hiển thị cho Payroll_Officer", mainFrame.getMenuBaoCao().isVisible());
        assertTrue("Menu Quản trị PHẢI ẨN đối với Payroll_Officer", !mainFrame.getMenuQuanTri().isVisible());

        mainFrame.dispose();
    }

    // ─── 6. Kiểm tra Role: Employee ──────────────────────────────────
    private static void testRoleEmployee() {
        System.out.println("\n--- 6. Kiểm tra Role: Employee ---");
        TaiKhoan employee = new TaiKhoan(4, 3, "employee01", "Employee", "Lê Văn Nhân Viên", "HOAT_DONG");
        Session session = Session.getInstance();
        session.login(employee);

        MainFrame mainFrame = new MainFrame();

        // Kiểm tra thông tin hiển thị
        assertEquals("MainFrame User Label", "Người dùng: Lê Văn Nhân Viên (employee01)", mainFrame.getLblStatusUser().getText());
        assertEquals("MainFrame Role Label", "Vai trò: Nhân viên (Employee)", mainFrame.getLblStatusRole().getText());
        assertTrue("MainFrame Title chứa tên và username", mainFrame.getTitle().contains("Lê Văn Nhân Viên (employee01)"));
        assertTrue("MainFrame Title chứa vai trò Nhân viên (Employee)", mainFrame.getTitle().contains("Nhân viên (Employee)"));
        assertTrue("MainFrame Title KHÔNG chứa 'Khách'", !mainFrame.getTitle().contains("Khách"));

        // Kiểm tra Menu: Chỉ thấy Báo cáo (và trong Báo cáo ẩn Báo cáo tổng hợp), tất cả menu khác ẩn
        assertTrue("Menu Nhân viên PHẢI ẨN đối với Employee", !mainFrame.getMenuNhanVien().isVisible());
        assertTrue("Menu Danh mục PHẢI ẨN đối với Employee", !mainFrame.getMenuDanhMuc().isVisible());
        assertTrue("Menu Chấm công PHẢI ẨN đối với Employee", !mainFrame.getMenuChamCong().isVisible());
        assertTrue("Menu Phụ cấp / Khấu trừ PHẢI ẨN đối với Employee", !mainFrame.getMenuPhuCapKhauTru().isVisible());
        assertTrue("Menu Lương PHẢI ẨN đối với Employee", !mainFrame.getMenuLuong().isVisible());
        assertTrue("Menu Báo cáo phải hiển thị cho Employee", mainFrame.getMenuBaoCao().isVisible());
        assertTrue("Báo cáo tổng hợp (item 0) PHẢI ẨN đối với Employee", !mainFrame.getMenuBaoCao().getItem(0).isVisible());
        assertTrue("Menu Quản trị PHẢI ẨN đối với Employee", !mainFrame.getMenuQuanTri().isVisible());

        mainFrame.dispose();
    }

    // ─── 7. Kiểm tra Logout ──────────────────────────────────────────
    private static void testLogout() {
        System.out.println("\n--- 7. Kiểm tra Logout ---");
        Session session = Session.getInstance();
        session.logout();

        assertTrue("Session.isLoggedIn() phải là false sau logout", !session.isLoggedIn());
        assertEquals("Session.getUserInfo() sau logout", "Khách", session.getUserInfo());
        assertEquals("Session.getFullRoleDisplayName() sau logout", "", session.getFullRoleDisplayName());
    }
}
