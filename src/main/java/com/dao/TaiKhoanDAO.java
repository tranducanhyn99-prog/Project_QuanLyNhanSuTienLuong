package com.dao;

import com.config.DatabaseConnection;
import com.model.TaiKhoan;

import java.sql.*;

/**
 * TaiKhoanDAO – Data Access Object cho bảng TAIKHOAN.
 *
 * Chịu trách nhiệm:
 * - Xác thực đăng nhập (findByCredentials)
 * - Truy vấn tài khoản theo mã nhân viên
 *
 * Tuân thủ quy tắc DAO:
 * - Dùng PreparedStatement (không dùng Statement)
 * - try-with-resources để đóng Connection/Statement/ResultSet
 * - Ném SQLException lên Service, không nuốt lỗi
 *
 * @author Trần Đức Anh (TV5 – MSSV 24110155)
 */
public class TaiKhoanDAO {

    /**
     * Tìm tài khoản theo TenDangNhap và MatKhau (đã hash SHA-256).
     * JOIN với NHANVIEN để lấy HoTen hiển thị trên Session.
     *
     * @param tenDangNhap Tên đăng nhập
     * @param matKhauHash Mật khẩu đã hash SHA-256 (64 ký tự hex)
     * @return TaiKhoan nếu tìm thấy; null nếu sai thông tin
     * @throws SQLException nếu lỗi kết nối hoặc truy vấn
     */
    public TaiKhoan findByCredentials(String tenDangNhap, String matKhauHash) throws SQLException {
        String sql =
            "SELECT tk.MaTK, tk.MaNV, tk.TenDangNhap, tk.VaiTro, tk.TrangThai, " +
            "       tk.NgayTao, tk.NgaySuaCuoi, " +
            "       nv.HoTen AS HoTenNV " +
            "FROM TAIKHOAN tk " +
            "LEFT JOIN NHANVIEN nv ON tk.MaNV = nv.MaNV " +
            "WHERE tk.TenDangNhap = ? AND tk.MatKhau = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhauHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm tài khoản theo MaNV.
     *
     * @param maNV Mã nhân viên
     * @return TaiKhoan nếu tìm thấy; null nếu nhân viên chưa có tài khoản
     */
    public TaiKhoan findByMaNV(int maNV) throws SQLException {
        String sql =
            "SELECT tk.MaTK, tk.MaNV, tk.TenDangNhap, tk.VaiTro, tk.TrangThai, " +
            "       tk.NgayTao, tk.NgaySuaCuoi, " +
            "       nv.HoTen AS HoTenNV " +
            "FROM TAIKHOAN tk " +
            "LEFT JOIN NHANVIEN nv ON tk.MaNV = nv.MaNV " +
            "WHERE tk.MaNV = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm tài khoản theo TenDangNhap (dùng để kiểm tra trùng khi tạo mới).
     */
    public TaiKhoan findByTenDangNhap(String tenDangNhap) throws SQLException {
        String sql =
            "SELECT tk.MaTK, tk.MaNV, tk.TenDangNhap, tk.VaiTro, tk.TrangThai, " +
            "       tk.NgayTao, tk.NgaySuaCuoi, " +
            "       nv.HoTen AS HoTenNV " +
            "FROM TAIKHOAN tk " +
            "LEFT JOIN NHANVIEN nv ON tk.MaNV = nv.MaNV " +
            "WHERE tk.TenDangNhap = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenDangNhap);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lấy toàn bộ danh sách tài khoản kèm họ tên nhân viên (cho DB_Admin quản trị).
     */
    public java.util.List<TaiKhoan> getAll() throws SQLException {
        java.util.List<TaiKhoan> list = new java.util.ArrayList<>();
        String sql =
            "SELECT tk.MaTK, tk.MaNV, tk.TenDangNhap, tk.VaiTro, tk.TrangThai, " +
            "       tk.NgayTao, tk.NgaySuaCuoi, " +
            "       nv.HoTen AS HoTenNV " +
            "FROM TAIKHOAN tk " +
            "LEFT JOIN NHANVIEN nv ON tk.MaNV = nv.MaNV " +
            "ORDER BY tk.MaTK ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Cập nhật trạng thái tài khoản (HOAT_DONG <-> KHOA).
     */
    public boolean updateTrangThai(int maTK, String trangThai) throws SQLException {
        String sql = "UPDATE TAIKHOAN SET TrangThai = ?, NgaySuaCuoi = GETDATE() WHERE MaTK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đặt lại mật khẩu (hash SHA-256).
     */
    public boolean resetPassword(int maTK, String matKhauHash) throws SQLException {
        String sql = "UPDATE TAIKHOAN SET MatKhau = ?, NgaySuaCuoi = GETDATE() WHERE MaTK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauHash);
            ps.setInt(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật vai trò tài khoản.
     */
    public boolean updateVaiTro(int maTK, String vaiTro) throws SQLException {
        String sql = "UPDATE TAIKHOAN SET VaiTro = ?, NgaySuaCuoi = GETDATE() WHERE MaTK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vaiTro);
            ps.setInt(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Mapping ─────────────────────────────────────────────────────

    private TaiKhoan mapResultSet(ResultSet rs) throws SQLException {
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTK(rs.getInt("MaTK"));

        int maNV = rs.getInt("MaNV");
        tk.setMaNV(rs.wasNull() ? -1 : maNV);

        tk.setTenDangNhap(rs.getString("TenDangNhap"));
        tk.setVaiTro(rs.getString("VaiTro"));
        tk.setTrangThai(rs.getString("TrangThai"));

        Date ngayTao = rs.getDate("NgayTao");
        if (ngayTao != null) {
            tk.setNgayTao(ngayTao.toLocalDate());
        }

        Timestamp ngaySua = rs.getTimestamp("NgaySuaCuoi");
        if (ngaySua != null) {
            tk.setNgaySuaCuoi(ngaySua.toLocalDateTime());
        }

        tk.setHoTenNV(rs.getString("HoTenNV"));

        return tk;
    }
}
