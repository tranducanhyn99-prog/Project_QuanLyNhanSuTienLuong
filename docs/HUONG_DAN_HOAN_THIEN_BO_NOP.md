# Hướng dẫn hoàn thiện bộ nộp — Nhóm 06

Cập nhật 10/10/2026, đối chiếu source sau PR #38 (`0223f2f`), [phân công](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md), [rubric README](../README.md), [FIX_TASKLIST](FIX_TASKLIST.md) và báo cáo Word cuối kỳ. Đây là danh sách việc cần bàn giao, **chưa đánh dấu hoàn tất**. Giữ nguyên ownership SQL/Java; vị trí cài function/trigger ở module phụ thuộc không chuyển người phụ trách.

Báo cáo [Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx](Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx) hiện có **55 trang nội dung, 61 trang tổng**, đáp ứng ngưỡng độ dài 50–100 trang nội dung. Các log đã PASS có thể dùng lại khi source và phạm vi đo tương ứng; không yêu cầu TV1/TV3/TV4 chạy lại toàn bộ suite. Phần còn thiếu bắt buộc là minh chứng benchmark TV2 hiện hành, deck ≤15 slide và package chứa bằng chứng thực tế ngoài Git.

## 1. Đầu ra và trách nhiệm từng thành viên

| Thành viên | Việc phải hoàn thiện | Đầu ra bàn giao / tiêu chí nhận |
|---|---|---|
| **TV1 — Nguyễn Minh Trí, 24110359** | Kiểm ERD, 10 bảng, PK/FK, từ điển dữ liệu và 3NF khớp SQL 01–05; có LICHSULUONG, NHANVIEN.NgayNghiViec, TAIKHOAN.SqlLogin. Chọn log/plan nhân sự đã PASS; chuẩn bị slide 1–3. | Checklist tên bảng/cột/kiểu/NULL/default/khóa khớp source; log nhân sự + benchmark + plan phù hợp. Giải thích `fn_TinhSoNgayCong` thuộc TV1 nhưng cài module 02, trigger xóa nhân viên cài module 05. Demo địa chỉ cập nhật rồi khôi phục, giữ email `.invalid`. |
| **TV2 — Phạm Minh Quân, 24110311** | **Đo benchmark index CHAMCONG hiện hành** theo mục 2; chọn log test module/batch rollback/trigger đã PASS. Kiểm từ điển CHAMCONG và view tổng hợp; chuẩn bị slide 4–6. | Log IO/TIME, actual plans trước/sau cho cả hai cặp truy vấn, kết quả dòng giống nhau, metadata index và cleanup; bảng số đo mới có ngày/môi trường/giới hạn. `fn_TongPhuCap` thuộc TV2, phối hợp TV3, cài module 03. Demo đọc công hiện có; trùng ngày là bài lỗi riêng. |
| **TV3 — Trần Tiến Đạt, 24110198** | Kiểm từ điển PHUCAPNHANVIEN/KHAUTRUNHANVIEN, tổng hợp/function và ownership; chọn log SQL/benchmark/plan đã PASS; chuẩn bị slide 7–9. | Log TV3 + benchmark đúng truy vấn; xác minh constraint số tiền/tháng/năm và transaction `sp_XoaKyLuongChuaChot`. Demo chỉ đọc khoản A đã có, không thử xóa kỳ chính. |
| **TV4 — Nguyễn Quang Vinh, 24110385** | Kiểm từ điển BANGLUONG/CHITIETBANGLUONG, công thức, snapshot lương, tên `MaChiTiet`; chọn E2E/benchmark đã PASS, phối hợp concurrency TV5; chuẩn bị slide 10–12. | E2E 10/10, benchmark khấu trừ 547→4 trên fixture 30.000 dòng, 128 dòng/tổng 133.356 và ghi rõ hint scan/seek. Số 8→2 của truy vấn nguồn công là phép đo khác, không thay benchmark index TV2. |
| **TV5 — Trần Đức Anh, 24110155** | Tổng hợp Word, deck ≤15 slide, package/README cài đặt; chọn minh chứng quyền và concurrency đã PASS; chuẩn bị slide 13–15, điều phối rehearsal. | Word 55 trang nội dung, PDF nếu yêu cầu, deck thực có; script/source/README và thư mục evidence mở được trên máy khác. Có minh chứng GRANT/REVOKE/DENY, danh tính Employee, hai phiên tính lương và bốn race khóa nguồn. Không gọi mô phỏng timeout là đã ngắt SQL trên GUI. |

