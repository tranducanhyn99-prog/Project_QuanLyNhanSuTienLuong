# TV5 — Hướng dẫn trình bày và thuyết trình cá nhân

**Trần Đức Anh · MSSV 24110155 · Nhóm 06 · Cập nhật 10/10/2026**

Đã đối chiếu phân công và SQL/Java trên `main` commit `0223f2f`, sau [PR #38](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/38). Trạng thái hiển thị **Bản nháp/Đã chốt**, mã SQL vẫn là `CHUA_CHOT/DA_CHOT`; số liệu minh chứng giữ đúng ngày chạy.

Demo nối tiếp thống nhất dùng **10/2026**, theo [báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md): A có lương 26.000.000đ, 26 công chuẩn, một công ngày 09/10/2026 (08:00–17:00), phụ cấp 730.000đ và khấu trừ 1.000.000đ; thực nhận **730.000đ**, B **0đ**. Đã có 20 ca thao tác và ba ca kiểm tra lại sau sửa. Trước demo, kiểm tra fixture hiện có theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md); dùng lại dòng công/khoản, không thêm trùng hoặc xóa nguồn để ép số. Kết thúc bằng mở lại kỳ, xác nhận `CHUA_CHOT` và `NgayChot IS NULL`.

Phương án phân bổ gợi ý: TV5 trình bày **slide 13–15**, khoảng **08:00–10:00**, điều phối demo và phụ trách **120 giây chốt/quyền truy cập** ở cuối demo. TV1→TV5 nói liên tục 10 phút, demo nối tiếp 5 phút, tổng 15 phút trình bày; sau đó vấn đáp 5–10 phút. Phân bổ này phục vụ tập dượt theo [kế hoạch nhóm](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md); nhóm có thể đổi khi giảng viên yêu cầu.

## 1. Phần việc và tiêu chí cần chứng minh

TV5 phụ trách kiến trúc tích hợp, đăng nhập/phân quyền, chốt/báo cáo lương, concurrency và tổng hợp minh chứng/bộ nộp. Trình bày đúng ranh giới: chức năng giao diện, kiểm tra service và quyền SQL là các lớp bảo vệ phối hợp.

| Mục rubric / phần phân công | Đối tượng TV5 | Điều cần trình bày |
|---|---|---|
| Kiến trúc và ứng dụng | Swing → Service → DAO/JDBC → SQL Server | Login tạo Session sau khi danh tính SQL/profile được xác minh; truy vấn chậm chạy qua SwingWorker. |
| ≥4 role/login, GRANT/REVOKE/DENY | `role_DBAdmin`, `role_HRManager`, `role_PayrollOfficer`, `role_Employee` | Quyền database thuộc user ánh xạ SQL login cá nhân, không chỉ ẩn nút. Có 5 login demo cho 4 vai trò. |
| SP có TRY…CATCH + transaction | `sp_ChotBangLuong` | Khóa header, kiểm tra tồn tại/chưa chốt/có chi tiết, ghi `DA_CHOT` và `NgayChot`; rollback khi lỗi. |
| Function | `fn_TinhThucNhan` | Tiền công + phụ cấp − khấu trừ; NULL được coi là 0; kết quả có thể âm. |
| Trigger | `trg_ChiTietLuong_KhongSuaKhiDaChot` | Chặn thêm/sửa/xóa/chuyển chi tiết liên quan kỳ đã chốt, kiểm tra cả `inserted` và `deleted`. |
| View báo cáo | `vw_BangLuongChiTiet`; bổ sung `vw_PhieuLuongCaNhan` | View tổng cho người có quyền; Employee chỉ đọc view cá nhân lọc `ORIGINAL_LOGIN()`. |
| Index + bằng chứng | `IX_NHANVIEN_MaPB_MaCV` | Lọc theo phòng ban/chức vụ, INCLUDE các cột hiển thị; có script benchmark và plan thực. |
| Concurrency/Recovery | Phối hợp SP tính lương của TV4 và trigger khóa nguồn | Hai session cùng kỳ phải phối hợp bằng khóa; lỗi rollback; không trùng header/chi tiết hoặc đổi nguồn sau chốt. |
| Tích hợp và bàn giao | Test report, source, script, README, slide/report | Tổng hợp bằng chứng đúng môi trường; checklist nộp phải phân biệt yêu cầu với sản phẩm thực có. |

