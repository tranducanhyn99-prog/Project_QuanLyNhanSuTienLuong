-- Run after 01 -> 05, in a dedicated QA database as the DBA.
-- WITHOUT LOGIN tests grants only; real personal identities are tested separately.
SET NOCOUNT ON;
IF DB_NAME() NOT LIKE 'PRJ[_]Fix[_]QA[_]%' THROW 53200,N'Use the dedicated QA database.',1;
IF USER_ID('qaFix_Admin') IS NOT NULL OR USER_ID('qaFix_HR') IS NOT NULL
 OR USER_ID('qaFix_Payroll') IS NOT NULL OR USER_ID('qaFix_Employee') IS NOT NULL
    THROW 53201,N'Test principals already exist; do not overwrite them.',1;
DECLARE @Impersonating BIT=0,@Passed INT=0;
BEGIN TRY
    CREATE USER qaFix_Admin WITHOUT LOGIN; ALTER ROLE role_DBAdmin ADD MEMBER qaFix_Admin;
    CREATE USER qaFix_HR WITHOUT LOGIN; ALTER ROLE role_HRManager ADD MEMBER qaFix_HR;
    CREATE USER qaFix_Payroll WITHOUT LOGIN; ALTER ROLE role_PayrollOfficer ADD MEMBER qaFix_Payroll;
    CREATE USER qaFix_Employee WITHOUT LOGIN; ALTER ROLE role_Employee ADD MEMBER qaFix_Employee;
    DECLARE @Cases TABLE(Id INT IDENTITY,UserName SYSNAME,ObjectName SYSNAME,PermissionName VARCHAR(20),Expected INT);
    INSERT @Cases VALUES
    ('qaFix_Admin','dbo.TAIKHOAN','SELECT',1),
    ('qaFix_HR','dbo.sp_ThemNhanVien','EXECUTE',1),
    ('qaFix_HR','dbo.NHANVIEN','DELETE',1),
    ('qaFix_HR','dbo.CHAMCONG','UPDATE',1),
    ('qaFix_HR','dbo.sp_ChotBangLuong','EXECUTE',0),
    ('qaFix_HR','dbo.sp_TinhBangLuongThang','EXECUTE',0),
    ('qaFix_HR','dbo.sp_XoaKyLuongChuaChot','EXECUTE',0),
    ('qaFix_HR','dbo.TAIKHOAN','UPDATE',0),
    ('qaFix_Payroll','dbo.sp_TinhBangLuongThang','EXECUTE',1),
    ('qaFix_Payroll','dbo.sp_ChotBangLuong','EXECUTE',1),
    ('qaFix_Payroll','dbo.sp_HuyChotBangLuong','EXECUTE',1),
    ('qaFix_Payroll','dbo.sp_XoaBangLuongChuaChot','EXECUTE',1),
    ('qaFix_Payroll','dbo.sp_XoaKyLuongChuaChot','EXECUTE',1),
    ('qaFix_Payroll','dbo.KHAUTRUNHANVIEN','DELETE',1),
    ('qaFix_Payroll','dbo.fn_TongPhuCap','EXECUTE',1),
    ('qaFix_Payroll','dbo.NHANVIEN','UPDATE',0),
    ('qaFix_Employee','dbo.vw_PhieuLuongCaNhan','SELECT',1),
    ('qaFix_Employee','dbo.vw_BangLuongChiTiet','SELECT',0),
    ('qaFix_Employee','dbo.NHANVIEN','SELECT',0),
    ('qaFix_Employee','dbo.TAIKHOAN','SELECT',0),
    ('qaFix_Employee','dbo.sp_LayTaiKhoanHienTai','EXECUTE',1),
    ('qaFix_Employee','dbo.sp_TinhBangLuongThang','EXECUTE',0);
    DECLARE @Id INT=1,@User SYSNAME,@Object SYSNAME,@Permission VARCHAR(20),@Expected INT,@Actual INT,@Sql NVARCHAR(MAX);
    WHILE @Id<=(SELECT COUNT(*) FROM @Cases)
    BEGIN
        SELECT @User=UserName,@Object=ObjectName,@Permission=PermissionName,@Expected=Expected FROM @Cases WHERE Id=@Id;
        SET @Sql=N'EXECUTE AS USER='+QUOTENAME(@User,CHAR(39))+N'; SELECT @Actual=HAS_PERMS_BY_NAME(@Object,N''OBJECT'',@Permission); REVERT;';
        EXEC sys.sp_executesql @Sql,N'@Object SYSNAME,@Permission VARCHAR(20),@Actual INT OUTPUT',@Object,@Permission,@Actual OUTPUT;
        IF ISNULL(@Actual,-1)<>@Expected THROW 53202,N'Permission matrix mismatch.',1;
        SET @Passed+=1; SET @Id+=1;
    END;
    DECLARE @Rejected BIT=0,@NewId INT;
    EXECUTE AS USER='qaFix_HR'; SET @Impersonating=1;
    BEGIN TRY EXEC dbo.sp_ChotBangLuong @MaBangLuong=-1; END TRY
    BEGIN CATCH IF ERROR_NUMBER()<>229 THROW; SET @Rejected=1; END CATCH;
    IF @Rejected<>1 THROW 53203,N'HR unexpectedly closed payroll.',1;
    SET @Rejected=0;
    BEGIN TRY
        EXEC dbo.sp_ThemNhanVien N'Forbidden', '1990-01-01', N'Nam', '999999999999', NULL, '0999999999',
            'forbidden@example.invalid','2020-01-01',26000000,0,0,1,'qa_forbidden','invalid','DB_Admin',@NewId OUTPUT;
    END TRY
    BEGIN CATCH
        IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%HR chỉ được%' THROW;
        SET @Rejected=1;
    END CATCH;
    REVERT; SET @Impersonating=0;
    IF @Rejected<>1 OR EXISTS(SELECT 1 FROM dbo.TAIKHOAN WHERE TenDangNhap='qa_forbidden')
        THROW 53204,N'HR role escalation was accepted.',1;
    EXECUTE AS USER='qaFix_Employee'; SET @Impersonating=1; SET @Rejected=0;
    BEGIN TRY SELECT TOP(1) MaNV FROM dbo.vw_BangLuongChiTiet; END TRY
    BEGIN CATCH IF ERROR_NUMBER()<>229 THROW; SET @Rejected=1; END CATCH;
    REVERT; SET @Impersonating=0;
    IF @Rejected<>1 THROW 53205,N'Employee read the global payroll view.',1;
    DROP USER qaFix_Admin; DROP USER qaFix_HR; DROP USER qaFix_Payroll; DROP USER qaFix_Employee;
    SELECT 'PASS' AS Result,@Passed AS PermissionChecks,3 AS ActualDeniedOperations,'PASS' AS Cleanup;
END TRY
BEGIN CATCH
    IF @Impersonating=1 REVERT;
    IF USER_ID('qaFix_Admin') IS NOT NULL DROP USER qaFix_Admin;
    IF USER_ID('qaFix_HR') IS NOT NULL DROP USER qaFix_HR;
    IF USER_ID('qaFix_Payroll') IS NOT NULL DROP USER qaFix_Payroll;
    IF USER_ID('qaFix_Employee') IS NOT NULL DROP USER qaFix_Employee;
    THROW;
END CATCH;
