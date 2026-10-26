# Script dong goi file JAR thuc thi (Fat JAR)
$ErrorActionPreference = "Stop"

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "   DONG GOI FILE JAR THUC THI (RUNNABLE FAT JAR)              " -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan

$repoRoot = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }
Set-Location $repoRoot

$binDir = Join-Path $repoRoot "build\app"
$resDir = Join-Path $repoRoot "src\resources"
$libJar = Join-Path $repoRoot "lib\mssql-jdbc-12.6.1.jre11.jar"

if (!(Test-Path $libJar)) {
    Write-Error "Khong tim thay thu vien SQL Server JDBC Driver: $libJar"
    exit 1
}

Write-Host "[1/3] Kiem tra va bien dich ma nguon Java..." -ForegroundColor Yellow
if (!(Test-Path $binDir)) { New-Item -ItemType Directory -Path $binDir | Out-Null }
if (Test-Path (Join-Path $resDir "config.properties")) {
    Copy-Item (Join-Path $resDir "config.properties") (Join-Path $binDir "config.properties") -Force
}

$cp = "$binDir;$resDir;$libJar"
$sources = Get-ChildItem -Path (Join-Path $repoRoot "src\main\java") -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
javac --release 11 -encoding UTF-8 -cp $cp -d $binDir $sources
if ($LASTEXITCODE -ne 0) {
    Write-Error "Bien dich ma nguon Java that bai!"
    exit 1
}

Write-Host "[2/3] Gop ma nguon va thu vien SQL Server JDBC vao thu muc dong goi..." -ForegroundColor Yellow
$fatDir = Join-Path $repoRoot "build\fatjar"
if (Test-Path $fatDir) { Remove-Item -Recurse -Force $fatDir }
New-Item -ItemType Directory -Path $fatDir | Out-Null
Copy-Item -Recurse (Join-Path $binDir "*") $fatDir

Push-Location $fatDir
jar xf $libJar
Get-ChildItem -Path "META-INF" -Include "*.SF","*.DSA","*.RSA" -File -Recurse -ErrorAction SilentlyContinue | Remove-Item -Force
Pop-Location

$manifestPath = Join-Path $fatDir "MANIFEST.MF"
@"
Manifest-Version: 1.0
Main-Class: com.ui.auth.LoginFrame
"@ | Set-Content -Path $manifestPath -Encoding ASCII

Write-Host "[3/3] Xuat file QuanLyNhanSuTienLuong.jar..." -ForegroundColor Yellow
$outJar = Join-Path $repoRoot "QuanLyNhanSuTienLuong.jar"
jar cfm $outJar $manifestPath -C $fatDir .
if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "==============================================================" -ForegroundColor Green
    Write-Host "[THANH CONG] Da tao file: QuanLyNhanSuTienLuong.jar" -ForegroundColor Green
    Write-Host "File JAR nay chua 100% giao dien, ma nguon va JDBC driver." -ForegroundColor Green
    Write-Host "Cach su dung:" -ForegroundColor Green
    Write-Host "  1. Click dup vao file QuanLyNhanSuTienLuong.jar de chay." -ForegroundColor Green
    Write-Host "  2. Hoac mo terminal chay: java -jar QuanLyNhanSuTienLuong.jar" -ForegroundColor Green
    Write-Host "==============================================================" -ForegroundColor Green
} else {
    Write-Error "Dong goi JAR that bai!"
}
