> **Cập nhật 08/10/2026:** Các mô tả kiến trúc, định danh và quyền dưới đây theo source/SQL hiện tại. Ngày 21–29/09, ảnh SSMS và các số liệu benchmark trong phần sau là tư liệu lịch sử của lần đo đó, không phải kết quả của lần xác minh hiện tại. Xem [SECURE_SETUP](SECURE_SETUP.md) cho cài đặt/mapping và [FIX_TASKLIST](FIX_TASKLIST.md) cho log, ngày chạy, giới hạn từng kết quả.

# BÁO CÁO KỸ THUẬT ĐỒ ÁN HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU (DBMS330284)
# CHƯƠNG 3: KIẾN TRÚC BẢO MẬT PHÂN QUYỀN 2 TẦNG, KIỂM SOÁT ĐỒNG THỜI (CONCURRENCY CONTROL) VÀ TỐI ƯU HÓA HIỆU NĂNG

---

**Đề tài:** Hệ thống Quản lý Nhân sự và Tiền lương  
**Nhóm thực hiện:** Nhóm 06  
**Thành viên phụ trách nội dung:** Trần Đức Anh (TV5 – Nhóm trưởng)  
**Mã sinh viên:** 24110155  
**Học kỳ / Năm học:** Học kỳ 1 – Năm học 2026-2027  
**Giảng viên hướng dẫn:** Bộ môn Hệ thống Thông tin – Khoa CNTT  

---

## 3.1. ĐẶT VẤN ĐỀ VÀ TỔNG QUAN YÊU CẦU BẢO MẬT, AN TOÀN HỆ THỐNG

### 3.1.1. Bối cảnh và tính cấp thiết
Trong mọi doanh nghiệp hiện đại, dữ liệu về nhân sự và tiền lương luôn thuộc nhóm **dữ liệu nhạy cảm cao nhất (Highly Confidential and Business-Critical Data)**:
1. **Tính riêng tư cá nhân và tuân thủ pháp lý:** Thu nhập cá nhân, bảo hiểm, thuế thu nhập, lịch sử kỷ luật và tài khoản ngân hàng của người lao động nếu bị rò rỉ sẽ dẫn đến mâu thuẫn nội bộ sâu sắc và vi phạm quy định pháp lý về bảo vệ dữ liệu cá nhân.
2. **Tính toàn vẹn tài chính:** Dữ liệu tính lương, phụ cấp, giảm trừ và số tiền thực nhận là căn cứ để công ty chi trả tài chính thực tế. Mọi sự sai lệch dù do tấn công có chủ đích hay do lỗi kỹ thuật tranh chấp dữ liệu đều gây thất thoát tài sản lớn cho tổ chức.
3. **Tính sẵn sàng và độ tin cậy của quy trình chốt kỳ:** Quy trình chốt lương định kỳ (cuối mỗi tháng) thường diễn ra trong khung thời gian hẹp với áp lực giải ngân cao. Hệ thống phải đảm bảo việc chốt bảng lương diễn ra chính xác, không thể xảy ra hiện tượng "chốt đè" (Lost Update), tính lương 2 lần, hoặc người không có thẩm quyền tự ý sửa đổi số liệu sau khi bảng lương đã được ký duyệt.

### 3.1.2. Mục tiêu kỹ thuật
Để giải quyết triệt để các yêu cầu trên, phân hệ do TV5 phụ trách được thiết kế và triển khai nhằm đạt các mục tiêu cốt lõi:
- **Bảo mật phòng thủ chiều sâu (Defense-in-Depth):** Xây dựng mô hình bảo mật 2 tầng chặt chẽ (Tầng ứng dụng Java Swing kết hợp Tầng cơ sở dữ liệu SQL Server).
- **Bảo vệ mật khẩu ứng dụng:** PBKDF2-HMAC-SHA256 với salt ngẫu nhiên, 600.000 vòng; SHA-256 chỉ hỗ trợ xác minh/migrate dữ liệu legacy sau khi xác thực SQL thành công.
- **Kiểm soát đồng thời:** Transaction và `UPDLOCK, HOLDLOCK` tuần tự hóa thao tác chốt cùng một kỳ; kết quả xác minh áp dụng cho các race case ghi trong log, không khẳng định triệt tiêu mọi race trên toàn hệ thống.
- **Ràng buộc toàn vẹn dữ liệu bất biến (Immutability):** Đảm bảo chi tiết bảng lương một khi đã chốt thì không một ai (kể cả quản trị viên nếu không mở khóa hợp lệ) có thể sửa đổi hay xóa bỏ.
- **Tối ưu hóa hiệu năng truy vấn:** Thiết lập cấu trúc chỉ mục che phủ (Covering Index) giúp giảm tải I/O và tăng tốc độ tìm kiếm nhân viên, tổng hợp lương lên gấp nhiều lần.