Căn cứ là [rubric trong README](../README.md) và mục 4–6 của [kế hoạch phân công](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md). Các ngưỡng ≥5 đối tượng, ≥5 transaction và ≥1 concurrency áp dụng toàn nhóm; không tự gán trọng số điểm khi tài liệu không nêu.

## 2. Ba slide và lời nói mẫu

### Slide 13 — Danh tính và quyền ở SQL Server (08:00–08:45)

**Trên slide:** sơ đồ đăng nhập cá nhân → SQL login/user → database role → profile TAIKHOAN/Session; ma trận 4 vai trò bên dưới.

> “Em là Trần Đức Anh, phụ trách tích hợp và phân quyền. Ứng dụng kết nối bằng SQL login cá nhân. Sau khi SQL Server xác thực, procedure đọc profile theo `ORIGINAL_LOGIN()` và kiểm tra role membership; Java kiểm tra mật khẩu lưu dạng PBKDF2 và trạng thái tài khoản rồi mới tạo Session. Nhóm có bốn vai trò: Admin quản lý database, HR xử lý hồ sơ và công, Payroll tính và chốt lương, Employee xem phiếu lương của mình. Ẩn nút chỉ giúp giao diện phù hợp; quyền SQL vẫn giới hạn thao tác nếu client gọi trực tiếp DAO hoặc tự đổi Session.”

### Slide 14 — Chốt lương và bảo vệ kết quả (08:45–09:25)

**Trên slide:** `CHUA_CHOT` → `sp_ChotBangLuong` → `DA_CHOT`; khóa header; trigger bảo vệ chi tiết và nguồn; view phiếu lương cá nhân.

> “Sau khi TV4 tính lương, procedure chốt kiểm tra kỳ có chi tiết và chưa chốt, giữ khóa header trong transaction rồi ghi trạng thái và ngày chốt. Trigger bảo vệ cả thêm, sửa, xóa và chuyển chi tiết; các trigger nguồn cũng ngăn đổi công, phụ cấp, khấu trừ của kỳ đã chốt. Khi cần điều chỉnh, người có quyền phải mở lại theo quy trình rồi tính và chốt lại. Báo cáo tổng lấy từ view tổng hợp; Employee đọc view riêng dựa vào login thực, không dựa vào MaNV tùy ý client truyền lên.”

### Slide 15 — Kiểm thử, concurrency và kết thúc (09:25–10:00)

**Trên slide:** nhóm bằng chứng quyền/rollback/concurrency; kết quả hai session và kiểm tra khóa nguồn; phạm vi hiện tại; luồng demo 5 bước của cả nhóm.

> “Nhóm đã thao tác 20 ca GUI ngày 09/10, sửa ba lỗi rồi kiểm tra lại; bốn bộ hồi quy Java đạt 224 assertions. SQL có minh chứng quyền, rollback và concurrency: phiên B chờ khoảng 20,6 giây khi tính cùng kỳ, không tạo trùng; bốn race sửa nguồn khi chốt đều bị từ chối. Kết quả GUI tháng 10 là A nhận 730 nghìn, B nhận 0; demo sau đây dùng lại fixture đó và trả kỳ về chưa chốt khi kết thúc. Phạm vi là công thức lương với khoản nhập sẵn, chưa tự tính thuế/BHXH. Mời bạn Minh Trí mở đầu demo.”

**Cách dựng và tập nói:** dùng sơ đồ và bảng quyền ngắn; không chiếu toàn bộ GRANT hoặc log nhiều trang. Để script/log bên ngoài slide và mở khi được hỏi. Rút phần thông số PBKDF2 sang vấn đáp nếu nói vượt 2 phút.

## 3. Ma trận quyền phải nắm

| Vai trò ứng dụng / database role | Thao tác được phép | Ranh giới cần giải thích |
|---|---|---|
| `DB_Admin` / `role_DBAdmin` | Quản trị database và các phân hệ | Là thành viên `db_owner`, không mặc nhiên là server admin. `gui_admin` không có quyền server để reset/khóa SQL login. |
| `HR_Manager` / `role_HRManager` | CRUD hồ sơ, công, phụ cấp/khấu trừ; đọc báo cáo lương | Không tính/chốt/mở lại/xóa kỳ; tạo profile Employee vẫn cần DBA provision SQL login. |
| `Payroll_Officer` / `role_PayrollOfficer` | Đọc hồ sơ/công; CRUD khoản; tính/chốt/mở lại/xóa kỳ nháp | Không sửa nhân sự/chấm công. UI xử lý payroll qua SP; SQL role hiện có SELECT/INSERT trực tiếp header/chi tiết, không được cấp UPDATE/DELETE trực tiếp hai bảng này. |
| `Employee` / `role_Employee` | Xem phiếu lương cá nhân | Không đọc bảng nghiệp vụ hoặc view lương toàn công ty; lọc theo login được SQL xác thực. |

