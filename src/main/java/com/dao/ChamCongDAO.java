package com.dao;

import com.config.DatabaseConnection;
import com.model.ChamCong;
import com.model.TongHopChamCong;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ChamCongDAO {

    public boolean insertSingle(ChamCong cc) throws SQLException {
        String sql = "{call dbo.sp_GhiNhanChamCong(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, cc.getMaNV());
            if (cc.getNgayChamCong() != null) {
                cs.setDate(2, Date.valueOf(cc.getNgayChamCong()));
            } else {
                cs.setNull(2, Types.DATE);
            }
            if (cc.getGioVao() != null) {
                cs.setTime(3, Time.valueOf(cc.getGioVao()));
            } else {
                cs.setNull(3, Types.TIME);
            }
            if (cc.getGioRa() != null) {
                cs.setTime(4, Time.valueOf(cc.getGioRa()));
            } else {
                cs.setNull(4, Types.TIME);
            }
            cs.setString(5, cc.getTrangThai() != null ? cc.getTrangThai() : "CO_MAT");
            cs.setString(6, cc.getGhiChu());
            cs.registerOutParameter(7, Types.INTEGER);

            cs.execute();
            int generatedId = cs.getInt(7);
            cc.setMaChamCong(generatedId);
            return generatedId > 0;
        }
    }

    public void insertInTransaction(Connection conn, ChamCong cc) throws SQLException {
        String sql = "{call dbo.sp_GhiNhanChamCong(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, cc.getMaNV());
            if (cc.getNgayChamCong() != null) {
                cs.setDate(2, Date.valueOf(cc.getNgayChamCong()));
            } else {
                cs.setNull(2, Types.DATE);
            }
            if (cc.getGioVao() != null) {
                cs.setTime(3, Time.valueOf(cc.getGioVao()));
            } else {
                cs.setNull(3, Types.TIME);
            }
            if (cc.getGioRa() != null) {
                cs.setTime(4, Time.valueOf(cc.getGioRa()));
            } else {
                cs.setNull(4, Types.TIME);
            }
            cs.setString(5, cc.getTrangThai() != null ? cc.getTrangThai() : "CO_MAT");
            cs.setString(6, cc.getGhiChu());
            cs.registerOutParameter(7, Types.INTEGER);

            cs.execute();
            int generatedId = cs.getInt(7);
            cc.setMaChamCong(generatedId);
        }
    }

    public List<ChamCong> findByNhanVienAndMonth(int maNV, int thang, int nam) throws SQLException {
        List<ChamCong> list = new ArrayList<>();
        String sql = "SELECT MaChamCong, MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu "
                   + "FROM CHAMCONG "
                   + "WHERE MaNV = ? AND MONTH(NgayChamCong) = ? AND YEAR(NgayChamCong) = ? "
                   + "ORDER BY NgayChamCong ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChamCong cc = new ChamCong();
                    cc.setMaChamCong(rs.getInt("MaChamCong"));
                    cc.setMaNV(rs.getInt("MaNV"));
                    Date ngay = rs.getDate("NgayChamCong");
                    if (ngay != null) {
                        cc.setNgayChamCong(ngay.toLocalDate());
                    }
                    Time gioVao = rs.getTime("GioVao");
                    if (gioVao != null) {
                        cc.setGioVao(gioVao.toLocalTime());
                    }
                    Time gioRa = rs.getTime("GioRa");
                    if (gioRa != null) {
                        cc.setGioRa(gioRa.toLocalTime());
                    }
                    cc.setTrangThai(rs.getString("TrangThai"));
                    cc.setGhiChu(rs.getString("GhiChu"));
                    list.add(cc);
                }
            }
        }
        return list;
    }

    /**
     * Lấy danh sách chấm công của toàn bộ công ty theo tháng và năm kèm theo họ tên nhân viên.
     */
    public List<ChamCong> findByMonth(int thang, int nam) throws SQLException {
        List<ChamCong> list = new ArrayList<>();
        String sql = "SELECT cc.MaChamCong, cc.MaNV, nv.HoTen, cc.NgayChamCong, cc.GioVao, cc.GioRa, cc.TrangThai, cc.GhiChu "
                   + "FROM CHAMCONG cc "
                   + "JOIN NHANVIEN nv ON cc.MaNV = nv.MaNV "
                   + "WHERE MONTH(cc.NgayChamCong) = ? AND YEAR(cc.NgayChamCong) = ? "
                   + "ORDER BY cc.NgayChamCong DESC, cc.MaNV ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChamCong cc = new ChamCong();
                    cc.setMaChamCong(rs.getInt("MaChamCong"));
                    cc.setMaNV(rs.getInt("MaNV"));
                    cc.setHoTen(rs.getString("HoTen"));
                    Date ngay = rs.getDate("NgayChamCong");
                    if (ngay != null) {
                        cc.setNgayChamCong(ngay.toLocalDate());
                    }
                    Time gioVao = rs.getTime("GioVao");
                    if (gioVao != null) {
                        cc.setGioVao(gioVao.toLocalTime());
                    }
                    Time gioRa = rs.getTime("GioRa");
                    if (gioRa != null) {
                        cc.setGioRa(gioRa.toLocalTime());
                    }
                    cc.setTrangThai(rs.getString("TrangThai"));
                    cc.setGhiChu(rs.getString("GhiChu"));
                    list.add(cc);
                }
            }
        }
        return list;
    }

    /**
     * Lấy dữ liệu tổng hợp chấm công theo tháng từ View dbo.vw_TongHopChamCongThang.
     */
    public List<TongHopChamCong> getTongHopTheoThang(int thang, int nam) throws SQLException {
        List<TongHopChamCong> list = new ArrayList<>();
        String sql = "SELECT MaNV, HoTen, Thang, Nam, SoNgayDiLam, SoLanDiTre, SoLanVeSom, SoNgayVang, TongSoGioLam "
                   + "FROM dbo.vw_TongHopChamCongThang "
                   + "WHERE Thang = ? AND Nam = ? "
                   + "ORDER BY MaNV ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang);
            ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TongHopChamCong th = new TongHopChamCong();
                    th.setMaNV(rs.getInt("MaNV"));
                    th.setHoTen(rs.getString("HoTen"));
                    th.setThang(rs.getInt("Thang"));
                    th.setNam(rs.getInt("Nam"));
                    th.setSoNgayDiLam(rs.getInt("SoNgayDiLam"));
                    th.setSoLanDiTre(rs.getInt("SoLanDiTre"));
                    th.setSoLanVeSom(rs.getInt("SoLanVeSom"));
                    th.setSoNgayVang(rs.getInt("SoNgayVang"));
                    th.setTongSoGioLam(rs.getDouble("TongSoGioLam"));
                    list.add(th);
                }
            }
        }
        return list;
    }

    /**
     * Lấy dữ liệu tổng hợp chấm công theo tháng cho 1 nhân viên cụ thể từ View.
     */
    public TongHopChamCong getTongHopTheoThangVaNhanVien(int maNV, int thang, int nam) throws SQLException {
        String sql = "SELECT MaNV, HoTen, Thang, Nam, SoNgayDiLam, SoLanDiTre, SoLanVeSom, SoNgayVang, TongSoGioLam "
                   + "FROM dbo.vw_TongHopChamCongThang "
                   + "WHERE MaNV = ? AND Thang = ? AND Nam = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setInt(2, thang);
            ps.setInt(3, nam);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TongHopChamCong th = new TongHopChamCong();
                    th.setMaNV(rs.getInt("MaNV"));
                    th.setHoTen(rs.getString("HoTen"));
                    th.setThang(rs.getInt("Thang"));
                    th.setNam(rs.getInt("Nam"));
                    th.setSoNgayDiLam(rs.getInt("SoNgayDiLam"));
                    th.setSoLanDiTre(rs.getInt("SoLanDiTre"));
                    th.setSoLanVeSom(rs.getInt("SoLanVeSom"));
                    th.setSoNgayVang(rs.getInt("SoNgayVang"));
                    th.setTongSoGioLam(rs.getDouble("TongSoGioLam"));
                    return th;
                }
            }
        }
        return null;
    }

    /**
     * Xóa một bản ghi chấm công theo mã chấm công.
     * Kiểm tra không cho phép xóa nếu kỳ lương của tháng đó đã được chốt (DA_CHOT).
     */
    public boolean deleteChamCong(int maChamCong) throws SQLException {
        // Kiểm tra xem lượt chấm công này có thuộc về kỳ lương đã chốt không
        String checkSql = "SELECT bl.TrangThai "
                        + "FROM dbo.CHAMCONG cc "
                        + "JOIN dbo.BANGLUONG bl ON MONTH(cc.NgayChamCong) = bl.Thang AND YEAR(cc.NgayChamCong) = bl.Nam "
                        + "WHERE cc.MaChamCong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, maChamCong);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && "DA_CHOT".equals(rs.getString("TrangThai"))) {
                    throw new SQLException("Không thể xóa lượt chấm công này vì kỳ lương của tháng đó đã CHỐT! Vui lòng mở lại bảng lương trước nếu muốn điều chỉnh.");
                }
            }
        }

        String sql = "DELETE FROM dbo.CHAMCONG WHERE MaChamCong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maChamCong);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin chấm công (giờ vào, giờ ra, trạng thái, ghi chú).
     * Kiểm tra không cho phép sửa nếu kỳ lương của tháng đó đã được chốt (DA_CHOT).
     */
    public boolean updateChamCong(ChamCong cc) throws SQLException {
        // Kiểm tra xem lượt chấm công này có thuộc về kỳ lương đã chốt không
        String checkSql = "SELECT bl.TrangThai "
                        + "FROM dbo.CHAMCONG cc "
                        + "JOIN dbo.BANGLUONG bl ON MONTH(cc.NgayChamCong) = bl.Thang AND YEAR(cc.NgayChamCong) = bl.Nam "
                        + "WHERE cc.MaChamCong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, cc.getMaChamCong());
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && "DA_CHOT".equals(rs.getString("TrangThai"))) {
                    throw new SQLException("Không thể sửa lượt chấm công này vì kỳ lương của tháng đó đã CHỐT! Vui lòng mở lại bảng lương trước nếu muốn điều chỉnh.");
                }
            }
        }

        String sql = "UPDATE dbo.CHAMCONG "
                   + "SET GioVao = ?, GioRa = ?, TrangThai = ?, GhiChu = ? "
                   + "WHERE MaChamCong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (cc.getGioVao() != null) {
                ps.setTime(1, Time.valueOf(cc.getGioVao()));
            } else {
                ps.setNull(1, Types.TIME);
            }
            if (cc.getGioRa() != null) {
                ps.setTime(2, Time.valueOf(cc.getGioRa()));
            } else {
                ps.setNull(2, Types.TIME);
            }
            ps.setString(3, cc.getTrangThai() != null ? cc.getTrangThai() : "CO_MAT");
            ps.setString(4, cc.getGhiChu());
            ps.setInt(5, cc.getMaChamCong());

            return ps.executeUpdate() > 0;
        }
    }
}
