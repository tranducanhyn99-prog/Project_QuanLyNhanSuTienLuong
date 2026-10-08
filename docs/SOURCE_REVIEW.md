# Review source — Quản lý nhân sự và tiền lương

Ngày review: 07/10/2026. Phiên bản: Git HEAD `8e4e79a`.

> Báo cáo này giữ nguyên phát hiện và bằng chứng của **lần review ban đầu**. Các câu "chưa sửa/chưa chạy SQL" bên dưới mô tả thời điểm đó. Trạng thái bản sửa và kiểm thử thực tế mới ở [FIX_TASKLIST](FIX_TASKLIST.md); cách triển khai ở [SECURE_SETUP](SECURE_SETUP.md).

## Kết quả và phạm vi

Đã rà soát 38 file Java ứng dụng, 9 file Java kiểm thử/công cụ, 5 module SQL chính, các script SQL kiểm thử/benchmark/concurrency, script PowerShell khởi chạy/kiểm thử, cấu hình mẫu và tài liệu hướng dẫn liên quan. Không chỉnh sửa mã ứng dụng hoặc schema trong lần review này.

Có **20 phát hiện cần xử lý: 9 P1 và 11 P2**. Những vấn đề lớn nhất là vượt qua đăng nhập/phân quyền, dữ liệu kỳ lương đã chốt vẫn thay đổi được và script cài đặt có thể xóa dữ liệu. Chưa nên xem các nhãn “100% PASS” hoặc ảnh minh chứng hiện có là bằng chứng hệ thống đã an toàn.

- **P1:** cần xử lý trước khi dùng dữ liệu thực hoặc nghiệm thu các yêu cầu bảo mật/toàn vẹn tương ứng.
- **P2:** lỗi chức năng, kiểm thử hoặc vận hành cần xử lý sau nhóm P1.

### Kiểm tra đã thực hiện

| Kiểm tra | Kết quả |
|---|---|
| Biên dịch toàn bộ 47 file Java bằng JDK 21, JDBC JAR có sẵn | Thành công |
| Biên dịch toàn bộ với `javac --release 11` | Thành công |
| Phân tích cú pháp 6 script PowerShell gốc | Thành công |
| `-Xlint:all` | 63 cảnh báo, chủ yếu liên quan Swing/serialization; không coi đây là lỗi nghiệp vụ |
| Probe JDBC đọc metadata | Không kết nối được: `SQLState=08S01, errorCode=0` |
| Probe phân quyền offline, JDBC giả | Xác nhận 3 đường gọi vượt kiểm tra quyền ở service |
| Chạy SQL E2E/concurrency/benchmark thực tế | Chưa thực hiện vì kết nối DB không thành công |

Các nhận định SQL dưới đây dựa trên source; chưa xác nhận schema, grants hay trigger thực sự đang được triển khai trên máy. Không đọc/đưa mật khẩu cấu hình thực vào báo cáo. Không chạy những test ghi dữ liệu hoặc các công cụ tạo ảnh có thể ghi đè minh chứng.

Probe offline dừng mọi thao tác ở ranh giới JDBC, không có SQL nào được thực thi:

```text
Unauthenticated payroll calculation: REACHED_JDBC
HR payroll deletion through PayrollService: DENIED_BEFORE_JDBC
HR payroll deletion through allowance service: REACHED_JDBC
Employee reading an arbitrary payroll period: REACHED_JDBC
```

Source kiểm tra nằm trong thư mục build được Git bỏ qua: [build/source-review/AuthorizationProbe.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/build/source-review/AuthorizationProbe.java). Có thể chạy lại sau khi biên dịch ứng dụng vào `build/source-review/classes`:

```powershell
javac --release 11 -encoding UTF-8 -cp "build/source-review/classes;lib/mssql-jdbc-12.6.1.jre11.jar" -d build/source-review/classes build/source-review/AuthorizationProbe.java
java -cp "build/source-review/classes;lib/mssql-jdbc-12.6.1.jre11.jar" AuthorizationProbe
```

