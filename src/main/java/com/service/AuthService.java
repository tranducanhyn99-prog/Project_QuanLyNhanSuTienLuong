package com.service;

import com.dao.TaiKhoanDAO;
import com.config.DatabaseConnection;
import java.sql.Connection;
import com.model.TaiKhoan;
import com.session.Session;
import com.util.PasswordUtil;

import java.sql.SQLException;

/**
 * AuthService – Dịch vụ xác thực đăng nhập.
 *
 * SQL Server xác thực login; DB trả profile theo ORIGINAL_LOGIN.
 * Hash PBKDF2 của ứng dụng được kiểm tra và migrate sau đăng nhập hợp lệ.
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

        logout();
        try (Connection connection = DatabaseConnection.openForLogin(tenDangNhap.trim(), matKhauPlainText)) {
            TaiKhoan taiKhoan = taiKhoanDAO.findCurrentIdentity(connection);
            if (taiKhoan == null || !PasswordUtil.verifyPassword(matKhauPlainText, taiKhoan.getMatKhau())) {
                throw new SecurityException("Tên đăng nhập hoặc mật khẩu không đúng, hoặc chưa được DBA mapping.");
            }
            if (!taiKhoan.isActive()) throw new SecurityException("Tài khoản đã bị khóa.");
            if (!taiKhoan.getMatKhau().startsWith("pbkdf2-sha256$")) {
                taiKhoanDAO.migrateOwnPassword(connection, PasswordUtil.hashPassword(matKhauPlainText));
            }
            DatabaseConnection.authenticated(tenDangNhap.trim(), matKhauPlainText);
            Session.getInstance().login(taiKhoan);
            // Password hash must not escape into UI/session objects.
            taiKhoan.setMatKhau(null);
            return taiKhoan;
        } catch (Exception ex) {
            logout();
            throw ex;
        }
    }

    /**
     * Đăng xuất – xóa sạch phiên làm việc.
     */
    public void logout() {
        Session.getInstance().logout();
        DatabaseConnection.logout();
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
        if (!java.util.Arrays.asList("KHOA", "HOAT_DONG").contains(trangThai)) throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        try {
            taiKhoanDAO.updateTrangThai(maTK, trangThai);
        } catch (SQLException ex) {
            throw new Exception("Lỗi cập nhật trạng thái tài khoản: " + ex.getMessage(), ex);
        }
    }

    /**
     * Đặt lại mật khẩu SQL login và hash PBKDF2 trong cùng transaction.
     */
    public void datLaiMatKhau(int maTK, String matKhauMoi) throws Exception {
        checkAdminPermission();
        if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
            throw new Exception("Mật khẩu mới không được để trống!");
        }
        validateNewPassword(matKhauMoi);
        String hash = PasswordUtil.hashPassword(matKhauMoi);
        try {
            taiKhoanDAO.resetPassword(maTK, hash, matKhauMoi);
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

    public static void validateNewPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 128) {
            throw new IllegalArgumentException("Mật khẩu mới phải có 8–128 ký tự và đáp ứng policy của SQL Server.");
        }
    }

    private void checkAdminPermission() throws Exception {
        Session.getInstance().requireRoles("DB_Admin");
    }
}
