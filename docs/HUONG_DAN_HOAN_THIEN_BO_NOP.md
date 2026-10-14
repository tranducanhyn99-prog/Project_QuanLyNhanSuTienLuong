# Hướng dẫn hoàn thiện bộ nộp — Nhóm 06

Cập nhật 14/10/2026 theo các PR #44–#46, [phân công](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md), [rubric README](../README.md), [FIX_TASKLIST](FIX_TASKLIST.md) và báo cáo Word cuối kỳ. Đây là danh sách việc cần bàn giao; các mục 4.1–4.2 lưu mốc lịch sử, **chưa hoàn tất nghiệm thu toàn nhóm**. Giữ nguyên ownership SQL/Java; vị trí cài function/trigger ở module phụ thuộc không chuyển người phụ trách.

Báo cáo [Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx](Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx) đã có benchmark TV2 ngày 13/10 trong §8.5/§9.3; [deck chung](../Bo_nop_Nhom06/slides/Nhom06_ThuyetTrinh_15_Slides.pptx) có đúng 15 slide, TV2 ở slide 4–6. Bản Word sau sửa trạng thái đã xuất kiểm bằng Microsoft Word, vẫn **55 trang nội dung, 61 trang tổng**. Các log đã PASS có thể dùng lại khi source và phạm vi đo tương ứng. Việc còn lại là xác nhận phần TV1/TV3, tập dượt chung, kiểm cài/chạy trên máy nhận và xuất PDF nộp nếu yêu cầu.

## 1. Đầu ra và trách nhiệm từng thành viên

| Thành viên | Việc phải hoàn thiện | Đầu ra bàn giao / tiêu chí nhận |
|---|---|---|
| **TV1 — Nguyễn Minh Trí, 24110359** | Kiểm ERD, 10 bảng, PK/FK, từ điển dữ liệu và 3NF khớp SQL 01–05; có LICHSULUONG, NHANVIEN.NgayNghiViec, TAIKHOAN.SqlLogin. Chọn log/plan nhân sự đã PASS; chuẩn bị slide 1–3. | Checklist tên bảng/cột/kiểu/NULL/default/khóa khớp source; log nhân sự + benchmark + plan phù hợp. Ảnh `TV1_Database_Diagram.png` thiếu LICHSULUONG nên phải chụp ERD mới nếu đưa vào báo cáo/deck. Giải thích `fn_TinhSoNgayCong` thuộc TV1 nhưng cài module 02, trigger xóa nhân viên cài module 05. Demo địa chỉ cập nhật rồi khôi phục, giữ email `.invalid`. |
| **TV2 — Phạm Minh Quân, 24110311** | Benchmark index CHAMCONG và slide 4–6 đã bàn giao qua PR #44; dùng [biên nhận TV5](../Bo_nop_Nhom06/evidence/TV2/README_TV5_RECEIPT.md) và bản hiệu đính v3. Kiểm từ điển CHAMCONG, view tổng hợp và phần trình bày của mình. | 52 file minh chứng, bốn actual plans, log IO/TIME, metadata và cleanup đã nhận; giữ Messages 574→5 riêng với XML 522→5 trên fixture 30.000 dòng. Năm ảnh benchmark cũ và ba ảnh UI lệch kỳ chỉ dùng làm tư liệu lịch sử. `fn_TongPhuCap` thuộc TV2, phối hợp TV3, cài module 03. Demo đọc công hiện có; trùng ngày là bài lỗi riêng. |
| **TV3 — Trần Tiến Đạt, 24110198** | Kiểm từ điển PHUCAPNHANVIEN/KHAUTRUNHANVIEN, tổng hợp/function và ownership; chọn log SQL/benchmark/plan đã PASS; chuẩn bị slide 7–9. | Log TV3 + benchmark đúng truy vấn; xác minh constraint số tiền/tháng/năm và transaction `sp_XoaKyLuongChuaChot`. Ảnh SQL/GUI trong `screenshots/TV3/` minh họa lần chạy cũ, không đại diện cho fixture hiện tại; nếu dùng thì ghi rõ ngày/phạm vi, còn xác nhận source hiện hành dựa trên log mới. Demo chỉ đọc khoản A đã có, không thử xóa kỳ chính. |
| **TV4 — Nguyễn Quang Vinh, 24110385** | Kiểm từ điển BANGLUONG/CHITIETBANGLUONG, công thức, snapshot lương, tên `MaChiTiet`; chọn E2E/benchmark đã PASS, phối hợp concurrency TV5; chuẩn bị slide 10–12. | E2E 10/10, benchmark khấu trừ 547→4 trên fixture 30.000 dòng, 128 dòng/tổng 133.356 và ghi rõ hint scan/seek. Ảnh `E2E_10Steps_Pass.png`, `Concurrency_2Sessions.png`, `Benchmark_*` và `Trigger_KhoaKyDaChot.png` là lần chạy 30/09 hoặc chưa ghép được log cùng lượt; dùng log QA mới ở mục 3 thay cho ảnh nếu cần minh chứng hiện hành. Số 8→2 của truy vấn nguồn công là phép đo khác, không thay benchmark index TV2. |
| **TV5 — Trần Đức Anh, 24110155** | Tổng hợp Word, deck ≤15 slide, package/README cài đặt; chọn minh chứng quyền và concurrency đã PASS; chuẩn bị slide 13–15, điều phối rehearsal. | Word 55 trang nội dung, PDF nếu yêu cầu, deck thực có; script/source/README và thư mục evidence mở được trên máy khác. Ảnh quyền `TV5_Security_Roles_Verification.png` là kiểm bằng `EXECUTE AS USER`, chỉ chứng minh quyền object trong bài thử, chưa chứng minh lọc dữ liệu theo `ORIGINAL_LOGIN`; dùng log identity hiện hành cùng test đăng nhập thật. Ảnh concurrency cũng là kỳ 1/2020 cũ. Có minh chứng GRANT/REVOKE/DENY, danh tính Employee, hai phiên tính lương và bốn race khóa nguồn. Không gọi mô phỏng timeout là đã ngắt SQL trên GUI. |

