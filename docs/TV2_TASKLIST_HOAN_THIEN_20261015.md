# TV2 hướng dẫn hoàn thành các việc còn lại

**Người phụ trách:** Phạm Minh Quân — TV2 — MSSV 24110311.

**Phối hợp:** Trần Đức Anh — TV5.

**Ngày lập:** 15/10/2026. Đối chiếu snapshot `4e12940` trên nhánh TV5 (PR #49); bằng chứng TV2 đã nhận từ PR #44 và hiệu đính sử dụng tại PR #46/#47.

TV2 đã bàn giao source, benchmark canonical v3 ngày 13/10/2026, bốn actual plans và slide 4–6. Nhóm đã phê duyệt quyết định triển khai nội bộ **Phương án B (Option B: sửa tối thiểu SQL TRY...CATCH + bare THROW trong `sp_GhiNhanChamCong`)**. Các minh chứng thử nghiệm trước đây (job 004 34 assertions, job 005 59 assertions, và đợt trung gian 174112) đã được lưu trữ dưới dạng lịch sử superseded và được thay thế hoàn toàn bằng bộ test 87 assertions nghiêm ngặt trên cơ sở dữ liệu QA dùng một lần có kiểm soát quyền sở hữu (`PRJ_Fix_QA_20261015_175527_ea6da333`) với **100% PASS** toàn diện: kiểm chứng 8 negative guards từ chối trước mutation, SQL suite 21/21 PASS, Concurrency PASS, JDBC Integration 87/87 assertions PASS chuẩn UTF-8 kiểm tra typed `SQLServerError` (Number=50000, Severity=16, State=1), đi kèm `.gitattributes` cục bộ miễn kiểm tra khoảng trắng cho file log thô và bản sao minh chứng Security/GuiQA offline. Bản sao package SQL `Bo_nop_Nhom06/database/02_Module_ChamCong_TV2.sql` và manifest `Bo_nop_Nhom06/SHA256_MANIFEST.csv` đã được đồng bộ chính xác (537 files verified). Trạng thái hiện tại: **Awaiting Independent Review** (chờ reviewer độc lập nghiệm thu trước khi chuyển giao Git; chưa tuyên bố PR được merge hay giảng viên đã nghiệm thu). Việc còn mở là TV5 cập nhật Báo cáo Word (đoạn 193/824) và Slide 5 speaker notes theo Ghi chú tích hợp tại mục 5, và tham gia rehearsal chung nhóm. Không yêu cầu đo lại benchmark hoặc tạo lại toàn bộ minh chứng đã hợp lệ.

## 1. Xác nhận cách xử lý lỗi và transaction

### Căn cứ cần đọc

| File | Nội dung cần đối chiếu |
|---|---|
| [Kế hoạch phân công](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md) | Bảng rubric ghi Stored Procedure ≥5 + TRY…CATCH; TV2 được giao transaction nhập công theo lô |
| [Module SQL TV2](../database/02_Module_ChamCong_TV2.sql) | `sp_GhiNhanChamCong` tiếp nhận 7 tham số, kiểm tra đầu vào, ném lỗi RAISERROR 16, ghi một dòng và trả `MaChamCong OUTPUT`; đã cập nhật Option B bao bọc TRY...CATCH với bare THROW; không tự mở/commit transaction (nhường Java Service quản lý lô); đã kiểm thử live SQL QA và JDBC integration test đạt 100% PASS trên database QA riêng |
| [ChamCongService](../src/main/java/com/service/ChamCongService.java) | `nhapChamCongTheoLo` validate cả lô, dùng một connection, tắt auto-commit, commit khi mọi dòng thành công và rollback khi lỗi |
| [ChamCongDAO](../src/main/java/com/dao/ChamCongDAO.java) | `insertInTransaction` dùng connection do service truyền vào; `insertSingle` là đường ghi đơn lẻ cần giữ hoạt động |
| [Log GUI TV2](../Bo_nop_Nhom06/evidence/TV5/gui-followup-20261013/tv2-attendance.log) và [giải thích QA](../Bo_nop_Nhom06/evidence/TV5/gui-followup-20261013/README.md) | Đã kiểm hai INSERT thực tới SQL trước khi dòng cuối lỗi trùng, sau đó Java rollback; lô hợp lệ commit ba dòng; tổng hợp tháng khớp điều chỉnh |

**Việc TV2 phải làm:**

- [x] Đọc các đường gọi trên và xác nhận bằng văn bản: service sở hữu transaction của cả lô, SP xử lý một dòng và chuyển lỗi về caller.
- [x] Cùng TV5 làm rõ rubric đòi TRY…CATCH ở từng SP được phân công: Người dùng đã phê duyệt quyết định nội bộ nhóm áp dụng **Phương án B (Option B)** (không tuyên bố giảng viên đã duyệt hay TV5 đã nghiệm thu) để triệt tiêu rủi ro rubric "Stored Procedure >= 5 + TRY...CATCH".
- [x] Chốt một trong hai phương án dưới đây và ghi vào tài liệu tiến độ: Đã chọn và thực thi **Phương án B** trên mã nguồn canonical `database/02_Module_ChamCong_TV2.sql`.

| Phương án | Khi áp dụng | Đầu ra cần bàn giao |
|---|---|---|
| **A. Giữ cơ chế hiện tại** | Nhóm/người duyệt chấp nhận transaction ở Java và cách SP phát lỗi hiện hành | Thuyết minh ngắn về quyền sở hữu transaction, đường truyền lỗi, bằng chứng rollback có sẵn và xác nhận của TV2 + TV5; không cần PR sửa code |
| **B. Sửa SQL để đáp ứng yêu cầu đã thống nhất** (ĐÃ CHỌN) | Xác nhận rằng SP TV2 bắt buộc có TRY…CATCH hoặc tìm được lỗi xử lý cụ thể | Bao bọc validation + INSERT + SCOPE_IDENTITY trong TRY/CATCH với bare THROW; bảo toàn 7 tham số, không mở transaction riêng để Java Service toàn quyền quản trị atomicity; PR ghi cả yêu cầu rubric lẫn hành vi thực tế |

Ưu tiên xác nhận thiết kế trước khi sửa. Không thêm catch chỉ để đếm cú pháp rồi báo đã khắc phục lỗi runtime. Nếu chọn B, không commit transaction của caller, không nuốt lỗi để Java tiếp tục commit và không thêm ROLLBACK vô điều kiện lên transaction do nơi khác quản lý. Giữ hợp đồng OUTPUT, validation, quyền SQL và đường ghi đơn lẻ.

**Hoàn tất mục này khi:** có phương án được phê duyệt; đã sửa code SQL Option B; tài liệu kỹ thuật đồng bộ; và kiểm chứng live QA đạt trên QA DB riêng.

## 2. Rà và cập nhật tài liệu cá nhân

- [x] Rà [tiến độ TV2](TV2_Tien_Do_Thuc_Hien.md), [báo cáo chuyên đề](TV2_BaoCao_ChuyenDe_ChamCong_CuoiKy.md) và [hướng dẫn thuyết trình](TV2_Huong_Dan_Thuyet_Trinh.md). Thêm cập nhật hiện tại tại những chỗ còn ghi benchmark mới chưa được nhận; giữ ngày và nhãn của phần lịch sử.
- [x] Ghi phương án transaction đã thống nhất ở mục 1, phân biệt kiểm SQL với đường nhập lô Java/GUI.
- [x] Dùng [biên nhận TV2](../Bo_nop_Nhom06/evidence/TV2/README_TV5_RECEIPT.md) và [bảng hiệu đính v3](../Bo_nop_Nhom06/evidence/TV2/presentation/reviewed_v2/TV5_Proposed_Corrections_v3.md) làm nguồn khi xác nhận Word/slide cuối.
- [x] Xác nhận slide 4–6 và phần chấm công trong Word nhóm đúng số liệu, ownership và giới hạn; TV5 đã cập nhật Word đoạn 193/824 và notes Slide 5 sau PR #52 (16/10).

Số liệu cần giữ đúng nguồn: fixture 30.000 dòng, 273 dòng chi tiết và 9 dòng tổng hợp; **Messages 574→5 reads**, **XML operator 522→5 reads** là hai nguồn đo khác nhau; nguyên nhân chênh lệch 52 reads ở baseline là chưa xác định (UNKNOWN). Không dùng việc ảnh cũ cũng có số 574→5 để coi ảnh đó là lượt đo 13/10. Không lấy ngày checkout hoặc ngày sửa tài liệu làm ngày chạy.

52 file TV2 gốc, raw log, plan, CSV, PPTX và manifest do TV2 bàn giao được giữ nguyên. Các hiệu đính sử dụng hiện nằm trong biên nhận TV5; không cần sửa lại biên bản v2 gốc. Nếu phải thu bằng chứng mới sau đổi code, lưu lượt mới riêng và ghi source commit, môi trường, ngày chạy, kết quả, cleanup và giới hạn.

## 3. Kiểm lại khi có thay đổi code

Nếu mục 1 chỉ chốt tài liệu và không đổi SQL/Java, có thể dẫn lại các log còn phù hợp; không bắt chạy lại suite chỉ để đổi ngày. Nếu sửa xử lý lỗi, kiểm ít nhất các đường bị ảnh hưởng sau trên QA riêng:

| Ca kiểm | Kết quả phải quan sát |
|---|---|
| Lô hoàn toàn hợp lệ | Mọi dòng được ghi đúng một lần; commit đủ lô |
| Hai dòng đã INSERT, dòng cuối trùng ngày | Hai dòng mới không còn sau rollback; dòng trùng đã có từ trước vẫn nguyên |
| Đầu vào sai bị Java chặn trước khi ghi | Không phát sinh dữ liệu; không lấy ca này thay bằng chứng rollback sau khi đã ghi |
| Lỗi tại SQL như nhân viên nghỉ việc hoặc kỳ đã chốt | Caller nhận lỗi, dữ liệu lô và dữ liệu có sẵn giữ đúng trạng thái; không có phần lô bị commit dở |
| Ghi đơn lẻ bằng `insertSingle` | Dòng hợp lệ và `MaChamCong OUTPUT` đúng; lỗi được báo, không trả thành công giả |
| Điều chỉnh và tổng hợp tháng nếu bị tác động | Trạng thái/giờ lưu thật và view tổng hợp khớp; fixture được khôi phục/dọn |

Tái sử dụng [test module TV2](../database/tests_TV2/test_module_chamcong_TV2.sql) và các ca Java/GUI hiện có. Test SQL đơn lẻ không thay test transaction Java. Nếu thay SQL, phối hợp TV5 kiểm cài/rerun phần liên quan và đồng bộ bản trong package sau khi xác minh xong.

Có thể kiểm compile/regression offline bằng runner từ thư mục gốc repo:

```powershell
.\run_verification.ps1 -TestClass SecurityRegressionTest
.\run_verification.ps1 -TestClass GuiQaRegressionTest
```

Kết quả kiểm thử thực tế ghi nhận (15/10/2026):
- Trình biên dịch: `javac 21.0.8 (với cờ --release 11)` biên dịch thành công 55 source files Java (bao gồm test harness có kiểm soát an toàn `src/test/java/com/test/Tv2JdbcIntegrationTest.java`).
- `SecurityRegressionTest`: **104 assertions PASS**, log tại [build/test-results/Java_Verification_20261015_174231_883.log](../build/test-results/Java_Verification_20261015_174231_883.log).
- `GuiQaRegressionTest`: **88 assertions PASS**, log tại [build/test-results/Java_Verification_20261015_174248_559.log](../build/test-results/Java_Verification_20261015_174248_559.log).
- **Kiểm thử live SQL trên Database QA dùng một lần có ownership marker (`PRJ_Fix_QA_20261015_175527_ea6da333`):**
  - Cài đặt đầy đủ 5 module DDL/procs (bao gồm TV2 Option B hash `4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471`), cấp quyền marker `dbo.__QAOwnershipMarker`.
  - Kiểm tra định nghĩa thủ tục: chứa khối `BEGIN TRY ... END TRY` và `BEGIN CATCH THROW; END CATCH`, giữ nguyên 7 tham số và `MaChamCong OUTPUT`, không mở transaction trong thủ tục.
  - Suite SQL TV2 (`database/tests_TV2/test_module_chamcong_TV2.sql`): **21 / 21 PASS (100%)**, log tại [docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/test_module_chamcong_TV2.log](evidence/TV2/optionb-20261015/20261015_175527_ea6da333/test_module_chamcong_TV2.log).
  - Test tương tranh đóng kỳ (`database/tests/Closed_Source_Concurrency.ps1`): **PASS**, log tại [docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Closed_Source_Concurrency.log](evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Closed_Source_Concurrency.log).
- **Kiểm thử live JDBC Transaction Integration Test (`src/test/java/com/test/Tv2JdbcIntegrationTest.java` qua `database/tests_TV2/run_tv2_jdbc_optionb.ps1`):**
  - Các lần thử nghiệm trước đây (job 004 34 assertions, job 005 59 assertions, và đợt trung gian 174112) đã được lưu trữ dưới dạng lịch sử superseded; toàn bộ quy trình kiểm chứng hiện tại sử dụng bộ 87 assertions độc lập, đầy đủ và an toàn.
  - Runner và harness kiểm tra chốt an toàn (negative guards: 8 kiểm tra từ chối null DB, DB production, DB baseline `PRJ_Fix_QA_20261013_001534_ae185d81`, các DB cũ đã drop `163041` và `171103`, sai format tên, thiếu marker, thiếu credential ngoại vi trước khi kết nối), sở hữu bí danh ngẫu nhiên trong `dbo.__QAOwnershipMarker`. Mật khẩu chỉ truyền qua biến môi trường tiến trình trong bộ nhớ (`TV2_QA_PASSWORD`/`DB_PASSWORD`), không lưu vết trong code, file cấu hình hay log.
  - Đường gọi thực qua Service/DAO: `ChamCongService.nhapChamCongTheoLo` -> `ChamCongDAO.insertInTransaction` -> `dbo.sp_GhiNhanChamCong` -> `SQLException` -> Service `rollback()`.
  - Kết quả: **87 / 87 assertions PASS (100%)** qua đủ 6 ca nghiệp vụ + 1 ca chẩn đoán transaction trên cùng connection:
    1. Lô hợp lệ: 3 bản ghi commit thành công, nhận đủ 3 mã `MaChamCong OUTPUT` > 0.
    2. Rollback khi lỗi trùng lặp: Chứng minh rõ ràng 2 bản ghi đầu đã INSERT thành công và sinh `MaChamCong` > 0 trước khi bản ghi thứ 3 lỗi trùng ngày; bắt đúng `SQLServerException` với `SQLServerError` có `Number=50000`, `Severity=16`, `State=1` và thông báo tiếng Việt chuẩn `"Nhân viên đã có bản ghi chấm công trong ngày này!"`; sau rollback toàn bộ lô bị xóa sạch (0 dòng trong DB), dữ liệu có sẵn từ trước được bảo toàn nguyên vẹn; chẩn đoán cùng connection ghi nhận `@@TRANCOUNT = 1`, `XACT_STATE() = 1` trước rollback và cả hai về 0 sau rollback.
    3. Tiền kiểm Java: ngày tương lai bị chặn trước khi mở kết nối SQL, 0 dòng ghi vào DB.
    4. Lỗi SQL từ chối trong lô:
       - Nhân viên nghỉ việc: dòng hợp lệ trước đó ghi thành công và có ID > 0, dòng sau bị SP từ chối với `SQLServerError` (Number=50000, Severity=16, State=1) thông báo `"Không thể ghi nhận chấm công cho nhân viên đã nghỉ việc hoặc không hoạt động!"`, rollback toàn bộ lô về 0 và bảo toàn dữ liệu gốc.
       - Kỳ đã chốt: dòng hợp lệ trước đó ghi thành công và có ID > 0, dòng sau bị trigger từ chối với `SQLServerError` (Number=50000, Severity=16, State=1) thông báo `"Kỳ lương đã chốt: không được thay đổi chấm công."`, rollback toàn bộ lô về 0 và bảo toàn dữ liệu gốc.
    5. Ghi đơn lẻ `insertSingle`: thành công nhận `MaChamCong OUTPUT` hợp lệ; trùng lặp ném `SQLException` với `SQLServerError` (Number=50000, Severity=16, State=1), không trả kết quả thành công giả.
    6. Điều chỉnh & tổng hợp: cập nhật công thành công, view tổng hợp tháng tính đúng số ngày và giờ công; chuyển công vào kỳ đã chốt bị trigger từ chối với `SQLServerError` (Number=50000, Severity=16, State=1).
  - Log chi tiết chuẩn UTF-8 nguyên vẹn: [docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Tv2JdbcIntegrationTest.log](evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Tv2JdbcIntegrationTest.log).
  - Dọn dẹp sạch sẽ: Database QA dùng một lần đã được xác minh marker và DROP hoàn toàn, kiểm tra xác nhận biến mất sau test; không ảnh hưởng DB khác.
- *Hiện trạng:* Đã hoàn tất toàn bộ kiểm thử nội bộ TV2 Option B đạt 100% PASS, hiện đang ở bước **Awaiting Independent Review** (chờ reviewer độc lập nghiệm thu trước khi Git delivery; chưa tuyên bố PR được merge hay giảng viên đã nghiệm thu).

## 4. Chuẩn bị bàn giao cuối và rehearsal

- [ ] Gửi TV5 xác nhận kỹ thuật theo mẫu dưới đây và đường dẫn các tài liệu đã cập nhật (Chưa gửi: chờ hoàn tất reviewer độc lập duyệt diff và TV5 tiếp nhận).
- [ ] Tập nói slide 4–6, phân biệt vai trò constraint/trigger, transaction nhập lô và phép đo index; giải thích `fn_TongPhuCap` thuộc TV2 nhưng cài tại module 03.
- [ ] Theo [kịch bản demo chung](../Bo_nop_Nhom06/TV5_TAP_DEMO.md): dùng kỳ 10/2026 và công đã chuẩn bị; bài lỗi trùng ngày làm riêng, không thêm/xóa nguồn để ép số lương.
- [ ] Cùng nhóm tập luồng thực và ghi phiếu kết quả. Cuối lượt xác nhận A 730.000đ/B 0đ, `CHUA_CHOT`, `NgayChot IS NULL`, nguồn không đổi. TV5 điều phối và kiểm phần cuối; TV2 chịu trách nhiệm đoạn chấm công của mình.

Mẫu xác nhận kỹ thuật để TV2 điền sau khi đối chiếu:

```text
TV2 xác nhận ngày: 15/10/2026
Source commit đã đọc/đã sửa: baseline main @ e00da71 / feature diff trên branch fix/tv2-sp-try-catch
Phương án A hoặc B: Option B (minimal SQL TRY...CATCH correction)
Cách hiểu yêu cầu TRY...CATCH và người xác nhận: Quyết định nội bộ nhóm duyệt Option B (không tuyên bố GVHD phê duyệt); bao bọc validation + INSERT + SCOPE_IDENTITY trong TRY/CATCH với bare THROW; bảo toàn 7 tham số, thông báo lỗi RAISERROR 16; không mở transaction trong SP để Java Service độc quyền quản trị atomicity.
Nơi sở hữu transaction của lô: ChamCongService.nhapChamCongTheoLo (JDBC Connection: setAutoCommit(false), commit(), rollback())
Cách SP chuyển lỗi về Java: RAISERROR 16/ràng buộc -> CATCH -> bare THROW -> SQLException trong CallableStatement -> ChamCongDAO -> ChamCongService rollback() -> đóng gói thông báo ném ra UI.
Đường ghi đơn lẻ và OUTPUT: ChamCongDAO.insertSingle gọi sp_GhiNhanChamCong, nhận @MaChamCong INT OUTPUT = SCOPE_IDENTITY().
Code có đổi không; PR nếu có: Có sửa database/02_Module_ChamCong_TV2.sql (10,064 bytes, SHA-256 4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471); đồng bộ package Bo_nop_Nhom06/database/02_Module_ChamCong_TV2.sql và Bo_nop_Nhom06/SHA256_MANIFEST.csv (537 files verified). Git delivery HOLD chờ reviewer độc lập.
Log kiểm thử thực tế:
  - Biên dịch: javac 21.0.8 (--release 11) biên dịch thành công 55 sources Java.
  - Offline Java: build/test-results/Java_Verification_20261015_174231_883.log (Security: 104 assertions PASS); build/test-results/Java_Verification_20261015_174248_559.log (GuiQa: 88 assertions PASS).
  - Live SQL QA (database PRJ_Fix_QA_20261015_175527_ea6da333): suite test_module_chamcong_TV2.sql đạt 21/21 PASS (log docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/test_module_chamcong_TV2.log); Closed_Source_Concurrency.log PASS.
  - Live JDBC Integration (Tv2JdbcIntegrationTest): 87/87 assertions PASS qua 8 negative guards + 6 kịch bản + chẩn đoán transaction, chứng minh 2 INSERTs có ID > 0 trước rollback, bắt đúng causal SQLException với typed SQLServerError tiếng Việt chuẩn, kiểm tra @@TRANCOUNT/XACT_STATE (log docs/evidence/TV2/optionb-20261015/20261015_175527_ea6da333/Tv2JdbcIntegrationTest.log).
  - Dọn dẹp: QA database dùng một lần có ownership marker đã được DROP hoàn toàn và xác nhận không còn tồn tại sau kiểm thử.
Tài liệu đã cập nhật: docs/TV2_TASKLIST_HOAN_THIEN_20261015.md, docs/TV2_Tien_Do_Thuc_Hien.md, docs/TV2_BaoCao_ChuyenDe_ChamCong_CuoiKy.md, docs/TV2_Huong_Dan_Thuyet_Trinh.md.
TV5 xác nhận tiếp nhận: Chưa gửi / Chưa tiếp nhận (chờ reviewer độc lập duyệt diff và TV5 tiếp nhận).
Phần chưa kiểm hoặc còn chờ: Rehearsal chung nhóm; TV5 cập nhật Word (đoạn 193/824) và Slide 5 notes theo ghi chú tích hợp.
```

## 5. Ghi chú chuyển giao tích hợp dành cho TV5 (Pending TV5 Integration Note)

Để bảo đảm tính toàn vẹn của package nộp và tuân thủ quy trình bàn giao của nhóm (việc kiểm thử live SQL/JDBC đã hoàn tất 100% trong môi trường QA nội bộ, đang ở trạng thái **Awaiting Independent Review** trước khi chuyển giao Git), TV2 lập ghi chú chi tiết dưới đây để TV5 chủ động đồng bộ tài liệu Office (Word/Slide) theo cấu trúc đã kiểm chứng:

### 5.1 Cập nhật Báo cáo Word nhóm (`Bo_nop_Nhom06/report/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx`)
Trong file Word nhóm (`word/document.xml`), hiện còn tồn tại 2 đoạn văn bản ghi nhận cấu trúc cũ của thủ tục chấm công cần TV5 biên tập:
1. **Đoạn 193:**
   - *Văn bản hiện tại:* `"SP ghi chấm công không có TRY…CATCH riêng; transaction nhập lô nằm ở JDBC caller. Báo cáo ghi đúng sự khác biệt này khi đối chiếu yêu cầu SP có xử lý lỗi. Các SP có transaction ở SQL được trình bày theo phạm vi commit, rollback và savepoint thực tế, không áp một khuôn chung cho mọi SP."*
   - *Đề xuất TV5 thay bằng:* `"Thủ tục sp_GhiNhanChamCong bao bọc toàn bộ validation, INSERT và gán SCOPE_IDENTITY trong khối TRY...CATCH với bare THROW để chuyển lỗi nguyên trạng về caller; thủ tục không mở transaction nội bộ để Java Service (ChamCongService.nhapChamCongTheoLo) quản lý atomicity của giao dịch lô. Lượt QA riêng ngày 15/10/2026 đã kiểm SQL 21/21 và JDBC 87 assertions, gồm hai INSERT thành công rồi rollback khi dòng thứ ba lỗi trùng ngày; không thay thế nghiệm thu GUI và rehearsal của nhóm."*
2. **Đoạn 824:**
   - *Văn bản hiện tại:* `"SP này không có BEGIN TRY…CATCH hoặc transaction riêng. Khi nhập lô, ChamCongService mở transaction JDBC, truyền cùng Connection cho DAO và xử lý commit/rollback. Lỗi SQL từ SP hoặc trigger được caller nhận. Vì vậy không mô tả năm SP lõi như cùng một cấu trúc transaction SQL."*
   - *Đề xuất TV5 thay bằng:* `"SP này đã được hoàn thiện với cấu trúc BEGIN TRY...CATCH kết hợp bare THROW bao bọc validation và INSERT (không mở transaction riêng). Khi nhập lô, ChamCongService mở transaction JDBC, truyền cùng Connection cho DAO và xử lý commit/rollback. Lỗi SQL từ SP hoặc trigger được re-throw nguyên trạng để caller nhận và rollback."*

### 5.2 Cập nhật Ghi chú Slide Deck nhóm (`Bo_nop_Nhom06/slides/Nhom06_ThuyetTrinh_15_Slides.pptx`)
Trong deck chung (`ppt/notesSlides/notesSlide5.xml`), phần text hiển thị của Slide 5 hoàn toàn tương thích, nhưng phần speaker notes cần TV5 hiệu chỉnh:
- *Ghi chú hiện tại:* `"sp_GhiNhanChamCong thực hiện kiểm tra/ghi từng dòng. Java DAO mở một transaction trên cùng connection cho toàn batch, lỗi một dòng rollback batch. Không tuyên bố SP có TRY/CATCH riêng nếu source không có. SQL log TV2 21 PASS/0 FAIL là kiểm SQL, không tự chứng minh GUI batch. fn_TongPhuCap thuộc TV2 theo phân công, nằm module03; payroll đọc function này. Dynamic SQL đọc các function nằm caller TV4."*
- *Đề xuất TV5 sửa lại:*
  1. Đổi `"Java DAO mở một transaction..."` thành `"ChamCongService mở một transaction..."` (chính xác hóa vai trò: Service là nơi mở và quản trị transaction JDBC, không phải DAO).
  2. Đổi `"Không tuyên bố SP có TRY/CATCH riêng nếu source không có."` thành: `"Thủ tục sp_GhiNhanChamCong đã có TRY...CATCH + bare THROW theo Option B, không mở transaction riêng để Service quản lý batch; SQL/JDBC đã được kiểm trên QA riêng ngày 15/10/2026, còn nghiệm thu GUI và rehearsal do nhóm thực hiện."*

### 5.3 Audit Slide 4 và Slide 6
- **Slide 4 (Nghiệp vụ & Ràng buộc):** Đã đối chiếu chỉ đọc với mã nguồn SQL gốc (`database/02_Module_ChamCong_TV2.sql`) và phân công. SQL/JDBC có bằng chứng QA riêng ở mục 3; lượt audit slide chỉ đọc nội dung, không phải nghiệm thu trình chiếu hoặc GUI trực tiếp.
- **Slide 6 (View, Covering Index, fn_TongPhuCap):** Đã kiểm tra đối chiếu (read-only audit), nội dung khớp chính xác với số liệu benchmark canonical v3 ngày 13/10/2026 (Messages 574→5 reads / 99.1289%, XML 522→5 reads / 99.0421%, unexplained 52 reads) và phân định quyền sở hữu `fn_TongPhuCap` (module 03).

### 5.4 Đồng bộ Bản sao SQL trong Package và Cập nhật Manifest SHA-256
- **Trạng thái thực hiện (ĐÃ HOÀN TẤT):**
  - Bản canonical source `database/02_Module_ChamCong_TV2.sql`: 10,064 bytes, SHA-256 `4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471`.
  - Bản sao trong package `Bo_nop_Nhom06/database/02_Module_ChamCong_TV2.sql`: Đã đồng bộ chính xác với bản canonical source (10,064 bytes, SHA-256 `4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471`).
  - Dòng 3 trong `Bo_nop_Nhom06/SHA256_MANIFEST.csv` đã được cập nhật:
    `database/02_Module_ChamCong_TV2.sql,10064,4dbb78fc0b3946d4048fb779eb5111f10748bb81d7f608b6ddf1f69acacfd471`
  - Đã chạy kiểm tra toàn diện: **Tất cả 537/537 dòng manifest đều khớp chính xác (100% verified match)**.
  - Các bản sao SQL lịch sử/evidence khác trong `Bo_nop_Nhom06/evidence/` được đóng băng nguyên vẹn, không chỉnh sửa.
- **Phần còn lại TV5 thực hiện sau khi cập nhật Word/Slide:**
  - Sau khi TV5 hoàn tất chỉnh sửa Word (`Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx` đoạn 193/824) và Slide (`Nhom06_ThuyetTrinh_15_Slides.pptx` notesSlide5), tính toán lại kích thước và SHA-256 để cập nhật dòng 448 và dòng 451 trong manifest.
  - Chạy lại script xác thực toàn diện để bảo đảm 100% mục trong `Bo_nop_Nhom06/SHA256_MANIFEST.csv` khớp chính xác.

## Tiêu chí đóng task TV2

TV5 chỉ đóng phần còn mở khi đã nhận xác nhận thiết kế/rubric, tài liệu hiện tại nhất quán và bằng chứng cho mọi code sửa thêm nếu có. Rehearsal là đầu việc phối hợp riêng, chỉ đánh dấu sau khi tập thật. Thử máy nhận, DPI Windows, ghép package và PDF cuối do TV5 điều phối, không chuyển thành yêu cầu TV2 làm lại benchmark.

## Cập nhật trạng thái sau PR #52 — 16/10/2026

PR #52 đã merge; TV5 đã tiếp nhận evidence cuối vào package, cập nhật hai đoạn Word (đoạn 193/824) và notes slide 5. Bản Word trong `docs/` và package hiện byte-identical. Kết quả cuối là controlled suite 179/179, SQL 21/21, concurrency 4/4, JDBC 105 assertions; evidence tại `Bo_nop_Nhom06/evidence/TV2/optionb-20261016/`. Do đó checkbox nội dung Word/slide đã hoàn tất ở phía TV5. Phần TV2 còn lại là tập nói slide 4–6 và rehearsal chung, chỉ đánh dấu sau khi thực hiện thật.
