-- TV5-01: exact DECIMAL(18,2), NULL=0, signed net pay and atomic overflow.
-- Writes are refused outside the isolated QA naming convention.
SET NOCOUNT ON;
SET XACT_ABORT OFF;
IF DB_NAME() NOT LIKE 'PRJ[_]Fix[_]QA[_]%'
    THROW 53500, N'Money regression requires an isolated PRJ_Fix_QA database.', 1;
IF @@TRANCOUNT <> 0 THROW 53501, N'Run outside a caller transaction.', 1;

DECLARE @Max DECIMAL(18,2)=9999999999999999.99;
DECLARE @Cases TABLE (CaseName VARCHAR(60), Wage DECIMAL(18,2), Allowance DECIMAL(18,2),
    Deduction DECIMAL(18,2), Expected DECIMAL(18,2));
INSERT @Cases VALUES
 ('all_null',NULL,NULL,NULL,0), ('zero',0,0,0,0),
 ('individual_null',NULL,25.35,10.10,15.25),
 ('negative_net',100.01,0,150.02,-50.01),
 ('decimal_exact',100.11,25.22,10.33,115.00),
 ('above_old_decimal15_limit',10000000000000.01,0,0,10000000000000.01),
 ('maximum',@Max,0,0,@Max), ('minimum',0,0,@Max,-@Max),
 ('large_intermediate_cancels',@Max,@Max,@Max,@Max);
IF EXISTS (SELECT 1 FROM @Cases
    WHERE dbo.fn_TinhThucNhan(Wage,Allowance,Deduction)<>Expected
       OR dbo.fn_TinhThucNhan(Wage,Allowance,Deduction) IS NULL)
    THROW 53502, N'Net pay boundary mismatch.', 1;
IF EXISTS (SELECT 1 FROM sys.parameters WHERE object_id=OBJECT_ID('dbo.fn_TinhThucNhan')
    AND (precision<>18 OR scale<>2 OR system_type_id<>106))
    THROW 53503, N'Net pay signature is not DECIMAL(18,2).', 1;
SELECT CaseName, Wage, Allowance, Deduction, Expected,
    dbo.fn_TinhThucNhan(Wage,Allowance,Deduction) AS Actual FROM @Cases;

DECLARE @Caught BIT=0, @Value DECIMAL(18,2);
BEGIN TRY
    SET @Value=dbo.fn_TinhThucNhan(@Max,0.01,0);
END TRY BEGIN CATCH
    IF ERROR_NUMBER()<>8115 THROW;
    SET @Caught=1;
    SELECT 'positive_output_overflow' AS CaseName, ERROR_NUMBER() AS ErrorNumber;
END CATCH;
IF @Caught=0 THROW 53504, N'Positive net overflow must fail.', 1;
SET @Caught=0;
BEGIN TRY
    SET @Value=dbo.fn_TinhThucNhan(-@Max,0,0.01);
END TRY BEGIN CATCH
    IF ERROR_NUMBER()<>8115 THROW;
    SET @Caught=1;
    SELECT 'negative_output_overflow' AS CaseName, ERROR_NUMBER() AS ErrorNumber;
END CATCH;
IF @Caught=0 THROW 53505, N'Negative net overflow must fail.', 1;
SET @Caught=0;
BEGIN TRY
    SET @Value=dbo.fn_TinhThucNhan(CAST(10000000000000000.00 AS DECIMAL(19,2)),0,0);
END TRY BEGIN CATCH
    IF ERROR_NUMBER()<>8115 THROW;
    SET @Caught=1;
    SELECT 'input_overflow' AS CaseName, ERROR_NUMBER() AS ErrorNumber;
END CATCH;
IF @Caught=0 THROW 53506, N'Out-of-domain input must fail.', 1;
IF dbo.fn_TinhTienCong(@Max,26,26)<>@Max
   OR dbo.fn_TinhTienCong(100.01,26,13)<>50.01
   OR dbo.fn_TinhTienCong(NULL,26,26)<>0
   OR dbo.fn_TinhTienCong(100,0,26)<>0
    THROW 53507, N'TV4 wage caller boundary mismatch.', 1;
PRINT 'PASS net pay cases, input/output overflow 8115, and wage rounding boundaries';