Mỗi người ghi rõ tên file, ngày chạy, môi trường, testcase/truy vấn, kết quả và giới hạn; gửi TV5 các file gốc cần dùng. Đọc và đối chiếu lại phần mình trong Word, không sửa số đo bằng suy đoán. Nếu số mới khác, giữ log thật và giải thích thay đổi; chưa chạy thì giữ trạng thái chờ.

## 2. TV2 — phép đo đã nhận và cách kiểm lại

[Bộ evidence TV2](../Bo_nop_Nhom06/evidence/TV2/README_TV5_RECEIPT.md) từ PR #44 đã có đủ 52 file, bốn actual plans và ba slide 4–6. TV5 dùng [bảng hiệu đính v3](../Bo_nop_Nhom06/evidence/TV2/presentation/reviewed_v2/TV5_Proposed_Corrections_v3.md) cùng phụ lục v3 do TV2 bàn giao; PR #46 hiệu đính biên nhận TV5. Không lấy thời gian sai hoặc lời dẫn cũ từ bản v2. Các bước dưới đây là quy trình tái kiểm, không phải phần TV2 còn chờ bàn giao.

Script đúng là [database/test_benchmark_index_TV2.sql](../database/test_benchmark_index_TV2.sql). Script tạo **#CHAMCONG_BENCHMARK tạm theo session**, 30.000 dòng; giữ UNIQUE `(MaNV, NgayChamCong)` làm baseline rồi thêm covering index INCLUDE `(GioVao, GioRa, TrangThai)`. Không xóa index/constraint của dbo.CHAMCONG và không xóa cache hệ thống.

