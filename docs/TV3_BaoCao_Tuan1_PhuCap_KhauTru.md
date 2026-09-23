# BÁO CÁO KỸ THUẬT VÀ PHÂN TÍCH THIẾT KẾ TUẦN 1
- **Học phần:** Hệ quản trị cơ sở dữ liệu (DBMS330284)
- **Đề tài:** Hệ thống Quản lý Nhân sự và Tiền lương
- **Nhóm:** Nhóm 06
- **Thành viên thực hiện:** TV3 – Trần Tiến Đạt (MSSV: 24110198)
- **Giai đoạn:** Tuần 1 (21/09/2026 – 27/09/2026)

---

## 1. MỤC TIÊU VÀ ĐẶC TẢ NGHIỆP VỤ PHỤ CẤP & KHẤU TRỪ

### 1.1. Bản chất nghiệp vụ
- **Phụ cấp (Allowances):** Các khoản phúc lợi hoặc trợ cấp được cộng thêm vào thu nhập của nhân viên theo từng kỳ lương (ăn trưa, xăng xe, trách nhiệm, thâm niên...).
- **Khấu trừ (Deductions):** Các khoản tiền bị trừ khỏi tổng thu nhập của nhân viên trong kỳ tính lương (tạm ứng lương, phạt vi phạm kỷ luật, đi trễ, bồi thường thiết bị hỏng...).
- **Kỳ phát sinh:** Mỗi khoản phát sinh đều gắn liền với định danh nhân viên (`MaNV`) và chu kỳ tính lương theo tháng/năm (`Thang`, `Nam`).

### 1.2. Danh sách quy tắc ràng buộc (Business Rules)
- **BR01 (Ràng buộc nhân sự):** Chỉ nhân viên có hồ sơ hợp lệ trong bảng `NHANVIEN` và đang có trạng thái làm việc mới được ghi nhận khoản phát sinh.
- **BR02 (Ràng buộc giá trị tiền):** Số tiền phụ cấp và khấu trừ bắt buộc phải là số thực không âm (`SoTien >= 0`).
- **BR03 (Ràng buộc chu kỳ):** Tháng phải thuộc khoảng `1` đến `12`; Năm phải hợp lệ (`Nam >= 2020`).
- **BR04 (Bảo toàn dữ liệu sau khi chốt lương):** Khi kỳ lương `BANGLUONG(Thang, Nam)` đã chuyển sang trạng thái `'Đã chốt'`, mọi hành động thêm/sửa/xóa khoản phụ cấp/khấu trừ thuộc kỳ này đều bị chặn hoàn toàn.
- **BR05 (Tích hợp phân hệ Lương):** Phân hệ cung cấp tổng số tiền phụ cấp và khấu trừ theo kỳ để TV4 hoàn thiện công thức:  
  $$\text{Thực nhận} = \text{Tiền công} + \sum \text{Phụ cấp} - \sum \text{Khấu trừ}$$

---

## 2. THIẾT KẾ CƠ SỞ DỮ LIỆU CHUẨN HÓA 3NF

### 2.1. Đánh giá chuẩn hóa
- **1NF:** Toàn bộ thuộc tính đều có giá trị đơn nhất (atomic values).
- **2NF:** Sử dụng surrogate primary key (`MaPCNV`, `MaKTNV`), không có phụ thuộc một phần vào khóa chính.
- **3NF:** Loại bỏ hoàn toàn phụ thuộc bắc cầu; các thuộc tính như họ tên, phòng ban được chuẩn hóa lưu trữ tại `NHANVIEN` và liên kết thông qua khóa ngoại `MaNV`.

### 2.2. Từ điển dữ liệu (Data Dictionary)

#### Bảng `PHUCAPNHANVIEN`
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `MaPCNV` | `INT` | No | PK, IDENTITY(1,1) | Mã định danh khoản phụ cấp |
| `MaNV` | `VARCHAR(20)` | No | FK -> NHANVIEN(MaNV) | Mã nhân viên nhận phụ cấp |
| `Thang` | `INT` | No | CHECK (1..12) | Tháng tính phụ cấp |
| `Nam` | `INT` | No | CHECK (>= 2020) | Năm tính phụ cấp |
| `TenPhuCap` | `NVARCHAR(100)` | No | | Tên khoản phụ cấp |
| `SoTien` | `DECIMAL(18,2)` | No | CHECK (>= 0), DF: 0 | Số tiền phụ cấp |
| `NgayGhiNhan` | `DATE` | No | DF: GETDATE() | Ngày ghi nhận vào hệ thống |
| `GhiChu` | `NVARCHAR(255)` | Yes | | Ghi chú thêm |

#### Bảng `KHAUTRUNHANVIEN`
| Tên cột | Kiểu dữ liệu | Nullable | Ràng buộc | Ý nghĩa |
| :--- | :--- | :--- | :--- | :--- |
| `MaKTNV` | `INT` | No | PK, IDENTITY(1,1) | Mã định danh khoản khấu trừ |
| `MaNV` | `VARCHAR(20)` | No | FK -> NHANVIEN(MaNV) | Mã nhân viên bị khấu trừ |
| `Thang` | `INT` | No | CHECK (1..12) | Tháng tính khấu trừ |
| `Nam` | `INT` | No | CHECK (>= 2020) | Năm tính khấu trừ |
| `TenKhauTru` | `NVARCHAR(100)` | No | | Tên khoản khấu trừ |
| `SoTien` | `DECIMAL(18,2)` | No | CHECK (>= 0), DF: 0 | Số tiền khấu trừ |
| `NgayGhiNhan` | `DATE` | No | DF: GETDATE() | Ngày ghi nhận khấu trừ |
| `LyDo` | `NVARCHAR(255)` | Yes | | Lý do khấu trừ |

