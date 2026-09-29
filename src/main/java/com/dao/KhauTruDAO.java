package com.dao;

import com.config.DatabaseConnection;
import com.model.KhauTruNhanVien;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhauTruDAO {

    public List<KhauTruNhanVien> getListByKy(int thang, int nam) throws SQLException {
        List<KhauTruNhanVien> list = new ArrayList<>();
        String sql = "SELECT kt.*, nv.HoTen " +
                "FROM dbo.KHAUTRUNHANVIEN kt " +
                "JOIN dbo.NHANVIEN nv ON kt.MaNV = nv.MaNV " +
                "WHERE kt.Thang = ? AND kt.Nam = ? " +
                "ORDER BY kt.MaKTNV DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhauTruNhanVien item = new KhauTruNhanVien();
                    item.setMaKTNV(rs.getInt("MaKTNV"));
                    item.setMaNV(rs.getInt("MaNV"));
                    item.setHoTen(rs.getString("HoTen"));
                    item.setThang(rs.getInt("Thang"));
                    item.setNam(rs.getInt("Nam"));
                    item.setTenKhauTru(rs.getString("TenKhauTru"));
                    item.setSoTien(rs.getBigDecimal("SoTien"));
                    item.setNgayGhiNhan(rs.getDate("NgayGhiNhan"));
                    item.setLyDo(rs.getString("LyDo"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    public boolean insert(KhauTruNhanVien kt) throws SQLException {
        String sql = "INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kt.getMaNV());
            ps.setInt(2, kt.getThang());
            ps.setInt(3, kt.getNam());
            ps.setString(4, kt.getTenKhauTru());
            ps.setBigDecimal(5, kt.getSoTien());
            ps.setDate(6, kt.getNgayGhiNhan() != null ? kt.getNgayGhiNhan() : new Date(System.currentTimeMillis()));
            ps.setString(7, kt.getLyDo());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int maKTNV) throws SQLException {
        String sql = "DELETE FROM dbo.KHAUTRUNHANVIEN WHERE MaKTNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maKTNV);
            return ps.executeUpdate() > 0;
        }
    }

    // Gọi FUNCTION fn_TongKhauTru do TV3 sở hữu
    public BigDecimal getTongKhauTruNV(int maNV, int thang, int nam) throws SQLException {
        String sql = "SELECT dbo.fn_TongKhauTru(?, ?, ?) AS TongKhauTru";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("TongKhauTru");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    // Thực thi TRANSACTION sp_XoaKyLuongChuaChot do TV3 sở hữu
    public void xoaKyLuongChuaChot(int thang, int nam) throws SQLException {
        String sql = "{CALL dbo.sp_XoaKyLuongChuaChot(?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, thang);
            cs.setInt(2, nam);
            cs.execute();
        }
    }
}