/*
================================================================================
 PEOPLEOS | TV2 - PHAM MINH QUAN | KICH BAN TEST KHI BAO CAO DBMS
 SQL Server / SSMS - DOI CHIEU SOURCE main @ fb9d516 (23/10/2026)

 CAC DOI TUONG TV2 SO HUU / PHOI HOP:
   1. dbo.trg_ChamCong_KiemTraGio        (AFTER INSERT, UPDATE)
   2. dbo.trg_ChamCong_KiemTraNhanVien   (AFTER INSERT, UPDATE)
   3. dbo.sp_GhiNhanChamCong            (TRY/CATCH, THROW, OUTPUT)
   4. dbo.fn_TongPhuCap                 (TV2 phoi hop TV3, module 03)
   5. dbo.vw_TongHopChamCongThang       (bonus)

 KHONG THUOC PHAN CHINH CUA TV2:
   dbo.fn_TinhSoNgayCong duoc giao TV1, du nam trong module 02.
   dbo.trg_ChamCong_KhongSuaKhiDaChotLuong thuoc module 05 (TV5).

 AN TOAN:
 - CHI chay voi DB QA: QuanLyNhanSuTienLuong.
 - Dung SSMS quyen DBA/sysadmin cho demo SQL, KHONG su dung gui_hr
   de UPDATE truc tiep NHANVIEN trong test trigger nhan vien nghi viec.
 - Mo 1 cua so Query trong SSMS, CHAY B0 DAU TIEN de tao #TV2_CTX.
 - Sau do BOI DEN TUNG TEST (ca batch ket thuc bang GO) de chay F5.
 - KHONG chay chung voi app/group dang demo; lock tam thoi co the xuat hien.
 - DML thu nghiem deu duoc ROLLBACK. Trigger co the tu ROLLBACK toan bo
   transaction va hien thong bao DO trong Messages: day la ket qua DUNG
   khi thong bao phu hop voi tinh huong.
 - Test su dung nhan vien QA gui-b@example.invalid, tu tim ngay chua co cong
   va thang chua chot. KHONG sua bang that neu chua doc script.
 - Xoa/rollback van co the lam IDENTITY nhay so; khong dua vao ma lien tiep.
 - Neu bat ky ket qua nao bat thuong, DUNG LAI va kiem tra truoc khi tiep.
================================================================================
*/

-- ============================================================================
-- B0. CHUAN BI MOI TRUONG - CHAY MOT LAN TRONG CUA SO SSMS NAY
-- ============================================================================
USE [QuanLyNhanSuTienLuong];
GO
SET NOCOUNT ON;
SET XACT_ABORT OFF;
IF DB_NAME() <> N'QuanLyNhanSuTienLuong'
    THROW 51001, N'SAI DATABASE! Chi duoc chay tren QA duoc chi dinh.', 1;
IF @@TRANCOUNT <> 0
    THROW 51002, N'Cua so query dang co transaction mo. ROLLBACK/COMMIT theo ngữ canh truoc khi test.', 1;
IF OBJECT_ID(N'dbo.CHAMCONG', N'U') IS NULL
   OR OBJECT_ID(N'dbo.NHANVIEN', N'U') IS NULL
   OR OBJECT_ID(N'dbo.PHUCAPNHANVIEN', N'U') IS NULL
   OR OBJECT_ID(N'dbo.BANGLUONG', N'U') IS NULL
   OR OBJECT_ID(N'dbo.sp_GhiNhanChamCong', N'P') IS NULL
   OR OBJECT_ID(N'dbo.trg_ChamCong_KiemTraGio', N'TR') IS NULL
   OR OBJECT_ID(N'dbo.trg_ChamCong_KiemTraNhanVien', N'TR') IS NULL
   OR OBJECT_ID(N'dbo.fn_TongPhuCap', N'FN') IS NULL
   OR OBJECT_ID(N'dbo.vw_TongHopChamCongThang', N'V') IS NULL
    THROW 51003, N'Thieu mot hoac nhieu doi tuong SQL can test trong database QA.', 1;

