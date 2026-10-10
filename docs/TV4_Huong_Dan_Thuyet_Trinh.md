# TV4 — Hướng dẫn trình bày và thuyết trình cá nhân

**Nguyễn Quang Vinh · MSSV 24110385 · Nhóm 06 · Cập nhật 10/10/2026**

Đã đối chiếu phân công và SQL/Java trên `main` commit `0223f2f`, sau [PR #38](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/38). Tên nút/tab theo giao diện PeopleOS sáng; công thức, stored procedure và số liệu SQL/benchmark giữ đúng ngày chạy.

Demo nối tiếp thống nhất dùng **10/2026**, theo [báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md): A có lương 26.000.000đ, 26 công chuẩn, một công ngày 09/10/2026 (08:00–17:00), phụ cấp 730.000đ và khấu trừ 1.000.000đ; thực nhận **730.000đ**, B **0đ**. Đã có 20 ca thao tác và ba ca kiểm tra lại sau sửa. Trước demo, kiểm tra fixture hiện có theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md); dùng lại dòng công/khoản, không thêm trùng hoặc xóa nguồn để ép số. Kết thúc bằng mở lại kỳ, xác nhận `CHUA_CHOT` và `NgayChot IS NULL`.

Phương án phân bổ gợi ý: TV4 trình bày **slide 10–12**, khoảng **06:00–08:00**, và phụ trách **60 giây tính lương** trong phần demo chung. TV1→TV5 nói liên tục 10 phút, sau đó demo nối tiếp 5 phút, tổng 15 phút trình bày; vấn đáp 5–10 phút sau đó. Đây là mốc tập dượt phù hợp [kế hoạch nhóm](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md), không phải rubric bắt buộc mỗi người nói đúng 2 phút.

## 1. Phần việc và tiêu chí cần chứng minh

TV4 phụ trách công thức, bảng kỳ/chi tiết lương, transaction tính lương và luồng Java gọi stored procedure. Phối hợp TV1 về lịch sử lương, TV2/TV3 về dữ liệu nguồn và TV5 về chốt, phân quyền, concurrency.

| Mục rubric / phần phân công | Đối tượng TV4 | Điều cần giải thích hoặc đưa bằng chứng |
|---|---|---|
| Bảng, khóa và constraint | `BANGLUONG`, `CHITIETBANGLUONG` | UNIQUE tháng/năm; UNIQUE kỳ/nhân viên; FK; ngày công chuẩn > 0. Khóa chi tiết hiện tên `MaChiTiet`. |
| Stored procedure có TRY…CATCH | `sp_TinhBangLuongThang` | Kiểm tra tham số, trạng thái kỳ, dữ liệu công; trả `MaBangLuong` bằng tham số OUTPUT; phát lại lỗi khi thất bại. |
| Function | `fn_TinhTienCong` | Lương cơ bản / ngày công chuẩn × ngày công thực tế, làm tròn 2 chữ số bằng DECIMAL. |
| Trigger | `trg_BangLuong_KhongSuaKhiDaChot` | Bảo vệ kỳ đã chốt; có ngoại lệ chuyển trạng thái mở lại hợp lệ theo quy trình. |
| View | `vw_TongKhauTruThang` | Tổng khấu trừ theo nhân viên/tháng/năm. Payroll thực tế lấy tổng bằng `fn_TongKhauTru`, không đọc view này để tính. |
| Index và đo trước/sau | `IX_KHAUTRU_MaNV_ThangNam` | Khóa `(MaNV, Thang, Nam)`, INCLUDE `SoTien`; đối chiếu IO, TIME và execution plan. |
| Transaction | Tính bảng lương tháng | Header và toàn bộ chi tiết thành công cùng nhau; lỗi không để lại kết quả nửa chừng. |
| Ứng dụng và kiểm thử | `BangLuongPanel` → `PayrollService` → `BangLuongDAO` | GUI nhận kết quả từ SQL, kiểm tra quyền, xử lý lỗi; E2E và concurrency có log riêng. |

