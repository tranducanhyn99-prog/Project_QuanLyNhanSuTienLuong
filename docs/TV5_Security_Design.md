# TV5 – Thiết kế Bảo mật và Phân quyền
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Trần Đức Anh (TV5, MSSV 24110155)  
**Ngày:** 21/09/2026  
**Phiên bản:** 1.0

---

## 1. Thiết kế bảng TAIKHOAN

### 1.1 Cấu trúc bảng

```sql
CREATE TABLE TAIKHOAN (
    MaTK          INT           IDENTITY(1,1)  PRIMARY KEY,
    MaNV          INT           NULL,           -- NULL nếu là DB_Admin hệ thống
    TenDangNhap   VARCHAR(50)   NOT NULL        UNIQUE,
    MatKhau       CHAR(64)      NOT NULL,       -- SHA-256 hex string (64 ký tự)
    VaiTro        VARCHAR(30)   NOT NULL
        CONSTRAINT CHK_TAIKHOAN_VaiTro
            CHECK (VaiTro IN ('DB_Admin','HR_Manager','Payroll_Officer','Employee')),
    TrangThai     VARCHAR(10)   NOT NULL        DEFAULT 'HOATDONG'
        CONSTRAINT CHK_TAIKHOAN_TrangThai
            CHECK (TrangThai IN ('HOATDONG','KHOA')),
    NgayTao       DATE          NOT NULL        DEFAULT GETDATE(),
    NgaySuaCuoi   DATETIME      NULL,

    CONSTRAINT FK_TAIKHOAN_NHANVIEN
        FOREIGN KEY (MaNV) REFERENCES NHANVIEN(MaNV)
);
```

### 1.2 Mô tả từng cột

| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `MaTK` | INT IDENTITY | PK | Mã tài khoản, tự tăng |
| `MaNV` | INT | FK → NHANVIEN, NULL | NULL cho tài khoản `DB_Admin` hệ thống |
| `TenDangNhap` | VARCHAR(50) | UNIQUE, NOT NULL | Username đăng nhập, không trùng |
| `MatKhau` | CHAR(64) | NOT NULL | Chuỗi hex SHA-256 của mật khẩu (64 ký tự) |
| `VaiTro` | VARCHAR(30) | CHECK | Một trong 4 giá trị cố định |
| `TrangThai` | VARCHAR(10) | CHECK, DEFAULT | `HOATDONG` hoặc `KHOA` |
| `NgayTao` | DATE | DEFAULT GETDATE() | Ngày tạo tài khoản |
| `NgaySuaCuoi` | DATETIME | NULL | Thời điểm đổi mật khẩu/sửa lần cuối |

### 1.3 Quy tắc Hash mật khẩu (SHA-256)

```java
// PasswordUtil.java
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    public static String hashSHA256(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainText.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString(); // 64 ký tự hex
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hash mật khẩu", e);
        }
    }
}
```

**Sử dụng:**
- **Tạo tài khoản:** `matKhauHash = PasswordUtil.hashSHA256(matKhauNhapVao)`
- **Đăng nhập:** so sánh `PasswordUtil.hashSHA256(matKhauNhapVao)` với `MatKhau` trong DB

---

## 2. Bốn Database Role trên SQL Server

### 2.1 Danh sách Role và mô tả

| Role SQL Server | Vai trò nghiệp vụ | Mô tả |
|---|---|---|
| `role_DBAdmin` | DB_Admin | Quản trị viên hệ thống — toàn quyền |
| `role_HRManager` | HR_Manager | Quản lý nhân sự — quản lý nhân viên, phòng ban, chấm công |
| `role_PayrollOfficer` | Payroll_Officer | Nhân viên kế toán lương — quản lý phụ cấp, tính lương, chốt lương |
| `role_Employee` | Employee | Nhân viên — chỉ xem phiếu lương cá nhân |

### 2.2 Kịch bản tạo Login và gán Role (SQL Server)

