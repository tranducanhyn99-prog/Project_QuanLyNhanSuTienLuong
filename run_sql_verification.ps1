param(
    [string]$ServerInstance = '.',
    [switch]$DemoData,
    [switch]$Benchmarks,
    [switch]$KeepDatabase
)
# SQL writes are confined to a database this invocation creates and owns.
$ErrorActionPreference = 'Stop'
$sqlcmdCommand = Get-Command sqlcmd -ErrorAction SilentlyContinue
$sqlcmd = if ($sqlcmdCommand) { $sqlcmdCommand.Source } else { 'C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\180\Tools\Binn\SQLCMD.EXE' }
if (!(Test-Path -LiteralPath $sqlcmd)) { Write-Host 'SKIPPED: SQLCMD is required.'; exit 2 }
$runId = (Get-Date -Format 'yyyyMMdd_HHmmss') + '_' + [Guid]::NewGuid().ToString('N').Substring(0,8)
$database = 'PRJ_Fix_QA_' + $runId
$output = Join-Path $PSScriptRoot ("build\sql-verification\$runId")
New-Item -ItemType Directory -Force -Path $output | Out-Null
$created = $false
$exitCode = 0
function Invoke-CommandText([string]$Text,[string]$Label,[string]$Catalog='master') {
    & $sqlcmd -S $ServerInstance -d $Catalog -E -C -I -b -f 65001 -Q $Text -o (Join-Path $output ($Label + '.log'))
    if ($LASTEXITCODE -ne 0) { throw "SQL command failed: $Label" }
}
function Invoke-SqlFile([string]$RelativePath,[string]$Label) {
    $copy = Join-Path $output ($Label + '.sql')
    $log = Join-Path $output ($Label + '.log')
    $sql = Get-Content -LiteralPath (Join-Path $PSScriptRoot $RelativePath) -Raw -Encoding UTF8
    # Rewrite both USE and module 01's conditional CREATE DATABASE to this owned name.
    $sql.Replace('QuanLyNhanSuTienLuong',$database) | Set-Content -LiteralPath $copy -Encoding UTF8
    & $sqlcmd -S $ServerInstance -d $database -E -C -I -b -f 65001 -i $copy -o $log
    if ($LASTEXITCODE -ne 0) { Get-Content -LiteralPath $log; throw "SQL FAIL: $RelativePath" }
    Write-Host "PASS executed: $RelativePath ($Label). Log: $log"
}
try {
    Invoke-CommandText "CREATE DATABASE [$database]" 'create_database'
    $created=$true
    Write-Host "QA target: $ServerInstance / $database (Windows DBA; local test certificate trusted)."
    $modules=@('01_Module_NhanSu_TV1.sql','02_Module_ChamCong_TV2.sql','03_phucap_khautru_TV3.sql','04_Module_TinhLuong_TV4.sql','05_Security_Payroll_TV5.sql')
    foreach ($phase in @('install','rerun')) {
        foreach ($module in $modules) { Invoke-SqlFile "database\$module" ($phase + '_' + [IO.Path]::GetFileNameWithoutExtension($module)) }
        if ($DemoData -and $phase -eq 'install') {
            Invoke-SqlFile 'database\06_Demo_Data.sql' 'demo_first'
            Invoke-CommandText @'
SELECT nv.MaNV,pc.MaPCNV,pc.SoTien INTO dbo.__QASeedSnapshot
FROM dbo.NHANVIEN nv JOIN dbo.PHUCAPNHANVIEN pc ON pc.MaNV=nv.MaNV
WHERE nv.Email='employee01@example.invalid' AND pc.GhiChu=N'DEMO_SEED_V1';
IF (SELECT COUNT(*) FROM dbo.__QASeedSnapshot)<>1 THROW 53300,N'Demo snapshot must contain one row.',1;
'@ 'demo_snapshot' $database
        }
    }
    if ($DemoData) {
        Invoke-CommandText @'
IF (SELECT COUNT(*) FROM dbo.__QASeedSnapshot s JOIN dbo.NHANVIEN nv ON nv.MaNV=s.MaNV
    JOIN dbo.PHUCAPNHANVIEN pc ON pc.MaPCNV=s.MaPCNV AND pc.MaNV=s.MaNV AND pc.SoTien=s.SoTien)<>1
    THROW 53303,N'Schema rerun deleted/changed preexisting demo rows.',1;
DROP TABLE dbo.__QASeedSnapshot;
PRINT 'PASS schema rerun preserved preexisting employee/allowance identities and amount';
'@ 'schema_preserves_data' $database
        Invoke-SqlFile 'database\06_Demo_Data.sql' 'demo_second'
        $seedCheck=Join-Path $output 'demo_assert.sql'
        @'
IF (SELECT COUNT(*) FROM dbo.NHANVIEN WHERE Email='employee01@example.invalid')<>1
    THROW 53301,N'Demo employee seed duplicated/missing.',1;
IF (SELECT COUNT(*) FROM dbo.PHUCAPNHANVIEN WHERE GhiChu=N'DEMO_SEED_V1')<>1
    THROW 53302,N'Demo allowance seed duplicated/missing.',1;
PRINT 'PASS demo rerun: one employee and one allowance';
'@ | Set-Content -LiteralPath $seedCheck -Encoding UTF8
        & $sqlcmd -S $ServerInstance -d $database -E -C -I -b -f 65001 -i $seedCheck -o (Join-Path $output 'demo_assert.log')
        if($LASTEXITCODE -ne 0) { throw 'Demo seed rerun assertion failed.' }
    }
    Invoke-CommandText @'
IF (SELECT COUNT(*) FROM sys.objects WHERE schema_id=SCHEMA_ID('dbo') AND type IN ('FN','IF','TF')
    AND name IN ('fn_TinhSoNgayCong','fn_TongKhauTru','fn_TongPhuCap','fn_TinhTienCong','fn_TinhThucNhan'))<>5
    THROW 53304,N'The five required business functions were not installed.',1;
SELECT DB_NAME() AS DatabaseName,ORIGINAL_LOGIN() AS SqlIdentity,@@VERSION AS SqlVersion;
PRINT 'PASS five business functions installed';
'@ 'schema_environment' $database
    foreach ($test in @(
        'database\test_module_nhansu_TV1.sql',
        'database\tests_TV2\test_module_chamcong_TV2.sql',
        'database\tests_TV3\test_module_phucap_khautru_TV3.sql',
        'database\tests\Fix_Regression.sql',
        'database\test_security_roles_TV5.sql',
        'database\tests\TV4_Payroll_E2E.sql'
    )) { Invoke-SqlFile $test ([IO.Path]::GetFileNameWithoutExtension($test)) }
    & (Join-Path $PSScriptRoot 'database\tests\Closed_Source_Concurrency.ps1') -ServerInstance $ServerInstance -Database $database *>&1 |
        Tee-Object -FilePath (Join-Path $output 'Closed_Source_Concurrency.log')
    Write-Host 'PASS close versus attendance/allowance/deduction/payroll-detail races.'
    if ($Benchmarks) {
        foreach ($test in @('database\test_benchmark_index_TV1.sql','database\tests_TV3\test_benchmark_index_TV3.sql','database\tests\TV4_Payroll_Benchmark.sql')) {
            Invoke-SqlFile $test ('benchmark_' + [IO.Path]::GetFileNameWithoutExtension($test))
        }
    }
    Write-Host 'PASS requested SQL suites and closing/source races. Two-session payroll calculation and real SQL identities run separately.'
} catch {
    $exitCode=1
    $message='FAIL: ' + $_.Exception.Message
    Write-Host $message
    $message | Set-Content -LiteralPath (Join-Path $output 'failure.log') -Encoding UTF8
} finally {
    if ($created -and !$KeepDatabase) {
        try {
            # The generated, validated identifier belongs only to this invocation.
            if ($database -notmatch '^PRJ_Fix_QA_[0-9]{8}_[0-9]{6}_[a-f0-9]{8}$') { throw 'Unexpected cleanup target.' }
            Invoke-CommandText "ALTER DATABASE [$database] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [$database]" 'drop_database'
            'PASS owned QA database cleanup' | Set-Content -LiteralPath (Join-Path $output 'cleanup.log') -Encoding UTF8
            Write-Host "Cleaned owned QA database: $database"
        } catch { $exitCode=1; Write-Host "FAIL cleanup: $($_.Exception.Message)" }
    } elseif ($created) { Write-Host "Kept QA database by explicit -KeepDatabase: $database" }
    Write-Host "Logs: $output"
}
exit $exitCode
