# TV5 – Kịch bản Concurrency & Security Demo
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Trần Đức Anh (TV5, MSSV 24110155)  
**Ngày:** 21/09/2026  
**Phiên bản:** 1.0

---

## PHẦN A – KỊCH BẢN CONCURRENCY

### A.1 Bối cảnh vấn đề

**Tình huống:** Hai `Payroll_Officer` cùng lúc thao tác tính/chốt bảng lương tháng 9/2026.

- **PO-1 (Nguyễn Văn A):** đang tính bảng lương tháng 9/2026 (gọi `sp_TinhBangLuongThang`)
- **PO-2 (Trần Thị B):** cũng đang tính bảng lương tháng 9/2026 ở một session khác

**Vấn đề nếu không kiểm soát:**

| Vấn đề | Mô tả |
|---|---|
| **Duplicate insert** | Cả hai cùng INSERT vào `BANGLUONG` với cùng Tháng/Năm → vi phạm UNIQUE constraint |
| **Dirty read** | PO-2 đọc dữ liệu chưa commit của PO-1, tính lương trên dữ liệu chưa chính xác |
| **Lost update** | PO-1 và PO-2 cùng UPDATE `TrangThai` của `BANGLUONG` → kết quả cuối chỉ lưu của 1 người |
| **Double chốt** | Cả hai cùng gọi `sp_ChotBangLuong` → kỳ lương bị chốt 2 lần |

---

### A.2 Timeline chi tiết (Không có kiểm soát)

```
Thời gian │ Session PO-1 (sp_TinhBangLuongThang)    │ Session PO-2 (sp_TinhBangLuongThang)
──────────┼──────────────────────────────────────────┼──────────────────────────────────────
T = 0ms   │ BEGIN TRANSACTION                        │
T = 10ms  │ SELECT * FROM BANGLUONG WHERE Thang=9... │
          │   → Kết quả: 0 dòng (chưa có kỳ lương)  │
T = 20ms  │                                          │ BEGIN TRANSACTION
T = 30ms  │                                          │ SELECT * FROM BANGLUONG WHERE Thang=9...
          │                                          │   → Kết quả: 0 dòng (PO-1 chưa commit)
T = 40ms  │ INSERT INTO BANGLUONG (Thang=9,Nam=2026) │
T = 50ms  │                                          │ INSERT INTO BANGLUONG (Thang=9,Nam=2026)
          │                                          │   → ??? Có thể thành công nếu PO-1 chưa commit
T = 60ms  │ COMMIT                                   │
T = 70ms  │                                          │ COMMIT
          │                                          │   → Hoặc lỗi UNIQUE violation
──────────┴──────────────────────────────────────────┴──────────────────────────────────────
KẾT QUẢ: Không xác định — phụ thuộc timing. Có thể lỗi hoặc duplicate bị bỏ qua.
```

---

### A.3 Giải pháp đề xuất

#### Giải pháp 1: UNIQUE Constraint + TRY…CATCH (Cơ bản)

```sql
-- Trong BANGLUONG:
CONSTRAINT UQ_BANGLUONG_ThangNam UNIQUE (Thang, Nam)

-- Trong sp_TinhBangLuongThang:
BEGIN TRY
    BEGIN TRANSACTION
        -- Nếu PO-2 cũng chạy cùng lúc, UNIQUE constraint sẽ reject 1 trong 2
        INSERT INTO BANGLUONG (Thang, Nam, TrangThai, ...)
        VALUES (@Thang, @Nam, 'CHUA_CHOT', ...)
        ...
    COMMIT TRANSACTION
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION
    -- Nếu lỗi 2627 (UNIQUE violation) → thông báo "Kỳ lương đã được tạo bởi người khác"
    DECLARE @msg NVARCHAR(500) = ERROR_MESSAGE()
    RAISERROR(@msg, 16, 1)
END CATCH
```

#### Giải pháp 2: UPDLOCK + SERIALIZABLE (Nâng cao — dùng cho chốt lương)

