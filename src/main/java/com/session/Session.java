package com.session;

/**
 * Session – Singleton lưu trữ thông tin phiên đăng nhập.
 *
 * Được thiết lập sau khi AuthService xác thực thành công.
 * MainFrame và các Panel sử dụng Session để ẩn/hiện chức năng theo vai trò.
 *
 * @author Trần Đức Anh (TV5 – MSSV 24110155)
 */
public class Session {

    private static Session instance;

    private int    maTK;
    private int    maNV;           // -1 nếu là DB_Admin không gắn nhân viên
    private String tenDangNhap;
    private String vaiTro;         // "DB_Admin" | "HR_Manager" | "Payroll_Officer" | "Employee"
    private String hoTenNV;        // Họ tên nhân viên (null nếu DB_Admin hệ thống)

    // ─── Constructor (private – Singleton) ───────────────────────────

    private Session() {
        reset();
    }

    // ─── Singleton accessor ──────────────────────────────────────────

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    // ─── Lifecycle ───────────────────────────────────────────────────

    /**
     * Gọi sau khi đăng nhập thành công.
     */
    public void login(int maTK, int maNV, String tenDangNhap, String vaiTro, String hoTenNV) {
        this.maTK        = maTK;
        this.maNV        = maNV;
        this.tenDangNhap = tenDangNhap;
        this.vaiTro      = vaiTro;
        this.hoTenNV     = hoTenNV;
    }

    /**
     * Gọi khi đăng xuất – xóa sạch thông tin phiên.
     */
    public void logout() {
        reset();
    }

    private void reset() {
        maTK        = 0;
        maNV        = -1;
        tenDangNhap = null;
        vaiTro      = null;
        hoTenNV     = null;
    }

    // ─── Kiểm tra quyền ─────────────────────────────────────────────

    /**
     * Kiểm tra vai trò hiện tại có nằm trong danh sách cho phép không.
     * Dùng trong MainFrame/Panel để ẩn/hiện menu và disable nút.
     *
     * Ví dụ: session.hasRole("DB_Admin", "HR_Manager")
     */
    public boolean hasRole(String... roles) {
        if (vaiTro == null) return false;
        for (String r : roles) {
            if (vaiTro.equals(r)) return true;
        }
        return false;
    }

    /**
     * Kiểm tra đã đăng nhập hay chưa.
     */
    public boolean isLoggedIn() {
        return tenDangNhap != null && vaiTro != null;
    }

    // ─── Getters ─────────────────────────────────────────────────────

    public int getMaTK() {
        return maTK;
    }

    public int getMaNV() {
        return maNV;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public String getHoTenNV() {
        return hoTenNV;
    }

    /**
     * Trả về tên hiển thị cho UI (ví dụ: thanh tiêu đề, label chào).
     */
    public String getDisplayName() {
        if (hoTenNV != null && !hoTenNV.trim().isEmpty()) {
            return hoTenNV;
        }
        return tenDangNhap != null ? tenDangNhap : "Khách";
    }

    /**
     * Trả về tên vai trò tiếng Việt cho hiển thị.
     */
    public String getVaiTroDisplayName() {
        if (vaiTro == null) return "";
        switch (vaiTro) {
            case "DB_Admin":        return "Quản trị viên";
            case "HR_Manager":      return "Quản lý nhân sự";
            case "Payroll_Officer": return "Nhân viên kế toán lương";
            case "Employee":        return "Nhân viên";
            default:                return vaiTro;
        }
    }
}