**Ví dụ GRANT/REVOKE/DENY cần chỉ đúng source:** `GRANT EXECUTE` SP nghiệp vụ cho role phù hợp; `REVOKE SELECT` view lương toàn công ty khỏi Employee; `DENY` Employee đọc/ghi các bảng gốc; `GRANT SELECT` view cá nhân. `REVOKE` gỡ quyền đã cấp, `DENY` thể hiện cấm rõ ràng. Chuỗi sở hữu cùng dbo cho phép SP/view được cấp quyền truy cập dữ liệu bên dưới; caller không vì vậy có quyền đọc/ghi trực tiếp bảng.

**Giao diện sau PR #38:** thẻ **Tài khoản của bạn** dùng `Session.getVaiTroDisplayName()` khi không gắn MaNV, nên HR hiện **Quản lý nhân sự**, Payroll hiện **Nhân viên kế toán lương**; đây là sửa nhãn, ma trận quyền giữ nguyên. Login sai mật khẩu SQL hiển thị câu ngắn “Tên đăng nhập hoặc mật khẩu không đúng.”; lỗi kết nối/timeout, truy cập dữ liệu và mapping có nhóm thông báo riêng, chi tiết ghi bằng `java.util.logging`. Kiểm tra trực tiếp đã xác nhận sai mật khẩu và nhãn HR; timeout mới được kiểm tra bằng exception giả lập, chưa ngắt SQL Server trên GUI.

## 4. Demo cuối — chốt và kiểm tra quyền trong 120 giây

**Chuẩn bị:** ứng dụng mở bằng `powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1`; đúng QA và schema mới. Mật khẩu `gui_hr/gui_payroll/gui_a/gui_b` lấy từ [demo_accounts.json](../database/demo_accounts.json). Kiểm tra A/B được mapping trước buổi trình bày; không tạo lại login hoặc nhân viên. TV4 đã tính kỳ 10/2026 và bàn giao tài khoản Payroll; kiểm chi tiết A 730.000đ/B 0đ. Dùng lại fixture, không thêm/xóa nguồn để ép số.

| Mốc gợi ý | Thao tác | Kết quả mong đợi |
|---|---|---|
| 0–25 giây | `gui_payroll` → **Báo cáo & Phiếu lương**, chọn **10/2026**, bấm **Chốt bảng lương**, xác nhận | Kỳ chuyển `DA_CHOT` (hiển thị **Đã chốt**), có ngày chốt; số tiền giữ nguyên. |
| 25–65 giây | Đăng xuất, vào `gui_hr` → **Nhật ký chấm công**; thử thêm A ngày **2026-10-10**, `08:00–17:00`, chọn **Có mặt** (`CO_MAT`) | Lỗi kỳ đã chốt; không có dòng công mới. Dùng ngày chưa tồn tại để chứng minh khóa kỳ, tránh bị lỗi trùng ngày che mất mục tiêu. |
| 65–95 giây | Đăng xuất, vào `gui_a` → **Báo cáo & Phiếu lương** (tiêu đề trang **Phiếu lương của tôi**) | Chỉ thấy phiếu lương A, không có nhân viên B hay bảng tổng. |
| 95–120 giây | Đăng xuất, vào `gui_b`, mở cùng báo cáo; sau khi đối chiếu, Payroll mở lại kỳ 10/2026. | B chỉ thấy phiếu 0đ của mình. Kết thúc `CHUA_CHOT`, `NgayChot IS NULL`; không có dòng nguồn mới. |

Nếu máy chậm hoặc nhập mật khẩu mất thời gian, ưu tiên chốt + một lần chặn nguồn + Employee A; dành kiểm tra B cho vấn đáp, nói rõ phần đã thao tác. Tập đổi tài khoản trước, không dùng chip username khác fixture như thể chúng là các login đã provision. Không reset mật khẩu hoặc đổi quyền giữa demo chính.