1. Dùng SQL Server QA có schema 01–05 và SSMS với quyền phù hợp. Xác nhận `SELECT DB_NAME(), ORIGINAL_LOGIN(), @@VERSION;` trước khi đo. Có thể dùng QA hiện có để chạy script bảng tạm. Nếu cần tạo **QA mới riêng**, launcher [run_sql_verification.ps1](../run_sql_verification.ps1) có `-KeepDatabase` để giữ database tên ngẫu nhiên mà nó tạo; lấy đúng tên từ output. Không chạy installer trực tiếp trên database nguồn để chuẩn bị benchmark.
2. Trong SSMS chọn đúng QA, mở toàn bộ script, bật **Include Actual Execution Plan** (`Ctrl+M`) và giữ cùng cửa sổ/connection xuyên các batch `GO`; bảng tạm phải tồn tại cùng session. Script đã bật `STATISTICS IO/TIME` ở hai phần trước/sau. Chạy toàn bộ từ tạo fixture đến cleanup; không chạy riêng đoạn AFTER trên session mới.
3. Lưu toàn bộ **Messages** thành `TV2_benchmark_messages.log`; lưu Results thể hiện số dòng/giá trị cả trước và sau. Lưu actual plan của BEFORE 1.1, BEFORE 1.2, AFTER 3.1, AFTER 3.2 thành bốn `.sqlplan` đặt tên rõ truy vấn. Không dùng estimated plan thay actual plan.
4. Đối chiếu **1.1 ↔ 3.1** (tra chi tiết) và **1.2 ↔ 3.2** (tổng hợp): cùng kết quả, logical reads, CPU/elapsed và operator. Baseline đã có unique index, nên mô tả seek + lookup so với covering seek theo plan thực; không gọi baseline là bảng chưa có index. Script dùng hint chọn index nên đây là phép đo fixture/hint, không chứng minh optimizer luôn chọn index đó ở mọi workload.
5. Lưu kết quả metadata cuối script: index thật `IX_CHAMCONG_MaNV_Ngay` có key và INCLUDE đúng; Messages phải xác nhận 30.000 dòng và đã dọn bảng tạm, không có lỗi cleanup. Kiểm bốn plan mở được, plan chứa runtime counters và đúng truy vấn. CPU/elapsed có thể 0 hoặc dao động; không ép số đẹp.
6. Đối chiếu `TV2_benchmark_summary.md` với log/plan ngày 13/10: 273 dòng chi tiết, 9 dòng tổng hợp; Messages 574→5 và XML 522→5 là hai nguồn đo khác nhau. Giữ điều kiện hint/warm cache và giới hạn thời gian dưới 1 ms trong báo cáo/slide; không coi các ảnh benchmark cũ là lượt đo mới.

File gốc đã nằm trong `Bo_nop_Nhom06/evidence/TV2/`. Nếu đo lại, lưu run mới riêng tại `build/evidence/TV2/<ngay-chay>/`, ghi rõ nguồn rồi mới cập nhật package. `run_sql_verification.ps1 -Benchmarks` chỉ chạy benchmark TV1, TV3, TV4; không thu benchmark/actual plan TV2. Dùng script SSMS riêng như trên và không dọn QA GUI đang dùng chung.

- [x] Log đầy đủ, môi trường/source và cleanup rõ.
- [x] Results của hai cặp truy vấn giống nhau trước/sau.
- [x] Bốn actual plans đúng truy vấn, có số liệu runtime.
- [x] Metadata index thật có key/INCLUDE đúng.
- [x] Summary và số đo trong Word/slide khớp file gốc; TV5 đã nhận bản sao.

## 3. Chọn bằng chứng đã PASS để bàn giao

Các đường dẫn sau đã có trên máy QA, nhưng `build/` bị Git bỏ qua. TV1/TV3/TV4 chọn đúng log tương ứng, đọc xác nhận PASS và nguồn đo, không cần chạy lại toàn bộ chỉ để đổi ngày log.

| Người | File có thể dùng / phần cần đối chiếu |
|---|---|
| TV1 | `build/sql-verification/20261008_101955_11b4e24c/test_module_nhansu_TV1.log`, `benchmark_test_benchmark_index_TV1.log`; `build/fix-verification/TV1_ActualPlan_*.sqlplan`. Fixture 5.000 dòng, logical reads 75→10. |
| TV2 | `build/sql-verification/20261008_101955_11b4e24c/test_module_chamcong_TV2.log` (21 PASS/0 FAIL) và source `ChamCongService.nhapChamCongTheoLo`; benchmark/plan mới xem [bộ evidence đã nhận](../Bo_nop_Nhom06/evidence/TV2/README_TV5_RECEIPT.md). |
| TV3 | `build/sql-verification/20261008_101955_11b4e24c/test_module_phucap_khautru_TV3.log`, `benchmark_test_benchmark_index_TV3.log`; `build/fix-verification/TV3_ActualPlan_*.sqlplan`. Fixture 30.000 dòng, logical reads 642→2. |
| TV4 | `build/sql-verification/20261008_101955_11b4e24c/TV4_Payroll_E2E.log`, `benchmark_TV4_Payroll_Benchmark.log`; đối chiếu IO/TIME và script benchmark, chọn plan cùng phép đo nếu có; thiếu file nào thì ghi chờ, không gán plan TV khác. |
| TV5 | `build/sql-verification/20261008_101955_11b4e24c/test_security_roles_TV5.log`, `Closed_Source_Concurrency.log`; `build/fix-verification/Concurrency_A.log`, `Concurrency_B.log`, `TV5_ActualPlan_*.sqlplan`; `build/identity-agent/identity.log`, `provision.log`, `config.log`, `cleanup.log`. |

