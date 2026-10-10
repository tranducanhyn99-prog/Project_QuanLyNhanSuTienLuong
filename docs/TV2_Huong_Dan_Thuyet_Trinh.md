# TV2 — Hướng dẫn thuyết trình phân hệ chấm công

**Người trình bày:** Phạm Minh Quân — MSSV 24110311

**Cập nhật:** 10/10/2026
**Phạm vi:** Slide 4–6; lời nói mẫu khoảng 02:00 phút; phần demo TV2 khoảng 00:45 phút nếu nhóm chọn cách phân bổ này.

Đã đối chiếu phân công và SQL/Java trên `main` commit `0223f2f`, sau [PR #38](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/38). Combo hiển thị tiếng Việt, còn mã nghiệp vụ trong model/SQL giữ nguyên: **Có mặt** = `CO_MAT`, **Đi trễ** = `DI_TRE`, **Về sớm** = `VE_SOM`, **Vắng mặt** = `VANG`.

Demo nối tiếp thống nhất dùng **10/2026**, theo [báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md): A có lương 26.000.000đ, 26 công chuẩn, một công ngày 09/10/2026 (08:00–17:00), phụ cấp 730.000đ và khấu trừ 1.000.000đ; thực nhận **730.000đ**, B **0đ**. Đã có 20 ca thao tác và ba ca kiểm tra lại sau sửa. Trước demo, kiểm tra fixture hiện có theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md); dùng lại dòng công/khoản, không thêm trùng hoặc xóa nguồn để ép số. Kết thúc bằng mở lại kỳ, xác nhận `CHUA_CHOT` và `NgayChot IS NULL`.

## 1. Vai trò trong phần trình bày nhóm

Theo giới hạn tối đa 15 slide trong kế hoạch, tài liệu này đề xuất TV1→TV5 nói liền mạch trong 10 phút, rồi demo nối tiếp 5 phút (tổng 15 phút trình bày), sau đó vấn đáp 5–10 phút. Đây là cách tổ chức buổi bảo vệ, không phải rubric bắt buộc mỗi người nói đúng 2 phút. TV2 giữ slide 4–6 theo phương án hiện tại. Khi kết thúc slide 6, khoảng phút 04:00, chuyển lời cho TV3 tiếp tục phần nói; demo chỉ bắt đầu sau khi cả nhóm nói xong 10 phút. Trong demo chung, nối mạch HR kiểm tra hồ sơ → TV2 xem công có sẵn → TV3 xem khoản phụ cấp/khấu trừ → TV4 tính lương → TV5 chốt kỳ. Mốc 45 giây cho TV2 là thời lượng rehearsal đề xuất; sau đoạn này trong demo mới bàn giao riêng cho TV3.

Phần này trình bày đúng ownership trong [kế hoạch phân công](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md): `sp_GhiNhanChamCong`; phối hợp TV3 với `fn_TongPhuCap` (cài trong module 03); hai trigger kiểm tra giờ và trạng thái nhân viên; `vw_TongHopChamCongThang`; `IX_CHAMCONG_MaNV_Ngay`; transaction nhập chấm công theo lô. Không nhận `fn_TinhSoNgayCong` là phần TV2: hàm nằm trong module 02 nhưng được giao TV1; thủ tục payroll hiện đếm trực tiếp CHAMCONG theo khoảng ngày.

Thứ tự sân khấu: TV1 chốt hồ sơ nhân viên và điều kiện dữ liệu; TV2 minh họa công đã ghi; TV3 nối khoản phát sinh đã có; TV4 tính lương; TV5 kết thúc bằng chốt kỳ và quyền/concurrency. Mỗi người giới thiệu ngắn module mình, tránh đọc lại kiến trúc đã có ở slide đầu.

Khi bị giới hạn thời gian, ưu tiên nêu quy tắc duy nhất nhân viên–ngày, transaction rollback và đầu ra view. Không dành thời gian mở nhiều file liên tiếp.

