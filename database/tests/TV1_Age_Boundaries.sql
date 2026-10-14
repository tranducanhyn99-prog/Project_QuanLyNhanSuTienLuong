SET NOCOUNT ON;
IF DB_NAME() NOT LIKE N'PRJ[_]Fix[_]QA[_]%' THROW 53900,N'Use an owned QA database.',1;
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name=N'CHK_NHANVIEN_DoTuoi'
    AND LOWER(definition) LIKE N'%dateadd%') THROW 53901,N'Fresh schema must contain the birthday-aware age constraint.',1;

DECLARE @Cases TABLE(Id INT PRIMARY KEY,Birth DATE,Hire DATE,Allowed BIT);
INSERT @Cases VALUES
 (1,'2008-10-14','2026-10-13',0),(2,'2008-10-13','2026-10-13',1),
 (3,'2008-10-12','2026-10-13',1),(4,'2008-12-31','2026-01-01',0),
 (5,'2008-02-29','2026-02-28',0),(6,'2008-02-29','2026-03-01',1),
 (7,'2006-02-28','2024-02-29',1),(8,'2010-01-01','2026-10-13',0),
 (9,'1990-01-01','2026-10-13',1);
DECLARE @PB INT=(SELECT MIN(MaPB) FROM dbo.PHONGBAN WHERE TrangThai=N'HOAT_DONG'),
 @CV INT=(SELECT MIN(MaCV) FROM dbo.CHUCVU),@Id INT=1,@Birth DATE,@Hire DATE,@Allowed BIT,
 @NV INT,@Rejected BIT,@Email VARCHAR(100),@CCCD VARCHAR(12),@Phone VARCHAR(10),@Login VARCHAR(50),
 @BeforeNV INT=(SELECT COUNT(*) FROM dbo.NHANVIEN),@BeforeTK INT=(SELECT COUNT(*) FROM dbo.TAIKHOAN),
 @BeforeHistory INT=(SELECT COUNT(*) FROM dbo.LICHSULUONG);
IF @PB IS NULL OR @CV IS NULL THROW 53902,N'Employee dictionaries missing.',1;
IF EXISTS(SELECT 1 FROM dbo.TAIKHOAN WHERE TenDangNhap LIKE 'tv1_age_boundary_%')
 OR EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE Email LIKE 'tv1_age_boundary_%')
 THROW 53903,N'Age fixture already exists; refusing overwrite.',1;
BEGIN TRY
 WHILE @Id<=9
 BEGIN
  SELECT @Birth=Birth,@Hire=Hire,@Allowed=Allowed FROM @Cases WHERE Id=@Id;
  SET @Email='tv1_age_boundary_'+CONVERT(VARCHAR(3),@Id)+'@example.invalid';
  SET @CCCD='68000000000'+CONVERT(VARCHAR(1),@Id);SET @Phone='068000000'+CONVERT(VARCHAR(1),@Id);
  SET @Login='tv1_age_boundary_'+CONVERT(VARCHAR(3),@Id);SET @NV=NULL;SET @Rejected=0;
  BEGIN TRY
   INSERT dbo.NHANVIEN(HoTen,NgaySinh,GioiTinh,CCCD,SoDienThoai,Email,NgayVaoLam,LuongCoBan,MaPB,MaCV)
   VALUES(N'TV1 age boundary',@Birth,N'Nam',@CCCD,@Phone,@Email,@Hire,1000000,@PB,@CV);
   SET @NV=SCOPE_IDENTITY();
  END TRY
  BEGIN CATCH
   IF ERROR_NUMBER()<>547 OR ERROR_MESSAGE() NOT LIKE N'%CHK_NHANVIEN_DoTuoi%' THROW;
   SET @Rejected=1;
  END CATCH;
  IF @Rejected=@Allowed THROW 53904,N'Direct INSERT birthday boundary disagrees with expected result.',1;
  IF @NV IS NOT NULL DELETE dbo.NHANVIEN WHERE MaNV=@NV;
  SET @NV=NULL;SET @Rejected=0;
  BEGIN TRY
   EXEC dbo.sp_ThemNhanVien N'TV1 age boundary',@Birth,N'Nam',@CCCD,N'QA',@Phone,@Email,@Hire,
    1000000,@PB,@CV,1,@Login,'pbkdf2-sha256$600000$fixture-only$fixture-only','Employee',@NV OUTPUT;
  END TRY
  BEGIN CATCH
   IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%CHK_NHANVIEN_DoTuoi%' THROW;
   SET @Rejected=1;
  END CATCH;
  IF @Rejected=@Allowed THROW 53905,N'Employee/profile SP birthday boundary disagrees with expected result.',1;
  IF @Allowed=1 AND NOT EXISTS(SELECT 1 FROM dbo.TAIKHOAN WHERE MaNV=@NV AND TenDangNhap=@Login)
   THROW 53906,N'Accepted employee/profile fixture missing.',1;
  IF @Allowed=0 AND (EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE Email=@Email)
   OR EXISTS(SELECT 1 FROM dbo.TAIKHOAN WHERE TenDangNhap=@Login) OR @@TRANCOUNT<>0)
   THROW 53907,N'Rejected age left employee/profile or transaction behind.',1;
  IF @NV IS NOT NULL DELETE dbo.NHANVIEN WHERE MaNV=@NV;
  PRINT CONCAT('PASS age case ',@Id,' birth=',CONVERT(VARCHAR(10),@Birth,23),' hire=',CONVERT(VARCHAR(10),@Hire,23),
   ' allowed=',@Allowed,' direct INSERT + employee/profile SP');
  SET @Id+=1;
 END;
 IF (SELECT COUNT(*) FROM dbo.NHANVIEN)<>@BeforeNV OR (SELECT COUNT(*) FROM dbo.TAIKHOAN)<>@BeforeTK
  OR (SELECT COUNT(*) FROM dbo.LICHSULUONG)<>@BeforeHistory OR @@TRANCOUNT<>0
  THROW 53908,N'Age fixtures changed persistent rows or left transaction.',1;
 PRINT 'PASS 9 age cases / 18 SQL operations; birthdays/leap days match Java Period rule; rejected profile atomic; cleanup unchanged.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK;
 DELETE dbo.NHANVIEN WHERE Email LIKE 'tv1_age_boundary_%';
 THROW;
END CATCH;
