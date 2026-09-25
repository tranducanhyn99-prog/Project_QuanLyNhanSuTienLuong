package com.dao;

import com.config.DatabaseConnection;
import com.model.BangLuong;
import com.model.ChiTietBangLuong;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BangLuongDAO {

    public int tinhBangLuongThang(int thang, int nam, int ngayCongChuan) throws SQLException {
        String sql = "{CALL dbo.sp_TinhBangLuongThang(?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, thang);
            cs.setInt(2, nam);
            cs.setInt(3, ngayCongChuan);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            return cs.getInt(4);
        }
    }

    public List<BangLuong> getAll() throws SQLException {
        List<BangLuong> list = new ArrayList<>();
        String sql = "SELECT bl.MaBangLuong, bl.Thang, bl.Nam, bl.NgayCongChuan, bl.TrangThai, "
                   + "bl.NgayTao, bl.NgayChot, "
                   + "COUNT(ct.MaChiTiet) AS SoNhanVien, "
                   + "ISNULL(SUM(ct.TienCong), 0) AS TongTienCong, "
                   + "ISNULL(SUM(ct.TongPhuCap), 0) AS TongPhuCap, "
                   + "ISNULL(SUM(ct.TongKhauTru), 0) AS TongKhauTru, "
                   + "ISNULL(SUM(ct.ThucNhan), 0) AS TongThucNhan "
                   + "FROM dbo.BANGLUONG bl "
                   + "LEFT JOIN dbo.CHITIETBANGLUONG ct ON bl.MaBangLuong = ct.MaBangLuong "
                   + "GROUP BY bl.MaBangLuong, bl.Thang, bl.Nam, bl.NgayCongChuan, bl.TrangThai, bl.NgayTao, bl.NgayChot "
                   + "ORDER BY bl.Nam DESC, bl.Thang DESC, bl.MaBangLuong DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBangLuong(rs));
            }
        }
        return list;
    }

    public BangLuong findById(int maBangLuong) throws SQLException {
        String sql = "SELECT bl.MaBangLuong, bl.Thang, bl.Nam, bl.NgayCongChuan, bl.TrangThai, "
                   + "bl.NgayTao, bl.NgayChot, "
                   + "COUNT(ct.MaChiTiet) AS SoNhanVien, "
                   + "ISNULL(SUM(ct.TienCong), 0) AS TongTienCong, "
                   + "ISNULL(SUM(ct.TongPhuCap), 0) AS TongPhuCap, "
                   + "ISNULL(SUM(ct.TongKhauTru), 0) AS TongKhauTru, "
                   + "ISNULL(SUM(ct.ThucNhan), 0) AS TongThucNhan "
                   + "FROM dbo.BANGLUONG bl "
                   + "LEFT JOIN dbo.CHITIETBANGLUONG ct ON bl.MaBangLuong = ct.MaBangLuong "
                   + "WHERE bl.MaBangLuong = ? "
                   + "GROUP BY bl.MaBangLuong, bl.Thang, bl.Nam, bl.NgayCongChuan, bl.TrangThai, bl.NgayTao, bl.NgayChot";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBangLuong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBangLuong(rs);
                }
            }
        }
        return null;
    }

    public BangLuong findByThangNam(int thang, int nam) throws SQLException {
        String sql = "SELECT bl.MaBangLuong, bl.Thang, bl.Nam, bl.NgayCongChuan, bl.TrangThai, "
                   + "bl.NgayTao, bl.NgayChot, "
                   + "COUNT(ct.MaChiTiet) AS SoNhanVien, "
                   + "ISNULL(SUM(ct.TienCong), 0) AS TongTienCong, "
                   + "ISNULL(SUM(ct.TongPhuCap), 0) AS TongPhuCap, "
                   + "ISNULL(SUM(ct.TongKhauTru), 0) AS TongKhauTru, "
                   + "ISNULL(SUM(ct.ThucNhan), 0) AS TongThucNhan "
                   + "FROM dbo.BANGLUONG bl "
                   + "LEFT JOIN dbo.CHITIETBANGLUONG ct ON bl.MaBangLuong = ct.MaBangLuong "
                   + "WHERE bl.Thang = ? AND bl.Nam = ? "
                   + "GROUP BY bl.MaBangLuong, bl.Thang, bl.Nam, bl.NgayCongChuan, bl.TrangThai, bl.NgayTao, bl.NgayChot";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBangLuong(rs);
                }
            }
        }
        return null;
    }

    public List<ChiTietBangLuong> getChiTietByBangLuong(int maBangLuong) throws SQLException {
        List<ChiTietBangLuong> list = new ArrayList<>();
        String sql = "SELECT ct.MaChiTiet, ct.MaBangLuong, ct.MaNV, nv.HoTen, "
                   + "ct.LuongCoBan, ct.NgayCongThucTe, ct.TienCong, "
                   + "ct.TongPhuCap, ct.TongKhauTru, ct.ThucNhan "
                   + "FROM dbo.CHITIETBANGLUONG ct "
                   + "INNER JOIN dbo.NHANVIEN nv ON ct.MaNV = nv.MaNV "
                   + "WHERE ct.MaBangLuong = ? "
                   + "ORDER BY nv.HoTen";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBangLuong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChiTiet(rs));
                }
            }
        }
        return list;
    }

    private BangLuong mapBangLuong(ResultSet rs) throws SQLException {
        BangLuong bangLuong = new BangLuong();
        bangLuong.setMaBangLuong(rs.getInt("MaBangLuong"));
        bangLuong.setThang(rs.getInt("Thang"));
        bangLuong.setNam(rs.getInt("Nam"));
        bangLuong.setNgayCongChuan(rs.getInt("NgayCongChuan"));
        bangLuong.setTrangThai(rs.getString("TrangThai"));
        bangLuong.setNgayTao(toLocalDateTime(rs.getTimestamp("NgayTao")));
        bangLuong.setNgayChot(toLocalDateTime(rs.getTimestamp("NgayChot")));
        bangLuong.setSoNhanVien(rs.getInt("SoNhanVien"));
        bangLuong.setTongTienCong(defaultZero(rs.getBigDecimal("TongTienCong")));
        bangLuong.setTongPhuCap(defaultZero(rs.getBigDecimal("TongPhuCap")));
        bangLuong.setTongKhauTru(defaultZero(rs.getBigDecimal("TongKhauTru")));
        bangLuong.setTongThucNhan(defaultZero(rs.getBigDecimal("TongThucNhan")));
        return bangLuong;
    }

    private ChiTietBangLuong mapChiTiet(ResultSet rs) throws SQLException {
        ChiTietBangLuong chiTiet = new ChiTietBangLuong();
        chiTiet.setMaChiTiet(rs.getInt("MaChiTiet"));
        chiTiet.setMaBangLuong(rs.getInt("MaBangLuong"));
        chiTiet.setMaNV(rs.getInt("MaNV"));
        chiTiet.setHoTen(rs.getString("HoTen"));
        chiTiet.setLuongCoBan(defaultZero(rs.getBigDecimal("LuongCoBan")));
        chiTiet.setNgayCongThucTe(rs.getInt("NgayCongThucTe"));
        chiTiet.setTienCong(defaultZero(rs.getBigDecimal("TienCong")));
        chiTiet.setTongPhuCap(defaultZero(rs.getBigDecimal("TongPhuCap")));
        chiTiet.setTongKhauTru(defaultZero(rs.getBigDecimal("TongKhauTru")));
        chiTiet.setThucNhan(defaultZero(rs.getBigDecimal("ThucNhan")));
        return chiTiet;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp value) {
        return value != null ? value.toLocalDateTime() : null;
    }
}
