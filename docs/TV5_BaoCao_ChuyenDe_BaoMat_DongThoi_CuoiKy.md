# BÁO CÁO CHUYÊN ĐỀ TỔNG KẾT CUỐI KỲ — THÀNH VIÊN 5 (TV5)
# ĐỀ TÀI: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (PeopleOS)
### Học phần: Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284_01) — Học kỳ I (2026 - 2027)
### Trường Đại học Sư phạm Kỹ thuật TP. Hồ Chí Minh (HCMUTE) — Khoa Công nghệ Thông tin

---

- **Thành viên thực hiện:** Trần Đức Anh (TV5)
- **Mã số sinh viên (MSSV):** 24110155
- **Giảng viên hướng dẫn:** TS. Phan Thị Thể
- **Vai trò chuyên môn:** 
  1. Trưởng nhóm điều phối kiến trúc, tích hợp mã nguồn và kiểm soát toàn vẹn bộ nộp bài.
  2. Phụ trách Kiến trúc 3 tầng & Giao diện ứng dụng PeopleOS (Java Swing Light Theme).
  3. Phụ trách Mô hình Bảo mật 2 lớp & Phân quyền 4 Database Roles (RBAC, Row-Level Security).
  4. Phụ trách Nghiệp vụ Chốt lương & Kiểm soát Truy cập Đồng thời (Concurrency Control).
  5. Phụ trách Đo kiểm Hiệu năng Chỉ mục (Index Benchmark) & Kế hoạch thực thi (Execution Plan).
  6. Xây dựng Bộ kiểm thử tự động toàn diện (Automated Testing Suite - 257 Assertions).
- **Trạng thái đối soát:** Đã tích hợp trọn vẹn vào `main` commit `02ad3b2`, đối soát toàn vẹn SHA-256 đạt **574/574 tệp khớp 100%**.

---

# MỤC LỤC

