# Kiểm thử trực tiếp giao diện PeopleOS — 09/10/2026

## Môi trường và kết luận

Kiểm thử bằng thao tác chuột/bàn phím trên ứng dụng Java Swing đang chạy, bản `main` sau PR #37 (commit `09258f7`). SQL Server local đã hoạt động; ứng dụng dùng riêng database `PRJ_Fix_QA_20261007_01`. Không ghi dữ liệu vào database `QuanLyNhanSuTienLuong`.

Luồng HR ghi công/khoản → Payroll tính/chốt/mở lại → Employee xem phiếu đã chạy thành công. Kết quả tiền lương và dữ liệu lưu được đối chiếu bằng truy vấn SQL chỉ đọc. Ba lỗi phát hiện ban đầu đã được sửa trên branch local `codex/fix-gui-qa`; kết quả kiểm tra sau sửa ở cuối tài liệu. Đây là kết quả của các ca bên dưới, không phải kết luận mọi chức năng đều PASS.

## Các ca đã thực hiện trước khi sửa

| Ca | Thao tác qua giao diện | Kết quả quan sát |
|---|---|---|
| G01 | Đăng nhập HR bằng mật khẩu sai | Bị từ chối; mật khẩu được xóa, nút đăng nhập hoạt giuđộng lại. Thông báo SQL dài bị cắt, xem lỗi F03. |
| G02 | Đăng nhập `gui_hr` bằng mật khẩu fixture | Thành công; tên tài khoản và vai trò HR đúng ở tiêu đề/góc trên. |
| G03 | Kiểm tra điều hướng HR | Có hồ sơ, danh mục, công, khoản và báo cáo; không có Bảng lương/Quản lý tài khoản. Thẻ Tổng quan sai nhãn, xem F02. |
| G04 | Tìm `TEST Giao dien A`, nhấp đúp kết quả | Trả đúng 1 hồ sơ; mở đúng mã NV 1008 và thông tin A. |
| G05 | Đổi địa chỉ A thành `GUI test 09/10/2026`, bấm Cập nhật | **FAIL:** báo email không hợp lệ dù giữ nguyên email fixture. SQL xác nhận địa chỉ vẫn là `GUI QA`. |
| G06 | HR ghi công A ngày 2026-10-09, Có mặt, 08:00–17:00 | Thành công, mã chấm công 1013. |
| G07 | HR xem Tổng hợp công tháng 10/2026 | A có 1 ngày đi làm, 9 giờ; trễ/sớm/vắng đều 0. |
| G08 | HR thêm Phụ cấp ăn trưa 730.000đ cho A | Thành công; mã khoản 1011. |
| G09 | HR thêm Tạm ứng lương 1.000.000đ cho A | Thành công; mã khoản 1008. |
| G10 | HR mở Báo cáo & Phiếu lương trước khi tính | Hiển thị chưa có kỳ; không có nút chốt/mở lại/xóa kỳ. |
| G11 | Đăng xuất và đổi tài khoản | Có xác nhận; đăng nhập kế tiếp không giữ username/password cũ. Đã chuyển HR → Payroll → A → B. |
| G12 | Payroll xem Nhật ký chấm công | Đọc được dòng công A; nút ghi công, điểm danh hàng loạt, làm mới biểu mẫu và xóa bị vô hiệu hóa. |
| G13 | Payroll Tính / cập nhật lương 10/2026, 26 công chuẩn | Thành công; kỳ 1009, 2 nhân viên, tổng thực nhận 730.000đ; chi tiết A/B đúng bảng bên dưới. |
| G14 | Payroll chốt kỳ trên Báo cáo | Thành công; kỳ hiển thị Đã chốt, có nút Mở lại bảng lương. |
| G15 | Payroll thử thêm phụ cấp cho B vào kỳ đã chốt | Bị từ chối: “Kỳ lương đã chốt: không được thay đổi phụ cấp.” SQL xác nhận không phát sinh khoản cho B. |
| G16 | Payroll mở lại kỳ | Thành công; trở về Bản nháp/`CHUA_CHOT`, `NgayChot` là NULL. |
| G17 | Employee A xem Phiếu lương của tôi | Chỉ 1 dòng kỳ 10/2026 của A, tổng 730.000đ; chỉ có Tổng quan/Báo cáo, không có quyền quản lý. |
| G18 | Employee B xem Phiếu lương của tôi | Chỉ 1 dòng kỳ 10/2026 với 0 công, 0 khoản, 0đ; không thấy dữ liệu 730.000đ của A. |
| G19 | B bấm Làm mới và phóng to cửa sổ | Dữ liệu vẫn đúng; các vùng chính và nút làm mới/đăng xuất sử dụng được. Bảng có thanh cuộn ngang để xem đủ cột. |
| G20 | B đăng xuất, bấm Đăng nhập khi hai ô trống | Về màn hình đăng nhập sạch; báo “Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!”. |