Trước khi kết thúc mỗi lượt demo, Payroll bấm **Mở lại bảng lương** (hủy chốt), tải lại và xác nhận kỳ 10/2026 là **Bản nháp**/`CHUA_CHOT`, `NgayChot IS NULL`. Kiểm không có dòng công mới do bài lỗi sau chốt. Nếu đổi tài khoản vượt 120 giây, dành thời gian phục hồi trạng thái sau phần trình diễn; không bỏ bước này. Lượt sau dùng lại công/khoản hiện có.

**SQL đọc dự phòng cho Employee:** SSMS phải kết nối thật bằng `gui_a`, rồi kết nối riêng bằng `gui_b` trên cùng QA; không dùng phiên DBA để chứng minh lọc danh tính cá nhân.

```sql
SELECT ORIGINAL_LOGIN() AS SqlLoginDangKetNoi,
       USER_NAME() AS DatabaseUser,
       IS_ROLEMEMBER('role_Employee') AS LaEmployee;

SELECT MaNV, HoTen, Thang, Nam, TrangThaiBangLuong, ThucNhan
FROM dbo.vw_PhieuLuongCaNhan
WHERE Thang = 10 AND Nam = 2026;
```

Muốn minh họa truy cập bị cấm ở SQL, Employee chạy riêng `SELECT * FROM dbo.vw_BangLuongChiTiet;`: kỳ vọng permission denied. Không thêm `WHERE MaNV=A` ở view tổng rồi gọi đó là bảo vệ database.

## 5. Concurrency và bộ bằng chứng để dành vấn đáp

**Demo hai session là phần mở rộng**, không chen vào 120 giây đổi role nếu chưa tập. Dùng hai cửa sổ SSMS trên cùng QA riêng và quyền DBA phù hợp để tạo fixture bằng IDENTITY_INSERT; script tái hiện khóa của nghiệp vụ Payroll nhưng việc chuẩn bị fixture không chạy bằng login Payroll thường.

1. Mở [Session A](../database/tests/TV4_Payroll_Concurrency_SessionA.sql) và [Session B](../database/tests/TV4_Payroll_Concurrency_SessionB.sql); đọc điều kiện đầu file, không chạy trên database gốc.
2. Chạy toàn bộ A; khi Messages ghi **SESSION A DA GIU LOCK**, chạy toàn bộ B ngay. A giữ transaction khoảng 20 giây.
3. B chờ khóa rồi dùng cùng header; kiểm tra một kỳ/nhân viên không trùng chi tiết. A thực hiện xác minh/cleanup sau khi B báo hoàn tất; đọc kết luận cleanup.
4. Giải thích: bảng tạm và app lock hỗ trợ bắt tay trong test; việc B bị chặn tại SP tính lương dựa trên khóa dòng/khoảng `UPDLOCK, HOLDLOCK`. Không gọi đây là lỗi treo ứng dụng.

**Mở sẵn khi được hỏi:**

