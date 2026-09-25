package com.dao;

import com.config.DatabaseConnection;
import com.model.BangLuong;
import com.model.ChiTietBangLuong;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BangLuongDAO – Data Access Object cho phân hệ Lương.
 *
 * Hợp nhất (merged) giữa mã nguồn của TV4 (Nguyễn Quang Vinh) và TV5 (Trần Đức Anh):
 * - TV4: Gọi sp_TinhBangLuongThang, truy vấn bảng lương theo kỳ, thống kê tổng hợp
 * - TV5: Gọi sp_ChotBangLuong (concurrency UPDLOCK), đọc View vw_BangLuongChiTiet, phiếu lương cá nhân
 */
public class BangLuongDAO {

    // ═══════════════════════════════════════════════════════════════════
    //  PHẦN TÍNH LƯƠNG & TRUY VẤN KỲ LƯƠNG (TV4)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Gọi Stored Procedure sp_TinhBangLuongThang (TV4)
     */
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

    /**
     * Lấy toàn bộ danh sách kỳ lương kèm số liệu thống kê tổng hợp (TV4)
     */
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

    /**
     * Alias method cho TV5 UI
     */
    public List<BangLuong> getAllBangLuong() throws SQLException {
        return getAll();
    }

    /**
     * Tìm kỳ lương theo ID (TV4)
     */
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

    /**
     * Tìm kỳ lương theo tháng/năm (TV4)
     */
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

    // ═══════════════════════════════════════════════════════════════════
    //  PHẦN CHỐT BẢNG LƯƠNG & BÁO CÁO (TV5)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Chốt bảng lương – gọi Stored Procedure sp_ChotBangLuong (TV5)
     * Sử dụng UPDLOCK + HOLDLOCK để chống xung đột Concurrency.
     */
    public void chotBangLuong(int maBangLuong) throws SQLException {
        String sql = "{CALL sp_ChotBangLuong(?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, maBangLuong);
            cs.execute();
        }
    }

    /**
     * Lấy chi tiết bảng lương theo mã kỳ lương (kết nối NHANVIEN hoặc View)
     */
    public List<ChiTietBangLuong> getChiTietByBangLuong(int maBangLuong) throws SQLException {
        List<ChiTietBangLuong> list = new ArrayList<>();
        // Nếu có View vw_BangLuongChiTiet thì dùng View để lấy thêm TenPB, TenCV
        String sql;
        boolean hasView = true;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT OBJECT_ID(N'dbo.vw_BangLuongChiTiet', N'V')")) {
            if (rs.next() && rs.getInt(1) == 0) {
                hasView = false;
            }
        } catch (Exception e) {
            hasView = false;
        }

        if (hasView) {
            sql = "SELECT * FROM vw_BangLuongChiTiet WHERE MaBangLuong = ? ORDER BY HoTen";
        } else {
            sql = "SELECT ct.MaChiTiet, ct.MaBangLuong, ct.MaNV, nv.HoTen, "
                + "ct.LuongCoBan, ct.NgayCongThucTe, ct.TienCong, "
                + "ct.TongPhuCap, ct.TongKhauTru, ct.ThucNhan, "
                + "NULL AS TenPB, NULL AS TenCV, NULL AS Thang, NULL AS Nam, "
                + "NULL AS NgayCongChuan, NULL AS TrangThaiBangLuong "
                + "FROM dbo.CHITIETBANGLUONG ct "
                + "INNER JOIN dbo.NHANVIEN nv ON ct.MaNV = nv.MaNV "
                + "WHERE ct.MaBangLuong = ? "
                + "ORDER BY nv.HoTen";
        }

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

    /**
     * Lấy chi tiết bảng lương theo tháng/năm từ View vw_BangLuongChiTiet (TV5)
     */
    public List<ChiTietBangLuong> getChiTietByThangNam(int thang, int nam) throws SQLException {
        List<ChiTietBangLuong> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_BangLuongChiTiet WHERE Thang = ? AND Nam = ? ORDER BY HoTen";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChiTiet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy phiếu lương cá nhân theo MaNV (cho Employee) (TV5)
     */
    public List<ChiTietBangLuong> getPhieuLuongByMaNV(int maNV) throws SQLException {
        List<ChiTietBangLuong> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_BangLuongChiTiet WHERE MaNV = ? ORDER BY Nam DESC, Thang DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChiTiet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy toàn bộ dữ liệu View cho báo cáo tổng hợp (TV5)
     */
    public List<ChiTietBangLuong> getAllChiTietView() throws SQLException {
        List<ChiTietBangLuong> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_BangLuongChiTiet ORDER BY Nam DESC, Thang DESC, HoTen";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapChiTiet(rs));
            }
        }
        return list;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  MAPPING METHODS
    // ═══════════════════════════════════════════════════════════════════

    private BangLuong mapBangLuong(ResultSet rs) throws SQLException {
        BangLuong bl = new BangLuong();
        bl.setMaBangLuong(rs.getInt("MaBangLuong"));
        bl.setThang(rs.getInt("Thang"));
        bl.setNam(rs.getInt("Nam"));
        bl.setNgayCongChuan(rs.getInt("NgayCongChuan"));
        bl.setTrangThai(rs.getString("TrangThai"));

        Timestamp tsTao = rs.getTimestamp("NgayTao");
        if (tsTao != null) bl.setNgayTao(tsTao.toLocalDateTime());

        Timestamp tsChot = rs.getTimestamp("NgayChot");
        if (tsChot != null) bl.setNgayChot(tsChot.toLocalDateTime());

        try {
            bl.setSoNhanVien(rs.getInt("SoNhanVien"));
            bl.setTongTienCong(defaultZero(rs.getBigDecimal("TongTienCong")));
            bl.setTongPhuCap(defaultZero(rs.getBigDecimal("TongPhuCap")));
            bl.setTongKhauTru(defaultZero(rs.getBigDecimal("TongKhauTru")));
            bl.setTongThucNhan(defaultZero(rs.getBigDecimal("TongThucNhan")));
        } catch (SQLException ignored) {
            // Không có cột thống kê
        }

        return bl;
    }

    private ChiTietBangLuong mapChiTiet(ResultSet rs) throws SQLException {
        ChiTietBangLuong ct = new ChiTietBangLuong();
        ct.setMaChiTiet(rs.getInt("MaChiTiet"));
        ct.setMaBangLuong(rs.getInt("MaBangLuong"));
        ct.setMaNV(rs.getInt("MaNV"));
        ct.setHoTen(rs.getString("HoTen"));
        ct.setLuongCoBan(defaultZero(rs.getBigDecimal("LuongCoBan")));
        ct.setNgayCongThucTe(rs.getInt("NgayCongThucTe"));
        ct.setTienCong(defaultZero(rs.getBigDecimal("TienCong")));
        ct.setTongPhuCap(defaultZero(rs.getBigDecimal("TongPhuCap")));
        ct.setTongKhauTru(defaultZero(rs.getBigDecimal("TongKhauTru")));
        ct.setThucNhan(defaultZero(rs.getBigDecimal("ThucNhan")));

        // Các cột từ View nếu có
        try {
            ct.setThang(rs.getInt("Thang"));
            ct.setNam(rs.getInt("Nam"));
            ct.setNgayCongChuan(rs.getInt("NgayCongChuan"));
            ct.setTrangThaiBangLuong(rs.getString("TrangThaiBangLuong"));
            ct.setTenPB(rs.getString("TenPB"));
            ct.setTenCV(rs.getString("TenCV"));
        } catch (SQLException ignored) {
        }

        return ct;
    }

    private BigDecimal defaultZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
