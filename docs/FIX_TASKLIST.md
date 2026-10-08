# Tasklist sửa source sau review

Ngày tạo: 07/10/2026. Nguồn: [docs/SOURCE_REVIEW.md](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/docs/SOURCE_REVIEW.md).

Danh sách gồm **20 task sửa source và 1 task chuẩn bị kiểm thử**. Source đã được sửa trong bản tích hợp hiện tại. Checkbox chỉ đánh dấu tiêu chí đã xác minh; các trường hợp còn chờ được nêu riêng. Phân công TV1–TV5 trong mô tả gốc là gợi ý theo module, không phải danh sách 3 sub-agent hiện tại.

## Trạng thái xác minh bản sửa — 07/10/2026

| Task | Source | Bằng chứng/chờ xác minh |
|---|---|---|
| C00 | Đã khôi phục SQL Server; chỉ test trên QA | SQL Server 2025 Developer 17.0.1000.7; install/schema/metadata đọc được. Database dự án gốc không bị thay đổi |
| T01–T03 | Đã sửa entry point và service/SQL/UI guard | Regression Java 104 assertions, grants 22 checks + 3 lệnh bị từ chối thật; login cá nhân kiểm tra DAO khi giả Session |
| T04 | Đã tách seed 06; bỏ DELETE tháng | Install 01→05, rerun sau khi đã seed giữ nguyên MaNV/MaPCNV/số tiền; seed hai lần vẫn đúng một nhân viên/một phụ cấp |
| T05 | Benchmark TV1 dùng bảng tạm | Benchmark thực + cleanup; logical reads 75→10 trên fixture 5.000 dòng. Không diễn giải thành tốc độ database thật |
| T06 | Test strict; runner không báo PASS khi thiếu live prerequisite | Java/SQL assert mã lỗi, -b/exit1 khi lỗi; thiếu credentials trả SKIPPED exit2; TV1/TV2/TV3 strict suites chạy PASS |
| T07–T08 | Trigger all mutation, kiểm tra kỳ nguồn/đích và khóa parent | Fix_Regression PASS detail insert/update/delete/move và phụ cấp/khấu trừ insert/update/delete/move; chốt/mở lại qua E2E |
| T09 | Trigger chấm công và DAO một mutation connection | PS5 integrated SQL runner exit 0; 4 race chốt với công/phụ cấp/khấu trừ/chi tiết PASS, nguồn không đổi, fixture cleanup PASS |
| T10–T12 | SQL login cá nhân, own view và grants đúng role | Real identity/DAO/business 41 checks + provisioning2 + URL guards6 + cleanup1; bốn vai trò, Employee A/B, giả Session, lệnh HR/Payroll được phép và bị cấm |
| T13 | Lưu/nạp lại NgayVaoLam, NgayChamCong | 6 DAO ngày/đọc lại/trùng ngày/chuyển kỳ chốt trong identity suite thực |
| T14 | Latest-request/session guard | Regression dùng panel thật và JDBC điều khiển độ trễ: A trả sau B, reload và đổi phiên không ghi đè dữ liệu mới |
| T15 | Lịch sử lương/ngày nghỉ, snapshot kỳ cũ | Fix_Regression PASS người nghỉ và đổi mức lương khi tính lại; lịch sử trước migration bị thiếu không tự phục hồi được |
| T16 | PBKDF2 salted/versioned và migrate legacy | Salt/verify/malformed hash offline; SQL login legacy/reset/status/role actual PASS và lỗi input không có side effects |
| T17 | SwingWorker dùng chung, timeout, restore và chống bấm lặp | Regression 104 kiểm tra có query ngoài EDT, stale session, pending selection, error/button restore và nested worker |
| T18 | Chip chỉ bật app.demo, đúng username, chỉ điền tên | Hidden JFrame regression 20 assertions PASS, 0 SQL connections; Java 11 compile 52 sources. Không khẳng định đăng nhập đầy đủ bốn tài khoản seed vì tài khoản không được tự seed |
| T19 | fn_TongPhuCap có sử dụng | Metadata đủ 5 UDF; sum/empty edge PASS; payroll E2E đối chiếu kết quả |
| T20 | Mock opt-in/watermark, log/plan thật | Log actual; .sqlplan từ STATISTICS XML cho TV1/TV3/TV5. Ảnh tracked cũ được ghi rõ là legacy/hardcode, không dùng làm bằng chứng đợt này |

