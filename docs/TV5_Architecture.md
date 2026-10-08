> **Bản cập nhật 08/10/2026:** Các phần kiến trúc và flow bảo mật dưới đây mô tả source hiện tại. Ngày 21/09/2026 và các ảnh/số liệu trong phụ lục là tư liệu lịch sử, không phải kết quả của đợt kiểm chứng hiện tại. Cài đặt danh tính xem [SECURE_SETUP](SECURE_SETUP.md); kết quả kiểm thử có mốc ngày trong [FIX_TASKLIST](FIX_TASKLIST.md).

# TV5 – Thiết kế Kiến trúc Hệ thống
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Trần Đức Anh (TV5, MSSV 24110155)  
**Ngày:** 21/09/2026  
**Phiên bản:** 1.0

---

## 1. Tổng quan kiến trúc

Hệ thống áp dụng kiến trúc **phân lớp (Layered Architecture)** 4 tầng:

```
┌─────────────────────────────────────────────────────────┐
│            PRESENTATION LAYER (Java Swing UI)           │
│   LoginFrame │ MainFrame │ *Panel (NhanVien, ChamCong,  │
│              │           │  BangLuong, BaoCao, DanhMuc) │
└─────────────────────┬───────────────────────────────────┘
                      │  gọi phương thức Service
┌─────────────────────▼───────────────────────────────────┐
│              SERVICE LAYER (Business Logic)             │
│   NhanVienService │ ChamCongService │ PayrollService     │
│   AuthService     │ BaoCaoService   │ DanhMucService     │
└─────────────────────┬───────────────────────────────────┘
                      │  gọi phương thức DAO
┌─────────────────────▼───────────────────────────────────┐
│              DAO LAYER (Data Access Object / JDBC)      │
│   NhanVienDAO │ ChamCongDAO │ BangLuongDAO               │
│   TaiKhoanDAO │ BaoCaoDAO   │ DanhMucDAO                 │
└─────────────────────┬───────────────────────────────────┘
                      │  JDBC Connection (CallableStatement / PreparedStatement)
┌─────────────────────▼───────────────────────────────────┐
│           DATABASE LAYER (Microsoft SQL Server)         │
│   Tables │ Views │ Stored Procedures │ Functions         │
│   Triggers │ Indexes │ Roles │ Transactions              │
└─────────────────────────────────────────────────────────┘
```

### Nguyên tắc kiến trúc

| Nguyên tắc | Mô tả |
|---|---|
| **Separation of Concerns** | UI không chứa SQL; Service không gọi trực tiếp DB |
| **SQL Server là trung tâm** | Logic nghiệp vụ quan trọng (lương, constraint) nằm trong SP/Trigger/Function |
| **DAO dùng tham số hóa** | Dùng `CallableStatement` cho procedure và `PreparedStatement` cho truy vấn; dữ liệu không được nội suy vào SQL |
| **Xử lý lỗi tập trung** | SQLException được bắt ở DAO, ném lên Service, Service ném lên UI để hiển thị |
| **Danh tính cá nhân** | SQL Server xác thực SQL login nhập tại màn hình đăng nhập; không dùng user/password chung trong file cấu hình |
| **Không lưu mật khẩu ứng dụng rõ** | Mật khẩu được xác minh bằng PBKDF2-HMAC-SHA256, salt ngẫu nhiên 16 byte, 600.000 vòng; hash SHA-256 cũ chỉ được đọc/migrate sau xác thực thành công |

---

## 2. Trách nhiệm từng lớp

### 2.1 Presentation Layer (UI)

| Class | Package | Trách nhiệm |
|---|---|---|
| `LoginFrame` | `ui.auth` | Màn hình đăng nhập; gọi `AuthService.login()` |
| `MainFrame` | `ui.main` | Khung chính sau đăng nhập; hiển thị/ẩn menu theo `Session.getVaiTro()` |
| `NhanVienPanel` | `ui.nhanvien` | CRUD nhân viên; gọi `NhanVienService` |
| `DanhMucPanel` | `ui.nhanvien` | Quản lý Phòng ban, Chức vụ; gọi `DanhMucService` |
| `ChamCongPanel` | `ui.chamcong` | Nhập/xem chấm công; gọi `ChamCongService` |
| `BangLuongPanel` | `ui.luong` | Tính lương, chốt lương; gọi `PayrollService` |
| `BaoCaoPanel` | `ui.baocao` | Tra cứu, thống kê; gọi `BaoCaoService` |