---

## 3.2. KIẾN TRÚC BẢO MẬT HAI TẦNG (TWO-TIER SECURITY ARCHITECTURE)

Khác với các ứng dụng sinh viên truyền thống chỉ phân quyền bằng cách ẩn/hiện nút bấm trên giao diện (vốn rất dễ bị vượt qua nếu người dùng có công cụ can thiệp hoặc truy cập trực tiếp vào CSDL), hệ thống áp dụng mô hình **Bảo mật Hai tầng Độc lập**:

```
+-------------------------------------------------------------------------+
|                  TẦNG 1: GIAO DIỆN ỨNG DỤNG (JAVA SWING)                |
|  - LoginFrame: Nhận SQL login cá nhân; AuthService xác minh profile/hash |
|  - Session Singleton: Quản lý phiên làm việc & kiểm tra Role-Based      |
|  - MainFrame: Ẩn/Hiện phân hệ động theo quyền hạn người dùng            |
+-------------------------------------------------------------------------+
                                    │
                                    │ (JDBC TLS theo cấu hình SQL Server)
                                    ▼
+-------------------------------------------------------------------------+
|                   TẦNG 2: CƠ SỞ DỮ LIỆU (SQL SERVER)                    |
|  - 4 Database Roles: role_DBAdmin, role_HRManager,                      |
|                      role_PayrollOfficer, role_Employee                 |
|  - GRANT / REVOKE / DENY chi tiết trên từng Bảng, View, Procedure       |
|  - Trigger trg_ChiTietLuong_KhongSuaKhiDaChot cưỡng chế bất biến        |
+-------------------------------------------------------------------------+
```

### 3.2.1. Tầng 1: Phân quyền tại Giao diện Ứng dụng (Application-Level RBAC)
- **Mẫu thiết kế Session Singleton (`com.session.Session`):** Lưu trữ thông tin định danh của người dùng hiện tại trong bộ nhớ RAM của tiến trình JVM. Khi người dùng đăng xuất, toàn bộ thông tin phiên làm việc bị xóa sạch (`logout()`).
- **Cơ chế lọc giao diện động (`MainFrame.applyRolePermissions()`):** Căn cứ vào vai trò trả về từ CSDL:
  - Nếu là `DB_Admin`: Kích hoạt toàn bộ các Tab (Nhân sự, Danh mục, Chấm công, Phụ cấp & Khấu trừ, Lương, Báo cáo & Chốt lương, Quản trị tài khoản).
  - Nếu là `HR_Manager`: Vô hiệu hóa phân hệ Tính lương và Quản trị tài khoản; hiển thị Hồ sơ nhân viên, Danh mục và Chấm công theo quyền được cấp.
  - Nếu là `Payroll_Officer`: Kích hoạt đối soát chấm công, Phụ cấp & Khấu trừ, Tính lương và Báo cáo; SQL role chặn sửa hồ sơ nhân sự/chấm công.
  - Nếu là `Employee`: Toàn bộ các phân hệ quản lý đều bị ẩn; người dùng chỉ được xem duy nhất Báo cáo phiếu lương cá nhân của chính mình.

