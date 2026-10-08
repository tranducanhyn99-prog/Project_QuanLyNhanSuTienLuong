-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương (DBMS330284) - Nhóm 06
-- HỌC PHẦN: Hệ Quản Trị Cơ Sở Dữ Liệu
-- BỘ KIỂM THỬ TỰ ĐỘNG TOÀN DIỆN PHÂN HỆ CHẤM CÔNG (TV2 - TUẦN 3)
-- TÁC GIẢ: TV2 - Phạm Minh Quân (MSSV: 24110311)
-- ĐỐI TƯỢNG SỞ HỮU ĐƯỢC KIỂM THỬ:
--   1. Bảng dữ liệu: dbo.CHAMCONG & các Ràng buộc toàn vẹn (PK, FK, UQ, CHK)
--   2. Stored Procedure: dbo.sp_GhiNhanChamCong (7 tham số, OUTPUT MaChamCong)
--   3. Trigger: dbo.trg_ChamCong_KiemTraGio (AFTER INSERT, UPDATE)
--   4. Trigger: dbo.trg_ChamCong_KiemTraNhanVien (AFTER INSERT, UPDATE)
--   5. View: dbo.vw_TongHopChamCongThang
--   6. Covering Index: dbo.IX_CHAMCONG_MaNV_Ngay
--   7. Transaction: Quản trị Giao dịch Nhập Lô (All-or-Nothing Commit & Rollback)
-- ============================================================================

-- Keep the caller's QA database context.
IF DB_NAME() NOT LIKE 'PRJ[_]Fix[_]QA[_]%' THROW 53101,N'Use a dedicated QA database.',1;
GO

PRINT '============================================================================';
PRINT '  BẮT ĐẦU THỰC THI BỘ TEST CASE TỰ ĐỘNG: MODULE CHẤM CÔNG (TV2 - TUẦN 3)';
PRINT '============================================================================';
GO

-- ============================================================================
-- PHẦN KHỞI TẠO: BẢNG KẾT QUẢ VÀ CONTEXT DỮ LIỆU ĐỘNG (SELF-SEEDING AN TOÀN)
-- Fixture riêng per-run; cleanup ở cuối khi thành công. Dùng run_sql_verification.ps1
-- để database riêng được dọn trong finally cả khi assertion dừng giữa các batch GO.
-- ============================================================================
IF OBJECT_ID('tempdb..#TestSummary') IS NOT NULL
    DROP TABLE #TestSummary;

CREATE TABLE #TestSummary (
    Id          INT IDENTITY(1,1) PRIMARY KEY,
    TestCase    VARCHAR(20)   NOT NULL,
    TestGroup   NVARCHAR(100) NOT NULL,
    Description NVARCHAR(255) NOT NULL,
    Result      VARCHAR(10)   NOT NULL,
    Detail      NVARCHAR(1000) NULL
);
GO

IF OBJECT_ID('tempdb..#TV2_Context') IS NOT NULL
    DROP TABLE #TV2_Context;

CREATE TABLE #TV2_Context (
    KeyName VARCHAR(50) PRIMARY KEY,
    IntVal  INT NULL,
    DateVal DATE NULL
);
GO