---

## 3. KIẾN TRÚC VÀ ĐỀ XUẤT GIAO DIỆN

### 3.1. Luồng xử lý phân tầng (Layered Architecture)
`PhuCapKhauTruPanel (UI)` $\rightarrow$ `PhuCapKhauTruService` $\rightarrow$ `PhuCapDAO / KhauTruDAO` $\rightarrow$ `SQL Server`
1. Tầng UI tiếp nhận tương tác người dùng, kiểm tra cơ bản rỗng/định dạng.
2. Tầng Service kiểm tra nghiệp vụ: kỳ lương đã chốt chưa, số tiền có hợp lệ không.
3. Tầng DAO thực thi câu lệnh SQL có tham số hóa bằng `PreparedStatement` hoặc `CallableStatement`.

### 3.2. Wireframe giao diện `PhuCapKhauTruPanel`
+------------------------------------------------------------------------------------------------------+
|  QUẢN LÝ PHỤ CẤP VÀ KHẤU TRỪ THEO KỲ                                                                |
+------------------------------------------------------------------------------------------------------+
|  Kỳ làm việc:  Tháng [ 09 ▼ ]  Năm [ 2026 ▼ ]   Phòng ban: [ -- Tất cả phòng ban -- ▼ ] [ Lọc ]     |
+------------------------------------------------------------------------------------------------------+
| [ TAB 1: PHỤ CẤP NHÂN VIÊN ]  |  [ TAB 2: KHẤU TRỪ NHÂN VIÊN ]  |  [ TAB 3: TỔNG HỢP THEO KỲ ]       |
+------------------------------------------------------------------------------------------------------+
|  DANH SÁCH BẢNG LƯƠNG PHÁT SINH THÁNG 09/2026:                                                       |
|  +-------+---------+--------------------+----------------------+---------------+------------+-----+  |
|  | Mã PC | Mã NV   | Họ và Tên          | Khoản phụ cấp        | Số tiền (VNĐ) | Ngày ghi   | ... |  |
|  +-------+---------+--------------------+----------------------+---------------+------------+-----+  |
|  | PC001 | NV001   | Nguyễn Văn An      | Ăn trưa văn phòng    |       730,000 | 01/09/2026 |     |  |
|  | PC002 | NV001   | Nguyễn Văn An      | Hỗ trợ xăng xe       |       500,000 | 01/09/2026 |     |  |
|  | PC003 | NV002   | Trần Thị Bích      | Phụ cấp trách nhiệm  |     1,500,000 | 05/09/2026 |     |  |
|  +-------+---------+--------------------+----------------------+---------------+------------+-----+  |
|  Tổng cộng phụ cấp kỳ: 2,730,000 VNĐ                                                                 |
+------------------------------------------------------------------------------------------------------+
|  FORM NHẬP LIỆU:                                                                                     |
|  Mã nhân viên:  [ NV001 - Nguyễn Văn An        ▼ ]   Khoản phát sinh: [ Phụ cấp xăng xe         ▼ ]  |
|  Số tiền (VNĐ): [ 500,000                        ]   Ngày áp dụng:    [ 2026-09-01              ]  |
|  Ghi chú/Lý do: [ Hỗ trợ đi lại công tác ngoài                                                  ]  |
|                                                                                                      |
|  [ + Thêm mới ]       [ ✎ Cập nhật ]       [ 🗑 Xóa ]       [ ⟳ Làm mới ]        [ 📄 Xuất Excel ]   |
+------------------------------------------------------------------------------------------------------+
---

## 4. BỘ TEST CASES KIỂM THỬ RÀNG BUỘC (WEEK 1 TESTCASES)

| Test ID | Mục tiêu kiểm thử | Đầu vào kiểm thử | Kết quả kỳ vọng | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| **TC_PC_01** | Thêm phụ cấp hợp lệ | `MaNV='NV001'`, `Thang=9`, `Nam=2026`, `SoTien=500000` | Insert thành công vào `PHUCAPNHANVIEN` | **PASS** |
| **TC_PC_02** | Vi phạm số tiền âm | `MaNV='NV001'`, `Thang=9`, `Nam=2026`, `SoTien=-100000` | Bị chặn bởi `CK_PHUCAP_SoTien` | **PASS** |
| **TC_PC_03** | Vi phạm tháng sai | `MaNV='NV001'`, `Thang=14`, `Nam=2026`, `SoTien=500000` | Bị chặn bởi `CK_PHUCAP_Thang` | **PASS** |
| **TC_PC_04** | Vi phạm năm nhỏ hơn 2020 | `MaNV='NV001'`, `Thang=9`, `Nam=2015`, `SoTien=500000` | Bị chặn bởi `CK_PHUCAP_Nam` | **PASS** |
| **TC_PC_05** | Vi phạm khóa ngoại NV | `MaNV='NV999'`, `Thang=9`, `Nam=2026`, `SoTien=500000` | Báo lỗi Foreign Key `FK_PHUCAP_NHANVIEN` | **PASS** |
| **TC_KT_01** | Thêm khấu trừ hợp lệ | `MaNV='NV002'`, `Thang=9`, `Nam=2026`, `SoTien=150000` | Insert thành công vào `KHAUTRUNHANVIEN` | **PASS** |
| **TC_KT_02** | Vi phạm khấu trừ tiền âm | `MaNV='NV002'`, `Thang=9`, `Nam=2026`, `SoTien=-50000` | Bị chặn bởi `CK_KHAUTRU_SoTien` | **PASS** |