TV5 ghép số đo với **đúng log và plan của cùng truy vấn/fixture**, không chọn theo thứ tự số file. Benchmark TV5 142→15 dùng plan 4/5; không lấy plan 6 trả 0 dòng làm cải thiện. Hai session payroll có B chờ 20631 ms, không trùng kỳ/chi tiết và cleanup; bốn race nguồn bị từ chối, nguồn giữ nguyên. Các log này chứng minh case đã chạy, không phải mọi workload hoặc phục hồi từ backup.

### 3.1 Rà soát thư mục `screenshots/`

Thư mục gốc có 44 file ảnh (41 nội dung duy nhất; ba cặp trùng byte: báo cáo chốt lương, quản trị tài khoản và hai phiên concurrency). Ảnh mở được và phần lớn phù hợp làm **tư liệu minh họa cho lần chạy được chụp**, nhưng không thể mặc định là minh chứng source/runtime hiện tại: nhiều ảnh dùng giao diện/fixture kỳ 9/2026, ảnh SQL ghi ngày 30/09–06/10, còn log xác minh hiện hành ở mục trên chạy 08–09/10. Giữ file trùng nếu cần lịch sử; trong package chỉ chép một bản và ghi nguồn/ngày.

| Thành viên | Ảnh sẵn có và kết luận để cập nhật checklist |
|---|---|
| TV1 | `TV1_Database_Diagram.png` chỉ vẽ 9 bảng, thiếu `LICHSULUONG`; không dùng làm ERD cuối. `TV1_Trigger_ChanXoaNhanVien.png` là lỗi chặn xóa đúng theo thông báo, còn ảnh transaction rollback thể hiện lỗi tạo tài khoản trùng và không có dòng nhân viên sau đó; cả hai là ảnh lịch sử, không thay log PASS đã lưu. Benchmark nhân sự hiển thị 4/7 dòng và 2 logical reads, nhưng không có thông tin commit/schema để xác nhận cùng source hiện tại. |
| TV2 | Năm ảnh benchmark được mô tả trong [báo cáo chuyên đề TV2](TV2_BaoCao_ChuyenDe_ChamCong_CuoiKy.md) là lịch sử; benchmark dùng fixture/index cũ. Trong ba ảnh UI, ảnh tổng hợp/điều chỉnh xem kỳ 9/2026, còn form chi tiết chọn 06/10/2026 nhưng bảng vẫn hiển thị bản ghi tháng 9. Không dùng chúng để khẳng định UI hoặc số đo hiện tại; lấy [lượt đo 13/10](../Bo_nop_Nhom06/evidence/TV2/README_TV5_RECEIPT.md). |
| TV3 | Ảnh test SQL cho thấy lần chạy cũ có kết quả PASS với constraint, trigger và transaction; ảnh thêm phụ cấp có cả lỗi âm tiền và thêm/xóa thành công. Chúng hỗ trợ minh họa hành vi, nhưng không kèm mã commit và có dữ liệu tháng 9/2026; dùng log hiện hành làm bằng chứng chính. |
| TV4 | Ảnh form tính lương và báo cáo chốt lương có bảng rỗng; form phụ cấp có sẵn 730.000 nhưng không cho thấy lưu thành công, nhân viên được chọn hay đúng kỳ 10/2026. Ảnh E2E/trigger/concurrency là SQL history; kết quả hiện hành lấy từ log QA 08/10, ghép plan cùng lượt nếu cần. |
| TV5 | Ảnh benchmark/trigger cho thấy lượt kiểm thử SQL riêng; ảnh role chứng minh các quyền object được từ chối trong `EXECUTE AS USER`, chưa chứng minh kiểm soát row theo login thật. Ảnh tài khoản/báo cáo là giao diện cũ và bảng rỗng. Dùng log identity, security, concurrency hiện hành ở `build/` rồi chép đúng file gốc vào package. |

Ba ảnh đã track tại `docs/screenshots/gui-qa-fixes/` thuộc GUI QA ngày 09/10 và chỉ hỗ trợ các lỗi giao diện F01–F03 được ghi trong [báo cáo GUI](GUI_TEST_REPORT_20261009.md); không mở rộng kết luận thành xác nhận mọi màn hình. Checklist đóng gói bên dưới cần ghi riêng ngày, commit, môi trường, thao tác và log đối chiếu cho từng ảnh mới.