```sql
-- Trong sp_ChotBangLuong:
BEGIN TRY
    BEGIN TRANSACTION

        -- Khóa dòng BANGLUONG với UPDLOCK để ngăn session khác cũng LOCK cùng dòng
        SELECT @TrangThai = TrangThai
        FROM BANGLUONG WITH (UPDLOCK, HOLDLOCK)
        WHERE MaBangLuong = @MaBangLuong

        IF @TrangThai = 'DA_CHOT'
        BEGIN
            RAISERROR(N'Kỳ lương đã được chốt. Không thể chốt lại.', 16, 1)
            RETURN
        END

        -- Chốt bảng lương
        UPDATE BANGLUONG
        SET TrangThai = 'DA_CHOT', NgayChot = GETDATE()
        WHERE MaBangLuong = @MaBangLuong

    COMMIT TRANSACTION
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION
    RAISERROR(ERROR_MESSAGE(), 16, 1)
END CATCH
```

---

### A.4 Timeline sau khi áp dụng giải pháp

```
Thời gian │ Session PO-1 (sp_ChotBangLuong)             │ Session PO-2 (sp_ChotBangLuong)
──────────┼──────────────────────────────────────────────┼────────────────────────────────────
T = 0ms   │ BEGIN TRANSACTION                            │
T = 10ms  │ SELECT ... FROM BANGLUONG WITH (UPDLOCK)     │
          │   → Lấy UPDLOCK trên dòng MaBangLuong=1      │
T = 20ms  │                                              │ BEGIN TRANSACTION
T = 30ms  │                                              │ SELECT ... FROM BANGLUONG WITH (UPDLOCK)
          │                                              │   → BLOCKED! Chờ PO-1 release lock
T = 40ms  │ UPDATE BANGLUONG SET TrangThai='DA_CHOT'...  │   (vẫn đang chờ)
T = 50ms  │ COMMIT TRANSACTION ✓                         │   (vẫn đang chờ)
T = 60ms  │                                              │   → Lock được release
          │                                              │ SELECT ... → TrangThai = 'DA_CHOT'
          │                                              │ → RAISERROR("Kỳ lương đã được chốt") ✓
T = 70ms  │                                              │ ROLLBACK TRANSACTION
──────────┴──────────────────────────────────────────────┴────────────────────────────────────
KẾT QUẢ: PO-1 thành công. PO-2 nhận thông báo lỗi rõ ràng. Dữ liệu nhất quán.
```

---

### A.5 Kịch bản demo cho GVHD (Tuần 3)

**Bước thực hiện:**

1. Mở 2 cửa sổ SQL Server Management Studio (SSMS), login bằng 2 user `Payroll_Officer` khác nhau.
2. Trên **SSMS-1**: chạy `BEGIN TRANSACTION` → `SELECT với UPDLOCK` → **PAUSE** (chưa COMMIT).
3. Trên **SSMS-2**: chạy `sp_ChotBangLuong` → quan sát **BLOCKED** (session đang chờ).
4. Trên **SSMS-1**: chạy `COMMIT` → quan sát **SSMS-2** nhận lỗi "Kỳ lương đã được chốt".
5. Chụp ảnh màn hình `sys.dm_exec_requests` trong lúc block để minh chứng.

```sql
-- Query kiểm tra blocking (chạy trong SSMS-3)
SELECT
    r.session_id,
    r.blocking_session_id,
    r.wait_type,
    r.wait_time,
    r.status,
    t.text AS sql_text
FROM sys.dm_exec_requests r
CROSS APPLY sys.dm_exec_sql_text(r.sql_handle) t
WHERE r.blocking_session_id > 0;
```

---

## PHẦN B – KỊCH BẢN SECURITY DEMO

### B.1 Danh sách 6 tình huống GRANT / REVOKE / DENY

---

#### Tình huống 1: HR_Manager KHÔNG được tính lương

**Mục tiêu:** Minh họa DENY đúng chức năng.

```sql
-- Setup: HR_Manager đã bị DENY EXECUTE sp_TinhBangLuongThang
-- Demo:
EXECUTE AS USER = 'user_HRManager';
    EXEC sp_TinhBangLuongThang @Thang=9, @Nam=2026, @MaBL=1;
    -- Kết quả mong đợi: Msg 229, Level 14 - EXECUTE permission denied
REVERT;
```

**Kết quả mong đợi:** `The EXECUTE permission was denied on the object 'sp_TinhBangLuongThang'`

---

#### Tình huống 2: Employee chỉ xem được phiếu lương qua View

**Mục tiêu:** Employee không truy cập trực tiếp bảng `CHITIETBANGLUONG`.

