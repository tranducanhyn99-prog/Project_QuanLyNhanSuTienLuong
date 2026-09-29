package com.dao;

import com.config.DatabaseConnection;
import com.model.PhuCapNhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhuCapDAO {

    public List<PhuCapNhanVien> getListByKy(int thang, int nam) throws SQLException {
        List<PhuCapNhanVien> list = new ArrayList<>();
        String sql = "SELECT pc.*, nv.HoTen " +
                "FROM dbo.PHUCAPNHANVIEN pc " +
                "JOIN dbo.NHANVIEN nv ON pc.MaNV = nv.MaNV " +
                "WHERE pc.Thang = ? AND pc.Nam = ? " +
                "ORDER BY pc.MaPCNV DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhuCapNhanVien item = new PhuCapNhanVien();
                    item.setMaPCNV(rs.getInt("MaPCNV"));
                    item.setMaNV(rs.getInt("MaNV"));
                    item.setHoTen(rs.getString("HoTen"));
                    item.setThang(rs.getInt("Thang"));
                    item.setNam(rs.getInt("Nam"));
                    item.setTenPhuCap(rs.getString("TenPhuCap"));
                    item.setSoTien(rs.getBigDecimal("SoTien"));
                    item.setNgayGhiNhan(rs.getDate("NgayGhiNhan"));
                    item.setGhiChu(rs.getString("GhiChu"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    public boolean insert(PhuCapNhanVien pc) throws SQLException {
        String sql = "INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pc.getMaNV());
            ps.setInt(2, pc.getThang());
            ps.setInt(3, pc.getNam());
            ps.setString(4, pc.getTenPhuCap());
            ps.setBigDecimal(5, pc.getSoTien());
            ps.setDate(6, pc.getNgayGhiNhan() != null ? pc.getNgayGhiNhan() : new Date(System.currentTimeMillis()));
            ps.setString(7, pc.getGhiChu());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int maPCNV) throws SQLException {
        String sql = "DELETE FROM dbo.PHUCAPNHANVIEN WHERE MaPCNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPCNV);
            return ps.executeUpdate() > 0;
        }
    }

    // Đọc dữ liệu từ VIEW vw_TongPhuCapThang do TV3 sở hữu
    public List<Map<String, Object>> getTongHopPhuCap(int thang, int nam) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT MaNV, HoTen, Thang, Nam, SoKhoanPhuCap, TongTienPhuCap " +
                "FROM dbo.vw_TongPhuCapThang " +
                "WHERE Thang = ? AND Nam = ? " +
                "ORDER BY MaNV ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("MaNV", rs.getInt("MaNV"));
                    map.put("HoTen", rs.getString("HoTen"));
                    map.put("Thang", rs.getInt("Thang"));
                    map.put("Nam", rs.getInt("Nam"));
                    map.put("SoKhoanPhuCap", rs.getInt("SoKhoanPhuCap"));
                    map.put("TongTienPhuCap", rs.getBigDecimal("TongTienPhuCap"));
                    list.add(map);
                }
            }
        }
        return list;
    }
}