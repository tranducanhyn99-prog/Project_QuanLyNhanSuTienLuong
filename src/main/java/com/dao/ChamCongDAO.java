package com.dao;

import com.config.DatabaseConnection;
import com.model.ChamCong;

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
        String sql = "INSERT INTO CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cc.getMaNV());
            if (cc.getNgayChamCong() != null) {
                ps.setDate(2, Date.valueOf(cc.getNgayChamCong()));
            } else {
                ps.setNull(2, Types.DATE);
            }
            if (cc.getGioVao() != null) {
                ps.setTime(3, Time.valueOf(cc.getGioVao()));
            } else {
                ps.setNull(3, Types.TIME);
            }
            if (cc.getGioRa() != null) {
                ps.setTime(4, Time.valueOf(cc.getGioRa()));
            } else {
                ps.setNull(4, Types.TIME);
            }
            ps.setString(5, cc.getTrangThai() != null ? cc.getTrangThai() : "CO_MAT");
            ps.setString(6, cc.getGhiChu());
            return ps.executeUpdate() > 0;
        }
    }

    public void insertInTransaction(Connection conn, ChamCong cc) throws SQLException {
        String sql = "INSERT INTO CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cc.getMaNV());
            if (cc.getNgayChamCong() != null) {
                ps.setDate(2, Date.valueOf(cc.getNgayChamCong()));
            } else {
                ps.setNull(2, Types.DATE);
            }
            if (cc.getGioVao() != null) {
                ps.setTime(3, Time.valueOf(cc.getGioVao()));
            } else {
                ps.setNull(3, Types.TIME);
            }
            if (cc.getGioRa() != null) {
                ps.setTime(4, Time.valueOf(cc.getGioRa()));
            } else {
                ps.setNull(4, Types.TIME);
            }
            ps.setString(5, cc.getTrangThai() != null ? cc.getTrangThai() : "CO_MAT");
            ps.setString(6, cc.getGhiChu());
            ps.executeUpdate();
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
}