**Quy tắc UI:**
- UI chỉ nhận sự kiện và hiển thị dữ liệu.
- Mọi thao tác nghiệp vụ phải đi qua tầng Service.
- Login và DB work chạy trong `SwingWorker`/`DatabaseTask`, không chặn EDT; callback UI chạy trên EDT. Kết quả lỗi thời hoặc thuộc phiên đã logout bị bỏ qua.
- `DatabaseTask.runExclusive` chặn submit lặp cho thao tác ghi/lookup; worker giữ identity token lúc submit và xác thực token trước/sau khi chạy.
- Kết nối đặt login timeout 5 giây, socket timeout 60 giây; truy vấn xác thực profile đặt query timeout 30 giây. Không suy ra mọi DAO query đều có query timeout từ các giá trị này.
- Các button bị `disable` nếu role không có quyền (kiểm tra qua `Session`).

### 2.2 Service Layer (Business Logic)

| Class | Trách nhiệm |
|---|---|
| `AuthService` | Mở SQL connection bằng thông tin người dùng nhập, xác minh mapping/profile và PBKDF2, migrate hash SHA-256 cũ khi hợp lệ |
| `NhanVienService` | Kiểm tra rule trước khi thêm/sửa nhân viên; điều phối transaction |
| `ChamCongService` | Validate dữ liệu chấm công (giờ, ngày, trạng thái NV) trước khi gọi DAO |
| `PhuCapKhauTruService` | Quản lý phụ cấp/khấu trừ; HR_Manager và Payroll_Officer đều có quyền CRUD theo grants |
| `PayrollService` | Điều phối tính lương, chốt lương; kiểm tra kỳ đã tồn tại/chốt |
| `DanhMucService` | CRUD Phòng ban, Chức vụ |
| `BaoCaoService` | Tổng hợp dữ liệu từ View/SP cho báo cáo |

**Quy tắc Service:**
- Service **không** biết đến Swing, `JTable`, `JTextField`.
- Service nhận/trả về **Java model object** hoặc **primitive/List**.
- Khi cần transaction liên span nhiều DAO, Service nhận `Connection` từ `DatabaseConnection`, giao cho DAO.

### 2.3 DAO Layer (Data Access Object)

| Class | Stored Procedure / Query chính |
|---|---|
| `TaiKhoanDAO` | `sp_LayTaiKhoanHienTai`, `sp_MigrateMatKhau`; các thủ tục admin cho reset/trạng thái/role |
| `NhanVienDAO` | `sp_ThemNhanVien`; UPDATE/DELETE dùng `PreparedStatement`, truy vấn danh sách qua `vw_NhanVien_PhongBan_ChucVu` |
| `ChamCongDAO` | `sp_GhiNhanChamCong`, SELECT chấm công theo tháng |
| `BangLuongDAO` | `sp_TinhBangLuongThang`, `sp_ChotBangLuong` |
| `DanhMucDAO` | SELECT/INSERT/UPDATE trực tiếp Phòng ban, Chức vụ |
| `BaoCaoDAO` | Gọi các View: `vw_NhanVien_PhongBan_ChucVu`, `vw_BangLuongChiTiet`, ... |

**Quy tắc DAO:**
- Dùng `CallableStatement` cho Stored Procedure, `PreparedStatement` cho truy vấn động.
- **Tuyệt đối không** dùng `Statement` (tránh SQL Injection).
- Mọi `Connection`, `PreparedStatement`, `ResultSet` phải đóng trong khối `finally` hoặc dùng `try-with-resources`.
- DAO ném `SQLException` lên Service, không tự xử lý im lặng.

### 2.4 Database Layer (SQL Server)

Tất cả logic nghiệp vụ quan trọng đặt tại SQL Server:
- **Constraint:** kiểm tra giá trị hợp lệ (lương > 0, tháng 1–12, v.v.)
- **Trigger:** bảo vệ toàn vẹn nghiệp vụ (không xóa NV đã phát sinh lương, không sửa kỳ đã chốt)
- **Stored Procedure:** logic xử lý dữ liệu với TRY…CATCH và transaction
- **Function:** tính toán thuần túy (tổng phụ cấp, số ngày công, thực nhận)
- **View:** tổng hợp dữ liệu cho báo cáo
- **Index:** tối ưu truy vấn thường xuyên

---

## 3. Cơ chế Session

### 3.1 Thiết kế `Session` (Singleton)

