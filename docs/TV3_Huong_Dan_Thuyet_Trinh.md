# TV3 – Hướng dẫn trình bày và thuyết trình

- **Người trình bày:** Trần Tiến Đạt – MSSV 24110198
- **Ngày cập nhật:** 10/10/2026
- **Phạm vi:** Phụ cấp, khấu trừ và transaction xóa kỳ lương chưa chốt
- **Phần nói:** Slide 7–9, tối đa 2 phút (04:00–06:00)
- **Demo:** khoảng 45 giây trong tổng 5 phút demo của nhóm

Đã đối chiếu phân công và SQL/Java trên `main` commit `0223f2f`, sau [PR #38](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/38). Tên nút/tab theo giao diện PeopleOS sáng; ownership và số liệu SQL/benchmark ngày 08/10 giữ nguyên.

Demo nối tiếp thống nhất dùng **10/2026**, theo [báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md): A có lương 26.000.000đ, 26 công chuẩn, một công ngày 09/10/2026 (08:00–17:00), phụ cấp 730.000đ và khấu trừ 1.000.000đ; thực nhận **730.000đ**, B **0đ**. Đã có 20 ca thao tác và ba ca kiểm tra lại sau sửa. Trước demo, kiểm tra fixture hiện có theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md); dùng lại dòng công/khoản, không thêm trùng hoặc xóa nguồn để ép số. Kết thúc bằng mở lại kỳ, xác nhận `CHUA_CHOT` và `NgayChot IS NULL`.

## 1. Vị trí trong chương trình

Kế hoạch nhóm giới hạn slide ở tối đa 15 trang, kèm báo cáo và demo. Lịch 10 phút nói + 5 phút demo + 5–10 phút vấn đáp dưới đây là **phương án gợi ý để nhóm phân bổ thời gian**, không phải thời lượng rubric bắt buộc. Theo phương án này, TV1→TV5 nói liên tục trong 10 phút; TV3 nhận ba slide số 7–9, khoảng 2 phút nói từ 04:00 đến 06:00, rồi nhường lời cho TV4. Sau khi TV5 kết thúc phần nói ở phút thứ 10, nhóm mới chuyển sang demo 5 phút; TV3 có thể nhận khoảng 45 giây thao tác và bàn giao demo riêng cho TV4. Nếu nhóm đổi thứ tự/thời lượng, cập nhật mốc cho phù hợp.

| Thời gian | Nội dung |
|---|---|
| Gợi ý 04:00–04:40 | Slide 7: dữ liệu, phạm vi và ràng buộc |
| Gợi ý 04:40–05:25 | Slide 8: tổng hợp, khóa kỳ, transaction xóa kỳ |
| Gợi ý 05:25–06:00 | Slide 9: giao diện, kiểm chứng và chuyển TV4 |
| Trong demo nhóm | Khoảng 45 giây: màn hình phụ cấp/khấu trừ và kết quả lọc |

Rubric trong kế hoạch nhóm đặt mục tiêu toàn dự án: tối thiểu 8 bảng và 3NF; 5 constraint, trigger, view, index, stored procedure, function và transaction; có concurrency/recovery, ít nhất 4 role/login và minh chứng. Đây là mục tiêu dự án, không phải điểm số hay kết luận TV3 một mình đạt toàn rubric.

## 2. Ma trận rubric và phần TV3

| Rubric liên quan | Phần TV3 có thể trình bày | Đối tượng / minh chứng |
|---|---|---|
| Constraint | Tiền không âm; tháng 1–12; năm từ 2020; khóa ngoại nhân viên | `CK_PHUCAP_*`, `CK_KHAUTRU_*`, FK trong `database/03_phucap_khautru_TV3.sql` |
| Stored procedure | Xóa một kỳ lương chưa chốt, xóa con trước cha | `sp_XoaKyLuongChuaChot` trong module 03; test TV3 |
| Function | Tổng khấu trừ theo nhân viên và kỳ; phối hợp function tổng phụ cấp | `fn_TongKhauTru` thuộc TV3; `fn_TongPhuCap` nằm module 03, ownership phối hợp TV2 theo ma trận |
| Trigger | Chặn sửa phụ cấp khi kỳ đã chốt | `trg_PhuCap_KhongSuaKhiDaChotLuong` được tạo trong module 05 |
| View | Số khoản và tổng phụ cấp theo nhân viên/kỳ | `vw_TongPhuCapThang` |
| Index | Tra cứu theo nhân viên, tháng, năm; include cột phục vụ tổng hợp | `IX_PHUCAP_MaNV_ThangNam`; benchmark trên bảng tạm |
| Transaction | Khóa kỳ, xóa chi tiết trước bảng lương, rollback nếu lỗi | `sp_XoaKyLuongChuaChot`; log testcase TV3 |
| Ứng dụng | Lọc và quản lý 2 bảng khoản phát sinh | `PhuCapKhauTruPanel`, service/DAO và GUI test guide |
| Phân quyền | HR/Payroll có CRUD 2 bảng; chỉ Admin/Payroll xóa kỳ | Service + grants module 05; không gán phần chấm công cho TV3 |

