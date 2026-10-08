-- OPTIONAL: run only in a dedicated demo database, after modules 01..05.
USE QuanLyNhanSuTienLuong;
GO
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
    DECLARE @PB INT = (SELECT TOP(1) MaPB FROM dbo.PHONGBAN WHERE TrangThai = N'HOAT_DONG' ORDER BY MaPB);
    DECLARE @CV INT = (SELECT TOP(1) MaCV FROM dbo.CHUCVU ORDER BY MaCV);
    IF @PB IS NULL OR @CV IS NULL THROW 51010, N'Thiếu phòng ban/chức vụ để tạo demo.', 1;
    IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE Email = 'employee01@example.invalid')
        INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,DiaChi,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV,TrangThai)
        VALUES(N'Nhân viên demo','1990-01-01',N'Nam','999999999991',N'Demo','0999999991','employee01@example.invalid','2020-01-01',12000000,@PB,@CV,N'DANG_LAM_VIEC');
    DECLARE @NV INT = (SELECT MaNV FROM dbo.NHANVIEN WHERE Email = 'employee01@example.invalid');
    IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV=@NV AND GhiChu=N'DEMO_SEED_V1')
       AND NOT EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE Thang=9 AND Nam=2026 AND TrangThai='DA_CHOT')
        INSERT dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien,NgayGhiNhan,GhiChu)
        VALUES(@NV,9,2026,N'Phụ cấp demo',730000,'2026-09-01',N'DEMO_SEED_V1');
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
-- Accounts/SQL logins are provisioned explicitly by a DBA; no default passwords.
