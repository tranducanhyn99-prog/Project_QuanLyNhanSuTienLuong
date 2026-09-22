# Source Code – Quản lý Nhân sự và Tiền lương
## Nhóm 06 – DBMS330284

Thư mục `src/` chứa toàn bộ mã nguồn ứng dụng Java Swing.

## Kiến trúc phân lớp

```
Presentation (Java Swing UI)
        ↓
Service Layer (Business Logic)
        ↓
DAO Layer (Data Access / JDBC)
        ↓
SQL Server (Tables, SP, Trigger, View, Function)
```

## Cấu trúc thư mục

```
src/
├── main/java/com/nhom06/
│   ├── config/         # DatabaseConnection, AppConfig
│   ├── model/          # Java bean classes (NhanVien, TaiKhoan, ...)
│   ├── dao/            # Data Access Objects (gọi SP/Query SQL Server)
│   ├── service/        # Business logic layer
│   ├── ui/
│   │   ├── auth/       # LoginFrame
│   │   ├── main/       # MainFrame
│   │   ├── nhanvien/   # NhanVienPanel, DanhMucPanel
│   │   ├── chamcong/   # ChamCongPanel
│   │   ├── luong/      # BangLuongPanel
│   │   └── baocao/     # BaoCaoPanel
│   ├── session/        # Session singleton (lưu thông tin đăng nhập)
│   └── util/           # PasswordUtil, ValidationUtil, MessageUtil
└── resources/
    └── config.properties.template   # Template cấu hình JDBC
```

## Cài đặt môi trường lần đầu

### 1. Yêu cầu

- **JDK:** 11 trở lên (khuyến nghị JDK 17 LTS)
- **SQL Server:** 2019 hoặc 2022
- **IDE:** IntelliJ IDEA / Eclipse

### 2. Cấu hình kết nối CSDL

```bash
# Sao chép template
copy src\resources\config.properties.template src\resources\config.properties

# Chỉnh sửa config.properties với thông tin SQL Server của bạn
```

Nội dung `config.properties`:
```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSuTienLuong;encrypt=false
db.user=YOUR_SQL_SERVER_USER
db.password=YOUR_SQL_SERVER_PASSWORD
```

> **QUAN TRỌNG:** File `config.properties` đã được thêm vào `.gitignore`. **Không commit file này.**

### 3. Thêm JDBC Driver

- Tải `mssql-jdbc-12.x.x.jre11.jar` từ [Microsoft JDBC Driver](https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server)
- Đặt vào thư mục `lib/` ở root project
- Thêm vào classpath của IDE

### 4. Khởi tạo CSDL

```sql
-- Chạy các script theo thứ tự trong thư mục database/
-- (xem hướng dẫn trong database/README.md)
```

## Quy ước code

| Quy tắc | Chi tiết |
|---|---|
| **Encoding** | UTF-8 toàn bộ |
| **Naming** | CamelCase cho class/method; UPPER_SNAKE cho hằng số |
| **SQL** | Dùng `CallableStatement` cho SP; `PreparedStatement` cho query |
| **Exception** | DAO ném `SQLException`; Service/UI bắt và hiển thị thông báo |
| **Password** | Luôn hash SHA-256 trước khi so sánh hoặc lưu |

## Phân công module

| Thành viên | Module Java chính |
|---|---|
| TV1 – Nguyễn Minh Trí | `NhanVienPanel`, `DanhMucPanel`, `NhanVienService`, `NhanVienDAO` |
| TV2 – Phạm Minh Quân | `ChamCongPanel`, `ChamCongService`, `ChamCongDAO` |
| TV3 – Trần Tiến Đạt | Module Phụ cấp/Khấu trừ (Panel + Service + DAO) |
| TV4 – Nguyễn Quang Vinh | `BangLuongPanel` (phần tính lương), `PayrollService`, `BangLuongDAO` |
| TV5 – Trần Đức Anh | `LoginFrame`, `MainFrame`, `Session`, `AuthService`, `BaoCaoPanel`, Security |
