# TV2 Option B Live Verification Summary (Run ID: 20261016_003727_46446d86)

## 1. Environment & Target Identity
- **Run ID**: 20261016_003727_46446d86
- **Database**: PRJ_Fix_QA_20261016_003727_46446d86 (Disposable, run-owned)
- **Ownership Marker**: 57ff28c877794da2aacdb01a05f6729b19a0106c09fd4c47b9e668280e91fc4a
- **Server Instance (CLI)**: .
- **Resolved SQLCMD Endpoint**: localhost,1433
- **Resolved JDBC URL**: jdbc:sqlserver://localhost:1433;databaseName=PRJ_Fix_QA_20261016_003727_46446d86;encrypt=true;trustServerCertificate=true
- **Connected SQL Server Name**: laptop-cua-mq
- **Connected Instance Name**: MSSQLSERVER
- **Connected Machine Name**: laptop-cua-mq
- **Connected Process ID**: 4112
- **Execution Date**: 2026-10-16 00:38:08
- **JDK Version**: 21.0.8 (--release 11 target)
- **Overall Status**: **PASS** (Exit code: 0)

## 2. Integrity & Cryptographic Hashes
- **Canonical Feature SQL**: `database/02_Module_ChamCong_TV2.sql` -> 4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471
- **Package Feature SQL**: `Bo_nop_Nhom06/database/02_Module_ChamCong_TV2.sql` -> 4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471
- **Harness Source**: `src/test/java/com/test/Tv2JdbcIntegrationTest.java` -> BAA0081EC95116F73E8D97D7396942809531F6AFA7F2A820042CCD203645C6F1
- **Runner Script**: `database/tests_TV2/run_tv2_jdbc_optionb.ps1` -> 60AB7EC6C6CFD10849B5BC60A636D49B83E18789A97D03FCA3A6E53776EF9111

## 3. Dynamic Verification Stages & Assertions Count
- **Java Compilation**: [PASS] 55 sources compiled (JDK 11 release target)
- **Negative Guards**: [PASS] 179 controlled negative checks passed successfully
- **Offline Security Regression**: [PASS] 104 assertions PASS (SecurityRegressionTest)
- **Offline GUI QA Regression**: [PASS] 88 assertions PASS (GuiQaRegressionTest)
- **SQL Schema Installation**: [PASS] Modules 01-05 installed successfully into PRJ_Fix_QA_20261016_003727_46446d86
- **Installed Procedure Definition**: [PASS] Verified TRY...CATCH + bare THROW; with 7 parameters and MaChamCong OUTPUT
- **SQL Integration Suite**: [PASS] 21 / 21 PASS (100%)
- **Concurrency Verification**: [PASS] PASS (4/4 cases waited, rejected, source unchanged, race fixture cleanup PASS)
- **Live JDBC Integration Test**: [PASS] All 6 scenarios PASS (105 total assertions matching [PASS] rows)
- **Disposable Database Cleanup**: [PASS] Successfully cleaned up owned disposable QA database: PRJ_Fix_QA_20261016_003727_46446d86 (marker verified, absence confirmed in sys.databases)

## 4. Exact Vietnamese Error Text & Typed SQLServerError Metadata Preserved
- Duplicate attendance error: `Nhân viên đã có bản ghi chấm công trong ngày này!` (Error Code: 50000, Severity: 16, State: 1)
- Inactive employee error: `Không thể ghi nhận chấm công cho nhân viên đã nghỉ việc hoặc không hoạt động!` (Error Code: 50000, Severity: 16, State: 1)
- Closed payroll period trigger error: `Kỳ lương đã chốt: không được thay đổi chấm công.` (Error Code: 50000, Severity: 16, State: 1)

## 5. Disposable Database Cleanup
- Status: **PASS**
- Details: Successfully cleaned up owned disposable QA database: PRJ_Fix_QA_20261016_003727_46446d86 (marker verified, absence confirmed in sys.databases)