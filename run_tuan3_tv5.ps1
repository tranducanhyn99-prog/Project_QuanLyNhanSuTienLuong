param (
    [switch]$SkipScreenshots
)

$ErrorActionPreference = "Stop"

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "   BỘ KIỂM THỬ TÍCH HỢP TOÀN DỰ ÁN & DEMO TUẦN 3 (TV5)        " -ForegroundColor Cyan
Write-Host "   Thành viên 5: Trần Đức Anh - MSSV: 24110155                " -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan

# Neo duong dan ve goc repository
$repoRoot = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }
Set-Location $repoRoot

# 1. Trinh phan giai driver SQL Server JDBC da nen tang
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
    Write-Error "Khong tim thay thu vien SQL Server JDBC Driver (mssql-jdbc*.jar)! Vui long kiem tra thu muc lib/."
    exit 1
}

$binDir = Join-Path $repoRoot "bin"
$resDir = Join-Path $repoRoot "src\resources"
$cp = "$binDir;$resDir;$jdbcJar"

if (!(Test-Path $binDir)) {
    New-Item -ItemType Directory -Path $binDir | Out-Null
}

$srcDir = Join-Path $repoRoot "src"
Write-Host "`n[1/3] Dang bien dich toan bo ma nguon Java du an..." -ForegroundColor Yellow
$sources = Get-ChildItem -Path $srcDir -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp $cp -d $binDir $sources
if ($LASTEXITCODE -ne 0) {
    Write-Error "Bien dich ma nguon Java that bai!"
    exit 1
}
Write-Host "[OK] Bien dich 100% thanh cong!" -ForegroundColor Green

# 2. Chay FullSystemIntegrationTest
Write-Host "`n[2/3] Dang thuc thi Kiem thu tich hop 5 module toan he thong..." -ForegroundColor Yellow
java -cp $cp com.test.FullSystemIntegrationTest
if ($LASTEXITCODE -ne 0) {
    Write-Error "Kiem thu tich hop he thong that bai!"
    exit 1
}

# 3. Chup anh man hinh tu dong
if (-not $SkipScreenshots) {
    Write-Host "`n[3/3] Dang tu dong chup 10 anh giao dien cho bao cao cuoi ky..." -ForegroundColor Yellow
    java -cp $cp com.test.CaptureScreenshots
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Da chup toan bo 10 anh giao dien vao thu muc screenshots/!" -ForegroundColor Green
    } else {
        Write-Warning "Khong the chup anh tu dong (co the do moi truong khong ho tro GUI). Ban co the bo qua bang -SkipScreenshots."
    }
} else {
    Write-Host "`n[3/3] Bo qua buoc chup anh man hinh theo yeu cau (-SkipScreenshots)." -ForegroundColor Cyan
}

Write-Host "`n==============================================================" -ForegroundColor Green
Write-Host "   HOAN THANH TOAN BO TASK TUAN 3 CHO TV5 (TRẦN ĐỨC ANH)     " -ForegroundColor Green
Write-Host "   - 1. Benchmark Index: database/test_benchmark_index_TV5.sql " -ForegroundColor Green
Write-Host "   - 2. Security Roles:  database/test_security_roles_TV5.sql   " -ForegroundColor Green
Write-Host "   - 3. Concurrency:     database/test_concurrency_demo_TV5.sql " -ForegroundColor Green
Write-Host "   - 4. Integration:     com.test.FullSystemIntegrationTest     " -ForegroundColor Green
Write-Host "   - 5. Screenshots:     screenshots/ (10 anh PNG)              " -ForegroundColor Green
Write-Host "==============================================================" -ForegroundColor Green