-- Thực hiện tự tạo dữ liệu mẫu (Self-seed) độc lập với token per-run
SET NOCOUNT ON;
BEGIN
    -- Sinh token số ngẫu nhiên 6 chữ số (100000 - 999999) cho phiên chạy hiện tại
    DECLARE @Suffix INT = ABS(CHECKSUM(NEWID())) % 900000 + 100000;
    DECLARE @SuffixStr VARCHAR(6) = CAST(@Suffix AS VARCHAR(6));

    -- Kiểm tra phòng ban hoạt động; nếu chưa có thì tạo mới và đánh dấu để dọn dẹp
    DECLARE @MaPB_Seed INT, @CreatedPB INT = NULL;
    SELECT TOP 1 @MaPB_Seed = MaPB FROM dbo.PHONGBAN WHERE TrangThai = N'HOAT_DONG';
    IF @MaPB_Seed IS NULL
    BEGIN
        INSERT INTO dbo.PHONGBAN (TenPB, SoDienThoai, TrangThai)
        VALUES (N'PB Test TV2 ' + @SuffixStr, '0289' + @SuffixStr, N'HOAT_DONG');
        SET @MaPB_Seed = SCOPE_IDENTITY();
        SET @CreatedPB = @MaPB_Seed;
    END

    -- Kiểm tra chức vụ; nếu chưa có thì tạo mới và đánh dấu để dọn dẹp
    DECLARE @MaCV_Seed INT, @CreatedCV INT = NULL;
    SELECT TOP 1 @MaCV_Seed = MaCV FROM dbo.CHUCVU;
    IF @MaCV_Seed IS NULL
    BEGIN
        INSERT INTO dbo.CHUCVU (TenCV, PhuCapChucVu)
        VALUES (N'CV Test TV2 ' + @SuffixStr, 0);
        SET @MaCV_Seed = SCOPE_IDENTITY();
        SET @CreatedCV = @MaCV_Seed;
    END

    -- 1. Nhân viên hoạt động 1 (MaNV_Active1) - CCCD 12 số, SDT 10 số bắt đầu bằng 0
    DECLARE @MaNV_Active1 INT;
    DECLARE @CCCD1 VARCHAR(12) = '88' + @SuffixStr + '0001';
    DECLARE @SDT1 VARCHAR(15) = '08' + @SuffixStr + '01';
    DECLARE @Email1 VARCHAR(100) = 'tv2_act1_' + @SuffixStr + '@test.internal';

    INSERT INTO dbo.NHANVIEN (
        HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai,
        Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai
    )
    VALUES (
        N'TV2 Test NV1 ' + @SuffixStr, '1995-01-01', N'Nam', @CCCD1,
        N'TP. Hồ Chí Minh', @SDT1, @Email1,
        '2022-01-01', 12000000, @MaPB_Seed, @MaCV_Seed, N'DANG_LAM_VIEC'
    );
    SET @MaNV_Active1 = SCOPE_IDENTITY();

    -- 2. Nhân viên hoạt động 2 (MaNV_Active2) phục vụ test nhập lô
    DECLARE @MaNV_Active2 INT;
    DECLARE @CCCD2 VARCHAR(12) = '88' + @SuffixStr + '0002';
    DECLARE @SDT2 VARCHAR(15) = '08' + @SuffixStr + '02';
    DECLARE @Email2 VARCHAR(100) = 'tv2_act2_' + @SuffixStr + '@test.internal';

    INSERT INTO dbo.NHANVIEN (
        HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai,
        Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai
    )
    VALUES (
        N'TV2 Test NV2 ' + @SuffixStr, '1996-02-02', N'Nữ', @CCCD2,
        N'TP. Hồ Chí Minh', @SDT2, @Email2,
        '2022-02-01', 13000000, @MaPB_Seed, @MaCV_Seed, N'DANG_LAM_VIEC'
    );
    SET @MaNV_Active2 = SCOPE_IDENTITY();

    -- 3. Nhân viên có trạng thái NGHI_VIEC (MaNV_NghiViec) phục vụ test kiểm tra nghiệp vụ
    DECLARE @MaNV_NghiViec INT;
    DECLARE @CCCD3 VARCHAR(12) = '99' + @SuffixStr + '0003';
    DECLARE @SDT3 VARCHAR(15) = '09' + @SuffixStr + '03';
    DECLARE @Email3 VARCHAR(100) = 'tv2_nghi_' + @SuffixStr + '@test.internal';

    INSERT INTO dbo.NHANVIEN (
        HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai,
        Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai
    )
    VALUES (
        N'TV2 Test Nghỉ Việc ' + @SuffixStr, '1990-03-03', N'Nam', @CCCD3,
        N'TP. Hồ Chí Minh', @SDT3, @Email3,
        '2020-01-01', 10000000, @MaPB_Seed, @MaCV_Seed, N'NGHI_VIEC'
    );
    SET @MaNV_NghiViec = SCOPE_IDENTITY();

    -- Các ngày động đảm bảo tính bất biến theo thời gian thực thi (Dynamic Dates)
    DECLARE @D1 DATE = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE);
    DECLARE @D2 DATE = CAST(DATEADD(DAY, -2, GETDATE()) AS DATE);
    DECLARE @D3 DATE = CAST(DATEADD(DAY, -3, GETDATE()) AS DATE);
    DECLARE @D4 DATE = CAST(DATEADD(DAY, -4, GETDATE()) AS DATE);
    DECLARE @D5 DATE = CAST(DATEADD(DAY, -5, GETDATE()) AS DATE);
    DECLARE @DFuture DATE = CAST(DATEADD(DAY, 1, GETDATE()) AS DATE);
    DECLARE @DRollbackA DATE = CAST(DATEADD(DAY, -11, GETDATE()) AS DATE);
    DECLARE @DRollbackB DATE = CAST(DATEADD(DAY, -12, GETDATE()) AS DATE);
    DECLARE @DRollbackC DATE = CAST(DATEADD(DAY, -13, GETDATE()) AS DATE);

    INSERT INTO #TV2_Context (KeyName, IntVal, DateVal) VALUES
        ('MaNV_Active1', @MaNV_Active1, NULL),
        ('MaNV_Active2', @MaNV_Active2, NULL),
        ('MaNV_NghiViec', @MaNV_NghiViec, NULL),
        ('CreatedPB_Id', @CreatedPB, NULL),
        ('CreatedCV_Id', @CreatedCV, NULL),
        ('Date_D1', NULL, @D1),
        ('Date_D2', NULL, @D2),
        ('Date_D3', NULL, @D3),
        ('Date_D4', NULL, @D4),
        ('Date_D5', NULL, @D5),
        ('Date_Future', NULL, @DFuture),
        ('Date_RollbackA', NULL, @DRollbackA),
        ('Date_RollbackB', NULL, @DRollbackB),
        ('Date_RollbackC', NULL, @DRollbackC);

    -- Dọn sạch nếu có bản ghi cũ của chính các nhân viên test vừa sinh
    DELETE FROM dbo.CHAMCONG
    WHERE MaNV IN (@MaNV_Active1, @MaNV_Active2, @MaNV_NghiViec);
END;
PRINT N'-> Khởi tạo Context và Self-seeding hoàn tất an toàn (Token per-run).';
GO

-- ============================================================================
-- NHÓM 1: KIỂM THỬ SCHEMA & CONSTRAINT (TC-CC-01 ĐẾN TC-CC-05)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- TC-CC-01: Chấm công đơn lẻ hợp lệ qua sp_GhiNhanChamCong
-- ----------------------------------------------------------------------------
DECLARE @MaNV INT, @Ngay DATE, @OutId INT;
SELECT @MaNV = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @Ngay = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D1';

BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = @MaNV,
        @NgayChamCong = @Ngay,
        @GioVao       = '08:00:00',
        @GioRa        = '17:00:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'TC-CC-01 Chấm công hợp lệ',
        @MaChamCong   = @OutId OUTPUT;

    IF @OutId IS NOT NULL AND @OutId > 0 AND EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaChamCong = @OutId)
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-01', N'Schema & Stored Procedure', N'Chấm công đơn lẻ hợp lệ thành công qua sp_GhiNhanChamCong', 'PASS',
                N'Sinh MaChamCong=' + CAST(@OutId AS NVARCHAR(10)));
        PRINT N'-> [PASS] TC-CC-01: Ghi nhận chấm công đơn lẻ thành công (MaChamCong=' + CAST(@OutId AS NVARCHAR(10)) + N')';
    END
    ELSE
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-01', N'Schema & Stored Procedure', N'Chấm công đơn lẻ hợp lệ thành công qua sp_GhiNhanChamCong', 'FAIL',
                N'Không sinh MaChamCong OUTPUT');
        PRINT N'-> [FAIL] TC-CC-01: Không sinh MaChamCong OUTPUT hợp lệ!';
    END
END TRY
BEGIN CATCH
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-01', N'Schema & Stored Procedure', N'Chấm công đơn lẻ hợp lệ thành công qua sp_GhiNhanChamCong', 'FAIL', ERROR_MESSAGE());
    PRINT N'-> [FAIL] TC-CC-01: Ngoại lệ không mong muốn: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TC-CC-02: Ngăn chặn trùng lặp bản ghi chấm công trong cùng ngày (UQ_CHAMCONG_MaNV_Ngay)