### Log lần chạy có thể đối chiếu

- Bộ tài khoản GUI QA bổ sung 08/10: năm login `gui_admin/gui_hr/gui_payroll/gui_a/gui_b`, bốn role, hai Employee gắn MaNV 1008/1009 tại máy này; mapping/SID/role đã khớp và cả năm PASS AuthService login + từ chối mật khẩu sai. QA cũ hiện giữ hai hồ sơ và năm profile để test giao diện. Mật khẩu được nhóm chọn đưa vào Git ở `database/demo_accounts.json`; xem [DEMO_ACCOUNTS](DEMO_ACCOUNTS.md) và [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md).

- Java: `build/test-results/Java_Verification_20261007_224554_492.log` — Java 11 compile 51 sources, regression **104 assertions PASS**.
- Login chips: `build/test-results/Java_Verification_20261008_101835_004.log` — hidden JFrame regression **20 assertions PASS**, **0 SQL connections**; Java 11 compile 52 sources.
- SQL install/rerun/module/payroll/benchmark: `build/sql-verification/20261007_224431_9b1d3b6a/` — database QA riêng được dọn; `schema_preserves_data.log`, `demo_assert.log`, `schema_environment.log`, TV1/TV2/TV3/Fix_Regression/security/TV4 E2E và benchmark đều chạy thành công.
- Login thật: `build/identity-agent/{identity,provision,config,cleanup}.log` — **50 checks** tổng cộng, gồm 41 Java identity/DAO/business, 2 provisioning, 6 URL và 1 cleanup. Database/principal do runner tạo đã dọn.
- Concurrency tính lương: `build/fix-verification/Concurrency_A.log` và `Concurrency_B.log` — hai session chung MaBL=8, một header/detail, B chờ 20.631ms, không duplicate và cleanup=0 fixture.
- Race chốt/nguồn: `build/fix-verification/Closed_Source_Concurrency.log` — standalone PowerShell 7: 4 case chốt với công/phụ cấp/khấu trừ/chi tiết, nguồn không đổi sau chốt và cleanup PASS. Lỗi parser/Unicode trên Windows PowerShell 5 đã sửa; kết quả tích hợp cuối cùng ở log SQL runner bên dưới. Script runnable: `database/tests/Closed_Source_Concurrency.ps1 -Database PRJ_Fix_QA_...`.
- SQL runner tích hợp PS5: `build/sql-verification/20261008_101955_11b4e24c/` — exit 0; install/rerun/demo/regression/security/TV4 E2E/benchmarks và 4 race đóng kỳ PASS. Race chờ lần lượt 1.101/1.046/1.042/1.033 giây, bị từ chối, nguồn không đổi và fixture cleanup PASS; QA ngẫu nhiên của lần chạy đã được drop.
- Execution plan thực: `build/fix-verification/*_ActualPlan_*.sqlplan` — TV1:4, TV3:4, TV5:6; không phải ảnh plan vẽ lại.

Các thư mục build/ được gitignore; giữ hoặc xuất log/plan khi cần bàn giao. Mỗi PASS chỉ áp dụng các case và môi trường đã chạy. Chưa áp dụng schema/mapping vào database dự án gốc. QA `PRJ_Fix_QA_20261007_01` cũ vẫn còn; không khẳng định mọi database QA đã được dọn. Kiểm thử thao tác thủ công toàn bộ giao diện với DB chậm chưa được thực hiện; năm tài khoản gui_* đã PASS đăng nhập thực như ghi ở trên. Bộ bốn tên trên chip app.demo không được tự provision; script migration phải được thử trên bản sao dữ liệu thật trước khi triển khai.


