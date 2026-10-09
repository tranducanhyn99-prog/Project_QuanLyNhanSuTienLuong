param(
    [switch]$LiveSql,
    [ValidateSet('SecurityRegressionTest','LoginChipRegressionTest','LightThemeRegressionTest','TestRealAuth','FullSystemIntegrationTest','NhanSuModuleTest','TestDatabaseConnection')]
    [string]$TestClass = 'SecurityRegressionTest'
)
$ErrorActionPreference = 'Stop'
$repoRoot = $PSScriptRoot
if (!(Get-Command javac -ErrorAction SilentlyContinue) -or !(Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host 'SKIPPED: JDK 11+ is required.'; exit 2
}
$classDir = Join-Path $repoRoot 'build\verification\classes'
$logDir = Join-Path $repoRoot 'build\test-results'
New-Item -ItemType Directory -Force -Path $classDir,$logDir | Out-Null
# Clear only this runner's output; deleted Java classes must not survive compilation.
$resolvedOutput = [IO.Path]::GetFullPath($classDir)
if ($resolvedOutput -ne [IO.Path]::GetFullPath((Join-Path $repoRoot 'build\verification\classes'))) { throw 'Unexpected output path.' }
Get-ChildItem -LiteralPath $classDir -Recurse -File | Remove-Item -Force
$jdbcJar = $null
if ($env:MSSQL_JDBC_JAR -and (Test-Path -LiteralPath $env:MSSQL_JDBC_JAR)) {
    $jdbcJar = (Resolve-Path -LiteralPath $env:MSSQL_JDBC_JAR).Path
} else {
    $candidates = @()
    foreach ($directory in @((Join-Path $repoRoot 'lib'),(Join-Path $env:USERPROFILE '.m2\repository\com\microsoft\sqlserver\mssql-jdbc'))) {
        if (Test-Path -LiteralPath $directory) {
            $candidates += Get-ChildItem -LiteralPath $directory -Recurse -File -Filter 'mssql-jdbc*.jar' |
                Where-Object { $_.Name -notmatch 'sources|javadoc' }
        }
    }
    $candidate = $candidates | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($candidate) { $jdbcJar = $candidate.FullName }
}
$classpath = "$classDir;$(Join-Path $repoRoot 'src\resources')"
if ($jdbcJar) { $classpath += ";$jdbcJar" }
$sourceFiles = @(Get-ChildItem (Join-Path $repoRoot 'src\main\java'),(Join-Path $repoRoot 'src\test\java') -Recurse -File -Filter '*.java' |
    Select-Object -ExpandProperty FullName)
& javac --release 11 -encoding UTF-8 -cp $classpath -d $classDir $sourceFiles
if ($LASTEXITCODE -ne 0) { exit 1 }
Write-Host "PASS Java 11 compilation: $($sourceFiles.Count) sources."
if (!$jdbcJar) { Write-Host 'SKIPPED: the SQL Server JDBC JAR is required even by the recording-driver regression.'; exit 2 }
$classes = if ($LiveSql) { @('SecurityRegressionTest','TestRealAuth','FullSystemIntegrationTest','NhanSuModuleTest') } else { @($TestClass) }
$needsSql = $LiveSql -or ($TestClass -notin @('SecurityRegressionTest','LoginChipRegressionTest','LightThemeRegressionTest'))
if ($needsSql -and ([string]::IsNullOrWhiteSpace($env:TEST_SQL_USER) -or [string]::IsNullOrEmpty($env:TEST_SQL_PASSWORD))) {
    Write-Host 'SKIPPED live SQL: JDBC driver and TEST_SQL_USER/TEST_SQL_PASSWORD are required. Set DB_URL to the QA database.'
    if ($LiveSql) { & java -cp $classpath com.test.SecurityRegressionTest; if ($LASTEXITCODE -ne 0) { exit 1 } }
    exit 2
}
$logPath = Join-Path $logDir ("Java_Verification_{0}.log" -f (Get-Date -Format 'yyyyMMdd_HHmmss_fff'))
$exitCode = 0
foreach ($class in $classes) {
    Write-Host "Running $class"
    $ErrorActionPreference = 'Continue'
    & java -cp $classpath "com.test.$class" 2>&1 | ForEach-Object { $_.ToString() } | Tee-Object -FilePath $logPath -Append
    $result = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($result -ne 0) { $exitCode = if ($result -eq 2) { 2 } else { 1 }; break }
}
Write-Host "Log: $logPath"
exit $exitCode