```java
// package: com.nhom06.session
public class Session {

    private static Session instance;

    private int    maTK;
    private int    maNV;         // -1 nếu là DB_Admin không gắn nhân viên
    private String tenDangNhap;
    private String vaiTro;       // "DB_Admin" | "HR_Manager" | "Payroll_Officer" | "Employee"
    private String hoTenNV;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    /** Gọi sau khi login thành công */
    public void login(int maTK, int maNV, String tenDangNhap, String vaiTro, String hoTenNV) { ... }

    /** Gọi khi logout */
    public void logout() {
        maTK = 0; maNV = -1; tenDangNhap = null; vaiTro = null; hoTenNV = null;
    }

    /** Kiểm tra quyền – dùng trong MainFrame để ẩn/hiện menu */
    public boolean hasRole(String... roles) {
        for (String r : roles) if (r.equals(vaiTro)) return true;
        return false;
    }

    // Getters...
}
```

### 3.2 Luồng đăng nhập

```
LoginFrame (SwingWorker; không khóa EDT)
  │── AuthService.login(TenDangNhap, MatKhau)
  │     │── DatabaseConnection.openForLogin(user, password), timeout kết nối 5s
  │     │── SQL Server xác thực SQL login; sp_LayTaiKhoanHienTai tra SqlLogin = ORIGINAL_LOGIN()
  │     │── PasswordUtil.verifyPassword: PBKDF2 salted (legacy SHA-256 được chấp nhận để migrate)
  │     │── Nếu mapping/hash/trạng thái không hợp lệ: logout, không tạo Session/token
  │     │── Nếu hợp lệ: migrate hash legacy nếu cần; giữ credentials và identity token; xóa hash khỏi model
  │     └── Session.getInstance().login(profile)
  └── MainFrame dùng Session để dựng menu; mọi DAO mở connection bằng đúng SQL login đã đăng nhập
```

### 3.3 Kiểm soát menu theo Role

| Menu / Chức năng | DB_Admin | HR_Manager | Payroll_Officer | Employee |
|---|:---:|:---:|:---:|:---:|
| Quản lý Nhân viên / Phòng ban / Chức vụ | ✓ | ✓ | ✗ | ✗ |
| Chấm công | ✓ | ✓ | Đọc/đối soát | ✗ |
| Phụ cấp & Khấu trừ | ✓ | ✓ | ✓ | ✗ |
| Tính / chốt / mở lại lương, xóa kỳ nháp | ✓ | ✗ | ✓ | ✗ |
| Báo cáo tổng hợp | ✓ | ✓ | ✓ | ✗ |
| Phiếu lương cá nhân | ✓ | ✓ | ✓ | Chỉ bản ghi của login hiện tại |
| Quản trị tài khoản trong ứng dụng | ✓ | ✗ | ✗ | ✗ |

UI menu chỉ hỗ trợ điều hướng; SQL Server role và kiểm tra quyền tại service/DAO mới là ranh giới thực thi. `role_DBAdmin` là thành viên `db_owner` của database ứng dụng, không tự cấp quyền `sysadmin` ở server.

---

## 4. Luồng xử lý dữ liệu (Data Flow)

### 4.1 Luồng tổng quát (Sequence)

```
[UI Panel]
    │
    │  1. Người dùng nhấn nút / submit form
    ▼
[Service]
    │  2. Validate dữ liệu đầu vào (null, định dạng, range)
    │  3. Áp dụng business rule (nếu cần kiểm tra CSDL)
    │  4. Gọi DAO tương ứng
    ▼
[DAO]
    │  5. Lấy Connection từ DatabaseConnection
    │  6. Tạo CallableStatement / PreparedStatement
    │  7. Set tham số
    │  8. Thực thi
    ▼
[SQL Server]
    │  9. Stored Procedure / Query chạy
    │  10. Trigger kích hoạt nếu có
    │  11. Trả về kết quả / OUTPUT param / ResultSet
    ▼
[DAO]
    │  12. Map ResultSet → Java model object
    │  13. Đóng resource
    │  14. Trả về kết quả cho Service
    ▼
[Service]
    │  15. Xử lý kết quả (tính toán thêm nếu cần)
    │  16. Trả về cho UI
    ▼
[UI Panel]
    17. Hiển thị kết quả / thông báo
```

### 4.2 Luồng xử lý lỗi