**Ranh giới ownership:** `sp_GhiNhanChamCong` là phần TV2. `fn_TongPhuCap` được TV2 phối hợp với TV3, còn ma trận ownership ghi TV2. TV3 sở hữu `sp_XoaKyLuongChuaChot`, `fn_TongKhauTru`, trigger phụ cấp, view tổng phụ cấp và index phụ cấp.

## 3. Ba slide và lời nói mẫu (khoảng 2 phút)

### Slide 7 — Dữ liệu phụ cấp và khấu trừ

**Ý chính trên slide:** Hai bảng theo nhân viên và kỳ; khóa ngoại; CHECK tiền/tháng/năm; không có quy tắc UNIQUE theo tên khoản.

**Lời nói (40 giây):**

“Phần của em là phụ cấp và khấu trừ theo từng nhân viên trong từng kỳ lương. Hai bảng lưu khoản phát sinh riêng, liên kết nhân viên bằng khóa ngoại nên không tạo dữ liệu mồ côi. CHECK chặn số tiền âm, tháng ngoài 1 đến 12 và năm trước 2020. Một nhân viên có thể có nhiều khoản cùng kỳ; tên khoản không phải khóa duy nhất. Dữ liệu này là đầu vào để tính thực nhận: tiền công cộng tổng phụ cấp rồi trừ tổng khấu trừ.”

### Slide 8 — Tổng hợp và bảo vệ kỳ lương

**Ý chính trên slide:** Function/View tổng hợp; trigger chặn kỳ đã chốt; procedure xóa kỳ nháp trong transaction; index phục vụ lọc.

**Lời nói (45 giây):**

“`fn_TongKhauTru` trả tổng khấu trừ của một nhân viên trong một tháng, còn `vw_TongPhuCapThang` gom số khoản và tổng tiền để tra cứu. Khi kỳ đã chốt, trigger ở module bảo mật khóa dòng kỳ cha bằng `UPDLOCK, HOLDLOCK` và kiểm tra cả dữ liệu cũ lẫn mới, nên sửa, xóa hoặc chuyển khoản sang kỳ chốt đều bị từ chối. Khi xóa kỳ nháp, `sp_XoaKyLuongChuaChot` mở transaction, khóa kỳ, xóa chi tiết trước bảng lương và rollback nếu có lỗi. Index phụ cấp hỗ trợ lọc theo nhân viên và kỳ.”

### Slide 9 — Giao diện và minh chứng

**Ý chính trên slide:** Giao diện lọc theo tháng/năm; QA SQL xác nhận function/view, constraint và xóa kỳ; benchmark fixture riêng.

**Lời nói (35 giây):**

“Trên giao diện, người dùng chọn kỳ, xem hai danh sách và tổng hợp phụ cấp. HR và Payroll được thao tác phụ cấp, khấu trừ theo quyền; xóa kỳ chỉ dành cho Admin hoặc Payroll. Test TV3 ngày 08/10 chạy trên database QA riêng đã xác nhận function, view, constraint, xóa kỳ nháp, từ chối kỳ đã chốt và cleanup fixture. Benchmark 30 nghìn dòng trên bảng tạm ghi nhận 642 logical reads trước index và 2 sau index ở lần chạy đó; đây là phép đo của fixture, không phải cam kết cho mọi máy. Em xin chuyển phần tính lương cho TV4.”

## 4. Mở code khi được hỏi

Mở nhanh từ thư mục gốc repository:

