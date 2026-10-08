package com.test;

import com.config.DatabaseConnection;
import com.service.AuthService;
import java.sql.*;

/** Read-only live schema check; absence of credentials is SKIPPED, never PASS. */
public final class FullSystemIntegrationTest {
    public static void main(String[] args) throws Exception {
        String user=System.getenv("TEST_SQL_USER"), password=System.getenv("TEST_SQL_PASSWORD");
        if(user==null || user.isBlank() || password==null || password.isEmpty()) { System.err.println("SKIPPED: set TEST_SQL_USER/TEST_SQL_PASSWORD (DB_Admin QA identity)."); System.exit(2); }
        AuthService auth=new AuthService();
        try {
            auth.login(user,password);
            try(Connection connection=DatabaseConnection.getConnection(); Statement sql=connection.createStatement()) {
                String[] required={"PHONGBAN","CHUCVU","NHANVIEN","TAIKHOAN","CHAMCONG","PHUCAPNHANVIEN","KHAUTRUNHANVIEN","BANGLUONG","CHITIETBANGLUONG","LICHSULUONG",
                    "sp_TinhBangLuongThang","sp_ChotBangLuong","sp_HuyChotBangLuong","sp_LayTaiKhoanHienTai","sp_AdminSetRole","sp_AdminSetStatus","sp_AdminResetPassword","vw_PhieuLuongCaNhan","fn_TongPhuCap"};
                for(String object:required) {
                    try(ResultSet result=sql.executeQuery("SELECT OBJECT_ID('dbo."+object+"')")) {
                        if(!result.next() || result.getObject(1)==null) throw new AssertionError("Missing schema object: "+object);
                    }
                }
                System.out.println("PASS live schema: "+required.length+" required objects. Payroll behavior is checked by SQL E2E scripts.");
            }
        } finally { auth.logout(); }
    }
}
