# BÁO CÁO KỸ THUẬT VÀ THIẾT KẾ CƠ SỞ DỮ LIỆU CUỐI KỲ
## CHUYÊN ĐỀ: PHÂN HỆ QUẢN LÝ PHỤ CẤP, KHẤU TRỪ VÀ NGHIỆP VỤ BỔ TRỢ

- **Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
- **Giảng viên hướng dẫn:** TS. Phan Thị Thể
- **Đề tài:** Hệ Thống Quản Lý Nhân Sự và Tiền Lương
- **Nhóm thực hiện:** Nhóm 06
- **Thành viên chịu trách nhiệm:** TV3 – Trần Tiến Đạt (MSSV: 24110198)
- **Rà soát source/kết quả QA:** 08/10/2026

---

### I. TỔNG QUAN PHÂN HỆ VÀ ĐẶC TẢ NGHIỆP VỤ

Phân hệ Phụ cấp & Khấu trừ giữ vai trò cầu nối quyết định tính chính xác của kỳ lương:
1. **Phụ cấp nhân viên:** Ghi nhận các khoản hỗ trợ phát sinh (Ăn trưa, xăng xe, trách nhiệm, độc hại) theo từng nhân viên (`MaNV`) và kỳ lương (`Thang`, `Nam`).
2. **Khấu trừ nhân viên:** Ghi nhận các khoản trừ thu nhập phát sinh (Tạm ứng giữa tháng, vi phạm nội quy, đi trễ, bồi hoàn thiết bị).
3. **Cung cấp số liệu tính lương:** Phân hệ cung cấp tổng phụ cấp qua `fn_TongPhuCap` và View `vw_TongPhuCapThang`; tổng khấu trừ qua `fn_TongKhauTru` và View `vw_TongKhauTruThang`. `sp_TinhBangLuongThang` gọi hai scalar function khi tính chi tiết:
   $$\text{Thực nhận} = \text{Tiền công} + \sum \text{Phụ cấp} - \sum \text{Khấu trừ}$$

---

### II. CÁC ĐỐI TƯỢNG CƠ SỞ DỮ LIỆU SỞ HỮU (OWNERSHIP CỦA TV3)

Theo đúng ma trận phân công đối tượng SQL của Nhóm 06:

| STT | Đối tượng SQL           | Tên đối tượng | Mục đích nghiệp vụ                                                            |
|:---:|:------------------------| :--- |:------------------------------------------------------------------------------|
|  1  | **Non-Clustered Index** | `IX_PHUCAP_MaNV_ThangNam` | Tăng tốc độ tra cứu phụ cấp theo nhân viên và kỳ lương.                       |
|  2  | **View**                | `vw_TongPhuCapThang` | Tổng hợp tự động số khoản và tổng tiền phụ cấp theo nhân viên/kỳ.             |
|  3  | **Scalar Function**     | `fn_TongKhauTru` | Trả về tổng tiền khấu trừ của một nhân viên trong kỳ tính lương.              |
|  4  | **Scalar Function**     | `fn_TongPhuCap` | Trả tổng phụ cấp một nhân viên trong kỳ; được procedure tính lương gọi. |
|  5  | **Trigger**             | `trg_PhuCap_KhongSuaKhiDaChotLuong` | Được cài trong module 05; khóa kỳ cha bằng `UPDLOCK, HOLDLOCK`, chặn insert/update/delete vào kỳ đã chốt. |
|  6  | **Stored Procedure**    | `sp_ThemPhuCapNhanVien` | Kiểm tra nhân viên đang làm, kỳ, tiền dương và trạng thái kỳ trước khi thêm. |
|  7  | **Transaction**         | `sp_XoaKyLuongChuaChot` | Khóa kỳ cha; xóa chi tiết trước header trong transaction, từ chối kỳ đã chốt. |

---

### III. KẾT QUẢ ĐO LƯỜNG VÀ ĐÁNH GIÁ BENCHMARK CHỈ MỤC

Lần chạy 08/10/2026 dùng 30.000 dòng trong temporary benchmark table tại `database/test_benchmark_index_TV3.sql`. Số đo mô tả fixture và máy chạy đó, không đảm bảo hiệu năng trên dữ liệu production.

#### 1. Bảng so sánh chỉ số hiệu năng