1. `database/03_phucap_khautru_TV3.sql`: hai bảng, constraints, index, view, `fn_TongKhauTru`, `sp_ThemPhuCapNhanVien`, `sp_XoaKyLuongChuaChot`, `fn_TongPhuCap`.
2. `database/05_Security_Payroll_TV5.sql`: trigger `trg_PhuCap_KhongSuaKhiDaChotLuong`; xem thêm trigger khấu trừ/chấm công và grants nếu câu hỏi mở rộng.
3. `database/tests_TV3/test_module_phucap_khautru_TV3.sql`: fixture riêng, assert function/view, lỗi constraint, xóa nháp, từ chối kỳ chốt, cleanup.
4. `database/tests_TV3/test_benchmark_index_TV3.sql`: benchmark 30.000 dòng trong `#PHUCAP_BENCHMARK`, tạo index rồi đo cùng truy vấn.
5. `src/main/java/com/ui/luong/PhuCapKhauTruPanel.java`: hai tab, lọc, nút thêm/xóa và nút xóa kỳ. Service/DAO ở `src/main/java/com/service/PhuCapKhauTruService.java`, `src/main/java/com/dao/PhuCapDAO.java`, `src/main/java/com/dao/KhauTruDAO.java`.

Log có thể dẫn: `build/sql-verification/20261008_101955_11b4e24c/test_module_phucap_khautru_TV3.log` xác nhận các assert test TV3; `benchmark_test_benchmark_index_TV3.log` ghi 642 logical reads trước và 2 sau index trên bảng tạm 30.000 dòng; `Closed_Source_Concurrency.log` ghi kết quả race kiểm tra kỳ chốt. Các log tồn tại trong checkout này và thuộc lần chạy trên database QA đã dọn. Ảnh trong `screenshots/TV3/` là ảnh có sẵn, không chứng minh thao tác GUI bản hiện tại.

Phân biệt minh chứng khi nói: log test là kết quả assert SQL tự động; log benchmark là phép đo trên bảng tạm; GUI test guide là kịch bản thao tác cho người chạy. [Báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md), G08/G09/G15, ghi HR thêm phụ cấp ăn trưa 730.000đ và tạm ứng 1.000.000đ cho A ở 10/2026; Payroll bị từ chối thêm phụ cấp B khi kỳ đã chốt, SQL xác nhận không có khoản B. Hồi quy sau sửa có **224 assertions PASS** (GuiQa 55, Security 104, LightTheme 45, LoginChip 20). Xóa khoản/xóa kỳ qua GUI chưa được kiểm tra trong đợt này.

## 5. Demo GUI 45 giây

**Chuẩn bị:** mở QA bằng `powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1`, đăng nhập `gui_hr`. Kiểm fixture theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md): A có một công ngày 09/10/2026, một phụ cấp 730.000đ và một khấu trừ 1.000.000đ ở 10/2026; B không có khoản. Không tạo lại nhân viên hay khoản. Nếu kỳ đã chốt, Payroll mở lại trong bước chuẩn bị.

1. Mở **Phụ cấp & Khấu trừ**, chọn **10/2026**.
2. Tab **Phụ cấp**: chỉ khoản **Phụ cấp ăn trưa** 730.000đ của A đã có. Tab **Khấu trừ**: chỉ khoản **Tạm ứng lương** 1.000.000đ đã có. Không bấm Thêm trong lượt demo này.
3. Bấm **Xem dữ liệu**, mở **Tổng hợp theo kỳ**, đối chiếu tổng phụ cấp 730.000đ/khấu trừ 1.000.000đ. Khoản đúng không cần nhập lại.
4. Bàn giao TV4: tiền công 1.000.000đ + phụ cấp 730.000đ − khấu trừ 1.000.000đ = **730.000đ**. B 0 công/0 khoản nên 0đ. Nếu dữ liệu khác, nêu số thực tế, kiểm tra nguồn trước khi tính; không thêm/xóa khoản để ép kết quả.

## 6. SQL đọc an toàn và phần dự phòng

Để minh họa tổng hợp, dùng truy vấn chỉ đọc và kỳ đã chọn:

