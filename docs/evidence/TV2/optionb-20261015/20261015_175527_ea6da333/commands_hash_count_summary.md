# TV2 Option B Live Verification Summary (Run ID: 20261015_175527_ea6da333)

## 1. Environment & Target Identity
- **Run ID**: 20261015_175527_ea6da333
- **Database**: PRJ_Fix_QA_20261015_175527_ea6da333 (Disposable, run-owned, cleaned up)
- **Ownership Marker**: 0f23c70f762d491e855291325e78a705a8c706251d254cd085f6d21ceba1bb98
- **Server Instance**: localhost
- **SQL Server Service**: MSSQLSERVER (Confirmed Running)
- **Execution Date**: 2026-10-15 17:55:48
- **JDK Version**: 21.0.8 (--release 11 target)
- **SQL Server Version**: Microsoft SQL Server 2025 RTM, 17.0.1000.7 (Enterprise Developer Edition, x64). Observed during this resumed task's connection preflight; the earlier connection output is retained locally at `build/sql-verification/20261015_163041_72cc2228/schema_environment.log`. The final runner does not capture a separate version query.
- **Git Baseline**: `e00da71904a68000e3b67d7634853795e7576630`; uncommitted feature changes on `fix/tv2-sp-try-catch` were tested. The source hashes below identify the executed files.

### Commands and exit-status provenance

The Desktop executor reported successful completion of the following commands. Child exit checks in the tracked runner reject nonzero SQL installation/test and JDBC process exits. The raw logs below record assertions and cleanup; they do not independently print every process exit code.

| Command | Recorded result | Evidence |
|---|---|---|
| `.\database\tests_TV2\run_tv2_jdbc_optionb.ps1 -ServerInstance localhost` | Runner exit 0, reported by executor; newly created run-owned QA database | SQL installation, procedure, TV2 SQL, concurrency, JDBC and cleanup logs in this directory |
| `.\database\tests_TV2\run_tv2_jdbc_optionb.ps1 -NegativeGuardsOnly` | Exit 0, reported by executor; no SQL mutation | Eight negative-guard assertions also appear in `Tv2JdbcIntegrationTest.log` |
| `.\run_verification.ps1 -TestClass SecurityRegressionTest` | Exit 0, reported by executor; 104 assertions | `SecurityRegressionTest.log`, copied exactly from `build/test-results/Java_Verification_20261015_174231_883.log` |
| `.\run_verification.ps1 -TestClass GuiQaRegressionTest` | Exit 0, reported by executor; 88 assertions | `GuiQaRegressionTest.log`, copied exactly from `build/test-results/Java_Verification_20261015_174248_559.log` |

Offline results were reused after the runner's final evidence-copy/formatting change; the Java harness hash stayed unchanged. The final runner compiled 55 sources and reran live SQL/JDBC on its new QA database. The separate same-connection DAO diagnostic records transaction state; the Service batch scenario proves rollback through generated IDs and persisted-row checks.

## 2. Integrity & Cryptographic Hashes
- **Canonical Feature SQL**: `database/02_Module_ChamCong_TV2.sql` -> 4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471
- **Package Feature SQL**: `Bo_nop_Nhom06/database/02_Module_ChamCong_TV2.sql` -> 4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471
- **Harness Source**: `src/test/java/com/test/Tv2JdbcIntegrationTest.java` -> 2E9699939E3516E3E27DD47BC29FBFF833050A5E494BC5B30228DD0116F176DD
- **Runner Script**: `database/tests_TV2/run_tv2_jdbc_optionb.ps1` -> 87152D42575C190B7DA1CBE9786427EC9CAB2DA1ABC29FC1A0C7C7A2F22EC508

## 3. Test Suites & Assertions Count
- **Offline Security Regression**: `SecurityRegressionTest.log` (104 assertions PASS).
- **Offline GUI QA Regression**: `GuiQaRegressionTest.log` (88 assertions PASS).
- **Negative Guards**: 8 guard rejection assertions PASS (demonstrated before DB connection).
- **Installed Procedure Definition**: Verified `TRY...CATCH` + bare `THROW;` with 7 parameters and `MaChamCong OUTPUT`.
- **SQL Integration Suite** (`database/tests_TV2/test_module_chamcong_TV2.sql`): **21 / 21 PASS (100%)**.
- **Concurrency Verification** (`database/tests/Closed_Source_Concurrency.ps1`): **PASS**.
- **Live JDBC Integration Test** (`Tv2JdbcIntegrationTest`): **All 6 scenarios PASS** (87 total assertions).
  - Scenario 1 (Valid batch commit): PASS (3 rows committed, OUTPUT IDs assigned).
  - Scenario 2 (Duplicate rollback & diagnostics): PASS (rows 1 & 2 rolled back to count=0, pre-existing record intact; @@TRANCOUNT=1 and XACT_STATE=1 in failing context before rollback, 0 after rollback; typed SQLServerError verified: Number=50000, Severity=16, State=1).
  - Scenario 3 (Java pre-validation): PASS (future date rejected, 0 rows written).
  - Scenario 4A (Inactive employee with prior valid insert): PASS (prior row rolled back, inactive row rejected; typed SQLServerError verified: Number=50000, Severity=16, State=1).
  - Scenario 4B (Closed period with prior valid insert): PASS (prior open-period row rolled back, closed row rejected by trigger; typed SQLServerError verified: Number=50000, Severity=16, State=1).
  - Scenario 5 (Single insert): PASS (OUTPUT generated; duplicate raises SQLException with exact message; typed SQLServerError verified: Number=50000, Severity=16, State=1).
  - Scenario 6 (Adjustment & aggregation): PASS (updated, view aggregates 2 days / 18.0h, move to closed period rejected; typed SQLServerError verified: Number=50000, Severity=16, State=1).

## 4. Exact Vietnamese Error Text & Typed SQLServerError Metadata Preserved
- Duplicate attendance error: `Nhân viên đã có bản ghi chấm công trong ngày này!` (Error Code: 50000, Severity: 16, State: 1)
- Inactive employee error: `Không thể ghi nhận chấm công cho nhân viên đã nghỉ việc hoặc không hoạt động!` (Error Code: 50000, Severity: 16, State: 1)
- Closed payroll period trigger error: `Kỳ lương đã chốt: không được thay đổi chấm công.` (Error Code: 50000, Severity: 16, State: 1)

## 5. Disposable Database Cleanup
- Ownership marker validated against DB before drop.
- Disposable database `PRJ_Fix_QA_20261015_175527_ea6da333` was dropped successfully after marker verification, as recorded in `environment_cleanup.log`. The executor reports a subsequent absence check; this evidence set does not contain the output of a separate `DB_ID` query.
