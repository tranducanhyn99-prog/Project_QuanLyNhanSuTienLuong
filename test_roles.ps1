$ErrorActionPreference = "Stop"

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "   KHOI DONG CONG CU TEST PHAN QUYEN & SESSION (TV5)          " -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan

$jdbcJar = "C:\Users\DUCANHZZ\.m2\repository\com\microsoft\sqlserver\mssql-jdbc\12.6.4.jre11\mssql-jdbc-12.6.4.jre11.jar"
$cp = "bin;$jdbcJar"

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "Dang bien dich ma nguon Java..." -ForegroundColor Yellow
$sources = Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp $cp -d bin $sources

Write-Host "[OK] Bien dich thanh cong! Dang mo giao dien QuickTestLauncher..." -ForegroundColor Green
Start-Process -FilePath "java" -ArgumentList "-cp `"$cp`" com.test.QuickTestLauncher"
Write-Host ">>> Cua so kiem thu da duoc mo tren man hinh cua ban! <<<" -ForegroundColor Green
