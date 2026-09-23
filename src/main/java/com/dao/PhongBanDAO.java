package com.dao;

import com.config.DatabaseConnection;
import com.model.PhongBan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhongBanDAO {

    public List<PhongBan> getAll() throws SQLException {
        List<PhongBan> list = new ArrayList<>();
        String sql = "SELECT MaPB, TenPB, SoDienThoai, TrangThai FROM PHONGBAN ORDER BY MaPB";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PhongBan(
                    rs.getInt("MaPB"),
                    rs.getString("TenPB"),
                    rs.getString("SoDienThoai"),
                    rs.getString("TrangThai")
                ));
            }
        }
        return list;
    }

    public List<PhongBan> getActive() throws SQLException {
        List<PhongBan> list = new ArrayList<>();
        String sql = "SELECT MaPB, TenPB, SoDienThoai, TrangThai FROM PHONGBAN WHERE TrangThai = N'HOAT_DONG' ORDER BY TenPB";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PhongBan(
                    rs.getInt("MaPB"),
                    rs.getString("TenPB"),
                    rs.getString("SoDienThoai"),
                    rs.getString("TrangThai")
                ));
            }
        }
        return list;
    }

    public boolean insert(PhongBan pb) throws SQLException {
        String sql = "INSERT INTO PHONGBAN (TenPB, SoDienThoai, TrangThai) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pb.getTenPB());
            ps.setString(2, pb.getSoDienThoai());
            ps.setString(3, pb.getTrangThai() != null ? pb.getTrangThai() : "HOAT_DONG");
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(PhongBan pb) throws SQLException {
        String sql = "UPDATE PHONGBAN SET TenPB = ?, SoDienThoai = ?, TrangThai = ? WHERE MaPB = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pb.getTenPB());
            ps.setString(2, pb.getSoDienThoai());
            ps.setString(3, pb.getTrangThai());
            ps.setInt(4, pb.getMaPB());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int maPB) throws SQLException {
        String sql = "DELETE FROM PHONGBAN WHERE MaPB = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPB);
            return ps.executeUpdate() > 0;
        }
    }
}
