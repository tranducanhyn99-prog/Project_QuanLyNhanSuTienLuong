package com.service;

import com.dao.TaiKhoanDAO;
import com.model.TaiKhoan;
import com.session.Session;
import com.util.PasswordUtil;

import java.sql.SQLException;

/**
 * AuthService – Dịch vụ xác thực đăng nhập.
 *
 * Luồng xử lý:
 * 1. Nhận TenDangNhap + MatKhau (plaintext) từ LoginFrame
 * 2. Hash mật khẩu bằng SHA-256 (PasswordUtil)
 * 3. Gọi TaiKhoanDAO.findByCredentials() để so sánh với CSDL
 * 4. Kiểm tra trạng thái tài khoản (HOAT_DONG / KHOA)
 * 5. Thiết lập Session singleton nếu thành công
 *
 * @author Trần Đức Anh (TV5 – MSSV 24110155)
 */
public class AuthService {

    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    /**
     * Xác thực đăng nhập.
     *
     * @param tenDangNhap Tên đăng nhập do người dùng nhập
     * @param matKhauPlainText Mật khẩu dạng plaintext do người dùng nhập
     * @return TaiKhoan đã xác thực thành công
     * @throws Exception nếu sai thông tin, tài khoản bị khóa, hoặc lỗi hệ thống
     */
    public TaiKhoan login(String tenDangNhap, String matKhauPlainText) throws Exception {
        // 1. Validate đầu vào
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            throw new Exception("Tên đăng nhập không được để trống!");
        }
        if (matKhauPlainText == null || matKhauPlainText.trim().isEmpty()) {
            throw new Exception("Mật khẩu không được để trống!");
        }

        // 2. Hash mật khẩu bằng SHA-256
        String matKhauHash = PasswordUtil.hashSHA256(matKhauPlainText.trim());

        // 3. Truy vấn CSDL
        TaiKhoan taiKhoan;
        try {
            taiKhoan = taiKhoanDAO.findByCredentials(tenDangNhap.trim(), matKhauHash);
        } catch (SQLException ex) {
            throw new Exception("Lỗi kết nối cơ sở dữ liệu: " + ex.getMessage(), ex);
        }

        // 4. Kiểm tra kết quả
        if (taiKhoan == null) {
            throw new Exception("Tên đăng nhập hoặc mật khẩu không đúng!");
        }

        // 5. Kiểm tra trạng thái tài khoản
        if (taiKhoan.isLocked()) {
            throw new Exception("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên!");
        }

        // 6. Thiết lập Session
        Session session = Session.getInstance();
        session.login(
            taiKhoan.getMaTK(),
            taiKhoan.getMaNV(),
            taiKhoan.getTenDangNhap(),
            taiKhoan.getVaiTro(),
            taiKhoan.getHoTenNV()
        );

        return taiKhoan;
    }

    /**
     * Đăng xuất – xóa sạch phiên làm việc.
     */
    public void logout() {
        Session.getInstance().logout();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  QUẢN TRỊ TÀI KHOẢN (Chỉ dành cho DB_Admin)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Lấy toàn bộ danh sách tài khoản (chỉ DB_Admin).
     */
    public java.util.List<TaiKhoan> layDanhSachTaiKhoan() throws Exception {
        checkAdminPermission();
        try {
            return taiKhoanDAO.getAll();
        } catch (SQLException ex) {
            throw new Exception("Lỗi khi tải danh sách tài khoản: " + ex.getMessage(), ex);
        }
    }

    /**
     * Đổi trạng thái khóa/mở khóa tài khoản.
     */
    public void doiTrangThaiTaiKhoan(int maTK, String trangThai) throws Exception {
        checkAdminPermission();
        try {
            taiKhoanDAO.updateTrangThai(maTK, trangThai);
        } catch (SQLException ex) {
            throw new Exception("Lỗi cập nhật trạng thái tài khoản: " + ex.getMessage(), ex);
        }
    }

    /**
     * Đặt lại mật khẩu tài khoản về mật khẩu mới (hash SHA-256).
     */
    public void datLaiMatKhau(int maTK, String matKhauMoi) throws Exception {
        checkAdminPermission();
        if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
            throw new Exception("Mật khẩu mới không được để trống!");
        }
        String hash = PasswordUtil.hashSHA256(matKhauMoi.trim());
        try {
            taiKhoanDAO.resetPassword(maTK, hash);
        } catch (SQLException ex) {
            throw new Exception("Lỗi đặt lại mật khẩu: " + ex.getMessage(), ex);
        }
    }

    /**
     * Cập nhật vai trò của tài khoản.
     */
    public void capNhatVaiTro(int maTK, String vaiTro) throws Exception {
        checkAdminPermission();
        if (vaiTro == null || vaiTro.trim().isEmpty()) {
            throw new Exception("Vai trò không hợp lệ!");
        }
        try {
            taiKhoanDAO.updateVaiTro(maTK, vaiTro.trim());
        } catch (SQLException ex) {
            throw new Exception("Lỗi cập nhật vai trò: " + ex.getMessage(), ex);
        }
    }

    private void checkAdminPermission() throws Exception {
        if (!Session.getInstance().hasRole("DB_Admin")) {
            throw new Exception("Chức năng chỉ dành cho quản trị viên (DB_Admin)!");
        }
    }
}