## Chuẩn bị kiểm thử

- [x] **C00 — Khôi phục kết nối và tạo DB test riêng.** Probe ban đầu trả SQLState=08S01; SQL Server và QA đã được khôi phục/xác minh. Kiểm tra SQL Server instance/TCP/cổng, cấu hình JDBC và quyền kết nối. Dùng DB test tách khỏi dữ liệu đang sử dụng; cài schema/seed sau T04. Chỉ chạy SQL ghi dữ liệu khi target test đã được xác nhận. Hoàn thành khi probe đọc metadata thành công và log ghi rõ tên DB test, phiên bản schema. Không đánh dấu test DB PASS khi chưa kết nối được.

## Thứ tự ưu tiên

| Nhóm | Task | Mục tiêu |
|---|---|---|
| 1 — Bắt đầu ngay | T01–T06 | Chặn vượt quyền ở ứng dụng, loại bỏ script làm mất/ô nhiễm dữ liệu, sửa cách đánh giá test |
| 2 — Toàn vẹn payroll | T07–T09 | Khóa chi tiết lương, khấu trừ và chấm công sau chốt |
| 3 — Quyền DB | T10–T12 | Bảo vệ theo danh tính thực; chốt T10 sớm vì T03/T11/T12 phụ thuộc |
| 4 — Đúng dữ liệu/UI | T13–T15 | Lưu ngày đúng, báo cáo đúng kỳ, tính lại kỳ cũ đúng |
| 5 — Hoàn thiện | T16–T20 | Mật khẩu, UI không treo, demo, rubric, minh chứng |

Có thể triển khai các nhóm SQL, service và UI độc lập khi không sửa cùng file; phải phối hợp T07–T09 về thứ tự lấy khóa. T16 phải hoàn tất trước triển khai thực; T19/T20 phải hoàn tất trước nghiệm thu rubric/minh chứng. Không cần đổi framework hoặc thêm tầng abstraction để xử lý các lỗi cục bộ.

## Checklist chi tiết

- [x] **T01 — Bỏ đường tự đăng nhập admin** (P1, R01).

  Phụ trách gợi ý: TV5. File chính: [src/main/java/com/ui/main/MainFrame.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/main/MainFrame.java).

  Việc sửa: Bỏ tạo Session DB_Admin trong MainFrame.main; đưa entry point về LoginFrame. Script chạy ứng dụng chỉ biên dịch src/main; tách QuickTestLauncher khỏi bản chạy.

  Đạt khi: Chạy MainFrame trực tiếp với Session trống phải yêu cầu đăng nhập; bản chạy không chứa class giả lập vai trò.

  Phụ thuộc: Không.

- [x] **T02 — Chặn thao tác khi chưa đăng nhập hoặc sai quyền** (P1, R03).

  Phụ trách gợi ý: TV5 + các module service. File chính: [src/main/java/com/service/PayrollService.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/service/PayrollService.java).

  Việc sửa: Bắt buộc đăng nhập trước thao tác nhạy cảm; kiểm tra vai trò ở service đọc/ghi. Đường xóa kỳ từ PhuCapKhauTruService phải dùng cùng quy tắc quyền xóa lương. Employee chỉ đọc phiếu lương thuộc danh tính của mình.

  Đạt khi: Session trống bị chặn trước JDBC; HR không xóa được kỳ qua cả hai đường; Employee không gọi API báo cáo/chi tiết lương toàn bộ.

  Phụ thuộc: Không; phối hợp T11 để bảo vệ ở DB.

- [x] **T03 — Chặn HR cấp tài khoản DB_Admin** (P1, R02).

  Phụ trách gợi ý: TV1 + TV5. File chính: [database/01_Module_NhanSu_TV1.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/01_Module_NhanSu_TV1.sql).

  Việc sửa: Giới hạn vai trò mà HR được cấp ở NhanVienService và sp_ThemNhanVien. Loại lựa chọn DB_Admin khỏi form của HR; đường cấp admin phải kiểm tra quyền đáng tin cậy.

  Đạt khi: HR gửi DB_Admin qua UI hoặc gọi procedure trực tiếp đều bị từ chối; nhân viên và tài khoản không được tạo dở dang.

  Phụ thuộc: T10 cho xác minh danh tính ở DB; có thể sửa service/UI ngay.