## 2. Rubric và bằng chứng cần chỉ ra

Các mục dưới đây là ngưỡng dự án ghi trong kế hoạch/rubric, không phải điểm số hay khẳng định nghiệm thu thay giảng viên.

| Hạng mục rubric | Đối tượng / phần TV2 | Bằng chứng để mở | Điều cần nói |
|---|---|---|---|
| Constraint có ý nghĩa (dự án ≥5) | `UQ_CHAMCONG_MaNV_Ngay`, `CHK_CHAMCONG_Ngay`, FK và check trạng thái | `database/02_Module_ChamCong_TV2.sql`; có thể đọc metadata | Một nhân viên chỉ có một dòng mỗi ngày; ngày tương lai bị chặn. |
| Stored Procedure (dự án ≥5) | `dbo.sp_GhiNhanChamCong` | Cùng SQL, phần procedure; `ChamCongDAO.insertSingle` | SP kiểm tra đầu vào, ngày, trạng thái, nhân viên đang làm và trùng ngày trước khi ghi. |
| Trigger (dự án ≥5) | `trg_ChamCong_KiemTraGio`, `trg_ChamCong_KiemTraNhanVien` | Cùng SQL, hai trigger; module 05 cho trigger kỳ chốt | Trigger kiểm soát cả thao tác SQL trực tiếp; module 05 khóa cả insert/update/delete nếu kỳ đã chốt. |
| View (dự án ≥5) | `vw_TongHopChamCongThang` | Cùng SQL; `ChamCongDAO.getTongHopTheoThang` | Tổng ngày làm, đi trễ, về sớm, vắng và giờ làm theo nhân viên/kỳ. |
| Index (dự án ≥5 + minh chứng hiệu năng) | `IX_CHAMCONG_MaNV_Ngay`, INCLUDE giờ vào/ra và trạng thái | Cùng SQL; thiết kế benchmark `database/test_benchmark_index_TV2.sql`; trạng thái thực tế tại [FIX_TASKLIST](FIX_TASKLIST.md) | Index phục vụ lọc theo nhân viên/ngày; khi trình bày số đo cần có log IO/TIME và execution plan cùng lần chạy. |
| Transaction (dự án ≥5 nghiệp vụ) | Nhập lô ở `ChamCongService.nhapChamCongTheoLo` | `src/main/java/com/service/ChamCongService.java`; `ChamCongDAO.insertInTransaction` | Một dòng lỗi thì rollback toàn bộ lô; DAO nhận cùng connection của transaction. |
| Ứng dụng và xử lý lỗi | `ChamCongPanel` → Service → DAO/JDBC → SQL Server | `src/main/java/com/ui/chamcong/ChamCongPanel.java`, `src/main/java/com/dao/ChamCongDAO.java` | UI lấy tổng hợp qua view, ghi đơn qua callable SP; lỗi được báo cho người dùng. |

Khi bị rút ngắn thời gian, dùng ba ý: (1) một nhân viên–một ngày và kiểm tra nhân viên còn làm; (2) ghi lô dùng một transaction; (3) view tổng hợp phục vụ đối soát, còn TV4 đếm trực tiếp CHAMCONG để tính ngày công. Sau đó chuyển phần nói cho TV3 theo điều phối.

Rubric toàn dự án còn gồm ≥8 bảng/3NF, concurrency hoặc recovery, ≥4 role/login và các nội dung phụ trách của TV khác. Chỉ nhận xét phần chấm công liên kết vào luồng tổng thể, không trình bày như ownership TV2.

## 3. Slide 4–6 và lời nói mẫu (02:00)

### Slide 4 — Nghiệp vụ và ràng buộc (khoảng 40 giây)

**Trên slide:** Một bản ghi cho cặp nhân viên–ngày; giờ ra lớn hơn giờ vào; chỉ nhân viên đang làm; ngày không ở tương lai. Luồng: giao diện → service → DAO → SQL Server.