- [Module TV5](../database/05_Security_Payroll_TV5.sql): SP chốt, function, trigger, view, index; phần GRANT/REVOKE/DENY và `sp_LayTaiKhoanHienTai`/`vw_PhieuLuongCaNhan`.
- [LoginFrame](../src/main/java/com/ui/auth/LoginFrame.java), [AuthService](../src/main/java/com/service/AuthService.java), [DatabaseConnection](../src/main/java/com/config/DatabaseConnection.java), [PasswordUtil](../src/main/java/com/util/PasswordUtil.java), [Session](../src/main/java/com/session/Session.java): danh tính SQL, PBKDF2 và trạng thái phiên.
- [MainFrame](../src/main/java/com/ui/main/MainFrame.java), [BaoCaoPanel](../src/main/java/com/ui/baocao/BaoCaoPanel.java), [DatabaseTask](../src/main/java/com/ui/theme/DatabaseTask.java): menu theo role, view báo cáo, SwingWorker, bỏ kết quả cũ khi đổi kỳ/phiên.
- [Security SQL test](../database/test_security_roles_TV5.sql), [Java regression](../src/test/java/com/test/SecurityRegressionTest.java), [SQL identity integration](../src/test/java/com/test/SqlIdentityIntegrationTest.java); kết quả và phạm vi ở [FIX_TASKLIST](FIX_TASKLIST.md).
- Java sau sửa 09/10: biên dịch Java 11 **54 nguồn PASS**, bốn bộ hồi quy **224 assertions PASS** — GuiQa 55, Security 104, LightTheme 45, LoginChip 20. LoginChip chạy frame ẩn với **0 SQL connections**. Identity suite riêng có **50 checks tổng hợp** gồm 41 Java identity/DAO/business + 2 provisioning + 6 URL + 1 cleanup; không cộng số này vào 224 hoặc gọi chúng là thao tác GUI/login SQL thành công.
- [Báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md): **20 ca thao tác + 3 ca kiểm tra lại sau sửa**, trong đó có ba lỗi ban đầu, không phải 20/20 PASS từ đầu. G14–G18 xác nhận chốt/mở lại 10/2026, chặn thêm khoản B sau chốt và Employee A/B chỉ thấy phiếu của mình. Ba ca kiểm tra lại xác nhận lỗi đăng nhập ngắn, nhãn HR đúng và cập nhật hồ sơ giữ email `.invalid` thành công; ảnh sau sửa lưu trong Git tại `docs/screenshots/gui-qa-fixes`.
- Concurrency: `build/fix-verification/Concurrency_A.log` và `Concurrency_B.log`: B chờ **20631 ms (khoảng 20,6 giây)**, không trùng và fixture được dọn. [Closed-source runner](../database/tests/Closed_Source_Concurrency.ps1): log `build/sql-verification/20261008_101955_11b4e24c/Closed_Source_Concurrency.log` xác nhận **4 race PASS**, nguồn không đổi.
- [Benchmark index TV5](../database/test_benchmark_index_TV5.sql): chuẩn bị truy vấn lọc phòng/chức vụ, IO/TIME và plan. Plan thực có ở `build/fix-verification/TV5_ActualPlan_*.sqlplan`; kiểm tra tên file tại máy. Chỉ ghi số đo khi có log tương ứng; không lấy số trong báo cáo/ảnh lịch sử làm kết quả mới.

Log/plan trong `build/` bị gitignore: cần mang theo bản thực có trong bộ minh chứng. Database QA ngẫu nhiên do runner tạo đã được dọn; QA GUI đang giữ công/khoản và kỳ 10/2026 `CHUA_CHOT` theo đối chiếu cuối phiên 09/10, A 730.000đ/B 0đ. Quản lý tài khoản/Admin, mất kết nối SQL trên GUI và nhiều phiên GUI đồng thời chưa được kiểm tra trong đợt 09/10.

## 6. Câu hỏi vấn đáp và câu trả lời ngắn

1. **Vì sao không chỉ ẩn nút theo role?** Client có thể gọi DAO trực tiếp hoặc đổi dữ liệu Session. SQL login cá nhân, role và quyền SQL giới hạn thao tác theo danh tính được DB xác thực.
2. **SqlLogin khác TenDangNhap thế nào?** TenDangNhap là tên profile ứng dụng; SqlLogin mapping profile với principal SQL. Đường login kết nối SQL trước, profile phải khớp ORIGINAL_LOGIN và membership, không tin vai trò client tự khai.
3. **Mật khẩu lưu thế nào?** PBKDF2-HMAC-SHA256, salt ngẫu nhiên, 600.000 vòng; lưu phiên bản/tham số/salt/hash. Java hỗ trợ kiểm tra và migrate hash legacy sau login hợp lệ; Session không giữ hash profile.
4. **A gửi MaNV của B thì xem được B không?** View cá nhân lọc theo login thực và mapping TAIKHOAN, còn quyền đọc view tổng/bảng gốc đã bị gỡ/cấm. Filter client không quyết định danh tính.
5. **Admin có reset/khóa login được ngay không?** DB_Admin là quyền database. Reset mật khẩu hoặc khóa SQL login cần quyền server bổ sung; gui_admin hiện thiếu quyền này, phải báo lỗi và rollback. Đổi role trong database là trường hợp khác; không nói mọi thao tác quản trị đều bị cấm.
6. **HR tạo profile thì vì sao vẫn chưa login được?** Profile chưa phải SQL login. DBA phải provision login/user/role và mapping; HR không tự cấp quyền server hoặc tạo profile Admin/Payroll.
7. **Chốt một kỳ hai lần thì sao?** SP giữ khóa header rồi kiểm tra trạng thái; lần sau thấy DA_CHOT sẽ bị từ chối. Lỗi rollback và được chuyển về Java xử lý.
8. **Bảo vệ sau chốt có chỉ chặn UPDATE?** Không: trigger kiểm tra cả inserted/deleted, bao gồm INSERT, UPDATE, DELETE và chuyển từ/đến kỳ chốt; nguồn công/khoản cũng được bảo vệ bằng khóa header.
9. **REVOKE khác DENY thế nào?** REVOKE xóa grant/deny đang có ở phạm vi đó; quyền vẫn có thể đến từ nguồn khác. DENY cấm rõ ràng với người dùng thường; cần xét ngoại lệ và role thực, không diễn giải như quyền hạn tuyệt đối của sysadmin/db_owner.
10. **Nếu hai phiên cùng tính thì có cần khóa bảng cả công ty?** Không chủ động khóa toàn bộ bảng cho mọi thao tác. Khóa kỳ và phạm vi nguồn cùng UNIQUE phối hợp tính đồng thời; mức khóa SQL chọn phụ thuộc plan và có thể escalation. Minh chứng chỉ khẳng định case đã đo.
11. **Thực nhận âm có bị ép về 0 không?** Không. Function hiện trả tiền công + phụ cấp − khấu trừ, dùng 0 cho NULL; có thể âm nếu khoản trừ vượt thu nhập.
12. **Vì sao giao diện không bị kết quả cũ ghi đè?** DatabaseTask chạy JDBC ngoài EDT, kiểm tra request/phiên trước cập nhật, chặn gửi thao tác ghi lặp và phục hồi nút khi lỗi. Regression có case trả kết quả ngược thứ tự.

