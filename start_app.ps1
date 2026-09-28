# Script khởi chạy ứng dụng chính (LoginFrame) kết nối CSDL thực tế
$ErrorActionPreference = "Stop"

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "   KHỞI CHẠY ỨNG DỤNG QUẢN LÝ NHÂN SỰ & TIỀN LƯƠNG            " -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan

$jdbcJar = "C:\Users\DUCANHZZ\.m2\repository\com\microsoft\sqlserver\mssql-jdbc\12.6.4.jre11\mssql-jdbc-12.6.4.jre11.jar"
$cp = "bin;src/resources;$jdbcJar"

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

# Sao chép config.properties vào bin nếu có
if (Test-Path "src/resources/config.properties") {
    Copy-Item "src/resources/config.properties" "bin/config.properties" -Force
}

Write-Host "Đang biên dịch mã nguồn Java..." -ForegroundColor Yellow
$sources = Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp $cp -d bin $sources

Write-Host "[OK] Đang mở màn hình đăng nhập (LoginFrame)..." -ForegroundColor Green
Start-Process -FilePath "java" -ArgumentList "-cp `"$cp`" com.ui.auth.LoginFrame"
Write-Host ">>> Màn hình LoginFrame đã sẵn sàng! Bạn có thể đăng nhập thử với admin / 123456 <<<" -ForegroundColor Green