- [x] **T04 — Tách schema và seed, bỏ DELETE dữ liệu theo tháng** (P1, R09).

  Phụ trách gợi ý: TV3 + các module SQL. File chính: [database/03_phucap_khautru_TV3.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/03_phucap_khautru_TV3.sql).

  Việc sửa: Bỏ DELETE dữ liệu tháng 9–10/2026 trong script cài đặt. Chuyển seed sau khi đủ bảng, tạo/lấy khóa nhân viên đúng và gói transaction; cập nhật thứ tự chạy trong README.

  Đạt khi: Cài DB trống không lỗi FK/dependency; chạy lại không xóa dữ liệu đã có, không tạo seed trùng; lỗi giữa seed không để dữ liệu dở dang.

  Phụ thuộc: Không.

- [x] **T05 — Cách ly benchmark và dọn nhân viên giả** (P2 — đưa lên sớm để tránh ô nhiễm dữ liệu, R18).

  Phụ trách gợi ý: TV1. File chính: [database/test_benchmark_index_TV1.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_benchmark_index_TV1.sql).

  Việc sửa: Dùng DB test/bảng tạm hoặc transaction rollback; sửa index hint đang đoán tên PK. Kiểm tra và dọn đúng dữ liệu benchmark cũ trong DB test nếu có, không xóa theo tên chung trong DB thật.

  Đạt khi: Benchmark chạy được; số nhân viên/bản ghi trước và sau không thay đổi; có xác nhận cleanup.

  Phụ thuộc: Chuẩn bị C00 trước khi chạy.

- [x] **T06 — Sửa test PASS giả và script concurrency lỗi thời** (P2 — cần trước khi dùng kết quả test, R16).

  Phụ trách gợi ý: TV5 + TV1/TV2. File chính: [database/test_security_roles_TV5.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_security_roles_TV5.sql).

  Việc sửa: Đồng bộ tham số procedure/cột hiện tại; assert lỗi mong đợi và trạng thái sau lỗi. Runner trả mã lỗi khi FAIL, báo SKIPPED riêng nếu DB không khả dụng. Kiểm tra sidebar thật và service.

  Đạt khi: Sai tham số hoặc thiếu bảng phải FAIL; lỗi quyền đúng mới PASS; không thể có kết luận 100% PASS khi DB bị bỏ qua. Demo TV5 không còn tham chiếu NguoiChot.

  Phụ thuộc: Có thể sửa ngay; chạy sau C00/T04.

- [x] **T07 — Khóa mọi thay đổi chi tiết kỳ đã chốt** (P1, R06).

  Phụ trách gợi ý: TV5 + TV4. File chính: [database/05_Security_Payroll_TV5.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql).

  Việc sửa: Trigger CHITIETBANGLUONG xử lý INSERT/UPDATE/DELETE; kiểm tra cả inserted/deleted và kỳ nguồn/đích. Đồng bộ khóa kỳ với chốt lương.

  Đạt khi: INSERT vào kỳ DA_CHOT, đổi số tiền, DELETE và chuyển MaBangLuong từ nháp sang chốt đều bị chặn; dữ liệu kỳ không đổi.

  Phụ thuộc: C00 để test; phối hợp T08/T09 về khóa.

- [x] **T08 — Khóa khấu trừ của kỳ đã chốt** (P1, R07).

  Phụ trách gợi ý: TV3 + TV4. File chính: [database/03_phucap_khautru_TV3.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/03_phucap_khautru_TV3.sql).

  Việc sửa: Bổ sung bảo vệ KHAUTRUNHANVIEN ở SQL cho INSERT/UPDATE/DELETE và chuyển kỳ; dùng cùng quy tắc khóa với thao tác chốt.

  Đạt khi: Thêm/sửa/xóa/chuyển khấu trừ liên quan kỳ DA_CHOT bị từ chối; kỳ nháp vẫn thao tác đúng.

  Phụ thuộc: C00; phối hợp T07/T09.

