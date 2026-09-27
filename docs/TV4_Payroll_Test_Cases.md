# TV4 - Test Case Transaction Module Tính Lương

**Tác giả:** Nguyễn Quang Vinh (TV4, MSSV 24110385)

**Phạm vi:** Kiểm thử `sp_TinhBangLuongThang`, `fn_TinhTienCong`, trigger `trg_BangLuong_KhongSuaKhiDaChot`, view/index khấu trừ và UI `BangLuongPanel`.

## Điều kiện chuẩn bị

1. Chạy script theo thứ tự: `01_Module_NhanSu_TV1.sql`, script chấm công của TV2, `03_phucap_khautru_TV3.sql`, `04_Module_TinhLuong_TV4.sql`.
2. Có tối thiểu 1 nhân viên `DANG_LAM_VIEC`.
3. Có dữ liệu `CHAMCONG` trong tháng/năm cần tính.

## TC-TV4-01 - Tính lương thành công

```sql
DECLARE @MaBangLuong INT;
EXEC dbo.sp_TinhBangLuongThang
    @Thang = 9,
    @Nam = 2026,
    @NgayCongChuan = 26,
    @MaBangLuong = @MaBangLuong OUTPUT;

SELECT @MaBangLuong AS MaBangLuongMoi;
SELECT * FROM dbo.BANGLUONG WHERE MaBangLuong = @MaBangLuong;
SELECT * FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong = @MaBangLuong;
```

Kết quả mong đợi: thêm 1 dòng `BANGLUONG` trạng thái `CHUA_CHOT`, mỗi nhân viên đang làm có 1 dòng chi tiết, `ThucNhan = TienCong + TongPhuCap - TongKhauTru`.

## TC-TV4-02 - Kỳ lương đã tồn tại

```sql
DECLARE @MaBangLuong INT;
EXEC dbo.sp_TinhBangLuongThang 9, 2026, 26, @MaBangLuong OUTPUT;
```

Kết quả mong đợi: procedure báo lỗi kỳ lương đã tồn tại, số dòng `BANGLUONG` và `CHITIETBANGLUONG` không đổi.

## TC-TV4-03 - Kỳ lương đã chốt

```sql
UPDATE dbo.BANGLUONG
SET TrangThai = 'DA_CHOT', NgayChot = GETDATE()
WHERE Thang = 9 AND Nam = 2026 AND TrangThai = 'CHUA_CHOT';

DECLARE @MaBangLuong INT;
EXEC dbo.sp_TinhBangLuongThang 9, 2026, 26, @MaBangLuong OUTPUT;
```

Kết quả mong đợi: procedure báo lỗi kỳ lương đã chốt và không tính lại.

## TC-TV4-04 - Trigger khóa sửa/xóa kỳ đã chốt

```sql
UPDATE dbo.BANGLUONG
SET NgayCongChuan = 27
WHERE Thang = 9 AND Nam = 2026 AND TrangThai = 'DA_CHOT';

DELETE FROM dbo.BANGLUONG
WHERE Thang = 9 AND Nam = 2026 AND TrangThai = 'DA_CHOT';
```

Kết quả mong đợi: cả 2 lệnh đều bị `trg_BangLuong_KhongSuaKhiDaChot` chặn.

## TC-TV4-05 - Rollback khi lỗi insert chi tiết

Dùng trigger tạm thời để giả lập lỗi giữa transaction, sau đó drop ngay sau khi kiểm thử:

```sql
IF OBJECT_ID('tempdb..#TV4_RowCountBefore') IS NOT NULL
    DROP TABLE #TV4_RowCountBefore;

SELECT
    (SELECT COUNT(*) FROM dbo.BANGLUONG) AS TruocBL,
    (SELECT COUNT(*) FROM dbo.CHITIETBANGLUONG) AS TruocCT
INTO #TV4_RowCountBefore;
GO

CREATE OR ALTER TRIGGER dbo.trg_Test_CTBL_ForceError
ON dbo.CHITIETBANGLUONG
AFTER INSERT
AS
BEGIN
    RAISERROR(N'Giả lập lỗi insert chi tiết lương.', 16, 1);
END;
GO

DECLARE @MaBangLuong INT;
EXEC dbo.sp_TinhBangLuongThang 10, 2026, 26, @MaBangLuong OUTPUT;
GO

DROP TRIGGER dbo.trg_Test_CTBL_ForceError;
GO

SELECT COUNT(*) AS SauBL FROM dbo.BANGLUONG;
SELECT COUNT(*) AS SauCT FROM dbo.CHITIETBANGLUONG;
SELECT * FROM #TV4_RowCountBefore;
```

Kết quả mong đợi: procedure rollback toàn bộ. Số dòng sau khi test bằng số dòng trước khi test.

## TC-TV4-06 - Đối chiếu công thức

```sql
SELECT
    ct.MaNV,
    ct.LuongCoBan,
    bl.NgayCongChuan,
    ct.NgayCongThucTe,
    ct.TienCong,
    dbo.fn_TinhTienCong(ct.LuongCoBan, bl.NgayCongChuan, ct.NgayCongThucTe) AS TienCongTinhLai,
    ct.TongPhuCap,
    ct.TongKhauTru,
    ct.ThucNhan,
    ct.TienCong + ct.TongPhuCap - ct.TongKhauTru AS ThucNhanTinhLai
FROM dbo.CHITIETBANGLUONG ct
INNER JOIN dbo.BANGLUONG bl ON ct.MaBangLuong = bl.MaBangLuong
WHERE bl.Thang = 9 AND bl.Nam = 2026;
```

Kết quả mong đợi: `TienCong` khớp `fn_TinhTienCong`; `ThucNhan` khớp công thức đã chốt.
