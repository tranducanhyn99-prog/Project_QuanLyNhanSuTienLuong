# BÁO CÁO KỸ THUẬT VÀ PHÂN TÍCH THIẾT KẾ TUẦN 1
- **Học phần:** Hệ quản trị cơ sở dữ liệu (DBMS330284)
- **Đề tài:** Hệ thống Quản lý Nhân sự và Tiền lương
- **Nhóm:** Nhóm 06
- **Thành viên thực hiện:** TV3 – Trần Tiến Đạt (MSSV: 24110198)
- **Giai đoạn:** Tuần 1 (21/09/2026 – 27/09/2026)

---

## 1. MỤC TIÊU VÀ ĐẶC TẢ NGHIỆP VỤ PHỤ CẤP & KHẤU TRỪ

### 1.1. Bản chất nghiệp vụ
- **Phụ cấp (Allowances):** Các khoản phụ cấp và phúc lợi được cộng vào thu nhập nhân viên trong kỳ tính lương (ăn trưa, xăng xe, trách nhiệm...).
- **Khấu trừ (Deductions):** Các khoản giảm trừ khỏi thu nhập trong kỳ tính lương (tạm ứng, phạt nội quy, đi trễ, bồi hoàn tài sản...).
- **Kỳ phát sinh:** Mọi khoản tiền phát sinh đều gắn liền với mã định danh nhân viên (`MaNV` kiểu `INT`) và chu kỳ tính lương theo tháng/năm (`Thang`, `Nam`).

### 1.2. Danh sách quy tắc ràng buộc (Business Rules)
- **BR01 (Ràng buộc nhân sự):** `MaNV` bắt buộc có kiểu dữ liệu `INT` tương thích tuyệt đối với khóa chính của bảng `NHANVIEN(MaNV)` do TV1 thiết kế.
- **BR02 (Ràng buộc giá trị tiền):** Số tiền phụ cấp và số tiền khấu trừ bắt buộc không âm (`SoTien >= 0`).
- **BR03 (Ràng buộc chu kỳ):** `Thang` thuộc đoạn `[1, 12]`, `Nam` hợp lệ (`Nam >= 2020`).
- **BR04 (Bảo toàn dữ liệu sau khi chốt):** Khi kỳ lương tương ứng đã được chốt, không được phép can thiệp chỉnh sửa dữ liệu phát sinh của kỳ đó.
- **BR05 (Tích hợp tính lương):** Cung cấp dữ liệu phụ cấp/khấu trừ để module của TV4 tổng hợp tính lương thực nhận:  
  $$\text{Thực nhận} = \text{Tiền công} + \sum \text{Phụ cấp} - \sum \text{Khấu trừ}$$

---

## 2. THIẾT KẾ CƠ SỞ DỮ LIỆU CHUẨN HÓA 3NF

### 2.1. Chuẩn hóa dữ liệu
- **1NF:** Toàn bộ giá trị lưu trữ đều nguyên tố, có khóa chính định danh riêng cho từng dòng.
- **2NF:** Sử dụng surrogate key (`MaPCNV`, `MaKTNV`), không tồn tại phụ thuộc hàm từng phần vào khóa chính.
- **3NF:** Không tồn tại phụ thuộc hàm bắc cầu; các thông tin nhân sự (họ tên, phòng ban) được truy xuất qua khóa ngoại `MaNV INT`.

### 2.2. Từ điển dữ liệu (Data Dictionary)

#### Bảng `PHUCAPNHANVIEN`
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `MaPCNV` | `INT` | No | PK, IDENTITY(1,1) | Mã định danh khoản phụ cấp |
| `MaNV` | `INT` | No | FK -> NHANVIEN(MaNV) | Mã nhân viên nhận phụ cấp (đồng bộ INT) |
| `Thang` | `INT` | No | CHECK (1..12) | Tháng áp dụng |
| `Nam` | `INT` | No | CHECK (>= 2020) | Năm áp dụng |
| `TenPhuCap` | `NVARCHAR(100)` | No | | Tên khoản phụ cấp |
| `SoTien` | `DECIMAL(18,2)` | No | CHECK (>= 0), DF: 0 | Số tiền phụ cấp |
| `NgayGhiNhan` | `DATE` | No | DF: GETDATE() | Ngày ghi nhận vào hệ thống |
| `GhiChu` | `NVARCHAR(255)` | Yes | | Ghi chú thêm |

#### Bảng `KHAUTRUNHANVIEN`
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `MaKTNV` | `INT` | No | PK, IDENTITY(1,1) | Mã định danh khoản khấu trừ |
| `MaNV` | `INT` | No | FK -> NHANVIEN(MaNV) | Mã nhân viên bị khấu trừ (đồng bộ INT) |
| `Thang` | `INT` | No | CHECK (1..12) | Tháng áp dụng |
| `Nam` | `INT` | No | CHECK (>= 2020) | Năm áp dụng |
| `TenKhauTru` | `NVARCHAR(100)` | No | | Tên khoản khấu trừ |
| `SoTien` | `DECIMAL(18,2)` | No | CHECK (>= 0), DF: 0 | Số tiền khấu trừ |
| `NgayGhiNhan` | `DATE` | No | DF: GETDATE() | Ngày ghi nhận vào hệ thống |
| `LyDo` | `NVARCHAR(255)` | Yes | | Lý do khấu trừ |

