# TV3 — CẨM NANG CÂU LỆNH SQL THỰC CHIẾN (KIỂM TRA & KÍCH HOẠT CHỨC NĂNG)

**Dành riêng cho:** Trần Tiến Đạt (TV3 · MSSV: 24110198)  
**Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284) — TS. Phan Thị Thể  
**Phân hệ sở hữu:** Phụ Cấp, Khấu Trừ & Quản Lý Kỳ Lương Nháp

---

> [!IMPORTANT]
> **Database sử dụng chính thức:** `QuanLyNhanSuTienLuong` (đảm bảo chọn đúng DB này trên SSMS để đồng bộ trực tiếp với ứng dụng Java).

---

## MỤC LỤC KỊCH BẢN VẤN ĐÁP SQL TV3

- [Phần 1: Kiểm Tra Các Ràng Buộc CHECK](#phan-1-kiem-tra-cac-rang-buoc-check)
  - [1.1. Chặn số tiền âm trên Phụ cấp (`CK_PHUCAP_SoTien`)](#11-chan-so-tien-am-tren-phu-cap)
  - [1.2. Chặn tháng không hợp lệ ngoài 1-12 (`CK_PHUCAP_Thang`)](#12-chan-thang-khong-hop-le-ngoai-1-12)
- [Phần 2: Kiểm Tra Stored Procedure Thêm Phụ Cấp (`sp_ThemPhuCapNhanVien`)](#phan-2-kiem-tra-stored-procedure-them-phu-cap)
  - [2.1. Thêm phụ cấp hợp lệ và nhận mã trả về `@MaPCNV OUTPUT`](#21-them-phu-cap-hop-le-va-nhan-ma-tra-ve-mapcnv-output)
  - [2.2. Bẫy lỗi khi thêm phụ cấp cho nhân viên đã nghỉ việc](#22-bay-loi-khi-them-phu-cap-cho-nhan-vien-da-nghi-viec)
- [Phần 3: Kiểm Tra Function Tổng Khấu Trừ (`fn_TongKhauTru`)](#phan-3-kiem-tra-function-tong-khau-tru)
- [Phần 4: Kiểm Tra View Tổng Hợp Phụ Cấp (`vw_TongPhuCapThang`)](#phan-4-kiem-tra-view-tong-hop-phu-cap)
- [Phần 5: Kiểm Tra Transaction Xóa Kỳ Lương Chưa Chốt (`sp_XoaKyLuongChuaChot`)](#phan-5-kiem-tra-transaction-xoa-ky-luong-chua-chot)
- [Phần 6: Đo Hiệu Năng Index Phụ Cấp (`IX_PHUCAP_MaNV_ThangNam`)](#phan-6-do-hieu-nang-index-phu-cap)

---

## PHẦN 1: KIỂM TRA CÁC RÀNG BUỘC CHECK

### 1.1. Chặn số tiền âm trên Phụ cấp (`CK_PHUCAP_SoTien`)
**Cô hỏi:** *"Em ràng buộc số tiền phụ cấp không được âm như thế nào? Chạy SQL cho cô xem."*

```sql
USE QuanLyNhanSuTienLuong;
GO

PRINT N'--- TEST 1.1: Cố tình chèn phụ cấp âm (Kỳ vọng: Lỗi Msg 547) ---';
BEGIN TRY
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan)
    VALUES (1008, 10, 2026, N'Phụ cấp âm', -500000, GETDATE());
    PRINT N'LỖI: Ràng buộc bị hổng!';
END TRY
BEGIN CATCH
    PRINT N'-> BẮT THÀNH CÔNG: ' + ERROR_MESSAGE();
END CATCH;
GO
```

---

### 1.2. Chặn tháng không hợp lệ ngoài 1-12 (`CK_PHUCAP_Thang`)
```sql
USE QuanLyNhanSuTienLuong;
GO

PRINT N'--- TEST 1.2: Cố tình nhập tháng 13 (Kỳ vọng: Lỗi Msg 547) ---';
BEGIN TRY
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan)
    VALUES (1008, 13, 2026, N'Phụ cấp tháng sai', 500000, GETDATE());
    PRINT N'LỖI: Nhập được tháng 13 là sai!';
END TRY
BEGIN CATCH
    PRINT N'-> BẮT THÀNH CÔNG: ' + ERROR_MESSAGE();
END CATCH;
GO
```

---

## PHẦN 2: KIỂM TRA STORED PROCEDURE THÊM PHỤ CẤP (`sp_ThemPhuCapNhanVien`)

### 2.1. Thêm phụ cấp hợp lệ và nhận mã trả về `@MaPCNV OUTPUT`
**Cô hỏi:** *"Em viết Stored Procedure thêm phụ cấp như thế nào? Cho cô xem mã tự sinh trả về qua tham số OUTPUT."*

```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @MaPC_Moi INT;

EXEC dbo.sp_ThemPhuCapNhanVien
    @MaNV = 1008,
    @Thang = 10,
    @Nam = 2026,
    @TenPhuCap = N'Thưởng dự án quý 3',
    @SoTien = 2500000.00,
    @NgayGhiNhan = '2026-10-20',
    @GhiChu = N'Khen thưởng hoàn thành xuất sắc',
    @MaPCNV = @MaPC_Moi OUTPUT;

SELECT @MaPC_Moi AS [MaPhuCapVuaTao];

SELECT * FROM dbo.PHUCAPNHANVIEN WHERE MaPCNV = @MaPC_Moi;
GO
```

---

### 2.2. Bẫy lỗi khi thêm phụ cấp cho nhân viên đã nghỉ việc
```sql
USE QuanLyNhanSuTienLuong;
GO

PRINT N'--- TEST 2.2: Thêm phụ cấp cho nhân viên đã nghỉ việc 1029 (Hồ Viết Thắng) ---';
DECLARE @Out INT;
BEGIN TRY
    EXEC dbo.sp_ThemPhuCapNhanVien
        @MaNV = 1029,
        @Thang = 10,
        @Nam = 2026,
        @TenPhuCap = N'Phụ cấp người nghỉ việc',
        @SoTien = 500000.00,
        @MaPCNV = @Out OUTPUT;
    PRINT N'LỖI: Thêm được cho nhân viên nghỉ việc!';
END TRY
BEGIN CATCH
    PRINT N'-> BẪY LỖI THÀNH CÔNG: ' + ERROR_MESSAGE();
END CATCH;
GO
```

---

## PHẦN 3: KIỂM TRA FUNCTION TỔNG KHẤU TRỪ (`fn_TongKhauTru`)

**Cô hỏi:** *"Hàm tính tổng khấu trừ của em hoạt động ra sao? Xử lý nhân viên không có khoản khấu trừ nào như thế nào?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

SELECT 
    nv.MaNV,
    nv.HoTen,
    dbo.fn_TongKhauTru(nv.MaNV, 10, 2026) AS TongKhauTruThang10
FROM dbo.NHANVIEN nv
WHERE nv.MaNV IN (1008, 1015, 1025);
GO
```

* **Giải thích:** Hàm sử dụng `ISNULL(SUM(SoTien), 0)`. Nếu nhân viên không có khoản khấu trừ nào trong kỳ thì hàm trả về `0.00` thay vì giá trị `NULL`.

---

## PHẦN 4: KIỂM TRA VIEW TỔNG HỢP PHỤ CẤP (`vw_TongPhuCapThang`)

**Cô hỏi:** *"View tổng hợp phụ cấp của em gom nhóm những thông tin gì?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

SELECT 
    MaNV,
    HoTen,
    Thang,
    Nam,
    SoKhoanPhuCap,
    TongTienPhuCap
FROM dbo.vw_TongPhuCapThang
WHERE Thang = 10 AND Nam = 2026
ORDER BY TongTienPhuCap DESC;
GO
```

---

## PHẦN 5: KIỂM TRA TRANSACTION XÓA KỲ LƯƠNG CHƯA CHỐT (`sp_XoaKyLuongChuaChot`)

**Cô hỏi:** *"Khi xóa một kỳ lương chưa chốt, em xử lý ràng buộc khóa ngoại giữa bảng lương và chi tiết lương như thế nào? Chứng minh Transaction an toàn."*

```sql
USE QuanLyNhanSuTienLuong;
GO

-- 1. Tạo một kỳ lương nháp thử nghiệm Tháng 12/2026
IF NOT EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE Thang = 12 AND Nam = 2026)
BEGIN
    INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai, NgayTao)
    VALUES (12, 2026, 26, 'CHUA_CHOT', GETDATE());
    
    DECLARE @MaBL_Tam INT = SCOPE_IDENTITY();
    -- Chèn chi tiết con
    INSERT INTO dbo.CHITIETBANGLUONG (MaBangLuong, MaNV, LuongCoBan, NgayCongThucTe, TienCong, TongPhuCap, TongKhauTru, ThucNhan)
    VALUES (@MaBL_Tam, 1008, 26000000, 26, 26000000, 0, 0, 26000000);
    PRINT N'[Chuẩn bị] Đã tạo kỳ nháp Tháng 12/2026 để test xóa.';
END;
GO

-- 2. Gọi Stored Procedure xóa kỳ nháp (xóa con trước cha trong Transaction)
EXEC dbo.sp_XoaKyLuongChuaChot @Thang = 12, @Nam = 2026;

-- 3. Kiểm tra xác nhận kỳ 12/2026 đã được dọn sạch hoàn toàn
SELECT * FROM dbo.BANGLUONG WHERE Thang = 12 AND Nam = 2026;
GO
```

* **Giải thích với Cô:**  
  *“Dạ thưa Cô, trong `sp_XoaKyLuongChuaChot`, em sử dụng `SELECT ... FROM BANGLUONG WITH (UPDLOCK, HOLDLOCK)`. Sau khi kiểm tra kỳ chưa chốt, em xóa dữ liệu con ở bảng `CHITIETBANGLUONG` trước, rồi mới xóa dòng cha ở `BANGLUONG`, bọc toàn bộ trong `BEGIN TRANSACTION ... COMMIT`. Nếu kỳ đã `DA_CHOT`, procedure sẽ phát sinh lỗi và rollback, đảm bảo không vi phạm khóa ngoại và tính toàn vẹn dữ liệu.”*

---

## PHẦN 6: ĐO HIỆU NĂNG INDEX PHỤ CẤP (`IX_PHUCAP_MaNV_ThangNam`)

```sql
USE QuanLyNhanSuTienLuong;
GO

SET NOCOUNT ON;
SET STATISTICS IO ON;

PRINT N'--- TRUY VẤN DÙNG INDEX IX_PHUCAP_MaNV_ThangNam (TV3) ---';
SELECT MaNV, Thang, Nam, TenPhuCap, SoTien
FROM dbo.PHUCAPNHANVIEN WITH (INDEX(IX_PHUCAP_MaNV_ThangNam))
WHERE MaNV = 1008 AND Thang = 10 AND Nam = 2026;

SET STATISTICS IO OFF;
GO
```
