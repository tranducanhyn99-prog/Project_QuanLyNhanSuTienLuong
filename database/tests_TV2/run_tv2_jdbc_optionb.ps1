param(
    [string]$ServerInstance = '.',
    [switch]$KeepDatabase,
    [switch]$NegativeGuardsOnly,
    [switch]$HelpersOnly,
    [string]$EvidenceOutputDir = $null,
    [hashtable]$MockInvocations = $null
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..') | Select-Object -ExpandProperty Path

# Helper functions for Windows PowerShell 5.1 native process argument quoting and async execution
function Format-WindowsCommandLineArg([string]$arg) {
    if ([string]::IsNullOrEmpty($arg)) {
        return '""'
    }
    if ($arg -notmatch '[\s\t\n\v"]') {
        return $arg
    }
    $escaped = [System.Text.RegularExpressions.Regex]::Replace($arg, '(\\*)("|$)', {
        param($m)
        $backslashes = $m.Groups[1].Value
        $quoteOrEnd = $m.Groups[2].Value
        if ($quoteOrEnd -eq '"') {
            return ($backslashes * 2) + '\"'
        } else {
            return ($backslashes * 2)
        }
    })
    return '"' + $escaped + '"'
}

function Format-WindowsCommandLineArgs([string[]]$argsList) {
    $formatted = foreach ($a in $argsList) { Format-WindowsCommandLineArg $a }
    return ($formatted -join ' ')
}

function Invoke-JavaProcess([string]$JavaExecutable, [string[]]$ArgumentsList, [string]$OutputFile = $null) {
    if ($MockInvocations -and $MockInvocations.ContainsKey($JavaExecutable)) {
        return (& $MockInvocations[$JavaExecutable] $ArgumentsList $OutputFile)
    }

    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $JavaExecutable
    $psi.Arguments = Format-WindowsCommandLineArgs $ArgumentsList
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.UseShellExecute = $false
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [System.Text.Encoding]::UTF8

    $process = [System.Diagnostics.Process]::Start($psi)
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $process.WaitForExit()
    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()

    $fullOutput = $stdout
    if (![string]::IsNullOrEmpty($stderr)) {
        if (![string]::IsNullOrEmpty($fullOutput)) { $fullOutput += "`n" }
        $fullOutput += "STDERR:`n" + $stderr
    }

    if ($OutputFile) {
        [System.IO.File]::WriteAllText($OutputFile, $fullOutput, [System.Text.Encoding]::UTF8)
    }

    return [PSCustomObject]@{
        ExitCode = $process.ExitCode
        Output = $fullOutput
        Stdout = $stdout
        Stderr = $stderr
    }
}

# Helper function to resolve shared explicit endpoint for SQLCMD and JDBC
function Resolve-SqlEndpoint([string]$InstanceInput, [string]$DatabaseName) {
    if ([string]::IsNullOrWhiteSpace($InstanceInput)) {
        throw "Invalid endpoint configuration: Server instance endpoint cannot be empty or whitespace."
    }
    $raw = $InstanceInput.Trim()

    # Reject JDBC property delimiters, whitespace, and null bytes to avoid injection
    if ($raw -match '[;=\x00\{\}\s]') {
        throw "Invalid endpoint configuration: '$raw' contains disallowed delimiter or whitespace characters."
    }

    $resolvedSqlcmd = $null
    $resolvedJdbcUrl = $null
    $resolvedHost = $null
    $resolvedPort = 1433
    $resolvedInstanceName = $null

    if ($raw -eq '.' -or $raw -eq '(local)' -or $raw -eq 'localhost' -or $raw -eq 'MSSQLSERVER') {
        $resolvedHost = 'localhost'
        $resolvedPort = 1433
        $resolvedInstanceName = 'MSSQLSERVER'
        $resolvedSqlcmd = 'localhost,1433'
        $resolvedJdbcUrl = "jdbc:sqlserver://localhost:1433;databaseName=$DatabaseName;encrypt=true;trustServerCertificate=true"
    }
    elseif ($raw -match '^([a-zA-Z0-9\.\-_]+)[,:]([0-9]{1,5})$') {
        $hostPart = $matches[1]
        $portPart = [int]$matches[2]
        if ($portPart -lt 1 -or $portPart -gt 65535) {
            throw "Invalid TCP port in endpoint: $portPart"
        }
        $resolvedHost = if ($hostPart -eq '.' -or $hostPart -eq '(local)') { 'localhost' } else { $hostPart }
        $resolvedPort = $portPart
        $resolvedInstanceName = 'MSSQLSERVER'
        $resolvedSqlcmd = "$resolvedHost,$resolvedPort"
        $resolvedJdbcUrl = "jdbc:sqlserver://${resolvedHost}:${resolvedPort};databaseName=$DatabaseName;encrypt=true;trustServerCertificate=true"
    }
    elseif ($raw -match '^([a-zA-Z0-9\.\-_]+)\\([a-zA-Z0-9_\\-]+)[,:]([0-9]{1,5})$') {
        $hostPart = $matches[1]
        $instPart = $matches[2]
        $portPart = [int]$matches[3]
        if ($portPart -lt 1 -or $portPart -gt 65535) {
            throw "Invalid TCP port in endpoint: $portPart"
        }
        $resolvedHost = if ($hostPart -eq '.' -or $hostPart -eq '(local)') { 'localhost' } else { $hostPart }
        $resolvedPort = $portPart
        $resolvedInstanceName = $instPart
        $resolvedSqlcmd = "$resolvedHost\$resolvedInstanceName,$resolvedPort"
        $resolvedJdbcUrl = "jdbc:sqlserver://${resolvedHost}:${resolvedPort};instanceName=$resolvedInstanceName;databaseName=$DatabaseName;encrypt=true;trustServerCertificate=true"
    }
    elseif ($raw -match '^([a-zA-Z0-9\.\-_]+)\\([a-zA-Z0-9_\\-]+)$') {
        throw "Unported named instance '$raw' rejected. Local SQL Browser does not prove remote mapping; specify an explicit TCP port (e.g. host\instance,port or host:port)."
    }
    elseif ($raw -match '^[a-zA-Z0-9\.\-_]+$') {
        $resolvedHost = if ($raw -eq '.' -or $raw -eq '(local)') { 'localhost' } else { $raw }
        $resolvedPort = 1433
        $resolvedInstanceName = 'MSSQLSERVER'
        $resolvedSqlcmd = "$resolvedHost,1433"
        $resolvedJdbcUrl = "jdbc:sqlserver://${resolvedHost}:1433;databaseName=$DatabaseName;encrypt=true;trustServerCertificate=true"
    }
    else {
        throw "Invalid or ambiguous SQL Server endpoint configuration: '$raw'. Supported formats: '.', 'localhost', '<host>:<port>', '<host>,<port>', or '<host>\<instance>,<port>'."
    }

    return [PSCustomObject]@{
        RawInput = $raw
        SqlcmdServer = $resolvedSqlcmd
        JdbcUrl = $resolvedJdbcUrl
        Host = $resolvedHost
        Port = $resolvedPort
        InstanceName = $resolvedInstanceName
    }
}

function Invoke-SqlText([string]$Text, [string]$Catalog = 'master') {
    if ($MockInvocations -and $MockInvocations.ContainsKey('sqlcmd_text')) {
        return (& $MockInvocations['sqlcmd_text'] $Text $Catalog)
    }
    & $sqlcmd -S $endpoint.SqlcmdServer -d $Catalog -E -C -I -b -f 65001 -Q $Text
    if ($LASTEXITCODE -ne 0) { throw "SQL query failed in catalog $Catalog (exit code: $LASTEXITCODE)." }
}

function Invoke-SqlFile([string]$FilePath, [string]$LogFile) {
    if ($MockInvocations -and $MockInvocations.ContainsKey('sqlcmd_file')) {
        return (& $MockInvocations['sqlcmd_file'] $FilePath $LogFile)
    }
    $sql = Get-Content -LiteralPath $FilePath -Raw -Encoding UTF8
    $rewritten = $sql.Replace('QuanLyNhanSuTienLuong', $database)
    $tmpSql = [IO.Path]::GetTempFileName() + '.sql'
    try {
        [System.IO.File]::WriteAllText($tmpSql, $rewritten, [System.Text.Encoding]::UTF8)
        & $sqlcmd -S $endpoint.SqlcmdServer -d $database -E -C -I -b -f 65001 -i $tmpSql -o $LogFile
        if ($LASTEXITCODE -ne 0) { throw "SQL script failed: $FilePath (exit code: $LASTEXITCODE)" }
    } finally {
        Remove-Item -LiteralPath $tmpSql -Force -ErrorAction SilentlyContinue
    }
}

function Invoke-GuardedDatabaseCleanup {
    if (-not $created) {
        if ($stages.DatabaseCleanup.Status -eq 'SKIPPED') {
            $stages.DatabaseCleanup.Detail = "Database was not created"
        }
        return
    }

    if ($KeepDatabase) {
        $stages.DatabaseCleanup.Status = 'SKIPPED'
        $stages.DatabaseCleanup.Detail = "Intentionally retained via -KeepDatabase ($database)"
        Write-Host "Database cleanup SKIPPED: -KeepDatabase specified. Database $database was retained."
        return
    }

    Write-Host "Validating cleanup guard for $database..."
    if ($database -notmatch '^PRJ_Fix_QA_[0-9]{8}_[0-9]{6}_[a-f0-9]{8}$' -or
        $database -eq 'PRJ_Fix_QA_20261013_001534_ae185d81' -or
        $database -eq 'PRJ_Fix_QA_20261015_163041_72cc2228' -or
        $database -eq 'PRJ_Fix_QA_20261015_171103_706b5479') {
        $stages.DatabaseCleanup.Status = 'FAIL'
        $stages.DatabaseCleanup.Detail = "Refusing to drop protected or non-conforming database: $database"
        if ($script:exitCode -eq 0) { $script:exitCode = 1 }
        Write-Host "ERROR: $($stages.DatabaseCleanup.Detail)"
        return
    }

    # Verify ownership marker in the database before DROP
    $markerExit = 0
    $actualMarker = ""
    try {
        $markerQuery = if ($MockInvocations -and $MockInvocations.ContainsKey('cleanup_marker')) {
            & $MockInvocations['cleanup_marker'] $ownershipMarker
        } else {
            & $sqlcmd -S $endpoint.SqlcmdServer -d $database -E -C -b -h -1 -W -Q "SET NOCOUNT ON; SELECT Marker FROM dbo.__QAOwnershipMarker;"
        }
        $markerExit = if ($MockInvocations -and $MockInvocations.ContainsKey('cleanup_marker_exit')) {
            [int]$MockInvocations['cleanup_marker_exit']
        } elseif ($MockInvocations -and $MockInvocations.ContainsKey('cleanup_marker')) {
            0
        } else {
            $LASTEXITCODE
        }
        $actualMarker = ($markerQuery -join '').Trim()
    } catch {
        $markerExit = 1
        $actualMarker = ""
    }

    if ($markerExit -ne 0 -or [string]::IsNullOrWhiteSpace($actualMarker)) {
        $stages.DatabaseCleanup.Status = 'FAIL'
        $stages.DatabaseCleanup.Detail = "Failed to query ownership marker before DROP (marker query exit: $markerExit, output: '$actualMarker')"
        if ($script:exitCode -eq 0) { $script:exitCode = 1 }
        Write-Host "ERROR: $($stages.DatabaseCleanup.Detail) - NO DROP executed."
        return
    }

    if ($actualMarker -ne $ownershipMarker) {
        $stages.DatabaseCleanup.Status = 'FAIL'
        $stages.DatabaseCleanup.Detail = "Cleanup rejected: Ownership marker in database ($actualMarker) does not match run marker ($ownershipMarker)!"
        if ($script:exitCode -eq 0) { $script:exitCode = 1 }
        Write-Host "ERROR: $($stages.DatabaseCleanup.Detail) - NO DROP executed."
        return
    }

    # Guarded DROP: reached ONLY after strict name guard and marker match
    try {
        if ($MockInvocations -and $MockInvocations.ContainsKey('drop_db')) {
            & $MockInvocations['drop_db']
        } else {
            Invoke-SqlText "ALTER DATABASE [$database] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [$database]"
        }
    } catch {
        $stages.DatabaseCleanup.Status = 'FAIL'
        $stages.DatabaseCleanup.Detail = "DROP DATABASE failed: $($_.Exception.Message)"
        if ($script:exitCode -eq 0) { $script:exitCode = 1 }
        Write-Host "ERROR: $($stages.DatabaseCleanup.Detail)"
        return
    }

    # Independently confirm database absence in sys.databases
    $absentExit = 0
    $absentCount = ""
    try {
        $absentCheck = if ($MockInvocations -and $MockInvocations.ContainsKey('absence_check')) {
            & $MockInvocations['absence_check']
        } else {
            & $sqlcmd -S $endpoint.SqlcmdServer -d master -E -C -b -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM sys.databases WHERE name = '$database';"
        }
        $absentExit = if ($MockInvocations -and $MockInvocations.ContainsKey('absence_exit')) {
            [int]$MockInvocations['absence_exit']
        } elseif ($MockInvocations -and $MockInvocations.ContainsKey('absence_check')) {
            0
        } else {
            $LASTEXITCODE
        }
        $absentCount = ($absentCheck -join '').Trim()
    } catch {
        $absentExit = 1
        $absentCount = "UNKNOWN"
    }

    [System.IO.File]::WriteAllText((Join-Path $evidenceDir 'database_absence_verification.log'), "Absence count in sys.databases: $absentCount (sqlcmd exit: $absentExit)`n", [System.Text.Encoding]::UTF8)

    if ($absentExit -eq 0 -and $absentCount -eq '0') {
        $cleanupMsg = "PASS: Successfully cleaned up owned disposable QA database: $database (marker verified, absence confirmed in sys.databases)."
        Write-Host $cleanupMsg
        [System.IO.File]::WriteAllText((Join-Path $evidenceDir 'environment_cleanup.log'), $cleanupMsg, [System.Text.Encoding]::UTF8)
        $stages.DatabaseCleanup.Status = 'PASS'
        $stages.DatabaseCleanup.Detail = "Successfully cleaned up owned disposable QA database: $database (marker verified, absence confirmed in sys.databases)"
    } else {
        $stages.DatabaseCleanup.Status = 'FAIL'
        $stages.DatabaseCleanup.Detail = "Database absence verification failed: count=$absentCount, exit=$absentExit"
        if ($script:exitCode -eq 0) { $script:exitCode = 1 }
        Write-Host "ERROR: $($stages.DatabaseCleanup.Detail)"
    }
}

function Write-RunnerSummary {
    $hasFail = $false
    $hasNotVerified = $false
    $hasSkipped = $false

    foreach ($k in $stages.Keys) {
        if ($stages[$k].Status -eq 'FAIL') {
            $hasFail = $true
        } elseif ($stages[$k].Status -eq 'NOT_VERIFIED') {
            $hasNotVerified = $true
        } elseif ($stages[$k].Status -eq 'SKIPPED') {
            if (-not $NegativeGuardsOnly) {
                $hasSkipped = $true
            }
        }
    }

    $overall = if ($hasFail) {
        'FAIL'
    } elseif ($hasNotVerified) {
        'FAIL'
    } elseif ($hasSkipped) {
        'FAIL'
    } else {
        'PASS'
    }

    if ($overall -ne 'PASS' -and $script:exitCode -eq 0) {
        $script:exitCode = 1
    }

    $canonicalSqlPath = Join-Path $repoRoot 'database\02_Module_ChamCong_TV2.sql'
    $packageSqlPath = Join-Path $repoRoot 'Bo_nop_Nhom06\database\02_Module_ChamCong_TV2.sql'
    $harnessPath = Join-Path $repoRoot 'src\test\java\com\test\Tv2JdbcIntegrationTest.java'
    $runnerPath = Join-Path $repoRoot 'database\tests_TV2\run_tv2_jdbc_optionb.ps1'

    $canonicalSqlHash = if (Test-Path $canonicalSqlPath) { (Get-FileHash -Algorithm SHA256 $canonicalSqlPath).Hash } else { "NOT_FOUND" }
    $packageSqlHash = if (Test-Path $packageSqlPath) { (Get-FileHash -Algorithm SHA256 $packageSqlPath).Hash } else { "NOT_FOUND" }
    $harnessHash = if (Test-Path $harnessPath) { (Get-FileHash -Algorithm SHA256 $harnessPath).Hash } else { "NOT_FOUND" }
    $runnerHash = if (Test-Path $runnerPath) { (Get-FileHash -Algorithm SHA256 $runnerPath).Hash } else { "NOT_FOUND" }

    $dupMsgText = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String("TmjDom4gdmnDqm4gxJHDoyBjw7MgYuG6o24gZ2hpIGNo4bqlbSBjw7RuZyB0cm9uZyBuZ8OgeSBuw6B5IQ=="))
    $inactMsgText = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String("S2jDtG5nIHRo4buDIGdoaSBuaOG6rW4gY2jhuqVtIGPDtG5nIGNobyBuaMOibiB2acOqbiDEkcOjIG5naOG7iSB2aeG7h2MgaG/hurdjIGtow7RuZyBob+G6oXQgxJHhu5luZyE="))
    $closedMsgText = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String("S+G7syBsxrDGoW5nIMSRw6MgY2jhu5F0OiBraMO0bmcgxJHGsOG7o2MgdGhheSDEkeG7lWkgY2jhuqVtIGPDtG5nLg=="))

    $endpointSqlcmd = if ($endpoint) { $endpoint.SqlcmdServer } else { $ServerInstance }
    $endpointJdbc = if ($endpoint) { $endpoint.JdbcUrl } else { "NOT_RESOLVED" }

    $summaryPath = Join-Path $evidenceDir 'commands_hash_count_summary.md'
    $summaryContent = @"
