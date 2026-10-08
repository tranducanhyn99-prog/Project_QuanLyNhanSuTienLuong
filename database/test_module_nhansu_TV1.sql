-- Strict personnel integration test with owned fixtures, no production IDs/logins.
SET NOCOUNT ON;
IF DB_NAME() NOT LIKE 'PRJ[_]Fix[_]QA[_]%' THROW 53300,N'Use a dedicated QA database.',1;
DECLARE @PB INT,@CV INT,@NV INT,@Rejected BIT,@FailedNV INT,@CC INT;
BEGIN TRY
    INSERT dbo.PHONGBAN(TenPB) VALUES(N'TV1_QA'); SET @PB=SCOPE_IDENTITY();
    INSERT dbo.CHUCVU(TenCV) VALUES(N'TV1_QA'); SET @CV=SCOPE_IDENTITY();
    EXEC dbo.sp_ThemNhanVien N'TV1 QA','1990-01-01',N'Nam','699900000001',N'QA','0699900001',
        'tv1_qa@example.invalid','2019-01-01',26000000,@PB,@CV,1,'tv1_qa_fixture',
        'pbkdf2-sha256$600000$fixture-only$fixture-only','Employee',@NV OUTPUT;
    IF @NV IS NULL OR NOT EXISTS(SELECT 1 FROM dbo.TAIKHOAN WHERE MaNV=@NV AND TenDangNhap='tv1_qa_fixture' AND SqlLogin IS NULL)
        THROW 53301,N'Employee/account creation failed.',1;
    PRINT 'PASS employee/profile creation (pending DBA mapping)';
    SET @Rejected=0;
    BEGIN TRY
        INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV)
        VALUES(N'Underage','2011-01-01',N'Nam','699900000002','0699900002','tv1_age@example.invalid','2020-01-01',26000000,@PB,@CV);
    END TRY
    BEGIN CATCH
        IF ERROR_NUMBER()<>547 OR ERROR_MESSAGE() NOT LIKE '%CHK_NHANVIEN_DoTuoi%' THROW;
        SET @Rejected=1;
    END CATCH;
    IF @Rejected<>1 THROW 53302,N'Age constraint failed.',1;
    PRINT 'PASS exact age-constraint rejection';
    SET @Rejected=0;
    BEGIN TRY
        EXEC dbo.sp_ThemNhanVien N'Rollback QA','1990-01-01',N'Nam','699900000003',N'QA','0699900003',
            'tv1_rollback@example.invalid','2019-01-01',26000000,@PB,@CV,1,'tv1_qa_fixture',
            'pbkdf2-sha256$600000$fixture-only$fixture-only','Employee',@FailedNV OUTPUT;
    END TRY
    BEGIN CATCH
        IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%Tên đăng nhập đã tồn tại%' THROW;
        SET @Rejected=1;
    END CATCH;
    IF @Rejected<>1 OR EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE Email='tv1_rollback@example.invalid') OR @@TRANCOUNT<>0
        THROW 53303,N'Account error did not roll back employee.',1;
    PRINT 'PASS account duplicate rolls back employee and history';
    UPDATE dbo.NHANVIEN SET NgayVaoLam='2018-02-03' WHERE MaNV=@NV;
    IF NOT EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE MaNV=@NV AND NgayVaoLam='2018-02-03')
        THROW 53304,N'Hire date update was not persisted.',1;
    PRINT 'PASS hire date persistence';
    EXEC dbo.sp_GhiNhanChamCong @NV,'2020-01-02','08:00','17:00',N'CO_MAT',N'TV1_QA',@CC OUTPUT;
    SET @Rejected=0;
    BEGIN TRY DELETE dbo.NHANVIEN WHERE MaNV=@NV; END TRY
    BEGIN CATCH
        IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%Không được phép xóa nhân viên%' THROW;
        SET @Rejected=1;
    END CATCH;
    IF @Rejected<>1 OR NOT EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE MaNV=@NV)
        THROW 53305,N'Employee with attendance was deleted.',1;
    PRINT 'PASS protected employee delete';
    IF dbo.fn_TinhSoNgayCong(@NV,1,2020)<>1 OR dbo.fn_TinhSoNgayCong(@NV,12,2020)<>0
        THROW 53306,N'Attendance function has wrong values.',1;
    IF NOT EXISTS(SELECT 1 FROM dbo.vw_NhanVien_PhongBan_ChucVu WHERE MaNV=@NV)
        THROW 53307,N'Employee view missed fixture.',1;
    IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.NHANVIEN') AND name='IX_NHANVIEN_HoTen')
        THROW 53308,N'Employee index missing.',1;
    PRINT 'PASS attendance function, joined view and index';
    DELETE dbo.CHAMCONG WHERE MaNV=@NV;
    DELETE dbo.NHANVIEN WHERE MaNV=@NV;
    DELETE dbo.PHONGBAN WHERE MaPB=@PB; DELETE dbo.CHUCVU WHERE MaCV=@CV;
    IF EXISTS(SELECT 1 FROM dbo.LICHSULUONG WHERE MaNV=@NV) OR EXISTS(SELECT 1 FROM dbo.TAIKHOAN WHERE TenDangNhap='tv1_qa_fixture')
        THROW 53309,N'Personnel fixture cleanup failed.',1;
    PRINT 'PASS owned fixture cleanup';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK;
    BEGIN TRY
        DELETE dbo.CHAMCONG WHERE MaNV IN(@NV,@FailedNV);
        DELETE dbo.NHANVIEN WHERE MaNV IN(@NV,@FailedNV);
        DELETE dbo.PHONGBAN WHERE MaPB=@PB; DELETE dbo.CHUCVU WHERE MaCV=@CV;
    END TRY
    BEGIN CATCH PRINT N'Cleanup failed: '+ERROR_MESSAGE(); END CATCH;
    THROW;
END CATCH;