Java: `build/test-results/Java_Verification_20261009_225255_650.log` có GuiQa 55 assertions; `build/gui-qa/20261009/fix-SecurityRegressionTest.log`, `fix-LightThemeRegressionTest.log`, `fix-LoginChipRegressionTest.log` lần lượt 104/45/20. Tổng **224**, không cộng identity suite 50 checks vào đó. GUI: [GUI_TEST_REPORT_20261009](GUI_TEST_REPORT_20261009.md), ảnh/log ở `build/gui-qa/20261009/` và ảnh sau sửa tracked tại `docs/screenshots/gui-qa-fixes/`.

## 4. TV5 — đóng gói và rehearsal

Thư mục [Bo_nop_Nhom06](../Bo_nop_Nhom06/README_BO_NOP.md) đã có `report/`, `slides/`, `source/`, `database/`, `evidence/TV1..TV5/`, `README_BO_NOP.md`. Benchmark TV2 và deck 15 slide đã tích hợp; việc xác nhận TV1/TV3, rehearsal và thử máy nhận còn mở. Mục 4.1–4.2 ghi lại trạng thái tại ngày nêu trong tiêu đề.

- [x] Có Word cuối với benchmark TV2 trong §8.5/§9.3; bản sau sửa trạng thái đã xuất kiểm **55 trang nội dung/61 trang tổng**. Nếu nộp PDF, xuất từ bản Word này.
- [x] Có [deck chung 15 slide](../Bo_nop_Nhom06/slides/Nhom06_ThuyetTrinh_15_Slides.pptx), đúng tên/MSSV/ownership; TV2 ở slide 4–6. Phần tập nói của từng người vẫn cần xác nhận.
- [ ] Có source Java, thư viện/cách lấy JDBC driver, launcher và README cài/chạy; không đóng gói mật khẩu thật. Tài khoản QA được provision theo hướng dẫn, không giả định profile tự tạo SQL login.
- [ ] Có SQL schema **01→05**; `06_Demo_Data.sql` là seed riêng, không tự chạy để đổi fixture GUI. Nộp bộ script tái tạo cùng hướng dẫn provisioning hoặc `.bak` nếu nhóm chọn backup; không ghi đã backup/restore khi chưa làm.
- [ ] Chép **file gốc** log/plan/ảnh cần dùng từ `build/` vào package; kiểm link/mục tham chiếu trong Word và README. Commit các guide/Word không tự mang theo `build/`.
- [ ] Có danh mục evidence: đường dẫn tương đối trong package, người phụ trách, ngày/môi trường, testcase/truy vấn, kết quả/giới hạn; đủ rubric thiết kế/constraint/trigger/view/index/SP/UDF/transaction/security/concurrency.
- [ ] Minh chứng quyền có login/user/role thật, GRANT/REVOKE/DENY, Employee chỉ xem phiếu riêng; không dùng phiên DBA lọc MaNV để chứng minh quyền Employee. Concurrency có cả log A/B, race nguồn và cleanup.
- [ ] Có phân công và thông tin phiên bản/lịch sử đóng góp; mở package trên máy khác để kiểm file/link, không phụ thuộc đường dẫn `build/` trên máy QA.
- [ ] Rehearsal dùng **10/2026** theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md): TV1 cập nhật/khôi phục địa chỉ giữ email; TV2 đọc công, bài trùng ngày riêng; TV3 đọc khoản; TV4 tính; TV5 chốt/xem phiếu/mở lại. Không thêm trùng hoặc xóa nguồn để ép 730.000đ. Kết thúc `CHUA_CHOT`, `NgayChot IS NULL`, A 730.000đ/B 0đ, nguồn không đổi.
- [ ] Ghi kết quả lượt tập thật và phần chưa thực hiện; chỉ đánh dấu hoàn tất sau khi có đầu ra và kiểm được. Chưa xác nhận cài package trên máy khác hoặc rehearsal chung đã PASS.

### 4.1 Tiến độ TV5 — 11/10/2026

Các mục dưới đây chỉ ghi phần TV5 có thể làm trước khi nhận bàn giao TV1–TV4. Chúng không tự hoàn tất các checklist toàn nhóm ở trên.

Thư mục package được lưu trên Git, có manifest SHA-256 toàn bộ file. `Bo_nop_Nhom06_TV5_20261011.zip` là bản local để chuyển máy, không commit vì trùng nội dung thư mục; sau khi checkout có thể tạo lại bằng `Compress-Archive -Path .\Bo_nop_Nhom06 -DestinationPath .\Bo_nop_Nhom06_TV5_20261011.zip -Force` tại root repo. Đã kiểm bản ZIP local mở được và byte từng file khớp thư mục; đây chưa phải kiểm cài/chạy trên máy mới.

