package com.tools;

import com.config.DatabaseConnection;
import com.service.AuthService;
import com.util.PasswordUtil;
import java.io.Console;
import java.sql.*;
import java.util.Arrays;

/** Run explicitly by the DBA; passwords never appear in process arguments. */
public final class ProvisionIdentity {
    public static void main(String[] args) throws Exception {
        Console console = System.console();
        if (console == null) throw new IllegalStateException("Run in an interactive terminal.");
        String dba = console.readLine("DBA SQL login: ");
        char[] dbaPassword = console.readPassword("DBA password: ");
        try (Connection connection = DatabaseConnection.openForLogin(dba, new String(dbaPassword))) {
            Arrays.fill(dbaPassword, '\0');
            String login = console.readLine("New personal SQL/app login: ").trim();
            String role = console.readLine("Role (DB_Admin/HR_Manager/Payroll_Officer/Employee): ").trim();
            String employee = console.readLine("MaNV (blank for system admin): ").trim();
            char[] password = console.readPassword("New password (8–128 characters; SQL policy applies): ");
            char[] confirm = console.readPassword("Confirm password: ");
            try {
                if (!Arrays.equals(password, confirm)) throw new IllegalArgumentException("Passwords differ.");
                String plain = new String(password);
                AuthService.validateNewPassword(plain);
                try (CallableStatement statement = connection.prepareCall("{call dbo.sp_DBAProvisionIdentity(?,?,?,?,?)}")) {
                    statement.setString(1, login); statement.setString(2, plain);
                    statement.setString(3, PasswordUtil.hashPassword(plain)); statement.setString(4, role);
                    if (employee.isEmpty()) statement.setNull(5, Types.INTEGER);
                    else statement.setInt(5, Integer.parseInt(employee));
                    statement.execute();
                }
                console.printf("Created and mapped identity %s (%s). No server privileges were granted.%n", login, role);
            } finally { Arrays.fill(password, '\0'); Arrays.fill(confirm, '\0'); }
        } finally { Arrays.fill(dbaPassword, '\0'); }
    }
}