IF OBJECT_ID('tempdb..#TV2_CTX') IS NOT NULL DROP TABLE #TV2_CTX;
CREATE TABLE #TV2_CTX
(
    MaNV INT NOT NULL,
    NgayTest DATE NOT NULL,
    ThangTest INT NOT NULL,
    NamTest INT NOT NULL
);

DECLARE @MaNV_B INT, @NgayTest DATE;
SELECT @MaNV_B = MaNV
FROM dbo.NHANVIEN
WHERE Email = 'gui-b@example.invalid' AND TrangThai = N'DANG_LAM_VIEC';
IF @MaNV_B IS NULL
    THROW 51004, N'Khong tim thay nhan vien QA B dang lam viec. Dung test.', 1;

;WITH ds AS
(
    SELECT TOP (180)
        DATEADD(DAY, -CONVERT(INT, ROW_NUMBER() OVER (ORDER BY (SELECT NULL))), CONVERT(DATE, GETDATE())) AS Ngay
    FROM sys.all_objects
)
SELECT TOP (1) @NgayTest = ds.Ngay
FROM ds
JOIN dbo.NHANVIEN nv ON nv.MaNV = @MaNV_B
WHERE ds.Ngay >= nv.NgayVaoLam
  AND NOT EXISTS
      (SELECT 1 FROM dbo.CHAMCONG cc
       WHERE cc.MaNV = @MaNV_B AND cc.NgayChamCong = ds.Ngay)
  AND NOT EXISTS
      (SELECT 1 FROM dbo.BANGLUONG bl
       WHERE bl.Thang = MONTH(ds.Ngay) AND bl.Nam = YEAR(ds.Ngay)
         AND bl.TrangThai = 'DA_CHOT')
ORDER BY ds.Ngay DESC;

IF @NgayTest IS NULL
    THROW 51005, N'Khong tim thay ngay QA chua cham cong trong ky chua chot. Dung test.', 1;
INSERT INTO #TV2_CTX VALUES (@MaNV_B, @NgayTest, MONTH(@NgayTest), YEAR(@NgayTest));
SELECT N'B0 - SAN SANG' AS TinhTrang, DB_NAME() AS DatabaseDangDung,
       MaNV, NgayTest, ThangTest, NamTest
FROM #TV2_CTX;
PRINT N'DA CHUAN BI: Bay gio chon va chay lan luot tung test TC01...';
GO

