# TV2 hướng dẫn hoàn thành các việc còn lại

**Người phụ trách:** Phạm Minh Quân — TV2 — MSSV 24110311.

**Phối hợp:** Trần Đức Anh — TV5.

**Ngày lập:** 15/10/2026. Đối chiếu snapshot `4e12940` trên nhánh TV5 (PR #49); bằng chứng TV2 đã nhận từ PR #44 và hiệu đính sử dụng tại PR #46/#47.

TV2 đã bàn giao source, benchmark hiện hành, bốn actual plans và slide 4–6. Việc còn mở chính là **chốt cách đáp ứng yêu cầu TRY…CATCH trong kế hoạch với cơ chế transaction hiện tại**, rà lại tài liệu cá nhân và tham gia rehearsal. Không yêu cầu đo lại benchmark hoặc tạo lại toàn bộ minh chứng đã hợp lệ.

## 1. Xác nhận cách xử lý lỗi và transaction

### Căn cứ cần đọc

| File | Nội dung cần đối chiếu |
|---|---|
| [Kế hoạch phân công](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md) | Bảng rubric ghi Stored Procedure ≥5 + TRY…CATCH; TV2 được giao transaction nhập công theo lô |
| [Module SQL TV2](../database/02_Module_ChamCong_TV2.sql) | `sp_GhiNhanChamCong` kiểm đầu vào, báo lỗi bằng RAISERROR/RETURN, ghi một dòng và trả `MaChamCong OUTPUT`; chưa có TRY…CATCH, không tự mở/commit transaction |
| [ChamCongService](../src/main/java/com/service/ChamCongService.java) | `nhapChamCongTheoLo` validate cả lô, dùng một connection, tắt auto-commit, commit khi mọi dòng thành công và rollback khi lỗi |
| [ChamCongDAO](../src/main/java/com/dao/ChamCongDAO.java) | `insertInTransaction` dùng connection do service truyền vào; `insertSingle` là đường ghi đơn lẻ cần giữ hoạt động |
| [Log GUI TV2](../Bo_nop_Nhom06/evidence/TV5/gui-followup-20261013/tv2-attendance.log) và [giải thích QA](../Bo_nop_Nhom06/evidence/TV5/gui-followup-20261013/README.md) | Đã kiểm hai INSERT thực tới SQL trước khi dòng cuối lỗi trùng, sau đó Java rollback; lô hợp lệ commit ba dòng; tổng hợp tháng khớp điều chỉnh |

**Việc TV2 phải làm:**

- [ ] Đọc các đường gọi trên và xác nhận bằng văn bản: service sở hữu transaction của cả lô, SP xử lý một dòng và chuyển lỗi về caller.
- [ ] Cùng TV5 làm rõ rubric đòi TRY…CATCH ở từng SP được phân công hay cho phép giải thích cơ chế hiện tại. Ghi căn cứ/người xác nhận; không tự coi việc giải thích là đã đáp ứng cách chấm khi chưa được thống nhất.
- [ ] Chốt một trong hai phương án dưới đây và ghi vào tài liệu tiến độ.

| Phương án | Khi áp dụng | Đầu ra cần bàn giao |
|---|---|---|
| **A. Giữ cơ chế hiện tại** | Nhóm/người duyệt chấp nhận transaction ở Java và cách SP phát lỗi hiện hành | Thuyết minh ngắn về quyền sở hữu transaction, đường truyền lỗi, bằng chứng rollback có sẵn và xác nhận của TV2 + TV5; không cần PR sửa code |
| **B. Sửa SQL để đáp ứng yêu cầu đã thống nhất** | Xác nhận rằng SP TV2 bắt buộc có TRY…CATCH hoặc tìm được lỗi xử lý cụ thể | Mô tả rõ cách xử lý lỗi cần bổ sung, sửa tối thiểu và kiểm lại các ca bị ảnh hưởng; PR ghi cả yêu cầu rubric lẫn hành vi thực tế |

Ưu tiên xác nhận thiết kế trước khi sửa. Không thêm catch chỉ để đếm cú pháp rồi báo đã khắc phục lỗi runtime. Nếu chọn B, không commit transaction của caller, không nuốt lỗi để Java tiếp tục commit và không thêm ROLLBACK vô điều kiện lên transaction do nơi khác quản lý. Giữ hợp đồng OUTPUT, validation, quyền SQL và đường ghi đơn lẻ.

**Hoàn tất mục này khi:** có phương án được TV2/TV5 xác nhận; nếu có sửa code thì kiểm chứng tương ứng đạt. Không đánh dấu hoàn tất chỉ vì đã có một đoạn TRY…CATCH.

## 2. Rà và cập nhật tài liệu cá nhân

- [ ] Rà [tiến độ TV2](TV2_Tien_Do_Thuc_Hien.md), [báo cáo chuyên đề](TV2_BaoCao_ChuyenDe_ChamCong_CuoiKy.md) và [hướng dẫn thuyết trình](TV2_Huong_Dan_Thuyet_Trinh.md). Thêm cập nhật hiện tại tại những chỗ còn ghi benchmark mới chưa được nhận; giữ ngày và nhãn của phần lịch sử.
- [ ] Ghi phương án transaction đã thống nhất ở mục 1, phân biệt kiểm SQL với đường nhập lô Java/GUI.
- [ ] Dùng [biên nhận TV2](../Bo_nop_Nhom06/evidence/TV2/README_TV5_RECEIPT.md) và [bảng hiệu đính v3](../Bo_nop_Nhom06/evidence/TV2/presentation/reviewed_v2/TV5_Proposed_Corrections_v3.md) làm nguồn khi xác nhận Word/slide cuối.
- [ ] Xác nhận slide 4–6 và phần chấm công trong Word nhóm đúng số liệu, ownership và giới hạn; báo TV5 nếu cần chỉnh bản tổng hợp.

Số liệu cần giữ đúng nguồn: fixture 30.000 dòng, 273 dòng chi tiết và 9 dòng tổng hợp; **Messages 574→5 reads**, **XML operator 522→5 reads** là hai nguồn đo khác nhau. Không dùng việc ảnh cũ cũng có số 574→5 để coi ảnh đó là lượt đo 13/10. Không lấy ngày checkout hoặc ngày sửa tài liệu làm ngày chạy.

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

Hai lệnh này không kết nối SQL và không chứng minh SP hoặc rollback tại SQL đã đạt. Bài SQL/GUI phải chạy bằng môi trường QA và danh tính phù hợp; không thử lỗi trên dữ liệu demo dùng chung.

## 4. Chuẩn bị bàn giao cuối và rehearsal

- [ ] Gửi TV5 xác nhận kỹ thuật theo mẫu dưới đây và đường dẫn các tài liệu đã cập nhật. Nếu có code mới, gửi PR cùng log đúng phần đã kiểm.
- [ ] Tập nói slide 4–6, phân biệt vai trò constraint/trigger, transaction nhập lô và phép đo index; giải thích `fn_TongPhuCap` thuộc TV2 nhưng cài tại module 03.
- [ ] Theo [kịch bản demo chung](../Bo_nop_Nhom06/TV5_TAP_DEMO.md): dùng kỳ 10/2026 và công đã chuẩn bị; bài lỗi trùng ngày làm riêng, không thêm/xóa nguồn để ép số lương.
- [ ] Cùng nhóm tập luồng thực và ghi phiếu kết quả. Cuối lượt xác nhận A 730.000đ/B 0đ, `CHUA_CHOT`, `NgayChot IS NULL`, nguồn không đổi. TV5 điều phối và kiểm phần cuối; TV2 chịu trách nhiệm đoạn chấm công của mình.

Mẫu xác nhận kỹ thuật để TV2 điền sau khi đối chiếu:

```text
TV2 xác nhận ngày: ...
Source commit đã đọc/đã sửa: ...
Phương án A hoặc B: ...
Cách hiểu yêu cầu TRY...CATCH và người xác nhận: ...
Nơi sở hữu transaction của lô: ...
Cách SP chuyển lỗi về Java: ...
Đường ghi đơn lẻ và OUTPUT: ...
Code có đổi không; PR nếu có: ...
Log hiện có được dùng lại / log mới và phạm vi: ...
Tài liệu đã cập nhật: ...
TV5 xác nhận tiếp nhận: ...
Phần chưa kiểm hoặc còn chờ: ...
```

## Tiêu chí đóng task TV2

TV5 chỉ đóng phần còn mở khi đã nhận xác nhận thiết kế/rubric, tài liệu hiện tại nhất quán và bằng chứng cho mọi code sửa thêm nếu có. Rehearsal là đầu việc phối hợp riêng, chỉ đánh dấu sau khi tập thật. Thử máy nhận, DPI Windows, ghép package và PDF cuối do TV5 điều phối, không chuyển thành yêu cầu TV2 làm lại benchmark.