DECLARE @Token VARCHAR(32)=REPLACE(CONVERT(VARCHAR(36),NEWID()),'-','');
DECLARE @PB INT,@CV INT,@NV INT,@Payroll INT,@Output INT;
DECLARE @Month INT=12,@Year INT=2020;
DECLARE @Phone VARCHAR(10)=CONCAT('09',RIGHT(CONCAT('00000000',ABS(CONVERT(BIGINT,CHECKSUM(NEWID())))%100000000),8));
DECLARE @CCCD VARCHAR(12)=CONVERT(VARCHAR(12),900000000000+ABS(CONVERT(BIGINT,CHECKSUM(NEWID())))%99999999999);
BEGIN TRY
    BEGIN TRANSACTION;
    WHILE EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE Thang=@Month AND Nam=@Year)
    BEGIN
        SET @Month=@Month-1;
        IF @Month=0 BEGIN SET @Month=12; SET @Year=@Year+1; END;
        IF @Year>YEAR(GETDATE())-1 THROW 53508,N'No past QA payroll period available.',1;
    END;
    INSERT dbo.PHONGBAN(TenPB) VALUES(CONCAT(N'TV5 money ',@Token));
    SET @PB=SCOPE_IDENTITY();
    INSERT dbo.CHUCVU(TenCV) VALUES(CONCAT(N'TV5 money ',@Token));
    SET @CV=SCOPE_IDENTITY();
    INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV)
    VALUES(N'TV5 money QA','19800101',N'Nam',@CCCD,@Phone,CONCAT(@Token,'@example.test'),'20200101',@Max,@PB,@CV);
    SET @NV=SCOPE_IDENTITY();
    INSERT dbo.CHAMCONG(MaNV,NgayChamCong,GioVao,GioRa,TrangThai)
    VALUES(@NV,DATEFROMPARTS(@Year,@Month,1),'08:00','17:00',N'CO_MAT');
    EXEC dbo.sp_TinhBangLuongThang @Month,@Year,1,@Output OUTPUT;
    SET @Payroll=@Output;
    IF NOT EXISTS(SELECT 1 FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Payroll AND MaNV=@NV
        AND TienCong=@Max AND ThucNhan=@Max)
        THROW 53509,N'Large value did not persist through payroll caller.',1;
    SELECT * INTO #TV5_HeaderBefore FROM dbo.BANGLUONG WHERE MaBangLuong=@Payroll;
    SELECT * INTO #TV5_DetailBefore FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Payroll;
    INSERT dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien,NgayGhiNhan)
    VALUES(@NV,@Month,@Year,N'TV5 money overflow',0.01,GETDATE());
    SET @Caught=0;
    SET @Output=NULL;
    BEGIN TRY
        EXEC dbo.sp_TinhBangLuongThang @Month,@Year,1,@Output OUTPUT;
    END TRY BEGIN CATCH
        -- Nested caller rethrows the original message as RAISERROR 50000.
        IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE '%overflow%' THROW;
        SET @Caught=1;
        SELECT 'payroll_overflow' AS CaseName,ERROR_NUMBER() AS ErrorNumber,ERROR_MESSAGE() AS ErrorMessage;
    END CATCH;
    IF @Caught=0 OR @Output IS NOT NULL OR @@TRANCOUNT<>1 OR XACT_STATE()<>1
        THROW 53510,N'Payroll overflow did not preserve caller transaction/output.',1;
    IF EXISTS(SELECT * FROM dbo.BANGLUONG WHERE MaBangLuong=@Payroll EXCEPT SELECT * FROM #TV5_HeaderBefore)
       OR EXISTS(SELECT * FROM #TV5_HeaderBefore EXCEPT SELECT * FROM dbo.BANGLUONG WHERE MaBangLuong=@Payroll)
       OR EXISTS(SELECT * FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Payroll EXCEPT SELECT * FROM #TV5_DetailBefore)
       OR EXISTS(SELECT * FROM #TV5_DetailBefore EXCEPT SELECT * FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong=@Payroll)
        THROW 53511,N'Overflow changed previously stored payroll data.',1;
    SELECT 'payroll_large_value_before_overflow' AS CaseName,* FROM #TV5_DetailBefore;
    ROLLBACK TRANSACTION;
    IF EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE MaNV=@NV)
       OR EXISTS(SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong=@Payroll)
        THROW 53512,N'Money fixture cleanup failed.',1;
    PRINT 'PASS payroll large value, overflow atomic rollback, unchanged full header/detail and fixture cleanup';
END TRY BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