```sql
-- =============================================
-- TẠO 4 LOGIN SQL SERVER
-- =============================================
CREATE LOGIN login_DBAdmin        WITH PASSWORD = 'Admin@2026!';
CREATE LOGIN login_HRManager      WITH PASSWORD = 'HR@2026!';
CREATE LOGIN login_PayrollOfficer WITH PASSWORD = 'Payroll@2026!';
CREATE LOGIN login_Employee       WITH PASSWORD = 'Emp@2026!';

-- =============================================
-- TẠO 4 DATABASE USER (trong database ứng dụng)
-- =============================================
USE QuanLyNhanSuTienLuong;

CREATE USER user_DBAdmin        FOR LOGIN login_DBAdmin;
CREATE USER user_HRManager      FOR LOGIN login_HRManager;
CREATE USER user_PayrollOfficer FOR LOGIN login_PayrollOfficer;
CREATE USER user_Employee       FOR LOGIN login_Employee;

-- =============================================
-- TẠO 4 DATABASE ROLE
-- =============================================
CREATE ROLE role_DBAdmin;
CREATE ROLE role_HRManager;
CREATE ROLE role_PayrollOfficer;
CREATE ROLE role_Employee;

-- =============================================
-- GÁN USER VÀO ROLE
-- =============================================
ALTER ROLE role_DBAdmin        ADD MEMBER user_DBAdmin;
ALTER ROLE role_HRManager      ADD MEMBER user_HRManager;
ALTER ROLE role_PayrollOfficer ADD MEMBER user_PayrollOfficer;
ALTER ROLE role_Employee       ADD MEMBER user_Employee;
```

---

## 3. Ma trận phân quyền CSDL (SQL Server)

### 3.1 Quyền trên bảng

| Bảng | role_DBAdmin | role_HRManager | role_PayrollOfficer | role_Employee |
|---|:---:|:---:|:---:|:---:|
| `PHONGBAN` | ALL | SELECT, INSERT, UPDATE | SELECT | — |
| `CHUCVU` | ALL | SELECT, INSERT, UPDATE | SELECT | — |
| `NHANVIEN` | ALL | SELECT, INSERT, UPDATE | SELECT | SELECT (chỉ bản thân) |
| `TAIKHOAN` | ALL | — | — | SELECT (chỉ bản thân) |
| `CHAMCONG` | ALL | SELECT, INSERT, UPDATE | SELECT | SELECT (chỉ bản thân) |
| `PHUCAPNHANVIEN` | ALL | SELECT, INSERT, UPDATE | SELECT, INSERT, UPDATE | — |
| `KHAUTRUNHANVIEN` | ALL | SELECT, INSERT, UPDATE | SELECT, INSERT, UPDATE | — |
| `BANGLUONG` | ALL | SELECT | SELECT, INSERT | SELECT |
| `CHITIETBANGLUONG` | ALL | SELECT | SELECT, INSERT | SELECT (chỉ bản thân) |

> **Ghi chú:** `ALL` = SELECT + INSERT + UPDATE + DELETE + ALTER (qua vai trò `db_owner` hoặc GRANT tường minh).  
> `—` = không có quyền (DENY hoặc không GRANT).

### 3.2 Quyền trên Stored Procedure

| Stored Procedure | role_DBAdmin | role_HRManager | role_PayrollOfficer | role_Employee |
|---|:---:|:---:|:---:|:---:|
| `sp_ThemNhanVien` | EXECUTE | EXECUTE | — | — |
| `sp_CapNhatNhanVien` | EXECUTE | EXECUTE | — | — |
| `sp_GhiNhanChamCong` | EXECUTE | EXECUTE | — | — |
| `sp_TinhBangLuongThang` | EXECUTE | — | EXECUTE | — |
| `sp_ChotBangLuong` | EXECUTE | — | EXECUTE | — |

### 3.3 Quyền trên View

| View | role_DBAdmin | role_HRManager | role_PayrollOfficer | role_Employee |
|---|:---:|:---:|:---:|:---:|
| `vw_NhanVien_PhongBan_ChucVu` | SELECT | SELECT | SELECT | — |
| `vw_TongHopChamCongThang` | SELECT | SELECT | SELECT | — |
| `vw_TongPhuCapThang` | SELECT | SELECT | SELECT | — |
| `vw_TongKhauTruThang` | SELECT | SELECT | SELECT | — |
| `vw_BangLuongChiTiet` | SELECT | SELECT | SELECT | SELECT |

### 3.4 Quyền trên Function

| Function | role_DBAdmin | role_HRManager | role_PayrollOfficer | role_Employee |
|---|:---:|:---:|:---:|:---:|
| `fn_TinhSoNgayCong` | EXECUTE | EXECUTE | EXECUTE | — |
| `fn_TongPhuCap` | EXECUTE | EXECUTE | EXECUTE | — |
| `fn_TongKhauTru` | EXECUTE | EXECUTE | EXECUTE | — |
| `fn_TinhTienCong` | EXECUTE | — | EXECUTE | — |
| `fn_TinhThucNhan` | EXECUTE | — | EXECUTE | — |

---

## 4. Script GRANT / REVOKE / DENY chi tiết

