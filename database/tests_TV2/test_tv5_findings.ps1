# TV5 Review Findings Negative & Unit Test Harness
# Exercises:
# 1. In-process negative guards via Tv2JdbcIntegrationTest (24 checks incl missing expectations, unported named instances, delimiter injection)
# 2. Windows PS5.1 argument quoting with single space, multiple spaces, real JDBC driver loading, -D props, and nonzero exit code
# 3. Deterministic endpoint resolution matrix (default local, host:port, host,port, ported named instances, deterministic unported rejection, injection rejection)
# 4. ACTUAL production runner orchestration using mocked boundary commands/no-DB (persisted summaries + exit codes for compile/guard/SQL/concurrency/KeepDatabase/DROP/absence/full PASS)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..') | Select-Object -ExpandProperty Path
$runnerPath = Join-Path $repoRoot 'database\tests_TV2\run_tv2_jdbc_optionb.ps1'

# Dot-source helper functions directly into the current execution scope
. $runnerPath -HelpersOnly

$totalChecks = 0
$passedChecks = 0

function Assert-Condition([bool]$condition, [string]$label) {
    $script:totalChecks++
    if (-not $condition) {
        throw "ASSERTION FAILED: $label"
    }
    $script:passedChecks++
    Write-Host "  [PASS] $label"
}

Write-Host "========================================================"
Write-Host "TV5 REVIEW FINDINGS CONTROLLED NEGATIVE TEST SUITE"
Write-Host "========================================================"

