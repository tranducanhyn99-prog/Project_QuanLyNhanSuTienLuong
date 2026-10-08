package com.test;

import com.config.DatabaseConnection;
import com.service.AuthService;
import java.sql.*;

/** Read-only connection check under a mapped personal SQL identity. */
public final class TestDatabaseConnection {
    public static void main(String[] args) throws Exception {
        String user=System.getenv("TEST_SQL_USER"), password=System.getenv("TEST_SQL_PASSWORD");
        if(user==null || user.isBlank() || password==null || password.isEmpty()) {
            System.err.println("SKIPPED: set TEST_SQL_USER/TEST_SQL_PASSWORD and DB_URL to the QA database.");
            System.exit(2);
        }
        AuthService auth=new AuthService();
        try {
            auth.login(user,password);
            try(Connection connection=DatabaseConnection.getConnection(); Statement sql=connection.createStatement();
                ResultSet result=sql.executeQuery("SELECT DB_NAME(),ORIGINAL_LOGIN()")) {
                if(!result.next() || !user.equalsIgnoreCase(result.getString(2))) throw new AssertionError("SQL identity mismatch");
                System.out.println("PASS live connection: "+result.getString(1)+" / "+result.getString(2)+" / "+connection.getMetaData().getDatabaseProductVersion());
            }
        } finally { auth.logout(); }
    }
}
