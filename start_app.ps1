# Script khoi chay ung dung chinh (LoginFrame) ket noi CSDL thuc te
$ErrorActionPreference = "Stop"

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "   KHOI CHAY UNG DUNG QUAN LY NHAN SU & TIEN LUONG            " -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan

$jdbcJar = "C:\Users\DUCANHZZ\.m2\repository\com\microsoft\sqlserver\mssql-jdbc\12.6.4.jre11\mssql-jdbc-12.6.4.jre11.jar"
$cp = "bin;src/resources;$jdbcJar"

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

# Sao chep config.properties vao bin neu co
if (Test-Path "src/resources/config.properties") {
    Copy-Item "src/resources/config.properties" "bin/config.properties" -Force
}

Write-Host "Dang bien dich ma nguon Java..." -ForegroundColor Yellow
$sources = Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp $cp -d bin $sources

Write-Host "[OK] Dang mo man hinh dang nhap (LoginFrame)..." -ForegroundColor Green
Start-Process -FilePath "java" -ArgumentList "-cp `"$cp`" com.ui.auth.LoginFrame"
Write-Host ">>> Man hinh LoginFrame da mo tren man hinh! <<<" -ForegroundColor Green
Write-Host ">>> Tai khoan test: admin / 123456 (DB_Admin) <<<" -ForegroundColor Green