- [x] Rà lại phần TV5 trong Word theo source và log đã có; giữ bản Word nguyên byte trong `report/`, ghi SHA-256 và phạm vi kiểm tại [TV5_Ra_Soat_Bao_Cao.md](../Bo_nop_Nhom06/report/TV5_Ra_Soat_Bao_Cao.md).
- [x] Tạo [PowerPoint TV5 slide 13–15](../Bo_nop_Nhom06/slides/TV5_Slide_13-15.pptx), có ghi chú thuyết trình và đúng tên/MSSV. Đây là ba slide để ghép vào deck chung ≤15 slide.
- [x] Gom log security, identity, concurrency A/B, bốn race nguồn và actual plans TV5; ghi nguồn, ngày chạy, môi trường, kết quả/giới hạn, đối chiếu hash trong `evidence/TV5/`. Không coi ảnh lịch sử là xác nhận runtime mới.
- [x] Chuẩn bị source Java, JDBC/thư viện, launcher, SQL và [README cài/chạy](../Bo_nop_Nhom06/README_BO_NOP.md); các phần evidence TV1–TV3 còn chờ lựa chọn/bàn giao. Không đóng gói cấu hình local có mật khẩu quản trị.
- [x] Viết [kịch bản tập demo TV5](../Bo_nop_Nhom06/TV5_TAP_DEMO.md) chốt → HR bị từ chối → Employee A/B → mở lại, kỳ 10/2026; có điều kiện bắt đầu, kết quả kỳ vọng, phương án dùng evidence và bảng ghi kết quả trống.
- [ ] Tập demo thật và xác nhận trạng thái cuối. Kiểm tra chỉ đọc 11/10: SQL Server `MSSQLSERVER` đang dừng, cổng 1433 chưa mở. Chưa ghi PASS cho một lượt rehearsal mới.
- [ ] Nhận ERD cuối TV1, benchmark/Results/bốn plans hiện hành TV2 và slide/minh chứng TV1–TV3 (TV4 đã nhận); ghép deck chung, cập nhật Word bằng số đo thật nếu cần.
- [ ] Cài/chạy trên máy khác, kiểm file/link và xuất PDF từ Word cuối nếu yêu cầu. Bản sao source và hướng dẫn chưa thay cho phép thử cài mới.


### 4.2 Bàn giao TV4 — 13/10/2026

[PR #41](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/41) đã merge vào main; bản bàn giao được tích hợp trong [evidence TV4](../Bo_nop_Nhom06/evidence/TV4/README_EVIDENCE.md). Các ảnh ở thư mục screenshots gốc vẫn là lịch sử theo mục 3.1; bộ TV4 mới có nguồn/ngày riêng, không sửa phân loại ảnh cũ.

- [x] Nhận đủ Word phần TV4, slide 10–12 có notes, 5 ảnh, 5 log, 2 actual plans và metadata ngày/môi trường/source.
- [x] Sửa hai bảng schema trong Word: độ rộng cột/font 11pt, header lặp; giữ nguyên nội dung, render và xem đủ 4 trang.
- [x] Đối chiếu E2E 10/10, benchmark 547→4 reads/128 khoản/tổng 133356 và cleanup; hai plan có actual counters. Source Java/SQL của lần thu ở 62223d5 khớp source hiện tại.
- [x] Giữ log TV4 A/B ngày 10/10 (B 21072 ms) riêng với TV5 ngày 07/10 (B 20631 ms). GUI TV4 ngày 11/10 có 31 kiểm tra tự động; không cộng vào bộ 224 assertions offline hoặc nhận là rehearsal tay.
- [x] Chép file gốc vào bộ nhóm, ghi SHA-256 và transformation duy nhất của Word; bỏ marker chờ TV4, cập nhật guide/README/manifest. Có các phần slide 10–15, còn chờ 1–9.
- [ ] TV4 tập nói và thao tác demo tay 10/2026 cùng nhóm; xác nhận fixture trên QA demo chung vì QA của lượt TV4 đã dọn.
- [ ] TV1–TV3 bàn giao phần còn thiếu, đặc biệt ERD TV1 và benchmark CHAMCONG TV2; ghép deck ≤15 slide, kiểm máy khác và rehearsal chung.

Không thay số đo của lượt 07–09/10 bằng số mới và không tự cộng 4 trang Word TV4 vào báo cáo nhóm 55 trang nội dung. Source ứng dụng và dữ liệu database không bị sửa trong lượt tích hợp tài liệu này.