```sql
USE QuanLyNhanSuTienLuong;
GO

-- =============================================
-- SECTION 1: role_HRManager
-- =============================================

-- GRANT quyền đọc/ghi nhân sự
GRANT SELECT, INSERT, UPDATE ON PHONGBAN          TO role_HRManager;
GRANT SELECT, INSERT, UPDATE ON CHUCVU            TO role_HRManager;
GRANT SELECT, INSERT, UPDATE ON NHANVIEN          TO role_HRManager;
GRANT SELECT, INSERT, UPDATE ON CHAMCONG          TO role_HRManager;
GRANT SELECT, INSERT, UPDATE ON PHUCAPNHANVIEN    TO role_HRManager;
GRANT SELECT, INSERT, UPDATE ON KHAUTRUNHANVIEN   TO role_HRManager;
GRANT SELECT                  ON BANGLUONG         TO role_HRManager;
GRANT SELECT                  ON CHITIETBANGLUONG  TO role_HRManager;

-- GRANT quyền gọi SP
GRANT EXECUTE ON sp_ThemNhanVien     TO role_HRManager;
GRANT EXECUTE ON sp_CapNhatNhanVien  TO role_HRManager;
GRANT EXECUTE ON sp_GhiNhanChamCong  TO role_HRManager;

-- GRANT quyền đọc View và Function
GRANT SELECT  ON vw_NhanVien_PhongBan_ChucVu  TO role_HRManager;
GRANT SELECT  ON vw_TongHopChamCongThang      TO role_HRManager;
GRANT SELECT  ON vw_TongPhuCapThang           TO role_HRManager;
GRANT SELECT  ON vw_TongKhauTruThang          TO role_HRManager;
GRANT SELECT  ON vw_BangLuongChiTiet          TO role_HRManager;
GRANT EXECUTE ON fn_TinhSoNgayCong            TO role_HRManager;
GRANT EXECUTE ON fn_TongPhuCap                TO role_HRManager;
GRANT EXECUTE ON fn_TongKhauTru               TO role_HRManager;

-- DENY quyền tính/chốt lương
DENY EXECUTE ON sp_TinhBangLuongThang TO role_HRManager;
DENY EXECUTE ON sp_ChotBangLuong      TO role_HRManager;

-- DENY quyền truy cập TAIKHOAN
DENY SELECT, INSERT, UPDATE, DELETE ON TAIKHOAN TO role_HRManager;

GO

-- =============================================
-- SECTION 2: role_PayrollOfficer
-- =============================================

-- GRANT quyền đọc dữ liệu nguồn
GRANT SELECT ON NHANVIEN            TO role_PayrollOfficer;
GRANT SELECT ON CHAMCONG            TO role_PayrollOfficer;
GRANT SELECT, INSERT, UPDATE ON PHUCAPNHANVIEN  TO role_PayrollOfficer;
GRANT SELECT, INSERT, UPDATE ON KHAUTRUNHANVIEN TO role_PayrollOfficer;
GRANT SELECT, INSERT         ON BANGLUONG        TO role_PayrollOfficer;
GRANT SELECT, INSERT         ON CHITIETBANGLUONG TO role_PayrollOfficer;

-- GRANT quyền gọi SP lương
GRANT EXECUTE ON sp_TinhBangLuongThang TO role_PayrollOfficer;
GRANT EXECUTE ON sp_ChotBangLuong      TO role_PayrollOfficer;

-- GRANT quyền View và Function
GRANT SELECT  ON vw_NhanVien_PhongBan_ChucVu  TO role_PayrollOfficer;
GRANT SELECT  ON vw_TongHopChamCongThang      TO role_PayrollOfficer;
GRANT SELECT  ON vw_TongPhuCapThang           TO role_PayrollOfficer;
GRANT SELECT  ON vw_TongKhauTruThang          TO role_PayrollOfficer;
GRANT SELECT  ON vw_BangLuongChiTiet          TO role_PayrollOfficer;
GRANT EXECUTE ON fn_TinhSoNgayCong            TO role_PayrollOfficer;
GRANT EXECUTE ON fn_TongPhuCap                TO role_PayrollOfficer;
GRANT EXECUTE ON fn_TongKhauTru               TO role_PayrollOfficer;
GRANT EXECUTE ON fn_TinhTienCong              TO role_PayrollOfficer;
GRANT EXECUTE ON fn_TinhThucNhan              TO role_PayrollOfficer;

-- DENY quyền quản lý nhân sự
DENY INSERT, UPDATE, DELETE ON NHANVIEN  TO role_PayrollOfficer;
DENY INSERT, UPDATE, DELETE ON PHONGBAN  TO role_PayrollOfficer;
DENY INSERT, UPDATE, DELETE ON CHUCVU    TO role_PayrollOfficer;
DENY SELECT, INSERT, UPDATE, DELETE ON TAIKHOAN TO role_PayrollOfficer;

GO

-- =============================================
-- SECTION 3: role_Employee
-- =============================================

-- GRANT chỉ xem phiếu lương cá nhân qua View
GRANT SELECT ON vw_BangLuongChiTiet TO role_Employee;

-- DENY tất cả bảng nghiệp vụ
DENY SELECT, INSERT, UPDATE, DELETE ON NHANVIEN          TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON CHAMCONG          TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON PHUCAPNHANVIEN    TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON KHAUTRUNHANVIEN   TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON BANGLUONG         TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON CHITIETBANGLUONG  TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON TAIKHOAN          TO role_Employee;

GO

-- =============================================
-- SECTION 4: role_DBAdmin — toàn quyền
-- =============================================
-- DB_Admin được thêm vào db_owner role của SQL Server
-- nên không cần GRANT từng đối tượng
EXEC sp_addrolemember 'db_owner', 'role_DBAdmin';
GO
```

