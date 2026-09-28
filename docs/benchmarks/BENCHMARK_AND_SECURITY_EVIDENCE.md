# BÁO CÁO MINH CHỨNG HIỆU NĂNG INDEX VÀ BẢO MẬT HỆ THỐNG
## HỌC PHẦN: HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU – TV5 (TRẦN ĐỨC ANH)

---

## 1. MINH CHỨNG TỐI ƯU HIỆU NĂNG INDEX (BENCHMARK)

### 1.1 Index do TV5 sở hữu: `IX_NHANVIEN_MaPB_MaCV`
- **Định nghĩa:**
  ```sql
  CREATE NONCLUSTERED INDEX IX_NHANVIEN_MaPB_MaCV
  ON dbo.NHANVIEN (MaPB, MaCV)
  INCLUDE (MaNV, HoTen, LuongCoBan, TrangThai);
  ```
- **Mục tiêu kỹ thuật:** Chuyển đổi từ `Table Scan / Clustered Index Scan` sang `Index Seek` (Covering Index), loại bỏ hoàn toàn chi phí Key Lookup khi truy vấn danh sách nhân viên theo phòng ban và chức vụ.

### 1.2 Kết quả đo lường thực tế trên SQL Server:
```sql
SET STATISTICS IO ON;
SET STATISTICS TIME ON;

-- [TEST 1] Ép quét bảng không dùng Index
SELECT MaNV, HoTen, LuongCoBan, TrangThai
FROM dbo.NHANVIEN WITH (INDEX(0))
WHERE MaPB = 1;
-- KẾT QUẢ: Table 'NHANVIEN'. Scan count 1, logical reads: 2, CPU time: 0 ms.

-- [TEST 2] Truy vấn tối ưu qua Covering Index của TV5
SELECT MaNV, HoTen, LuongCoBan, TrangThai
FROM dbo.NHANVIEN WITH (INDEX(IX_NHANVIEN_MaPB_MaCV))
WHERE MaPB = 1;
-- KẾT QUẢ: Table 'NHANVIEN'. Scan count 1, logical reads: 2 (Index Seek trực tiếp không qua bảng dữ liệu gốc).
```

---

## 2. MINH CHỨNG XỬ LÝ TRANH CHẤP ĐỒNG THỜI (CONCURRENCY CONTROL)

### 2.1 Cơ chế khóa độc quyền UPDLOCK + HOLDLOCK
Trong thủ tục `dbo.sp_ChotBangLuong`, cơ chế bảo vệ được cài đặt như sau:
```sql
SELECT TrangThai
FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
WHERE MaBangLuong = @MaBangLuong;
```

### 2.2 Kịch bản kiểm thử 2 Session chạy đồng thời:
1. **Session 1 (Kế toán A):** Mở giao dịch, lấy khóa `UPDLOCK, HOLDLOCK` và giả lập trễ 10 giây bằng `WAITFOR DELAY '00:00:10'`.
2. **Session 2 (Kế toán B):** Cùng gọi lệnh `sp_ChotBangLuong` trên cùng một kỳ lương.
3. **Hiện tượng quan sát:** Session 2 bị chặn (Blocked) trong hàng đợi chờ Session 1 giải phóng khóa.
4. **Kết quả kết thúc:** Session 1 commit thành công chuyển trạng thái `DA_CHOT`. Session 2 đọc được trạng thái `DA_CHOT` mới và tự động quăng lỗi cảnh báo `Msg 50000: Kỳ lương này đã được chốt trước đó!`, ngăn chặn hoàn toàn hiện tượng Double Chốt (Lost Update).

---

## 3. MINH CHỨNG PHÂN QUYỀN TRUY CẬP (RBAC - GRANT/DENY)

### 3.1 Kiểm thử quyền của HR_Manager:
```sql
EXECUTE AS USER = 'user_HRManager';
-- Thử tính lương:
EXEC dbo.sp_TinhBangLuongThang @Thang = 9, @Nam = 2026, @NgayCongChuan = 26;
-- KẾT QUẢ: Msg 229, Level 14, State 5: The EXECUTE permission was denied on the object 'sp_TinhBangLuongThang'.
REVERT;
```

### 3.2 Kiểm thử quyền của Employee:
```sql
EXECUTE AS USER = 'user_Employee';
-- Thử đọc bảng lương gốc:
SELECT * FROM dbo.CHITIETBANGLUONG;
-- KẾT QUẢ: Msg 229: The SELECT permission was denied on the object 'CHITIETBANGLUONG'.

-- Đọc qua View được cấp quyền:
SELECT * FROM dbo.vw_BangLuongChiTiet;
-- KẾT QUẢ: Thành công hiển thị thông tin phiếu lương hợp lệ.
REVERT;
```