-- ----------------------------------------------------------------------------
DECLARE @MaNV INT, @Ngay DATE, @OutId INT;
SELECT @MaNV = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @Ngay = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D1';

-- 2.1. Thử gọi lại SP với cùng cặp (MaNV, NgayChamCong)
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = @MaNV,
        @NgayChamCong = @Ngay,
        @GioVao       = '08:30:00',
        @GioRa        = '17:30:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'Cố ý chấm lần 2 cùng ngày',
        @MaChamCong   = @OutId OUTPUT;

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-02.1', N'Constraint & SP Validation', N'Chặn trùng lặp ngày qua SP (sp_GhiNhanChamCong)', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-02.1: SP không chặn ghi nhận trùng lặp trong ngày!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%đã có bản ghi%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-02.1', N'Constraint & SP Validation', N'Chặn trùng lặp ngày qua SP (sp_GhiNhanChamCong)', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-02.1: Đã chặn trùng lặp qua SP thành công: ' + ERROR_MESSAGE();
END CATCH;

-- 2.2. Thử Insert trực tiếp vào bảng để kiểm chứng ràng buộc DB UQ_CHAMCONG_MaNV_Ngay
BEGIN TRY
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)
    VALUES (@MaNV, @Ngay, '08:00:00', '17:00:00', N'CO_MAT');

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-02.2', N'Database Constraint', N'Ràng buộc duy nhất UQ_CHAMCONG_MaNV_Ngay', 'FAIL', N'Không kích hoạt!');
    PRINT N'-> [FAIL] TC-CC-02.2: Ràng buộc UQ_CHAMCONG_MaNV_Ngay không hoạt động!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>2627 OR ERROR_MESSAGE() NOT LIKE N'%UQ_CHAMCONG_MaNV_Ngay%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-02.2', N'Database Constraint', N'Ràng buộc duy nhất UQ_CHAMCONG_MaNV_Ngay', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-02.2: Đã kích hoạt UQ_CHAMCONG_MaNV_Ngay thành công: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TC-CC-03: Chặn nhân viên không tồn tại trong hệ thống (FK_CHAMCONG_NHANVIEN)
-- ----------------------------------------------------------------------------
DECLARE @Ngay DATE, @OutId INT;
DECLARE @FakeMaNV INT = 2147483647;
IF EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE MaNV=@FakeMaNV)
    THROW 53102,N'The missing-employee test id already exists.',1;
SELECT @Ngay = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D2';

-- 3.1. Qua Stored Procedure
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = @FakeMaNV,
        @NgayChamCong = @Ngay,
        @GioVao       = '08:00:00',
        @GioRa        = '17:00:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'Test nhân viên giả lập',
        @MaChamCong   = @OutId OUTPUT;

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-03.1', N'Foreign Key & SP Validation', N'Chặn nhân viên không tồn tại qua SP', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-03.1: SP không chặn nhân viên không tồn tại!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%không tồn tại%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-03.1', N'Foreign Key & SP Validation', N'Chặn nhân viên không tồn tại qua SP', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-03.1: Đã chặn nhân viên không tồn tại qua SP: ' + ERROR_MESSAGE();
END CATCH;

-- 3.2. Qua ràng buộc Khóa ngoại cơ sở dữ liệu FK_CHAMCONG_NHANVIEN
BEGIN TRY
    DECLARE @InvalidFK INT;
    SELECT @InvalidFK = ISNULL(MAX(MaNV), 0) + 9999 FROM dbo.NHANVIEN;

    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)
    VALUES (@InvalidFK, @Ngay, '08:00:00', '17:00:00', N'CO_MAT');

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-03.2', N'Database Constraint', N'Ràng buộc khóa ngoại FK_CHAMCONG_NHANVIEN', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-03.2: Ràng buộc FK_CHAMCONG_NHANVIEN không chặn bản ghi!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>547 OR ERROR_MESSAGE() NOT LIKE N'%FK_CHAMCONG_NHANVIEN%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-03.2', N'Database Constraint', N'Ràng buộc khóa ngoại FK_CHAMCONG_NHANVIEN', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-03.2: Đã kích hoạt FK_CHAMCONG_NHANVIEN chặn thành công: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TC-CC-04: Chặn ngày chấm công trong tương lai (CHK_CHAMCONG_Ngay)
-- ----------------------------------------------------------------------------
DECLARE @MaNV INT, @NgayTuongLai DATE, @OutId INT;
SELECT @MaNV = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @NgayTuongLai = DateVal FROM #TV2_Context WHERE KeyName = 'Date_Future';

-- 4.1. Qua Stored Procedure
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = @MaNV,
        @NgayChamCong = @NgayTuongLai,
        @GioVao       = '08:00:00',
        @GioRa        = '17:00:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'Test ngày tương lai',
        @MaChamCong   = @OutId OUTPUT;

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-04.1', N'Check Constraint & SP Validation', N'Chặn ngày trong tương lai qua SP', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-04.1: SP không chặn ngày chấm công trong tương lai!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%ngày hiện tại%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-04.1', N'Check Constraint & SP Validation', N'Chặn ngày trong tương lai qua SP', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-04.1: SP đã chặn thành công ngày tương lai: ' + ERROR_MESSAGE();
END CATCH;

-- 4.2. Qua Check Constraint CHK_CHAMCONG_Ngay
BEGIN TRY
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)
    VALUES (@MaNV, @NgayTuongLai, '08:00:00', '17:00:00', N'CO_MAT');

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-04.2', N'Database Constraint', N'Ràng buộc miền giá trị CHK_CHAMCONG_Ngay', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-04.2: CHK_CHAMCONG_Ngay không chặn ngày tương lai!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>547 OR ERROR_MESSAGE() NOT LIKE N'%CHK_CHAMCONG_Ngay%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-04.2', N'Database Constraint', N'Ràng buộc miền giá trị CHK_CHAMCONG_Ngay', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-04.2: Đã kích hoạt CHK_CHAMCONG_Ngay chặn thành công: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TC-CC-05: Chặn giá trị trạng thái không nằm trong danh mục (CHK_CHAMCONG_TrangThai)
-- ----------------------------------------------------------------------------
DECLARE @MaNV INT, @Ngay DATE, @OutId INT;
SELECT @MaNV = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @Ngay = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D2';