- [x] **T09 — Khóa chấm công và loại bỏ khoảng trống kiểm tra–ghi** (P1, R08).

  Phụ trách gợi ý: TV2 + TV4. File chính: [src/main/java/com/dao/ChamCongDAO.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/ChamCongDAO.java).

  Việc sửa: Bảo vệ chấm công kỳ DA_CHOT ở DB cho mọi thao tác; kiểm tra ngày cũ/ngày mới. Thay SELECT và ghi trên hai connection bằng thao tác nguyên tử hoặc transaction/khóa nhất quán.

  Đạt khi: Không thêm/sửa/xóa được công tháng đã chốt. Hai session chỉnh công và chốt không tạo trạng thái nguồn thay đổi sau chốt; không để transaction treo.

  Bằng chứng hiện có: SQL closed-period cases, standalone PowerShell 7 race 4 nguồn, và integrated PowerShell 5 SQL runner đều PASS. Log integrated run `build/sql-verification/20261008_101955_11b4e24c/` xác nhận bốn race bị từ chối, nguồn không đổi và fixture cleanup PASS.

  Phụ thuộc: C00; thống nhất khóa với T07/T08.

- [x] **T10 — Gắn quyền SQL với danh tính thực** (P1, R04).

  Phụ trách gợi ý: TV5. File chính: [src/main/java/com/config/DatabaseConnection.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/config/DatabaseConnection.java).

  Việc sửa: Chốt mô hình danh tính. Với Swing kết nối trực tiếp: dùng danh tính SQL/Windows được DB xác thực; nếu dùng DB account chung: xác thực/phân quyền phải ở thành phần server đáng tin cậy. Bỏ dùng sa làm cấu hình chạy mặc định.

  Đạt khi: Thay Session/gọi DAO trực tiếp không làm tăng quyền DB; chứng minh danh tính SQL và quyền tương ứng cho từng vai trò. Đổi sa sang một account chung đơn thuần chưa đủ để đánh dấu hoàn thành.

  Phụ thuộc: Chốt mô hình trước T03 ở DB/T11/T12.

- [x] **T11 — Employee chỉ được đọc lương của mình ở SQL** (P1, R05).

  Phụ trách gợi ý: TV5. File chính: [database/05_Security_Payroll_TV5.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql).

  Việc sửa: Thu hồi quyền Employee đọc toàn bộ vw_BangLuongChiTiet. Cấp đường đọc cá nhân dựa trên danh tính được DB xác thực; không tin MaNV do client tự truyền.

  Đạt khi: Nhân viên A đọc được lương A, không đọc được B qua Java và SQL trực tiếp; thử đổi tham số/filter vẫn không lấy được B.

  Phụ thuộc: T10.

- [x] **T12 — Đồng bộ grants với các thao tác hợp lệ** (P2, R12).

  Phụ trách gợi ý: TV5 + TV4. File chính: [database/05_Security_Payroll_TV5.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql).

  Việc sửa: Lập ma trận UI→service→procedure→role. Cấp đúng quyền cần cho tính/chốt/mở lại/xóa kỳ và các thao tác phụ cấp/khấu trừ hợp lệ; giải quyết đường xác thực tài khoản theo T10.

  Đạt khi: Các thao tác được phép chạy bằng principal thật; thao tác cấm vẫn bị từ chối. Không cần sa/db_owner để chạy toàn bộ chức năng thường ngày.

  Phụ thuộc: T10; T11; T06.

