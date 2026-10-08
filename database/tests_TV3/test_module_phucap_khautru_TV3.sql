-- Dedicated QA fixture test; closed-period mutations are also covered by tests/Fix_Regression.sql.
SET NOCOUNT ON;
IF DB_NAME() NOT LIKE 'PRJ[_]Fix[_]QA[_]%' THROW 53400,N'Use a dedicated QA database.',1;
IF EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE Nam=2020 AND Thang IN(5,6)) THROW 53401,N'QA periods already exist.',1;
DECLARE @PB INT,@CV INT,@NV INT,@Draft INT,@Closed INT,@Rejected BIT;
BEGIN TRY
    INSERT dbo.PHONGBAN(TenPB) VALUES(N'TV3_QA'); SET @PB=SCOPE_IDENTITY();
    INSERT dbo.CHUCVU(TenCV) VALUES(N'TV3_QA'); SET @CV=SCOPE_IDENTITY();
    INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV)
    VALUES(N'TV3 QA','1990-01-01',N'Nam','699900000010','0699900010','tv3_qa@example.invalid','2019-01-01',26000000,@PB,@CV);
    SET @NV=SCOPE_IDENTITY();
    INSERT dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien) VALUES(@NV,5,2020,N'QA A',100),(@NV,5,2020,N'QA B',200);
    INSERT dbo.KHAUTRUNHANVIEN(MaNV,Thang,Nam,TenKhauTru,SoTien) VALUES(@NV,5,2020,N'QA A',20),(@NV,5,2020,N'QA B',30);
    IF dbo.fn_TongPhuCap(@NV,5,2020)<>300 OR dbo.fn_TongPhuCap(@NV,12,2020)<>0
      OR dbo.fn_TongKhauTru(@NV,5,2020)<>50 OR dbo.fn_TongKhauTru(@NV,12,2020)<>0
        THROW 53402,N'Sum/empty allowance or deduction function failed.',1;
    IF NOT EXISTS(SELECT 1 FROM dbo.vw_TongPhuCapThang WHERE MaNV=@NV AND Thang=5 AND Nam=2020 AND TongTienPhuCap=300 AND SoKhoanPhuCap=2)
      OR NOT EXISTS(SELECT 1 FROM dbo.vw_TongKhauTruThang WHERE MaNV=@NV AND Thang=5 AND Nam=2020 AND TongKhauTru=50)
        THROW 53403,N'Aggregate view values failed.',1;
    PRINT 'PASS sum/empty functions and both aggregate views';
    SET @Rejected=0;
    BEGIN TRY INSERT dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien) VALUES(@NV,5,2020,N'Negative',-1); END TRY
    BEGIN CATCH IF ERROR_NUMBER()<>547 OR ERROR_MESSAGE() NOT LIKE '%CK_PHUCAP_SoTien%' THROW; SET @Rejected=1; END CATCH;
    IF @Rejected<>1 THROW 53404,N'Negative allowance accepted.',1;
    SET @Rejected=0;
    BEGIN TRY INSERT dbo.KHAUTRUNHANVIEN(MaNV,Thang,Nam,TenKhauTru,SoTien) VALUES(@NV,13,2020,N'Wrong month',1); END TRY
    BEGIN CATCH IF ERROR_NUMBER()<>547 OR ERROR_MESSAGE() NOT LIKE '%CK_KHAUTRU_Thang%' THROW; SET @Rejected=1; END CATCH;
    IF @Rejected<>1 THROW 53405,N'Invalid deduction month accepted.',1;
    PRINT 'PASS exact money/month constraint errors';
    INSERT dbo.BANGLUONG(Thang,Nam,NgayCongChuan) VALUES(5,2020,26); SET @Draft=SCOPE_IDENTITY();
    EXEC dbo.sp_XoaKyLuongChuaChot 5,2020;
    IF EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong=@Draft) THROW 53406,N'Draft deletion failed.',1;
    INSERT dbo.BANGLUONG(Thang,Nam,NgayCongChuan,TrangThai) VALUES(6,2020,26,'DA_CHOT'); SET @Closed=SCOPE_IDENTITY();
    SET @Rejected=0;
    BEGIN TRY EXEC dbo.sp_XoaKyLuongChuaChot 6,2020; END TRY
    BEGIN CATCH IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%CHỐT SỔ%' THROW; SET @Rejected=1; END CATCH;
    IF @Rejected<>1 OR NOT EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong=@Closed AND TrangThai='DA_CHOT') OR @@TRANCOUNT<>0
        THROW 53407,N'Closed deletion or transaction invariant failed.',1;
    PRINT 'PASS draft delete and closed delete rejection';
    EXEC dbo.sp_HuyChotBangLuong @Closed;
    DELETE dbo.BANGLUONG WHERE MaBangLuong=@Closed;
    DELETE dbo.PHUCAPNHANVIEN WHERE MaNV=@NV; DELETE dbo.KHAUTRUNHANVIEN WHERE MaNV=@NV;
    DELETE dbo.NHANVIEN WHERE MaNV=@NV; DELETE dbo.PHONGBAN WHERE MaPB=@PB; DELETE dbo.CHUCVU WHERE MaCV=@CV;
    PRINT 'PASS owned fixture cleanup';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK;
    BEGIN TRY
        IF EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong=@Closed AND TrangThai='DA_CHOT') EXEC dbo.sp_HuyChotBangLuong @Closed;
        DELETE dbo.CHITIETBANGLUONG WHERE MaBangLuong IN(@Closed,@Draft);
        DELETE dbo.BANGLUONG WHERE MaBangLuong IN(@Closed,@Draft);
        DELETE dbo.PHUCAPNHANVIEN WHERE MaNV=@NV; DELETE dbo.KHAUTRUNHANVIEN WHERE MaNV=@NV;
        DELETE dbo.NHANVIEN WHERE MaNV=@NV; DELETE dbo.PHONGBAN WHERE MaPB=@PB; DELETE dbo.CHUCVU WHERE MaCV=@CV;
    END TRY
    BEGIN CATCH PRINT N'Cleanup failed: '+ERROR_MESSAGE(); END CATCH;
    THROW;
END CATCH;