**Lời nói:** “Em là Phạm Minh Quân, phụ trách phân hệ chấm công. Chấm công là dữ liệu đầu vào cho tính lương nên nhóm kiểm soát ngay khi ghi. Mỗi nhân viên chỉ có một bản ghi trong một ngày; giờ ra nếu có phải sau giờ vào; ngày không được vượt ngày hiện tại và không ghi cho nhân viên đã nghỉ. Quy tắc nằm cả ở thủ tục, constraint và trigger để dữ liệu vẫn được bảo vệ khi có đường ghi trực tiếp xuống SQL Server.”

### Slide 5 — SQL và toàn vẹn dữ liệu (khoảng 40 giây)

**Trên slide:** `sp_GhiNhanChamCong`; trigger kiểm tra giờ/nhân viên; transaction lô all-or-nothing; trigger module 05 khóa kỳ đã chốt.

**Lời nói:** “Khi ghi một dòng, DAO gọi `sp_GhiNhanChamCong`. Với điểm danh theo lô, service mở một connection, tắt auto-commit, gọi DAO cho từng dòng rồi commit; nếu có lỗi thì rollback cả lô. Hai trigger kiểm tra giờ và trạng thái nhân viên cũng áp dụng với cập nhật. Khi kỳ lương đã chốt, trigger ở module 05 kiểm tra kỳ cũ và kỳ đích, nên thêm, sửa, xóa hoặc chuyển ngày sang kỳ đã chốt đều bị chặn ở cơ sở dữ liệu.”

### Slide 6 — Tổng hợp và nối sang lương (khoảng 40 giây, kết thúc gần phút 04:00)

**Trên slide:** `vw_TongHopChamCongThang`; `IX_CHAMCONG_MaNV_Ngay`; `fn_TongPhuCap` phối hợp TV2–TV3. Payroll đếm trực tiếp CHAMCONG để tính ngày công.

**Lời nói:** “Tab Tổng hợp theo tháng đọc `vw_TongHopChamCongThang`, cho biết ngày làm, ngày vắng, đi trễ, về sớm và giờ làm. Để tổng phụ cấp theo nhân viên và kỳ, `fn_TongPhuCap` cộng `SoTien` trong tháng, năm được truyền vào và trả 0 nếu chưa có khoản; em phối hợp TV3, và thủ tục tính lương gọi function này khi có trong schema. Riêng số ngày công, TV4 đếm trực tiếp CHAMCONG trong transaction, không gọi `fn_TinhSoNgayCong` của TV1. Em xin chuyển phần nói tiếp theo cho TV3.”

**Câu bàn giao ở demo:** Sau đoạn demo TV2 khoảng 45 giây, nói: “Phần công có sẵn đã được đối chiếu và tổng hợp; em xin chuyển màn hình cho bạn Trần Tiến Đạt trình bày phụ cấp và khấu trừ.” Đây là bàn giao trong demo sau 10 phút nói, tách biệt với câu chuyển cho TV3 ở cuối slide 6.

## 4. Demo GUI 45 giây trong mạch chung

Dùng QA do `start_gui_qa.ps1` chọn, phiên `gui_hr` do TV1 bàn giao. Fixture A/B đã có sẵn; kiểm tra theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md), không thêm lại hồ sơ, công hoặc khoản.

| Thời gian | Thao tác | Kết quả đối chiếu |
|---|---|---|
| 0–8 giây | Mở **Nhật ký chấm công**, lọc tháng **10**, năm **2026**, chọn A. | Đúng kỳ và nhân viên. |
| 8–23 giây | Chỉ dòng công có sẵn ngày `2026-10-09`, 08:00–17:00, **Có mặt** (`CO_MAT`). | Một dòng công của A; không bấm ghi mới. |
| 23–35 giây | Mở **Tổng hợp theo tháng**, chọn 10/2026, bấm **Xem tổng hợp**. | A có 1 ngày làm, 9 giờ, trễ/sớm/vắng đều 0. B không có công. |
| 35–45 giây | Nêu transaction nhập lô và bàn giao TV3. | Một dòng lỗi sẽ rollback toàn lô; dùng log đã PASS để giải thích. |

