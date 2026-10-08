-- Dedicated QA database only. Every expected rejection checks its specific error.
SET NOCOUNT ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
IF DB_NAME() NOT LIKE 'PRJ[_]Fix[_]QA[_]%' THROW 53000,N'Run only in the dedicated PRJ_Fix_QA database.',1;
IF EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE Nam=2020 AND Thang IN (1,2))
    THROW 53001,N'QA periods must be unused.',1;
DECLARE @PB INT,@CV INT,@NV INT,@NV2 INT,@Closed INT,@Draft INT,@PC INT,@PCDraft INT,@KT INT,@KTDraft INT,@CC INT,@CCDraft INT,@Detail INT,@DetailDraft INT;
BEGIN TRY
    INSERT dbo.PHONGBAN(TenPB) VALUES(N'FIX_REGRESSION'); SET @PB=SCOPE_IDENTITY();
    INSERT dbo.CHUCVU(TenCV) VALUES(N'FIX_REGRESSION'); SET @CV=SCOPE_IDENTITY();
    INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV)
    VALUES(N'QA A','1990-01-01',N'Nam','909900000001','0909900001','qa_a@example.invalid','2019-01-01',26000000,@PB,@CV);
    SET @NV=SCOPE_IDENTITY();
    INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV)
    VALUES(N'QA B','1990-01-01',N'Nam','909900000002','0909900002','qa_b@example.invalid','2019-01-01',26000000,@PB,@CV);
    SET @NV2=SCOPE_IDENTITY();
    INSERT dbo.CHAMCONG(MaNV,NgayChamCong,GioVao,GioRa,TrangThai) VALUES(@NV,'2020-01-02','08:00','17:00',N'CO_MAT'); SET @CC=SCOPE_IDENTITY();
    INSERT dbo.CHAMCONG(MaNV,NgayChamCong,GioVao,GioRa,TrangThai) VALUES(@NV2,'2020-02-02','08:00','17:00',N'CO_MAT'); SET @CCDraft=SCOPE_IDENTITY();
    INSERT dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien) VALUES(@NV,1,2020,N'QA',100); SET @PC=SCOPE_IDENTITY();
    INSERT dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien) VALUES(@NV2,2,2020,N'QA',100); SET @PCDraft=SCOPE_IDENTITY();
    INSERT dbo.KHAUTRUNHANVIEN(MaNV,Thang,Nam,TenKhauTru,LyDo,SoTien) VALUES(@NV,1,2020,N'QA',N'QA',50); SET @KT=SCOPE_IDENTITY();
    INSERT dbo.KHAUTRUNHANVIEN(MaNV,Thang,Nam,TenKhauTru,LyDo,SoTien) VALUES(@NV2,2,2020,N'QA',N'QA',50); SET @KTDraft=SCOPE_IDENTITY();
    EXEC dbo.sp_TinhBangLuongThang 1,2020,26,@Closed OUTPUT;
    EXEC dbo.sp_TinhBangLuongThang 2,2020,26,@Draft OUTPUT;
    SELECT @Detail=MaChiTiet FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Closed AND MaNV=@NV;
    SELECT @DetailDraft=MaChiTiet FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Draft AND MaNV=@NV2;
    -- A departed employee remains in the old period; a new rate does not rewrite old pay.
    UPDATE dbo.NHANVIEN SET TrangThai=N'NGHI_VIEC',LuongCoBan=52000000 WHERE MaNV=@NV;
    EXEC dbo.sp_TinhBangLuongThang 1,2020,26,@Closed OUTPUT;
    IF NOT EXISTS(SELECT 1 FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Closed AND MaNV=@NV AND LuongCoBan=26000000)
        THROW 53002,N'Historical employee/rate regression.',1;
    SELECT @Detail=MaChiTiet FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Closed AND MaNV=@NV;
    -- Remove B's zero-day January row so move/insert tests reach the close trigger.
    DELETE dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Closed AND MaNV=@NV2;
    DELETE dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Draft AND MaNV=@NV;
    EXEC dbo.sp_ChotBangLuong @MaBangLuong=@Closed;
    PRINT 'PASS historical employee/rate';

    DECLARE @Cases TABLE(Id INT IDENTITY,Label VARCHAR(80),SqlText NVARCHAR(MAX));
    INSERT @Cases(Label,SqlText) VALUES
    ('detail insert',N'INSERT dbo.CHITIETBANGLUONG(MaBangLuong,MaNV,LuongCoBan) VALUES('+CAST(@Closed AS VARCHAR)+N','+CAST(@NV2 AS VARCHAR)+N',26000000)'),
    ('detail update',N'UPDATE dbo.CHITIETBANGLUONG SET TienCong=1 WHERE MaChiTiet='+CAST(@Detail AS VARCHAR)),
    ('detail delete',N'DELETE dbo.CHITIETBANGLUONG WHERE MaChiTiet='+CAST(@Detail AS VARCHAR)),
    ('detail move into closed',N'UPDATE dbo.CHITIETBANGLUONG SET MaBangLuong='+CAST(@Closed AS VARCHAR)+N' WHERE MaChiTiet='+CAST(@DetailDraft AS VARCHAR)),
    ('detail move out of closed',N'UPDATE dbo.CHITIETBANGLUONG SET MaBangLuong='+CAST(@Draft AS VARCHAR)+N' WHERE MaChiTiet='+CAST(@Detail AS VARCHAR)),
    ('allowance insert',N'INSERT dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien) VALUES('+CAST(@NV2 AS VARCHAR)+N',1,2020,N''QA'',1)'),
    ('allowance update',N'UPDATE dbo.PHUCAPNHANVIEN SET SoTien=1 WHERE MaPCNV='+CAST(@PC AS VARCHAR)),
    ('allowance delete',N'DELETE dbo.PHUCAPNHANVIEN WHERE MaPCNV='+CAST(@PC AS VARCHAR)),
    ('allowance move',N'UPDATE dbo.PHUCAPNHANVIEN SET Thang=1 WHERE MaPCNV='+CAST(@PCDraft AS VARCHAR)),
    ('deduction insert',N'INSERT dbo.KHAUTRUNHANVIEN(MaNV,Thang,Nam,TenKhauTru,LyDo,SoTien) VALUES('+CAST(@NV2 AS VARCHAR)+N',1,2020,N''QA'',N''QA'',1)'),
    ('deduction update',N'UPDATE dbo.KHAUTRUNHANVIEN SET SoTien=1 WHERE MaKTNV='+CAST(@KT AS VARCHAR)),
    ('deduction delete',N'DELETE dbo.KHAUTRUNHANVIEN WHERE MaKTNV='+CAST(@KT AS VARCHAR)),
    ('deduction move',N'UPDATE dbo.KHAUTRUNHANVIEN SET Thang=1 WHERE MaKTNV='+CAST(@KTDraft AS VARCHAR)),
    ('attendance insert',N'INSERT dbo.CHAMCONG(MaNV,NgayChamCong,GioVao,GioRa,TrangThai) VALUES('+CAST(@NV2 AS VARCHAR)+N',''2020-01-03'',''08:00'',''17:00'',N''CO_MAT'')'),
    ('attendance update',N'UPDATE dbo.CHAMCONG SET GhiChu=N''changed'' WHERE MaChamCong='+CAST(@CC AS VARCHAR)),
    ('attendance delete',N'DELETE dbo.CHAMCONG WHERE MaChamCong='+CAST(@CC AS VARCHAR)),
    ('attendance move',N'UPDATE dbo.CHAMCONG SET NgayChamCong=''2020-01-03'' WHERE MaChamCong='+CAST(@CCDraft AS VARCHAR));
    DECLARE @Id INT=1,@Sql NVARCHAR(MAX),@Label VARCHAR(80),@Rejected BIT;
    WHILE @Id<=(SELECT COUNT(*) FROM @Cases)
    BEGIN
        SELECT @Sql=SqlText,@Label=Label FROM @Cases WHERE Id=@Id;
        SET @Rejected=0;
        BEGIN TRY EXEC sys.sp_executesql @Sql; END TRY
        BEGIN CATCH
            IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%chốt%' THROW;
            SET @Rejected=1;
        END CATCH;
        IF @Rejected=0 THROW 53003,N'Closed-period mutation was accepted.',1;
        IF @@TRANCOUNT<>0 THROW 53004,N'Transaction leaked after rejection.',1;
        PRINT 'PASS '+@Label;
        SET @Id+=1;
    END;
    IF dbo.fn_TongPhuCap(@NV,1,2020)<>100 OR dbo.fn_TongPhuCap(@NV,12,2020)<>0
        THROW 53005,N'Allowance function regression.',1;
    PRINT 'PASS allowance function sum/empty';
    EXEC dbo.sp_HuyChotBangLuong @Closed;
    DELETE dbo.CHITIETBANGLUONG WHERE MaBangLuong IN (@Closed,@Draft);
    DELETE dbo.BANGLUONG WHERE MaBangLuong IN (@Closed,@Draft);
    DELETE dbo.PHUCAPNHANVIEN WHERE MaNV IN (@NV,@NV2);
    DELETE dbo.KHAUTRUNHANVIEN WHERE MaNV IN (@NV,@NV2);
    DELETE dbo.CHAMCONG WHERE MaNV IN (@NV,@NV2);
    DELETE dbo.NHANVIEN WHERE MaNV IN (@NV,@NV2);
    DELETE dbo.PHONGBAN WHERE MaPB=@PB;
    DELETE dbo.CHUCVU WHERE MaCV=@CV;
    IF EXISTS(SELECT 1 FROM dbo.LICHSULUONG WHERE MaNV IN (@NV,@NV2)) THROW 53006,N'History fixture leaked.',1;
    PRINT 'PASS cleanup';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK;
    BEGIN TRY
        IF EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong=@Closed AND TrangThai='DA_CHOT') EXEC dbo.sp_HuyChotBangLuong @Closed;
        DELETE dbo.CHITIETBANGLUONG WHERE MaBangLuong IN (@Closed,@Draft);
        DELETE dbo.BANGLUONG WHERE MaBangLuong IN (@Closed,@Draft);
        DELETE dbo.PHUCAPNHANVIEN WHERE MaNV IN (@NV,@NV2);
        DELETE dbo.KHAUTRUNHANVIEN WHERE MaNV IN (@NV,@NV2);
        DELETE dbo.CHAMCONG WHERE MaNV IN (@NV,@NV2);
        DELETE dbo.NHANVIEN WHERE MaNV IN (@NV,@NV2);
        DELETE dbo.PHONGBAN WHERE MaPB=@PB;
        DELETE dbo.CHUCVU WHERE MaCV=@CV;
    END TRY
    BEGIN CATCH
        PRINT N'Cleanup failed: '+ERROR_MESSAGE();
    END CATCH;
    THROW;
END CATCH;