---

## 5. Ma trận phân quyền ứng dụng (Java Swing)

Kiểm tra tại `MainFrame` khi build menu, dựa trên `Session.getInstance().getVaiTro()`:

| Chức năng | DB_Admin | HR_Manager | Payroll_Officer | Employee |
|---|:---:|:---:|:---:|:---:|
| **Menu: Nhân viên** | ✓ | ✓ | ✗ | ✗ |
| &emsp;→ Thêm nhân viên | ✓ | ✓ | ✗ | ✗ |
| &emsp;→ Sửa nhân viên | ✓ | ✓ | ✗ | ✗ |
| &emsp;→ Tìm kiếm nhân viên | ✓ | ✓ | ✓ | ✗ |
| **Menu: Danh mục** | ✓ | ✓ | ✗ | ✗ |
| &emsp;→ Phòng ban | ✓ | ✓ | ✗ | ✗ |
| &emsp;→ Chức vụ | ✓ | ✓ | ✗ | ✗ |
| **Menu: Chấm công** | ✓ | ✓ | ✗ | ✗ |
| &emsp;→ Nhập chấm công | ✓ | ✓ | ✗ | ✗ |
| &emsp;→ Xem tổng hợp | ✓ | ✓ | ✓ | ✗ |
| **Menu: Phụ cấp / Khấu trừ** | ✓ | ✓ | ✓ | ✗ |
| **Menu: Lương** | ✓ | ✗ | ✓ | ✗ |
| &emsp;→ Tính bảng lương | ✓ | ✗ | ✓ | ✗ |
| &emsp;→ Chốt bảng lương | ✓ | ✗ | ✓ | ✗ |
| **Menu: Báo cáo** | ✓ | ✓ | ✓ | ✗ |
| &emsp;→ Phiếu lương cá nhân | ✓ | ✓ | ✓ | ✓ |
| **Menu: Quản trị** | ✓ | ✗ | ✗ | ✗ |
| &emsp;→ Quản lý tài khoản | ✓ | ✗ | ✗ | ✗ |
| &emsp;→ GRANT/REVOKE | ✓ | ✗ | ✗ | ✗ |

**Cách implement trong `MainFrame`:**

```java
// Ví dụ ẩn/hiện menu theo role
String role = Session.getInstance().getVaiTro();

menuLuong.setVisible(role.equals("DB_Admin") || role.equals("Payroll_Officer"));
menuNhanVien.setVisible(role.equals("DB_Admin") || role.equals("HR_Manager"));
menuQuanTri.setVisible(role.equals("DB_Admin"));
// Employee chỉ thấy mục "Phiếu lương cá nhân" trong menu Báo cáo
menuBaoCao.setVisible(true);
menuItemPhieuLuong.setVisible(true);
menuItemTongHop.setVisible(!role.equals("Employee"));
```

---

## 6. Nguyên tắc bảo mật bổ sung

| Nguyên tắc | Chi tiết |
|---|---|
| **Kiểm tra quyền 2 tầng** | Tầng ứng dụng ẩn UI; tầng SQL Server DENY/GRANT thực thi |
| **Không lưu mật khẩu rõ** | SHA-256 hash, không bao giờ lưu plaintext |
| **Tài khoản có thể bị khóa** | Cột `TrangThai = 'KHOA'` – login bị từ chối kể cả mật khẩu đúng |
| **Không hardcode credential** | Connection string đọc từ `config.properties`, không commit lên git |
| **PreparedStatement bắt buộc** | Tránh SQL Injection hoàn toàn |
| **Audit trail cơ bản** | Cột `NgaySuaCuoi` trong TAIKHOAN ghi lại lần sửa cuối |