**Bài lỗi trùng ngày là phần riêng:** nếu được hỏi, thử ghi lại đúng A–09/10/2026 khi kỳ đang `CHUA_CHOT`; phải bị từ chối và dòng gốc giữ nguyên. Đây không phải bước ghi công mới thành công. Không đổi sang ngày khác để lách lỗi. Nhập lô GUI chưa có minh chứng trong đợt 09/10, nên không nhận là đã demo tay.

TV3 tiếp tục đọc phụ cấp 730.000đ/khấu trừ 1.000.000đ của A; TV4 tính kỳ 10/2026 với 26 công chuẩn; TV5 chốt, xem phiếu và mở lại để kết thúc `CHUA_CHOT`.

### SQL chỉ đọc khi GUI không thuận tiện

SSMS phải ở đúng QA. Tra A bằng email, không dùng ID đoán:

```sql
DECLARE @MaNV INT = (SELECT MaNV FROM dbo.NHANVIEN WHERE Email = 'gui-a@example.invalid');
IF @MaNV IS NULL THROW 51000, N'Không tìm thấy fixture A.', 1;
SELECT MaNV, NgayChamCong, GioVao, GioRa, TrangThai
FROM dbo.CHAMCONG
WHERE MaNV = @MaNV
  AND NgayChamCong >= '20261001' AND NgayChamCong < '20261101'
ORDER BY NgayChamCong;
SELECT MaNV, Thang, Nam, SoNgayDiLam, SoLanDiTre, SoLanVeSom, SoNgayVang, TongSoGioLam
FROM dbo.vw_TongHopChamCongThang
WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026;
```

Nếu dòng công/tổng hợp khác minh chứng, xác minh fixture trước khi tiếp tục; không ghi/xóa nguồn để ép kết quả. Không chạy seed, installer hoặc benchmark trong demo. Mật khẩu chỉ tra [demo_accounts.json](../database/demo_accounts.json) lúc đăng nhập; không đưa vào ảnh/slide.

## 5. Minh chứng dự phòng cho vấn đáp

Mở đúng file khi được hỏi; không cần trình diễn lại toàn bộ.

| Chủ đề | Mở ở đâu | Điểm trả lời |
|---|---|---|
| Procedure/trigger | `database/02_Module_ChamCong_TV2.sql` | Unique `(MaNV, NgayChamCong)` ngăn trùng; SP kiểm tra nghiệp vụ; trigger bảo vệ INSERT/UPDATE trực tiếp. Trigger kỳ chốt nằm trong module 05. |
| Function | `database/03_phucap_khautru_TV3.sql`, `fn_TongPhuCap` | Đây là function TV2 phối hợp TV3, thuộc module 03. TV1 sở hữu `fn_TinhSoNgayCong` dù được cài trong module 02. Payroll hiện đếm trực tiếp CHAMCONG, không gọi hàm đó. |
| View | SQL module 02 và `ChamCongDAO.getTongHopTheoThang` | View nhóm theo nhân viên/kỳ và phân loại trạng thái; DAO đọc view bằng PreparedStatement. |
| Index | SQL module 02; `database/test_benchmark_index_TV2.sql`; [FIX_TASKLIST](FIX_TASKLIST.md) | Key `(MaNV, NgayChamCong)`, INCLUDE giờ vào/ra/trạng thái. Báo cáo cũ ghi 574→5 logical reads trên fixture tạm; đây là benchmark lịch sử, không phải số đo hiện tại. |
| Transaction rollback | `ChamCongService.nhapChamCongTheoLo` | Validate trước; cùng JDBC connection cho cả lô; commit khi tất cả hợp lệ, rollback khi có lỗi. Không tự tạo lỗi trên database dùng chung để biểu diễn. |
| Kỳ đã chốt / đồng thời | `database/05_Security_Payroll_TV5.sql` và `database/tests/Closed_Source_Concurrency.ps1` | Trigger kiểm tra khóa cha `BANGLUONG` với `UPDLOCK, HOLDLOCK`; cả kỳ nguồn và đích của thao tác đổi ngày đều được bảo vệ. Trạng thái test hiện hành đối chiếu FIX_TASKLIST. |
| Payroll lấy công | `database/04_Module_TinhLuong_TV4.sql`, `sp_TinhBangLuongThang` | Đếm trực tiếp CHAMCONG theo khoảng đầu tháng đến trước đầu tháng sau trong transaction; payroll chỉ đọc CHAMCONG làm nguồn công. |