Mỗi người ghi rõ tên file, ngày chạy, môi trường, testcase/truy vấn, kết quả và giới hạn; gửi TV5 các file gốc cần dùng. Đọc và đối chiếu lại phần mình trong Word, không sửa số đo bằng suy đoán. Nếu số mới khác, giữ log thật và giải thích thay đổi; chưa chạy thì giữ trạng thái chờ.

## 2. TV2 — cách đo và lưu minh chứng bắt buộc

Script đúng là [database/test_benchmark_index_TV2.sql](../database/test_benchmark_index_TV2.sql). Script tạo **#CHAMCONG_BENCHMARK tạm theo session**, 30.000 dòng; giữ UNIQUE `(MaNV, NgayChamCong)` làm baseline rồi thêm covering index INCLUDE `(GioVao, GioRa, TrangThai)`. Không xóa index/constraint của dbo.CHAMCONG và không xóa cache hệ thống.

1. Dùng SQL Server QA có schema 01–05 và SSMS với quyền phù hợp. Xác nhận `SELECT DB_NAME(), ORIGINAL_LOGIN(), @@VERSION;` trước khi đo. Có thể dùng QA hiện có để chạy script bảng tạm. Nếu cần tạo **QA mới riêng**, launcher [run_sql_verification.ps1](../run_sql_verification.ps1) có `-KeepDatabase` để giữ database tên ngẫu nhiên mà nó tạo; lấy đúng tên từ output. Không chạy installer trực tiếp trên database nguồn để chuẩn bị benchmark.
2. Trong SSMS chọn đúng QA, mở toàn bộ script, bật **Include Actual Execution Plan** (`Ctrl+M`) và giữ cùng cửa sổ/connection xuyên các batch `GO`; bảng tạm phải tồn tại cùng session. Script đã bật `STATISTICS IO/TIME` ở hai phần trước/sau. Chạy toàn bộ từ tạo fixture đến cleanup; không chạy riêng đoạn AFTER trên session mới.
3. Lưu toàn bộ **Messages** thành `TV2_benchmark_messages.log`; lưu Results thể hiện số dòng/giá trị cả trước và sau. Lưu actual plan của BEFORE 1.1, BEFORE 1.2, AFTER 3.1, AFTER 3.2 thành bốn `.sqlplan` đặt tên rõ truy vấn. Không dùng estimated plan thay actual plan.
4. Đối chiếu **1.1 ↔ 3.1** (tra chi tiết) và **1.2 ↔ 3.2** (tổng hợp): cùng kết quả, logical reads, CPU/elapsed và operator. Baseline đã có unique index, nên mô tả seek + lookup so với covering seek theo plan thực; không gọi baseline là bảng chưa có index. Script dùng hint chọn index nên đây là phép đo fixture/hint, không chứng minh optimizer luôn chọn index đó ở mọi workload.
5. Lưu kết quả metadata cuối script: index thật `IX_CHAMCONG_MaNV_Ngay` có key và INCLUDE đúng; Messages phải xác nhận 30.000 dòng và đã dọn bảng tạm, không có lỗi cleanup. Kiểm bốn plan mở được, plan chứa runtime counters và đúng truy vấn. CPU/elapsed có thể 0 hoặc dao động; không ép số đẹp.
6. Tạo `TV2_benchmark_summary.md` ghi ngày chạy, source commit, SQL Server/database, fixture, truy vấn, số dòng và bảng before/after cho **từng cặp**, kèm tên log/plan. Số 574→5 hiện chỉ là lịch sử; thay trong báo cáo/slide bằng số mới **sau khi có bằng chứng**, không tự nhận số đó là kết quả hiện hành.

