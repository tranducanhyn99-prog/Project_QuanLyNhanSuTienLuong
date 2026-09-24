package com.dao;

import com.config.DatabaseConnection;
import com.model.NhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    /**
     * Lấy danh sách nhân viên từ View vw_NhanVien_PhongBan_ChucVu (Do TV1 sở hữu)
     */
    public List<NhanVien> getAll() throws SQLException {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_NhanVien_PhongBan_ChucVu ORDER BY MaNV DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNhanVien(rs));
            }
        }
        return list;
    }

    /**
     * Tìm kiếm nhân viên theo họ tên (sử dụng Index IX_NHANVIEN_HoTen)
     */
    public List<NhanVien> searchByHoTen(String hoTen) throws SQLException {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_NhanVien_PhongBan_ChucVu WHERE HoTen LIKE ? ORDER BY MaNV DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + hoTen + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNhanVien(rs));
                }
            }
        }
        return list;
    }

    /**
     * Thêm nhân viên qua Stored Procedure sp_ThemNhanVien
     * Hỗ trợ Transaction tạo kèm tài khoản theo phân công TV1
     */
    public int themNhanVien(NhanVien nv, boolean taoTaiKhoan, String tenDangNhap, String matKhauSHA256, String vaiTro) throws SQLException {
        String sql = "{CALL sp_ThemNhanVien(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, nv.getHoTen());
            cs.setDate(2, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);
            cs.setString(3, nv.getGioiTinh());
            cs.setString(4, nv.getCccd());
            cs.setString(5, nv.getDiaChi());
            cs.setString(6, nv.getSoDienThoai());
            cs.setString(7, nv.getEmail());
            cs.setDate(8, nv.getNgayVaoLam() != null ? Date.valueOf(nv.getNgayVaoLam()) : null);
            cs.setBigDecimal(9, nv.getLuongCoBan());
            cs.setInt(10, nv.getMaPB());
            cs.setInt(11, nv.getMaCV());
            cs.setBoolean(12, taoTaiKhoan);
            cs.setString(13, tenDangNhap);
            cs.setString(14, matKhauSHA256);
            cs.setString(15, vaiTro != null ? vaiTro : "Employee");

            cs.registerOutParameter(16, Types.INTEGER);

            cs.execute();
            return cs.getInt(16);
        }
    }

    /**
     * Cập nhật thông tin nhân viên
     */
    public boolean update(NhanVien nv) throws SQLException {
        String sql = "UPDATE NHANVIEN SET HoTen = ?, NgaySinh = ?, GioiTinh = ?, CCCD = ?, "
                   + "DiaChi = ?, SoDienThoai = ?, Email = ?, LuongCoBan = ?, MaPB = ?, MaCV = ?, TrangThai = ? "
                   + "WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setDate(2, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(3, nv.getGioiTinh());
            ps.setString(4, nv.getCccd());
            ps.setString(5, nv.getDiaChi());
            ps.setString(6, nv.getSoDienThoai());
            ps.setString(7, nv.getEmail());
            ps.setBigDecimal(8, nv.getLuongCoBan());
            ps.setInt(9, nv.getMaPB());
            ps.setInt(10, nv.getMaCV());
            ps.setString(11, nv.getTrangThai());
            ps.setInt(12, nv.getMaNV());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa nhân viên (Nếu đã phát sinh lương hoặc chấm công, Trigger trg_NhanVien_KhongXoaKhiDaPhatSinhLuong sẽ ném lỗi)
     */
    public boolean delete(int maNV) throws SQLException {
        String sql = "DELETE FROM NHANVIEN WHERE MaNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            return ps.executeUpdate() > 0;
        }
    }

    private NhanVien mapResultSetToNhanVien(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getInt("MaNV"));
        nv.setHoTen(rs.getString("HoTen"));
        Date dSinh = rs.getDate("NgaySinh");
        if (dSinh != null) nv.setNgaySinh(dSinh.toLocalDate());
        nv.setGioiTinh(rs.getString("GioiTinh"));
        nv.setCccd(rs.getString("CCCD"));
        nv.setDiaChi(rs.getString("DiaChi"));
        nv.setSoDienThoai(rs.getString("SoDienThoai"));
        nv.setEmail(rs.getString("Email"));
        Date dVao = rs.getDate("NgayVaoLam");
        if (dVao != null) nv.setNgayVaoLam(dVao.toLocalDate());
        nv.setLuongCoBan(rs.getBigDecimal("LuongCoBan"));
        nv.setTrangThai(rs.getString("TrangThai"));

        nv.setMaPB(rs.getInt("MaPB"));
        nv.setTenPB(rs.getString("TenPB"));
        nv.setMaCV(rs.getInt("MaCV"));
        nv.setTenCV(rs.getString("TenCV"));
        nv.setPhuCapChucVu(rs.getBigDecimal("PhuCapChucVu"));

        nv.setTenDangNhap(rs.getString("TenDangNhap"));
        nv.setVaiTro(rs.getString("VaiTro"));

        return nv;
    }
}
