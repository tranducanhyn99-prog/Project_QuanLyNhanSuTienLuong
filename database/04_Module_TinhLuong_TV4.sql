SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- ============================================================================
-- PROJECT: Quan Ly Nhan Su va Tien Luong
-- HOC PHAN: He Quan Tri Co So Du Lieu (DBMS330284)
-- TAC GIA: Nguyen Quang Vinh (TV4 - MSSV: 24110385)
-- MODULE: Tinh bang luong (DDL, Function, View, Index, Trigger, Stored Procedure, Transaction)
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

-- ============================================================================
-- PHAN 1: TAO BANG VA RANG BUOC CHO MODULE TINH LUONG
-- ============================================================================

IF OBJECT_ID(N'dbo.BANGLUONG', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.BANGLUONG (
        MaBangLuong   INT IDENTITY(1,1) NOT NULL,
        Thang         INT NOT NULL,
        Nam           INT NOT NULL,
        NgayCongChuan INT NOT NULL CONSTRAINT DF_BANGLUONG_NgayCongChuan DEFAULT 26,
        TrangThai     VARCHAR(15) NOT NULL CONSTRAINT DF_BANGLUONG_TrangThai DEFAULT 'CHUA_CHOT',
        NgayTao       DATETIME NOT NULL CONSTRAINT DF_BANGLUONG_NgayTao DEFAULT GETDATE(),
        NgayChot      DATETIME NULL,

        CONSTRAINT PK_BANGLUONG PRIMARY KEY CLUSTERED (MaBangLuong),
        CONSTRAINT UQ_BANGLUONG_ThangNam UNIQUE (Thang, Nam),
        CONSTRAINT CHK_BANGLUONG_Thang CHECK (Thang BETWEEN 1 AND 12),
        CONSTRAINT CHK_BANGLUONG_Nam CHECK (Nam BETWEEN 2020 AND 2100),
        CONSTRAINT CHK_BANGLUONG_NgayCongChuan CHECK (NgayCongChuan > 0),
        CONSTRAINT CHK_BANGLUONG_TrangThai CHECK (TrangThai IN ('CHUA_CHOT', 'DA_CHOT'))
    );
END;
GO

IF OBJECT_ID(N'dbo.CHITIETBANGLUONG', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.CHITIETBANGLUONG (
        MaChiTiet       INT IDENTITY(1,1) NOT NULL,
        MaBangLuong     INT NOT NULL,
        MaNV            INT NOT NULL,
        LuongCoBan      DECIMAL(18,2) NOT NULL,
        NgayCongThucTe  INT NOT NULL CONSTRAINT DF_CTBL_NgayCongThucTe DEFAULT 0,
        TienCong        DECIMAL(18,2) NOT NULL CONSTRAINT DF_CTBL_TienCong DEFAULT 0,
        TongPhuCap      DECIMAL(18,2) NOT NULL CONSTRAINT DF_CTBL_TongPhuCap DEFAULT 0,
        TongKhauTru     DECIMAL(18,2) NOT NULL CONSTRAINT DF_CTBL_TongKhauTru DEFAULT 0,
        ThucNhan        DECIMAL(18,2) NOT NULL CONSTRAINT DF_CTBL_ThucNhan DEFAULT 0,

        CONSTRAINT PK_CHITIETBANGLUONG PRIMARY KEY CLUSTERED (MaChiTiet),
        CONSTRAINT FK_CTBL_BANGLUONG FOREIGN KEY (MaBangLuong)
            REFERENCES dbo.BANGLUONG(MaBangLuong),
        CONSTRAINT FK_CTBL_NHANVIEN FOREIGN KEY (MaNV)
            REFERENCES dbo.NHANVIEN(MaNV),
        CONSTRAINT UQ_CTBL_BangLuong_NhanVien UNIQUE (MaBangLuong, MaNV),
        CONSTRAINT CHK_CTBL_LuongCoBan CHECK (LuongCoBan >= 0),
        CONSTRAINT CHK_CTBL_NgayCongThucTe CHECK (NgayCongThucTe >= 0),
        CONSTRAINT CHK_CTBL_TienCong CHECK (TienCong >= 0),
        CONSTRAINT CHK_CTBL_TongPhuCap CHECK (TongPhuCap >= 0),
        CONSTRAINT CHK_CTBL_TongKhauTru CHECK (TongKhauTru >= 0)
    );
END;
GO

-- ============================================================================
-- PHAN 2: FUNCTION THEO PHAN CONG TV4 - fn_TinhTienCong
-- ============================================================================

CREATE OR ALTER FUNCTION dbo.fn_TinhTienCong
(
    @LuongCoBan      DECIMAL(18,2),
    @NgayCongChuan   INT,
    @NgayCongThucTe  INT
)
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @KetQua DECIMAL(18,2) = 0;

    IF (@LuongCoBan IS NULL OR @LuongCoBan <= 0)
        RETURN 0;

    IF (@NgayCongChuan IS NULL OR @NgayCongChuan <= 0)
        RETURN 0;

    IF (@NgayCongThucTe IS NULL OR @NgayCongThucTe <= 0)
        RETURN 0;

    SET @KetQua = ROUND((@LuongCoBan / CAST(@NgayCongChuan AS DECIMAL(18,2))) * @NgayCongThucTe, 2);
    RETURN @KetQua;
END;
GO

-- ============================================================================
-- PHAN 3: VIEW THEO PHAN CONG TV4 - vw_TongKhauTruThang
-- ============================================================================

IF OBJECT_ID(N'dbo.KHAUTRUNHANVIEN', N'U') IS NOT NULL
   AND OBJECT_ID(N'dbo.NHANVIEN', N'U') IS NOT NULL
BEGIN
    EXEC(N'
        CREATE OR ALTER VIEW dbo.vw_TongKhauTruThang
        AS
        SELECT
            ktn.MaNV,
            nv.HoTen,
            ktn.Thang,
            ktn.Nam,
            CAST(SUM(ktn.SoTien) AS DECIMAL(18,2)) AS TongKhauTru
        FROM dbo.KHAUTRUNHANVIEN ktn
        INNER JOIN dbo.NHANVIEN nv ON ktn.MaNV = nv.MaNV
        GROUP BY ktn.MaNV, nv.HoTen, ktn.Thang, ktn.Nam;
    ');
END
ELSE
BEGIN
    PRINT N'Bo qua view vw_TongKhauTruThang vi chua co bang KHAUTRUNHANVIEN hoac NHANVIEN.';
END;
GO

-- ============================================================================
-- PHAN 4: INDEX THEO PHAN CONG TV4 - IX_KHAUTRU_MaNV_ThangNam
-- ============================================================================

IF OBJECT_ID(N'dbo.KHAUTRUNHANVIEN', N'U') IS NOT NULL
   AND NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = N'IX_KHAUTRU_MaNV_ThangNam'
          AND object_id = OBJECT_ID(N'dbo.KHAUTRUNHANVIEN')
   )
BEGIN
    CREATE NONCLUSTERED INDEX IX_KHAUTRU_MaNV_ThangNam
    ON dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam)
    INCLUDE (SoTien);
END;
GO

-- ============================================================================
-- PHAN 5: TRIGGER THEO PHAN CONG TV4 - trg_BangLuong_KhongSuaKhiDaChot
-- ============================================================================

CREATE OR ALTER TRIGGER dbo.trg_BangLuong_KhongSuaKhiDaChot
ON dbo.BANGLUONG
INSTEAD OF UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Ky da chot la bat bien. Ngoai le duy nhat la thao tac mo lai co kiem soat:
    -- DA_CHOT -> CHUA_CHOT, xoa NgayChot va khong thay doi bat ky du lieu nao khac.
    -- Quyen UPDATE truc tiep tren bang khong duoc cap cho Payroll Officer; thao tac nay
    -- duoc thuc hien qua dbo.sp_HuyChotBangLuong.
    IF EXISTS (
        SELECT 1
        FROM deleted d
        LEFT JOIN inserted i ON i.MaBangLuong = d.MaBangLuong
        WHERE d.TrangThai = 'DA_CHOT'
          AND (
                i.MaBangLuong IS NULL
                OR i.TrangThai <> 'CHUA_CHOT'
                OR i.NgayChot IS NOT NULL
                OR i.Thang <> d.Thang
                OR i.Nam <> d.Nam
                OR i.NgayCongChuan <> d.NgayCongChuan
                OR i.NgayTao <> d.NgayTao
          )
    )
    BEGIN
        RAISERROR(N'Khong duoc sua hoac xoa bang luong da chot.', 16, 1);
        RETURN;
    END;

    IF EXISTS (SELECT 1 FROM inserted)
    BEGIN
        UPDATE bl
        SET
            bl.Thang = i.Thang,
            bl.Nam = i.Nam,
            bl.NgayCongChuan = i.NgayCongChuan,
            bl.TrangThai = i.TrangThai,
            bl.NgayTao = i.NgayTao,
            bl.NgayChot = i.NgayChot
        FROM dbo.BANGLUONG bl
        INNER JOIN inserted i ON bl.MaBangLuong = i.MaBangLuong;

        RETURN;
    END;

    DELETE bl
    FROM dbo.BANGLUONG bl
    INNER JOIN deleted d ON bl.MaBangLuong = d.MaBangLuong;
END;
GO

-- ============================================================================
-- PHAN 6: STORED PROCEDURE THEO PHAN CONG TV4 - sp_TinhBangLuongThang
-- Co TRY/CATCH va transaction rollback toan bo neu loi insert chi tiet.
-- ============================================================================

CREATE OR ALTER PROCEDURE dbo.sp_TinhBangLuongThang
    @Thang         INT,
    @Nam           INT,
    @NgayCongChuan INT = 26,
    @MaBangLuong   INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    SET @MaBangLuong = NULL;

    DECLARE @TrangThaiKy VARCHAR(15);
    DECLARE @SoDongChamCong INT = 0;
    DECLARE @MaNV INT;
    DECLARE @LuongCoBan DECIMAL(18,2);
    DECLARE @NgayCongThucTe INT;
    DECLARE @TienCong DECIMAL(18,2);
    DECLARE @TongPhuCap DECIMAL(18,2);
    DECLARE @TongKhauTru DECIMAL(18,2);
    DECLARE @ThucNhan DECIMAL(18,2);
    DECLARE @curNhanVien CURSOR;
    DECLARE @NhanVienNguon TABLE
    (
        MaNV INT NOT NULL PRIMARY KEY,
        LuongCoBan DECIMAL(18,2) NOT NULL
    );
    DECLARE @InitialTranCount INT = @@TRANCOUNT;
    DECLARE @OwnTransaction BIT = 0;
    DECLARE @SavepointCreated BIT = 0;
    DECLARE @SourceLockCount BIGINT = 0;
    DECLARE @CallerXactAbort BIT = CASE WHEN (16384 & @@OPTIONS) = 16384 THEN 1 ELSE 0 END;

    -- Transaction do procedure tu mo dung XACT_ABORT ON. Neu dang tham gia
    -- transaction cua caller, tam tat de loi statement khong lam doom toan bo
    -- transaction va savepoint van co the rollback cuc bo.
    IF @InitialTranCount = 0
        SET XACT_ABORT ON;
    ELSE
        SET XACT_ABORT OFF;

    BEGIN TRY
        IF (@Thang IS NULL OR @Thang NOT BETWEEN 1 AND 12)
            RAISERROR(N'Thang tinh luong phai nam trong khoang 1 den 12.', 16, 1);

        IF (@Nam IS NULL OR @Nam NOT BETWEEN 2020 AND 2100)
            RAISERROR(N'Nam tinh luong phai nam trong khoang 2020 den 2100.', 16, 1);

        IF (@NgayCongChuan IS NULL OR @NgayCongChuan <= 0)
            RAISERROR(N'So ngay cong chuan phai lon hon 0.', 16, 1);

        IF OBJECT_ID(N'dbo.CHAMCONG', N'U') IS NULL
            RAISERROR(N'Chua co bang CHAMCONG. Can tich hop du lieu cham cong truoc khi tinh luong.', 16, 1);

        IF @InitialTranCount = 0
        BEGIN
            BEGIN TRANSACTION;
            SET @OwnTransaction = 1;
        END
        ELSE
        BEGIN
            SAVE TRANSACTION TV4_TinhBangLuong;
            SET @SavepointCreated = 1;
        END;

        SELECT @TrangThaiKy = TrangThai
        FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
        WHERE Thang = @Thang AND Nam = @Nam;

        IF (@TrangThaiKy = 'DA_CHOT')
            RAISERROR(N'Kỳ lương này đã được chốt. Không thể tính lại! Vui lòng mở lại (hủy chốt) bảng lương trước nếu cần điều chỉnh.', 16, 1);

        -- Khoa tap du lieu nguon trong transaction de mot lan tinh luong khong
        -- tron du lieu truoc/sau mot thay doi cham cong, phu cap hoac khau tru.
        EXEC sys.sp_executesql
            N'SELECT @SoDongChamCongOut = COUNT(1)
              FROM dbo.CHAMCONG WITH (HOLDLOCK)
              WHERE NgayChamCong >= DATEFROMPARTS(@NamIn, @ThangIn, 1)
                AND NgayChamCong < DATEADD(MONTH, 1, DATEFROMPARTS(@NamIn, @ThangIn, 1));',
            N'@ThangIn INT, @NamIn INT, @SoDongChamCongOut INT OUTPUT',
            @ThangIn = @Thang,
            @NamIn = @Nam,
            @SoDongChamCongOut = @SoDongChamCong OUTPUT;

        IF (@SoDongChamCong = 0)
            RAISERROR(N'Chua co du lieu cham cong cho ky luong nay.', 16, 1);

        -- Materialize tap nhan vien va luong co ban mot lan trong transaction.
        -- Cursor ben duoi chi doc snapshot nay, khong tron gia tri truoc/sau
        -- mot UPDATE LuongCoBan/TrangThai dong thoi.
        INSERT INTO @NhanVienNguon (MaNV, LuongCoBan)
        SELECT nv.MaNV, COALESCE(CASE WHEN DATEFROMPARTS(@Nam,@Thang,1)<DATEFROMPARTS(YEAR(GETDATE()),MONTH(GETDATE()),1) THEN old.LuongCoBan END,h.LuongCoBan,nv.LuongCoBan)
        FROM dbo.NHANVIEN nv WITH (HOLDLOCK)
        OUTER APPLY (SELECT TOP(1) hl.LuongCoBan FROM dbo.LICHSULUONG hl WITH(HOLDLOCK)
                     WHERE hl.MaNV=nv.MaNV AND hl.TuThang<=DATEFROMPARTS(@Nam,@Thang,1)
                     ORDER BY hl.TuThang DESC) h
        OUTER APPLY (SELECT TOP(1) ct.LuongCoBan FROM dbo.CHITIETBANGLUONG ct
                     JOIN dbo.BANGLUONG bl ON bl.MaBangLuong=ct.MaBangLuong
                     WHERE ct.MaNV=nv.MaNV AND bl.Thang=@Thang AND bl.Nam=@Nam) old
        WHERE nv.NgayVaoLam<DATEADD(MONTH,1,DATEFROMPARTS(@Nam,@Thang,1))
          AND (nv.TrangThai=N'DANG_LAM_VIEC' OR nv.NgayNghiViec>=DATEFROMPARTS(@Nam,@Thang,1)
               OR EXISTS(SELECT 1 FROM dbo.CHAMCONG cc WHERE cc.MaNV=nv.MaNV
                    AND cc.NgayChamCong>=DATEFROMPARTS(@Nam,@Thang,1)
                    AND cc.NgayChamCong<DATEADD(MONTH,1,DATEFROMPARTS(@Nam,@Thang,1)))
               OR EXISTS(SELECT 1 FROM dbo.PHUCAPNHANVIEN pc WHERE pc.MaNV=nv.MaNV AND pc.Thang=@Thang AND pc.Nam=@Nam)
               OR EXISTS(SELECT 1 FROM dbo.KHAUTRUNHANVIEN kt WHERE kt.MaNV=nv.MaNV AND kt.Thang=@Thang AND kt.Nam=@Nam));

        SET @SourceLockCount = @@ROWCOUNT;

        IF @SourceLockCount = 0
            RAISERROR(N'Khong co nhan vien dang lam viec de tinh luong.', 16, 1);

        IF OBJECT_ID(N'dbo.PHUCAPNHANVIEN', N'U') IS NOT NULL
        BEGIN
            EXEC sys.sp_executesql
                N'SELECT @SourceLockCountOut = COUNT_BIG(1)
                  FROM dbo.PHUCAPNHANVIEN WITH (HOLDLOCK)
                  WHERE Thang = @ThangIn AND Nam = @NamIn;',
                N'@ThangIn INT, @NamIn INT, @SourceLockCountOut BIGINT OUTPUT',
                @ThangIn = @Thang,
                @NamIn = @Nam,
                @SourceLockCountOut = @SourceLockCount OUTPUT;
        END;

        IF OBJECT_ID(N'dbo.KHAUTRUNHANVIEN', N'U') IS NOT NULL
        BEGIN
            EXEC sys.sp_executesql
                N'SELECT @SourceLockCountOut = COUNT_BIG(1)
                  FROM dbo.KHAUTRUNHANVIEN WITH (HOLDLOCK)
                  WHERE Thang = @ThangIn AND Nam = @NamIn;',
                N'@ThangIn INT, @NamIn INT, @SourceLockCountOut BIGINT OUTPUT',
                @ThangIn = @Thang,
                @NamIn = @Nam,
                @SourceLockCountOut = @SourceLockCount OUTPUT;
        END;

        IF (@TrangThaiKy IS NOT NULL)
        BEGIN
            -- Bang luong nhap da co: xoa chi tiet cu va nap lai trong cung transaction.
            SELECT @MaBangLuong = MaBangLuong FROM dbo.BANGLUONG WHERE Thang = @Thang AND Nam = @Nam;
            DELETE FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong = @MaBangLuong;
            UPDATE dbo.BANGLUONG
            SET NgayCongChuan = @NgayCongChuan,
                NgayTao = GETDATE()
            WHERE MaBangLuong = @MaBangLuong;
        END
        ELSE
        BEGIN
            INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai)
            VALUES (@Thang, @Nam, @NgayCongChuan, 'CHUA_CHOT');

            SET @MaBangLuong = CONVERT(INT, SCOPE_IDENTITY());
        END;

        SET @curNhanVien = CURSOR LOCAL FAST_FORWARD FOR
            SELECT MaNV, LuongCoBan
            FROM @NhanVienNguon
            ORDER BY MaNV;

        OPEN @curNhanVien;
        FETCH NEXT FROM @curNhanVien INTO @MaNV, @LuongCoBan;

        WHILE @@FETCH_STATUS = 0
        BEGIN
            SET @NgayCongThucTe = 0;
            SET @TongPhuCap = 0;
            SET @TongKhauTru = 0;

            -- Dung truc tiep khoang ngay nua mo de tranh MONTH/YEAR tren cot
            -- va de duong truy van thuc te co the tan dung index cham cong.
            EXEC sys.sp_executesql
                N'SELECT @NgayCongOut = COUNT(1)
                  FROM dbo.CHAMCONG WITH (HOLDLOCK)
                  WHERE MaNV = @MaNVIn
                    AND NgayChamCong >= DATEFROMPARTS(@NamIn, @ThangIn, 1)
                    AND NgayChamCong < DATEADD(MONTH, 1, DATEFROMPARTS(@NamIn, @ThangIn, 1))
                    AND TrangThai IN (N''CO_MAT'', N''DI_TRE'', N''VE_SOM'');',
                N'@MaNVIn INT, @ThangIn INT, @NamIn INT, @NgayCongOut INT OUTPUT',
                @MaNVIn = @MaNV,
                @ThangIn = @Thang,
                @NamIn = @Nam,
                @NgayCongOut = @NgayCongThucTe OUTPUT;

            SET @TienCong = dbo.fn_TinhTienCong(@LuongCoBan, @NgayCongChuan, @NgayCongThucTe);

            IF OBJECT_ID(N'dbo.fn_TongPhuCap', N'FN') IS NOT NULL
            BEGIN
                EXEC sys.sp_executesql
                    N'SELECT @TongPhuCapOut = ISNULL(CONVERT(DECIMAL(18,2), dbo.fn_TongPhuCap(@MaNVIn, @ThangIn, @NamIn)), 0);',
                    N'@MaNVIn INT, @ThangIn INT, @NamIn INT, @TongPhuCapOut DECIMAL(18,2) OUTPUT',
                    @MaNVIn = @MaNV,
                    @ThangIn = @Thang,
                    @NamIn = @Nam,
                    @TongPhuCapOut = @TongPhuCap OUTPUT;
            END
            ELSE IF OBJECT_ID(N'dbo.PHUCAPNHANVIEN', N'U') IS NOT NULL
            BEGIN
                EXEC sys.sp_executesql
                    N'SELECT @TongPhuCapOut = ISNULL(SUM(SoTien), 0)
                      FROM dbo.PHUCAPNHANVIEN
                      WHERE MaNV = @MaNVIn AND Thang = @ThangIn AND Nam = @NamIn;',
                    N'@MaNVIn INT, @ThangIn INT, @NamIn INT, @TongPhuCapOut DECIMAL(18,2) OUTPUT',
                    @MaNVIn = @MaNV,
                    @ThangIn = @Thang,
                    @NamIn = @Nam,
                    @TongPhuCapOut = @TongPhuCap OUTPUT;
            END;

            IF OBJECT_ID(N'dbo.fn_TongKhauTru', N'FN') IS NOT NULL
            BEGIN
                EXEC sys.sp_executesql
                    N'SELECT @TongKhauTruOut = ISNULL(CONVERT(DECIMAL(18,2), dbo.fn_TongKhauTru(@MaNVIn, @ThangIn, @NamIn)), 0);',
                    N'@MaNVIn INT, @ThangIn INT, @NamIn INT, @TongKhauTruOut DECIMAL(18,2) OUTPUT',
                    @MaNVIn = @MaNV,
                    @ThangIn = @Thang,
                    @NamIn = @Nam,
                    @TongKhauTruOut = @TongKhauTru OUTPUT;
            END
            ELSE IF OBJECT_ID(N'dbo.KHAUTRUNHANVIEN', N'U') IS NOT NULL
            BEGIN
                EXEC sys.sp_executesql
                    N'SELECT @TongKhauTruOut = ISNULL(SUM(SoTien), 0)
                      FROM dbo.KHAUTRUNHANVIEN
                      WHERE MaNV = @MaNVIn AND Thang = @ThangIn AND Nam = @NamIn;',
                    N'@MaNVIn INT, @ThangIn INT, @NamIn INT, @TongKhauTruOut DECIMAL(18,2) OUTPUT',
                    @MaNVIn = @MaNV,
                    @ThangIn = @Thang,
                    @NamIn = @Nam,
                    @TongKhauTruOut = @TongKhauTru OUTPUT;
            END;

            IF OBJECT_ID(N'dbo.fn_TinhThucNhan', N'FN') IS NOT NULL
            BEGIN
                EXEC sys.sp_executesql
                    N'SELECT @ThucNhanOut = ISNULL(CONVERT(DECIMAL(18,2), dbo.fn_TinhThucNhan(@TienCongIn, @TongPhuCapIn, @TongKhauTruIn)),
                                                    @TienCongIn + @TongPhuCapIn - @TongKhauTruIn);',
                    N'@TienCongIn DECIMAL(18,2), @TongPhuCapIn DECIMAL(18,2), @TongKhauTruIn DECIMAL(18,2), @ThucNhanOut DECIMAL(18,2) OUTPUT',
                    @TienCongIn = @TienCong,
                    @TongPhuCapIn = @TongPhuCap,
                    @TongKhauTruIn = @TongKhauTru,
                    @ThucNhanOut = @ThucNhan OUTPUT;
            END
            ELSE
            BEGIN
                SET @ThucNhan = @TienCong + @TongPhuCap - @TongKhauTru;
            END;

            INSERT INTO dbo.CHITIETBANGLUONG (
                MaBangLuong,
                MaNV,
                LuongCoBan,
                NgayCongThucTe,
                TienCong,
                TongPhuCap,
                TongKhauTru,
                ThucNhan
            )
            VALUES (
                @MaBangLuong,
                @MaNV,
                @LuongCoBan,
                @NgayCongThucTe,
                @TienCong,
                @TongPhuCap,
                @TongKhauTru,
                @ThucNhan
            );

            FETCH NEXT FROM @curNhanVien INTO @MaNV, @LuongCoBan;
        END;

        CLOSE @curNhanVien;
        DEALLOCATE @curNhanVien;

        IF @OwnTransaction = 1
            COMMIT TRANSACTION;

        IF @CallerXactAbort = 1
            SET XACT_ABORT ON;
        ELSE
            SET XACT_ABORT OFF;
    END TRY
    BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(2048) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();
        DECLARE @CursorStatus INT = CURSOR_STATUS('variable', '@curNhanVien');

        -- Khong tra ve ID cua header da bi rollback hoac cua lan tinh that bai.
        SET @MaBangLuong = NULL;

        IF @CursorStatus IN (0, 1)
            CLOSE @curNhanVien;

        IF @CursorStatus IN (-1, 0, 1)
            DEALLOCATE @curNhanVien;

        IF @OwnTransaction = 1 AND XACT_STATE() <> 0
            ROLLBACK TRANSACTION;
        ELSE IF @SavepointCreated = 1 AND XACT_STATE() = 1
            ROLLBACK TRANSACTION TV4_TinhBangLuong;

        IF @InitialTranCount = 0
        BEGIN
            IF @CallerXactAbort = 1 SET XACT_ABORT ON ELSE SET XACT_ABORT OFF;
            THROW;
        END;

        -- Giu XACT_ABORT OFF trong luc phat lai loi cho caller. SET option ben
        -- trong stored procedure tu khoi phuc khi control tro ve caller.
        RAISERROR(N'%s', @ErrorSeverity, @ErrorState, @ErrorMessage);
        RETURN;
    END CATCH