Lưu mới tại `build/evidence/TV2/<ngay-chay>/` rồi bàn giao bản sao vào `Bo_nop_Nhom06/evidence/TV2/` khi đóng gói. Hai đường dẫn này là **đầu ra đề xuất, chưa được tạo trong lượt sửa hướng dẫn**. `run_sql_verification.ps1 -Benchmarks` hiện chỉ chạy benchmark TV1, TV3, TV4; lệnh đó **không thu benchmark TV2**, cũng không xuất actual plan TV2. TV2 phải chạy script SSMS riêng như trên. Nếu đã tạo QA mới với `-KeepDatabase`, ghi tên database và bàn giao việc dọn đúng database do lượt đó tạo cho người quản lý QA sau khi lưu đủ minh chứng; không dọn QA GUI đang dùng chung.

- [ ] Log đầy đủ, môi trường/source và cleanup rõ.
- [ ] Results của hai cặp truy vấn giống nhau trước/sau.
- [ ] Bốn actual plans đúng truy vấn, có số liệu runtime.
- [ ] Metadata index thật có key/INCLUDE đúng.
- [ ] Summary và số đo trong Word/slide khớp file gốc; TV5 đã nhận bản sao.

## 3. Chọn bằng chứng đã PASS để bàn giao

Các đường dẫn sau đã có trên máy QA, nhưng `build/` bị Git bỏ qua. TV1/TV3/TV4 chọn đúng log tương ứng, đọc xác nhận PASS và nguồn đo, không cần chạy lại toàn bộ chỉ để đổi ngày log.

| Người | File có thể dùng / phần cần đối chiếu |
|---|---|
| TV1 | `build/sql-verification/20261008_101955_11b4e24c/test_module_nhansu_TV1.log`, `benchmark_test_benchmark_index_TV1.log`; `build/fix-verification/TV1_ActualPlan_*.sqlplan`. Fixture 5.000 dòng, logical reads 75→10. |
| TV2 | `build/sql-verification/20261008_101955_11b4e24c/test_module_chamcong_TV2.log` (21 PASS/0 FAIL) và source `ChamCongService.nhapChamCongTheoLo`; bổ sung benchmark/plan mới ở mục 2. |
| TV3 | `build/sql-verification/20261008_101955_11b4e24c/test_module_phucap_khautru_TV3.log`, `benchmark_test_benchmark_index_TV3.log`; `build/fix-verification/TV3_ActualPlan_*.sqlplan`. Fixture 30.000 dòng, logical reads 642→2. |
| TV4 | `build/sql-verification/20261008_101955_11b4e24c/TV4_Payroll_E2E.log`, `benchmark_TV4_Payroll_Benchmark.log`; đối chiếu IO/TIME và script benchmark, chọn plan cùng phép đo nếu có; thiếu file nào thì ghi chờ, không gán plan TV khác. |
| TV5 | `build/sql-verification/20261008_101955_11b4e24c/test_security_roles_TV5.log`, `Closed_Source_Concurrency.log`; `build/fix-verification/Concurrency_A.log`, `Concurrency_B.log`, `TV5_ActualPlan_*.sqlplan`; `build/identity-agent/identity.log`, `provision.log`, `config.log`, `cleanup.log`. |

TV5 ghép số đo với **đúng log và plan của cùng truy vấn/fixture**, không chọn theo thứ tự số file. Benchmark TV5 142→15 dùng plan 4/5; không lấy plan 6 trả 0 dòng làm cải thiện. Hai session payroll có B chờ 20631 ms, không trùng kỳ/chi tiết và cleanup; bốn race nguồn bị từ chối, nguồn giữ nguyên. Các log này chứng minh case đã chạy, không phải mọi workload hoặc phục hồi từ backup.

