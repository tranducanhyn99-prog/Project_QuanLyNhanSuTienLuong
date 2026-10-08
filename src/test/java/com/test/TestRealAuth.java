package com.test;

import com.service.AuthService;
import com.session.Session;
import com.model.TaiKhoan;
import java.sql.SQLException;

public final class TestRealAuth {
    public static void main(String[] args) throws Exception {
        String user=System.getenv("TEST_SQL_USER"), password=System.getenv("TEST_SQL_PASSWORD");
        if(user==null || user.isBlank() || password==null || password.isEmpty()) { System.err.println("SKIPPED: set TEST_SQL_USER/TEST_SQL_PASSWORD."); System.exit(2); }
        AuthService auth=new AuthService();
        try {
            TaiKhoan account=auth.login(user,password);
            if(!Session.getInstance().isLoggedIn() || account.getMatKhau()!=null) throw new AssertionError("Invalid authenticated session/hash exposure");
            auth.logout();
            boolean rejected=false;
            try { auth.login(user,"wrong_"+java.util.UUID.randomUUID()); }
            catch(SQLException ex) {
                // Connectivity/certificate errors are failures, not proof of an auth rejection.
                if(ex.getErrorCode()!=18456 && !"28000".equals(ex.getSQLState())) throw ex;
                rejected=true;
            } catch(SecurityException ex) { rejected=true; }
            if(!rejected || Session.getInstance().isLoggedIn()) throw new AssertionError("Wrong password was accepted or session remained active");
            System.out.println("PASS live SQL login + wrong-password rejection; no claim about unexecuted accounts.");
        } finally { auth.logout(); }
    }
}
