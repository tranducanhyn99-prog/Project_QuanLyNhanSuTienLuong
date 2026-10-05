# BÁO CÁO KỸ THUẬT VÀ THIẾT KẾ CƠ SỞ DỮ LIỆU CUỐI KỲ
## CHUYÊN ĐỀ: PHÂN HỆ QUẢN LÝ PHỤ CẤP, KHẤU TRỪ VÀ NGHIỆP VỤ BỔ TRỢ

- **Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
- **Giảng viên hướng dẫn:** TS. Phan Thị Thể
- **Đề tài:** Hệ Thống Quản Lý Nhân Sự và Tiền Lương
- **Nhóm thực hiện:** Nhóm 06
- **Thành viên chịu trách nhiệm:** TV3 – Trần Tiến Đạt (MSSV: 24110198)

---

### I. TỔNG QUAN PHÂN HỆ VÀ ĐẶC TẢ NGHIỆP VỤ

Phân hệ Phụ cấp & Khấu trừ giữ vai trò cầu nối quyết định tính chính xác của kỳ lương:
1. **Phụ cấp nhân viên:** Ghi nhận các khoản hỗ trợ phát sinh (Ăn trưa, xăng xe, trách nhiệm, độc hại) theo từng nhân viên (`MaNV`) và kỳ lương (`Thang`, `Nam`).
2. **Khấu trừ nhân viên:** Ghi nhận các khoản trừ thu nhập phát sinh (Tạm ứng giữa tháng, vi phạm nội quy, đi trễ, bồi hoàn thiết bị).
3. **Cung cấp số liệu tính lương:** Phân hệ cung cấp tổng phụ cấp thông qua View `vw_TongPhuCapThang` và tổng khấu trừ qua Function `fn_TongKhauTru` để TV4 tính toán lương thực nhận theo công thức:
   $$\text{Thực nhận} = \text{Tiền công} + \sum \text{Phụ cấp} - \sum \text{Khấu trừ}$$

---

### II. CÁC ĐỐI TƯỢNG CƠ SỞ DỮ LIỆU SỞ HỮU (OWNERSHIP CỦA TV3)

Theo đúng ma trận phân công đối tượng SQL của Nhóm 06:

| STT | Đối tượng SQL           | Tên đối tượng | Mục đích nghiệp vụ                                                            |
|:---:|:------------------------| :--- |:------------------------------------------------------------------------------|
|  1  | **Non-Clustered Index** | `IX_PHUCAP_MaNV_ThangNam` | Tăng tốc độ tra cứu phụ cấp theo nhân viên và kỳ lương.                       |
|  2  | **View**                | `vw_TongPhuCapThang` | Tổng hợp tự động số khoản và tổng tiền phụ cấp theo nhân viên/kỳ.             |
|  3  | **Scalar Function**     | `fn_TongKhauTru` | Trả về tổng tiền khấu trừ của một nhân viên trong kỳ tính lương.              |
|  4  | **Trigger**             | `trg_PhuCap_KhongSuaKhiDaChotLuong` | Chặn thêm, sửa, xóa phụ cấp khi kỳ lương trong BANGLUONG đã `DA_CHOT`.        |
|  5  | **Stored Procedure**    | `sp_ThemPhuCapNhanVien` | Thêm phụ cấp vào bảng `PHUCAPNHANVIEN` với mã nhân viên và số tiền tương ứng. |
|  6  | **Transaction**         | `sp_XoaKyLuongChuaChot` | Xóa bảng lương chưa chốt, rollback an toàn khi kỳ đã chốt hoặc gặp lỗi.       |

---

### III. KẾT QUẢ ĐO LƯỜNG VÀ ĐÁNH GIÁ BENCHMARK CHỈ MỤC

Thử nghiệm được thực hiện trên tập dữ liệu mô phỏng 30.000 bản ghi phụ cấp tại `database/test_benchmark_index_TV3.sql`:

#### 1. Bảng so sánh chỉ số hiệu năng

| Chỉ số đo lường | Trước khi có Index (Scan) | Sau khi có Index (Index Seek) | Tỉ lệ cải thiện |
| :--- | :---: | :---: | :---: |
| **Physical Operator** | Clustered Index Scan | **Index Seek (Non-Clustered)** | Tránh quét toàn bộ bảng |
| **Logical Reads** | **188 pages** | **3 pages** | **Giảm 98.4% I/O** |
| **CPU Time** | 16 ms | **0 ms** | Tối ưu tài nguyên CPU |
| **Elapsed Time** | 28 ms | **2 ms** | **Nhanh gấp 14 lần** |
| **Key Lookup** | Không | **Không (Covering Index)** | Do có `INCLUDE` |

#### 2. Minh chứng hình ảnh Benchmark
* Ảnh Execution Plan: `screenshots/TV3/TV3_Benchmark_ExecutionPlan.png`
* Ảnh Statistics IO/Time: `screenshots/TV3/TV3_Benchmark_StatisticsIO.png`

---

### IV. KIỂM THỬ CHỨC NĂNG VÀ TRANSACTION

#### 1. Ràng buộc toàn vẹn dữ liệu
* Thử chèn `SoTien = -500000` $\rightarrow$ Bị chặn bởi `CK_PHUCAP_SoTien` (PASS).
* Thử chèn `Thang = 13` $\rightarrow$ Bị chặn bởi `CK_KHAUTRU_Thang` (PASS).
* Thử chèn chấm công có `GioRa <= GioVao` $\rightarrow$ Bị chặn bởi `trg_ChamCong_KiemTraGio` (PASS).

#### 2. Kiểm thử Transaction `sp_XoaKyLuongChuaChot`
* **Trường hợp kỳ chưa chốt (`TrangThai = N'CHUA_CHOT'`):** Transaction thực hiện xóa chi tiết trong `CHITIETBANGLUONG` trước, sau đó xóa bản ghi trong `BANGLUONG`, `COMMIT TRANSACTION` thành công.
* **Trường hợp kỳ đã chốt (`TrangThai = N'DA_CHOT'`):** Hệ thống phát sinh `RAISERROR`, nhảy vào khối `CATCH` và thực thi `ROLLBACK TRANSACTION`, bảo toàn 100% dữ liệu kỳ lương.

---

### V. GIAO DIỆN VÀ TÍCH HỢP ỨNG DỤNG JAVA SWING

Module `PhuCapKhauTruPanel` đã được tích hợp hoàn chỉnh vào ứng dụng chính `MainFrame`:
* **Tab 1 - Phụ cấp:** Thêm mới và xóa phụ cấp của nhân viên theo tháng/năm.
* **Tab 2 - Khấu trừ:** Quản lý các khoản trừ lương theo kỳ.
* **Tab 3 - Tổng hợp:** Đọc trực tiếp từ View `vw_TongPhuCapThang` và gọi Function `fn_TongKhauTru` để kế toán theo dõi trực quan.
* **Nút Transaction:** Cho phép xóa kỳ lương chưa chốt an toàn kèm hộp thoại cảnh báo người dùng.