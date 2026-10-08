package com.test;

import com.config.DatabaseConnection;
import com.service.AuthService;
import com.service.DanhMucService;
import com.service.NhanVienService;
import java.sql.*;

/** Read-only live Java smoke; transaction/trigger cases run in the SQL module test. */
public final class NhanSuModuleTest {
    public static void main(String[] args) throws Exception {
        String user=System.getenv("TEST_SQL_USER"), password=System.getenv("TEST_SQL_PASSWORD");
        if(user==null || user.isBlank() || password==null || password.isEmpty()) {
            System.err.println("SKIPPED: set TEST_SQL_USER/TEST_SQL_PASSWORD (HR_Manager or DB_Admin QA identity).");
            System.exit(2);
        }
        AuthService auth=new AuthService();
        try {
            auth.login(user,password);
            DanhMucService catalogs=new DanhMucService();
            if(catalogs.layTatCaPhongBan()==null || catalogs.layTatCaChucVu()==null
                || new NhanVienService().layDanhSachNhanVien()==null) throw new AssertionError("Null read result");
            try(Connection connection=DatabaseConnection.getConnection(); Statement sql=connection.createStatement();
                ResultSet result=sql.executeQuery("SELECT dbo.fn_TinhSoNgayCong(-1,1,2020)")) {
                if(!result.next() || result.getInt(1)!=0) throw new AssertionError("Empty attendance should return zero");
            }
            System.out.println("PASS live Java HR reads + empty attendance function. No fixture was written; SQL transaction/trigger cases run separately.");
        } finally { auth.logout(); }
    }
}