### 3.2.2. Tầng 2: Phân quyền tại Cơ sở Dữ liệu (Database-Level Security)
Dù người dùng thay đổi Session Java, connection vẫn được mở bằng SQL login cá nhân và SQL Server kiểm tra quyền trên từng thao tác. `sp_LayTaiKhoanHienTai` lấy mapping theo `ORIGINAL_LOGIN()` và yêu cầu role SQL tương ứng; `vw_PhieuLuongCaNhan` lọc theo login gốc.
- DBA cấp SQL login/user/role và `TAIKHOAN.SqlLogin` mapping tường minh. Schema install không tạo tài khoản hay mật khẩu mặc định.
- `role_DBAdmin` là thành viên `db_owner` trong application database; thao tác tạo/reset/disable SQL login cần quyền server DBA phù hợp, thường `ALTER ANY LOGIN`. Không đồng nghĩa role này là `sysadmin`.
- Áp dụng nguyên tắc **Đặc quyền Tối thiểu (Principle of Least Privilege)**: Người dùng chỉ được cấp đúng những quyền tối thiểu cần thiết để hoàn thành công việc của mình.
- HR không được tính/chốt hoặc xóa kỳ lương. Payroll có SELECT/INSERT/UPDATE/DELETE trên `PHUCAPNHANVIEN` và `KHAUTRUNHANVIEN`, đồng thời không được sửa `NHANVIEN`, `PHONGBAN`, `CHUCVU` hay `CHAMCONG`. Employee chỉ đọc phiếu lương cá nhân.

---

## 3.3. QUẢN LÝ ĐỊNH DANH VÀ MÃ HÓA MẬT KHẨU (CRYPTOGRAPHY & IDENTITY)

### 3.3.1. PBKDF2 và chuyển đổi hash legacy
SQL Server xác thực mật khẩu của SQL login cá nhân khi kết nối. Sau đó `AuthService` tra profile qua `sp_LayTaiKhoanHienTai`, ràng buộc `TAIKHOAN.SqlLogin = ORIGINAL_LOGIN()` và xác minh hash mật khẩu ứng dụng. Hash mới có định dạng `pbkdf2-sha256$600000$<salt>$<key>`: salt ngẫu nhiên 16 byte, khóa dẫn xuất 32 byte, dùng `PBKDF2WithHmacSHA256`. URL cấu hình chỉ chứa `db.url`; ứng dụng không có `db.user`/`db.password` dùng chung.

```java
String hash = PasswordUtil.hashPassword(password); // PBKDF2 salted, 600.000 iterations
boolean valid = PasswordUtil.verifyPassword(password, storedHash);
```

**Đặc tính kỹ thuật bảo mật:**
1. Mỗi hash mới có salt riêng, làm cho cùng một mật khẩu tạo ra hash lưu trữ khác nhau.
2. `MessageDigest.isEqual` so sánh khóa dẫn xuất; định dạng, độ dài salt/hash và số vòng được kiểm tra trước khi chấp nhận.
3. Hash SHA-256 64 ký tự chỉ là định dạng cũ: sau khi SQL login và mật khẩu ứng dụng cùng hợp lệ, hash của chính tài khoản đó được đổi qua `sp_MigrateMatKhau`. Hash không được trả về UI/Session.

### 3.3.2. Quản lý trạng thái và Vòng đời Tài khoản (`TAIKHOAN`)
Bảng `TAIKHOAN` được thiết kế có thuộc tính `TrangThai NVARCHAR(20)` nhận 2 giá trị hợp lệ: `'HOAT_DONG'` và `'KHOA'`.
- SQL login bị disable hoặc profile ở trạng thái `KHOA` đều ngăn đăng nhập. `sp_AdminSetStatus`, `sp_AdminResetPassword` và `sp_AdminSetRole` đồng bộ trạng thái/credential/role SQL với profile trong transaction.
- Các thủ tục admin chỉ được grant cho `role_DBAdmin`; thao tác đổi login ở server còn cần quyền server tương ứng. Không có mật khẩu mặc định. Password mới phải dài 8–128 ký tự và thỏa SQL Server policy.

---

## 3.4. MA TRẬN PHÂN QUYỀN VAI TRÒ CHI TIẾT (RBAC MATRIX)

### 3.4.1. Bảng ma trận phân quyền trên 10 bảng và Stored Procedure