## Phát hiện P1

### R01 — MainFrame tự cấp quyền admin khi chạy trực tiếp

**Vị trí:** [src/main/java/com/ui/main/MainFrame.java:854](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/main/MainFrame.java:854); [start_app.ps1:60](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/start_app.ps1:60).

`MainFrame.main()` tự gọi `Session.login(new TaiKhoan(..., "DB_Admin", ...))` nếu chưa đăng nhập. Chạy class này trực tiếp sẽ mở ứng dụng với quyền admin mà không kiểm tra mật khẩu. Script chính mở LoginFrame, nhưng vẫn biên dịch và phân phối class chứa đường vào này, cùng cả source test.

**Hướng sửa:** đưa mọi entry point ứng dụng về LoginFrame; bỏ tự đăng nhập khỏi main source. Chỉ biên dịch `src/main` cho bản chạy ứng dụng. Giữ công cụ giả lập vai trò ở môi trường test riêng. Kiểm tra lại bằng cách chạy trực tiếp MainFrame khi Session trống: phải yêu cầu xác thực.

### R02 — HR có thể tạo tài khoản DB_Admin

**Vị trí:** [src/main/java/com/ui/nhanvien/NhanVienPanel.java:202](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/nhanvien/NhanVienPanel.java:202); [src/main/java/com/service/NhanVienService.java:31](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/service/NhanVienService.java:31); [database/01_Module_NhanSu_TV1.sql:264](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/01_Module_NhanSu_TV1.sql:264); [database/05_Security_Payroll_TV5.sql:310](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql:310).

HR được mở màn hình nhân viên. Form cho chọn `DB_Admin`; service không hạn chế vai trò được cấp; `sp_ThemNhanVien` ghi nguyên `@VaiTro` vào TAIKHOAN. Kết quả là HR có thể tạo một nhân viên kèm tài khoản admin rồi đăng nhập bằng tài khoản đó.