```
[SQL Server] → SQLException (ví dụ: trigger RAISERROR, constraint violation)
    ↓
[DAO]        → catch SQLException → ném lại (hoặc wrap thành AppException)
    ↓
[Service]    → catch Exception → rollback nếu đang trong transaction → ném lại
    ↓
[UI]         → catch Exception → JOptionPane.showMessageDialog(null, e.getMessage(), "Lỗi", ERROR)
```

**Quy tắc thông báo lỗi:**
- Lỗi từ SQL Server (RAISERROR) phải có message tiếng Việt rõ ràng.
- UI không hiển thị stack trace thô cho người dùng cuối.
- Log stack trace ra console (hoặc file log) để debug.

---

## 5. `DatabaseConnection` – Quản lý kết nối

```properties
# config.properties: endpoint only; never put a shared SQL user/password here.
db.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSuTienLuong;encrypt=true;trustServerCertificate=false
```

`DatabaseConnection.openForLogin(user, password)` nhận SQL credential do người dùng nhập. Sau đăng nhập, `getConnection()` mở mỗi request bằng credential đó và chạy `sp_LayTaiKhoanHienTai` để kiểm tra mapping/trạng thái; worker dùng identity token để phát hiện phiên đã đổi.

**Lưu ý:**
- File `config.properties` **không được commit** lên GitHub (đã có trong `.gitignore`).
- Cấu hình chỉ có `db.url`; không khai báo `db.user`/`db.password`. Mỗi request mở SQL connection với danh tính đã xác thực từ LoginFrame.
- Kết nối là **per-request** (mỗi DAO method tự mở/đóng), không dùng connection pool (phù hợp quy mô đồ án).

---

## 6. Cấu trúc thư mục `src/`

```
src/
├── main/
│   └── java/
│       └── com/
│           ├── config/
│           │   ├── DatabaseConnection.java   # Quản lý kết nối JDBC
│           │   └── AppConfig.java            # Hằng số ứng dụng (tên app, version)
│           ├── model/
│           │   ├── NhanVien.java
│           │   ├── PhongBan.java
│           │   ├── ChucVu.java
│           │   ├── TaiKhoan.java
│           │   ├── ChamCong.java
│           │   ├── PhuCapNhanVien.java
│           │   ├── KhauTruNhanVien.java
│           │   ├── BangLuong.java
│           │   └── ChiTietBangLuong.java
│           ├── dao/
│           │   ├── TaiKhoanDAO.java
│           │   ├── NhanVienDAO.java
│           │   ├── PhongBanDAO.java
│           │   ├── ChucVuDAO.java
│           │   ├── ChamCongDAO.java
│           │   ├── PhuCapDAO.java
│           │   ├── KhauTruDAO.java
│           │   ├── BangLuongDAO.java
│           │   └── BaoCaoDAO.java
│           ├── service/
│           │   ├── AuthService.java
│           │   ├── NhanVienService.java
│           │   ├── DanhMucService.java
│           │   ├── ChamCongService.java
│           │   ├── PhuCapKhauTruService.java
│           │   ├── PayrollService.java
│           │   └── BaoCaoService.java
│           ├── ui/
│           │   ├── auth/
│           │   │   └── LoginFrame.java
│           │   ├── main/
│           │   │   └── MainFrame.java
│           │   ├── nhanvien/
│           │   │   ├── NhanVienPanel.java
│           │   │   └── DanhMucPanel.java
│           │   ├── chamcong/
│           │   │   └── ChamCongPanel.java
│           │   ├── luong/
│           │   │   └── BangLuongPanel.java
│           │   └── baocao/
│           │       └── BaoCaoPanel.java
│           ├── session/
│           │   └── Session.java              # Singleton lưu thông tin đăng nhập
│           └── util/
│               ├── PasswordUtil.java          # PBKDF2 và xác minh/migrate legacy SHA-256
│               ├── ValidationUtil.java        # Kiểm tra null, định dạng, range
│               └── MessageUtil.java           # Hiển thị JOptionPane chuẩn
└── resources/
    └── config.properties.template                # Template cấu hình JDBC
```

---

## 7. Phụ lục – Dependency

Dự án chỉ cần **2 JAR bên ngoài**:

| Thư viện | Phiên bản gợi ý | Mục đích |
|---|---|---|
| `mssql-jdbc-*.jar` | 12.x | JDBC Driver kết nối SQL Server |
| *(Không cần thêm)* | — | PBKDF2-HMAC-SHA256 dùng `javax.crypto` trong JDK |

Đặt JAR trong thư mục `lib/` (root project), không commit vào git — thành viên tự tải từ Maven Central.