1. [CHƯƠNG 1. KIẾN TRÚC 3 TẦNG & GIAO DIỆN HIỆN ĐẠI (PeopleOS)](#chương-1-kiến-trúc-3-tầng--giao-diện-hiện-đại-peopleos)
   - [1.1 Kiến trúc tổng thể 3 tầng (Three-Tier Architecture)](#11-kiến-trúc-tổng-thể-3-tầng-three-tier-architecture)
   - [1.2 Thiết kế Giao diện PeopleOS Light Theme](#12-thiết-kế-giao-diện-peopleos-light-theme)
   - [1.3 Tác vụ nền bất đồng bộ với SwingWorker](#13-tác-vụ-nền-bất-đồng-bộ-với-swingworker)
   - [1.4 Thanh Đăng nhập nhanh (Quick Demo) đa vai trò](#14-thanh-đăng-nhập-nhanh-quick-demo-đa-vai-trò)
2. [CHƯƠNG 2. BẢO MẬT 2 LỚP & MA TRẬN PHÂN QUYỀN 4 DATABASE ROLES](#chương-2-bảo-mật-2-lớp--ma-trận-phân-quyền-4-database-roles)
   - [2.1 Cơ chế Xác thực 2 Lớp (Two-Layer Security Architecture)](#21-cơ-chế-xác-thực-2-lớp-two-layer-security-architecture)
   - [2.2 Thuật toán Băm mật khẩu PBKDF2-HMAC-SHA256 & Quản trị Bộ nhớ](#22-thuật-toán-băm-mật-khẩu-pbkdf2-hmac-sha256--quản-trị-bộ-nhớ)
   - [2.3 Thiết kế & Ánh xạ 4 Database Roles tại SQL Server Engine](#23-thiết-kế--ánh-xạ-4-database-roles-tại-sql-server-engine)
   - [2.4 Bảng Ma trận Phân quyền Thực tế (RBAC Matrix Table)](#24-bảng-ma-trận-phân-quyền-thực-tế-rbac-matrix-table)
   - [2.5 Bảo vệ Dữ liệu mức Dòng (Row-Level Security) với ORIGINAL_LOGIN()](#25-bảo-vệ-dữ-liệu-mức-dòng-row-level-security-với-original_login)
3. [CHƯƠNG 3. QUY TRÌNH CHỐT LƯƠNG & KIỂM SOÁT ĐỒNG THỜI (CONCURRENCY)](#chương-3-quy-trình-chốt-lương--kiểm-soát-đồng-thời-concurrency)
   - [3.1 Vòng đời Bảng lương (Payroll Lifecycle Workflow)](#31-vòng-đời-bảng-lương-payroll-lifecycle-workflow)
   - [3.2 Nghiệp vụ Chốt lương an toàn (sp_ChotBangLuong)](#32-nghiệp-vụ-chốt-lương-an-toàn-sp_chotbangluong)
   - [3.3 Nghiệp vụ Mở lại kỳ lương có điều kiện (sp_MoLaiKyLuong)](#33-nghiệp-vụ-mở-lại-kỳ-lương-có-điều-kiện-sp_molaikyluong)
   - [3.4 Kiểm soát Xung đột Đồng thời (Concurrency Control với UPDLOCK)](#34-kiểm-soát-xung-đột-đồng-thời-concurrency-control-với-updlock)
   - [3.5 Bộ ba Trigger bảo vệ kỳ lương đã chốt chống xâm phạm](#35-bộ-ba-trigger-bảo-vệ-kỳ-lương-đã-chốt-chống-xâm-phạm)
4. [CHƯƠNG 4. ĐO LƯỜNG HIỆU NĂNG CHỈ MỤC & KẾ HOẠCH THỰC THI (BENCHMARK)](#chương-4-đo-lường-hiệu-năng-chỉ-mục--kế-hoạch-thực-thi-benchmark)
   - [4.1 Bối cảnh Thử nghiệm Benchmark trên 10.000 dòng dữ liệu](#41-bối-cảnh-thử-nghiệm-benchmark-trên-10000-dòng-dữ-liệu)
   - [4.2 Phân tích Toán tử Clustered Index Scan (Trước tối ưu)](#42-phân-tích-toán-tử-clustered-index-scan-trước-tối-ưu)
   - [4.3 Phân tích Toán tử Index Seek NonClustered & Covering Index (Sau tối ưu)](#43-phân-tích-toán-tử-index-seek-nonclustered--covering-index-sau-tối-ưu)
   - [4.4 Bảng Đối chiếu Chỉ số Thực tế (STATISTICS IO/TIME & Subtree Cost)](#44-bảng-đối-chiếu-chỉ-số-thực-tế-statistics-iotime--subtree-cost)
5. [CHƯƠNG 5. BỘ KIỂM THỬ TỰ ĐỘNG & NGHIỆM THU CHỨC NĂNG (TEST SUITE)](#chương-5-bộ-kiểm-thử-tự-động--nghiệm-thu-chức-năng-test-suite)
   - [5.1 Cấu trúc Bộ kiểm thử 257 Assertions](#51-cấu-trúc-bộ-kiểm-thử-257-assertions)
   - [5.2 Kết quả Kiểm thử Phân quyền (SecurityRegressionTest - 104 Assertions)](#52-kết-quả-kiểm-thử-phân-quyền-securityregressiontest---104-assertions)
   - [5.3 Kết quả Kiểm thử Giao diện (GuiQaRegressionTest - 88 Assertions)](#53-kết-quả-kiểm-thử-giao-diện-guiqaregressiontest---88-assertions)
   - [5.4 Thử nghiệm Đồng thời 2 Phiên thực tế trong SSMS](#54-thử-nghiệm-đồng-thời-2-phiên-thực-tế-trong-ssms)
6. [CHƯƠNG 6. KỊCH BẢN THUYẾT TRÌNH & BỘ CÂU HỎI VẤN ĐÁP CÁ NHÂN](#chương-6-kịch-bản-thuyết-trình--bộ-câu-hỏi-vấn-đáp-cá-nhân)
   - [6.1 Kịch bản Báo cáo Trực tiếp trên Phần mềm (Live Demo Script)](#61-kịch-bản-báo-cáo-trực-tiếp-trên-phần-mềm-live-demo-script)
   - [6.2 Trả lời Câu hỏi Phản biện của Giảng viên (Hội đồng Q&A)](#62-trả-lời-câu-hỏi-phản-biện-của-giảng-viên-hội-đồng-qa)

---

# CHƯƠNG 1. KIẾN TRÚC 3 TẦNG & GIAO DIỆN HIỆN ĐẠI (PeopleOS)

### 1.1 Kiến trúc tổng thể 3 tầng (Three-Tier Architecture)

Toàn bộ hệ thống được xây dựng theo mô hình phân tầng chặt chẽ nhằm tách biệt hoàn toàn giữa giao diện, logic ứng dụng và tầng quản trị dữ liệu:

```
┌─────────────────────────────────────────────────────────────────────────┐
│               PRESENTATION LAYER (GIAO DIỆN NGƯỜI DÙNG)                 │
│  - Java Swing Light Theme (PeopleOS Design System, Canvas #F8FAFC)      │
│  - Tác vụ nền SwingWorker ngoài Event Dispatch Thread (EDT)             │
│  - Thanh Quick Login đa vai trò (Admin, HR, Payroll, Employee A & B)   │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ (DTO / Data Model)
┌────────────────────────────────────▼────────────────────────────────────┐
│              BUSINESS LOGIC & DATA ACCESS LAYER (SERVICE/DAO)           │
│  - SecurityService (Xác thực PBKDF2, Quản lý Session cá nhân)          │
│  - PayrollService & AttendanceService (Điều phối Transaction)           │
│  - DatabaseConnection (JDBC Driver, trustServerCertificate=true)        │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ (T-SQL Commands / CallableStatement)
┌────────────────────────────────────▼────────────────────────────────────┐
│               DATABASE ENGINE LAYER (MICROSOFT SQL SERVER)               │
│  - 10 Bảng chuẩn hóa 3NF + Quan hệ Khóa chính / Khóa ngoại             │
│  - 14 Stored Procedures (Nghiệp vụ giao dịch phân tầng, Savepoints)     │
│  - 9 Triggers (Khóa kỳ chốt, bảo toàn lịch sử, toàn vẹn ngày công)      │
│  - 5 Functions (fn_TinhThucNhan, fn_TongHop, fn_KiemTra)                │
│  - 4 Database Roles (role_DBAdmin, role_HRManager, role_Payroll...)     │
└─────────────────────────────────────────────────────────────────────────┘
```

- **Tính toàn vẹn:** Ứng dụng Java không can thiệp trực tiếp tính toán lương mà ủy quyền hoàn toàn cho Stored Procedures và UDF tại SQL Server.
- **Tính độc lập:** Các thay đổi cấu trúc bảng nội bộ không làm vỡ giao diện nhờ tầng Service/DAO trung gian.

---

### 1.2 Thiết kế Giao diện PeopleOS Light Theme

Nhằm khắc phục tình trạng giao diện Java Swing mặc định cũ kỹ (Metal theme màu xám), TV5 đã thiết kế hệ thống giao diện **PeopleOS Light Theme**:
- **Bảng màu chủ đạo:**
  - Nền trang (Canvas Background): Slate 50 (`#F8FAFC`) dịu mắt.
  - Thẻ giao diện (Card Surfaces): Trắng tinh (`#FFFFFF`) với viền phân cách bo tròn (`#E2E8F0`).
  - Màu thương hiệu (Primary Accent): Indigo (`#4F46E5`), Hover (`#4338CA`), Nền nhạt (`#EEF2FF`).
  - Màu trạng thái: Xanh lá thành công (`#10B981`), Đỏ cảnh báo (`#EF4444`), Vàng hổ phách (`#F59E0B`).
- **Typography:** Toàn bộ font chữ sử dụng chuẩn **Segoe UI** sắc nét, chống răng cưa Unicode tiếng Việt, kích thước phân tầng rõ ràng (11pt - 24pt).
- **Thành phần UI tùy biến:**
  - `SoftPanel`: Panel bo góc mượt mà (radius 16px - 24px) có đổ bóng nhẹ.
  - `ModernScrollBarUI`: Thanh cuộn thanh mảnh, tự động ẩn hiện theo phong cách hệ điều hành hiện đại.
  - `ModernTableRenderer`: Bảng dữ liệu xen kẽ sọc ngựa vằn, padding rộng rãi, tiêu đề nổi bật.

---

### 1.3 Tác vụ nền bất đồng bộ với SwingWorker

Trong các thao tác tốn thời gian (truy vấn tổng hợp bảng lương, chấm công 20 nhân sự, chốt kỳ lương), nếu thực thi trực tiếp trên luồng giao diện (**Event Dispatch Thread - EDT**) sẽ khiến cửa sổ ứng dụng bị đơ (Not Responding).

TV5 đã triển khai mẫu thiết kế bất đồng bộ qua lớp `SwingWorker<T, V>`:
1. **`doInBackground()`**: Chạy trên luồng nền (Worker Thread), thực thi các lệnh JDBC / Stored Procedure kết nối SQL Server.
2. **`done()`**: Tự động trả kết quả về luồng EDT để cập nhật dữ liệu lên bảng (`JTable`), thanh tiến trình (`JProgressBar`) hoặc nhãn trạng thái (`JLabel`) mà không gây deadlock hoặc xung đột luồng.

---

### 1.4 Thanh Đăng nhập nhanh (Quick Demo) đa vai trò

Nhằm phục vụ buổi bảo vệ đồ án diễn ra trôi chảy, không tốn thời gian gõ mật khẩu băm dài, TV5 đã thiết kế thanh **Quick Login Chips** ngay dưới form đăng nhập:

```
[ ĐĂNG NHẬP NHANH (DEMO) ]                                Nhấp đúp để vào ngay
┌────────────────────┐ ┌────────────────────┐ ┌────────────────────┐
│      Admin         │ │     HR Manager     │ │      Kế toán       │
│   (role_DBAdmin)   │ │  (role_HRManager)  │ │(role_PayrollOfficer│
└────────────────────┘ └────────────────────┘ └────────────────────┘
┌─────────────────────────────────┐ ┌─────────────────────────────────┐
│     Nhân viên A (730.000đ)      │ │      Nhân viên B (0đ)           │
│   (role_Employee - 1 ngày công) │ │ (role_Employee - 0 ngày công)   │
└─────────────────────────────────┘ └─────────────────────────────────┘
```

- **Thao tác 1 click:** Tự động điền đúng Tên đăng nhập và Mật khẩu băm tương ứng.
- **Thao tác nhấp đúp (Double-click):** Tự động điền và kích hoạt đăng nhập thẳng vào hệ thống trong 0.5 giây.
- **Tính an toàn:** Cho phép ẩn hoàn toàn thanh demo bằng cờ hệ thống `-Dapp.hideDemo=true` khi đưa vào môi trường sản xuất.

---

# CHƯƠNG 2. BẢO MẬT 2 LỚP & MA TRẬN PHÂN QUYỀN 4 DATABASE ROLES

### 2.1 Cơ chế Xác thực 2 Lớp (Two-Layer Security Architecture)

Bảo mật là một trong những điểm sáng lớn nhất của đồ án, kết hợp chặt chẽ giữa tầng Ứng dụng Java và tầng CSDL SQL Server:

```
[ NGƯỜI DÙNG NHẬP THÔNG TIN ]
        │
        ▼ (Tên đăng nhập + Mật khẩu gốc)
┌─────────────────────────────────────────────────────────────────────────┐
│ LỚP 1: ỨNG DỤNG JAVA (MÃ HÓA & PHÒNG THỦ BỘ NHỚ)                         │
│ 1. Đọc Hash & Salt ngẫu nhiên từ bảng TAIKHOAN.                         │
│ 2. Băm mật khẩu bằng PBKDF2WithHmacSHA256 qua 600.000 vòng lặp.         │
│ 3. So sánh bằng thuật toán Constant-Time Arrays.equals chống Timing     │
│    Attack.                                                              │
│ 4. Lập tức xóa trắng mảng char[] mật khẩu trên RAM (Arrays.fill(0)).    │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ (Xác thực thành công)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ LỚP 2: DATABASE ENGINE (SQL LOGIN & DATABASE ROLES)                     │
│ 1. Lấy thông tin TAIKHOAN.SqlLogin được DBA gán riêng.                  │
│ 2. Mở Connection JDBC bằng chính SQL Login đó.                          │
│ 3. SQL Server Engine kiểm tra quyền hạn thực tế thông qua 4 Roles.      │
│ 4. Áp dụng Row-Level Security tự động lọc dữ liệu cá nhân.              │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### 2.2 Thuật toán Băm mật khẩu PBKDF2-HMAC-SHA256 & Quản trị Bộ nhớ

- **Tiêu chuẩn mã hóa:** Tuân thủ hướng dẫn mới nhất của **NIST SP 800-132**:
  - Thuật toán: `PBKDF2WithHmacSHA256`
  - Chuỗi muối (Salt): **16 bytes (128 bits)** sinh bằng bộ sinh số ngẫu nhiên mật mã `SecureRandom`.
  - Số vòng lặp (Iterations): **600.000 vòng** (chống lại hoàn toàn các cuộc tấn công vét cạn brute-force và bảng Rainbow Table bằng GPU chuyên dụng).
  - Độ dài khóa sinh (Key Length): **256 bits**.
- **Chống rò rỉ bộ nhớ (Memory Leak Prevention):**
  - Tuyệt đối không lưu mật khẩu ở dạng kiểu `String` (vì String mang tính immutable, tồn tại trong String Pool của JVM cho đến khi Garbage Collector dọn dẹp, dễ bị đọc trộm qua Memory Dump).
  - Sử dụng mảng ký tự `char[]` và hàm `Arrays.fill(password, '\0')` ngay sau khi hoàn tất xác thực.

---

### 2.3 Thiết kế & Ánh xạ 4 Database Roles tại SQL Server Engine

Hệ thống phân tách quyền hạn thành 4 Database Roles riêng biệt, không dùng tài khoản `sa` để kết nối nghiệp vụ:

1. **`role_DBAdmin` (Quản trị hệ thống):**
   - Quản trị tài khoản, phân quyền, cấu hình hệ thống.
   - Toàn quyền SELECT, INSERT, UPDATE trên các bảng danh mục và bảng nghiệp vụ.
   - Có quyền thực thi Stored Procedure mở lại kỳ lương (`sp_MoLaiKyLuong`).
2. **`role_HRManager` (Trưởng phòng Nhân sự):**
   - Quản lý danh mục Phòng ban, Chức vụ, Hồ sơ nhân viên (`NHANVIEN`).
   - Ghi nhận và tổng hợp dữ liệu chấm công (`CHAMCONG`).
   - Quyền hạn: `CRUD` trên hồ sơ nhân sự và chấm công; `SELECT` trên bảng lương; **BỊ TỪ CHỐI (DENY)** quyền tính lương hoặc chốt lương.
3. **`role_PayrollOfficer` (Kế toán Tiền lương):**
   - Quản lý các khoản phụ cấp và khấu trừ biến động (`PHUCAPNHANVIEN`, `KHAUTRUNHANVIEN`).
   - Tính toán bảng lương tự động (`sp_TinhLuongThang`) và chốt kỳ lương (`sp_ChotBangLuong`).
   - Quyền hạn: `CRUD` phụ cấp/khấu trừ; `EXECUTE` các thủ tục tính và chốt lương; `SELECT` hồ sơ nhân sự (chỉ đọc); **BỊ TỪ CHỐI (DENY)** quyền chỉnh sửa lý lịch hoặc chức vụ nhân viên.
4. **`role_Employee` (Nhân viên thông thường):**
   - Tra cứu thông tin cá nhân và phiếu lương của chính mình.
   - Quyền hạn: **BỊ TỪ CHỐI (DENY)** toàn bộ các bảng gốc; chỉ được phép `SELECT` trên View bảo vệ dòng `vw_PhieuLuongCaNhan`.

---

### 2.4 Bảng Ma trận Phân quyền Thực tế (RBAC Matrix Table)

Bảng ma trận thể hiện sự phân định trách nhiệm rõ ràng (Separation of Duties - SoD):

| Đối tượng CSDL / Thao tác | `role_DBAdmin` | `role_HRManager` | `role_PayrollOfficer` | `role_Employee` | Cơ chế kỹ thuật kiểm soát |
|---|:---:|:---:|:---:|:---:|---|
| **Hồ sơ Nhân sự (`NHANVIEN`)** | Toàn quyền | **CRUD** | SELECT (Chỉ đọc) | **DENY** | Phân quyền GRANT/DENY tại mức Table |
| **Bảng Chấm công (`CHAMCONG`)** | Toàn quyền | **CRUD** | SELECT (Chỉ đọc) | **DENY** | Quyền ghi thuộc về phân hệ HR |
| **Phụ cấp & Khấu trừ** | Toàn quyền | Nhập liệu | **CRUD** | **DENY** | Quyền thẩm định thuộc về Kế toán |
| **Tính lương (`sp_TinhLuongThang`)** | Toàn quyền | **DENY** | **EXECUTE** | **DENY** | Chỉ Payroll Officer được chạy tính lương |
| **Chốt lương (`sp_ChotBangLuong`)** | Toàn quyền | **DENY** | **EXECUTE** | **DENY** | Ngăn HR can thiệp số liệu tài chính |
| **Mở lại kỳ lương (`sp_MoLaiKyLuong`)** | **EXECUTE** | **DENY** | **EXECUTE (có lý do)** | **DENY** | Thủ tục đặc biệt yêu cầu thẩm quyền cao |
| **Xem phiếu lương cá nhân** | Tất cả | Tất cả | Tất cả | **DÒNG RIÊNG (*)** | **Row-Level Security với `ORIGINAL_LOGIN()`** |

---

### 2.5 Bảo vệ Dữ liệu mức Dòng (Row-Level Security) với ORIGINAL_LOGIN()

Nhân viên thông thường tuyệt đối không được cấp quyền `SELECT` trực tiếp trên bảng `CHITIETBANGLUONG` (để tránh lộ mức lương của đồng nghiệp hoặc ban giám đốc). 

TV5 đã triển khai **Row-Level Security** thông qua View `vw_PhieuLuongCaNhan`:

```sql
CREATE OR ALTER VIEW dbo.vw_PhieuLuongCaNhan
AS
SELECT 
    ct.MaChiTiet, ct.MaBangLuong, ct.MaNV,
    nv.HoTen, pb.TenPB, cv.TenCV,
    ct.LuongCoBan, ct.NgayCongChuan, ct.NgayCongThucTe,
    ct.TienCong, ct.TongPhuCap, ct.TongKhauTru, ct.ThucNhan,
    bl.Thang, bl.Nam, bl.TrangThai
FROM dbo.CHITIETBANGLUONG ct
INNER JOIN dbo.BANGLUONG bl ON ct.MaBangLuong = bl.MaBangLuong
INNER JOIN dbo.NHANVIEN nv ON ct.MaNV = nv.MaNV
INNER JOIN dbo.PHONGBAN pb ON nv.MaPB = pb.MaPB
INNER JOIN dbo.CHUCVU cv ON nv.MaCV = cv.MaCV
INNER JOIN dbo.TAIKHOAN tk ON nv.MaNV = tk.MaNV
WHERE 
    -- Nếu là Admin hoặc Kế toán: Được xem toàn bộ
    IS_MEMBER('role_DBAdmin') = 1 
    OR IS_MEMBER('role_PayrollOfficer') = 1
    -- Nếu là Nhân viên thông thường: BẮT BUỘC KHỚP SQL LOGIN THỰC TẾ
    OR (
        IS_MEMBER('role_Employee') = 1 
        AND tk.SqlLogin = ORIGINAL_LOGIN()
    );
GO
```

> **Điểm mấu chốt bảo mật:** Sử dụng `ORIGINAL_LOGIN()` (hàm trả về tài khoản SQL Server kết nối ban đầu) thay vì `SUSER_SNAME()`. Nhờ đó, ngay cả khi mã nguồn Java bị can thiệp hoặc cố tình đổi Session ID, SQL Server Engine vẫn chặn đứng mọi hành vi truy cập chéo phiếu lương của người khác!

---

# CHƯƠNG 3. QUY TRÌNH CHỐT LƯƠNG & KIỂM SOÁT ĐỒNG THỜI (CONCURRENCY)

### 3.1 Vòng đời Bảng lương (Payroll Lifecycle Workflow)

Vòng đời của một kỳ lương trải qua 2 trạng thái rõ rệt:

```
   ┌────────────────────────────────────────────────────────┐
   │ BƯỚC 1: KỲ LƯƠNG NHÁP (TrangThai = 'CHUA_CHOT')        │
   │ - Cho phép sửa chấm công, thêm phụ cấp, phạt khấu trừ.  │
   │ - Được phép chạy sp_TinhLuongThang nhiều lần để đối soát│
   └───────────────────────────┬────────────────────────────┘
                               │
                               │ GỌI: sp_ChotBangLuong
                               │ (Kiểm tra điều kiện, UPDLOCK, khóa dòng)
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │ BƯỚC 2: KỲ LƯƠNG ĐÃ CHỐT (TrangThai = 'DA_CHOT')       │
   │ - Cập nhật NgayChot = GETDATE().                       │
   │ - Kích hoạt 3 Triggers tự động chặn sửa đổi dữ liệu gốc:│
   │   + trg_ChamCong_ChanSuaKyDaChot                       │
   │   + trg_PhuCap_ChanSuaKyDaChot                         │
   │   + trg_KhauTru_ChanSuaKyDaChot                        │
   │ - Không thể chạy lại tính lương hoặc xóa bảng lương.   │
   └───────────────────────────┬────────────────────────────┘
                               │
                               │ Khi có sai sót nghiêm trọng:
                               │ GỌI: sp_MoLaiKyLuong (Admin / Payroll)
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │ BƯỚC 3: MỞ LẠI KỲ LƯƠNG CÓ ĐIỀU KIỆN (REOPEN)          │
   │ - Ghi nhận lý do mở lại vào Audit Log.                 │
   │ - Chuyển TrangThai về 'CHUA_CHOT', NgayChot = NULL.    │
   │ - Cho phép chỉnh sửa lại và chốt lại sau khi hoàn tất. │
   └────────────────────────────────────────────────────────┘
```

---

### 3.2 Nghiệp vụ Chốt lương an toàn (sp_ChotBangLuong)

Thủ tục `sp_ChotBangLuong` được thiết kế đảm bảo các tiêu chí an toàn cao nhất:

```sql
CREATE OR ALTER PROCEDURE dbo.sp_ChotBangLuong
    @MaBangLuong INT,
    @NguoiChot NVARCHAR(100) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Kiểm tra tồn tại và khóa độc quyền kỳ lương bằng UPDLOCK, ROWLOCK
        DECLARE @TrangThai NVARCHAR(20);
        SELECT @TrangThai = TrangThai
        FROM dbo.BANGLUONG WITH (UPDLOCK, ROWLOCK)
        WHERE MaBangLuong = @MaBangLuong;

        IF @TrangThai IS NULL
            THROW 50001, N'Không tìm thấy bảng lương chỉ định.', 1;

        -- 2. Kiểm tra trạng thái: Nếu đã chốt thì không được chốt lại
        IF @TrangThai = N'DA_CHOT'
            THROW 50002, N'Bảng lương này đã được chốt sổ trước đó. Không thể thao tác lại!', 1;

        -- 3. Kiểm tra tính toàn vẹn: Bảng lương phải có dữ liệu chi tiết
        IF NOT EXISTS (SELECT 1 FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong = @MaBangLuong)
            THROW 50003, N'Bảng lương chưa có dữ liệu chi tiết. Vui lòng tính lương trước khi chốt!', 1;

        -- 4. Thực hiện chuyển trạng thái và ghi nhận mốc thời gian
        UPDATE dbo.BANGLUONG
        SET 
            TrangThai = N'DA_CHOT',
            NgayChot = GETDATE(),
            NguoiChot = ISNULL(@NguoiChot, ORIGINAL_LOGIN())
        WHERE MaBangLuong = @MaBangLuong;

        COMMIT TRANSACTION;
        SELECT 1 AS Status, N'Chốt sổ kỳ lương thành công!' AS Message;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO
```

---

### 3.3 Nghiệp vụ Mở lại kỳ lương có điều kiện (sp_MoLaiKyLuong)

Khi có sự cố phát sinh (ví dụ bổ sung quyết định khen thưởng đột xuất hoặc phát hiện nhầm lẫn ngày công), hệ thống cung cấp thủ tục `sp_MoLaiKyLuong`:
- **Kiểm soát quyền hạn:** Chỉ tài khoản thuộc `role_DBAdmin` hoặc `role_PayrollOfficer` được phép gọi.
- **Tham số bắt buộc:** Phải cung cấp lý do mở lại (`@LyDoMoLai NVARCHAR(255)`) để lưu vết kiểm toán.
- **Rollback an toàn:** Trả trạng thái về `CHUA_CHOT` và set `NgayChot = NULL`.

---

### 3.4 Kiểm soát Xung đột Đồng thời (Concurrency Control với UPDLOCK)

Trong môi trường doanh nghiệp có nhiều kế toán và quản lý cùng thao tác trên hệ thống, nguy cơ tranh chấp tài nguyên (Concurrency Conflict) là rất lớn:

#### Kịch bản 1: Hai phiên cùng bấm "Tính lương" cho cùng 1 tháng
- **Rủi ro:** Gây trùng lặp dữ liệu (Duplicate Rows), xuất hiện 2 dòng header trong `BANGLUONG` hoặc tính chồng chéo trong `CHITIETBANGLUONG`.
- **Giải pháp của TV5:** 
  - Trong `sp_TinhLuongThang`, sử dụng gợi ý khóa `WITH (UPDLOCK, HOLDLOCK)` khi kiểm tra sự tồn tại của header `BANGLUONG`.
  - Phiên A vào trước sẽ giữ khóa cập nhật (`Update Lock`). Phiên B vào sau sẽ tự động bị giữ lại (Block) ở trạng thái chờ an toàn.
  - Khi Phiên A hoàn tất và Commit, Phiên B thức dậy thấy header đã tồn tại và chỉ việc cập nhật hoặc thông báo đã có bảng lương.
  - **Kết quả kiểm thử:** Duy nhất 1 bảng lương được tạo, 0 dòng trùng lặp, tính toàn vẹn đạt 100%.

#### Kịch bản 2: Đối đầu giữa Kế toán bấm "Chốt lương" và Nhân sự đang "Sửa công"
- **Rủi ro:** Hiện tượng đọc bóng ma hoặc dữ liệu bẩn (Dirty Write).
- **Giải pháp của TV5:** 
  - Đặt mức cô lập giao dịch `SET TRANSACTION ISOLATION LEVEL READ COMMITTED`.
  - Phép chốt lương dùng `UPDLOCK, ROWLOCK`.
  - Khi `sp_ChotBangLuong` Commit thành công (`TrangThai = 'DA_CHOT'`), giao dịch sửa công của nhân sự bên kia lập tức bị Trigger `trg_ChamCong_ChanSuaKyDaChot` chặn lại và ném ra lỗi ngoại lệ `50005`. Dữ liệu bảng lương đã chốt được bảo vệ an toàn tuyệt đối.

---

### 3.5 Bộ ba Trigger bảo vệ kỳ lương đã chốt chống xâm phạm

Để phòng thủ chiều sâu (ngay cả khi ai đó dùng công cụ ngoài như SSMS hoặc DBeaver để can thiệp trực tiếp), TV5 thiết lập 3 Trigger chặn đứng các thao tác `INSERT, UPDATE, DELETE` trên các bảng dữ liệu gốc một khi kỳ lương của tháng đó đã ở trạng thái `DA_CHOT`:

1. `trg_ChamCong_ChanSuaKyDaChot`: Khóa bảng `CHAMCONG`.
2. `trg_PhuCap_ChanSuaKyDaChot`: Khóa bảng `PHUCAPNHANVIEN`.
3. `trg_KhauTru_ChanSuaKyDaChot`: Khóa bảng `KHAUTRUNHANVIEN`.

---

# CHƯƠNG 4. ĐO LƯỜNG HIỆU NĂNG CHỈ MỤC & KẾ HOẠCH THỰC THI (BENCHMARK)

### 4.1 Bối cảnh Thử nghiệm Benchmark trên 10.000 dòng dữ liệu

Để minh chứng hiệu quả tối ưu hóa theo đúng chuẩn học thuật của bộ môn Hệ Quản Trị CSDL:
- **Tập dữ liệu:** Sinh bảng tạm `#BenchmarkNhanVien` với **10.000 bản ghi nhân viên**, phân bổ đều vào các Phòng ban (`MaPB` từ 1 đến 10) và Chức vụ (`MaCV` từ 1 đến 12).
- **Yêu cầu nghiệp vụ:** Tìm kiếm danh sách nhân sự thuộc Phòng Kỹ thuật (`MaPB = 2`) và giữ Chức danh Nhân viên (`MaCV = 1`), lấy ra các thông tin: `MaNV, HoTen, LuongCoBan, TrangThai`.
- **Số lượng bản ghi thỏa mãn:** Chính xác **833 dòng**.
- **Công cụ đo lường:** Kích hoạt `SET STATISTICS IO ON`, `SET STATISTICS TIME ON` và bật **Include Actual Execution Plan (Ctrl + M)** trong SQL Server Management Studio (SSMS).

---

### 4.2 Phân tích Toán tử Clustered Index Scan (Trước tối ưu)

Khi chưa có chỉ mục chuyên biệt trên cặp khóa `(MaPB, MaCV)`:

```
[ KẾ HOẠCH THỰC THI: CLUSTERED INDEX SCAN ]
  Physical Operation: Clustered Index Scan
  Logical Operation:  Clustered Index Scan
  Object:             [#BenchmarkNhanVien].[PK_Bench_MaNV]
  Predicate:          [MaPB]=(2) AND [MaCV]=(1)
  Actual Rows Read:   10,000 dòng
  Output Rows:        833 dòng
  Estimated I/O Cost: 0.106829
  Estimated CPU Cost: 0.011157
  Subtree Cost:       0.117986 (100%)
```

- **Nhận xét chuyên sâu:**
  - SQL Server phải đọc tuần tự từ trang đầu tiên đến trang cuối cùng của bảng (toàn bộ 10.000 bản ghi).
  - Tốn **142 Logical Reads** (số trang đĩa 8KB được đọc vào Buffer Cache).
  - Áp dụng bộ lọc `Predicate` sau khi đọc, gây lãng phí công suất đọc của 9.167 dòng không liên quan.

---

### 4.3 Phân tích Toán tử Index Seek NonClustered & Covering Index (Sau tối ưu)

TV5 tiến hành tạo chỉ mục bao phủ (Covering Index) trên bảng:

```sql
CREATE NONCLUSTERED INDEX IX_Bench_MaPB_MaCV
ON dbo.#BenchmarkNhanVien (MaPB, MaCV)
INCLUDE (HoTen, LuongCoBan, TrangThai);
```

Kết quả trong Execution Plan chuyển đổi hoàn toàn:

```
[ KẾ HOẠCH THỰC THI: INDEX SEEK (NONCLUSTERED) ]
  Physical Operation: Index Seek
  Logical Operation:  Index Seek
  Object:             [#BenchmarkNhanVien].[IX_Bench_MaPB_MaCV]
  Seek Predicates:    Prefix: [MaPB], [MaCV] = Scalar Operator((2)), Scalar Operator((1))
  Actual Rows Read:   833 dòng
  Output Rows:        833 dòng
  Estimated I/O Cost: 0.0109645
  Estimated CPU Cost: 0.0010737
  Subtree Cost:       0.0120382 (100%)
```

- **Nhận xét chuyên sâu:**
  - Database Engine sử dụng cấu trúc cây B-Tree, tìm kiếm trực tiếp bằng toán tử `Seek Predicates`.
  - **Chỉ đọc đúng 833 dòng**, không đọc thừa dù chỉ 1 dòng!
  - **Covering Index triệt tiêu hoàn toàn `Key Lookup`**: Do các cột `HoTen, LuongCoBan, TrangThai` đã được đưa vào mệnh đề `INCLUDE`, toàn bộ dữ liệu cần thiết nằm trọn ở tầng lá của Nonclustered Index, không cần phải tốn 833 lần Bookmark Lookup ngược về Clustered Index.

---

### 4.4 Bảng Đối chiếu Chỉ số Thực tế (STATISTICS IO/TIME & Subtree Cost)

| Tiêu chí đo lường | Trước tối ưu (Clustered Scan) | Sau tối ưu (Covering Seek) | Tỷ lệ cải thiện |
|---|:---:|:---:|:---:|
| **Toán tử thực thi (Operator)** | `Clustered Index Scan` | `Index Seek (NonClustered)` | Chuyển đổi tối ưu |
| **Số dòng thực tế đọc (`Actual Rows Read`)** | 10.000 dòng | 833 dòng | **Giảm 91.67%** |
| **Số trang đọc logic (`Logical Reads`)** | 142 reads | 15 reads | **Giảm 89.44%** |
| **Chi phí I/O ước tính (`Estimated I/O Cost`)** | 0.106829 | 0.0109645 | **Giảm 89.74%** |
| **Chi phí CPU ước tính (`Estimated CPU Cost`)** | 0.011157 | 0.0010737 | **Giảm 90.38%** |
| **Tổng chi phí cây truy vấn (`Subtree Cost`)** | 0.117986 | 0.0120382 | **Nhanh gấp ~9.8 lần** |
| **Thời gian CPU (`CPU Time`)** | 3 ms | 0 – 1 ms | **Giảm ~66 - 100%** |
| **Toán tử tra cứu phụ (`Key Lookup`)** | Không có | **0 (Triệt tiêu hoàn toàn)** | Tối ưu tuyệt đối |

---

# CHƯƠNG 5. BỘ KIỂM THỬ TỰ ĐỘNG & NGHIỆM THU CHỨC NĂNG (TEST SUITE)

### 5.1 Cấu trúc Bộ kiểm thử 257 Assertions

Nhằm đảm bảo hệ thống đạt độ tin cậy cấp doanh nghiệp (Enterprise Grade), TV5 đã thiết kế bộ kiểm thử tự động gồm **257 Assertions độc lập**, chia thành 3 nhóm kiểm định:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    TỔNG HỢP KẾT QUẢ TEST SUITE (257 PASS)               │
├──────────────────────────────────────┬────────────┬──────────┬──────────┤
│ Nhóm kiểm thử                        │ Số lượng   │ Kết quả  │ Tỷ lệ    │
├──────────────────────────────────────┼────────────┼──────────┼──────────┤
│ 1. SecurityRegressionTest            │ 104 tests  │ 104 PASS │ 100%     │
│ 2. GuiQaRegressionTest               │  88 tests  │  88 PASS │ 100%     │
│ 3. LightTheme & LoginChip Component  │  65 tests  │  65 PASS │ 100%     │
├──────────────────────────────────────┼────────────┼──────────┼──────────┤
│ TỔNG CỘNG                            │ 257 tests  │ 257 PASS │ 100%     │
└──────────────────────────────────────┴────────────┴──────────┴──────────┘
```

---

### 5.2 Kết quả Kiểm thử Phân quyền (SecurityRegressionTest - 104 Assertions)

- **Mục tiêu:** Kiểm tra nghiêm ngặt quyền hạn của 4 Database Roles đối với tất cả các bảng và thủ tục.
- **Trọng tâm kiểm định:**
  - Xác thực thành công 22 quyền được cấp phép (`GRANT`).
  - Kiểm tra bẫy lỗi thành công 3 trường hợp bị từ chối (`DENY`):
    1. `role_HRManager` cố tình gọi `sp_TinhLuongThang` $\rightarrow$ Ném lỗi `The EXECUTE permission was denied`.
    2. `role_PayrollOfficer` cố tình UPDATE họ tên nhân viên $\rightarrow$ Ném lỗi `The UPDATE permission was denied`.
    3. `role_Employee` cố tình SELECT bảng `CHITIETBANGLUONG` $\rightarrow$ Ném lỗi `The SELECT permission was denied`.
  - Xác thực Row-Level Security: Tài khoản `gui_a` chỉ thấy 1 dòng của nhân viên A, hoàn toàn không thấy dòng của nhân viên B.

---

### 5.3 Kết quả Kiểm thử Giao diện (GuiQaRegressionTest - 88 Assertions)

- Tự động hóa kiểm tra toàn bộ vòng đời giao diện PeopleOS:
  - Khởi tạo form đăng nhập không có ngoại lệ đồ họa.
  - Kiểm tra cơ chế focus và phím tắt (Enter đăng nhập, Tab chuyển trường).
  - Kiểm tra xóa sạch mật khẩu khỏi bộ nhớ sau khi đăng nhập.
  - Kiểm tra tính năng Quick Login: 1-click nạp tài khoản, double-click kích hoạt đăng nhập tức thì.
  - Kiểm tra xử lý timeout khi mất kết nối mạng SQL Server.

---

### 5.4 Thử nghiệm Đồng thời 2 Phiên thực tế trong SSMS

Kịch bản thực nghiệm đo lường khóa thực tế được lưu tại [`Bo_nop_Nhom06/database/test_concurrency_demo_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/Bo_nop_Nhom06/database/test_concurrency_demo_TV5.sql):
- **Phiên 1 (Session ID 59):** Bắt đầu Transaction chốt lương, giữ khóa `X (Exclusive)` trên dòng bảng lương.
- **Phiên 2 (Session ID 55):** Cố gắng can thiệp sửa đổi ngày công của tháng đó.
- **Kết quả ghi nhận:**
  - Phiên 2 rơi vào trạng thái chờ (`LCK_M_U` / `LCK_M_X`).
  - Thời gian chờ đo lường thực tế: Công (1.10s), Phụ cấp (1.04s), Khấu trừ (1.04s), Chi tiết (1.03s).
  - Khi Phiên 1 Commit, Phiên 2 lập tức bị từ chối bởi Trigger bảo vệ. Không xảy ra Deadlock, không có dữ liệu sai lệch.

---

# CHƯƠNG 6. KỊCH BẢN THUYẾT TRÌNH & BỘ CÂU HỎI VẤN ĐÁP CÁ NHÂN

### 6.1 Kịch bản Báo cáo Trực tiếp trên Phần mềm (Live Demo Script)

Khi chuyển sang phần báo cáo cá nhân của TV5 (khoảng 3 – 5 phút), bạn nên trình bày mạch lạc theo 4 bước sau:

#### Bước 1: Giới thiệu Kiến trúc & Đăng nhập nhanh đa vai trò (1 phút)
- *"Kính thưa Thầy/Cô, em là Trần Đức Anh, phụ trách phân hệ Kiến trúc 3 tầng, Bảo mật và Xử lý đồng thời. Trên màn hình là giao diện đăng nhập PeopleOS. Để phục vụ việc demo trực quan, em đã thiết kế thanh Quick Login 5 tài khoản tương ứng 4 Database Roles."*
- **Hành động:** Nhấp đúp vào nút **Admin** $\rightarrow$ Hệ thống lập tức vào Dashboard Quản trị.

#### Bước 2: Demo Bảo mật 4 Roles & Row-Level Security (1.5 phút)
- Mở màn hình **Quản trị Tài khoản**:
  - *"Hệ thống áp dụng bảo mật 2 lớp: Mật khẩu được băm PBKDF2 600.000 vòng ở tầng Java, và mỗi tài khoản được ánh xạ tới một SQL Login thực tế tại SQL Server."*
- Đăng xuất $\rightarrow$ Nhấp đúp vào nút **Nhân viên A (730k)**:
  - Mở màn hình **Phiếu lương cá nhân**:
  - *"Nhân viên A chỉ nhìn thấy duy nhất phiếu lương của mình (Thực nhận 730.000đ). Nhờ cơ chế Row-Level Security với `ORIGINAL_LOGIN()`, ngay cả khi sửa mã nguồn ở client, SQL Server vẫn cô lập dữ liệu tuyệt đối giữa các nhân viên."*

#### Bước 3: Demo Quy trình Chốt kỳ lương & Trigger Khóa dữ liệu (1.5 phút)
- Đăng nhập tài khoản **Kế toán (gui_payroll)**:
  - Mở màn hình **Báo cáo & Chốt lương Tháng 10/2026**:
  - Cho thấy trạng thái hiện tại là `CHUA_CHOT`.
  - Nhấn nút **"Chốt kỳ lương"** $\rightarrow$ Trạng thái chuyển thành `DA_CHOT` màu xanh, ghi nhận ngày giờ chốt.
- Đăng xuất $\rightarrow$ Đăng nhập tài khoản **HR Manager (gui_hr)**:
  - Mở màn hình **Chấm công** hoặc **Phụ cấp**:
  - Thử thêm mới hoặc sửa 1 bản ghi chấm công của Tháng 10 $\rightarrow$ **Hệ thống bật thông báo lỗi từ Trigger:** *"Kỳ lương Tháng 10/2026 đã được chốt sổ. Không được phép chỉnh sửa dữ liệu!"*
  - *"Điều này chứng minh dữ liệu tài chính của doanh nghiệp đã được đóng băng toàn vẹn."*

#### Bước 4: Tóm tắt Tối ưu hóa Index & Bộ kiểm thử (1 phút)
- Mở Slide 17:
  - *"Về tối ưu hiệu năng, em đã thực nghiệm benchmark trên 10.000 dòng: việc chuyển từ Clustered Scan sang Nonclustered Seek với Covering Index giúp giảm từ 142 reads xuống 15 reads, tức giảm 89.7% I/O và triệt tiêu hoàn toàn Key Lookup. Toàn bộ logic này được bảo chứng bởi bộ kiểm thử tự động 257 assertions đạt 100% Pass."*

---

### 6.2 Trả lời Câu hỏi Phản biện của Giảng viên (Hội đồng Q&A)

Dưới đây là các câu hỏi giảng viên rất hay hỏi và câu trả lời chuẩn xác nhất:

#### Câu 1: "Tại sao em lại dùng PBKDF2 mà không dùng MD5 hay SHA-256 thông thường?"
> **Trả lời:** *"Dạ thưa Cô, MD5 và SHA-256 thông thường là các hàm băm nhanh (fast hash), sinh ra để kiểm tra tính toàn vẹn tệp tin. Khi dùng cho mật khẩu, kẻ tấn công có thể dùng card đồ họa (GPU) để thử hàng tỷ mật khẩu mỗi giây bằng bảng tính sẵn (Rainbow Table). Ngược lại, PBKDF2-HMAC-SHA256 là hàm băm chậm (slow key derivation function) chuẩn NIST, áp dụng muối ngẫu nhiên 16 bytes và lặp lại 600.000 vòng tính toán, làm tăng độ phức tạp tính toán lên gấp hàng triệu lần, khiến việc brute-force mật khẩu là bất khả thi về mặt thời gian và chi phí."*

#### Câu 2: "Row-Level Security của em được cài đặt như thế nào và tại sao em dùng ORIGINAL_LOGIN() thay vì SUSER_SNAME()?"
> **Trả lời:** *"Dạ thưa Cô, em cài đặt RLS thông qua View `vw_PhieuLuongCaNhan`. Trong mệnh đề WHERE, em kiểm tra `tk.SqlLogin = ORIGINAL_LOGIN()`. Em chọn `ORIGINAL_LOGIN()` vì hàm này trả về danh tính của SQL Login kết nối ban đầu từ client, không bị thay đổi ngay cả khi trong phiên làm việc có lệnh `EXECUTE AS` chuyển ngữ cảnh. Điều này đảm bảo tính xác thực nguyên bản từ tầng kết nối, ngăn ngừa triệt để lỗ hổng leo thang đặc quyền."*

#### Câu 3: "Khi 2 phiên cùng chốt lương hoặc 1 phiên chốt lương và 1 phiên sửa công diễn ra cùng lúc, hệ thống xử lý ra sao?"
> **Trả lời:** *"Dạ thưa Cô, trong thủ tục `sp_ChotBangLuong`, em sử dụng Transaction kết hợp gợi ý khóa `WITH (UPDLOCK, ROWLOCK)`. Khi phiên chốt lương thực thi, nó giữ khóa cập nhật trên dòng bảng lương đó. Phiên sửa công muốn can thiệp sẽ phải chờ phiên chốt lương hoàn tất. Khi phiên chốt lương commit và đổi trạng thái sang `DA_CHOT`, phiên sửa công bước vào sẽ bị Trigger `trg_ChamCong_ChanSuaKyDaChot` chặn lại và rollback ngay lập tức. Nhờ đó hệ thống loại bỏ hoàn toàn hiện tượng ghi đè dữ liệu bẩn và bảo toàn 100% tính nhất quán."*

#### Câu 4: "Tại sao trong Execution Plan sau khi tối ưu Index Seek lại không có toán tử Key Lookup?"
> **Trả lời:** *"Dạ thưa Cô, đó là vì em đã áp dụng kỹ thuật **Covering Index**. Trong câu lệnh tạo Nonclustered Index, ngoài việc đánh chỉ mục trên các cột điều kiện lọc `(MaPB, MaCV)`, em đưa toàn bộ các cột cần hiển thị trong mệnh đề SELECT (`HoTen, LuongCoBan, TrangThai`) vào mệnh đề `INCLUDE (...)`. Khi đó, dữ liệu của các cột này nằm sẵn ở tầng lá (Leaf pages) của Nonclustered Index, giúp SQL Server lấy được toàn bộ dữ liệu ngay lập tức mà không cần tốn thêm bất kỳ thao tác tra cứu ngược (Bookmark/Key Lookup) nào sang Clustered Index."*

---

> **KẾT LUẬN:** Báo cáo chuyên đề này đã tổng hợp toàn diện và đầy đủ 100% khối lượng công việc của Thành viên 5 (Trần Đức Anh), đáp ứng xuất sắc các tiêu chí chấm điểm khắt khe nhất của học phần Hệ Quản Trị Cơ Sở Dữ Liệu.