```sql
DECLARE @MaNV INT = (
    SELECT MaNV FROM dbo.NHANVIEN WHERE Email = 'gui-a@example.invalid'
);
SELECT MaNV, HoTen, Thang, Nam, SoKhoanPhuCap, TongTienPhuCap
FROM dbo.vw_TongPhuCapThang
WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026;
SELECT dbo.fn_TongKhauTru(@MaNV, 10, 2026) AS TongKhauTru;
```

Đoạn SQL tự tìm MaNV fixture A qua email và chỉ đọc kỳ 10/2026. Chạy trên database QA đã chọn; nếu email trả về NULL, dừng và xác minh database/fixture. Tránh truy vấn rộng toàn bảng khi demo.

**Nếu demo xóa kỳ:** chỉ dùng database QA tách biệt và kỳ nháp tạo riêng cho QA; kiểm tra kỳ đó là `CHUA_CHOT`, không phải 10/2026 đang demo, rồi mới gọi `EXEC dbo.sp_XoaKyLuongChuaChot @Thang=..., @Nam=...;`. Không chạy trên kỳ dữ liệu thật hay kỳ demo nhóm. Kỳ chốt phải bị từ chối và transaction rollback; không thử xóa để “chứng minh” trên dữ liệu quan trọng.

**Nếu GUI không sẵn sàng:** chuyển sang log SQL test TV3 và giải thích đây là kiểm chứng tự động trên QA. Nếu hỏi benchmark, nêu con số fixture cùng giới hạn. Không nói đã PASS thao tác GUI thủ công khi chưa có log xác nhận.