Các ngưỡng số lượng như ≥5 SP/function/transaction là yêu cầu **toàn nhóm** trong [rubric của kế hoạch](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md#5-checklist-nghiệm-thu-theo-projectrubric) và [README](../README.md). Phần TV4 phải chứng minh đối tượng được giao và sự liên kết trong nghiệp vụ, không chỉ đọc tên để đủ số lượng.

## 2. Ba slide và lời nói mẫu

### Slide 10 — Từ dữ liệu nguồn đến kết quả lương (06:00–06:40)

**Trên slide:** luồng NHANVIEN/LICHSULUONG + CHAMCONG + phụ cấp/khấu trừ → BANGLUONG/CHITIETBANGLUONG; hai công thức; kết quả GUI 10/2026 A 730.000 đồng, B 0 đồng.

> “Em là Nguyễn Quang Vinh, phụ trách module tính lương. Đầu vào là mức lương áp dụng cho kỳ, số ngày công và các khoản phát sinh mà hai bạn vừa trình bày. Tiền công bằng lương cơ bản chia ngày công chuẩn rồi nhân ngày công thực tế; thực nhận bằng tiền công cộng phụ cấp trừ khấu trừ. Minh chứng GUI tháng 10/2026 ghi A có lương 26 triệu, một ngày công trên chuẩn 26 ngày, phụ cấp 730 nghìn và khấu trừ một triệu, nên thực nhận 730 nghìn. B không có công hoặc khoản nên nhận 0. Khi demo, nhóm kiểm tra và dùng lại đúng dữ liệu đó.”

### Slide 11 — Transaction tính lương và bảo vệ tính lại (06:40–07:25)

**Trên slide:** kiểm tra → khóa kỳ/nguồn → chọn mức lương → tạo hoặc cập nhật kỳ nháp → ghi chi tiết → commit; nhánh lỗi rollback.

> “Logic chính đặt trong `sp_TinhBangLuongThang`. Procedure kiểm tra kỳ và ngày công chuẩn, yêu cầu có dữ liệu chấm công, rồi giữ khóa kỳ và dữ liệu nguồn trong transaction. Kỳ chưa chốt có thể tính lại trên cùng mã bảng lương; xóa chi tiết cũ và tạo chi tiết mới nằm trong cùng transaction. Nếu ghi chi tiết lỗi, TRY…CATCH xử lý rollback nên không mất kết quả cũ hoặc để lại bảng lương dở dang. Kỳ đã chốt bị từ chối. Khi tính lại tháng quá khứ, hệ thống ưu tiên mức lương đã lưu ở chi tiết cũ, tiếp đến lịch sử lương, nhằm tránh áp mức lương hiện tại cho kỳ cũ.”

### Slide 12 — JDBC, hiệu năng và kiểm thử (07:25–08:00)

**Trên slide:** luồng Panel → Service → DAO → SP; E2E 10/10; benchmark fixture 30.000 dòng, logical reads 547→4; phối hợp TV5 về concurrency.

> “Từ giao diện, service kiểm tra quyền và DAO gọi procedure bằng CallableStatement, nhận mã bảng lương OUTPUT. Bộ E2E ghi nhận 10 trên 10 trường hợp đạt, gồm công thức, tính lại và rollback; benchmark khấu trừ trên fixture 30 nghìn dòng cho logical reads từ 547 xuống 4 khi so sánh scan và index seek. Đây là số đo truy vấn trên dữ liệu kiểm thử, không phải cam kết tốc độ toàn ứng dụng. Tiếp theo, bạn Đức Anh trình bày chốt lương, danh tính SQL và kiểm soát truy cập.”

**Cách dựng và tập nói:** mỗi slide giữ một hình hoặc bảng nhỏ, khoảng 3–5 ý; lời mẫu để trong ghi chú người thuyết trình. Bấm giờ khi đọc; nếu vượt 2 phút, chuyển chi tiết savepoint và index INCLUDE sang vấn đáp. Không đưa nguyên procedure lên slide.

## 3. Demo nối tiếp — tính lương trong 60 giây

**Chuẩn bị trước buổi trình bày:** chạy `powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1` tại thư mục gốc; dùng QA theo [DEMO_ACCOUNTS](DEMO_ACCOUNTS.md), không dùng nhầm database dự án gốc. Mật khẩu tra [demo_accounts.json](../database/demo_accounts.json). Nếu kỳ 10/2026 đã chốt từ lần tập trước, Payroll mở lại trong khâu chuẩn bị; nhóm kiểm tra và dùng lại dòng công/khoản đúng đã có, tránh cộng lần hai.

Sau khi TV3 kết thúc demo, kiểm lại fixture A có lương áp dụng 26.000.000, một công `CO_MAT` ngày 09/10/2026 (08:00–17:00), phụ cấp 730.000 và khấu trừ 1.000.000 cho tháng 10/2026; B 0 công/0 khoản. Tra nhân viên qua tên/email, không phụ thuộc MaNV 1008 trên máy hiện tại. Kỳ demo phải chưa chốt.

1. Đăng xuất `gui_hr`, đăng nhập `gui_payroll`.
2. Mở **Bảng lương**; chọn tháng **10**, năm **2026**, ngày công chuẩn **26**.
3. Bấm **Tính / cập nhật lương**, xác nhận và đợi thông báo hoàn tất; không bấm lặp khi đang chạy.
4. Mở chi tiết kỳ, tìm `TEST Giao dien A` và chỉ vào các số dưới đây. Nếu đủ thời gian, tính lại một lần để kiểm tra cùng một kỳ; phần này có thể để dành cho vấn đáp.
5. Bàn giao máy cho TV5: **“Bảng lương đang ở trạng thái chưa chốt; bạn Đức Anh sẽ chốt kỳ và chứng minh quyền xem phiếu lương cá nhân.”**

| Chỉ tiêu của A | Kỳ vọng với đúng dữ liệu đã thống nhất |
|---|---:|
| Ngày công thực tế | 1 |
| Tiền công | 1.000.000 |
| Tổng phụ cấp | 730.000 |
| Tổng khấu trừ | 1.000.000 |
| Thực nhận | 730.000 |

B có thực nhận **0đ**; bộ QA đã đối chiếu có 2 nhân viên và tổng 730.000đ. Nếu database hiện có thêm nhân viên, tổng công ty có thể khác số này. Nếu sai số, đối chiếu nguồn/mức lương đã lưu trước khi kết luận lỗi công thức. Không sửa lương cơ bản hoặc xóa cả kỳ chỉ để làm số trên màn hình khớp ví dụ.

**SQL đọc dự phòng:** mở SSMS trên đúng QA bằng tài khoản Payroll hoặc DBA, dùng sau khi đã tính lương; không thay phần demo GUI bằng việc khẳng định đã thao tác thành công.

```sql
DECLARE @MaNV INT = (SELECT MaNV FROM dbo.NHANVIEN
                     WHERE Email = 'gui-a@example.invalid');
IF @MaNV IS NULL THROW 51000, N'Không tìm thấy fixture A.', 1;

SELECT MaBangLuong, MaNV, HoTen, Thang, Nam, TrangThaiBangLuong,
       LuongCoBan, NgayCongThucTe, TienCong,
       TongPhuCap, TongKhauTru, ThucNhan
FROM dbo.vw_BangLuongChiTiet
WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026;

SELECT Thang, Nam, COUNT(*) AS SoKy
FROM dbo.BANGLUONG
WHERE Thang = 10 AND Nam = 2026
GROUP BY Thang, Nam;
```

## 4. Source và bằng chứng cần mở sẵn

- [Module SQL TV4](../database/04_Module_TinhLuong_TV4.sql): tìm `sp_TinhBangLuongThang`, `fn_TinhTienCong`, trigger, view và index trong bảng ownership.
- [BangLuongPanel](../src/main/java/com/ui/luong/BangLuongPanel.java), [PayrollService](../src/main/java/com/service/PayrollService.java), [BangLuongDAO](../src/main/java/com/dao/BangLuongDAO.java): lần theo `tinhBangLuongThang`, `CallableStatement` và tham số OUTPUT.
- [E2E](../database/tests/TV4_Payroll_E2E.sql): các ca công thức, rollback, tính lại và bảo toàn header/chi tiết; kết quả tại `build/sql-verification/20261008_101955_11b4e24c/TV4_Payroll_E2E.log` là **10/10 PASS**.
- [Benchmark](../database/tests/TV4_Payroll_Benchmark.sql): log `benchmark_TV4_Payroll_Benchmark.log` trong cùng thư mục runner ghi fixture khấu trừ 30.000 dòng, scan 547/seek 4 logical reads; hai truy vấn cùng trả 128 dòng, tổng 133.356. So sánh có hint ép scan/seek, cần ghi rõ khi diễn giải.
- Concurrency phối hợp TV5: [Session A](../database/tests/TV4_Payroll_Concurrency_SessionA.sql) và [Session B](../database/tests/TV4_Payroll_Concurrency_SessionB.sql); log `build/fix-verification/Concurrency_B.log` ghi B chờ **20631 ms (khoảng 20,6 giây)**, không trùng kỳ/chi tiết.
- [FIX_TASKLIST](FIX_TASKLIST.md), mục T15: xác minh người nghỉ việc và mức lương kỳ cũ. **Lịch sử trước migration bị thiếu không tự phục hồi được.**
- [Báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md), G13: Payroll tính 10/2026 với 26 công chuẩn; A có 1 công, tiền công 1.000.000đ, phụ cấp 730.000đ, khấu trừ 1.000.000đ → thực nhận 730.000đ; B 0đ. Kỳ có 2 nhân viên, tổng 730.000đ và đã được đối chiếu SQL. Hồi quy sau sửa có **224 assertions PASS** (GuiQa 55, Security 104, LightTheme 45, LoginChip 20).

Các thư mục `build/` không nằm trong Git; chép log/plan cần trình bày vào bộ minh chứng trước buổi bảo vệ. PASS trên QA của runner không phải PASS cho database gốc. Đã có các ca GUI 09/10 được báo cáo riêng; chưa thử đồng thời nhiều phiên GUI trong đợt đó.

## 5. Câu hỏi vấn đáp và câu trả lời ngắn

1. **Vì sao tính lương ở SQL thay vì Java?** Để quy tắc tính và transaction nằm cùng dữ liệu; Java gọi SP, hiển thị kết quả và xử lý lỗi. Service và SQL cùng kiểm tra quyền theo trách nhiệm từng tầng.
2. **Một kỳ có bị tạo hai lần không?** Có UNIQUE `(Thang, Nam)` và khóa `UPDLOCK, HOLDLOCK` trên kỳ; tính lại kỳ nháp giữ mã header. Chi tiết có UNIQUE `(MaBangLuong, MaNV)`.
3. **Ngày công được tính thế nào?** Procedure đếm các dòng `CO_MAT`, `DI_TRE`, `VE_SOM` trong khoảng ngày đầu tháng đến trước tháng sau. Hiện không tính giờ tăng ca hoặc nửa ngày; không gọi `fn_TinhSoNgayCong` trong payroll.
4. **Ngày công chuẩn 0 và tháng không có công thì sao?** SP báo lỗi khi chuẩn ≤0 hoặc không có dòng chấm công trong toàn kỳ. Function tiền công trả 0 với đầu vào không hợp lệ; kiểm tra nghiệp vụ tại SP vẫn từ chối tính.
5. **Nếu lỗi sau khi xóa chi tiết cũ?** Xóa và ghi mới cùng transaction, rollback khôi phục trạng thái trước lần tính; OUTPUT được đặt NULL khi thất bại. E2E kiểm tra tình huống này.
6. **Nếu caller đã mở transaction thì sao?** SP tính lương dùng savepoint, không tự commit transaction caller. Khi transaction còn committable, rollback về savepoint; nếu đã không thể commit thì caller phải xử lý transaction ngoài. Không khẳng định mọi SP đều có hành vi này.
7. **Người đã nghỉ còn được tính tháng trước không?** Có thể được đưa vào nếu ngày làm/nghỉ hoặc phát sinh cho kỳ phù hợp. Không chỉ lọc theo trạng thái hiện tại; xem phần tạo tập `@NhanVienNguon`.
8. **Đổi lương hiện tại có thay lương tháng cũ không?** Với kỳ quá khứ đã có chi tiết, ưu tiên mức đã lưu; nếu chưa có dùng lịch sử hiệu lực rồi fallback mức hiện tại. Không thể tự suy ra mức lương đã mất trước khi có lịch sử.
9. **Vì sao lưu LuongCoBan ở chi tiết khi NHANVIEN đã có?** Chi tiết lưu mức dùng lúc tính để giữ kết quả kỳ lịch sử; mức hiện tại của nhân viên có thể đổi. Đây là snapshot nghiệp vụ cần giải thích khi nói về chuẩn hóa.
10. **Có tự tính BHXH/thuế không?** Công thức hiện cộng/trừ các khoản nhập sẵn; chưa có công cụ tự tính thuế/BHXH theo luật. Không lấy mô tả trên thẻ giao diện làm bằng chứng cho logic chưa cài.
11. **Index INCLUDE giải quyết gì?** Khi lọc đúng MaNV/tháng/năm và tổng SoTien, index có thể cover truy vấn. 547→4 là logical reads trên fixture và truy vấn đã đo, không phải ứng dụng nhanh hơn tương ứng ở mọi tình huống.
12. **Chốt rồi muốn tính lại?** Payroll/Admin mở lại đúng quy trình trước, chỉnh nguồn, tính lại rồi chốt. HR không có quyền tính/chốt/mở lại/xóa kỳ.

## 6. Checklist tập dượt và bàn giao

- [ ] Slide 10–12 đúng tên/MSSV; đọc và bấm giờ khoảng 2 phút, phân biệt công thức với số liệu minh họa.
- [ ] Kỳ QA 10/2026 chưa chốt; A đúng mức lương và đúng một công/những khoản đã thống nhất.
- [ ] Kiểm nguồn cùng kỳ 10/2026 trước khi tính; A 730.000đ/B 0đ theo fixture đã đối chiếu, không thêm/xóa công/khoản.
- [ ] Có mật khẩu `gui_payroll`, đăng xuất/đăng nhập và chọn kỳ thành thạo; thao tác demo khoảng 60 giây.
- [ ] Đối chiếu một kỳ, một chi tiết mỗi nhân viên và thực nhận A; không dùng tổng toàn công ty thay cho A.
- [ ] Mở sẵn SP/DAO, E2E và log benchmark; phân biệt fixture với dữ liệu thật.
- [ ] Phần nói bàn giao cho TV5 ở 08:00; phần demo bàn giao kỳ **CHUA_CHOT** để TV5 chốt.
- [ ] Nắm câu trả lời về rollback/savepoint, snapshot lương, người nghỉ việc và giới hạn công thức.

## Tài liệu đối chiếu

- [Phân công và rubric nhóm](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md)
- [Hướng dẫn thao tác GUI](GUI_TEST_GUIDE.md) · [Tài khoản và fixture demo](DEMO_ACCOUNTS.md)
- [Kết quả kiểm thử](FIX_TASKLIST.md) · [Phần tiếp theo: TV5](TV5_Huong_Dan_Thuyet_Trinh.md)

- [Hướng dẫn hoàn thiện bộ nộp và đầu ra từng TV](HUONG_DAN_HOAN_THIEN_BO_NOP.md)
