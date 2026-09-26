package com.dao;

import com.config.DatabaseConnection;
import com.model.ChamCong;

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
}
