# PR 52 independent review, round 2

Intermediate review after the first repair loop; this is not final acceptance.

- Reviewer: fresh read-only `reviewer` role from `reviewer.toml`.
- Agent ID: `01a14004-c978-7d72-bb69-dca946241eeb`.
- Builder: Antigravity Desktop job `2026-10-15-009`.
- Verdict: **FAIL**. Codex adjudicated the findings below as VALID and TASK_CAUSED.

## Reviewed hashes

| File | SHA256 |
|---|---|
| `database/tests_TV2/run_tv2_jdbc_optionb.ps1` | `7778538BE1D6C5FC4E4DE01E34EFAF74C8155F74BA1725421EAB26795D9F21AF` |
| `src/test/java/com/test/Tv2JdbcIntegrationTest.java` | `BAA0081EC95116F73E8D97D7396942809531F6AFA7F2A820042CCD203645C6F1` |
| `database/tests_TV2/test_tv5_findings.ps1` | `5DDA8EEE034668DBFADD31D36E8FCA24B799D1FA086B8823D8B42866496B4BE3` |

## Required fixes

1. **Critical:** runner lines 820-829 retry DROP after a failed pipeline using name checks only. This bypasses ownership-marker verification, can run after a normal marker rejection, does not prove absence, and swallows cleanup errors. Use one guarded cleanup routine from `finally`; require successful marker query and exact marker equality before every DROP, then independently verify absence and persist actual status.
2. Runner prerequisite checks near line 20 precede evidence/stage initialization. Missing SQLCMD, JDK or JDBC JAR must be recorded as failed preflight with a fresh summary and nonzero exit.
3. The 69-check suite's forced SQL failure does not assert emergency cleanup ownership checks, DROP suppression, status or absence. Add production-runner mock tests for post-create failures with missing/mismatched markers, valid guarded cleanup, and cleanup-query failures.

The intermediate run `../optionb-20261015/20261015_214120_65d0e7a7/` records JDBC failure but cleanup as SKIPPED / Not executed and has no absence evidence. Its database must remain unverified until a separate read-only check or guarded recovery produces evidence.

Codex additionally inspected runner lines 724-734: a JDBC completion marker plus a numeric total of zero is accepted because only UNKNOWN is rejected. The required malformed-output protection therefore needs a positive total, unique completion/count markers, matching actual PASS rows and no failure marker. Runner lines 204-206 also overwrite an explicitly supplied existing evidence directory; reject nonempty existing output directories to preserve historical evidence. Explicit invalid blank endpoints must fail rather than fall back to localhost.

## Happy-path evidence is separate

For `../optionb-20261015/20261015_214512_8f198420/`, Codex read the raw JDBC output and counted exactly 105 PASS rows, one completion marker, one reported total of 105 and no test-failure marker. The separate absence log records count 0 and SQLCMD exit 0. These observations validate the successful path for the hashes above; they do not validate failure cleanup.

Read-only AST parsing in Windows PowerShell 5.1.26100.7920 found no parser errors in this runner and test snapshot; the machine-readable record is `codex_ps51_parse.json`.

The final authorized repair loop was delegated to Antigravity Desktop job `2026-10-15-010`. Maximum repair loops: two; no further repair loop may be started after that job. No Git metadata writes or delivery were performed.