| Chỉ số đo lường | Trước khi có Index (Scan) | Sau khi có Index (Index Seek) | Tỉ lệ cải thiện |
| :--- | :---: | :---: | :---: |
| **Physical Operator** | Clustered Index Scan | **Index Seek (Non-Clustered)** | Tránh quét toàn bộ bảng |
| **Logical Reads** | **642 pages** | **2 pages** | **Giảm khoảng 99,7% trong lần chạy này** |
| **CPU Time** | 0 ms | **0 ms** | Độ phân giải báo cáo của lần chạy |
| **Elapsed Time** | 2 ms | **0 ms** | Phụ thuộc cache và máy chạy |
| **Key Lookup** | Không | **Không (Covering Index)** | Do có `INCLUDE` |

#### 2. Ảnh benchmark đã lưu
* `screenshots/TV3/TV3_Benchmark_ExecutionPlan.png`
* `screenshots/TV3/TV3_Benchmark_StatisticsIO.png`

Đây là ảnh có sẵn từ lần chụp trước; chưa xác minh cùng fixture/ngày với log 08/10. Dùng log benchmark được dẫn ở trên để trích số liệu lần chạy mới.

---

### IV. KIỂM THỬ CHỨC NĂNG VÀ TRANSACTION

#### 1. Ràng buộc toàn vẹn dữ liệu
* Thử chèn `SoTien = -500000` $\rightarrow$ Bị chặn bởi `CK_PHUCAP_SoTien` (PASS).
* Thử chèn `Thang = 13` $\rightarrow$ Bị chặn bởi `CK_KHAUTRU_Thang` (PASS).
* Lần chạy tích hợp ngày 08/10 xác nhận function/view kể cả kết quả rỗng, lỗi ràng buộc tiền/kỳ, xóa kỳ nháp, từ chối xóa kỳ chốt và cleanup fixture. Kiểm tra giờ chấm công thuộc module TV2, không được tính là test sở hữu TV3.

#### 2. Kiểm thử Transaction `sp_XoaKyLuongChuaChot`
* Trong lần chạy 08/10, fixture kỳ nháp 05/2020 được xóa và kỳ chốt bị từ chối; cleanup của fixture PASS. Procedure khóa kỳ bằng `UPDLOCK, HOLDLOCK`, sau đó xóa chi tiết trước header trong transaction. Log: `build/sql-verification/20261008_101955_11b4e24c/test_module_phucap_khautru_TV3.log`.

#### 3. Đồng bộ với thao tác chốt
Trigger trong module 05 cho phụ cấp, khấu trừ và chấm công khóa dòng kỳ cha bằng `UPDLOCK, HOLDLOCK`; INSERT/UPDATE/DELETE kiểm tra các kỳ ở cả `inserted` và `deleted`. Procedure tính lương khóa kỳ trước rồi giữ khóa nguồn trong cùng transaction. Log tích hợp 08/10 xác nhận bốn cuộc đua chốt với sửa attendance/allowance/deduction/payroll detail đều chờ khoảng 1,0–1,1 giây, bị từ chối và nguồn không đổi.

#### 4. Seed và tài khoản
`06_Demo_Data.sql` là bước tùy chọn sau modules 01–05, thêm bản ghi demo còn thiếu trong transaction và không xóa phụ cấp/khấu trừ theo tháng. Schema không tạo mật khẩu hoặc tài khoản demo mặc định; DBA provision SQL accounts riêng.

---

### V. GIAO DIỆN VÀ TÍCH HỢP ỨNG DỤNG JAVA SWING

Module `PhuCapKhauTruPanel` đã được tích hợp hoàn chỉnh vào ứng dụng chính `MainFrame`:
* **Tab 1 - Phụ cấp:** Thêm mới và xóa phụ cấp của nhân viên theo tháng/năm.
* **Tab 2 - Khấu trừ:** Quản lý các khoản trừ lương theo kỳ.
* **Tab 3 - Tổng hợp:** Đọc trực tiếp từ View `vw_TongPhuCapThang` và gọi Function `fn_TongKhauTru` để kế toán theo dõi trực quan.
* **Nút Transaction:** Cho phép xóa kỳ lương chưa chốt an toàn kèm hộp thoại cảnh báo người dùng.

`PhuCapKhauTruService` cho phép `DB_Admin`, `HR_Manager` và `Payroll_Officer` xem/thêm/xóa phụ cấp, khấu trừ và xem tổng hợp. Xóa kỳ lương chỉ cho `DB_Admin` và `Payroll_Officer`. SQL Server vẫn là điểm phân quyền đáng tin cậy: Payroll có quyền ghi allowance/deduction nhưng chỉ đọc nhân viên, chấm công, phòng ban và chức vụ.