## Đối chiếu tiền lương

| Nhân viên | Lương cơ bản | Công chuẩn | Công thực tế | Tiền công | Phụ cấp | Khấu trừ | Thực nhận |
|---|---:|---:|---:|---:|---:|---:|---:|
| A (1008) | 26.000.000 | 26 | 1 | 1.000.000 | 730.000 | 1.000.000 | **730.000** |
| B (1009) | 26.000.000 | 26 | 0 | 0 | 0 | 0 | **0** |

`26.000.000 / 26 × 1 + 730.000 − 1.000.000 = 730.000đ`.

Đối chiếu SQL PASS, log: [sql-verification.log](../build/gui-qa/20261009/sql-verification.log). Script [verify-ui-results.sql](../build/gui-qa/20261009/verify-ui-results.sql) chỉ đọc và có guard đúng database QA. Nó kiểm tra dữ liệu lưu; kiểm tra tách phiếu A/B được thực hiện bằng hai lần đăng nhập trên giao diện.

## Các lỗi phát hiện ban đầu và task đã sửa

### F01 — P2: Email đuôi miền dài chặn cập nhật hồ sơ

- Tái hiện: HR mở A, giữ email `gui-a@example.invalid`, sửa địa chỉ, bấm Cập nhật.
- Trước sửa: “Định dạng email không hợp lệ!”, không lưu được hồ sơ. Regex tại [NhanVienService.java](../src/main/java/com/service/NhanVienService.java) giới hạn phần đuôi miền `{2,4}`; các đuôi dài như `.invalid` hoặc `.school` đều bị từ chối.
- [x] Sửa giới hạn đuôi miền thành `{2,63}` trong validator dùng chung, giữ kiểm tra email sai cấu trúc.
- [x] Kiểm tra đuôi miền dài, email thông thường và email thiếu `@`; chạy lại ca cập nhật địa chỉ, kiểm tra SQL rồi khôi phục địa chỉ fixture.

### F02 — P2: Tổng quan ghi HR/Payroll thành Quản trị viên

- Tái hiện: đăng nhập `gui_hr` hoặc `gui_payroll`, xem thẻ “Tài khoản của bạn”.
- Trước sửa: thẻ ghi “Quản trị viên” khi tài khoản không gắn MaNV. Tiêu đề/góc trên và quyền điều hướng vẫn đúng; chưa thấy việc cấp thêm quyền.
- Nguyên nhân: [MainFrame.java](../src/main/java/com/ui/main/MainFrame.java) chọn nhãn chỉ dựa vào `session.getMaNV() > 0`.
- [x] Dùng `Session.getVaiTroDisplayName()` khi không có MaNV; giữ mã NV khi có liên kết hồ sơ và giảm font cho nhãn dài.
- [x] Kiểm tra component Tổng quan cho cả 4 vai trò, có/không có MaNV bằng frame ẩn; kiểm tra HR trực tiếp trên ứng dụng QA.

### F03 — P3: Đăng nhập sai hiển thị lỗi JDBC dài, bị cắt

- Tái hiện: đăng nhập `gui_hr` bằng mật khẩu sai.
- Trước sửa: dòng trạng thái hiển thị tiếng Anh `Login failed for user ... ClientConnectionId:...`, tràn chiều rộng nên bị cắt.
- Nguyên nhân: nhánh catch trong [LoginFrame.java](../src/main/java/com/ui/auth/LoginFrame.java) đưa thẳng `cause.getMessage()` vào JLabel.
- [x] Chuyển lỗi đăng nhập thành câu tiếng Việt ngắn; phân biệt lỗi xác thực, kết nối/timeout, truy cập dữ liệu và mapping. Ghi chi tiết kỹ thuật bằng `java.util.logging`.
- [x] Kiểm tra phân loại lỗi bằng SQLException/SQLTimeoutException giả lập và kiểm tra sai mật khẩu trên GUI; nút nhập/đăng nhập được bật lại. Chưa ngắt SQL Server để thử timeout trực tiếp trên GUI.

## Bằng chứng và dữ liệu còn lại

Ảnh chụp trong thư mục local [build/gui-qa/20261009](../build/gui-qa/20261009):