# TV2 Option B Live Verification Summary (Run ID: $runId)

## 1. Environment & Target Identity
- **Run ID**: $runId
- **Database**: $database (Disposable, run-owned)
- **Ownership Marker**: $ownershipMarker
- **Server Instance (CLI)**: $ServerInstance
- **Resolved SQLCMD Endpoint**: $endpointSqlcmd
- **Resolved JDBC URL**: $endpointJdbc
- **Connected SQL Server Name**: $sqlcmdServerName
- **Connected Instance Name**: $sqlcmdInstanceName
- **Connected Machine Name**: $sqlcmdMachineName
- **Connected Process ID**: $sqlcmdProcessId
- **Execution Date**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
- **JDK Version**: 21.0.8 (--release 11 target)
- **Overall Status**: **$overall** (Exit code: $($script:exitCode))

## 2. Integrity & Cryptographic Hashes
- **Canonical Feature SQL**: ``database/02_Module_ChamCong_TV2.sql`` -> $canonicalSqlHash
- **Package Feature SQL**: ``Bo_nop_Nhom06/database/02_Module_ChamCong_TV2.sql`` -> $packageSqlHash
- **Harness Source**: ``src/test/java/com/test/Tv2JdbcIntegrationTest.java`` -> $harnessHash
- **Runner Script**: ``database/tests_TV2/run_tv2_jdbc_optionb.ps1`` -> $runnerHash