---

## 3. KIẾN TRÚC VÀ GIAO DIỆN ĐỀ XUẤT

### 3.1. Luồng kiến trúc
`PhuCapKhauTruPanel` $\rightarrow$ `PhuCapKhauTruService` $\rightarrow$ `PhuCapDAO / KhauTruDAO` $\rightarrow$ `SQL Server`

### 3.2. Wireframe giao diện `PhuCapKhauTruPanel`
+------------------------------------------------------------------------------------------------------+
|  QUẢN LÝ PHỤ CẤP VÀ KHẤU TRỪ THEO KỲ                                                                |
+------------------------------------------------------------------------------------------------------+
|  Kỳ làm việc:  Tháng [ 09 ▼ ]  Năm [ 2026 ▼ ]   Phòng ban: [ -- Tất cả phòng ban -- ▼ ] [ Lọc ]     |
+------------------------------------------------------------------------------------------------------+
| [ TAB 1: PHỤ CẤP NHÂN VIÊN ]  |  [ TAB 2: KHẤU TRỪ NHÂN VIÊN ]  |  [ TAB 3: TỔNG HỢP THEO KỲ ]       |
+------------------------------------------------------------------------------------------------------+
|  DANH SÁCH BẢNG PHỤ CẤP THÁNG 09/2026:                                                               |
|  +-------+-------+--------------------+----------------------+---------------+------------+-------+  |
|  | Mã PC | MaNV  | Họ và Tên          | Khoản phụ cấp        | Số tiền (VNĐ) | Ngày ghi   | ...   |  |
|  +-------+-------+--------------------+----------------------+---------------+------------+-------+  |
|  | 1     | 1     | Nguyễn Minh Trí    | Ăn trưa văn phòng    |       730,000 | 01/09/2026 |       |  |
|  | 2     | 1     | Nguyễn Minh Trí    | Hỗ trợ xăng xe       |       500,000 | 01/09/2026 |       |  |
|  | 3     | 2     | Phạm Minh Quân     | Phụ cấp trách nhiệm  |     1,500,000 | 05/09/2026 |       |  |
|  +-------+-------+--------------------+----------------------+---------------+------------+-------+  |
|  Tổng cộng phụ cấp kỳ: 2,730,000 VNĐ                                                                 |
+------------------------------------------------------------------------------------------------------+
|  FORM NHẬP LIỆU:                                                                                     |
|  Mã nhân viên:  [ 1 - Nguyễn Minh Trí          ▼ ]   Khoản phát sinh: [ Hỗ trợ xăng xe          ▼ ]  |
|  Số tiền (VNĐ): [ 500,000                        ]   Ngày áp dụng:    [ 2026-09-01              ]  |
|  Ghi chú/Lý do: [ Hỗ trợ đi lại công tác ngoài                                                  ]  |
|                                                                                                      |
|  [ + Thêm mới ]       [ ✎ Cập nhật ]       [ 🗑 Xóa ]       [ ⟳ Làm mới ]        [ 📄 Xuất Excel ]   |
+------------------------------------------------------------------------------------------------------+
---

## 4. BỘ TEST CASES KIỂM TRA RÀNG BUỘC (WEEK 1 TESTCASES)

| Test ID | Mục tiêu kiểm thử | Dữ liệu đầu vào (Input) | Kết quả kỳ vọng | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| **TC_PC_01** | Thêm phụ cấp hợp lệ | `MaNV = 1`, `Thang = 9`, `Nam = 2026`, `SoTien = 500000` | Insert thành công vào `PHUCAPNHANVIEN` | **PASS** |
| **TC_PC_02** | Vi phạm số tiền âm | `MaNV = 1`, `Thang = 9`, `Nam = 2026`, `SoTien = -100000` | Bị chặn bởi `CK_PHUCAP_SoTien`[cite: 2] | **PASS** |
| **TC_PC_03** | Vi phạm tháng sai | `MaNV = 1`, `Thang = 14`, `Nam = 2026`, `SoTien = 500000` | Bị chặn bởi `CK_PHUCAP_Thang`[cite: 2] | **PASS** |
| **TC_PC_04** | Vi phạm năm sai | `MaNV = 1`, `Thang = 9`, `Nam = 2018`, `SoTien = 500000` | Bị chặn bởi `CK_PHUCAP_Nam`[cite: 2] | **PASS** |
| **TC_PC_05** | Vi phạm khóa ngoại NV | `MaNV = 9999` (chưa có), `Thang = 9`, `Nam = 2026` | Báo lỗi Foreign Key `FK_PHUCAP_NHANVIEN` | **PASS** |
| **TC_KT_01** | Thêm khấu trừ hợp lệ | `MaNV = 2`, `Thang = 9`, `Nam = 2026`, `SoTien = 150000` | Insert thành công vào `KHAUTRUNHANVIEN` | **PASS** |
| **TC_KT_02** | Vi phạm khấu trừ tiền âm | `MaNV = 2`, `Thang = 9`, `Nam = 2026`, `SoTien = -50000` | Bị chặn bởi `CK_KHAUTRU_SoTien`[cite: 2] | **PASS** |