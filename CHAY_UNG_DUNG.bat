@echo off
title Quan Ly Nhan Su va Tien Luong - Nhom 06
cd /d "%~dp0"

echo ======================================================================
echo    KHOI CHAY UNG DUNG QUAN LY NHAN SU VA TIEN LUONG (NHOM 06)
echo ======================================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start_app.ps1"

if %errorlevel% neq 0 (
    echo.
    echo [LOI] Khong the khoi chay ung dung!
    echo Vui long kiem tra lai moi truong Java va SQL Server.
    echo.
    pause
    exit /b %errorlevel%
)

echo.
echo ----------------------------------------------------------------------
echo  DANH SACH TAI KHOAN DEMO (Dung dang nhap tren man hinh GUI):
echo  - DB Admin:        gui_admin   / Gui9!RViC8RsC5P6e0Xt8kGcEXw==
echo  - HR Manager:      gui_hr      / Gui9!2aC+pyk01j8/1/rs3V+1pg==
echo  - Ke toan luong:   gui_payroll / Gui9!M2oYx/cQugmI/Y9pYrawlA==
echo  - Nhan vien A:     gui_a       / Gui9!41Ay2bMK+OIlMSeew2Or3Q==
echo  - Nhan vien B:     gui_b       / Gui9!f9bTBwZZNFygXZU3FkknSw==
echo ----------------------------------------------------------------------
echo.
echo [OK] Giao dien dang nhap da duoc mo.
echo Nhan phim bat ky de dong cua so nay...
pause >nul