Java: `build/test-results/Java_Verification_20261009_225255_650.log` có GuiQa 55 assertions; `build/gui-qa/20261009/fix-SecurityRegressionTest.log`, `fix-LightThemeRegressionTest.log`, `fix-LoginChipRegressionTest.log` lần lượt 104/45/20. Tổng **224**, không cộng identity suite 50 checks vào đó. GUI: [GUI_TEST_REPORT_20261009](GUI_TEST_REPORT_20261009.md), ảnh/log ở `build/gui-qa/20261009/` và ảnh sau sửa tracked tại `docs/screenshots/gui-qa-fixes/`.

## 4. TV5 — đóng gói và rehearsal

Thư mục đề xuất `Bo_nop_Nhom06/` gồm `report/`, `slides/`, `source/`, `database/`, `evidence/TV1..TV5/`, `README_BO_NOP.md`. Đây là cấu trúc cần tạo khi đóng gói, chưa phải package đã hoàn thành.

- [ ] Có Word cuối **55 trang nội dung**, kiểm mục lục/số trang; xuất PDF nếu yêu cầu, kiểm mở được và đúng pagination. Không tính bìa/mục lục/phụ lục vào 55.
- [ ] Có file `.pptx` hoặc định dạng deck được yêu cầu, **≤15 slide**; tên/MSSV/ownership đúng bảng mục 1, mỗi người biết phần mình. Các guide Markdown không thay cho deck.
- [ ] Có source Java, thư viện/cách lấy JDBC driver, launcher và README cài/chạy; không đóng gói mật khẩu thật. Tài khoản QA được provision theo hướng dẫn, không giả định profile tự tạo SQL login.
- [ ] Có SQL schema **01→05**; `06_Demo_Data.sql` là seed riêng, không tự chạy để đổi fixture GUI. Nộp bộ script tái tạo cùng hướng dẫn provisioning hoặc `.bak` nếu nhóm chọn backup; không ghi đã backup/restore khi chưa làm.
- [ ] Chép **file gốc** log/plan/ảnh cần dùng từ `build/` vào package; kiểm link/mục tham chiếu trong Word và README. Commit các guide/Word không tự mang theo `build/`.
- [ ] Có danh mục evidence: đường dẫn tương đối trong package, người phụ trách, ngày/môi trường, testcase/truy vấn, kết quả/giới hạn; đủ rubric thiết kế/constraint/trigger/view/index/SP/UDF/transaction/security/concurrency.
- [ ] Minh chứng quyền có login/user/role thật, GRANT/REVOKE/DENY, Employee chỉ xem phiếu riêng; không dùng phiên DBA lọc MaNV để chứng minh quyền Employee. Concurrency có cả log A/B, race nguồn và cleanup.
- [ ] Có phân công và thông tin phiên bản/lịch sử đóng góp; mở package trên máy khác để kiểm file/link, không phụ thuộc đường dẫn `build/` trên máy QA.
- [ ] Rehearsal dùng **10/2026** theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md): TV1 cập nhật/khôi phục địa chỉ giữ email; TV2 đọc công, bài trùng ngày riêng; TV3 đọc khoản; TV4 tính; TV5 chốt/xem phiếu/mở lại. Không thêm trùng hoặc xóa nguồn để ép 730.000đ. Kết thúc `CHUA_CHOT`, `NgayChot IS NULL`, A 730.000đ/B 0đ, nguồn không đổi.
- [ ] Ghi kết quả lượt tập thật và phần chưa thực hiện; chỉ đánh dấu hoàn tất sau khi có đầu ra và kiểm được. Hướng dẫn này không tuyên bố benchmark TV2, deck, package hoặc rehearsal mới đã PASS.