END;
GO

-- ============================================================================
-- PHAN 7: STORED PROCEDURE XOA BANG LUONG CHUA CHOT (RESET BANG LUONG)
-- ============================================================================
CREATE OR ALTER PROCEDURE dbo.sp_XoaBangLuongChuaChot
    @MaBangLuong INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @TrangThai VARCHAR(15);
    BEGIN TRY
        BEGIN TRANSACTION;
        SELECT @TrangThai = TrangThai
        FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
        WHERE MaBangLuong = @MaBangLuong;

        IF @TrangThai IS NULL
            RAISERROR(N'Không tìm thấy bảng lương với mã %d!', 16, 1, @MaBangLuong);

        IF @TrangThai = 'DA_CHOT'
            RAISERROR(N'Bảng lương đã chốt không thể xóa trực tiếp! Vui lòng mở lại (hủy chốt) trước nếu muốn điều chỉnh.', 16, 1);

        DELETE FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong = @MaBangLuong;
        DELETE FROM dbo.BANGLUONG WHERE MaBangLuong = @MaBangLuong;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        DECLARE @Err NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@Err, 16, 1);
    END CATCH
END;
GO

-- ============================================================================
-- PHAN 8: STORED PROCEDURE HUY CHOT / MO LAI BANG LUONG (REOPEN / UNLOCK)
-- ============================================================================
CREATE OR ALTER PROCEDURE dbo.sp_HuyChotBangLuong
    @MaBangLuong INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @TrangThai VARCHAR(15);
    DECLARE @InitialTranCount INT = @@TRANCOUNT;
    DECLARE @OwnTransaction BIT = 0;
    DECLARE @SavepointCreated BIT = 0;
    DECLARE @CallerXactAbort BIT = CASE WHEN (16384 & @@OPTIONS) = 16384 THEN 1 ELSE 0 END;

    IF @InitialTranCount = 0 SET XACT_ABORT ON ELSE SET XACT_ABORT OFF;

    BEGIN TRY
        IF @InitialTranCount = 0
        BEGIN
            BEGIN TRANSACTION;
            SET @OwnTransaction = 1;
        END
        ELSE
        BEGIN
            SAVE TRANSACTION TV4_HuyChot;
            SET @SavepointCreated = 1;
        END;

        SELECT @TrangThai = TrangThai
        FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
        WHERE MaBangLuong = @MaBangLuong;

        IF @TrangThai IS NULL
            RAISERROR(N'Không tìm thấy bảng lương với mã %d!', 16, 1, @MaBangLuong);

        IF @TrangThai <> 'DA_CHOT'
            RAISERROR(N'Bảng lương chưa chốt, không cần hủy chốt!', 16, 1);

        UPDATE dbo.BANGLUONG
        SET TrangThai = 'CHUA_CHOT',
            NgayChot  = NULL
        WHERE MaBangLuong = @MaBangLuong;

        IF @OwnTransaction = 1
            COMMIT TRANSACTION;

        IF @CallerXactAbort = 1 SET XACT_ABORT ON ELSE SET XACT_ABORT OFF;
    END TRY
    BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(2048) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();

        IF @OwnTransaction = 1 AND XACT_STATE() <> 0
            ROLLBACK TRANSACTION;
        ELSE IF @SavepointCreated = 1 AND XACT_STATE() = 1
            ROLLBACK TRANSACTION TV4_HuyChot;

        IF @InitialTranCount = 0
        BEGIN
            IF @CallerXactAbort = 1 SET XACT_ABORT ON ELSE SET XACT_ABORT OFF;
            THROW;
        END;

        RAISERROR(N'%s', @ErrorSeverity, @ErrorState, @ErrorMessage);
        RETURN;
    END CATCH
END;
GO
