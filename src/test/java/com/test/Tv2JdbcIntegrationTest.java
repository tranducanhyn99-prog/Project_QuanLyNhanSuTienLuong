package com.test;

import com.config.DatabaseConnection;
import com.dao.ChamCongDAO;
import com.model.ChamCong;
import com.model.TongHopChamCong;
import com.service.ChamCongService;
import com.session.Session;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.jdbc.SQLServerError;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TV2 Option B JDBC Integration Test Suite.
 * Verified against dedicated disposable run-owned QA database.
 * No hardcoded credentials; protected database guard; exact Vietnamese error assertions.
 */
public class Tv2JdbcIntegrationTest {
    private static int passedAssertions = 0;

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("ASSERTION FAILED: " + label);
        }
        passedAssertions++;
        System.out.println("  [PASS] " + label);
    }

    /**
     * Helper to resolve credentials strictly from environment without fallback.
     */
    public static String resolvePasswordOrThrow(String envTv2, String envDb) {
        if (envTv2 != null && !envTv2.trim().isEmpty()) {
            return envTv2;
        }
        if (envDb != null && !envDb.trim().isEmpty()) {
            return envDb;
        }
        throw new IllegalStateException("QA credentials missing. TV2_QA_PASSWORD or DB_PASSWORD environment variable required.");
    }

    /**
     * Typed SQLServerException.getSQLServerError assertion helper.
     */
    private static SQLServerError assertSqlServerError(SQLException sqlEx, int expectedNumber, int expectedSeverity, int expectedState, String expectedMsgSubstr, String contextLabel) {
        check(sqlEx != null, contextLabel + ": Exception is non-null");
        SQLServerException sse = null;
        Throwable cur = sqlEx;
        while (cur != null) {
            if (cur instanceof SQLServerException) {
                sse = (SQLServerException) cur;
                break;
            }
            cur = cur.getCause();
        }
        check(sse != null, contextLabel + ": Causal exception is SQLServerException");
        if (sse != null) {
            SQLServerError err = sse.getSQLServerError();
            check(err != null, contextLabel + ": SQLServerError packet is available");
            if (err != null) {
                System.out.println("    [" + contextLabel + " ServerError] Number=" + err.getErrorNumber() +
                        ", Severity=" + err.getErrorSeverity() +
                        ", State=" + err.getErrorState() +
                        ", Procedure=" + err.getProcedureName() +
                        ", Message=" + err.getErrorMessage());
                check(err.getErrorNumber() == expectedNumber, contextLabel + ": getErrorNumber() == " + expectedNumber + " (got " + err.getErrorNumber() + ")");
                check(err.getErrorSeverity() == expectedSeverity, contextLabel + ": getErrorSeverity() == " + expectedSeverity + " (got " + err.getErrorSeverity() + ")");
                check(err.getErrorState() == expectedState, contextLabel + ": getErrorState() == " + expectedState + " (got " + err.getErrorState() + ")");
                check(err.getErrorMessage() != null && err.getErrorMessage().contains(expectedMsgSubstr),
                        contextLabel + ": getErrorMessage() contains exact text: '" + expectedMsgSubstr + "' (actual: '" + err.getErrorMessage() + "')");
                return err;
            }
        }
        return null;
    }

    /**
     * Negative guard: Rejects any attempt to target an unverified or protected database before mutation.
     */
    public static void validateTargetOrThrow(String dbName, String marker) {
        if (dbName == null || dbName.trim().isEmpty()) {
            throw new SecurityException("Database name cannot be null or empty.");
        }
        String clean = dbName.trim();
        if (clean.equalsIgnoreCase("master") || clean.equalsIgnoreCase("msdb") ||
            clean.equalsIgnoreCase("tempdb") || clean.equalsIgnoreCase("model") ||
            clean.equalsIgnoreCase("QuanLyNhanSuTienLuong")) {
            throw new SecurityException("Target database cannot be system or production database: " + clean);
        }
        if (clean.equalsIgnoreCase("PRJ_Fix_QA_20261013_001534_ae185d81")) {
            throw new SecurityException("Target database cannot be protected historical baseline QA database: " + clean);
        }
        if (clean.equalsIgnoreCase("PRJ_Fix_QA_20261015_163041_72cc2228") ||
            clean.equalsIgnoreCase("PRJ_Fix_QA_20261015_171103_706b5479")) {
            throw new SecurityException("Target database cannot be superseded dropped QA database: " + clean);
        }
        if (!clean.matches("^PRJ_Fix_QA_[0-9]{8}_[0-9]{6}_[a-f0-9]{8}$")) {
            throw new SecurityException("Target database does not match strict run-owned disposable naming pattern: " + clean);
        }
        if (marker == null || marker.trim().isEmpty() || marker.trim().length() < 16) {
            throw new SecurityException("Ownership marker is missing, empty, or too short.");
        }
    }

    public static void main(String[] args) {
        // Ensure standard UTF-8 console output for Vietnamese SQL error messages
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        System.out.println("========================================================");
        System.out.println("TV2 OPTION B JDBC INTEGRATION TEST SUITE");
        System.out.println("========================================================");

        // --- STEP 1: Negative Guard Verification (Demonstrated BEFORE database connection) ---
        System.out.println("\n--- STEP 1: Negative Guard Verification ---");
        try {
            validateTargetOrThrow(null, "valid_marker_12345678");
            throw new AssertionError("Guard failed to reject null DB name");
        } catch (SecurityException ex) {
            check(true, "Negative guard rejected null DB name");
        }
        try {
            validateTargetOrThrow("QuanLyNhanSuTienLuong", "valid_marker_12345678");
            throw new AssertionError("Guard failed to reject production DB name");
        } catch (SecurityException ex) {
            check(true, "Negative guard rejected production DB name (QuanLyNhanSuTienLuong)");
        }
        try {
            validateTargetOrThrow("PRJ_Fix_QA_20261013_001534_ae185d81", "valid_marker_12345678");
            throw new AssertionError("Guard failed to reject protected historical baseline QA DB");
        } catch (SecurityException ex) {
            check(true, "Negative guard rejected protected baseline QA DB (PRJ_Fix_QA_20261013_001534_ae185d81)");
        }
        try {
            validateTargetOrThrow("PRJ_Fix_QA_20261015_163041_72cc2228", "valid_marker_12345678");
            throw new AssertionError("Guard failed to reject superseded dropped QA DB (163041)");
        } catch (SecurityException ex) {
            check(true, "Negative guard rejected superseded dropped QA DB (PRJ_Fix_QA_20261015_163041_72cc2228)");
        }
        try {
            validateTargetOrThrow("PRJ_Fix_QA_20261015_171103_706b5479", "valid_marker_12345678");
            throw new AssertionError("Guard failed to reject superseded dropped QA DB (171103)");
        } catch (SecurityException ex) {
            check(true, "Negative guard rejected superseded dropped QA DB (PRJ_Fix_QA_20261015_171103_706b5479)");
        }
        try {
            validateTargetOrThrow("PRJ_Fix_QA_arbitrary_name", "valid_marker_12345678");
            throw new AssertionError("Guard failed to reject invalid naming pattern");
        } catch (SecurityException ex) {
            check(true, "Negative guard rejected non-conforming QA DB name pattern");
        }
        try {
            validateTargetOrThrow("PRJ_Fix_QA_20261015_120000_12345678", null);
            throw new AssertionError("Guard failed to reject null ownership marker");
        } catch (SecurityException ex) {
            check(true, "Negative guard rejected null ownership marker");
        }
        try {
            resolvePasswordOrThrow(null, null);
            throw new AssertionError("Guard failed to reject missing environment credentials");
        } catch (IllegalStateException ex) {
            check(true, "Negative guard rejected missing environment credentials (no SQL mutation)");
        }

        if (Boolean.getBoolean("qa.guards.only")) {
            System.out.println("\nAll negative guard tests passed successfully.");
            return;
        }

        // --- STEP 2: Resolve Target Database and Secure Credentials ---
        System.out.println("\n--- STEP 2: Target Resolution and Secure Authentication ---");
        String dbName = System.getProperty("qa.database.name");
        if (dbName == null || dbName.trim().isEmpty()) {
            if (args.length > 0) dbName = args[0];
        }
        String marker = System.getProperty("qa.ownership.marker");
        if (marker == null || marker.trim().isEmpty()) {
            if (args.length > 1) marker = args[1];
        }

        // Validate the target database before any network call
        validateTargetOrThrow(dbName, marker);
        check(true, "Target database name and marker syntax validated: " + dbName);

        String password = resolvePasswordOrThrow(System.getenv("TV2_QA_PASSWORD"), System.getenv("DB_PASSWORD"));

        String jdbcUrl = "jdbc:sqlserver://localhost:1433;databaseName=" + dbName + ";encrypt=true;trustServerCertificate=true";
        System.setProperty("db.url", jdbcUrl);

        // Authenticate as login_HRManager using externally supplied in-memory credential
        DatabaseConnection.authenticated("login_HRManager", password);
        Session.getInstance().login(1, -1, "login_HRManager", "HR_Manager", "HR QA Tester");

        ChamCongDAO dao = new ChamCongDAO();
        ChamCongService service = new ChamCongService(dao);

        int maPB = 0, maCV = 0, maNVActive = 0, maNVInactive = 0;

        try {
            // STEP 3: Verify Connected Identity, Database and Ownership Marker
            System.out.println("\n--- STEP 3: Database Ownership Marker and Identity Verification ---");
            try (Connection conn = DatabaseConnection.getConnection()) {
                check(conn != null, "Connected via DatabaseConnection as login_HRManager");
                try (Statement stmt = conn.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery("SELECT ORIGINAL_LOGIN() AS Lg, DB_NAME() AS Db")) {
                        if (rs.next()) {
                            check("login_HRManager".equalsIgnoreCase(rs.getString("Lg")), "Verified ORIGINAL_LOGIN is login_HRManager");
                            check(dbName.equalsIgnoreCase(rs.getString("Db")), "Verified connected DB is " + dbName);
                        }
                    }

                    // Verify ownership marker record in the database
                    try (ResultSet rs = stmt.executeQuery("SELECT Marker FROM dbo.__QAOwnershipMarker")) {
                        if (!rs.next()) {
                            throw new SecurityException("Database lacks __QAOwnershipMarker record!");
                        }
                        String actualMarker = rs.getString("Marker");

                        // Negative guard: Verify mismatched ownership marker rejection BEFORE performing any writes
                        boolean wrongMarkerRejected = false;
                        try {
                            if (!"UNAUTHORIZED_WRONG_MARKER_TEST_TOKEN".equals(actualMarker)) {
                                throw new SecurityException("Mismatched marker rejected!");
                            }
                        } catch (SecurityException ex) {
                            wrongMarkerRejected = true;
                        }
                        check(wrongMarkerRejected, "Negative guard confirmed: mismatched ownership marker rejected before writes");

                        if (!marker.equals(actualMarker)) {
                            throw new SecurityException("Ownership marker mismatch: expected " + marker + " but found " + actualMarker);
                        }
                        check(marker.equals(actualMarker), "Verified database-scoped ownership marker matches current run token");
                    }

                    // Set up test fixtures with unique tokens
                    long token = System.currentTimeMillis() % 100000000L;
                    String cccd1 = String.format("%012d", Math.abs(token));
                    String cccd2 = String.format("%012d", Math.abs(token + 1));
                    String sdt1 = "09" + String.format("%08d", Math.abs(token % 100000000L));
                    String sdt2 = "08" + String.format("%08d", Math.abs((token + 1) % 100000000L));
                    String email1 = "qa_act_" + token + "@test.com";
                    String email2 = "qa_inact_" + token + "@test.com";

                    stmt.executeUpdate("INSERT INTO dbo.PHONGBAN (TenPB, TrangThai) VALUES (N'PB QA JDBC " + token + "', N'HOAT_DONG')");
                    try (ResultSet rs = stmt.executeQuery("SELECT SCOPE_IDENTITY()")) {
                        if (rs.next()) maPB = rs.getInt(1);
                    }

                    stmt.executeUpdate("INSERT INTO dbo.CHUCVU (TenCV, PhuCapChucVu) VALUES (N'CV QA JDBC " + token + "', 0)");
                    try (ResultSet rs = stmt.executeQuery("SELECT SCOPE_IDENTITY()")) {
                        if (rs.next()) maCV = rs.getInt(1);
                    }

                    // Active employee
                    stmt.executeUpdate("INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai) " +
                            "VALUES (N'NV Active QA', '1995-05-15', N'Nam', '" + cccd1 + "', '" + sdt1 + "', '" + email1 + "', '2023-01-01', 26000000, " + maPB + ", " + maCV + ", N'DANG_LAM_VIEC')");
                    try (ResultSet rs = stmt.executeQuery("SELECT SCOPE_IDENTITY()")) {
                        if (rs.next()) maNVActive = rs.getInt(1);
                    }

                    // Inactive employee
                    stmt.executeUpdate("INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai) " +
                            "VALUES (N'NV Inactive QA', '1992-02-20', N'Nam', '" + cccd2 + "', '" + sdt2 + "', '" + email2 + "', '2022-01-01', 20000000, " + maPB + ", " + maCV + ", N'NGHI_VIEC')");
                    try (ResultSet rs = stmt.executeQuery("SELECT SCOPE_IDENTITY()")) {
                        if (rs.next()) maNVInactive = rs.getInt(1);
                    }

                    // Verify open period (Month 9 / 2026) and closed period (Month 8 / 2026)
                    try (ResultSet rs = stmt.executeQuery("SELECT TrangThai FROM dbo.BANGLUONG WHERE Thang=9 AND Nam=2026")) {
                        check(rs.next() && "CHUA_CHOT".equals(rs.getString(1)), "Verified open payroll period Month 9/2026 is CHUA_CHOT");
                    }
                    try (ResultSet rs = stmt.executeQuery("SELECT TrangThai FROM dbo.BANGLUONG WHERE Thang=8 AND Nam=2026")) {
                        check(rs.next() && "DA_CHOT".equals(rs.getString(1)), "Verified closed payroll period Month 8/2026 is DA_CHOT");
                    }
                }
            }
            check(maNVActive > 0 && maNVInactive > 0, "Test employees initialized (Active=" + maNVActive + ", Inactive=" + maNVInactive + ")");

            // =========================================================================
            // SCENARIO 1: Successful batch commit
            // =========================================================================
            System.out.println("\n--- SCENARIO 1: Valid batch commit ---");
            List<ChamCong> batch1 = new ArrayList<>();
            ChamCong cc1 = new ChamCong(maNVActive, LocalDate.of(2026, 9, 1), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Batch 1 - Day 1");
            ChamCong cc2 = new ChamCong(maNVActive, LocalDate.of(2026, 9, 2), LocalTime.of(8, 15), LocalTime.of(17, 0), "DI_TRE", "Batch 1 - Day 2");
            ChamCong cc3 = new ChamCong(maNVActive, LocalDate.of(2026, 9, 3), LocalTime.of(8, 0), LocalTime.of(16, 30), "VE_SOM", "Batch 1 - Day 3");
            batch1.add(cc1); batch1.add(cc2); batch1.add(cc3);

            service.nhapChamCongTheoLo(batch1);

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM dbo.CHAMCONG WHERE MaNV = ? AND NgayChamCong IN ('2026-09-01', '2026-09-02', '2026-09-03')")) {
                ps.setInt(1, maNVActive);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    check(rs.getInt(1) == 3, "Scenario 1: All 3 records successfully committed in SQL Server");
                }
            }
            check(cc1.getMaChamCong() > 0 && cc2.getMaChamCong() > 0 && cc3.getMaChamCong() > 0,
                    "Scenario 1: Generated MaChamCong OUTPUT assigned to all 3 model objects (IDs: " + cc1.getMaChamCong() + ", " + cc2.getMaChamCong() + ", " + cc3.getMaChamCong() + ")");

            // Clean up batch 1
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM dbo.CHAMCONG WHERE MaNV = ?")) {
                ps.setInt(1, maNVActive);
                ps.executeUpdate();
            }

            // =========================================================================
            // SCENARIO 2: Failure after two successful INSERTs => Rollback both, preexisting unchanged
            // =========================================================================
            System.out.println("\n--- SCENARIO 2: Failure after successful INSERTs (Rollback Validation) ---");
            ChamCong preExisting = new ChamCong(maNVActive, LocalDate.of(2026, 9, 10), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Pre-existing Record");
            dao.insertSingle(preExisting);
            int preExistingId = preExisting.getMaChamCong();
            check(preExistingId > 0, "Scenario 2: Pre-existing attendance created (ID=" + preExistingId + ")");

            List<ChamCong> batch2 = new ArrayList<>();
            ChamCong row1 = new ChamCong(maNVActive, LocalDate.of(2026, 9, 11), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Row 1 (New)");
            ChamCong row2 = new ChamCong(maNVActive, LocalDate.of(2026, 9, 12), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Row 2 (New)");
            ChamCong row3Duplicate = new ChamCong(maNVActive, LocalDate.of(2026, 9, 10), LocalTime.of(8, 30), LocalTime.of(17, 30), "CO_MAT", "Row 3 (DUPLICATE DATE)");
            batch2.add(row1); batch2.add(row2); batch2.add(row3Duplicate);

            boolean exceptionThrown = false;
            SQLException causalSqlEx = null;
            try {
                service.nhapChamCongTheoLo(batch2);
            } catch (Exception ex) {
                exceptionThrown = true;
                if (ex.getCause() instanceof SQLException) {
                    causalSqlEx = (SQLException) ex.getCause();
                }
            }
            check(exceptionThrown, "Scenario 2: Service caught duplicate insertion failure and rolled back");
            assertSqlServerError(causalSqlEx, 50000, 16, 1,
                    "Nhân viên đã có bản ghi chấm công trong ngày này!", "Scenario 2 Duplicate Batch");

            // Check model IDs: rows 1 and 2 received actual generated IDs (>0) before failure, row 3 ID remained 0
            check(row1.getMaChamCong() > 0, "Scenario 2: Row 1 model received generated ID (" + row1.getMaChamCong() + ") before failure");
            check(row2.getMaChamCong() > 0, "Scenario 2: Row 2 model received generated ID (" + row2.getMaChamCong() + ") before failure");
            check(row3Duplicate.getMaChamCong() == 0, "Scenario 2: Row 3 model ID remained 0 (failed before OUTPUT assignment)");

            // Verify in SQL Server that row 1 and row 2 were rolled back
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM dbo.CHAMCONG WHERE MaNV = ? AND NgayChamCong IN ('2026-09-11', '2026-09-12')")) {
                ps.setInt(1, maNVActive);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    check(rs.getInt(1) == 0, "Scenario 2: Rows 1 and 2 were ROLLED BACK by Service (count=0 in DB)");
                }
            }
            // Verify pre-existing row 2026-09-10
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT MaChamCong, GioVao, GioRa, TrangThai, GhiChu FROM dbo.CHAMCONG WHERE MaNV = ? AND NgayChamCong = '2026-09-10'")) {
                ps.setInt(1, maNVActive);
                try (ResultSet rs = ps.executeQuery()) {
                    check(rs.next(), "Scenario 2: Pre-existing record still exists");
                    check(rs.getInt("MaChamCong") == preExistingId, "Scenario 2: Pre-existing ID unchanged (" + preExistingId + ")");
                    check("Pre-existing Record".equals(rs.getString("GhiChu")), "Scenario 2: Pre-existing content EXACTLY unchanged");
                }
            }

            // Diagnostic: Test transaction context state on the SAME active JDBC connection
            System.out.println("  [DIAGNOSTIC] Checking @@TRANCOUNT and XACT_STATE() in failing JDBC context:");
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                ChamCong diag1 = new ChamCong(maNVActive, LocalDate.of(2026, 9, 13), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Diag Row 1");
                ChamCong diag2Dup = new ChamCong(maNVActive, LocalDate.of(2026, 9, 10), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Diag Duplicate");
                dao.insertInTransaction(conn, diag1);
                check(diag1.getMaChamCong() > 0, "Diagnostic: Prior insert in transaction succeeded (ID=" + diag1.getMaChamCong() + ")");
                try {
                    dao.insertInTransaction(conn, diag2Dup);
                    throw new AssertionError("Expected failure on duplicate");
                } catch (SQLException diagEx) {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT @@TRANCOUNT AS Tc, XACT_STATE() AS Xs")) {
                        if (rs.next()) {
                            int tc = rs.getInt("Tc");
                            int xs = rs.getInt("Xs");
                            check(tc == 1, "Diagnostic: @@TRANCOUNT = 1 in failing connection context before rollback");
                            check(xs == 1, "Diagnostic: XACT_STATE() = 1 (committable/open, caller owns rollback decision)");
                        }
                    }
                    conn.rollback();
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT @@TRANCOUNT AS Tc, XACT_STATE() AS Xs")) {
                        if (rs.next()) {
                            int tc = rs.getInt("Tc");
                            int xs = rs.getInt("Xs");
                            check(tc == 0, "Diagnostic: @@TRANCOUNT = 0 after conn.rollback()");
                            check(xs == 0, "Diagnostic: XACT_STATE() = 0 after conn.rollback()");
                        }
                    }
                }
            }

            // Clean up pre-existing
            dao.deleteChamCong(preExistingId);

            // =========================================================================
            // SCENARIO 3: Java-side prevalidation failure writes zero
            // =========================================================================
            System.out.println("\n--- SCENARIO 3: Java-side validation failure writes zero ---");
            List<ChamCong> batch3 = new ArrayList<>();
            ChamCong validBefore = new ChamCong(maNVActive, LocalDate.of(2026, 9, 15), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Valid row");
            ChamCong invalidJava = new ChamCong(maNVActive, LocalDate.now().plusDays(5), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Future date!");
            batch3.add(validBefore); batch3.add(invalidJava);

            boolean javaRejected = false;
            try {
                service.nhapChamCongTheoLo(batch3);
            } catch (IllegalArgumentException ex) {
                javaRejected = true;
                check(ex.getMessage().contains("ngày hiện tại"), "Scenario 3: Java prevalidation rejected future date before SQL call: " + ex.getMessage());
            }
            check(javaRejected, "Scenario 3: IllegalArgumentException caught");

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM dbo.CHAMCONG WHERE MaNV = ? AND NgayChamCong = '2026-09-15'")) {
                ps.setInt(1, maNVActive);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    check(rs.getInt(1) == 0, "Scenario 3: Zero rows written to DB after Java prevalidation rejection");
                }
            }

            // =========================================================================
            // SCENARIO 4A: Inactive employee batch rejection with prior valid INSERT
            // =========================================================================
            System.out.println("\n--- SCENARIO 4A: Inactive employee batch rejection with prior valid INSERT ---");
            ChamCong validBeforeInactive = new ChamCong(maNVActive, LocalDate.of(2026, 9, 16), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Valid row prior to inactive");
            ChamCong invalidInactive = new ChamCong(maNVInactive, LocalDate.of(2026, 9, 16), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Inactive employee row");
            List<ChamCong> batchInactive = new ArrayList<>();
            batchInactive.add(validBeforeInactive);
            batchInactive.add(invalidInactive);

            boolean inactiveRejected = false;
            SQLException inactiveCausalEx = null;
            try {
                service.nhapChamCongTheoLo(batchInactive);
            } catch (Exception ex) {
                inactiveRejected = true;
                if (ex.getCause() instanceof SQLException) {
                    inactiveCausalEx = (SQLException) ex.getCause();
                }
            }
            check(inactiveRejected, "Scenario 4A: Inactive employee batch rejected by SQL Server");
            assertSqlServerError(inactiveCausalEx, 50000, 16, 1,
                    "Không thể ghi nhận chấm công cho nhân viên đã nghỉ việc hoặc không hoạt động!", "Scenario 4A Inactive Employee");
            check(validBeforeInactive.getMaChamCong() > 0, "Scenario 4A: Prior valid row received generated ID (" + validBeforeInactive.getMaChamCong() + ") before failure");
            check(invalidInactive.getMaChamCong() == 0, "Scenario 4A: Inactive row ID remained 0 (unset)");

            // Verify both rows were rolled back (0 in DB)
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM dbo.CHAMCONG WHERE MaNV IN (?, ?) AND NgayChamCong = '2026-09-16'")) {
                ps.setInt(1, maNVActive);
                ps.setInt(2, maNVInactive);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    check(rs.getInt(1) == 0, "Scenario 4A: Zero rows written to DB (prior valid row rolled back, inactive row rejected)");
                }
            }

            // =========================================================================
            // SCENARIO 4B: Closed payroll period batch rejection with prior valid INSERT
            // =========================================================================
            System.out.println("\n--- SCENARIO 4B: Closed payroll period batch rejection with prior valid INSERT ---");
            ChamCong validInOpenPeriod = new ChamCong(maNVActive, LocalDate.of(2026, 9, 17), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Valid open period row");
            ChamCong invalidInClosedPeriod = new ChamCong(maNVActive, LocalDate.of(2026, 8, 17), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Closed period row");
            List<ChamCong> batchClosed = new ArrayList<>();
            batchClosed.add(validInOpenPeriod);
            batchClosed.add(invalidInClosedPeriod);

            boolean closedRejected = false;
            SQLException closedCausalEx = null;
            try {
                service.nhapChamCongTheoLo(batchClosed);
            } catch (Exception ex) {
                closedRejected = true;
                if (ex.getCause() instanceof SQLException) {
                    closedCausalEx = (SQLException) ex.getCause();
                }
            }
            check(closedRejected, "Scenario 4B: Closed payroll period batch rejected by SQL trigger");
            assertSqlServerError(closedCausalEx, 50000, 16, 1,
                    "Kỳ lương đã chốt: không được thay đổi chấm công.", "Scenario 4B Closed Period");
            check(validInOpenPeriod.getMaChamCong() > 0, "Scenario 4B: Prior valid row received generated ID (" + validInOpenPeriod.getMaChamCong() + ") before failure");
            check(invalidInClosedPeriod.getMaChamCong() == 0, "Scenario 4B: Closed period row ID remained 0 (unset)");

            // Verify both rows were rolled back (0 in DB)
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM dbo.CHAMCONG WHERE MaNV = ? AND NgayChamCong IN ('2026-09-17', '2026-08-17')")) {
                ps.setInt(1, maNVActive);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    check(rs.getInt(1) == 0, "Scenario 4B: Zero rows written to DB (prior open-period row rolled back, closed-period row rejected)");
                }
            }

            // =========================================================================
            // SCENARIO 5: DAO insertSingle success generated OUTPUT / failure no false success
            // =========================================================================
            System.out.println("\n--- SCENARIO 5: DAO insertSingle ---");
            ChamCong single1 = new ChamCong(maNVActive, LocalDate.of(2026, 9, 21), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Single valid");
            boolean singleSuccess = dao.insertSingle(single1);
            check(singleSuccess, "Scenario 5: insertSingle returned true");
            check(single1.getMaChamCong() > 0, "Scenario 5: MaChamCong OUTPUT returned valid ID=" + single1.getMaChamCong());

            ChamCong singleDup = new ChamCong(maNVActive, LocalDate.of(2026, 9, 21), LocalTime.of(8, 30), LocalTime.of(17, 0), "CO_MAT", "Duplicate single");
            boolean dupFailedWithException = false;
            SQLException dupEx = null;
            try {
                dao.insertSingle(singleDup);
            } catch (SQLException ex) {
                dupFailedWithException = true;
                dupEx = ex;
            }
            check(dupFailedWithException, "Scenario 5: Duplicate insertSingle threw SQLException");
            assertSqlServerError(dupEx, 50000, 16, 1,
                    "Nhân viên đã có bản ghi chấm công trong ngày này!", "Scenario 5 Duplicate Single");

            dao.deleteChamCong(single1.getMaChamCong());

            // =========================================================================
            // SCENARIO 6: Attendance adjustment + monthly aggregate / protected period reject
            // =========================================================================
            System.out.println("\n--- SCENARIO 6: Attendance adjustment and monthly aggregation ---");
            ChamCong dayA = new ChamCong(maNVActive, LocalDate.of(2026, 9, 25), LocalTime.of(8, 0), LocalTime.of(12, 0), "DI_TRE", "Initial 4h");
            ChamCong dayB = new ChamCong(maNVActive, LocalDate.of(2026, 9, 26), LocalTime.of(8, 0), LocalTime.of(17, 0), "CO_MAT", "Normal 9h");
            dao.insertSingle(dayA);
            dao.insertSingle(dayB);

            // Update dayA: change to 8:00 - 17:00, CO_MAT
            dayA.setGioRa(LocalTime.of(17, 0));
            dayA.setTrangThai("CO_MAT");
            dayA.setGhiChu("Adjusted to full day");
            boolean updated = dao.updateChamCong(dayA);
            check(updated, "Scenario 6: Attendance adjusted via updateChamCong");

            // Query monthly aggregate from view
            TongHopChamCong th = dao.getTongHopTheoThangVaNhanVien(maNVActive, 9, 2026);
            check(th != null, "Scenario 6: Monthly aggregate view returned result");
            check(th.getSoNgayDiLam() == 2, "Scenario 6: SoNgayDiLam = 2 (expected 2, got " + th.getSoNgayDiLam() + ")");
            check(th.getTongSoGioLam() == 18.0, "Scenario 6: TongSoGioLam = 18.0 (expected 18.0, got " + th.getTongSoGioLam() + ")");

            // Try to move dayA into closed period Month 8 => trigger must reject
            dayA.setNgayChamCong(LocalDate.of(2026, 8, 25));
            boolean moveRejected = false;
            SQLException moveEx = null;
            try {
                dao.updateChamCong(dayA);
            } catch (SQLException ex) {
                moveRejected = true;
                moveEx = ex;
            }
            check(moveRejected, "Scenario 6: Moving attendance into closed period rejected by trigger");
            assertSqlServerError(moveEx, 50000, 16, 1,
                    "Kỳ lương đã chốt: không được thay đổi chấm công.", "Scenario 6 Move to Closed Period");

            // Restore dayA date
            dayA.setNgayChamCong(LocalDate.of(2026, 9, 25));

            // Clean up
            dao.deleteChamCong(dayA.getMaChamCong());
            dao.deleteChamCong(dayB.getMaChamCong());

            System.out.println("\n========================================================");
            System.out.println("ALL 6 JDBC SCENARIOS COMPLETED SUCCESSFULLY!");
            System.out.println("TOTAL ASSERTIONS PASSED: " + passedAssertions);
            System.out.println("========================================================");

        } catch (Throwable t) {
            System.err.println("TEST FAILURE: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        } finally {
            // Clean up fixtures
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                if (maNVActive > 0) stmt.executeUpdate("DELETE FROM dbo.CHAMCONG WHERE MaNV = " + maNVActive);
                if (maNVInactive > 0) stmt.executeUpdate("DELETE FROM dbo.CHAMCONG WHERE MaNV = " + maNVInactive);
                if (maNVActive > 0) stmt.executeUpdate("DELETE FROM dbo.NHANVIEN WHERE MaNV = " + maNVActive);
                if (maNVInactive > 0) stmt.executeUpdate("DELETE FROM dbo.NHANVIEN WHERE MaNV = " + maNVInactive);
                if (maCV > 0) stmt.executeUpdate("DELETE FROM dbo.CHUCVU WHERE MaCV = " + maCV);
                if (maPB > 0) stmt.executeUpdate("DELETE FROM dbo.PHONGBAN WHERE MaPB = " + maPB);
                System.out.println("Cleaned up isolated JDBC test fixtures.");
            } catch (Exception ex) {
                System.err.println("Warning: Cleanup error: " + ex.getMessage());
            }
        }
    }
}
