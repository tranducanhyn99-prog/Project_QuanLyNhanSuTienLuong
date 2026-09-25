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

    IF EXISTS (SELECT 1 FROM deleted WHERE TrangThai = 'DA_CHOT')
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
    SET XACT_ABORT ON;

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

    BEGIN TRY
        IF (@Thang IS NULL OR @Thang NOT BETWEEN 1 AND 12)
            RAISERROR(N'Thang tinh luong phai nam trong khoang 1 den 12.', 16, 1);

        IF (@Nam IS NULL OR @Nam NOT BETWEEN 2020 AND 2100)
            RAISERROR(N'Nam tinh luong phai nam trong khoang 2020 den 2100.', 16, 1);

        IF (@NgayCongChuan IS NULL OR @NgayCongChuan <= 0)
            RAISERROR(N'So ngay cong chuan phai lon hon 0.', 16, 1);

        IF OBJECT_ID(N'dbo.CHAMCONG', N'U') IS NULL
            RAISERROR(N'Chua co bang CHAMCONG. Can tich hop du lieu cham cong truoc khi tinh luong.', 16, 1);

        EXEC sys.sp_executesql
            N'SELECT @SoDongChamCongOut = COUNT(1)
              FROM dbo.CHAMCONG
              WHERE MONTH(NgayChamCong) = @ThangIn
                AND YEAR(NgayChamCong) = @NamIn;',
            N'@ThangIn INT, @NamIn INT, @SoDongChamCongOut INT OUTPUT',
            @ThangIn = @Thang,
            @NamIn = @Nam,
            @SoDongChamCongOut = @SoDongChamCong OUTPUT;

        IF (@SoDongChamCong = 0)
            RAISERROR(N'Chua co du lieu cham cong cho ky luong nay.', 16, 1);

        IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE TrangThai = N'DANG_LAM_VIEC')
            RAISERROR(N'Khong co nhan vien dang lam viec de tinh luong.', 16, 1);

        BEGIN TRANSACTION;

        SELECT @TrangThaiKy = TrangThai
        FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
        WHERE Thang = @Thang AND Nam = @Nam;

        IF (@TrangThaiKy IS NOT NULL)
        BEGIN
            IF (@TrangThaiKy = 'DA_CHOT')
                RAISERROR(N'Ky luong nay da duoc chot. Khong the tinh lai.', 16, 1);
            ELSE
                RAISERROR(N'Ky luong nay da ton tai. Vui long xoa ky chua chot neu muon tinh lai.', 16, 1);
        END;

        INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai)
        VALUES (@Thang, @Nam, @NgayCongChuan, 'CHUA_CHOT');

        SET @MaBangLuong = CONVERT(INT, SCOPE_IDENTITY());

        SET @curNhanVien = CURSOR LOCAL FAST_FORWARD FOR
            SELECT MaNV, LuongCoBan
            FROM dbo.NHANVIEN
            WHERE TrangThai = N'DANG_LAM_VIEC'
            ORDER BY MaNV;

        OPEN @curNhanVien;
        FETCH NEXT FROM @curNhanVien INTO @MaNV, @LuongCoBan;

        WHILE @@FETCH_STATUS = 0
        BEGIN
            SET @NgayCongThucTe = 0;
            SET @TongPhuCap = 0;
            SET @TongKhauTru = 0;

            IF OBJECT_ID(N'dbo.fn_TinhSoNgayCong', N'FN') IS NOT NULL
            BEGIN
                EXEC sys.sp_executesql
                    N'SELECT @NgayCongOut = CONVERT(INT, dbo.fn_TinhSoNgayCong(@MaNVIn, @ThangIn, @NamIn));',
                    N'@MaNVIn INT, @ThangIn INT, @NamIn INT, @NgayCongOut INT OUTPUT',
                    @MaNVIn = @MaNV,
                    @ThangIn = @Thang,
                    @NamIn = @Nam,
                    @NgayCongOut = @NgayCongThucTe OUTPUT;
            END
            ELSE
            BEGIN
                EXEC sys.sp_executesql
                    N'SELECT @NgayCongOut = COUNT(1)
                      FROM dbo.CHAMCONG
                      WHERE MaNV = @MaNVIn
                        AND MONTH(NgayChamCong) = @ThangIn
                        AND YEAR(NgayChamCong) = @NamIn
                        AND TrangThai IN (N''CO_MAT'', N''DI_TRE'', N''VE_SOM'');',
                    N'@MaNVIn INT, @ThangIn INT, @NamIn INT, @NgayCongOut INT OUTPUT',
                    @MaNVIn = @MaNV,
                    @ThangIn = @Thang,
                    @NamIn = @Nam,
                    @NgayCongOut = @NgayCongThucTe OUTPUT;
            END;

            SET @TienCong = dbo.fn_TinhTienCong(@LuongCoBan, @NgayCongChuan, @NgayCongThucTe);

            IF OBJECT_ID(N'dbo.fn_TongPhuCap', N'FN') IS NOT NULL
            BEGIN
                EXEC sys.sp_executesql
                    N'SELECT @TongPhuCapOut = CONVERT(DECIMAL(18,2), dbo.fn_TongPhuCap(@MaNVIn, @ThangIn, @NamIn));',
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
                    N'SELECT @TongKhauTruOut = CONVERT(DECIMAL(18,2), dbo.fn_TongKhauTru(@MaNVIn, @ThangIn, @NamIn));',
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
                    N'SELECT @ThucNhanOut = CONVERT(DECIMAL(18,2), dbo.fn_TinhThucNhan(@TienCongIn, @TongPhuCapIn, @TongKhauTruIn));',
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

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF CURSOR_STATUS('variable', '@curNhanVien') >= 0
            CLOSE @curNhanVien;

        IF CURSOR_STATUS('variable', '@curNhanVien') > -3
            DEALLOCATE @curNhanVien;

        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrorMessage, 16, 1);
    END CATCH
END;
GO