-- 5.1. Qua Stored Procedure
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = @MaNV,
        @NgayChamCong = @Ngay,
        @GioVao       = '08:00:00',
        @GioRa        = '17:00:00',
        @TrangThai    = N'KHONG_HOP_LE',
        @GhiChu       = N'Test trạng thái sai',
        @MaChamCong   = @OutId OUTPUT;

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-05.1', N'Check Constraint & SP Validation', N'Chặn trạng thái sai qua SP', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-05.1: SP không chặn trạng thái không hợp lệ!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%Trạng thái%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-05.1', N'Check Constraint & SP Validation', N'Chặn trạng thái sai qua SP', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-05.1: SP đã chặn thành công trạng thái sai: ' + ERROR_MESSAGE();
END CATCH;

-- 5.2. Qua Check Constraint CHK_CHAMCONG_TrangThai
BEGIN TRY
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)
    VALUES (@MaNV, @Ngay, '08:00:00', '17:00:00', N'KHONG_HOP_LE');

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-05.2', N'Database Constraint', N'Ràng buộc miền giá trị CHK_CHAMCONG_TrangThai', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-05.2: CHK_CHAMCONG_TrangThai không chặn giá trị sai!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>547 OR ERROR_MESSAGE() NOT LIKE N'%CHK_CHAMCONG_TrangThai%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-05.2', N'Database Constraint', N'Ràng buộc miền giá trị CHK_CHAMCONG_TrangThai', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-05.2: Đã kích hoạt CHK_CHAMCONG_TrangThai chặn thành công: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ============================================================================
-- NHÓM 2: TRIGGER NGHIỆP VỤ DO TV2 SỞ HỮU (TC-CC-06 VÀ TC-CC-07)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- TC-CC-06: Trigger chặn chấm công nhân viên đã nghỉ việc (trg_ChamCong_KiemTraNhanVien)
-- ----------------------------------------------------------------------------
DECLARE @MaNV_NghiViec INT, @Ngay DATE, @OutId INT;
SELECT @MaNV_NghiViec = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_NghiViec';
SELECT @Ngay = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D2';

-- 6.1. Qua Stored Procedure
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = @MaNV_NghiViec,
        @NgayChamCong = @Ngay,
        @GioVao       = '08:00:00',
        @GioRa        = '17:00:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'Test nhân viên đã nghỉ việc qua SP',
        @MaChamCong   = @OutId OUTPUT;

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-06.1', N'Trigger & Business Logic', N'Chặn nhân viên đã nghỉ việc qua SP', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-06.1: SP không chặn nhân viên đã nghỉ việc!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%nghỉ việc%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-06.1', N'Trigger & Business Logic', N'Chặn nhân viên đã nghỉ việc qua SP', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-06.1: SP đã chặn thành công nhân viên đã nghỉ việc: ' + ERROR_MESSAGE();
END CATCH;

-- 6.2. Qua Trigger trực tiếp dbo.trg_ChamCong_KiemTraNhanVien
BEGIN TRY
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)
    VALUES (@MaNV_NghiViec, @Ngay, '08:00:00', '17:00:00', N'CO_MAT');

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-06.2', N'Trigger & Business Logic', N'Trigger trg_ChamCong_KiemTraNhanVien chặn Insert', 'FAIL', N'Trigger không chặn!');
    PRINT N'-> [FAIL] TC-CC-06.2: Trigger trg_ChamCong_KiemTraNhanVien không chặn!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%nghỉ việc%' THROW;
    -- Xác minh thêm rằng Trigger đã ROLLBACK và không lưu bản ghi
    IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV_NghiViec AND NgayChamCong = @Ngay)
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-06.2', N'Trigger & Business Logic', N'Trigger trg_ChamCong_KiemTraNhanVien chặn Insert', 'PASS', ERROR_MESSAGE());
        PRINT N'-> [PASS] TC-CC-06.2: Trigger đã chặn và ROLLBACK thành công: ' + ERROR_MESSAGE();
    END
    ELSE
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-06.2', N'Trigger & Business Logic', N'Trigger trg_ChamCong_KiemTraNhanVien chặn Insert', 'FAIL', N'Bản ghi vẫn tồn tại sau lỗi!');
        PRINT N'-> [FAIL] TC-CC-06.2: Bản ghi vẫn tồn tại sau khi báo lỗi!';
    END
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TC-CC-07: Trigger chặn giờ ra nhỏ hơn hoặc bằng giờ vào làm (trg_ChamCong_KiemTraGio)
-- ----------------------------------------------------------------------------
DECLARE @MaNV INT, @Ngay DATE, @OutId INT;
SELECT @MaNV = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @Ngay = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D2';

-- 7.1. Qua Stored Procedure (GioRa < GioVao)
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = @MaNV,
        @NgayChamCong = @Ngay,
        @GioVao       = '17:00:00',
        @GioRa        = '08:00:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'Test giờ ra < giờ vào qua SP',
        @MaChamCong   = @OutId OUTPUT;

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-07.1', N'Trigger & Business Logic', N'Chặn giờ ra < giờ vào qua SP', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-07.1: SP không chặn giờ ra nhỏ hơn giờ vào!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%Giờ ra%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-07.1', N'Trigger & Business Logic', N'Chặn giờ ra < giờ vào qua SP', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-07.1: SP đã chặn thành công giờ ra nhỏ hơn giờ vào: ' + ERROR_MESSAGE();
END CATCH;

-- 7.2. Qua Trigger trực tiếp dbo.trg_ChamCong_KiemTraGio (GioRa < GioVao)
BEGIN TRY
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)
    VALUES (@MaNV, @Ngay, '17:00:00', '08:00:00', N'CO_MAT');

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-07.2', N'Trigger & Business Logic', N'Trigger trg_ChamCong_KiemTraGio (GioRa < GioVao)', 'FAIL', N'Trigger không chặn!');
    PRINT N'-> [FAIL] TC-CC-07.2: Trigger trg_ChamCong_KiemTraGio không chặn!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%Giờ ra%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-07.2', N'Trigger & Business Logic', N'Trigger trg_ChamCong_KiemTraGio (GioRa < GioVao)', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-07.2: Trigger đã chặn và ROLLBACK thành công (GioRa < GioVao): ' + ERROR_MESSAGE();