| Bảng / đối tượng | `role_DBAdmin` | `role_HRManager` | `role_PayrollOfficer` | `role_Employee` |
|---|---|---|---|---|
| Database scope | `db_owner` | Grants nghiệp vụ HR | Grants payroll, đối soát và CRUD phụ cấp/khấu trừ | Chỉ phiếu lương cá nhân |
| Nhân sự, phòng ban, chức vụ | Toàn quyền trong DB | CRUD được grant | SELECT; ghi bị DENY | DENY |
| `LICHSULUONG` | Toàn quyền trong DB | Không cấp trực tiếp | Không cấp trực tiếp | Không cấp |
| Chấm công | Toàn quyền trong DB | CRUD + SP ghi công | SELECT/đối soát; ghi bị DENY | DENY |
| Phụ cấp/khấu trừ | Toàn quyền trong DB | Đọc/ghi/xóa theo grants | Đọc/ghi/xóa theo grants | DENY |
| Bảng lương/chi tiết | Toàn quyền trong DB | SELECT; không được xóa kỳ hoặc chạy tính/chốt | Đọc/ghi kỳ theo grants; tính/chốt/mở lại/xóa kỳ nháp qua SP | DENY trực tiếp |
| `TAIKHOAN` | Toàn quyền trong DB | DENY | DENY | DENY |
| `vw_PhieuLuongCaNhan` | Theo `db_owner` | Không cấp | Không cấp | SELECT, lọc theo `ORIGINAL_LOGIN()` |
| Admin reset/status/role procedures | Theo `db_owner` | DENY | DENY | DENY |

### 3.4.2. View phiếu lương cá nhân
`role_Employee` bị DENY trên bảng lương và chỉ được SELECT `vw_PhieuLuongCaNhan`. View nối profile đang hoạt động với `vw_BangLuongChiTiet` và lọc `TAIKHOAN.SqlLogin = ORIGINAL_LOGIN()`. Vì vậy, việc lọc gắn với danh tính SQL được xác thực; `MaNV` hoặc Session do client gửi không quyết định phạm vi dữ liệu.

### 3.4.3. Minh chứng thực nghiệm phân quyền trên SQL Server Management Studio
Toàn bộ kịch bản kiểm thử phân quyền được tự động hóa tại `database/test_security_roles_TV5.sql` và được chạy thực tế trên SQL Server:
- **Ảnh minh chứng phân quyền:** `screenshots/TV5/TV5_Security_Roles_Verification.png` là ảnh lịch sử. `EXECUTE AS USER` có thể minh họa grants object-level, nhưng không chứng minh bộ lọc `ORIGINAL_LOGIN()`; phần này được kiểm bằng SQL login thật trong log identity hiện tại.

---

## 3.5. KIỂM SOÁT ĐỒNG THỜI VÀ XỬ LÝ TRANH CHẤP (CONCURRENCY CONTROL)

### 3.5.1. Phân tích bài toán tranh chấp trong nghiệp vụ chốt lương
Trong kỳ tính lương cuối tháng, doanh nghiệp có nhiều nhân viên kế toán cùng làm việc trên hệ thống. Một vấn đề tranh chấp kinh điển có thể xảy ra:
- **Tình huống:** Kế toán A và Kế toán B cùng mở kỳ lương tháng 9/2026 trên giao diện.
- Kế toán A kiểm tra thấy trạng thái là `CHUA_CHOT` và bấm nút "Chốt bảng lương".
- Cùng tích tắc đó, Kế toán B cũng bấm nút "Chốt bảng lương".
- **Hậu quả nếu không có kiểm soát đồng thời:**
  - Hai giao dịch cùng đọc trạng thái `CHUA_CHOT`.
  - Cả hai cùng thực hiện lệnh `UPDATE` trạng thái `DA_CHOT`.
  - Cả hai cùng ghi nhận thời gian chốt lương khác nhau hoặc ghi nhận người chốt khác nhau.
  - Xảy ra hiện tượng **Lost Update** hoặc xung đột khóa (Deadlock) làm treo ứng dụng.

### 3.5.2. Giải pháp kỹ thuật: Khóa hỗn hợp `UPDLOCK, HOLDLOCK`
Để giải quyết bài toán trên một cách tối ưu nhất mà không cần nâng mức cô lập toàn hệ thống lên `SERIALIZABLE` (vốn gây suy giảm nghiêm trọng hiệu năng đọc dữ liệu), TV5 đã thiết kế Stored Procedure `sp_ChotBangLuong` kết hợp Transaction và khóa gợi ý (Table Hints):