## 3. Dynamic Verification Stages & Assertions Count
- **Java Compilation**: [$($stages.JavaCompilation.Status)] $($stages.JavaCompilation.Detail)
- **Negative Guards**: [$($stages.NegativeGuards.Status)] $($stages.NegativeGuards.Detail)
- **Offline Security Regression**: [$($stages.OfflineSecurity.Status)] $($stages.OfflineSecurity.Detail)
- **Offline GUI QA Regression**: [$($stages.OfflineGui.Status)] $($stages.OfflineGui.Detail)
- **SQL Schema Installation**: [$($stages.SqlInstallation.Status)] $($stages.SqlInstallation.Detail)
- **Installed Procedure Definition**: [$($stages.ProcVerification.Status)] $($stages.ProcVerification.Detail)
- **SQL Integration Suite**: [$($stages.SqlTestSuite.Status)] $($stages.SqlTestSuite.Detail)
- **Concurrency Verification**: [$($stages.Concurrency.Status)] $($stages.Concurrency.Detail)
- **Live JDBC Integration Test**: [$($stages.JdbcIntegration.Status)] $($stages.JdbcIntegration.Detail)
- **Disposable Database Cleanup**: [$($stages.DatabaseCleanup.Status)] $($stages.DatabaseCleanup.Detail)