END CATCH;

-- 7.3. Qua Trigger trực tiếp dbo.trg_ChamCong_KiemTraGio (GioRa = GioVao)
BEGIN TRY
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)
    VALUES (@MaNV, @Ngay, '08:00:00', '08:00:00', N'CO_MAT');

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-07.3', N'Trigger & Business Logic', N'Trigger trg_ChamCong_KiemTraGio (GioRa = GioVao)', 'FAIL', N'Trigger không chặn!');
    PRINT N'-> [FAIL] TC-CC-07.3: Trigger trg_ChamCong_KiemTraGio không chặn khi giờ ra bằng giờ vào!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%Giờ ra%' THROW;
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-07.3', N'Trigger & Business Logic', N'Trigger trg_ChamCong_KiemTraGio (GioRa = GioVao)', 'PASS', ERROR_MESSAGE());
    PRINT N'-> [PASS] TC-CC-07.3: Trigger đã chặn thành công trường hợp GioRa = GioVao: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ============================================================================
-- NHÓM 3: GIAO DỊCH NHẬP LÔ ALL-OR-NOTHING (TC-CC-08 VÀ TC-CC-09)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- TC-CC-08: Nhập lô 5 bản ghi hợp lệ thành công (Transaction Commit)
-- ----------------------------------------------------------------------------
DECLARE @NV1 INT, @NV2 INT;
DECLARE @D2 DATE, @D3 DATE, @D4 DATE, @D5 DATE;
SELECT @NV1 = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @NV2 = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active2';
SELECT @D2  = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D2';
SELECT @D3  = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D3';
SELECT @D4  = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D4';
SELECT @D5  = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D5';

BEGIN TRY
    BEGIN TRANSACTION;

    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@NV1, @D2, '08:00:00', '17:00:00', N'CO_MAT', N'Batch 1');

    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@NV1, @D3, '08:15:00', '17:15:00', N'DI_TRE', N'Batch 2');

    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@NV1, @D4, '07:50:00', '16:30:00', N'VE_SOM', N'Batch 3');

    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@NV2, @D2, '08:00:00', '17:00:00', N'CO_MAT', N'Batch 4');

    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@NV2, @D3, '08:00:00', '17:00:00', N'CO_MAT', N'Batch 5');

    COMMIT TRANSACTION;

    DECLARE @CountBatch8 INT;
    SELECT @CountBatch8 = COUNT(1)
    FROM dbo.CHAMCONG
    WHERE MaNV IN (@NV1, @NV2) AND NgayChamCong IN (@D2, @D3, @D4);

    IF @CountBatch8 = 5
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-08', N'Transaction All-or-Nothing', N'Nhập lô 5 bản ghi hợp lệ thành công (COMMIT)', 'PASS',
                N'Đã commit đầy đủ 5 bản ghi vào CSDL');
        PRINT N'-> [PASS] TC-CC-08: Giao dịch nhập lô 5 dòng hợp lệ đã COMMIT thành công.';
    END
    ELSE
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-08', N'Transaction All-or-Nothing', N'Nhập lô 5 bản ghi hợp lệ thành công (COMMIT)', 'FAIL',
                N'Số dòng thực tế không khớp 5: ' + CAST(@CountBatch8 AS NVARCHAR(10)));
        PRINT N'-> [FAIL] TC-CC-08: Số lượng bản ghi sau commit không khớp 5!';
    END
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-08', N'Transaction All-or-Nothing', N'Nhập lô 5 bản ghi hợp lệ thành công (COMMIT)', 'FAIL', ERROR_MESSAGE());
    PRINT N'-> [FAIL] TC-CC-08: Ngoại lệ trong lô hợp lệ: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TC-CC-09: Rollback toàn bộ lô khi có 1 dòng sai - Xác nhận không lưu dữ liệu dở dang
-- Ràng buộc kiểm tra và dọn dẹp chỉ giới hạn trong 3 nhân viên test và 3 ngày rollback
-- ----------------------------------------------------------------------------
DECLARE @NV1 INT, @NV2 INT, @NV_NghiViec INT;
DECLARE @D_RollbackA DATE, @D_RollbackB DATE, @D_RollbackC DATE;

SELECT @NV1 = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @NV2 = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active2';
SELECT @NV_NghiViec = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_NghiViec';
SELECT @D_RollbackA = DateVal FROM #TV2_Context WHERE KeyName = 'Date_RollbackA';
SELECT @D_RollbackB = DateVal FROM #TV2_Context WHERE KeyName = 'Date_RollbackB';
SELECT @D_RollbackC = DateVal FROM #TV2_Context WHERE KeyName = 'Date_RollbackC';

-- Dọn dẹp an toàn: CHỈ xóa dữ liệu của 3 nhân viên test hiện tại trên 3 ngày rollback này
DELETE FROM dbo.CHAMCONG
WHERE MaNV IN (@NV1, @NV2, @NV_NghiViec)
  AND NgayChamCong IN (@D_RollbackA, @D_RollbackB, @D_RollbackC);