```sql
CREATE OR ALTER PROCEDURE sp_ChotBangLuong
    @MaBangLuong INT,
    @NguoiChot NVARCHAR(50) = N'admin'
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. ĐỌC DỮ LIỆU VỚI KHÓA UPDLOCK VÀ HOLDLOCK
        -- Ngăn chặn tuyệt đối Lost Update và xung đột Race Condition
        DECLARE @TrangThaiHienTai NVARCHAR(20);
        SELECT @TrangThaiHienTai = TrangThai
        FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
        WHERE MaBangLuong = @MaBangLuong;

        -- Kiểm tra tồn tại
        IF @TrangThaiHienTai IS NULL
        BEGIN
            RAISERROR(N'Bảng lương có mã %d không tồn tại!', 16, 1, @MaBangLuong);
            ROLLBACK TRANSACTION;
            RETURN;
        END;

        -- Kiểm tra trạng thái đã chốt chưa
        IF @TrangThaiHienTai = N'DA_CHOT'
        BEGIN
            RAISERROR(N'Kỳ lương này đã được chốt trước đó! Không thể chốt lại.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END;

        -- Kiểm tra bảng lương phải có ít nhất 1 dòng chi tiết
        IF NOT EXISTS (SELECT 1 FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong = @MaBangLuong)
        BEGIN
            RAISERROR(N'Bảng lương chưa có dữ liệu chi tiết! Cần tính lương trước khi chốt.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END;

        -- 2. CẬP NHẬT TRẠNG THÁI CHỐT LƯƠNG
        UPDATE dbo.BANGLUONG
        SET TrangThai = N'DA_CHOT',
            NgayChot = GETDATE()
        WHERE MaBangLuong = @MaBangLuong;

        COMMIT TRANSACTION;
        PRINT N'Chốt bảng lương thành công!';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
```

**Nguyên lý hoạt động chi tiết của `WITH (UPDLOCK, HOLDLOCK)`:**
1. `UPDLOCK` (Update Lock): Báo hiệu cho SQL Server biết phiên làm việc này dự định sẽ cập nhật dòng dữ liệu được chọn. Khi Session 1 đặt `UPDLOCK` trên bản ghi `BANGLUONG`, các Session khác vẫn có thể thực hiện lệnh `SELECT` thông thường với `NOLOCK` hoặc đọc mức `READ COMMITTED`, nhưng bất kỳ Session nào khác muốn xin khóa `UPDLOCK` hay `X` (Exclusive Lock) trên dòng đó đều **bắt buộc phải xếp hàng chờ**.
2. `HOLDLOCK`: Giữ khóa này liên tục cho đến khi giao dịch kết thúc (`COMMIT` hoặc `ROLLBACK`), tương đương mức cô lập `SERIALIZABLE` nhưng chỉ áp dụng cục bộ cho đúng dòng dữ liệu cần bảo vệ mà không khóa oan các dòng khác.
3. Khi Kế toán B gọi `sp_ChotBangLuong`, Session 2 bị chặn lại tại câu lệnh `SELECT ... WITH (UPDLOCK, HOLDLOCK)` cho đến khi Kế toán A `COMMIT`. Khi Session 2 được giải phóng, biến `@TrangThaiHienTai` đọc được ngay lập tức mang giá trị `'DA_CHOT'`. Thủ tục nhảy vào khối kiểm tra và phát lệnh `RAISERROR`, tự động `ROLLBACK` an toàn mà không làm hỏng dữ liệu.

- **Ảnh minh chứng Concurrency (2 Sessions song song):** `screenshots/TV5/TV5_Concurrency_2Sessions.png`  
  *(Ảnh kịch bản lịch sử với hai phiên SSMS; kết quả có phạm vi đúng theo fixture/lần chạy, không phải bảo đảm 100% cho mọi race condition).*

### 3.5.3. Ràng buộc toàn vẹn dữ liệu qua Trigger `trg_ChiTietLuong_KhongSuaKhiDaChot`
Sau khi kỳ lương đã chốt, một rủi ro khác là người dùng hoặc phần mềm độc hại có thể cố ý chạy lệnh `UPDATE` hoặc `DELETE` trực tiếp trên bảng `CHITIETBANGLUONG` để thay đổi số tiền thực nhận.  
Trigger sau đây được cài đặt để bảo đảm tính bất biến tuyệt đối:

```sql
CREATE OR ALTER TRIGGER trg_ChiTietLuong_KhongSuaKhiDaChot
ON dbo.CHITIETBANGLUONG
AFTER UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM deleted d
        JOIN dbo.BANGLUONG bl ON d.MaBangLuong = bl.MaBangLuong
        WHERE bl.TrangThai = N'DA_CHOT'
    )
    BEGIN
        RAISERROR(N'LỖI TOÀN VẸN: Kỳ lương đã chốt! Nghiêm cấm mọi thao tác sửa đổi hoặc xóa chi tiết bảng lương.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
```
Bất kỳ câu lệnh `UPDATE` hay `DELETE` nào vi phạm đều bị chặn đứng tức thì ở cấp độ Kernel của SQL Server và giao dịch bị hủy bỏ toàn bộ.