SQL `DENY INSERT ON TAIKHOAN` cho HR không khắc phục đường gọi procedure cùng chủ sở hữu: quyền truy cập bảng có thể được bỏ qua theo ownership chaining. [Tài liệu Microsoft](https://learn.microsoft.com/en-us/sql/relational-databases/tutorial-ownership-chains-and-context-switching?view=sql-server-ver17).

**Hướng sửa:** giới hạn quyền cấp vai trò trong service và procedure; chỉ nguồn xác thực đáng tin cậy mới được cấp DB_Admin. Ẩn lựa chọn trên UI để giảm nhầm lẫn, nhưng kiểm tra bắt buộc phải nằm ở nơi thực thi.

### R03 — Kiểm tra quyền service cho phép Session trống và có đường xóa lương dành cho HR

**Vị trí:** [src/main/java/com/service/PayrollService.java:34](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/service/PayrollService.java:34); [src/main/java/com/service/PhuCapKhauTruService.java:67](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/service/PhuCapKhauTruService.java:67); [src/main/java/com/ui/luong/PhuCapKhauTruPanel.java:85](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/luong/PhuCapKhauTruPanel.java:85).

PayrollService dùng điều kiện `isLoggedIn() && !hasRole(...)`: Session chưa đăng nhập sẽ không bị chặn. Các API đọc chi tiết kỳ lương không kiểm tra vai trò/phạm vi nhân viên. Ngoài ra, HR bị chặn xóa lương ở PayrollService nhưng được vào panel phụ cấp, có nút xóa kỳ và service phụ cấp không kiểm tra quyền.

Probe offline xác nhận cả ba đường gọi đi tới JDBC. Việc thao tác SQL thành công còn phụ thuộc quyền của tài khoản kết nối chung; với tài khoản đủ quyền, đây là đường thực thi có tác động thực.

**Hướng sửa:** bắt buộc đăng nhập và kiểm tra đúng vai trò ở mọi entry point service nhạy cảm, cả đọc lẫn ghi. API phiếu lương cá nhân phải lấy MaNV từ danh tính đã xác thực. Cho đường xóa kỳ ở module phụ cấp đi qua cùng kiểm tra quyền quản lý lương.

### R04 — Các vai trò Java cùng dùng một danh tính SQL

**Vị trí:** [src/main/java/com/config/DatabaseConnection.java:35](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/config/DatabaseConnection.java:35); [src/main/java/com/config/DatabaseConnection.java:60](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/config/DatabaseConnection.java:60); [src/resources/config.properties.template:14](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/resources/config.properties.template:14).

DatabaseConnection lấy một bộ URL/user/password tĩnh rồi dùng cho tất cả người đăng nhập. AuthService chỉ thay đổi Session Java. Không có bước gắn người dùng ứng dụng với SQL principal hay danh tính được DB xác thực. Cấu hình mẫu dùng `sa`.

Nếu chạy theo mẫu, SQL Server nhìn mọi thao tác dưới quyền sa, nên GRANT/DENY của role_Employee/role_HRManager không bảo vệ các phiên Java tương ứng. Đổi Session hoặc gọi DAO trực tiếp có thể vượt kiểm soát phía client. Chưa xác nhận tài khoản DB thực tế có quyền sa vì review không đọc nội dung cấu hình bí mật.

**Hướng sửa:** thống nhất mô hình bảo mật trước khi sửa grants. Nếu tiếp tục Swing kết nối trực tiếp, quyền phải gắn với danh tính SQL/Windows thực sự của người dùng. Nếu dùng một tài khoản DB chung, xác thực và phân quyền phải được thực thi ở thành phần server đáng tin cậy, cùng tài khoản DB tối thiểu quyền. Chỉ thay `sa` bằng một tài khoản chung khác chưa giải quyết được danh tính từng người.

### R05 — role_Employee được đọc view lương của toàn bộ nhân viên

**Vị trí:** [database/05_Security_Payroll_TV5.sql:119](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql:119); [database/05_Security_Payroll_TV5.sql:379](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql:379).

View `vw_BangLuongChiTiet` không lọc theo người dùng. Role Employee được SELECT view này. Với cấu hình cùng chủ sở hữu hiện tại, DENY đọc bảng gốc không ngăn truy vấn qua view. Employee có thể chạy `SELECT * FROM dbo.vw_BangLuongChiTiet` để đọc lương của người khác.

Java thêm `WHERE MaNV = ?` ở đường phiếu lương cá nhân, nhưng điều đó không giới hạn quyền SELECT trên DB.

**Hướng sửa:** bỏ quyền đọc toàn bộ view cho Employee; cung cấp truy vấn/procedure hoặc RLS gắn với danh tính được DB xác thực. Kiểm thử bằng hai nhân viên: A phải không đọc được dòng của B dù truy vấn trực tiếp.

### R06 — Chi tiết lương vẫn có thể được thêm hoặc chuyển vào kỳ đã chốt

**Vị trí:** [database/05_Security_Payroll_TV5.sql:154](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql:154); [database/05_Security_Payroll_TV5.sql:343](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql:343).

Trigger chi tiết chỉ chạy `AFTER UPDATE, DELETE`, chỉ kiểm tra kỳ của các dòng `deleted`. Không có kiểm tra INSERT và không kiểm tra kỳ đích trong `inserted`. PayrollOfficer lại được INSERT bảng chi tiết.

Do đó có thể thêm chi tiết của một nhân viên chưa có trong kỳ DA_CHOT, hoặc UPDATE MaBangLuong từ kỳ nháp sang kỳ đã chốt. Cả hai thay đổi tổng lương của kỳ đã chốt mà không kích hoạt nhánh bảo vệ.

**Hướng sửa:** bảo vệ INSERT/UPDATE/DELETE và cả kỳ nguồn/kỳ đích. Đồng bộ khóa trên kỳ với thao tác chốt để tránh race condition. Bổ sung test INSERT và chuyển kỳ, vì TV4 E2E hiện chỉ kiểm tra UPDATE số tiền/DELETE dòng đã thuộc kỳ chốt.

### R07 — Khấu trừ của kỳ đã chốt không được khóa

**Vị trí:** [src/main/java/com/dao/KhauTruDAO.java:44](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/KhauTruDAO.java:44); [database/03_phucap_khautru_TV3.sql:100](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/03_phucap_khautru_TV3.sql:100).

KhauTruDAO INSERT/DELETE trực tiếp; service không kiểm tra trạng thái kỳ. Các module SQL chỉ có trigger khóa PHUCAPNHANVIEN, không có trigger tương ứng cho KHAUTRUNHANVIEN.

Người dùng có quyền thêm/xóa khấu trừ sau chốt qua UI, làm dữ liệu nguồn không khớp số tiền đã đóng băng trong chi tiết lương.

**Hướng sửa:** bảo vệ bảng khấu trừ ở SQL như quy tắc phụ cấp, cho mọi thao tác và mọi kỳ liên quan; phối hợp khóa với chốt lương. Test cả thêm/xóa/sửa/chuyển kỳ sau chốt.

### R08 — Chấm công vẫn được thêm vào kỳ đã chốt; sửa/xóa có race condition

**Vị trí:** [src/main/java/com/dao/ChamCongDAO.java:20](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/ChamCongDAO.java:20); [database/02_Module_ChamCong_TV2.sql:84](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/02_Module_ChamCong_TV2.sql:84); [src/main/java/com/dao/ChamCongDAO.java:225](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/ChamCongDAO.java:225).

Đường INSERT và sp_GhiNhanChamCong không kiểm tra kỳ DA_CHOT. Với ngày trong quá khứ, HR vẫn thêm được công của tháng đã chốt. Đường sửa/xóa có kiểm tra trạng thái nhưng thực hiện SELECT và mutation bằng hai connection khác nhau, không có transaction chung. Nếu session khác chốt giữa hai bước, thay đổi chấm công vẫn xảy ra.

**Hướng sửa:** thực thi quy tắc kỳ chốt ở DB cho cả INSERT/UPDATE/DELETE, kiểm tra cả ngày cũ/ngày mới. Giữ kiểm tra và thay đổi trong cơ chế khóa nhất quán với chốt lương. Test hai session: một chỉnh công, một chốt.

### R09 — Script cài đặt trộn seed gây mất dữ liệu và lỗi trên DB mới

**Vị trí:** [database/03_phucap_khautru_TV3.sql:226](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/03_phucap_khautru_TV3.sql:226); [database/03_phucap_khautru_TV3.sql:231](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/03_phucap_khautru_TV3.sql:231); [database/README.md:25](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/README.md:25).

Module 03 xóa toàn bộ phụ cấp/khấu trừ tháng 9 và 10/2026, rồi INSERT dữ liệu cho MaNV 1..5. Những DELETE này không giới hạn dữ liệu demo và không nằm trong transaction chung với seed. Chạy lại trên DB có dữ liệu sẽ xóa dữ liệu thật của các kỳ nháp tương ứng.

Trên DB mới theo thứ tự 01→05, module 01 chỉ seed phòng ban/chức vụ, chưa tạo các nhân viên đó. Seed module 03 vì vậy vi phạm FK. Trigger phụ cấp còn tham chiếu BANGLUONG trước khi bảng này được tạo ở module 04. Những lỗi này có thể để lại cài đặt/seed dở dang.

**Hướng sửa:** tách schema và seed demo. Chạy seed sau khi đủ bảng phụ thuộc, tạo/lấy đúng khóa nhân viên thay vì giả định ID 1..5. Seed phải có transaction, chạy lại an toàn và không xóa dữ liệu không thuộc fixture.

## Phát hiện P2

### R10 — Tính lại kỳ cũ bỏ mất nhân viên đã nghỉ và dùng lương cơ bản hiện tại

**Vị trí:** [database/04_Module_TinhLuong_TV4.sql:289](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/04_Module_TinhLuong_TV4.sql:289); [database/04_Module_TinhLuong_TV4.sql:327](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/04_Module_TinhLuong_TV4.sql:327).

Procedure lấy nhân viên có trạng thái hiện tại DANG_LAM_VIEC và LuongCoBan hiện tại; tính lại xóa toàn bộ chi tiết cũ. Ví dụ nhân viên làm tháng 9 nhưng nghỉ tháng 10: tính lại tháng 9 khi chưa chốt sẽ loại người đó dù vẫn có công tháng 9. Thay lương cơ bản tháng 10 cũng ảnh hưởng tính lại tháng 9.

**Hướng sửa:** xác định nhân viên/lương áp dụng theo kỳ, dựa trên quy tắc nghiệp vụ được thống nhất. Tối thiểu phải xử lý người đã nghỉ nhưng còn công hoặc khoản phát sinh của kỳ cũ; cần lịch sử ngày nghỉ/lương nếu hệ thống phải tái lập chính xác payroll quá khứ. Thêm test hồi tố với nghỉ việc và thay lương.

### R11 — Form cho sửa ngày nhưng DAO bỏ qua giá trị

**Vị trí:** [src/main/java/com/ui/nhanvien/NhanVienPanel.java:345](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/nhanvien/NhanVienPanel.java:345); [src/main/java/com/dao/NhanVienDAO.java:81](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/NhanVienDAO.java:81); [src/main/java/com/ui/chamcong/DieuChinhChamCongDialog.java:369](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/chamcong/DieuChinhChamCongDialog.java:369); [src/main/java/com/dao/ChamCongDAO.java:269](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/ChamCongDAO.java:269).

Form nhân viên nhận NgayVaoLam nhưng UPDATE không có cột đó; khi chọn dòng cũng không nạp lại ngày vào làm thực tế. Dialog sửa chấm công nhận NgayChamCong mới nhưng UPDATE chỉ ghi giờ/trạng thái/ghi chú. Người dùng nhận thông báo thành công trong khi ngày vẫn giữ nguyên.

**Hướng sửa:** nạp và lưu đầy đủ trường được phép sửa. Nếu nghiệp vụ cấm đổi ngày, hiển thị trường chỉ đọc. Nếu cho đổi ngày công, kiểm tra trùng ngày và khóa cả kỳ cũ/kỳ mới ở SQL.

### R12 — Grants SQL không đủ cho các thao tác hợp lệ của ứng dụng

**Vị trí:** [database/05_Security_Payroll_TV5.sql:339](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql:339); [src/main/java/com/dao/BangLuongDAO.java:28](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/BangLuongDAO.java:28); [database/05_Security_Payroll_TV5.sql:328](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql:328).

PayrollOfficer chỉ được EXECUTE sp_ChotBangLuong, chưa được cấp EXECUTE các procedure tính/mở lại/xóa kỳ mà ứng dụng gọi. HR/Payroll UI có thao tác xóa phụ cấp/khấu trừ nhưng grants không cho DELETE các bảng đó. Các role ngoài admin còn DENY SELECT TAIKHOAN, trong khi đăng nhập Java truy vấn chính bảng này.

Nếu kết nối bằng user_SQL mang role tương ứng, các chức năng hợp lệ sẽ lỗi quyền. Kết nối bằng sa che khuất các lỗi này nhưng tạo vấn đề R04.

**Hướng sửa:** sau khi chốt mô hình danh tính, lập ma trận thao tác UI→service→procedure→quyền DB. Kiểm thử cả thao tác được phép và bị cấm bằng principal thực, không bổ sung quyền rộng để làm test xanh.

### R13 — Mật khẩu dùng SHA-256 một lượt, không salt

**Vị trí:** [src/main/java/com/util/PasswordUtil.java:9](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/util/PasswordUtil.java:9); [database/01_Module_NhanSu_TV1.sql:90](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/01_Module_NhanSu_TV1.sql:90).

Hash nhanh và giống nhau cho cùng mật khẩu làm tăng khả năng dò offline khi bảng tài khoản bị lộ. Schema CHAR(64) cũng chỉ phù hợp kiểu lưu hiện tại. Đây là hạn chế thiết kế cho triển khai thực; không phải lỗi biên dịch.

**Hướng sửa:** dùng hàm dẫn xuất khóa mật khẩu chậm với salt riêng mỗi người, lưu phiên bản/tham số/hash để migrate. JDK đã có PBKDF2, không cần thêm thư viện chỉ để xử lý việc này. Cấu hình demo phải tách khỏi triển khai thực. [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) khuyến nghị các thuật toán lưu mật khẩu phù hợp thay cho SHA-256 nhanh.

### R14 — Báo cáo có thể hiển thị dữ liệu của kỳ khác kỳ đang chọn

**Vị trí:** [src/main/java/com/ui/baocao/BaoCaoPanel.java:250](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/baocao/BaoCaoPanel.java:250).

Mỗi lựa chọn khởi động SwingWorker và done() luôn ghi dữ liệu vào bảng. Chọn A rồi B; nếu A trả chậm hơn B, dữ liệu A ghi đè bảng trong khi combobox/trạng thái đang là B. Người dùng có thể đánh giá sai dữ liệu trước khi thao tác kỳ B.

**Hướng sửa:** chỉ áp dụng kết quả nếu MaBangLuong vẫn khớp lựa chọn hiện tại, hoặc hủy worker cũ và bỏ qua kết quả đã lỗi thời. Test hai truy vấn có độ trễ đảo thứ tự.

### R15 — Nhiều thao tác JDBC chạy trực tiếp trên Swing EDT

**Vị trí:** [src/main/java/com/ui/luong/BangLuongPanel.java:162](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/luong/BangLuongPanel.java:162); [src/main/java/com/ui/nhanvien/NhanVienPanel.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/nhanvien/NhanVienPanel.java); [src/main/java/com/ui/luong/PhuCapKhauTruPanel.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/luong/PhuCapKhauTruPanel.java).

Tải dữ liệu và nhiều thao tác ghi/tính lương chạy đồng bộ trong constructor/action listener. Khi DB chậm hoặc chờ khóa, cửa sổ ngừng phản hồi. Login timeout 2 giây không giới hạn thời gian một query đã kết nối.

**Hướng sửa:** dùng lại SwingWorker hiện có cho thao tác DB chậm; cập nhật UI trong done(), vô hiệu hóa nút liên quan khi đang chạy, đặt statement/query timeout phù hợp. Không cần đổi framework giao diện.

### R16 — Một số test báo PASS dù chưa chứng minh đúng hành vi

**Vị trí:** [database/test_security_roles_TV5.sql:48](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_security_roles_TV5.sql:48); [database/test_concurrency_demo_TV5.sql:38](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_concurrency_demo_TV5.sql:38); [src/test/java/com/test/FullSystemIntegrationTest.java:192](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/FullSystemIntegrationTest.java:192); [src/test/java/com/test/TestRealAuth.java:46](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/TestRealAuth.java:46); [database/tests_TV2/test_module_chamcong_TV2.sql:230](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/tests_TV2/test_module_chamcong_TV2.sql:230).

Test TV5 gọi sp_ChotBangLuong bằng Thang/Nam/NguoiChot, trong khi signature hiện tại nhận MaBangLuong; test sp_ThemNhanVien cũng dùng tham số không đúng. CATCH coi mọi lỗi là PASS, nên lỗi sai tham số có thể bị nhận nhầm là lỗi phân quyền. Demo concurrency TV5 UPDATE cột NguoiChot không tồn tại.

FullSystemIntegrationTest bắt lỗi kiểm tra DB/schema rồi bỏ qua; các assert UI còn đọc menu legacy không gắn vào giao diện thật. TestRealAuth có nhánh chỉ in lỗi khi kết quả xác thực trái kỳ vọng. Một số case âm TV2 cũng nhận mọi ngoại lệ là PASS; summary không THROW khi có FAIL.

**Hướng sửa:** đồng bộ signature/cột, assert mã lỗi hoặc thông điệp mong đợi, kiểm tra dữ liệu sau lỗi và trả exit code khác 0 nếu test thất bại. DB không khả dụng phải báo SKIPPED riêng. Kiểm tra sidebar thật/service thay vì menu legacy. Có thể dùng cách assert + THROW của TV4 E2E, không cần thêm framework.

### R17 — Công cụ ảnh minh chứng dựng số liệu cố định

**Vị trí:** [src/test/java/com/test/GenerateSSMSProofScreenshots.java:142](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/GenerateSSMSProofScreenshots.java:142); [src/test/java/com/test/GenerateSSMSProofScreenshots.java:111](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/GenerateSSMSProofScreenshots.java:111); [src/test/java/com/test/CaptureScreenshots.java:68](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/CaptureScreenshots.java:68).

GenerateSSMSProofScreenshots vẽ ảnh bằng Graphics2D, không chạy SQL/JDBC. Logical reads 428→4, thời gian 35→2 ms, thông báo trigger và nhãn PASS được viết cố định. CaptureScreenshots cũng thay dữ liệu JTable bằng số liệu giả trước khi chụp.

Các ảnh này phù hợp làm minh họa UI/kịch bản nếu được ghi rõ, nhưng không chứng minh hiệu năng, trigger hay transaction đã chạy thành công. Tỷ lệ cost 98%/2% được vẽ cũng không phải phép đo tốc độ thực thi.

**Hướng sửa:** ghi rõ ảnh minh họa; minh chứng kiểm thử phải lấy từ kết quả SQL thật, log, execution plan và môi trường chạy. Không sử dụng số hardcode làm kết quả benchmark.

### R18 — Benchmark TV1 có index hint sai và để lại nhân viên giả

**Vị trí:** [database/test_benchmark_index_TV1.sql:26](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_benchmark_index_TV1.sql:26); [database/test_benchmark_index_TV1.sql:78](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_benchmark_index_TV1.sql:78).

Script INSERT 5.000 nhân viên DANG_LAM_VIEC vào NHANVIEN thật nhưng không rollback/dọn fixture. Sau đó các nhân viên giả có thể bị đưa vào payroll hoặc chấm công hàng loạt. Hint `INDEX(PK__NHANVIEN)` không khớp tên PK tự sinh đầy đủ do schema không đặt tên primary key này.

**Hướng sửa:** benchmark trong DB test/bảng tạm, hoặc transaction rollback có kiểm tra cleanup. Dùng index ID/metadata/tên constraint được đặt rõ thay vì đoán prefix. TV2 benchmark dùng bảng tạm và TV4 benchmark có rollback là mẫu sẵn để tham khảo.

### R19 — Source chính mới định nghĩa 4 UDF, thiếu so với yêu cầu 5

**Vị trí:** [README.md:35](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/README.md:35); [database/01_Module_NhanSu_TV1.sql:147](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/01_Module_NhanSu_TV1.sql:147); [database/03_phucap_khautru_TV3.sql:80](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/03_phucap_khautru_TV3.sql:80); [database/04_Module_TinhLuong_TV4.sql:68](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/04_Module_TinhLuong_TV4.sql:68); [database/05_Security_Payroll_TV5.sql:98](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql:98).

README yêu cầu tối thiểu 5 User-Defined Function. Năm module chính chỉ CREATE 4 hàm: fn_TinhSoNgayCong, fn_TongKhauTru, fn_TinhTienCong, fn_TinhThucNhan. fn_TongPhuCap được nhắc như dependency tùy chọn trong payroll nhưng không có định nghĩa trong repository.

**Hướng sửa:** đối chiếu rubric và bổ sung hàm thứ năm có mục đích nghiệp vụ thực, tích hợp vào đường chạy và kiểm thử. Không tính procedure/view là function. Xác nhận bằng sys.objects sau cài đặt sạch.

### R20 — Nút đăng nhập nhanh dùng username khác dữ liệu seed

**Vị trí:** [src/main/java/com/ui/auth/LoginFrame.java:178](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/auth/LoginFrame.java:178); [database/05_Security_Payroll_TV5.sql:450](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql:450).

Ba chip dùng hrmanager/payroll/employee, còn script seed tạo hr_manager/payroll_officer/employee01. Theo seed trong repo, chỉ chip admin khớp; các chip còn lại điền thông tin không đăng nhập được.

**Hướng sửa:** đồng bộ tên demo với seed và chỉ bật đăng nhập nhanh trong cấu hình demo. Kiểm tra từng chip sau cài đặt sạch.

## Phần đang làm tốt và nên giữ

- Phân tách UI/service/DAO/model tương đối rõ; PreparedStatement/CallableStatement và try-with-resources được dùng rộng rãi.
- Payroll dùng BigDecimal/DECIMAL; SQL có khóa duy nhất theo kỳ/nhân viên, kiểm tra dữ liệu đầu vào, transaction và khóa để chống tính trùng.
- TV4 E2E có oracle công thức, force-error, đối chiếu snapshot trước/sau rollback và kiểm tra mã lỗi cụ thể. Test concurrency TV4 có hai session, đo thời gian blocking, kiểm tra duplicate và cleanup. Cần mở rộng cho các trường hợp R06–R08, không viết lại bộ test này.
- Benchmark TV2 dùng bảng tạm, giữ unique index thực tế và so sánh lợi ích covering index. TV4 benchmark rollback fixture và đối chiếu kết quả scan/seek.
- Các lỗi cần sửa không đòi hỏi thay framework hoặc thêm tầng abstraction hàng loạt. Quy tắc dùng chung nên đặt tại ranh giới thực thi; quy tắc toàn vẹn dữ liệu cần được DB bảo vệ.

## Thứ tự xử lý đề xuất

1. **Đăng nhập và quyền:** R01–R05; chốt mô hình danh tính rồi xử lý R12. Thêm test cho Session trống, HR cấp admin, HR xóa kỳ và Employee đọc lương người khác.
2. **Đóng kỳ:** R06–R08; test INSERT/UPDATE/DELETE, chuyển kỳ và cạnh tranh với thao tác chốt.
3. **Cài đặt an toàn:** R09; chạy toàn bộ trên DB trống rồi chạy lại, bảo đảm không mất dữ liệu.
4. **Đúng nghiệp vụ/giao diện:** R10–R15 và R20; ưu tiên ngày bị bỏ qua và dữ liệu báo cáo sai kỳ.
5. **Kiểm thử và nghiệm thu:** R16–R19; chạy bằng principal thực, giữ log/plan thật, phân biệt PASS/FAIL/SKIPPED.

Những kiểm tra cần chạy khi DB khả dụng: cài đặt sạch/chạy lại, ma trận quyền thực tế, khóa dữ liệu sau chốt, tính lại kỳ có nhân viên nghỉ việc, TV4 E2E, hai session concurrency và benchmark có cleanup. Chưa có đủ bằng chứng runtime để kết luận các kiểm tra này PASS.
