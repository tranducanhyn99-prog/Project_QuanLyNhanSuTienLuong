# Script khoi chay ung dung chinh (LoginFrame) ket noi CSDL thuc te
$ErrorActionPreference = "Stop"

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "   KHOI CHAY UNG DUNG QUAN LY NHAN SU & TIEN LUONG            " -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan

# Neo duong dan ve goc repository
$repoRoot = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }
Set-Location $repoRoot

# Trinh phan giai driver SQL Server JDBC da nen tang
$jdbcJar = $null
if ($env:MSSQL_JDBC_JAR -and (Test-Path $env:MSSQL_JDBC_JAR)) {
    $jdbcJar = (Resolve-Path $env:MSSQL_JDBC_JAR).Path
}

if (-not $jdbcJar) {
    $projectLib = Join-Path $repoRoot "lib"
    if (Test-Path $projectLib) {
        $candidate = Get-ChildItem -Path $projectLib -Filter "*.jar" -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match "mssql-jdbc" -and $_.Name -notmatch "sources|javadoc" } |
            Select-Object -First 1
        if ($candidate) { $jdbcJar = $candidate.FullName }
    }
}

if (-not $jdbcJar) {
    $userHome = if ($env:USERPROFILE) { $env:USERPROFILE } else { [Environment]::GetFolderPath("UserProfile") }
    $m2Dir = Join-Path $userHome ".m2\repository\com\microsoft\sqlserver\mssql-jdbc"
    if (Test-Path $m2Dir) {
        $candidate = Get-ChildItem -Path $m2Dir -Recurse -Filter "*.jar" -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match "mssql-jdbc" -and $_.Name -notmatch "sources|javadoc" } |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
        if ($candidate) { $jdbcJar = $candidate.FullName }
    }
}

if (-not $jdbcJar -or !(Test-Path $jdbcJar)) {
    Write-Error "Khong tim thay thu vien SQL Server JDBC Driver (mssql-jdbc*.jar)! Vui long dat file JAR vao thu muc lib/, tai ve qua Maven (.m2), hoac cau hinh bien moi truong MSSQL_JDBC_JAR."
    exit 1
}

$binDir = Join-Path $repoRoot "bin"
$resDir = Join-Path $repoRoot "src\resources"
$cp = "$binDir;$resDir;$jdbcJar"

if (!(Test-Path $binDir)) {
    New-Item -ItemType Directory -Path $binDir | Out-Null
}

# Sao chep config.properties vao bin neu co
$configFile = Join-Path $resDir "config.properties"
$targetConfig = Join-Path $binDir "config.properties"
if (Test-Path $configFile) {
    Copy-Item $configFile $targetConfig -Force
}

$srcDir = Join-Path $repoRoot "src"
Write-Host "Dang bien dich ma nguon Java..." -ForegroundColor Yellow
$sources = Get-ChildItem -Path $srcDir -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp $cp -d $binDir $sources
if ($LASTEXITCODE -ne 0) {
    Write-Error "Bien dich ma nguon Java that bai!"
    exit 1
}

Write-Host "[OK] Bien dich thanh cong! Dang mo man hinh dang nhap (LoginFrame)..." -ForegroundColor Green
Start-Process -FilePath "java" -ArgumentList "-cp `"$cp`" com.ui.auth.LoginFrame"
Write-Host "[INFO] Man hinh LoginFrame da mo tren man hinh!" -ForegroundColor Green
Write-Host "[INFO] Tai khoan test: admin / 123456 (DB_Admin)" -ForegroundColor Green