- [x] **T13 — Sửa ngày vào làm/ngày công không được lưu** (P2, R11).

  Phụ trách gợi ý: TV1 + TV2. File chính: [src/main/java/com/dao/NhanVienDAO.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/NhanVienDAO.java).

  Việc sửa: Nạp lại NgayVaoLam từ DB; lưu các trường ngày được phép sửa trong DAO. Nếu cấm sửa, chuyển sang chỉ đọc. Sửa ngày công phải kiểm tra trùng và kỳ cũ/mới.

  Đạt khi: Đổi ngày rồi tải lại cho giá trị đúng; thông báo thành công tương ứng dữ liệu đã lưu. Chuyển công sang kỳ chốt bị từ chối.

  Phụ thuộc: T09 khi cho sửa ngày công.

- [x] **T14 — Ngăn kết quả truy vấn cũ ghi đè kỳ báo cáo mới** (P2, R14).

  Phụ trách gợi ý: TV5. File chính: [src/main/java/com/ui/baocao/BaoCaoPanel.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/baocao/BaoCaoPanel.java).

  Việc sửa: Trong done(), chỉ áp dụng kết quả nếu MaBangLuong còn khớp kỳ chọn; hủy/bỏ qua worker lỗi thời.

  Đạt khi: Chọn A rồi B, ép A trả sau B: bảng, nhãn trạng thái và thao tác vẫn cùng kỳ B.

  Phụ thuộc: Không.

- [x] **T15 — Tính lại kỳ cũ đúng nhân viên và mức lương áp dụng** (P2, R10).

  Phụ trách gợi ý: TV4 + TV1. File chính: [database/04_Module_TinhLuong_TV4.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/04_Module_TinhLuong_TV4.sql).

  Việc sửa: Thống nhất quy tắc người đã nghỉ nhưng có công/khoản phát sinh kỳ cũ và mức lương theo kỳ. Sửa lọc trạng thái hiện tại; thêm dữ liệu lịch sử cần thiết nếu phải tái lập payroll quá khứ.

  Đạt khi: Người làm tháng 9, nghỉ tháng 10 vẫn có kết quả tháng 9 đúng khi tính lại; thay mức lương tháng 10 không tự áp cho tháng 9.

  Phụ thuộc: Chốt quy tắc nghiệp vụ; T13 để ngày nhân viên đúng.

- [x] **T16 — Nâng cách lưu mật khẩu và migrate hash** (P2 — trước triển khai thật, R13).

  Phụ trách gợi ý: TV5 + TV1. File chính: [src/main/java/com/util/PasswordUtil.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/util/PasswordUtil.java).

  Việc sửa: Dùng thuật toán dẫn xuất khóa chậm với salt riêng, có thể dùng PBKDF2 của JDK. Điều chỉnh schema để lưu phiên bản/tham số/salt/hash; có đường migrate tài khoản hiện tại.

  Đạt khi: Hai tài khoản cùng mật khẩu có giá trị lưu khác nhau; đăng nhập/reset/migration chạy đúng; tài khoản demo tách khỏi triển khai thật.

  Phụ thuộc: T10 để thống nhất đường xác thực.

- [x] **T17 — Đưa JDBC chậm ra khỏi Swing EDT** (P2, R15).

  Phụ trách gợi ý: Các chủ module UI. File chính: [src/main/java/com/ui/luong/BangLuongPanel.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/luong/BangLuongPanel.java).

  Việc sửa: Dùng lại SwingWorker cho tải dữ liệu và thao tác DB chậm; cập nhật UI trong done(), chặn bấm lặp khi đang chạy và đặt query timeout thích hợp.

  Đạt khi: Trong khi DB chậm/chờ khóa, cửa sổ vẫn phản hồi; thao tác không chạy trùng; timeout/lỗi có thông báo và nút được bật lại.

  Phụ thuộc: Giữ kiểm tra kết quả lỗi thời của T14.