BEGIN TRY
    BEGIN TRANSACTION;

    -- Dòng 1: Hợp lệ (NV1)
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@NV1, @D_RollbackA, '08:00:00', '17:00:00', N'CO_MAT', N'Rollback test - Dong 1 hop le');

    -- Dòng 2: Hợp lệ (NV2)
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@NV2, @D_RollbackB, '08:00:00', '17:00:00', N'CO_MAT', N'Rollback test - Dong 2 hop le');

    -- Dòng 3: CỐ Ý VI PHẠM (Nhân viên đã nghỉ việc -> Trigger sẽ chặn và ném ngoại lệ)
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@NV_NghiViec, @D_RollbackC, '08:00:00', '17:00:00', N'CO_MAT', N'Rollback test - Dong 3 loi');

    -- Nếu dòng 3 không lỗi (không kỳ vọng), commit
    COMMIT TRANSACTION;

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-09', N'Transaction All-or-Nothing', N'Rollback toàn bộ lô khi có 1 dòng vi phạm', 'FAIL', N'Lô vi phạm không bị ném lỗi!');
    PRINT N'-> [FAIL] TC-CC-09: Lô có dòng lỗi nhưng không bị ném ngoại lệ!';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%nghỉ việc%' THROW;
    -- KIỂM CHỨNG TOÀN VẸN: Kiểm tra xem dòng 1 và dòng 2 của nhân viên test có bị lưu dở dang không
    -- Giới hạn chặt chẽ theo đúng MaNV của nhân viên test và ngày test
    DECLARE @ResidualCount INT;
    SELECT @ResidualCount = COUNT(1)
    FROM dbo.CHAMCONG
    WHERE MaNV IN (@NV1, @NV2, @NV_NghiViec)
      AND NgayChamCong IN (@D_RollbackA, @D_RollbackB, @D_RollbackC);

    IF @ResidualCount = 0
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-09', N'Transaction All-or-Nothing', N'Rollback toàn bộ lô khi có 1 dòng vi phạm', 'PASS',
                N'Cả lô bị Rollback 100%, 0 bản ghi dở dang lưu lại. Lỗi bắt được: ' + ERROR_MESSAGE());
        PRINT N'-> [PASS] TC-CC-09: Giao dịch đã ROLLBACK toàn bộ an toàn. Số dòng dở dang tồn đọng = 0.';
    END
    ELSE
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-09', N'Transaction All-or-Nothing', N'Rollback toàn bộ lô khi có 1 dòng vi phạm', 'FAIL',
                N'Cảnh báo: Có ' + CAST(@ResidualCount AS NVARCHAR(10)) + N' bản ghi bị lưu dở dang!');
        PRINT N'-> [FAIL] TC-CC-09: Có dữ liệu dở dang bị lưu lại sau Rollback!';
    END
END CATCH;
GO

-- ============================================================================
-- NHÓM 4: VIEW TỔNG HỢP & HIỆU NĂNG CHỈ MỤC (TC-CC-10 VÀ TC-CC-11)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- TC-CC-10: Kiểm thử View tổng hợp chấm công tháng (vw_TongHopChamCongThang)
-- ----------------------------------------------------------------------------
DECLARE @MaNV INT;
SELECT @MaNV = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';

DECLARE @TestThang INT, @TestNam INT;
SELECT TOP 1 @TestThang = MONTH(NgayChamCong), @TestNam = YEAR(NgayChamCong)
FROM dbo.CHAMCONG
WHERE MaNV = @MaNV;

IF @TestThang IS NOT NULL
BEGIN
    DECLARE @View_SoNgayDiLam INT, @View_SoLanDiTre INT, @View_SoLanVeSom INT, @View_TongGioLam DECIMAL(10,2);
    SELECT
        @View_SoNgayDiLam = SoNgayDiLam,
        @View_SoLanDiTre  = SoLanDiTre,
        @View_SoLanVeSom  = SoLanVeSom,
        @View_TongGioLam  = CAST(TongSoGioLam AS DECIMAL(10,2))
    FROM dbo.vw_TongHopChamCongThang
    WHERE MaNV = @MaNV AND Thang = @TestThang AND Nam = @TestNam;

    DECLARE @Raw_SoNgayDiLam INT, @Raw_SoLanDiTre INT, @Raw_SoLanVeSom INT, @Raw_TongGioLam DECIMAL(10,2);
    SELECT
        @Raw_SoNgayDiLam = COUNT(CASE WHEN TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM') THEN 1 END),
        @Raw_SoLanDiTre  = COUNT(CASE WHEN TrangThai = N'DI_TRE' THEN 1 END),
        @Raw_SoLanVeSom  = COUNT(CASE WHEN TrangThai = N'VE_SOM' THEN 1 END),
        @Raw_TongGioLam  = CAST(SUM(CASE WHEN GioRa IS NOT NULL AND GioRa > GioVao THEN DATEDIFF(MINUTE, GioVao, GioRa) ELSE 0 END) / 60.0 AS DECIMAL(10,2))
    FROM dbo.CHAMCONG
    WHERE MaNV = @MaNV AND MONTH(NgayChamCong) = @TestThang AND YEAR(NgayChamCong) = @TestNam;

    IF @View_SoNgayDiLam = @Raw_SoNgayDiLam
       AND @View_SoLanDiTre = @Raw_SoLanDiTre
       AND @View_SoLanVeSom = @Raw_SoLanVeSom
       AND @View_TongGioLam = @Raw_TongGioLam
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-10', N'View vw_TongHopChamCongThang', N'Tính toán tổng hợp công tháng khớp 100% dữ liệu gốc', 'PASS',
                N'SoNgayDiLam=' + CAST(@View_SoNgayDiLam AS NVARCHAR(10)) + N', TongGioLam=' + CAST(@View_TongGioLam AS NVARCHAR(10)));
        PRINT N'-> [PASS] TC-CC-10: View vw_TongHopChamCongThang tính toán chính xác 100% dữ liệu chi tiết.';
    END
    ELSE
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-10', N'View vw_TongHopChamCongThang', N'Tính toán tổng hợp công tháng khớp 100% dữ liệu gốc', 'FAIL',
                N'Lệch số liệu giữa View và bảng gốc');
        PRINT N'-> [FAIL] TC-CC-10: Số liệu từ View không khớp với bảng gốc!';
    END
END
ELSE
BEGIN
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-10', N'View vw_TongHopChamCongThang', N'Tính toán tổng hợp công tháng khớp 100% dữ liệu gốc', 'FAIL', N'Không tìm thấy dữ liệu để đối soát');
    PRINT N'-> [FAIL] TC-CC-10: Chưa có dữ liệu chấm công để kiểm thử View!';
END;
GO

-- ----------------------------------------------------------------------------
-- TC-CC-11: Kiểm thử Covering Index IX_CHAMCONG_MaNV_Ngay
-- ----------------------------------------------------------------------------
DECLARE @IndexExists BIT = 0;
DECLARE @HasIncludeColumns BIT = 0;

IF EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.CHAMCONG')
      AND name = N'IX_CHAMCONG_MaNV_Ngay'
)
BEGIN
    SET @IndexExists = 1;
END;

IF (
    SELECT COUNT(1)
    FROM sys.index_columns ic
    JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
    WHERE ic.object_id = OBJECT_ID(N'dbo.CHAMCONG')
      AND ic.index_id = INDEXPROPERTY(OBJECT_ID(N'dbo.CHAMCONG'), N'IX_CHAMCONG_MaNV_Ngay', 'IndexID')
      AND ic.is_included_column = 1
      AND c.name IN (N'GioVao', N'GioRa', N'TrangThai')
) = 3
BEGIN
    SET @HasIncludeColumns = 1;
