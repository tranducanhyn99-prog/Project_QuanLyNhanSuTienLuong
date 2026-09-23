package com.dao;

import com.config.DatabaseConnection;
import com.model.ChucVu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChucVuDAO {

    public List<ChucVu> getAll() throws SQLException {
        List<ChucVu> list = new ArrayList<>();
        String sql = "SELECT MaCV, TenCV, PhuCapChucVu FROM CHUCVU ORDER BY MaCV";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ChucVu(
                    rs.getInt("MaCV"),
                    rs.getString("TenCV"),
                    rs.getBigDecimal("PhuCapChucVu")
                ));
            }
        }
        return list;
    }

    public boolean insert(ChucVu cv) throws SQLException {
        String sql = "INSERT INTO CHUCVU (TenCV, PhuCapChucVu) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cv.getTenCV());
            ps.setBigDecimal(2, cv.getPhuCapChucVu());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(ChucVu cv) throws SQLException {
        String sql = "UPDATE CHUCVU SET TenCV = ?, PhuCapChucVu = ? WHERE MaCV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cv.getTenCV());
            ps.setBigDecimal(2, cv.getPhuCapChucVu());
            ps.setInt(3, cv.getMaCV());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int maCV) throws SQLException {
        String sql = "DELETE FROM CHUCVU WHERE MaCV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maCV);
            return ps.executeUpdate() > 0;
        }
    }
}