# Locate classpath for tests
$classDir = Join-Path $repoRoot 'build\verification\classes'
$candidates = @()
foreach ($dir in @((Join-Path $repoRoot 'lib'), (Join-Path $env:USERPROFILE '.m2\repository\com\microsoft\sqlserver\mssql-jdbc'))) {
    if (Test-Path -LiteralPath $dir) {
        $candidates += Get-ChildItem -LiteralPath $dir -Recurse -File -Filter 'mssql-jdbc*.jar' | Where-Object { $_.Name -notmatch 'sources|javadoc' }
    }
}
$jdbcJar = ($candidates | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName
$classpath = "$classDir;$(Join-Path $repoRoot 'src\resources');$jdbcJar"

# -------------------------------------------------------------------------
# TEST GROUP 1: In-process Negative Guards via Tv2JdbcIntegrationTest
# -------------------------------------------------------------------------
Write-Host "`n--- TEST GROUP 1: Negative Guards (Target, Marker, Credentials, Identity Mismatch) ---"
$guardArgs = @("-cp", $classpath, "-Dfile.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8", "-Dqa.guards.only=true", "com.test.Tv2JdbcIntegrationTest")
$guardRes = Invoke-JavaProcess "java" $guardArgs
$guardText = $guardRes.Output

Assert-Condition ($guardRes.ExitCode -eq 0) "Runner exited with 0 for negative guards"
Assert-Condition ($guardText -match 'Negative guard rejected null DB name') "Rejected null DB name"
Assert-Condition ($guardText -match 'Negative guard rejected production DB name') "Rejected production DB name"
Assert-Condition ($guardText -match 'Negative guard rejected protected baseline QA DB') "Rejected protected baseline QA DB"
Assert-Condition ($guardText -match 'Negative guard rejected superseded dropped QA DB') "Rejected superseded dropped QA DB"
Assert-Condition ($guardText -match 'Negative guard rejected non-conforming QA DB name pattern') "Rejected non-conforming QA DB pattern"
Assert-Condition ($guardText -match 'Negative guard rejected null ownership marker') "Rejected null ownership marker"
Assert-Condition ($guardText -match 'Negative guard rejected missing environment credentials') "Rejected missing credentials"
Assert-Condition ($guardText -match 'Negative guard rejected missing expected server name') "Rejected missing expected server name"
Assert-Condition ($guardText -match 'Negative guard rejected missing expected instance name') "Rejected missing expected instance name"
Assert-Condition ($guardText -match 'Negative guard rejected missing expected machine name') "Rejected missing expected machine name"
Assert-Condition ($guardText -match 'Negative guard rejected invalid expected process ID') "Rejected invalid expected process ID"
Assert-Condition ($guardText -match 'Negative guard rejected server name mismatch') "Rejected server name mismatch"
Assert-Condition ($guardText -match 'Negative guard rejected instance name mismatch') "Rejected instance name mismatch"
Assert-Condition ($guardText -match 'Negative guard rejected machine name mismatch') "Rejected machine name mismatch"
Assert-Condition ($guardText -match 'Negative guard rejected process ID mismatch') "Rejected process ID mismatch"
Assert-Condition ($guardText -match 'Negative guard rejected invalid server instance format') "Rejected invalid instance format"
Assert-Condition ($guardText -match 'Negative guard rejected out-of-range TCP port') "Rejected out-of-range TCP port"
Assert-Condition ($guardText -match 'Negative guard rejected unported named instance') "Rejected unported named instance"
Assert-Condition ($guardText -match 'Negative guard rejected server instance delimiter injection') "Rejected server instance delimiter injection"
Assert-Condition ($guardText -match 'Negative guard rejected database name delimiter injection') "Rejected database name delimiter injection"
Assert-Condition ($guardText -match 'Negative guard rejected missing endpoint configuration') "Rejected missing endpoint"
Assert-Condition ($guardText -match 'Negative guard confirmed default local instance resolves to localhost:1433') "Confirmed local endpoint resolution"
Assert-Condition ($guardText -match 'Negative guard confirmed ported named instance resolves with explicit TCP port') "Confirmed ported named instance resolution"

# -------------------------------------------------------------------------
# TEST GROUP 2: Windows PS5.1 Argument Quoting (Spaces, Multi-spaces, Real Driver, Exit Code)
# -------------------------------------------------------------------------
Write-Host "`n--- TEST GROUP 2: Classpath with Spaces, Multi-spaces, Driver Loading, and Exit Code ---"
$tempBase = Join-Path ([System.IO.Path]::GetTempPath()) ("tv5_quote_test_" + [Guid]::NewGuid().ToString('N').Substring(0, 8))
$singleSpaceDir = Join-Path $tempBase "path with single space"
$multiSpaceDir  = Join-Path $tempBase "path   with   multiple   spaces"

New-Item -ItemType Directory -Force -Path $singleSpaceDir | Out-Null
New-Item -ItemType Directory -Force -Path $multiSpaceDir | Out-Null

try {
    # Create a Java source that loads the real JDBC driver and reads system properties
    $javaSrc = @"
package com.test.quote;
public class SpaceJdbcRunner {
    public static void main(String[] args) {
        try {
            Class<?> driverClass = Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("DRIVER_LOADED: " + driverClass.getName());
        } catch (Throwable t) {
            System.err.println("DRIVER_LOAD_FAILED: " + t.getMessage());
            System.exit(10);
        }

        String prop = System.getProperty("test.prop");
        System.out.println("PROP_VALUE: " + prop);
        System.out.println("SPACE_RUNNER_OK: args=" + args.length);

        if (args.length > 0 && args[0].equals("exit42")) {
            System.err.println("PROVOKED_EXIT_42");
            System.exit(42);
        }
    }
}
"@
    $pkgDir = Join-Path $singleSpaceDir "com\test\quote"
    New-Item -ItemType Directory -Force -Path $pkgDir | Out-Null
    $srcPath = Join-Path $pkgDir "SpaceJdbcRunner.java"
    $utf8NoBom = [System.Text.UTF8Encoding]::new($false)
    [System.IO.File]::WriteAllText($srcPath, $javaSrc, $utf8NoBom)

    # Compile with JDBC JAR on classpath
    & javac -encoding UTF-8 -cp $jdbcJar -d $singleSpaceDir $srcPath
    Assert-Condition ($LASTEXITCODE -eq 0) "Compiled SpaceJdbcRunner in single-space directory"

    # Copy class to multiSpaceDir to test compound classpath
    Copy-Item -Recurse (Join-Path $singleSpaceDir 'com') (Join-Path $multiSpaceDir 'com')

    # Multi-path compound classpath with normal, single-space, and multi-space entries
    $testCompoundCp = "$classDir;$singleSpaceDir;$multiSpaceDir;$jdbcJar"

    # Invoke Java with normal arguments directly in-process
    $procRes = Invoke-JavaProcess "java" @("-cp", $testCompoundCp, "-Dtest.prop=value with spaces and quotes", "com.test.quote.SpaceJdbcRunner", "normal_arg", "arg with space")
    Assert-Condition ($procRes.ExitCode -eq 0) "Normal invocation with spaced classpath exited 0"
    Assert-Condition ($procRes.Output -match 'DRIVER_LOADED:\s*com\.microsoft\.sqlserver\.jdbc\.SQLServerDriver') "Actual SQL Server JDBC driver loaded from compound spaced classpath"
    Assert-Condition ($procRes.Output -match 'PROP_VALUE:\s*value with spaces and quotes') "System property with spaces received accurately"
    Assert-Condition ($procRes.Output -match 'SPACE_RUNNER_OK: args=2') "Spaced classpath passed multiple arguments correctly"

    # Invoke Java expecting exit code 42 directly in-process
    $exitRes = Invoke-JavaProcess "java" @("-cp", $testCompoundCp, "com.test.quote.SpaceJdbcRunner", "exit42")
    Assert-Condition ($exitRes.ExitCode -eq 42) "Nonzero Java exit code (42) preserved and propagated"
    Assert-Condition ($exitRes.Output -match 'PROVOKED_EXIT_42') "Stderr captured asynchronously from Java child process"

} finally {
    Remove-Item -LiteralPath $tempBase -Recurse -Force -ErrorAction SilentlyContinue
}

# -------------------------------------------------------------------------
# TEST GROUP 3: Endpoint Resolution Matrix (Deterministic, No SQLBrowser Dependency)
# -------------------------------------------------------------------------
Write-Host "`n--- TEST GROUP 3: Endpoint Resolution Matrix ---"
$e1 = Resolve-SqlEndpoint -InstanceInput '.' -DatabaseName 'PRJ_Fix_QA_test'
Assert-Condition ($e1.SqlcmdServer -eq 'localhost,1433') "Default '.' resolves to SQLCMD 'localhost,1433' (shared explicit TCP)"
Assert-Condition ($e1.JdbcUrl -eq 'jdbc:sqlserver://localhost:1433;databaseName=PRJ_Fix_QA_test;encrypt=true;trustServerCertificate=true') "Default '.' resolves to JDBC localhost:1433"

$e2 = Resolve-SqlEndpoint -InstanceInput 'localhost:1433' -DatabaseName 'PRJ_Fix_QA_test'
Assert-Condition ($e2.SqlcmdServer -eq 'localhost,1433') "localhost:1433 resolves to SQLCMD comma format"
Assert-Condition ($e2.JdbcUrl -eq 'jdbc:sqlserver://localhost:1433;databaseName=PRJ_Fix_QA_test;encrypt=true;trustServerCertificate=true') "localhost:1433 resolves to JDBC colon format"

$e3 = Resolve-SqlEndpoint -InstanceInput '127.0.0.1,1433' -DatabaseName 'PRJ_Fix_QA_test'
Assert-Condition ($e3.SqlcmdServer -eq '127.0.0.1,1433') "127.0.0.1,1433 preserves SQLCMD format"
Assert-Condition ($e3.JdbcUrl -eq 'jdbc:sqlserver://127.0.0.1:1433;databaseName=PRJ_Fix_QA_test;encrypt=true;trustServerCertificate=true') "127.0.0.1,1433 formats JDBC correctly"

$e4 = Resolve-SqlEndpoint -InstanceInput 'customhost:14333' -DatabaseName 'PRJ_Fix_QA_test'
Assert-Condition ($e4.SqlcmdServer -eq 'customhost,14333') "customhost:14333 preserves port for SQLCMD"
Assert-Condition ($e4.JdbcUrl -eq 'jdbc:sqlserver://customhost:14333;databaseName=PRJ_Fix_QA_test;encrypt=true;trustServerCertificate=true') "customhost:14333 preserves port for JDBC"

$e5 = Resolve-SqlEndpoint -InstanceInput 'myhost\myinstance,1433' -DatabaseName 'PRJ_Fix_QA_test'
Assert-Condition ($e5.SqlcmdServer -eq 'myhost\myinstance,1433') "Explicit port named instance formats SQLCMD correctly"
Assert-Condition ($e5.JdbcUrl.Contains('myhost:1433;instanceName=myinstance')) "Explicit port named instance formats JDBC with explicit port and instanceName"

# Test deterministic unported named instance rejection (without checking SQL Browser)
$e6Rejected = $false
try {
    $null = Resolve-SqlEndpoint -InstanceInput '.\SQLEXPRESS' -DatabaseName 'PRJ_Fix_QA_test'
} catch {
    if ($_.Exception.Message -match 'Unported named instance.*rejected') {
        $e6Rejected = $true
    }
}
Assert-Condition ($e6Rejected) "Unported named instance deterministically rejected without SQLBrowser query"

# Test delimiter injection rejection
$e7Rejected = $false
try {
    $null = Resolve-SqlEndpoint -InstanceInput 'localhost;databaseName=evil;' -DatabaseName 'PRJ_Fix_QA_test'
} catch {
    if ($_.Exception.Message -match 'contains disallowed delimiter') {
        $e7Rejected = $true
    }
}
Assert-Condition ($e7Rejected) "Endpoint containing delimiter characters safely rejected"

# Test invalid endpoint syntax rejection
$e8Rejected = $false
try {
    $null = Resolve-SqlEndpoint -InstanceInput 'invalid:::server' -DatabaseName 'PRJ_Fix_QA_test'
} catch {
    if ($_.Exception.Message -match 'Invalid or ambiguous SQL Server endpoint') {
        $e8Rejected = $true
    }
}
Assert-Condition ($e8Rejected) "Invalid endpoint syntax safely rejected"

# Test explicit blank and whitespace endpoint rejection
$eBlank1Rejected = $false
try {
    $null = Resolve-SqlEndpoint -InstanceInput '' -DatabaseName 'PRJ_Fix_QA_test'
} catch {
    if ($_.Exception.Message -match 'cannot be empty or whitespace') {
        $eBlank1Rejected = $true
    }
}
Assert-Condition ($eBlank1Rejected) "Explicit empty endpoint deterministically rejected"

$eBlank2Rejected = $false
try {
    $null = Resolve-SqlEndpoint -InstanceInput '   ' -DatabaseName 'PRJ_Fix_QA_test'
} catch {
    if ($_.Exception.Message -match 'cannot be empty or whitespace') {
        $eBlank2Rejected = $true
    }
}
Assert-Condition ($eBlank2Rejected) "Explicit whitespace endpoint deterministically rejected"

# -------------------------------------------------------------------------
# TEST GROUP 4: ACTUAL Production Runner Orchestration with Mocked Boundaries
# -------------------------------------------------------------------------
Write-Host "`n--- TEST GROUP 4: Production Runner Orchestration with Mocked Boundaries ---"
$origTv2QaPwd = $env:TV2_QA_PASSWORD
$testOrchDir = Join-Path ([System.IO.Path]::GetTempPath()) ("tv5_orch_test_" + [Guid]::NewGuid().ToString('N').Substring(0, 8))
New-Item -ItemType Directory -Force -Path $testOrchDir | Out-Null

try {
    if ([string]::IsNullOrWhiteSpace($env:TV2_QA_PASSWORD) -and [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        $env:TV2_QA_PASSWORD = 'MockPassword123!'
    }

    # Helper valid JDBC mock returning matching [PASS] assertion rows and exact markers
    $validJdbcMock = { param($args, $log)
        $txt = @"
  [PASS] Scenario 1: Attendance insertion verified
  [PASS] Scenario 2: Rollback diagnostic verified
========================================================
ALL 6 JDBC SCENARIOS COMPLETED SUCCESSFULLY!
TOTAL ASSERTIONS PASSED: 2
========================================================
"@
        [System.IO.File]::WriteAllText($log, $txt, [System.Text.Encoding]::UTF8)
        [PSCustomObject]@{ ExitCode = 0; Output = $txt; Stdout = $txt; Stderr = '' }
    }

    # Base valid mock set
    $baseValidMock = @{
        'javac' = { param($args, $out) [PSCustomObject]@{ ExitCode = 0; Output = ''; Stdout = ''; Stderr = '' } }
        'guard' = { param($args, $out) [PSCustomObject]@{ ExitCode = 0; Output = "[PASS] 1`nAll negative guard tests passed successfully"; Stdout = "[PASS] 1`nAll negative guard tests passed successfully"; Stderr = '' } }
        'security' = { param($args, $out) [PSCustomObject]@{ ExitCode = 0; Output = 'PASS offline Java regressions: 104 assertions'; Stdout = 'PASS offline Java regressions: 104 assertions'; Stderr = '' } }
        'gui' = { param($args, $out) [PSCustomObject]@{ ExitCode = 0; Output = 'PASS GUI QA regressions: 88 assertions'; Stdout = 'PASS GUI QA regressions: 88 assertions'; Stderr = '' } }
        'identity' = { "laptop-cua-mq|MSSQLSERVER|laptop-cua-mq|4112" }
        'sqlcmd_text' = { param($text, $cat) }
        'sqlcmd_file' = { param($f, $l) }
        'proc_verify' = { "PASS: HasTryCatchThrow | PASS: HasMaChamCongOutput | 7" }
        'sql_suite' = { param($log) [System.IO.File]::WriteAllText($log, "TONG SO KICH BAN THUC THI: 21`nSO KICH BAN PASS: 21`nSO KICH BAN FAIL: 0", [System.Text.Encoding]::UTF8) }
        'concurrency' = { "PASS close versus attendance: waited 1000 ms, rejected, source unchanged.`nPASS close versus allowance: waited 1000 ms, rejected, source unchanged.`nPASS close versus deduction: waited 1000 ms, rejected, source unchanged.`nPASS close versus payroll detail: waited 1000 ms, rejected, source unchanged.`nPASS closing-race fixture cleanup." }
        'jdbc' = $validJdbcMock
        'cleanup_marker' = { param($m) $m }
        'drop_db' = { }
        'absence_check' = { "0" }
    }

    # Test 4.1: Forced compilation failure -> summary has JavaCompilation FAIL, downstream SKIPPED, exit nonzero
    $dir41 = Join-Path $testOrchDir "run_41"
    $mock41 = @{
        'javac' = { param($args, $out) [PSCustomObject]@{ ExitCode = 1; Output = 'javac: mock syntax error'; Stdout = ''; Stderr = 'mock syntax error' } }
    }
    $res41 = & $runnerPath -EvidenceOutputDir $dir41 -MockInvocations $mock41 2>&1
    $exit41 = $LASTEXITCODE
    $summary41 = Get-Content -LiteralPath (Join-Path $dir41 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit41 -ne 0) "Forced compile failure returns nonzero exit code ($exit41)"
    Assert-Condition ($summary41 -match 'Java Compilation.*\[FAIL\]') "Persisted summary records JavaCompilation as [FAIL]"
    Assert-Condition ($summary41 -match 'Negative Guards.*\[SKIPPED\]') "Persisted summary records NegativeGuards as [SKIPPED]"
    Assert-Condition ($summary41 -match 'Sql Integration Suite.*\[SKIPPED\]') "Persisted summary records SqlTestSuite as [SKIPPED]"
    Assert-Condition ($summary41 -match 'Overall Status.*FAIL') "Persisted summary records Overall Status as FAIL"

    # Test 4.2: Preflight tool probe: Mock missing JDK (javac/java) -> JavaCompilation FAIL, exit 2
    $dir42 = Join-Path $testOrchDir "run_42"
    $mock42 = @{ 'mock_javac_missing' = $true }
    $res42 = & $runnerPath -EvidenceOutputDir $dir42 -MockInvocations $mock42 2>&1
    $exit42 = $LASTEXITCODE
    $summary42 = Get-Content -LiteralPath (Join-Path $dir42 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit42 -eq 2) "Missing JDK probe returns exit code 2 ($exit42)"
    Assert-Condition ($summary42 -match 'Java Compilation.*\[FAIL\]') "Missing JDK probe records JavaCompilation as [FAIL]"
    Assert-Condition ($summary42 -match 'JDK 11\+ \(javac and java\) is required but missing') "Summary detail explains missing JDK"
    Assert-Condition ($summary42 -match 'Overall Status.*FAIL') "Missing JDK causes Overall Status FAIL"

    # Test 4.3: Preflight tool probe: Mock missing JDBC JAR -> JavaCompilation FAIL, exit 2
    $dir43 = Join-Path $testOrchDir "run_43"
    $mock43 = @{ 'mock_jdbcjar_missing' = $true }
    $res43 = & $runnerPath -EvidenceOutputDir $dir43 -MockInvocations $mock43 2>&1
    $exit43 = $LASTEXITCODE
    $summary43 = Get-Content -LiteralPath (Join-Path $dir43 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit43 -eq 2) "Missing JDBC JAR probe returns exit code 2 ($exit43)"
    Assert-Condition ($summary43 -match 'Java Compilation.*\[FAIL\]') "Missing JDBC JAR records JavaCompilation as [FAIL]"
    Assert-Condition ($summary43 -match 'SQL Server JDBC JAR is required but missing') "Summary detail explains missing JDBC JAR"
    Assert-Condition ($summary43 -match 'Overall Status.*FAIL') "Missing JDBC JAR causes Overall Status FAIL"

    # Test 4.4: Preflight tool probe: Mock missing SQLCMD tool -> SqlInstallation FAIL, exit 2
    $dir44 = Join-Path $testOrchDir "run_44"
    $mock44 = @{ 'mock_sqlcmd_missing' = $true }
    $res44 = & $runnerPath -EvidenceOutputDir $dir44 -MockInvocations $mock44 2>&1
    $exit44 = $LASTEXITCODE
    $summary44 = Get-Content -LiteralPath (Join-Path $dir44 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit44 -eq 2) "Missing SQLCMD probe returns exit code 2 ($exit44)"
    Assert-Condition ($summary44 -match 'SQL Schema Installation.*\[FAIL\]') "Missing SQLCMD records SqlInstallation as [FAIL]"
    Assert-Condition ($summary44 -match 'SQLCMD tool is required but missing') "Summary detail explains missing SQLCMD"
    Assert-Condition ($summary44 -match 'Overall Status.*FAIL') "Missing SQLCMD causes Overall Status FAIL"

    # Test 4.5: Isolated sentinel directory test: non-empty EvidenceOutputDir rejected and preserved
    $sentinelDir = Join-Path $testOrchDir "sentinel_dir"
    New-Item -ItemType Directory -Force -Path $sentinelDir | Out-Null
    $sentinelFile = Join-Path $sentinelDir "sentinel.txt"
    [System.IO.File]::WriteAllText($sentinelFile, "SENTINEL_CONTENT_DO_NOT_DELETE", [System.Text.Encoding]::UTF8)

    $res45 = $null
    $exit45 = 0
    try {
        $res45 = & $runnerPath -EvidenceOutputDir $sentinelDir 2>&1
        $exit45 = $LASTEXITCODE
    } catch {
        $exit45 = 1
        $res45 = $_.Exception.Message
    }

    Assert-Condition ($exit45 -ne 0) "Runner rejects non-empty EvidenceOutputDir ($exit45)"
    Assert-Condition (Test-Path -LiteralPath $sentinelFile) "Existing sentinel file remains on disk"
    $sentinelContent = Get-Content -LiteralPath $sentinelFile -Raw -Encoding UTF8
    Assert-Condition ($sentinelContent -match 'SENTINEL_CONTENT_DO_NOT_DELETE') "Sentinel file content preserved intact"

    # Test 4.6: Explicit whitespace ServerInstance rejected before mutation
    $dir46 = Join-Path $testOrchDir "run_46"
    $res46 = & $runnerPath -ServerInstance "   " -EvidenceOutputDir $dir46 -MockInvocations $baseValidMock 2>&1
    $exit46 = $LASTEXITCODE
    $summary46 = Get-Content -LiteralPath (Join-Path $dir46 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit46 -ne 0) "Whitespace ServerInstance parameter rejected with nonzero exit code ($exit46)"
    Assert-Condition ($summary46 -match 'SQL Schema Installation.*\[FAIL\]') "Persisted summary records SqlInstallation as [FAIL]"
    Assert-Condition ($summary46 -match 'cannot be empty or whitespace') "Summary detail explains empty/whitespace endpoint rejection"

    # Test 4.7: Negative guard child exit 0 but missing completion marker -> NOT_VERIFIED, exit nonzero
    $dir47 = Join-Path $testOrchDir "run_47"
    $mock47 = @{
        'javac' = { param($args, $out) [PSCustomObject]@{ ExitCode = 0; Output = ''; Stdout = ''; Stderr = '' } }
        'guard' = { param($args, $out) [PSCustomObject]@{ ExitCode = 0; Output = '[PASS] Step 1 passed'; Stdout = '[PASS] Step 1 passed'; Stderr = '' } }
    }
    $res47 = & $runnerPath -EvidenceOutputDir $dir47 -MockInvocations $mock47 2>&1
    $exit47 = $LASTEXITCODE
    $summary47 = Get-Content -LiteralPath (Join-Path $dir47 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit47 -ne 0) "Missing guard marker with child exit 0 returns nonzero exit code ($exit47)"
    Assert-Condition ($summary47 -match 'Negative Guards.*\[NOT_VERIFIED\]') "Persisted summary records NegativeGuards as [NOT_VERIFIED]"
    Assert-Condition ($summary47 -match 'Missing negative guard completion marker') "Persisted summary detail explains missing marker"
    Assert-Condition ($summary47 -match 'Overall Status.*FAIL') "Missing guard marker causes Overall Status FAIL"

    # Test 4.8: Negative guard child exit nonzero (e.g. exit 2)
    $dir48 = Join-Path $testOrchDir "run_48"
    $mock48 = @{
        'javac' = { param($args, $out) [PSCustomObject]@{ ExitCode = 0; Output = ''; Stdout = ''; Stderr = '' } }
        'guard' = { param($args, $out) [PSCustomObject]@{ ExitCode = 2; Output = 'AssertionError'; Stdout = ''; Stderr = 'AssertionError' } }
    }
    $res48 = & $runnerPath -EvidenceOutputDir $dir48 -MockInvocations $mock48 2>&1
    $exit48 = $LASTEXITCODE
    $summary48 = Get-Content -LiteralPath (Join-Path $dir48 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit48 -eq 2) "Child exit 2 propagates accurately to runner exit ($exit48)"
    Assert-Condition ($summary48 -match 'Negative Guards.*\[FAIL\]') "Persisted summary records NegativeGuards as [FAIL]"

    # Test 4.9: Forced SQL stage failure -> summary has SqlTestSuite FAIL, downstream SKIPPED, exit nonzero
    $dir49 = Join-Path $testOrchDir "run_49"
    $mock49 = [hashtable]$baseValidMock.Clone()
    $mock49['sql_suite'] = { param($log) [System.IO.File]::WriteAllText($log, "TONG SO KICH BAN THUC THI: 21`nSO KICH BAN PASS: 20`nSO KICH BAN FAIL: 1", [System.Text.Encoding]::UTF8) }

    $res49 = & $runnerPath -EvidenceOutputDir $dir49 -MockInvocations $mock49 2>&1
    $exit49 = $LASTEXITCODE
    $summary49 = Get-Content -LiteralPath (Join-Path $dir49 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit49 -ne 0) "SQL stage failure returns nonzero exit code ($exit49)"
    Assert-Condition ($summary49 -match 'SQL Integration Suite.*\[FAIL\]') "Persisted summary records SqlTestSuite as [FAIL]"
    Assert-Condition ($summary49 -match 'Concurrency Verification.*\[SKIPPED\]') "Downstream Concurrency reported [SKIPPED]"
    Assert-Condition ($summary49 -match 'Live JDBC Integration Test.*\[SKIPPED\]') "Downstream JdbcIntegration reported [SKIPPED]"

    # Test 4.10: Hardened JDBC log parse: Total assertions 0 -> NOT_VERIFIED, exit nonzero
    $dir410 = Join-Path $testOrchDir "run_410"
    $mock410 = [hashtable]$baseValidMock.Clone()
    $mock410['jdbc'] = { param($args, $log)
        $txt = "ALL 6 JDBC SCENARIOS COMPLETED SUCCESSFULLY!`nTOTAL ASSERTIONS PASSED: 0`n"
        [System.IO.File]::WriteAllText($log, $txt, [System.Text.Encoding]::UTF8)
        [PSCustomObject]@{ ExitCode = 0; Output = $txt; Stdout = $txt; Stderr = '' }
    }
    $res410 = & $runnerPath -EvidenceOutputDir $dir410 -MockInvocations $mock410 2>&1
    $exit410 = $LASTEXITCODE
    $summary410 = Get-Content -LiteralPath (Join-Path $dir410 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit410 -ne 0) "JDBC total assertions 0 returns nonzero exit code ($exit410)"
    Assert-Condition ($summary410 -match 'Live JDBC Integration Test.*\[NOT_VERIFIED\]') "Persisted summary records JdbcIntegration as [NOT_VERIFIED] on zero count"

    # Test 4.11: Hardened JDBC log parse: Total count mismatch vs [PASS] rows -> NOT_VERIFIED, exit nonzero
    $dir411 = Join-Path $testOrchDir "run_411"
    $mock411 = [hashtable]$baseValidMock.Clone()
    $mock411['jdbc'] = { param($args, $log)
        $txt = "  [PASS] Only one assertion`nALL 6 JDBC SCENARIOS COMPLETED SUCCESSFULLY!`nTOTAL ASSERTIONS PASSED: 5`n"
        [System.IO.File]::WriteAllText($log, $txt, [System.Text.Encoding]::UTF8)
        [PSCustomObject]@{ ExitCode = 0; Output = $txt; Stdout = $txt; Stderr = '' }
    }
    $res411 = & $runnerPath -EvidenceOutputDir $dir411 -MockInvocations $mock411 2>&1
    $exit411 = $LASTEXITCODE
    $summary411 = Get-Content -LiteralPath (Join-Path $dir411 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit411 -ne 0) "JDBC row count mismatch returns nonzero exit code ($exit411)"
    Assert-Condition ($summary411 -match 'Live JDBC Integration Test.*\[NOT_VERIFIED\]') "Persisted summary records JdbcIntegration as [NOT_VERIFIED] on count mismatch"

    # Test 4.12: Hardened JDBC log parse: Duplicate completion marker -> NOT_VERIFIED, exit nonzero
    $dir412 = Join-Path $testOrchDir "run_412"
    $mock412 = [hashtable]$baseValidMock.Clone()
    $mock412['jdbc'] = { param($args, $log)
        $txt = "  [PASS] Row 1`nALL 6 JDBC SCENARIOS COMPLETED SUCCESSFULLY!`nALL 6 JDBC SCENARIOS COMPLETED SUCCESSFULLY!`nTOTAL ASSERTIONS PASSED: 1`n"
        [System.IO.File]::WriteAllText($log, $txt, [System.Text.Encoding]::UTF8)
        [PSCustomObject]@{ ExitCode = 0; Output = $txt; Stdout = $txt; Stderr = '' }
    }
    $res412 = & $runnerPath -EvidenceOutputDir $dir412 -MockInvocations $mock412 2>&1
    $exit412 = $LASTEXITCODE
    $summary412 = Get-Content -LiteralPath (Join-Path $dir412 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit412 -ne 0) "JDBC duplicate completion marker returns nonzero exit code ($exit412)"
    Assert-Condition ($summary412 -match 'Live JDBC Integration Test.*\[NOT_VERIFIED\]') "Persisted summary records JdbcIntegration as [NOT_VERIFIED] on duplicate marker"

    # Test 4.13: Hardened JDBC log parse: Failure marker present with child exit 0 -> FAIL, exit nonzero
    $dir413 = Join-Path $testOrchDir "run_413"
    $mock413 = [hashtable]$baseValidMock.Clone()
    $mock413['jdbc'] = { param($args, $log)
        $txt = "  [PASS] Row 1`n  [FAIL] Row 2 failed`nALL 6 JDBC SCENARIOS COMPLETED SUCCESSFULLY!`nTOTAL ASSERTIONS PASSED: 1`n"
        [System.IO.File]::WriteAllText($log, $txt, [System.Text.Encoding]::UTF8)
        [PSCustomObject]@{ ExitCode = 0; Output = $txt; Stdout = $txt; Stderr = '' }
    }
    $res413 = & $runnerPath -EvidenceOutputDir $dir413 -MockInvocations $mock413 2>&1
    $exit413 = $LASTEXITCODE
    $summary413 = Get-Content -LiteralPath (Join-Path $dir413 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit413 -ne 0) "JDBC failure marker returns nonzero exit code ($exit413)"
    Assert-Condition ($summary413 -match 'Live JDBC Integration Test.*\[FAIL\]') "Persisted summary records JdbcIntegration as [FAIL] on failure marker"

    # Test 4.14: Guarded cleanup finally: Post-create failure + missing marker => DROP invocation count 0
    $dir414 = Join-Path $testOrchDir "run_414"
    $global:dropCount414 = 0
    $mock414 = [hashtable]$baseValidMock.Clone()
    $mock414['jdbc'] = { throw "Forced post-create JDBC failure" }
    $mock414['cleanup_marker'] = { "" } # Missing marker
    $mock414['drop_db'] = { $global:dropCount414++ }

    $res414 = & $runnerPath -EvidenceOutputDir $dir414 -MockInvocations $mock414 2>&1
    $exit414 = $LASTEXITCODE
    $summary414 = Get-Content -LiteralPath (Join-Path $dir414 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit414 -ne 0) "Post-create failure with missing marker returns nonzero ($exit414)"
    Assert-Condition ($global:dropCount414 -eq 0) "DROP invocation count is exactly 0 when marker is missing"
    Assert-Condition ($summary414 -match 'Disposable Database Cleanup.*\[FAIL\]') "Persisted summary records DatabaseCleanup as [FAIL] on missing marker"
    Assert-Condition ($summary414 -match 'Overall Status.*FAIL') "Overall status is FAIL"

    # Test 4.15: Guarded cleanup finally: Post-create failure + mismatched marker => DROP invocation count 0
    $dir415 = Join-Path $testOrchDir "run_415"
    $global:dropCount415 = 0
    $mock415 = [hashtable]$baseValidMock.Clone()
    $mock415['jdbc'] = { throw "Forced post-create JDBC failure" }
    $mock415['cleanup_marker'] = { "completely_wrong_marker_value" }
    $mock415['drop_db'] = { $global:dropCount415++ }

    $res415 = & $runnerPath -EvidenceOutputDir $dir415 -MockInvocations $mock415 2>&1
    $exit415 = $LASTEXITCODE
    $summary415 = Get-Content -LiteralPath (Join-Path $dir415 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit415 -ne 0) "Post-create failure with mismatched marker returns nonzero ($exit415)"
    Assert-Condition ($global:dropCount415 -eq 0) "DROP invocation count is exactly 0 when marker mismatches"
    Assert-Condition ($summary415 -match 'Disposable Database Cleanup.*\[FAIL\]') "Persisted summary records DatabaseCleanup as [FAIL] on mismatched marker"

    # Test 4.16: Guarded cleanup finally: Post-create failure + marker query exit failure => DROP invocation count 0
    $dir416 = Join-Path $testOrchDir "run_416"
    $global:dropCount416 = 0
    $mock416 = [hashtable]$baseValidMock.Clone()
    $mock416['jdbc'] = { throw "Forced post-create JDBC failure" }
    $mock416['cleanup_marker_exit'] = 1
    $mock416['drop_db'] = { $global:dropCount416++ }

    $res416 = & $runnerPath -EvidenceOutputDir $dir416 -MockInvocations $mock416 2>&1
    $exit416 = $LASTEXITCODE
    $summary416 = Get-Content -LiteralPath (Join-Path $dir416 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit416 -ne 0) "Post-create failure with marker query exit failure returns nonzero ($exit416)"
    Assert-Condition ($global:dropCount416 -eq 0) "DROP invocation count is exactly 0 when marker query fails"
    Assert-Condition ($summary416 -match 'Disposable Database Cleanup.*\[FAIL\]') "Persisted summary records DatabaseCleanup as [FAIL] on marker query exit failure"

    # Test 4.17: Guarded cleanup finally: Post-create failure + valid marker => guarded cleanup PASS + absence evidence while overall FAIL
    $dir417 = Join-Path $testOrchDir "run_417"
    $global:dropCount417 = 0
    $mock417 = [hashtable]$baseValidMock.Clone()
    $mock417['jdbc'] = { throw "Forced post-create JDBC failure" }
    $mock417['cleanup_marker'] = { param($m) $m } # Valid matching marker
    $mock417['drop_db'] = { $global:dropCount417++ }
    $mock417['absence_check'] = { "0" }

    $res417 = & $runnerPath -EvidenceOutputDir $dir417 -MockInvocations $mock417 2>&1
    $exit417 = $LASTEXITCODE
    $summary417 = Get-Content -LiteralPath (Join-Path $dir417 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit417 -ne 0) "Post-create failure returns nonzero runner exit ($exit417)"
    Assert-Condition ($global:dropCount417 -eq 1) "Guarded DROP invocation count is exactly 1 when marker is valid"
    Assert-Condition ($summary417 -match 'Live JDBC Integration Test.*\[FAIL\]') "Persisted summary records failed JDBC stage as [FAIL]"
    Assert-Condition ($summary417 -match 'Disposable Database Cleanup.*\[PASS\]') "Persisted summary records guarded DatabaseCleanup as [PASS]"
    Assert-Condition ($summary417 -match 'Overall Status.*FAIL') "Overall status remains truthful FAIL due to JDBC failure"
    Assert-Condition (Test-Path -LiteralPath (Join-Path $dir417 'database_absence_verification.log')) "Independent database absence log exists in evidence"

    # Test 4.18: Guarded cleanup finally: DROP failure -> DatabaseCleanup FAIL, exit nonzero
    $dir418 = Join-Path $testOrchDir "run_418"
    $mock418 = [hashtable]$baseValidMock.Clone()
    $mock418['drop_db'] = { throw "DROP DATABASE failed: exclusive lock timeout" }

    $res418 = & $runnerPath -EvidenceOutputDir $dir418 -MockInvocations $mock418 2>&1
    $exit418 = $LASTEXITCODE
    $summary418 = Get-Content -LiteralPath (Join-Path $dir418 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit418 -ne 0) "Failed DROP database causes nonzero runner exit ($exit418)"
    Assert-Condition ($summary418 -match 'Disposable Database Cleanup.*\[FAIL\]') "Persisted summary records DatabaseCleanup as [FAIL]"

    # Test 4.19: Guarded cleanup finally: Failed absence check (count > 0) -> DatabaseCleanup FAIL, exit nonzero
    $dir419 = Join-Path $testOrchDir "run_419"
    $mock419 = [hashtable]$baseValidMock.Clone()
    $mock419['absence_check'] = { "1" } # DB still exists!

    $res419 = & $runnerPath -EvidenceOutputDir $dir419 -MockInvocations $mock419 2>&1
    $exit419 = $LASTEXITCODE
    $summary419 = Get-Content -LiteralPath (Join-Path $dir419 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit419 -ne 0) "Failed absence check causes nonzero runner exit ($exit419)"
    Assert-Condition ($summary419 -match 'Disposable Database Cleanup.*\[FAIL\]') "Persisted summary records DatabaseCleanup as [FAIL] on positive count"

    # Test 4.20: -KeepDatabase => DatabaseCleanup SKIPPED, Overall Status FAIL, exit nonzero (verification contract)
    $dir420 = Join-Path $testOrchDir "run_420"
    $mock420 = [hashtable]$baseValidMock.Clone()

    $res420 = & $runnerPath -KeepDatabase -EvidenceOutputDir $dir420 -MockInvocations $mock420 2>&1
    $exit420 = $LASTEXITCODE
    $summary420 = Get-Content -LiteralPath (Join-Path $dir420 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit420 -ne 0) "-KeepDatabase causes nonzero runner exit per contract ($exit420)"
    Assert-Condition ($summary420 -match 'Disposable Database Cleanup.*\[SKIPPED\]') "Persisted summary records DatabaseCleanup as [SKIPPED]"
    Assert-Condition ($summary420 -match 'Intentionally retained via -KeepDatabase') "Persisted summary reports intentional retention"
    Assert-Condition ($summary420 -match 'Overall Status.*FAIL') "-KeepDatabase causes Overall Status FAIL per contract"

    # Test 4.21: Complete Mocked Pipeline PASS -> All stages [PASS], overall PASS, exit 0
    $dir421 = Join-Path $testOrchDir "run_421"
    $mock421 = [hashtable]$baseValidMock.Clone()

    $res421 = & $runnerPath -EvidenceOutputDir $dir421 -MockInvocations $mock421 2>&1
    $exit421 = $LASTEXITCODE
    $summary421 = Get-Content -LiteralPath (Join-Path $dir421 'commands_hash_count_summary.md') -Raw -Encoding UTF8

    Assert-Condition ($exit421 -eq 0) "Full passing pipeline returns exit code 0"
    Assert-Condition ($summary421 -match 'Overall Status.*PASS') "Persisted summary records Overall Status as PASS"

} finally {
    $env:TV2_QA_PASSWORD = $origTv2QaPwd
    Remove-Item -LiteralPath $testOrchDir -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "`n========================================================"
Write-Host "ALL $passedChecks / $totalChecks CONTROLLED NEGATIVE CHECKS PASSED!"
Write-Host "========================================================"