## 7. Checklist điều phối và bộ nộp

- [ ] Slide 13–15 đúng tên/MSSV; tổng slide nhóm ≤15; tập tổng 10 phút nói + 5 phút demo.
- [ ] TV1/2/3 dùng `gui_hr`; TV4 đổi `gui_payroll`; TV5 chốt, thử HR rồi vào Employee A/B. Cùng một QA/kỳ và điều kiện dữ liệu.
- [ ] Cùng dùng fixture 10/2026 đã xác minh A 730.000đ/B 0đ; tập đủ mạch và trả kỳ `CHUA_CHOT`, `NgayChot IS NULL`.
- [ ] Login/mapping A/B hoạt động; có sẵn mật khẩu JSON và phương án SQL đọc/ảnh chụp từ lần chạy thật khi demo gặp lỗi.
- [ ] Có log quyền, rollback, concurrency, IO/TIME và execution plan; phân biệt kết quả tự động với thao tác trình bày tại chỗ.
- [ ] Từng thành viên biết SP/function/trigger/view/index/transaction được giao; vị trí cài đặt có thể thuộc module phụ thuộc khác.
- [ ] Kiểm tra bộ nộp: source, README, script/backup, dữ liệu demo, phân công/commit, báo cáo Word/PDF theo yêu cầu **50–100 trang**, slide ≤15 trang. Báo cáo cuối [Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx](Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx) đã có 55 trang nội dung (61 trang tổng); deck và package vẫn phải kiểm tra đầu ra thực tế theo [hướng dẫn hoàn thiện bộ nộp](HUONG_DAN_HOAN_THIEN_BO_NOP.md).
- [ ] Chuẩn bị trả lời giới hạn: công thức khoản nhập sẵn, thiếu lịch sử trước migration, quyền server của DBA, bằng chứng chỉ áp dụng case/môi trường đã chạy.
- [ ] Kết thúc demo: **“Nhóm đã minh họa luồng dữ liệu từ hồ sơ đến bảng lương đã chốt và phiếu lương theo danh tính. Nhóm xin nhận câu hỏi của cô.”** Chỉ nói những bước đã thao tác được.

## Tài liệu đối chiếu

- [Phân công và rubric nhóm](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md)
- [Hướng dẫn GUI](GUI_TEST_GUIDE.md) · [Tài khoản demo](DEMO_ACCOUNTS.md) · [Cấp danh tính và quyền](SECURE_SETUP.md)
- [Kết quả kiểm thử](FIX_TASKLIST.md) · [Phần trước: TV4](TV4_Huong_Dan_Thuyet_Trinh.md)
- [Báo cáo GUI và kiểm tra sau sửa 09/10](GUI_TEST_REPORT_20261009.md)

- [Hướng dẫn hoàn thiện bộ nộp và đầu ra từng TV](HUONG_DAN_HOAN_THIEN_BO_NOP.md)