- [x] **T18 — Đồng bộ đăng nhập nhanh với seed** (P2, R20).

  Phụ trách gợi ý: TV5. File chính: [src/main/java/com/ui/auth/LoginFrame.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/auth/LoginFrame.java).

  Việc sửa: Đổi username demo cho khớp hr_manager/payroll_officer/employee01; chỉ bật chip trong chế độ demo.

  Đạt khi: Các chip chỉ điền đúng tên, người dùng nhập mật khẩu SQL cá nhân đã được DBA provision; bản triển khai thật không hiển thị chip. Seed 06 không còn tự tạo tài khoản/mật khẩu demo. Hidden JFrame regression 20 assertions xác minh hành vi chip với 0 SQL connections; không khẳng định đăng nhập end-to-end cho bốn tài khoản demo vì chúng không tự seed.

  Phụ thuộc: T04.

- [x] **T19 — Hoàn thiện UDF thứ năm có sử dụng thực** (P2 — trước nghiệm thu rubric, R19).

  Phụ trách gợi ý: TV3 + nhóm. File chính: [database/03_phucap_khautru_TV3.sql](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/03_phucap_khautru_TV3.sql).

  Việc sửa: Đối chiếu rubric, bổ sung một UDF nghiệp vụ còn thiếu; fn_TongPhuCap là ứng viên đã được payroll nhắc tới. Tích hợp và test giá trị/bên rìa thay vì chỉ tạo để đếm.

  Đạt khi: DB cài sạch có ít nhất 5 UDF đúng yêu cầu; hàm mới được dùng và có kiểm tra kết quả.

  Phụ thuộc: T04; xác nhận rubric.

- [x] **T20 — Thay minh chứng hardcode bằng kết quả thực thi** (P2 — trước nộp minh chứng, R17).

  Phụ trách gợi ý: TV1 + TV5. File chính: [src/test/java/com/test/GenerateSSMSProofScreenshots.java](C:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/GenerateSSMSProofScreenshots.java).

  Việc sửa: Ghi rõ ảnh dựng là minh họa. Thu log/ảnh kết quả SQL thật, execution plan và thông số môi trường; cập nhật báo cáo từ số đo thực.

  Đạt khi: Mỗi kết luận PASS/hiệu năng truy về được script, lần chạy và kết quả thật; không dùng số hardcode làm benchmark.

  Phụ thuộc: C00/T05/T06 và các bản sửa liên quan đã qua kiểm tra.

## Kiểm tra trước khi đóng task

Mỗi task chỉ được đánh dấu hoàn thành khi thay đổi đã được review và kiểm tra đúng tiêu chí của task. Sau các nhóm sửa, cần biên dịch lại với Java 11 và chạy các kiểm tra liên quan. Với kiểm thử tiền/quyền/transaction, giữ một kiểm tra tái hiện nhỏ; mở rộng test có sẵn khi có thể.

Đợt xác minh cuối cần chứng minh:

- Session trống và vai trò sai bị từ chối; HR không cấp admin/xóa lương; Employee không đọc lương người khác.
- Kỳ DA_CHOT bất biến với INSERT/UPDATE/DELETE/chuyển kỳ ở các bảng liên quan; hai session chỉnh dữ liệu/chốt không tạo dữ liệu lệch.
- Cài sạch và chạy lại an toàn; benchmark/test không để fixture.
- Ngày lưu/tải lại đúng; đổi nhanh kỳ báo cáo không hiển thị kết quả cũ; tính lại kỳ có nhân viên nghỉ/thay lương đúng quy tắc.
- Test phân biệt PASS/FAIL/SKIPPED; TV4 E2E/concurrency và ma trận quyền chạy bằng DB/principal thực; minh chứng dùng log/plan thực.

Các phần SQL nêu ở bảng trạng thái đã chạy runtime trên QA; T09 và T18 đã có bằng chứng xác minh được ghi ở trên. QA `PRJ_Fix_QA_20261007_01` cũ vẫn được giữ lại, không nằm trong cleanup của các runner tự tạo QA. Bộ bốn tên trên chip app.demo không được tự provision; năm tài khoản gui_* bổ sung đã PASS đăng nhập thực như ghi ở phần log. Không thay kết quả thực thi bằng việc đọc source.