Nếu cần mở source Java, vào `ChamCongService.nhapChamCongTheoLo` để chỉ commit/rollback; chuyển sang `ChamCongDAO.insertInTransaction` để chỉ connection được truyền xuống và câu gọi stored procedure.

Nếu cần mở giao diện, vào `ChamCongPanel` phần tải nhật ký và phần tải tổng hợp; truy vấn tổng hợp đi tiếp qua `ChamCongService.layTongHopChamCongThang` tới DAO đọc view.

Không diễn đạt bằng chứng fixture hoặc test tự động thành “đã demo tay”. [FIX_TASKLIST](FIX_TASKLIST.md) ghi log runtime: suite TV2 strict PASS trong runner tích hợp; các race khóa kỳ và cleanup PASS; ảnh tracked cũ không phải bằng chứng runtime hiện tại. [Báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md), G06/G07/G12, xác nhận HR ghi A ngày 09/10/2026, tổng hợp 10/2026 có 1 ngày làm/9 giờ và Payroll chỉ đọc công, các nút ghi bị vô hiệu hóa. Hồi quy sau sửa có **224 assertions PASS** (GuiQa 55, Security 104, LightTheme 45, LoginChip 20). Nhập lô GUI và kiểm thử thủ công đầy đủ UI với database chậm vẫn chưa có minh chứng trong đợt này.

## 6. Câu hỏi vấn đáp gợi ý

1. **Vì sao cần cả SP, constraint và trigger?** SP kiểm tra đầu vào và trả thông báo theo nghiệp vụ; constraint bảo vệ quan hệ/miền dữ liệu; trigger bảo vệ các quy tắc cần tra bảng khác và mọi đường ghi.
2. **Làm sao ngăn hai yêu cầu cùng ghi trùng?** Unique constraint `(MaNV, NgayChamCong)` là chốt cuối ở SQL Server; kiểm tra trùng trong SP giúp báo lỗi sớm nhưng không thay thế unique constraint.
3. **Một dòng lỗi trong batch thì chuyện gì xảy ra?** Service rollback transaction trên connection dùng chung; không giữ các dòng đã chèn trước đó.
4. **Tại sao cập nhật ngày phải kiểm tra kỳ cũ và kỳ mới?** Một bản ghi có thể bị chuyển ra khỏi kỳ đã chốt hoặc chuyển vào kỳ đã chốt; trigger module 05 khóa cả hai phía.
5. **Vì sao dùng index này?** Truy vấn thường lọc theo mã nhân viên và ngày; các cột giờ/trạng thái được INCLUDE để phục vụ đọc tổng hợp. Đánh đổi là tăng dung lượng và chi phí ghi.
6. **Nếu được hỏi về số liệu benchmark trong báo cáo?** Nêu rõ đó là kết quả lịch sử trên bảng tạm; trạng thái minh chứng hiện tại nằm trong FIX_TASKLIST và log được trỏ tại đó.
7. **Ai sở hữu `fn_TinhSoNgayCong` và payroll dùng gì?** TV1 sở hữu hàm, dù script cài ở module 02. Payroll hiện đếm trực tiếp CHAMCONG trong transaction và không gọi hàm đó.
8. **`fn_TongPhuCap` làm gì và thuộc ai?** Nhận MaNV/tháng/năm, SUM SoTien phụ cấp đúng nhân viên/kỳ, trả 0 khi không có khoản. TV2 phối hợp TV3; function ở module 03 và được payroll gọi để tính tổng phụ cấp.
9. **Payroll có thể sửa công không?** Role Payroll chỉ được SELECT CHAMCONG; INSERT/UPDATE/DELETE và execute SP ghi công bị DENY. TV5 phụ trách quyền DB.
10. **Sau chốt, điều gì bảo vệ nguồn chấm công?** Trigger ở module 05 kiểm tra trạng thái kỳ bằng khóa `UPDLOCK, HOLDLOCK` và từ chối insert/update/delete có ngày thuộc kỳ đã chốt.
11. **Trạng thái kiểm thử hiện tại được xác nhận ở đâu?** Mở FIX_TASKLIST cho suite SQL/race và báo cáo GUI 09/10 cho ca thao tác trực tiếp. GUI đã kiểm tra công 10/2026; lần tập tiếp theo dùng lại fixture này. Ảnh cũ và hướng dẫn GUI không thay cho kết quả chạy thực tế.