-- ============================================================================
-- TC01. SP HOP LE: EXEC sp_GhiNhanChamCong -> INSERT -> SCOPE_IDENTITY -> OUTPUT
-- MONG DOI: CO MOT MaChamCong > 0; sau ROLLBACK khong con ban ghi.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51101, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV01 INT = (SELECT MaNV FROM #TV2_CTX),
        @D01 DATE = (SELECT NgayTest FROM #TV2_CTX),
        @ID01 INT = NULL;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV = @NV01, @NgayChamCong = @D01,
        @GioVao = '08:00', @GioRa = '17:00',
        @TrangThai = N'CO_MAT', @GhiChu = N'TV2 TC01 SP hop le',
        @MaChamCong = @ID01 OUTPUT;
    IF @ID01 IS NULL OR NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaChamCong=@ID01)
        THROW 51201, N'SP khong tra OUTPUT hoac khong tao dong.', 1;
    SELECT N'TC01 PASS - SP tao duoc ban ghi (TRUOC ROLLBACK)' AS KetQua,
           @ID01 AS MaChamCongOutput, cc.*
    FROM dbo.CHAMCONG cc WHERE cc.MaChamCong = @ID01;
    ROLLBACK TRANSACTION;
    SELECT N'TC01 SAU ROLLBACK - phai bang 0' AS KiemTra,
           COUNT(*) AS SoDongConLai
    FROM dbo.CHAMCONG WHERE MaNV = @NV01 AND NgayChamCong = @D01;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC01 FAIL - SP hop le nhung bi loi' AS KetQua, ERROR_MESSAGE() AS Loi;
END CATCH;
GO

-- ============================================================================
-- TC02. SP TRUNG NGAY: goi hai lan trong cung transaction
-- MONG DOI: lan 2 bao 'da co ban ghi'; CATCH rollback ca lan 1.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51102, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV02 INT = (SELECT MaNV FROM #TV2_CTX), @D02 DATE = (SELECT NgayTest FROM #TV2_CTX),
        @ID02 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV02, @NgayChamCong=@D02,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @GhiChu=N'TV2 TC02 dong 1', @MaChamCong=@ID02 OUTPUT;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV02, @NgayChamCong=@D02,
         @GioVao='09:00', @GioRa='18:00', @TrangThai=N'CO_MAT',
         @GhiChu=N'TV2 TC02 dong 2', @MaChamCong=@ID02 OUTPUT;
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC02 FAIL - SP khong chan trung ngay' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC02 - SP trung ngay' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
SELECT N'TC02 DOI CHIEU - phai 0' AS KiemTra, COUNT(*) AS SoDongConLai
FROM dbo.CHAMCONG WHERE MaNV=@NV02 AND NgayChamCong=@D02;
GO

-- ============================================================================
-- TC03. SP NGAY TUONG LAI: kiem tra @NgayChamCong > GETDATE()
-- MONG DOI: tu choi truoc khi INSERT.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51103, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV03 INT=(SELECT MaNV FROM #TV2_CTX), @ID03 INT,
        @NgayTuongLai03 DATE = DATEADD(DAY, 1, CONVERT(DATE, GETDATE()));
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV03,
         @NgayChamCong=@NgayTuongLai03,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @MaChamCong=@ID03 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC03 FAIL - SP cho phep ngay tuong lai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC03 - SP ngay tuong lai' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO

-- ============================================================================
-- TC04. SP GIO RA KHONG HOP LE: SP tu kiem tra TRUOC trigger.
-- MONG DOI: GioRa <= GioVao, thong bao loi tu SP.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51104, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV04 INT=(SELECT MaNV FROM #TV2_CTX), @D04 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID04 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV04, @NgayChamCong=@D04,
         @GioVao='17:00', @GioRa='08:00', @TrangThai=N'CO_MAT',
         @MaChamCong=@ID04 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC04 FAIL - SP nhan gio sai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC04 - SP GioRa <= GioVao' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO

-- ============================================================================
-- TC05. SP TRANG THAI KHONG HOP LE: chi nhan 4 gia tri.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51105, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV05 INT=(SELECT MaNV FROM #TV2_CTX), @D05 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID05 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV05, @NgayChamCong=@D05,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'NGHI_PHEP',
         @MaChamCong=@ID05 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC05 FAIL - SP nhan trang thai la' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC05 - SP trang thai khong hop le' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO

-- ============================================================================
-- TC06. SP NHAN VIEN KHONG TON TAI: validation o SP (khong den FK).
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51106, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @D06 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID06 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=99999999, @NgayChamCong=@D06,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @MaChamCong=@ID06 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC06 FAIL - SP cho nhan vien khong ton tai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC06 - SP nhan vien khong ton tai' AS KetQua, ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO

-- ============================================================================
-- TC07. SP NHAN VIEN NGHI VIEC: UPDATE tam trang thai NV B trong TRANSACTION.
-- MONG DOI: SP tu choi, sau ROLLBACK NV B van DANG_LAM_VIEC.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51107, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV07 INT=(SELECT MaNV FROM #TV2_CTX), @D07 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID07 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    UPDATE dbo.NHANVIEN SET TrangThai=N'NGHI_VIEC' WHERE MaNV=@NV07;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV07, @NgayChamCong=@D07,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @MaChamCong=@ID07 OUTPUT;
    ROLLBACK TRANSACTION;
    SELECT N'TC07 FAIL - SP cho NV nghi viec' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC07 - SP nhan vien nghi viec' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
SELECT N'TC07 KIEM TRA NV SAU ROLLBACK' AS KiemTra, MaNV, TrangThai
FROM dbo.NHANVIEN WHERE MaNV=@NV07;
GO

-- ============================================================================
-- TC08. TRIGGER GIO - AFTER INSERT, BYPASS SP de chung minh trigger bao ve DB.
-- MONG DOI: Messages hien 'Gio ra ve phai lon hon gio vao lam';
--           trigger ROLLBACK TRANSACTION -> khong co dong duoc luu.
-- LUU Y: SQL Server co the them Msg 3609 do ROLLBACK o trigger.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51108, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV08 INT=(SELECT MaNV FROM #TV2_CTX), @D08 DATE=(SELECT NgayTest FROM #TV2_CTX);
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.CHAMCONG(MaNV,NgayChamCong,GioVao,GioRa,TrangThai,GhiChu)
    VALUES (@NV08,@D08,'17:00','08:00',N'CO_MAT',N'TV2 TC08 trigger INSERT gio');
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC08 FAIL - trigger khong chan gio sai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC08 - KIEM TRA Messages: loi GioRa <= GioVao la mong doi' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
-- TC08 DOI CHIEU SAU KHI TRIGGER CO THE DA HUY BATCH TRUOC
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51180, N'Chay B0 truoc.', 1;
SELECT N'TC08 DOI CHIEU: 0 DONG' AS KiemTra, COUNT(*) AS SoDongConLai
FROM dbo.CHAMCONG cc CROSS JOIN #TV2_CTX c
WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest;
GO

-- ============================================================================
-- TC09. TRIGGER GIO - AFTER UPDATE (seed dong hop le qua SP, UPDATE gio sai).
-- MONG DOI: trigger chan va rollback LUON ca ban ghi seed trong transaction.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51109, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV09 INT=(SELECT MaNV FROM #TV2_CTX), @D09 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID09 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV09, @NgayChamCong=@D09,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @GhiChu=N'TV2 TC09 seed', @MaChamCong=@ID09 OUTPUT;
    UPDATE dbo.CHAMCONG SET GioRa='07:30' WHERE MaChamCong=@ID09;
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC09 FAIL - trigger khong chan UPDATE gio sai' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC09 - KIEM TRA Messages: loi gio khi UPDATE la mong doi' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
-- TC09 DOI CHIEU
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51190, N'Chay B0 truoc.', 1;
SELECT N'TC09 DOI CHIEU: 0 DONG' AS KiemTra, COUNT(*) AS SoDongConLai
FROM dbo.CHAMCONG cc CROSS JOIN #TV2_CTX c
WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest;
GO

-- ============================================================================
-- TC10. TRIGGER NHAN VIEN - AFTER INSERT.
-- Gia lap NV B NGHI_VIEC ben trong TRANSACTION, chen THANG (bypass SP).
-- MONG DOI: trigger tu choi, ROLLBACK ca thay doi trang thai NV.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51110, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV10 INT=(SELECT MaNV FROM #TV2_CTX), @D10 DATE=(SELECT NgayTest FROM #TV2_CTX);
BEGIN TRY
    BEGIN TRANSACTION;
    UPDATE dbo.NHANVIEN SET TrangThai=N'NGHI_VIEC' WHERE MaNV=@NV10;
    INSERT INTO dbo.CHAMCONG(MaNV,NgayChamCong,GioVao,GioRa,TrangThai,GhiChu)
    VALUES (@NV10,@D10,'08:00','17:00',N'CO_MAT',N'TV2 TC10 trigger NV');
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC10 FAIL - trigger cho NV nghi viec' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC10 - KIEM TRA Messages: khong duoc cham cong NV nghi viec' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
-- TC10 DOI CHIEU: NV B PHAI DANG LAM VIEC, CHAMCONG PHAI 0
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51111, N'Chay B0 truoc.', 1;
SELECT N'TC10 DOI CHIEU' AS KiemTra, n.MaNV, n.TrangThai,
       (SELECT COUNT(*) FROM dbo.CHAMCONG cc CROSS JOIN #TV2_CTX c
        WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest) AS SoDongTest
FROM dbo.NHANVIEN n JOIN #TV2_CTX c ON n.MaNV=c.MaNV;
GO

-- ============================================================================
-- TC11. TRIGGER NHAN VIEN - AFTER UPDATE.
-- Seed chấm công hop le, chuyen trang thai NV sang NGHI_VIEC trong transaction,
-- roi UPDATE GhiChu tren dong chấm công de kich hoat trigger.
-- MONG DOI: trigger tu choi; rollback ca seed va cap nhat trang thai.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51112, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV11 INT=(SELECT MaNV FROM #TV2_CTX), @D11 DATE=(SELECT NgayTest FROM #TV2_CTX), @ID11 INT;
BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.sp_GhiNhanChamCong @MaNV=@NV11, @NgayChamCong=@D11,
         @GioVao='08:00', @GioRa='17:00', @TrangThai=N'CO_MAT',
         @GhiChu=N'TV2 TC11 seed', @MaChamCong=@ID11 OUTPUT;
    UPDATE dbo.NHANVIEN SET TrangThai=N'NGHI_VIEC' WHERE MaNV=@NV11;
    UPDATE dbo.CHAMCONG SET GhiChu=N'TV2 TC11 force UPDATE' WHERE MaChamCong=@ID11;
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC11 FAIL - trigger cho UPDATE cong cua NV nghi viec' AS KetQua;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC11 - KIEM TRA Messages: khong duoc cham cong NV nghi viec' AS KetQua,
           ERROR_MESSAGE() AS ThongBao;
END CATCH;
GO
-- TC11 DOI CHIEU
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51113, N'Chay B0 truoc.', 1;
SELECT N'TC11 DOI CHIEU' AS KiemTra, n.MaNV, n.TrangThai,
       (SELECT COUNT(*) FROM dbo.CHAMCONG cc CROSS JOIN #TV2_CTX c
        WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest) AS SoDongTest
FROM dbo.NHANVIEN n JOIN #TV2_CTX c ON n.MaNV=c.MaNV;
GO

-- ============================================================================
-- TC12. FUNCTION fn_TongPhuCap - DOC KHONG GHI, SO SANH KET QUA VOI SUM
-- FN la ham DUOC GOI khi SELECT hoac tu SP tinh luong, khong phai TRIGGER.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51114, N'Chay B0 truoc va dam bao dung QA.', 1;
SELECT N'TC12 - HAM TONG PHU CAP (DU LIEU HIEN CO)' AS KiemTra,
       c.MaNV, c.ThangTest AS Thang, c.NamTest AS Nam,
       dbo.fn_TongPhuCap(c.MaNV,c.ThangTest,c.NamTest) AS KetQuaFunction,
       (SELECT ISNULL(SUM(pc.SoTien),0)
        FROM dbo.PHUCAPNHANVIEN pc
        WHERE pc.MaNV=c.MaNV AND pc.Thang=c.ThangTest AND pc.Nam=c.NamTest) AS KetQuaSUM
FROM #TV2_CTX c;
SELECT N'TC12 - NHAN VIEN KHONG CO PHU CAP' AS KiemTra,
       dbo.fn_TongPhuCap(99999999,1,2020) AS MongDoiBang0;
GO

-- ============================================================================
-- TC13. FUNCTION fn_TongPhuCap - THEM 2 PHU CAP TAM TRONG TRANSACTION
-- MONG DOI: KetQuaSau - KetQuaTruoc = 300000, SAU ROLLBACK tro ve ban dau.
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51115, N'Chay B0 truoc va dam bao dung QA.', 1;
DECLARE @NV13 INT=(SELECT MaNV FROM #TV2_CTX),
        @Thang13 INT=(SELECT ThangTest FROM #TV2_CTX),
        @Nam13 INT=(SELECT NamTest FROM #TV2_CTX),
        @Truoc13 DECIMAL(18,2), @Sau13 DECIMAL(18,2);
SET @Truoc13=dbo.fn_TongPhuCap(@NV13,@Thang13,@Nam13);
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT INTO dbo.PHUCAPNHANVIEN(MaNV,Thang,Nam,TenPhuCap,SoTien,GhiChu)
    VALUES (@NV13,@Thang13,@Nam13,N'TV2 Test PC 1',100000,N'TC13 rollback'),
           (@NV13,@Thang13,@Nam13,N'TV2 Test PC 2',200000,N'TC13 rollback');
    SET @Sau13=dbo.fn_TongPhuCap(@NV13,@Thang13,@Nam13);
    SELECT N'TC13 - DELTA PHAI BANG 300000' AS KiemTra,
           @Truoc13 AS TongTruoc, @Sau13 AS TongTrongTransaction,
           @Sau13-@Truoc13 AS PhuCapMoi;
    ROLLBACK TRANSACTION;
    SELECT N'TC13 - SAU ROLLBACK PHAI BANG TongTruoc' AS KiemTra,
           dbo.fn_TongPhuCap(@NV13,@Thang13,@Nam13) AS TongSauRollback,
           @Truoc13 AS TongTruoc;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    SELECT N'TC13 FAIL / BI CHAN BOI OBJECT KHAC' AS KetQua, ERROR_MESSAGE() AS Loi;
END CATCH;
GO

-- ============================================================================
-- TC14. BONUS - VIEW vw_TongHopChamCongThang (SELECT KHONG THAY DOI DU LIEU)
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51116, N'Chay B0 truoc va dam bao dung QA.', 1;
SELECT N'TC14 - VIEW TONG HOP THANG (NEU CHUA CO CONG SE 0 DONG)' AS KiemTra,
       v.*
FROM dbo.vw_TongHopChamCongThang v
JOIN #TV2_CTX c ON v.MaNV=c.MaNV AND v.Thang=c.ThangTest AND v.Nam=c.NamTest;
GO

-- ============================================================================
-- TC15. BONUS - XEM DINH NGHIA INDEX TV2 (CHỈ ĐỌC METADATA)
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51117, N'Chay B0 truoc va dam bao dung QA.', 1;
SELECT N'TC15 - COVERING INDEX TV2' AS KiemTra,
       i.name AS TenIndex, i.type_desc,
       ic.key_ordinal AS ViTriKhoa,
       col.name AS TenCot,
       ic.is_included_column AS LaCotINCLUDE
FROM sys.indexes i
JOIN sys.index_columns ic ON ic.object_id=i.object_id AND ic.index_id=i.index_id
JOIN sys.columns col ON col.object_id=ic.object_id AND col.column_id=ic.column_id
WHERE i.object_id=OBJECT_ID(N'dbo.CHAMCONG')
  AND i.name=N'IX_CHAMCONG_MaNV_Ngay'
ORDER BY ic.key_ordinal, ic.index_column_id;
GO

-- ============================================================================
-- TC16. KIEM TRA CUOI - PHAI KHONG CO DU LIEU TV2 TEST BI LUU LAI
-- ============================================================================
IF DB_NAME() <> N'QuanLyNhanSuTienLuong' OR OBJECT_ID('tempdb..#TV2_CTX') IS NULL
    THROW 51118, N'Chay B0 truoc va dam bao dung QA.', 1;
SELECT N'TC16 - CHECK CUOI' AS KiemTra,
       c.MaNV, c.NgayTest,
       n.TrangThai AS TrangThaiNhanVien,
       (SELECT COUNT(*) FROM dbo.CHAMCONG cc
        WHERE cc.MaNV=c.MaNV AND cc.NgayChamCong=c.NgayTest) AS BanGhiChamCongConLai,
       @@TRANCOUNT AS SoTransactionDangMo
FROM #TV2_CTX c
JOIN dbo.NHANVIEN n ON n.MaNV=c.MaNV;
-- Mong doi: TrangThaiNhanVien=DANG_LAM_VIEC, BanGhiChamCongConLai=0,
--          SoTransactionDangMo=0.
GO