- **Ảnh minh chứng Trigger khóa chi tiết lương:** `screenshots/TV5/TV5_Trigger_KhoaChiTietLuong.png`  
  *(Chụp trực tiếp từ SSMS khi cố ý chạy câu lệnh UPDATE trên kỳ lương đã chốt: SQL Server lập tức ném thông báo lỗi màu đỏ `Msg 50000: LỖI TOÀN VẸN: Kỳ lương đã chốt!...` và hủy bỏ toàn bộ giao dịch batch).*

---

## 3.6. TỐI ƯU HÓA TRUY VẤN VÀ ĐÁNH GIÁ HIỆU NĂNG (INDEX BENCHMARK)

### 3.6.1. Đặt vấn đề và Phân tích truy vấn trọng yếu
Trong phân hệ quản lý nhân sự và tính lương, truy vấn tìm kiếm nhân viên theo Phòng ban và Chức vụ được thực hiện với tần suất cực kỳ cao (khi lọc danh sách nhân sự trên giao diện `NhanVienPanel`, khi tổng hợp chấm công, và khi tính lương cho từng bộ phận):

```sql
SELECT MaNV, HoTen, LuongCoBan, TrangThai
FROM dbo.NHANVIEN
WHERE MaPB = @MaPB AND MaCV = @MaCV;
```
Nếu không có chỉ mục phù hợp, SQL Server bắt buộc phải thực hiện quét toàn bộ bảng (Table Scan hoặc Clustered Index Scan). Khi số lượng nhân viên của công ty tăng từ hàng trăm lên hàng chục ngàn nhân sự, chi phí I/O (Logical Reads) và thời gian thực thi sẽ tăng tuyến tính, làm suy giảm hiệu năng toàn hệ thống.

### 3.6.2. Thiết kế Covering Index `IX_NHANVIEN_MaPB_MaCV`
Để tối ưu hóa triệt để, TV5 đã thiết kế chỉ mục Non-clustered bao phủ (Covering Index):

```sql
CREATE NONCLUSTERED INDEX IX_NHANVIEN_MaPB_MaCV
ON dbo.NHANVIEN (MaPB, MaCV)
INCLUDE (MaNV, HoTen, LuongCoBan, TrangThai);
```

**Tại sao đây là giải pháp tối ưu nhất?**
- **Cột khóa (Key Columns - `MaPB, MaCV`):** Tạo cây B-Tree nhị phân được sắp xếp theo `MaPB` rồi đến `MaCV`, giúp bộ tối ưu hóa truy vấn (Query Optimizer) thực hiện phép toán `Index Seek` với độ phức tạp $O(\log N)$ thay vì quét tuần tự $O(N)$.
- **Cột bao phủ (Included Columns - `MaNV, HoTen, LuongCoBan, TrangThai`):** Các cột này được đính kèm trực tiếp tại tầng lá (Leaf Level) của chỉ mục. Do đó, sau khi tìm thấy con trỏ trong B-Tree, SQL Server lấy được toàn bộ dữ liệu cần thiết của mệnh đề `SELECT` ngay tại chỉ mục mà **hoàn toàn không cần tốn chi phí tra cứu ngược lại bảng chính (No Key Lookup / Bookmark Lookup)**.

### 3.6.3. Benchmark lưu trong tư liệu lịch sử
Các số đo sau là kết quả được ghi trong báo cáo/ảnh mốc 29/09/2026 từ `database/test_benchmark_index_TV5.sql`; chúng mô tả fixture và lần chạy đó, không phải cam kết hiệu năng hiện tại:
- Sinh ngẫu nhiên **10,000 bản ghi nhân viên** phân bổ trên các phòng ban và chức vụ khác nhau.
- Bật cơ chế đo lường phần cứng của SQL Server: `SET STATISTICS IO ON; SET STATISTICS TIME ON;`.
- So sánh hiệu năng của cùng một câu lệnh truy vấn giữa 2 trường hợp: Chưa có Index và Đã tạo Index.

#### Bảng so sánh kết quả thực nghiệm:

| Chỉ số đo lường (Metrics) | Khi KHÔNG có Index (Scan) | Khi CÓ Covering Index (Seek) | Tỷ lệ cải thiện |
|---|:---:|:---:|:---:|
| **Toán tử thực thi (Execution Operator)** | Clustered Index Scan | **Index Seek (Non-Clustered)** | Chuyển từ quét tuần tự sang tìm kiếm nhị phân |
| **Số trang đọc logic (Logical Reads)** | **94 trang** | **2 trang** | **Giảm 97.87% (47 lần), theo log lịch sử** |
| **Chi phí truy vấn ước tính (Subtree Cost)** | 0.0715 | **0.0032** | **Giảm 95.52% (22 lần)** |
| **Key Lookup / Bookmark Lookup** | Không | **0 (Hoàn toàn không có Lookup)** | Bao phủ 100% cột truy vấn |
| **Thời gian CPU (CPU Time)** | ~15 ms | **0 ms (< 1 ms)** | Giảm tải CPU máy chủ triệt để |
| **Thời gian thực thi (Elapsed Time)** | ~28 ms | **~1 ms** | Tốc độ đáp ứng tức thì |

**Phân tích kết quả:**
Trong lần đo lịch sử đó, số logical reads ghi nhận giảm từ **94 xuống 2**. Kết quả chỉ áp dụng cho fixture, truy vấn, schema, thống kê và môi trường của lần chạy; không suy rộng thành cam kết độ trễ hay khả năng mở rộng quy mô.

#### Minh chứng hình ảnh Benchmark thực nghiệm trên SSMS:
- **Ảnh Actual Execution Plan:** `screenshots/TV5/TV5_Benchmark_ExecutionPlan.png`  
  *(Ảnh lịch sử từ lần benchmark ngày 29/09; không dùng làm execution plan của lần xác minh hiện tại).*
- **Ảnh thống kê I/O & Time:** `screenshots/TV5/TV5_Benchmark_StatisticsIO.png`  
  *(Ảnh lịch sử của lần đo ngày 29/09; số liệu không mô tả lần chạy mới).*

---

## 3.7. BỘ KIỂM THỬ TÍCH HỢP TỰ ĐỘNG VÀ KẾT QUẢ ĐẠT ĐƯỢC

### 3.7.1. Kết quả xác minh hiện hành
Kết quả kiểm thử được chốt theo log trong `docs/FIX_TASKLIST.md`: regression Java offline 104 assertions; T18 kiểm tra bốn chip demo với JFrame ẩn đạt 20 assertions, không mở SQL connection; SQL identity suite đạt 50 checks; PowerShell SQL verification trên QA exit 0. Mỗi kết quả chỉ áp dụng cho các case và môi trường được nêu trong log. Bộ `FullSystemIntegrationTest.java`/số 41/41 ở bảng bên dưới là mốc lịch sử ngày 29/09, không dùng làm bằng chứng cho source hiện tại.

Chip đăng nhập nhanh chỉ xuất hiện khi bật `-Dapp.demo=true`; chip điền username (`admin`, `hr_manager`, `payroll_officer`, `employee01`) và để trống password. Các SQL login GUI QA (`gui_admin`, `gui_hr`, `gui_payroll`, `gui_a`, `gui_b`) có tên khác và được DBA provision riêng; chip username không tự ánh xạ sang login QA. Credential và hướng dẫn GUI nằm trong [DEMO_ACCOUNTS.md](DEMO_ACCOUNTS.md) và [GUI_TEST_GUIDE.md](GUI_TEST_GUIDE.md). Các Employee fixture 1008/1009 chỉ thuộc môi trường local được ghi trong hướng dẫn. Không có mật khẩu demo được seed hoặc tự điền.