END;

DECLARE @MaNV INT, @D1 DATE;
SELECT @MaNV = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @D1 = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D1';

BEGIN TRY
    SELECT MaNV, NgayChamCong, GioVao, GioRa, TrangThai
    FROM dbo.CHAMCONG WITH (INDEX(IX_CHAMCONG_MaNV_Ngay))
    WHERE MaNV = @MaNV AND NgayChamCong = @D1;

    IF @IndexExists = 1 AND @HasIncludeColumns = 1
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-11', N'Covering Index Structure', N'Chỉ mục IX_CHAMCONG_MaNV_Ngay có cấu trúc INCLUDE và truy vấn được', 'PASS',
                N'INCLUDE: GioVao, GioRa, TrangThai; execution plan/performance checked separately');
        PRINT N'-> [PASS] TC-CC-11: Covering Index IX_CHAMCONG_MaNV_Ngay hoạt động chuẩn xác.';
    END
    ELSE
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-11', N'Covering Index Performance', N'Chỉ mục IX_CHAMCONG_MaNV_Ngay bao phủ đầy đủ và Seek thành công', 'FAIL',
                N'Thiếu cấu trúc Covering INCLUDE');
        PRINT N'-> [FAIL] TC-CC-11: Chỉ mục thiếu các cột INCLUDE theo thiết kế!';
    END
END TRY
BEGIN CATCH
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-11', N'Covering Index Performance', N'Chỉ mục IX_CHAMCONG_MaNV_Ngay bao phủ đầy đủ và Seek thành công', 'FAIL', ERROR_MESSAGE());
    PRINT N'-> [FAIL] TC-CC-11: Lỗi khi truy vấn với gợi ý chỉ mục: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ============================================================================
-- NHÓM 5: CÁC KỊCH BẢN MỞ RỘNG CÓ Ý NGHĨA KỸ THUẬT (TC-CC-12, TC-CC-13, TC-CC-14)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- TC-CC-12: Trigger trg_ChamCong_KiemTraGio kiểm soát khi UPDATE bản ghi
-- ----------------------------------------------------------------------------
DECLARE @MaNV INT, @D1 DATE;
SELECT @MaNV = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @D1 = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D1';

DECLARE @MaCC_Update INT;
SELECT @MaCC_Update = MaChamCong FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = @D1;

BEGIN TRY
    -- Cố tình cập nhật giờ ra sớm hơn giờ vào (GioVao='08:00:00', cố tình set GioRa='07:00:00')
    UPDATE dbo.CHAMCONG
    SET GioRa = '07:00:00'
    WHERE MaChamCong = @MaCC_Update;

    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-12', N'Trigger Update Validation', N'Trigger trg_ChamCong_KiemTraGio chặn UPDATE giờ sai', 'FAIL', N'Không bị chặn!');
    PRINT N'-> [FAIL] TC-CC-12: Trigger không chặn UPDATE giờ ra < giờ vào!';
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%Giờ ra%' THROW;
    -- Xác minh giá trị gốc không bị thay đổi
    DECLARE @GioRaSauLoi TIME(0);
    SELECT @GioRaSauLoi = GioRa FROM dbo.CHAMCONG WHERE MaChamCong = @MaCC_Update;

    IF @GioRaSauLoi = '17:00:00'
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-12', N'Trigger Update Validation', N'Trigger trg_ChamCong_KiemTraGio chặn UPDATE giờ sai', 'PASS',
                N'Trigger chặn UPDATE và Rollback an toàn: ' + ERROR_MESSAGE());
        PRINT N'-> [PASS] TC-CC-12: Trigger đã chặn và Rollback thành công lệnh UPDATE không hợp lệ.';
    END
    ELSE
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-12', N'Trigger Update Validation', N'Trigger trg_ChamCong_KiemTraGio chặn UPDATE giờ sai', 'FAIL',
                N'Dữ liệu bị biến đổi sau lỗi UPDATE!');
        PRINT N'-> [FAIL] TC-CC-12: Dữ liệu bị thay đổi dù có lỗi!';
    END
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TC-CC-13: Kiểm tra tham số bắt buộc trong sp_GhiNhanChamCong (NULL Parameter Validation)
-- ----------------------------------------------------------------------------
DECLARE @MaNV INT, @D5 DATE, @OutId INT;
SELECT @MaNV = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @D5 = DateVal FROM #TV2_Context WHERE KeyName = 'Date_D5';

DECLARE @NullParamPassed BIT = 1;

-- 13.1. MaNV NULL
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong @MaNV = NULL, @NgayChamCong = @D5, @GioVao = '08:00:00', @GioRa = '17:00:00', @TrangThai = N'CO_MAT', @MaChamCong = @OutId OUTPUT;
    SET @NullParamPassed = 0;
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%không%' THROW;
END CATCH;

-- 13.2. NgayChamCong NULL
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong @MaNV = @MaNV, @NgayChamCong = NULL, @GioVao = '08:00:00', @GioRa = '17:00:00', @TrangThai = N'CO_MAT', @MaChamCong = @OutId OUTPUT;
    SET @NullParamPassed = 0;
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%không%' THROW;
END CATCH;

-- 13.3. GioVao NULL
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong @MaNV = @MaNV, @NgayChamCong = @D5, @GioVao = NULL, @GioRa = '17:00:00', @TrangThai = N'CO_MAT', @MaChamCong = @OutId OUTPUT;
    SET @NullParamPassed = 0;
END TRY
BEGIN CATCH
    IF ERROR_NUMBER()<>50000 OR ERROR_MESSAGE() NOT LIKE N'%không%' THROW;
END CATCH;

IF @NullParamPassed = 1
BEGIN
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-13', N'Stored Procedure Validation', N'Kiểm tra chặn tham số bắt buộc NULL trong sp_GhiNhanChamCong', 'PASS',
            N'Đã chặn thành công tất cả trường hợp thiếu tham số bắt buộc');
    PRINT N'-> [PASS] TC-CC-13: sp_GhiNhanChamCong validate chặt chẽ các tham số bắt buộc không được NULL.';
