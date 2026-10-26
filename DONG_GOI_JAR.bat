@echo off
title Dong goi Runnable JAR - Quan Ly Nhan Su va Tien Luong
cd /d "%~dp0"

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build_jar.ps1"

if %errorlevel% neq 0 (
    echo.
    echo [LOI] Khong the dong goi file JAR!
    pause
    exit /b %errorlevel%
)

echo.
pause