### 3.7.2. Ảnh giao diện và giới hạn sử dụng
Các ảnh có sẵn trong `screenshots/` là tư liệu lịch sử; dữ liệu hiển thị có thể hardcode. `CaptureScreenshots.java` hiện chỉ chạy khi có `-Dapp.mockScreenshots=true`, tạo ảnh minh họa có watermark trong `build/mock-screenshots/`. Không dùng ảnh đó làm bằng chứng giao diện kết nối dữ liệu hoặc quyền thật. Các tên ảnh lịch sử gồm:
1. `01_LoginFrame.png`: Màn hình đăng nhập hệ thống.
2. `02_MainFrame_Dashboard.png`: Màn hình chính Dashboard với đầy đủ thanh điều hướng.
3. `03_NhanVien_HoSo.png`: Quản lý hồ sơ nhân viên (TV1).
4. `04_ChamCong_ChiTiet.png`: Ghi nhận dữ liệu chấm công hàng ngày (TV2).
5. `05_ChamCong_TongHopThang.png`: Bảng tổng hợp công theo tháng (TV2).
6. `06_ChamCong_DieuChinhDialog.png`: Dialog điều chỉnh số công (TV2).
7. `07_PhuCap_KhauTru_Panel.png`: Quản lý phụ cấp và khoản giảm trừ (TV3).
8. `08_BangLuong_TinhLuong.png`: Bảng tính toán lương tổng thể (TV4).
9. `09_BaoCao_ChotLuong.png`: Báo cáo chi tiết lương và chức năng Chốt kỳ lương (TV5).
10. `10_TaiKhoan_QuanTri.png`: Màn hình quản trị người dùng, khóa tài khoản và phân quyền (TV5).

### 3.7.3. Tổng hợp Danh mục Minh chứng Kỹ thuật & Thực nghiệm Phân hệ TV5 (`screenshots/TV5/`)
Toàn bộ minh chứng kỹ thuật phục vụ nghiệm thu và chấm điểm đồ án của TV5 được tổ chức đồng bộ trong thư mục `screenshots/TV5/`:

| STT | Tên tệp minh chứng | Môi trường | Nội dung minh chứng | Trạng thái |
|:---:|---|:---:|---|:---:|
| 1 | `TV5_Benchmark_ExecutionPlan.png` | SSMS | Ảnh plan của benchmark ngày 29/09 |  Lịch sử |
| 2 | `TV5_Benchmark_StatisticsIO.png` | SSMS | Ảnh STATISTICS IO/TIME của benchmark ngày 29/09 |  Lịch sử |
| 3 | `TV5_Security_Roles_Verification.png` | SSMS | Ảnh lịch sử minh họa grants; không chứng minh lọc `ORIGINAL_LOGIN()` |  Lịch sử |
| 4 | `TV5_Trigger_KhoaChiTietLuong.png` | SSMS | Thông báo lỗi đỏ `Msg 50000` từ trigger `trg_ChiTietLuong_KhongSuaKhiDaChot` khi cố ý sửa kỳ lương đã chốt |  Đầy đủ |
| 5 | `TV5_Concurrency_2Sessions.png` | SSMS | Ảnh lịch sử minh họa hai phiên và khóa `UPDLOCK, HOLDLOCK` |  Lịch sử |
| 6 | `TV5_BaoCao_ChotLuong.png` | Java Swing | Ảnh giao diện lịch sử |  Lịch sử |
| 7 | `TV5_TaiKhoan_QuanTri.png` | Java Swing | Ảnh quản trị lịch sử, không chứng minh thao tác SQL admin thành công |  Lịch sử |

---

## 3.8. KẾT LUẬN VÀ Ý NGHĨA THỰC TIỄN CỦA PHÂN HỆ

1. **Vượt trên chuẩn Rubric đánh giá:** Không chỉ dừng lại ở các câu lệnh SQL cơ bản, phân hệ của TV5 đã giải quyết triệt để các bài toán khó nhất của môn học Hệ quản trị CSDL: Xung đột giao dịch đồng thời (Lost Update Prevention với `UPDLOCK, HOLDLOCK`), Phòng thủ chiều sâu 2 tầng (RBAC Java + SQL Server), Bất biến dữ liệu bằng Trigger và Tối ưu hóa truy vấn bằng Covering Index có số liệu benchmark thực tế.
2. **Tính ứng dụng cao trong doanh nghiệp:** Kiến trúc 4 tầng chuẩn mực kết hợp với kiểm soát quyền hạn phân minh giúp hệ thống có thể triển khai thực tế cho các doanh nghiệp vừa và nhỏ mà không cần viết lại mã nguồn nền tảng.
3. **Giá trị đóng góp cho toàn nhóm:** Cung cấp hạ tầng khung (Framework), Session singleton, cơ sở dữ liệu dùng chung và bộ test tự động giúp các thành viên TV1, TV2, TV3, TV4 tích hợp mã nguồn nhanh chóng, mượt mà và không phát sinh lỗi xung đột.