END
ELSE
BEGIN
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-13', N'Stored Procedure Validation', N'Kiểm tra chặn tham số bắt buộc NULL trong sp_GhiNhanChamCong', 'FAIL',
            N'Có tham số bắt buộc NULL không bị chặn');
    PRINT N'-> [FAIL] TC-CC-13: SP không chặn đầy đủ tham số NULL!';
END;
GO

-- ----------------------------------------------------------------------------
-- TC-CC-14: Dọn dẹp dữ liệu kiểm thử an toàn & Bảo toàn cơ sở dữ liệu (Cleanup & Isolation)
-- Xóa toàn bộ dữ liệu chấm công và nhân viên được sinh trong phiên chạy hiện tại
-- ----------------------------------------------------------------------------
DECLARE @NV1 INT, @NV2 INT, @NV_NghiViec INT;
DECLARE @CreatedPB INT, @CreatedCV INT;
SELECT @NV1 = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active1';
SELECT @NV2 = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_Active2';
SELECT @NV_NghiViec = IntVal FROM #TV2_Context WHERE KeyName = 'MaNV_NghiViec';
SELECT @CreatedPB = IntVal FROM #TV2_Context WHERE KeyName = 'CreatedPB_Id';
SELECT @CreatedCV = IntVal FROM #TV2_Context WHERE KeyName = 'CreatedCV_Id';

BEGIN TRY
    -- 1. Xóa toàn bộ dữ liệu chấm công của các nhân viên test phiên này
    DELETE FROM dbo.CHAMCONG WHERE MaNV IN (@NV1, @NV2, @NV_NghiViec);

    -- 2. Xóa các nhân viên test đã sinh trong phiên chạy này (không để lại rác trong NHANVIEN)
    DELETE FROM dbo.NHANVIEN WHERE MaNV IN (@NV1, @NV2, @NV_NghiViec);

    -- 3. Xóa phòng ban / chức vụ fallback nếu được tạo bởi phiên này
    IF @CreatedPB IS NOT NULL
        DELETE FROM dbo.PHONGBAN WHERE MaPB = @CreatedPB;

    IF @CreatedCV IS NOT NULL
        DELETE FROM dbo.CHUCVU WHERE MaCV = @CreatedCV;

    -- Kiểm tra xác nhận không còn dữ liệu tồn đọng
    DECLARE @RemainCC INT, @RemainNV INT;
    SELECT @RemainCC = COUNT(1) FROM dbo.CHAMCONG WHERE MaNV IN (@NV1, @NV2, @NV_NghiViec);
    SELECT @RemainNV = COUNT(1) FROM dbo.NHANVIEN WHERE MaNV IN (@NV1, @NV2, @NV_NghiViec);

    IF @RemainCC = 0 AND @RemainNV = 0
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-14', N'Cleanup & Isolation', N'Dọn dẹp sạch toàn bộ dữ liệu chấm công và nhân viên test', 'PASS',
                N'0 bản ghi rác tồn đọng (CHAMCONG=0, NHANVIEN=0)');
        PRINT N'-> [PASS] TC-CC-14: Đã dọn dẹp sạch sẽ toàn bộ chấm công và nhân viên test, CSDL được bảo toàn 100%.';
    END
    ELSE
    BEGIN
        INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
        VALUES ('TC-CC-14', N'Cleanup & Isolation', N'Dọn dẹp sạch toàn bộ dữ liệu chấm công và nhân viên test', 'FAIL',
                N'Còn tồn đọng CHAMCONG=' + CAST(@RemainCC AS NVARCHAR(10)) + N', NHANVIEN=' + CAST(@RemainNV AS NVARCHAR(10)));
        PRINT N'-> [FAIL] TC-CC-14: Còn tồn đọng dữ liệu test sau khi dọn dẹp!';
    END
END TRY
BEGIN CATCH
    INSERT INTO #TestSummary (TestCase, TestGroup, Description, Result, Detail)
    VALUES ('TC-CC-14', N'Cleanup & Isolation', N'Dọn dẹp sạch toàn bộ dữ liệu chấm công và nhân viên test', 'FAIL', ERROR_MESSAGE());
    PRINT N'-> [FAIL] TC-CC-14: Lỗi khi dọn dẹp: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ============================================================================
-- PHẦN TỔNG KẾT: XUẤT BẢNG BÁO CÁO KẾT QUẢ KIỂM THỬ
-- ============================================================================
PRINT '';
PRINT '============================================================================';
PRINT '             BẢNG TỔNG HỢP KẾT QUẢ KIỂM THỬ PHÂN HỆ CHẤM CÔNG (TV2)';
PRINT '============================================================================';

SELECT
    TestCase AS [Mã Test],
    TestGroup AS [Phân Loại],
    Description AS [Mô Tả Kịch Bản],
    Result AS [Kết Quả],
    Detail AS [Chi Tiết]
FROM #TestSummary
ORDER BY Id ASC;

DECLARE @TotalTests INT, @PassedTests INT, @FailedTests INT;
SELECT
    @TotalTests  = COUNT(1),
    @PassedTests = SUM(CASE WHEN Result = 'PASS' THEN 1 ELSE 0 END),
    @FailedTests = SUM(CASE WHEN Result = 'FAIL' THEN 1 ELSE 0 END)
FROM #TestSummary;

PRINT '----------------------------------------------------------------------------';
PRINT '  TỔNG SỐ KỊCH BẢN THỰC THI : ' + CAST(@TotalTests AS VARCHAR(10));
PRINT '  SỐ KỊCH BẢN ĐẠT (PASS)    : ' + CAST(@PassedTests AS VARCHAR(10));
PRINT '  SỐ KỊCH BẢN LỖI (FAIL)    : ' + CAST(@FailedTests AS VARCHAR(10));
PRINT '  TỶ LỆ THÀNH CÔNG          : ' + CAST(CAST((@PassedTests * 100.0 / @TotalTests) AS DECIMAL(5,2)) AS VARCHAR(10)) + '%';
PRINT '============================================================================';

IF @FailedTests>0 THROW 53100,N'Attendance test contains failed assertions.',1;

-- Dọn dẹp các bảng tạm trong session
DROP TABLE #TestSummary;
DROP TABLE #TV2_Context;
GO