## 7. Checklist trước khi lên trình bày

- [ ] Slide 4–6 khớp tên đối tượng SQL và ownership; không ghi điểm rubric chưa có.
- [ ] Nếu nhóm chọn phân bổ này, phần nói TV2 khoảng 02:00 và demo khoảng 00:45; thời lượng được thống nhất khi rehearsal.
- [ ] Launcher trỏ database QA; đăng nhập đúng `gui_hr`; không hiển thị/copy mật khẩu.
- [ ] Chỉ đọc dòng A–2026-10-09; nếu trình diễn trùng ngày, gọi rõ đây là ca lỗi riêng.
- [ ] Dùng kết quả tổng hợp đã kiểm chứng; không mặc định fixture chỉ có một dòng công.
- [ ] Tổng hợp 10/2026 có A 1 công/9 giờ, B 0 công; bàn giao TV3 xem khoản hiện có.
- [ ] SSMS, nếu cần, đang ở QA và chỉ chạy SELECT.
- [ ] Không tạo/reset QA, seed dữ liệu hay thực hiện thao tác ghi trên database dự án gốc.
- [ ] Sau slide 6, chuyển phần nói cho TV3; chưa bắt đầu demo ở phút 04:00.
- [ ] Sau khoảng 45 giây TV2 trong demo sau phần nói 10 phút, bàn giao demo cho TV3.

## Nguồn trong repository

- [README](../README.md)
- [Kế hoạch phân công và rubric](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md)
- [Tasklist xác minh hiện tại](FIX_TASKLIST.md)
- [Kịch bản kiểm thử giao diện](GUI_TEST_GUIDE.md)
- [Báo cáo GUI và kiểm tra sau sửa 09/10](GUI_TEST_REPORT_20261009.md)
- [Tài khoản demo](DEMO_ACCOUNTS.md)
- [Module SQL chấm công](../database/02_Module_ChamCong_TV2.sql)
- [Module SQL phụ cấp/khấu trừ](../database/03_phucap_khautru_TV3.sql)
- [Module SQL tính lương](../database/04_Module_TinhLuong_TV4.sql)
- [Module SQL bảo mật và khóa kỳ](../database/05_Security_Payroll_TV5.sql)
- [ChamCongService](../src/main/java/com/service/ChamCongService.java) · [ChamCongDAO](../src/main/java/com/dao/ChamCongDAO.java) · [ChamCongPanel](../src/main/java/com/ui/chamcong/ChamCongPanel.java)

- [Hướng dẫn hoàn thiện bộ nộp và đầu ra từng TV](HUONG_DAN_HOAN_THIEN_BO_NOP.md)