Runner tích hợp tạo database QA có tên ngẫu nhiên, cài schema và cleanup database đó khi hoàn tất. Nếu nhóm chủ động chạy lại trước buổi bảo vệ, lệnh là:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run_sql_verification.ps1 -Benchmarks
```

`-Benchmarks` bật benchmark; không thêm `-KeepDatabase` nếu muốn runner dọn database QA của lần chạy. Kết quả mới sẽ ở `build/sql-verification/<runId>/`; trích đúng log lần chạy mới, không lấy ảnh cũ thay thế.

## 7. Câu hỏi vấn đáp dự kiến

1. **Vì sao tách hai bảng?** Hai loại phát sinh có ý nghĩa nghiệp vụ và trường mô tả riêng; mỗi dòng vẫn tham chiếu nhân viên và kỳ.
2. **Vì sao không UNIQUE theo tên khoản?** Một nhân viên có thể phát sinh nhiều khoản cùng tên trong một kỳ; tên mô tả không định danh bản ghi.
3. **Function khác View thế nào ở đây?** `fn_TongKhauTru(MaNV, Thang, Nam)` SUM SoTien cho nhân viên/kỳ, trả 0 khi không có khoản; View trả tập dòng tổng hợp phụ cấp cho nhiều nhân viên/kỳ.
4. **Vì sao dùng transaction khi xóa kỳ?** Nếu xóa chi tiết thành công nhưng xóa kỳ thất bại, transaction rollback để tránh trạng thái dở dang.
5. **Vì sao xóa con trước cha?** Chi tiết bảng lương tham chiếu bảng lương bằng khóa ngoại; xóa con trước tránh vi phạm ràng buộc.
6. **Trigger khóa kỳ bằng cách nào?** Trigger module 05 lấy khóa trên dòng kỳ cha bằng `UPDLOCK, HOLDLOCK`, xét kỳ từ `inserted` và `deleted`, rồi từ chối thay đổi nếu kỳ đã chốt.
7. **Chuyển khoản từ nháp sang kỳ chốt có bị chặn không?** Có. Trigger kiểm tra cả kỳ cũ lẫn kỳ mới nên không thể lách bằng UPDATE đổi tháng/năm.
8. **HR có được xóa kỳ lương không?** Không. Service và quyền SQL chỉ cho Admin/Payroll gọi nghiệp vụ xóa kỳ; HR được CRUD khoản phụ cấp/khấu trừ.
9. **Index cải thiện bao nhiêu?** Benchmark fixture 30.000 dòng ghi 642 logical reads trước và 2 sau. Số đo phụ thuộc máy, cache và dữ liệu; không khái quát thành tốc độ production.
10. **Test hiện tại chứng minh điều gì?** Log 08/10 chứng minh các assert SQL trên QA; báo cáo GUI 09/10 ghi thao tác trực tiếp khoản 10/2026 và từ chối ghi kỳ đã chốt. Minh chứng này không xác nhận kiểm thử database dự án gốc.
11. **Ai sở hữu `sp_GhiNhanChamCong` và `fn_TongPhuCap`?** `sp_GhiNhanChamCong` thuộc TV2. `fn_TongPhuCap` ở module 03 và TV2 phối hợp TV3; theo ma trận ownership là TV2.

## 8. Checklist trước khi lên trình bày

- [ ] Đúng slide 7–9; nếu nhóm dùng phương án thời gian ở mục 1, nói khoảng 04:00–06:00 và demo tối đa khoảng 45 giây.
- [ ] Mở sẵn ứng dụng và tab Phụ cấp & Khấu trừ; đăng nhập đúng tài khoản QA.
- [ ] Xác nhận tên nhân viên A và email fixture `gui-a@example.invalid` trước khi chạy SQL.
- [ ] Lọc đúng 10/2026; chỉ đọc khoản GUI QA hiện có, không bấm Thêm.
- [ ] A có một phụ cấp ăn trưa 730.000đ và một tạm ứng 1.000.000đ; B không có khoản.
- [ ] Nếu thiếu/khác fixture, xác minh trước buổi diễn; không ghi/xóa nguồn để ép expected.
- [ ] Kiểm tra kỳ 10/2026 chưa chốt; nếu đã chốt thì Payroll mở lại trong bước chuẩn bị.
- [ ] Không ghi dữ liệu vào kỳ lương đã chốt; không xóa kỳ demo chính.
- [ ] Mở sẵn module 03, module 05, test TV3 và log QA cần viện dẫn.
- [ ] Đặt sẵn SQL Server Management Studio ở đúng database QA nếu cần chạy truy vấn chỉ đọc.
- [ ] Đối chiếu số liệu thực tế trước khi nhắc kết quả tính lương 730.000.
- [ ] Nếu số khoản trên giao diện khác kỳ vọng, chỉ mô tả kết quả đang có; không sửa dữ liệu ngẫu nhiên tại chỗ.
- [ ] Chỉ đọc SQL trong demo; nếu cần ghi/xóa, dùng database QA và kỳ nháp dành riêng.
- [ ] Nêu phạm vi và giới hạn của log/benchmark; không dùng ảnh cũ làm bằng chứng mới.
- [ ] Chuyển lời đúng mốc cho TV4.

**Chuyển TV4:** “Phần em trình bày đã cho thấy phụ cấp và khấu trừ được tổng hợp theo kỳ, đồng thời dữ liệu kỳ chốt được bảo vệ. Em xin chuyển cho bạn Vinh trình bày cách dùng các số liệu này để tính bảng lương và kiểm tra kết quả thực nhận.”

**Bàn giao TV4 trong phần demo (sau 10 phút nói):** “Em đã lọc xong các khoản phụ cấp và khấu trừ của nhân viên A. Mời bạn Vinh tiếp tục phần tính lương để đối chiếu thực nhận.”

**Giới hạn minh chứng:** Benchmark và PASS SQL áp dụng log QA ngày 08/10; các ca GUI trực tiếp áp dụng báo cáo 09/10 và dữ liệu 10/2026. Chưa kiểm thử database dự án gốc.

## Tài liệu đối chiếu

- [Phân công và rubric nhóm](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md)
- [Module SQL TV3](../database/03_phucap_khautru_TV3.sql) · [Trigger và quyền SQL](../database/05_Security_Payroll_TV5.sql)
- [Test TV3](../database/tests_TV3/test_module_phucap_khautru_TV3.sql) · [Benchmark](../database/tests_TV3/test_benchmark_index_TV3.sql)
- [Hướng dẫn GUI](GUI_TEST_GUIDE.md) · [Tài khoản demo](DEMO_ACCOUNTS.md) · [Kết quả kiểm thử](FIX_TASKLIST.md)
- [Báo cáo GUI và kiểm tra sau sửa 09/10](GUI_TEST_REPORT_20261009.md)
- [Phần tiếp theo: TV4](TV4_Huong_Dan_Thuyet_Trinh.md)

- [Hướng dẫn hoàn thiện bộ nộp và đầu ra từng TV](HUONG_DAN_HOAN_THIEN_BO_NOP.md)