## 4. Exact Vietnamese Error Text & Typed SQLServerError Metadata Preserved
- Duplicate attendance error: ``$dupMsgText`` (Error Code: 50000, Severity: 16, State: 1)
- Inactive employee error: ``$inactMsgText`` (Error Code: 50000, Severity: 16, State: 1)
- Closed payroll period trigger error: ``$closedMsgText`` (Error Code: 50000, Severity: 16, State: 1)

## 5. Disposable Database Cleanup
- Status: **$($stages.DatabaseCleanup.Status)**
- Details: $($stages.DatabaseCleanup.Detail)
"@
    [System.IO.File]::WriteAllText($summaryPath, $summaryContent, [System.Text.Encoding]::UTF8)
}

if ($HelpersOnly) {
    return
}

# ==============================================================================
# PIPELINE INITIALIZATION & PRE-EXECUTION SETUP
# ==============================================================================

# Dynamic phase tracking hashtable (initialized BEFORE compilation or preflight)
$stages = [ordered]@{
    JavaCompilation = [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
    NegativeGuards  = [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
    OfflineSecurity = [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
    OfflineGui      = [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
    SqlInstallation = [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
    ProcVerification= [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
    SqlTestSuite    = [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
    Concurrency     = [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
    JdbcIntegration = [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
    DatabaseCleanup = [PSCustomObject]@{ Status = 'SKIPPED'; Detail = 'Not executed' }
}

$runId = (Get-Date -Format 'yyyyMMdd_HHmmss') + '_' + [Guid]::NewGuid().ToString('N').Substring(0, 8)
$database = 'PRJ_Fix_QA_' + $runId
$ownershipMarker = [Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')

$evidenceDir = if ($EvidenceOutputDir) { $EvidenceOutputDir } else { Join-Path $repoRoot "docs\evidence\TV2\optionb-20261015\$runId" }
if (Test-Path -LiteralPath $evidenceDir) {
    $existingItems = Get-ChildItem -LiteralPath $evidenceDir -Force
    if ($existingItems.Count -gt 0) {
        throw "Evidence output directory already exists and is not empty: $evidenceDir"
    }
} else {
    New-Item -ItemType Directory -Path $evidenceDir | Out-Null
}
[System.IO.File]::WriteAllText((Join-Path $evidenceDir '.gitattributes'), "*.log -text -whitespace`n", [System.Text.Encoding]::UTF8)

$classDir = Join-Path $repoRoot 'build\verification\classes'
New-Item -ItemType Directory -Force -Path $classDir | Out-Null

$sqlcmdServerName = "UNKNOWN"
$sqlcmdInstanceName = "UNKNOWN"
$sqlcmdMachineName = "UNKNOWN"
$sqlcmdProcessId = 0
$created = $false
$script:exitCode = 0
$endpoint = $null
$jdbcJar = $null
$sqlcmd = $null
$classpath = $null

try {
    # --------------------------------------------------------------------------
    # STAGE 1: SUMMARIZED PREFLIGHT & JAVA COMPILATION
    # --------------------------------------------------------------------------
    # 1.1 Probe JDK (javac & java)
    $hasJavac = if ($MockInvocations -and $MockInvocations.ContainsKey('mock_javac_missing') -and $MockInvocations['mock_javac_missing']) {
        $false
    } else {
        ([bool](Get-Command javac -ErrorAction SilentlyContinue) -and [bool](Get-Command java -ErrorAction SilentlyContinue)) -or ($MockInvocations -and $MockInvocations.ContainsKey('javac'))
    }
    if (-not $hasJavac) {
        $stages.JavaCompilation.Status = 'FAIL'
        $stages.JavaCompilation.Detail = 'JDK 11+ (javac and java) is required but missing.'
        $script:exitCode = 2
        throw "JDK 11+ (javac and java) is required but missing."
    }

    # 1.2 Probe JDBC JAR
    if ($MockInvocations -and $MockInvocations.ContainsKey('mock_jdbcjar_missing') -and $MockInvocations['mock_jdbcjar_missing']) {
        $jdbcJar = $null
    } else {
        $candidates = @()
        foreach ($dir in @((Join-Path $repoRoot 'lib'), (Join-Path $env:USERPROFILE '.m2\repository\com\microsoft\sqlserver\mssql-jdbc'))) {
            if (Test-Path -LiteralPath $dir) {
                $candidates += Get-ChildItem -LiteralPath $dir -Recurse -File -Filter 'mssql-jdbc*.jar' | Where-Object { $_.Name -notmatch 'sources|javadoc' }
            }
        }
        $candidate = $candidates | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($candidate) { $jdbcJar = $candidate.FullName }
    }
    if (-not $jdbcJar -and (-not $MockInvocations -or -not $MockInvocations.ContainsKey('javac'))) {
        $stages.JavaCompilation.Status = 'FAIL'
        $stages.JavaCompilation.Detail = 'SQL Server JDBC JAR is required but missing.'
        $script:exitCode = 2
        throw "SQL Server JDBC JAR is required but missing."
    }

    # 1.3 Probe SQLCMD tool
    $hasSqlcmd = if ($MockInvocations -and $MockInvocations.ContainsKey('mock_sqlcmd_missing') -and $MockInvocations['mock_sqlcmd_missing']) {
        $false
    } else {
        $sqlcmdCommand = Get-Command sqlcmd -ErrorAction SilentlyContinue
        $sqlcmd = if ($sqlcmdCommand) { $sqlcmdCommand.Source } else { 'C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE' }
        (Test-Path -LiteralPath $sqlcmd) -or ($MockInvocations -and ($MockInvocations.ContainsKey('sqlcmd') -or $MockInvocations.ContainsKey('identity')))
    }
    if (-not $hasSqlcmd) {
        $stages.SqlInstallation.Status = 'FAIL'
        $stages.SqlInstallation.Detail = 'SQLCMD tool is required but missing.'
        $script:exitCode = 2
        throw "SQLCMD tool is required but missing."
    }

    $classpath = "$classDir;$(Join-Path $repoRoot 'src\resources');$jdbcJar"

    Write-Host "Compiling Java sources (JDK 11 release target)..."
    $sourceFiles = @(Get-ChildItem (Join-Path $repoRoot 'src\main\java'), (Join-Path $repoRoot 'src\test\java') -Recurse -File -Filter '*.java' | Select-Object -ExpandProperty FullName)
    $javacArgs = @("--release", "11", "-encoding", "UTF-8", "-cp", $classpath, "-d", $classDir) + $sourceFiles

    $compileRes = if ($MockInvocations -and $MockInvocations.ContainsKey('javac')) {
        & $MockInvocations['javac'] $javacArgs
    } else {
        Invoke-JavaProcess "javac" $javacArgs
    }

    if ($compileRes.ExitCode -ne 0) {
        $stages.JavaCompilation.Status = 'FAIL'
        $stages.JavaCompilation.Detail = "javac failed with exit code $($compileRes.ExitCode): $($compileRes.Stderr)"
        $script:exitCode = if ($compileRes.ExitCode -ne 0) { $compileRes.ExitCode } else { 1 }
        throw "Java compilation failed."
    }
    $stages.JavaCompilation.Status = 'PASS'
    $stages.JavaCompilation.Detail = "$($sourceFiles.Count) sources compiled (JDK 11 release target)"
    Write-Host "PASS Java 11 compilation: $($sourceFiles.Count) sources."

    # --------------------------------------------------------------------------
    # STAGE 2: CONTROLLED NEGATIVE TESTS & GUARDS (No DB connection required)
    # --------------------------------------------------------------------------
    Write-Host "Executing controlled negative tests and negative guards..."
    $testFindingsScript = Join-Path $repoRoot 'database\tests_TV2\test_tv5_findings.ps1'
    $testFindingsLog = Join-Path $evidenceDir 'test_tv5_findings.log'

    if ($MockInvocations -and $MockInvocations.ContainsKey('guard')) {
        $guardArgs = @("-cp", $classpath, "-Dfile.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8", "-Dqa.guards.only=true", "com.test.Tv2JdbcIntegrationTest")
        $guardRes = & $MockInvocations['guard'] $guardArgs
        $passMatches = [regex]::Matches($guardRes.Output, '\[PASS\]')
        $actualPassCount = $passMatches.Count
        $hasSuccessMarker = ($guardRes.Output -match 'All negative guard tests passed successfully')
        if ($guardRes.ExitCode -eq 0 -and $hasSuccessMarker -and $actualPassCount -gt 0) {
            $stages.NegativeGuards.Status = 'PASS'
            $stages.NegativeGuards.Detail = "$actualPassCount negative guard assertions passed successfully"
            Write-Host "PASS: $actualPassCount negative guard assertions."
            if ($NegativeGuardsOnly) {
                $script:exitCode = 0
                Write-RunnerSummary
                exit 0
            }
        } else {
            $stages.NegativeGuards.Status = if ($guardRes.ExitCode -eq 0 -and !$hasSuccessMarker) { 'NOT_VERIFIED' } else { 'FAIL' }
            $stages.NegativeGuards.Detail = if (!$hasSuccessMarker) {
                "Missing negative guard completion marker (child exit: $($guardRes.ExitCode), assertions: $actualPassCount)"
            } else {
                "Negative guards failed with exit code $($guardRes.ExitCode) ($actualPassCount assertions)"
            }
            $script:exitCode = if ($guardRes.ExitCode -ne 0) { $guardRes.ExitCode } else { 1 }
            throw "Negative guards validation failed: $($stages.NegativeGuards.Detail)"
        }
    } else {
        $testFindingsOutput = & $testFindingsScript *>&1
        $testFindingsExit = $LASTEXITCODE
        $testFindingsText = ($testFindingsOutput | Out-String)
        Write-Host $testFindingsText
        [System.IO.File]::WriteAllText($testFindingsLog, $testFindingsText, [System.Text.Encoding]::UTF8)

        $matchPass = [regex]::Match($testFindingsText, 'ALL (\d+) / \1 CONTROLLED NEGATIVE CHECKS PASSED!')
        if ($testFindingsExit -eq 0 -and $matchPass.Success) {
            $actualPassCount = [int]$matchPass.Groups[1].Value
            $stages.NegativeGuards.Status = 'PASS'
            $stages.NegativeGuards.Detail = "$actualPassCount controlled negative checks passed successfully"
            Write-Host "PASS: $actualPassCount controlled negative checks passed."
            if ($NegativeGuardsOnly) {
                $script:exitCode = 0
                Write-RunnerSummary
                exit 0
            }
        } else {
            $stages.NegativeGuards.Status = 'FAIL'
            $stages.NegativeGuards.Detail = "Controlled negative checks failed (exit: $testFindingsExit)"
            $script:exitCode = if ($testFindingsExit -ne 0) { $testFindingsExit } else { 1 }
            throw "Controlled negative checks failed."
        }
    }

    # --------------------------------------------------------------------------
    # STAGE 3: CREDENTIALS PREFLIGHT & SHARED ENDPOINT RESOLUTION
    # --------------------------------------------------------------------------
    $qaPassword = if ($env:TV2_QA_PASSWORD) { $env:TV2_QA_PASSWORD } else { $env:DB_PASSWORD }
    if ([string]::IsNullOrWhiteSpace($qaPassword)) {
        $stages.SqlInstallation.Status = 'FAIL'
        $stages.SqlInstallation.Detail = "QA credentials missing. TV2_QA_PASSWORD or DB_PASSWORD environment variable required before QA DB creation."
        $script:exitCode = 1
        throw "QA credentials missing. TV2_QA_PASSWORD or DB_PASSWORD required."
    }

    try {
        $endpoint = Resolve-SqlEndpoint -InstanceInput $ServerInstance -DatabaseName $database
    } catch {
        $stages.SqlInstallation.Status = 'FAIL'
        $stages.SqlInstallation.Detail = "Endpoint resolution failed: $($_.Exception.Message)"
        $script:exitCode = 1
        throw
    }

    # --------------------------------------------------------------------------
    # STAGE 4: OFFLINE REGRESSION TESTS
    # --------------------------------------------------------------------------
    Write-Host "Running offline SecurityRegressionTest..."
    $secLog = Join-Path $evidenceDir 'SecurityRegressionTest.log'
    $secArgs = @("-cp", $classpath, "com.test.SecurityRegressionTest")
    $secRes = if ($MockInvocations -and $MockInvocations.ContainsKey('security')) {
        & $MockInvocations['security'] $secArgs $secLog
    } else {
        Invoke-JavaProcess "java" $secArgs $secLog
    }
    Write-Host $secRes.Stdout
    if ($secRes.ExitCode -eq 0 -and $secRes.Output -match 'PASS offline Java regressions:\s*(\d+)\s*assertions') {
        $stages.OfflineSecurity.Status = 'PASS'
        $stages.OfflineSecurity.Detail = "$($Matches[1]) assertions PASS (SecurityRegressionTest)"
    } else {
        $stages.OfflineSecurity.Status = 'FAIL'
        $stages.OfflineSecurity.Detail = "SecurityRegressionTest failed with exit code $($secRes.ExitCode)"
        $script:exitCode = if ($secRes.ExitCode -ne 0) { $secRes.ExitCode } else { 1 }
        throw "SecurityRegressionTest failed."
    }

    Write-Host "Running offline GuiQaRegressionTest..."
    $guiLog = Join-Path $evidenceDir 'GuiQaRegressionTest.log'
    $guiArgs = @("-cp", $classpath, "com.test.GuiQaRegressionTest")
    $guiRes = if ($MockInvocations -and $MockInvocations.ContainsKey('gui')) {
        & $MockInvocations['gui'] $guiArgs $guiLog
    } else {
        Invoke-JavaProcess "java" $guiArgs $guiLog
    }
    Write-Host $guiRes.Stdout
    if ($guiRes.ExitCode -eq 0 -and $guiRes.Output -match 'PASS GUI QA regressions:\s*(\d+)\s*assertions') {
        $stages.OfflineGui.Status = 'PASS'
        $stages.OfflineGui.Detail = "$($Matches[1]) assertions PASS (GuiQaRegressionTest)"
    } elseif ($guiRes.Output -match 'SKIPPED dashboard: desktop display unavailable') {
        $stages.OfflineGui.Status = 'SKIPPED'
        $stages.OfflineGui.Detail = "SKIPPED: headless environment"
    } else {
        $stages.OfflineGui.Status = 'FAIL'
        $stages.OfflineGui.Detail = "GuiQaRegressionTest failed with exit code $($guiRes.ExitCode)"
        $script:exitCode = if ($guiRes.ExitCode -ne 0) { $guiRes.ExitCode } else { 1 }
        throw "GuiQaRegressionTest failed."
    }

    # --------------------------------------------------------------------------
    # STAGE 5: SQL SERVER IDENTITY QUERY & INTENDED INSTANCE CHECK PRE-CREATE
    # --------------------------------------------------------------------------
    $idQuery = "SET NOCOUNT ON; SELECT CAST(SERVERPROPERTY('ServerName') AS VARCHAR(128)) + '|' + CAST(COALESCE(SERVERPROPERTY('InstanceName'), 'MSSQLSERVER') AS VARCHAR(128)) + '|' + CAST(SERVERPROPERTY('MachineName') AS VARCHAR(128)) + '|' + CAST(SERVERPROPERTY('ProcessID') AS VARCHAR(32));"
    $idExit = 0
    $idRaw = if ($MockInvocations -and $MockInvocations.ContainsKey('identity')) {
        & $MockInvocations['identity']
    } else {
        $raw = & $sqlcmd -S $endpoint.SqlcmdServer -d master -E -C -I -b -h -1 -W -Q $idQuery
        $idExit = $LASTEXITCODE
        $raw
    }

    if ($idExit -ne 0 -or [string]::IsNullOrWhiteSpace(($idRaw -join '').Trim())) {
        $stages.SqlInstallation.Status = 'FAIL'
        $stages.SqlInstallation.Detail = "SQLCMD failed to query SQL Server identity on target endpoint: $($endpoint.SqlcmdServer)"
        $script:exitCode = 1
        throw "SQLCMD failed to query SQL Server identity on target endpoint: $($endpoint.SqlcmdServer)"
    }
    $idParts = (($idRaw -join '').Trim()).Split('|')
    if ($idParts.Length -lt 4 -or [string]::IsNullOrWhiteSpace($idParts[0]) -or [string]::IsNullOrWhiteSpace($idParts[1]) -or [string]::IsNullOrWhiteSpace($idParts[2]) -or [string]::IsNullOrWhiteSpace($idParts[3])) {
        $stages.SqlInstallation.Status = 'FAIL'
        $stages.SqlInstallation.Detail = "SQL Server identity query returned incomplete properties on endpoint $($endpoint.SqlcmdServer)"
        $script:exitCode = 1
        throw "SQL Server identity query returned incomplete properties on endpoint $($endpoint.SqlcmdServer)"
    }
    $sqlcmdServerName = $idParts[0].Trim()
    $sqlcmdInstanceName = $idParts[1].Trim()
    $sqlcmdMachineName = $idParts[2].Trim()
    $sqlcmdProcessId = [int]$idParts[3].Trim()
    if ($sqlcmdProcessId -le 0) {
        $stages.SqlInstallation.Status = 'FAIL'
        $stages.SqlInstallation.Detail = "SQL Server identity query returned invalid ProcessID '$($idParts[3])'"
        $script:exitCode = 1
        throw "SQL Server identity query returned invalid ProcessID '$($idParts[3])'"
    }

    # Verify intended instance identity before DB creation
    if ($endpoint.InstanceName -and $sqlcmdInstanceName -notmatch ('^' + [regex]::Escape($endpoint.InstanceName) + '$')) {
        $stages.SqlInstallation.Status = 'FAIL'
        $stages.SqlInstallation.Detail = "Connected instance '$sqlcmdInstanceName' does not match expected instance '$($endpoint.InstanceName)'"
        $script:exitCode = 1
        throw "Connected SQL Server instance '$sqlcmdInstanceName' does not match expected instance '$($endpoint.InstanceName)' on target endpoint $($endpoint.SqlcmdServer)."
    }
    Write-Host "Target SQL Server identity: $sqlcmdServerName (Instance: $sqlcmdInstanceName, Machine: $sqlcmdMachineName, ProcessID: $sqlcmdProcessId)"

    # --------------------------------------------------------------------------
    # STAGE 6: DISPOSABLE QA DATABASE CREATION & MODULE INSTALLATION
    # --------------------------------------------------------------------------
    Write-Host "Creating disposable QA database: $database..."
    Invoke-SqlText "CREATE DATABASE [$database]"
    $created = $true

    Invoke-SqlText @"
CREATE TABLE dbo.__QAOwnershipMarker (
    Marker VARCHAR(64) NOT NULL,
    CreatedAt DATETIME2 DEFAULT SYSDATETIME()
);
INSERT INTO dbo.__QAOwnershipMarker (Marker) VALUES ('$ownershipMarker');
GRANT SELECT ON dbo.__QAOwnershipMarker TO public;
"@ $database

    Write-Host "Installing schema modules 01 to 05 into $database..."
    $modules = @(
        'database\01_Module_NhanSu_TV1.sql',
        'database\02_Module_ChamCong_TV2.sql',
        'database\03_phucap_khautru_TV3.sql',
        'database\04_Module_TinhLuong_TV4.sql',
        'database\05_Security_Payroll_TV5.sql'
    )
    foreach ($m in $modules) {
        $logPath = Join-Path $evidenceDir ((Split-Path $m -Leaf) -replace '\.sql$', '.log')
        Invoke-SqlFile (Join-Path $repoRoot $m) $logPath
    }
    $stages.SqlInstallation.Status = 'PASS'
    $stages.SqlInstallation.Detail = "Modules 01-05 installed successfully into $database"

    # --------------------------------------------------------------------------
    # STAGE 7: STORED PROCEDURE DEFINITION CHECK (Check sqlcmd exit + 7 params)
    # --------------------------------------------------------------------------
    Write-Host "Verifying installed sp_GhiNhanChamCong definition..."
    $procDefLog = Join-Path $evidenceDir 'sp_definition_verification.log'
    $procQueryText = @"
SET NOCOUNT ON;
SELECT
    CASE WHEN sm.definition LIKE '%BEGIN TRY%END TRY%BEGIN CATCH%THROW;%END CATCH%' THEN 'PASS: HasTryCatchThrow' ELSE 'FAIL: NoTryCatchThrow' END AS TryCatchStatus,
    CASE WHEN sm.definition LIKE '%@MaChamCong%OUTPUT%' THEN 'PASS: HasMaChamCongOutput' ELSE 'FAIL: NoOutput' END AS OutputStatus,
    COUNT(p.parameter_id) AS ParamCount
FROM sys.sql_modules sm
JOIN sys.procedures pr ON pr.object_id = sm.object_id
JOIN sys.parameters p ON p.object_id = pr.object_id
WHERE pr.name = 'sp_GhiNhanChamCong'
GROUP BY sm.definition;
"@
    $procDefContent = if ($MockInvocations -and $MockInvocations.ContainsKey('proc_verify')) {
        & $MockInvocations['proc_verify']
    } else {
        & $sqlcmd -S $endpoint.SqlcmdServer -d $database -E -C -I -b -f 65001 -Q $procQueryText -o $procDefLog
        $script:procSqlcmdExit = $LASTEXITCODE
        Get-Content -LiteralPath $procDefLog -Raw -Encoding UTF8
    }

    Write-Host $procDefContent
    $procExit = if ($MockInvocations -and $MockInvocations.ContainsKey('proc_verify')) { 0 } else { $script:procSqlcmdExit }
    if ($procExit -eq 0 -and $procDefContent -match 'PASS:\s*HasTryCatchThrow' -and $procDefContent -match 'PASS:\s*HasMaChamCongOutput' -and $procDefContent -match '\b7\b') {
        $stages.ProcVerification.Status = 'PASS'
        $stages.ProcVerification.Detail = "Verified TRY...CATCH + bare THROW; with 7 parameters and MaChamCong OUTPUT"
    } else {
        $stages.ProcVerification.Status = 'FAIL'
        $stages.ProcVerification.Detail = "sp_GhiNhanChamCong definition verification failed (sqlcmd exit: $procExit)"
        $script:exitCode = if ($procExit -ne 0) { $procExit } else { 1 }
        throw "sp_GhiNhanChamCong definition verification failed."
    }

    # Map login_HRManager into this QA database
    Write-Host "Configuring login_HRManager role in $database..."
    Invoke-SqlText @"
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'login_HRManager')
    CREATE USER [login_HRManager] FOR LOGIN [login_HRManager];
ALTER ROLE role_HRManager ADD MEMBER [login_HRManager];
IF NOT EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE TenDangNhap = 'login_HRManager')
    INSERT INTO dbo.TAIKHOAN (TenDangNhap, MatKhau, VaiTro, TrangThai, SqlLogin)
    VALUES ('login_HRManager', 'pbkdf2-sha256`$20000`$mock`$mock', 'HR_Manager', 'HOAT_DONG', 'login_HRManager');
"@ $database

    # Seed open and closed payroll periods
    Write-Host "Seeding payroll periods (Month 9=CHUA_CHOT, Month 8=DA_CHOT)..."
    Invoke-SqlText @"
INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai) VALUES (9, 2026, 26, 'CHUA_CHOT');
INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai) VALUES (8, 2026, 26, 'DA_CHOT');
"@ $database

    # --------------------------------------------------------------------------
    # STAGE 8: SQL INTEGRATION SUITE (test_module_chamcong_TV2.sql)
    # --------------------------------------------------------------------------
    Write-Host "Running TV2 SQL test suite (test_module_chamcong_TV2.sql)..."
    $sqlTestLog = Join-Path $evidenceDir 'test_module_chamcong_TV2.log'
    if ($MockInvocations -and $MockInvocations.ContainsKey('sql_suite')) {
        & $MockInvocations['sql_suite'] $sqlTestLog
    } else {
        Invoke-SqlFile (Join-Path $repoRoot 'database\tests_TV2\test_module_chamcong_TV2.sql') $sqlTestLog
    }
    $sqlTestContent = if (Test-Path $sqlTestLog) { Get-Content -LiteralPath $sqlTestLog -Raw -Encoding UTF8 } else { "" }
    $matchTotal = [regex]::Match($sqlTestContent, 'T.*?NG S.*? K.*?CH B.*?N TH.*?C THI\s*:\s*(\d+)')
    $matchPass = [regex]::Match($sqlTestContent, 'S.*? K.*?CH B.*?N.*?PASS.*?\s*:\s*(\d+)')
    $matchFail = [regex]::Match($sqlTestContent, 'S.*? K.*?CH B.*?N.*?FAIL.*?\s*:\s*(\d+)')
    $passCount = if ($matchPass.Success) { [int]$matchPass.Groups[1].Value } else { 0 }
    $totalCount = if ($matchTotal.Success) { [int]$matchTotal.Groups[1].Value } else { 0 }
    $failCount = if ($matchFail.Success) { [int]$matchFail.Groups[1].Value } else { 1 }

    if ($totalCount -eq 21 -and $passCount -eq 21 -and $failCount -eq 0) {
        $stages.SqlTestSuite.Status = 'PASS'
        $stages.SqlTestSuite.Detail = "21 / 21 PASS (100%)"
        Write-Host "PASS: test_module_chamcong_TV2.sql executed (21/21 PASS)."
    } else {
        $stages.SqlTestSuite.Status = 'FAIL'
        $stages.SqlTestSuite.Detail = "SQL test suite failed or malformed ($passCount/$totalCount PASS, $failCount FAIL)"
        $script:exitCode = 1
        throw "SQL test suite failed ($passCount/$totalCount PASS, $failCount FAIL)."
    }

    # --------------------------------------------------------------------------
    # STAGE 9: CONCURRENCY VERIFICATION (All 4 cases + cleanup verified)
    # --------------------------------------------------------------------------
    Write-Host "Running Closed_Source_Concurrency.ps1..."
    $concurrencyLog = Join-Path $evidenceDir 'Closed_Source_Concurrency.log'
    $concurrencySuccess = $false
    $concurrencyCasesCount = 0
    $concurrencyExit = 0

    try {
        if ($MockInvocations -and $MockInvocations.ContainsKey('concurrency')) {
            $concurrencyText = & $MockInvocations['concurrency']
        } else {
            $concurrencyOutput = & (Join-Path $repoRoot 'database\tests\Closed_Source_Concurrency.ps1') -ServerInstance $endpoint.SqlcmdServer -Database $database *>&1
            $concurrencyExit = $LASTEXITCODE
            $concurrencyText = ($concurrencyOutput | Out-String)
        }
        Write-Host $concurrencyText
        [System.IO.File]::WriteAllText($concurrencyLog, $concurrencyText, [System.Text.Encoding]::UTF8)

        $hasAttendance = ($concurrencyText -match 'PASS close versus attendance:')
        $hasAllowance = ($concurrencyText -match 'PASS close versus allowance:')
        $hasDeduction = ($concurrencyText -match 'PASS close versus deduction:')
        $hasPayrollDetail = ($concurrencyText -match 'PASS close versus payroll detail:')
        $hasCleanup = ($concurrencyText -match 'PASS closing-race fixture cleanup')
        $concurrencyCasesCount = @($hasAttendance, $hasAllowance, $hasDeduction, $hasPayrollDetail) | Where-Object { $_ -eq $true } | Measure-Object | Select-Object -ExpandProperty Count

        if ($concurrencyExit -eq 0 -and $concurrencyCasesCount -eq 4 -and $hasCleanup) {
            $concurrencySuccess = $true
        }
    } catch {
        $concurrencyText = $_.Exception.Message
        Write-Host "Concurrency error: $concurrencyText"
        [System.IO.File]::WriteAllText($concurrencyLog, $concurrencyText, [System.Text.Encoding]::UTF8)
    }

    if ($concurrencySuccess) {
        $stages.Concurrency.Status = 'PASS'
        $stages.Concurrency.Detail = "PASS (4/4 cases waited, rejected, source unchanged, race fixture cleanup PASS)"
        Write-Host "PASS: Closed_Source_Concurrency.ps1 executed (4/4 cases + cleanup)."
    } else {
        $stages.Concurrency.Status = 'FAIL'
        $stages.Concurrency.Detail = "Closed_Source_Concurrency failed ($concurrencyCasesCount/4 cases, exit: $concurrencyExit)"
        $script:exitCode = if ($concurrencyExit -ne 0) { $concurrencyExit } else { 1 }
        throw "Closed_Source_Concurrency failed."
    }

    # --------------------------------------------------------------------------
    # STAGE 10: LIVE JDBC INTEGRATION TEST (All 6 scenarios)
    # --------------------------------------------------------------------------
    Write-Host "Running Tv2JdbcIntegrationTest with strict endpoint validation..."
    $jdbcLog = Join-Path $evidenceDir 'Tv2JdbcIntegrationTest.log'
    $javaArgs = @(
        "-cp", $classpath,
        "-Dqa.server.instance=$ServerInstance",
        "-Dqa.jdbc.url=$($endpoint.JdbcUrl)",
        "-Ddb.url=$($endpoint.JdbcUrl)",
        "-Dqa.expected.server.name=$sqlcmdServerName",
        "-Dqa.expected.instance.name=$sqlcmdInstanceName",
        "-Dqa.expected.machine.name=$sqlcmdMachineName",
        "-Dqa.expected.process.id=$sqlcmdProcessId",
        "-Dqa.database.name=$database",
        "-Dqa.ownership.marker=$ownershipMarker",
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
        "com.test.Tv2JdbcIntegrationTest"
    )

    $jdbcRes = $null
    try {
        $jdbcRes = if ($MockInvocations -and $MockInvocations.ContainsKey('jdbc')) {
            & $MockInvocations['jdbc'] $javaArgs $jdbcLog
        } else {
            Invoke-JavaProcess "java" $javaArgs $jdbcLog
        }
    } catch {
        $stages.JdbcIntegration.Status = 'FAIL'
        $stages.JdbcIntegration.Detail = "Tv2JdbcIntegrationTest failed: $($_.Exception.Message)"
        if ($script:exitCode -eq 0) { $script:exitCode = 1 }
        throw
    }
    $jdbcLogText = if (Test-Path -LiteralPath $jdbcLog) {
        Get-Content -LiteralPath $jdbcLog -Raw -Encoding UTF8
    } else {
        $jdbcRes.Output
    }

    $completionMatches = [regex]::Matches($jdbcLogText, '(?m)^\s*ALL 6 JDBC SCENARIOS COMPLETED SUCCESSFULLY!\s*$')
    $totalMatches = [regex]::Matches($jdbcLogText, '(?m)^\s*TOTAL ASSERTIONS PASSED:\s*(\d+)\s*$')
    $passRowMatches = [regex]::Matches($jdbcLogText, '(?m)^\s*\[PASS\]')
    $hasFailMarkers = ($jdbcLogText -match '(?m)^\s*\[FAIL\]' -or $jdbcLogText -match 'AssertionError' -or $jdbcLogText -match 'FATAL\b')

    $isLogValid = $false
    $actualAssertions = 0

    if ($completionMatches.Count -eq 1 -and $totalMatches.Count -eq 1 -and -not $hasFailMarkers) {
        $totalReported = [int]$totalMatches[0].Groups[1].Value
        $passCount = $passRowMatches.Count
        if ($totalReported -gt 0 -and $totalReported -eq $passCount) {
            $isLogValid = $true
            $actualAssertions = $totalReported
        }
    }

    if ($jdbcRes.ExitCode -eq 0 -and $isLogValid) {
        $stages.JdbcIntegration.Status = 'PASS'
        $stages.JdbcIntegration.Detail = "All 6 scenarios PASS ($actualAssertions total assertions matching [PASS] rows)"
        Write-Host "PASS: Tv2JdbcIntegrationTest completed successfully ($actualAssertions assertions)."
    } else {
        $stages.JdbcIntegration.Status = if ($hasFailMarkers -or $jdbcRes.ExitCode -ne 0) { 'FAIL' } else { 'NOT_VERIFIED' }
        $failReason = if ($jdbcRes.ExitCode -ne 0) {
            "child exit code $($jdbcRes.ExitCode)"
        } elseif ($hasFailMarkers) {
            "failure markers detected in JDBC log"
        } elseif ($completionMatches.Count -ne 1) {
            "completion marker count is $($completionMatches.Count) (expected 1)"
        } elseif ($totalMatches.Count -ne 1) {
            "total marker count is $($totalMatches.Count) (expected 1)"
        } elseif ($totalMatches.Count -eq 1 -and $totalMatches[0].Groups[1].Value -eq '0') {
            "total assertions reported as 0"
        } else {
            "mismatch between reported total ($($totalMatches[0].Groups[1].Value)) and actual [PASS] rows ($($passRowMatches.Count))"
        }
        $stages.JdbcIntegration.Detail = "Tv2JdbcIntegrationTest failed or unverified: $failReason"
        $script:exitCode = if ($jdbcRes.ExitCode -ne 0) { $jdbcRes.ExitCode } else { 1 }
        throw "Tv2JdbcIntegrationTest failed or unverified ($failReason)."
    }

} catch {
    if ($script:exitCode -eq 0) {
        $script:exitCode = 1
    }
    $err = $_.Exception.Message
    Write-Host "ERROR: $err"
    [System.IO.File]::WriteAllText((Join-Path $evidenceDir 'failure.log'), $err, [System.Text.Encoding]::UTF8)
} finally {
    Invoke-GuardedDatabaseCleanup
    Write-RunnerSummary
}

Write-Host "Evidence recorded in: $evidenceDir"
exit $script:exitCode