- [Payroll chỉ đọc công](../build/gui-qa/20261009/payroll-attendance-readonly.jpg)
- [Chi tiết tính lương](../build/gui-qa/20261009/payroll-calculation.jpg)
- [Từ chối ghi kỳ đã chốt](../build/gui-qa/20261009/closed-period-rejected.jpg)
- [Phiếu A](../build/gui-qa/20261009/employee-a-payslip.jpg)
- [Phiếu B](../build/gui-qa/20261009/employee-b-payslip.jpg)
- [Bố cục phóng to](../build/gui-qa/20261009/employee-b-maximized.jpg)

Các ảnh/log nằm trong `build`, được bỏ qua bởi Git; các link này dùng trên máy vừa test. G01/G05/F02 được quan sát trực tiếp nhưng chưa lưu ảnh riêng.

QA giữ lại 1 ngày công A, 1 phụ cấp A, 1 khấu trừ A và kỳ 10/2026 mã 1009 ở trạng thái **CHUA_CHOT** để có thể test tiếp. Lần thử ghi phụ cấp B sau chốt không tạo dữ liệu. Hồ sơ A không thay đổi do validation từ chối. Cuối phiên ứng dụng ở màn hình đăng nhập.

## Chưa kiểm tra trong đợt này

Chưa thao tác Quản lý tài khoản/Admin, CRUD Phòng ban & Chức vụ, thêm/xóa nhân viên, xóa khoản/kỳ, điểm danh hàng loạt, điều chỉnh công tháng, nhập ngày không tồn tại/lương âm, khôi phục sau mất kết nối SQL, chuyển kỳ liên tục khi đang tải, nhiều phiên đồng thời, bố cục cửa sổ tối thiểu và các mức DPI khác. Không đổi mật khẩu hoặc quyền tài khoản.

## Kiểm tra sau sửa — 09/10/2026

Biên dịch Java 11: **54 nguồn PASS**. Bốn bộ kiểm tra hồi quy: **224 assertions PASS** — GuiQa 55, Security 104, LightTheme 45, LoginChip 20. Chạy lại bộ mới bằng lệnh ở README; không yêu cầu thông tin đăng nhập SQL. [Log GuiQa](../build/test-results/Java_Verification_20261009_225255_650.log).

GuiQa kiểm tra email đuôi miền dài và ranh giới 63/64 ký tự, email thiếu/sai `@`, các nhóm exception đăng nhập và độ rộng thông báo, nhãn Tổng quan cho bốn vai trò có/không có MaNV. Lỗi xác thực SQL Server được nhận diện theo mã lỗi 18456 hoặc SQLState lớp 28; tham khảo [Microsoft về lỗi 18456](https://learn.microsoft.com/en-us/sql/relational-databases/errors-events/mssqlserver-18456-database-engine-error).

Kiểm tra lại trực tiếp ứng dụng QA:

- Sai mật khẩu HR: hiện “Tên đăng nhập hoặc mật khẩu không đúng.”, đọc đủ câu; mật khẩu bị xóa và nút đăng nhập bật lại. [Ảnh](screenshots/gui-qa-fixes/fix-login-error.jpg).
- Đăng nhập HR đúng: thẻ tài khoản ghi “Quản lý nhân sự”, điều hướng HR giữ nguyên. [Ảnh](screenshots/gui-qa-fixes/fix-hr-role.jpg).
- Mở A, giữ email `.invalid`, cập nhật địa chỉ `GUI test 09/10/2026`: thành công. [Ảnh](screenshots/gui-qa-fixes/fix-profile-updated.jpg); [SQL xác nhận giá trị đã lưu](../build/gui-qa/20261009/fix-profile-sql.log).
- Khôi phục địa chỉ A về `GUI QA` bằng UPDATE có guard đúng database/email/giá trị cũ và transaction. [Đối chiếu cuối phiên PASS](../build/gui-qa/20261009/fix-final-sql.log): kỳ 1009 vẫn CHUA_CHOT, A nhận 730.000đ, B nhận 0đ, dữ liệu công/khoản giữ nguyên.

Bản sửa chỉ thay đổi Java, runner kiểm thử và tài liệu; không cần migration database. Bản sửa được quản lý qua branch `codex/fix-gui-qa` và Pull Request. Ba ảnh kiểm tra sau sửa được lưu trong Git tại `docs/screenshots/gui-qa-fixes`; các log SQL vẫn là bằng chứng local. Ứng dụng bản mới vẫn mở trong phiên HR sau ca kiểm tra hồ sơ.