```sql
EXECUTE AS USER = 'user_Employee';
    -- Thử đọc bảng trực tiếp → DENIED
    SELECT * FROM CHITIETBANGLUONG;
    -- Kết quả: Permission denied

    -- Đọc qua View → OK (nếu View chứa dữ liệu của nhân viên đó)
    SELECT * FROM vw_BangLuongChiTiet;
    -- Kết quả: Hiển thị phiếu lương
REVERT;
```

---

#### Tình huống 3: Payroll_Officer KHÔNG được sửa hồ sơ nhân viên

```sql
EXECUTE AS USER = 'user_PayrollOfficer';
    UPDATE NHANVIEN SET LuongCoBan = 20000000 WHERE MaNV = 1;
    -- Kết quả mong đợi: UPDATE permission denied
REVERT;
```

---

#### Tình huống 4: REVOKE quyền sau khi GRANT (live demo)

**Mục tiêu:** Minh họa quyền có thể thu hồi động.

```sql
-- Bước 1: GRANT quyền tạm thời
GRANT SELECT ON NHANVIEN TO role_Employee;

EXECUTE AS USER = 'user_Employee';
    SELECT * FROM NHANVIEN;  -- Thành công
REVERT;

-- Bước 2: REVOKE
REVOKE SELECT ON NHANVIEN FROM role_Employee;

EXECUTE AS USER = 'user_Employee';
    SELECT * FROM NHANVIEN;  -- Permission denied
REVERT;
```

---

#### Tình huống 5: DENY override GRANT (DENY ưu tiên hơn GRANT)

**Mục tiêu:** Minh họa nguyên tắc DENY > GRANT trong SQL Server.

```sql
-- Dù role_PayrollOfficer có GRANT SELECT trên NHANVIEN,
-- nhưng nếu DENY tường minh thì vẫn bị từ chối
DENY SELECT ON NHANVIEN TO user_PayrollOfficer;  -- DENY trực tiếp trên user

EXECUTE AS USER = 'user_PayrollOfficer';
    SELECT * FROM NHANVIEN;  -- Permission denied (dù role có GRANT)
REVERT;

-- Cleanup
REVOKE SELECT ON NHANVIEN FROM user_PayrollOfficer;
```

---

#### Tình huống 6: Tài khoản bị khóa

**Mục tiêu:** Minh họa cơ chế khóa tài khoản ở tầng ứng dụng.

```sql
-- Admin khóa tài khoản
UPDATE TAIKHOAN SET TrangThai = 'KHOA' WHERE TenDangNhap = 'nhanvien01';

-- Ứng dụng Java: khi login, AuthService kiểm tra TrangThai
-- Nếu 'KHOA' → throw new Exception("Tài khoản đã bị khóa. Liên hệ quản trị viên.");
-- LoginFrame hiển thị thông báo lỗi.
```

---

### B.2 Script tổng hợp để chạy demo nhanh

```sql
-- Chạy trước buổi demo để reset về trạng thái sạch
USE QuanLyNhanSuTienLuong;

-- Xác nhận quyền của từng role
SELECT
    dp.name          AS role_name,
    o.name           AS object_name,
    o.type_desc      AS object_type,
    p.permission_name,
    p.state_desc     AS grant_deny
FROM sys.database_permissions p
JOIN sys.database_principals dp ON p.grantee_principal_id = dp.principal_id
JOIN sys.objects o              ON p.major_id = o.object_id
WHERE dp.name IN ('role_HRManager','role_PayrollOfficer','role_Employee','role_DBAdmin')
ORDER BY dp.name, o.name, p.permission_name;
```

---

## PHẦN C – GHI CHÚ THỰC HIỆN

| Hạng mục | Tuần thực hiện | Người chịu trách nhiệm |
|---|---|---|
| Thiết kế kịch bản (tài liệu này) | Tuần 1 | TV5 |
| Cài đặt SP với UPDLOCK/TRY…CATCH | Tuần 2 | TV5 |
| Test concurrency (2 SSMS session) | Tuần 2 | TV5 phối hợp TV4 |
| Script GRANT/REVOKE/DENY hoàn chỉnh | Tuần 2 | TV5 |
| Chụp ảnh/video minh chứng cho báo cáo | Tuần 3 | TV5 |
| Diễn giải trong phần thuyết trình | Tuần 3 | TV5 |
