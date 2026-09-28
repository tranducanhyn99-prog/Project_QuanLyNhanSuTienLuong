package com.test;

import com.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("Kiểm tra kết nối CSDL qua config.properties...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println(">>> KẾT NỐI THÀNH CÔNG! <<<");
            System.out.println("Database Product: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("Database Version: " + conn.getMetaData().getDatabaseProductVersion());

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DB_NAME() AS CurrentDB, SUSER_SNAME() AS CurrentUser")) {
                if (rs.next()) {
                    System.out.println("Current Database: " + rs.getString("CurrentDB"));
                    System.out.println("Current User: " + rs.getString("CurrentUser"));
                }
            }

            // Kiểm tra bảng TAIKHOAN
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) AS TotalTK FROM TAIKHOAN")) {
                if (rs.next()) {
                    System.out.println("Số lượng tài khoản trong bảng TAIKHOAN: " + rs.getInt("TotalTK"));
                }
            } catch (Exception ex) {
                System.err.println("Cảnh báo: Không thể truy vấn bảng TAIKHOAN: " + ex.getMessage());
            }

        } catch (Throwable ex) {
            System.err.println(">>> KẾT NỐI THẤT BẠI! <<<");
            ex.printStackTrace();
        }
    }
}
