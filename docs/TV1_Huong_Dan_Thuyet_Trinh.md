# TV1 — Hướng dẫn trình bày và thuyết trình cá nhân

**Nguyễn Minh Trí · MSSV 24110359 · Nhóm 06 · Cập nhật 10/10/2026**

Đã đối chiếu phân công và SQL/Java trên `main` commit `0223f2f`, sau [PR #38](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/38). Tên nút/tab theo giao diện PeopleOS sáng; số liệu minh chứng giữ đúng ngày chạy.

Demo nối tiếp thống nhất dùng **10/2026**, theo [báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md): A có lương 26.000.000đ, 26 công chuẩn, một công ngày 09/10/2026 (08:00–17:00), phụ cấp 730.000đ và khấu trừ 1.000.000đ; thực nhận **730.000đ**, B **0đ**. Đã có 20 ca thao tác và ba ca kiểm tra lại sau sửa. Trước demo, kiểm tra fixture hiện có theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md); dùng lại dòng công/khoản, không thêm trùng hoặc xóa nguồn để ép số. Kết thúc bằng mở lại kỳ, xác nhận `CHUA_CHOT` và `NgayChot IS NULL`.

Phương án phân bổ gợi ý: TV1 giữ slide 1–3, nói trong 00:00–02:00 và thao tác GUI tối đa 30 giây trong demo chung. TV1→TV5 nói liên tục 10 phút, sau đó demo nối tiếp 5 phút, tổng 15 phút trình bày; vấn đáp 5–10 phút sau đó. Đây không phải thời lượng cá nhân bắt buộc của rubric.

## 1. Vai trò TV1 và căn cứ trình bày

TV1 phụ trách phân tích hồ sơ nhân sự, ERD/lược đồ quan hệ, chuẩn hóa đến 3NF; triển khai module phòng ban, chức vụ, nhân viên, tích hợp profile tài khoản và benchmark chỉ mục nhân viên. Dùng kế hoạch phân công để nói về ownership; dùng SQL/Java hiện hành và tasklist để nói về cách cài đặt, kết quả.

| Chủ đề | Đối tượng thật | Cách mô tả chính xác |
|---|---|---|
| ERD, PK/FK, 3NF | [`Nhom_06_Phan_Tich_Thiet_Ke_He_Thong.docx`](Nhom_06_Phan_Tich_Thiet_Ke_He_Thong.docx), SQL module 01 | Dùng ERD Word làm điểm xuất phát, rồi đối chiếu schema SQL mới nhất. Hiện có 10 bảng, gồm LICHSULUONG. |
| Thay đổi schema mới | `database/01_Module_NhanSu_TV1.sql`, `database/05_Security_Payroll_TV5.sql` | Bổ sung LICHSULUONG(MaNV, TuThang, LuongCoBan), NHANVIEN.NgayNghiViec và TAIKHOAN.SqlLogin. Không vẽ TuThang hay SqlLogin thành bảng độc lập. |
| Ràng buộc dữ liệu | CHECK/UNIQUE/FK trong module 01 | Lương cơ bản dương; CCCD 12 chữ số; điện thoại 10 chữ số bắt đầu bằng 0; trạng thái có tập giá trị; khóa ngoại phòng ban/chức vụ. |
| Tạo nhân viên và profile | `sp_ThemNhanVien`; `NhanVienDAO.themNhanVien(...)` | Source khai báo đúng method `themNhanVien`. Procedure có transaction; lỗi khi tạo profile sẽ rollback. HR chỉ được tạo profile Employee. |
| Sửa và tra cứu hồ sơ | `NhanVienDAO.update(...)`; `vw_NhanVien_PhongBan_ChucVu`; `IX_NHANVIEN_HoTen` | Update hiện dùng câu UPDATE JDBC trực tiếp. Không có `sp_CapNhatNhanVien`. View nối danh mục; index phục vụ tìm theo họ tên. |
| Bảo vệ xóa hồ sơ | `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong` trong module 05 | Trigger cài ở module 05 khi bảng nghiệp vụ/payroll đã tồn tại; chặn xóa vật lý nếu có phát sinh. Nghỉ việc dùng trạng thái, không xóa cứng. |
| Function ngày công | `fn_TinhSoNgayCong` trong module 02 | TV1 chịu trách nhiệm theo phân công; function được cài sau bảng CHAMCONG. Payroll hiện COUNT trực tiếp trong transaction, không gọi function này. |
| Benchmark | [`test_benchmark_index_TV1.sql`](../database/test_benchmark_index_TV1.sql), T05 trong [`FIX_TASKLIST.md`](FIX_TASKLIST.md) | Bảng tạm 5.000 dòng, logical reads 75→10. Đây là kết quả fixture của truy vấn benchmark, không phải hiệu năng end-to-end. Số 428→4 trong ảnh cũ là lịch sử. |

**Rubric nhóm:** README ghi mục tiêu tối thiểu 8 bảng có quan hệ, chuẩn hóa ít nhất 3NF, 5 constraint, 5 trigger, 5 view, 5 index kèm minh chứng, 5 stored procedure, 5 UDF, 5 nghiệp vụ dùng transaction, minh họa concurrency/recovery và ít nhất 4 role/login với GRANT/REVOKE/DENY. README không cho trọng số điểm; không tự gán điểm hoặc kết luận đạt toàn rubric chỉ dựa trên phần TV1.

**Phân công khác với vị trí cài đặt:** TV1 sở hữu function `fn_TinhSoNgayCong`, nhưng function đặt trong module 02 vì phụ thuộc CHAMCONG. Trigger chặn xóa nhân viên đặt trong module 05 vì cần các bảng đã được tạo. Điều đó không chuyển ownership toàn module 02/05 cho TV1. Payroll hiện đếm trực tiếp ngày công; Java update không dùng stored procedure riêng.

## 2. Ba slide và lời nói mẫu — 2 phút

### Slide 1 — Bài toán và phạm vi thiết kế (00:00–00:35)

**Nội dung:** quản lý nhân sự–tiền lương; luồng từ hồ sơ nhân viên đến chấm công, khoản phát sinh và bảng lương; tên và MSSV TV1.

**Lời nói:**

> “Em là Nguyễn Minh Trí, TV1. Phần em phụ trách là nền dữ liệu nhân sự và thiết kế cơ sở dữ liệu. Hệ thống bắt đầu từ hồ sơ nhân viên, phòng ban, chức vụ; các phân hệ khác dùng mã nhân viên để ghi công, phụ cấp, khấu trừ rồi lập chi tiết lương. Vì vậy em tập trung vào khóa và quan hệ rõ ràng, dữ liệu danh mục không bị lặp trong hồ sơ, và bảo vệ hồ sơ đã phát sinh nghiệp vụ. Em xin trình bày ERD, cách chuẩn hóa và một thao tác hồ sơ.”

### Slide 2 — ERD và 3NF (00:35–01:20)

**Nội dung:** dùng ERD Word làm gốc, đối chiếu SQL hiện hành; làm rõ PHONGBAN/CHUCVU → NHANVIEN, các bảng nghiệp vụ, LICHSULUONG và các cột bổ sung. Đảm bảo sơ đồ không thiếu NHANVIEN.NgayNghiViec, TAIKHOAN.SqlLogin.

**Lời nói:**

> “Trong mô hình hiện tại có 10 bảng. Hai danh mục phòng ban và chức vụ nối đến NHANVIEN bằng khóa ngoại; từ nhân viên phát sinh nhiều dòng chấm công, phụ cấp, khấu trừ và chi tiết lương. TAIKHOAN giữ profile ứng dụng; SQL login được ánh xạ qua cột SqlLogin. LICHSULUONG dùng khóa MaNV cùng TuThang để lưu mức lương theo thời gian, còn NHANVIEN có NgayNghiViec phục vụ nghiệp vụ lịch sử. Để đạt 3NF, em tách tên phòng và chức vụ khỏi hồ sơ; thuộc tính danh mục phụ thuộc vào khóa danh mục, tránh phụ thuộc bắc cầu qua MaNV. TuThang là cột ngày đại diện tháng, không phải bảng riêng.”

### Slide 3 — Ràng buộc, transaction, bằng chứng (01:20–02:00)

**Nội dung:** constraint tiêu biểu; `sp_ThemNhanVien`; view/index; benchmark 75→10 với nhãn “fixture tạm 5.000 dòng”. Ghi ngắn chức năng ngày công/trigger cài ở module phụ thuộc.

**Lời nói:**

> “Ở module nhân sự, SQL Server kiểm tra định danh, trạng thái, khóa và giá trị lương. `sp_ThemNhanVien` gom việc thêm nhân viên và profile Employee vào transaction, tránh dữ liệu dở dang nếu một bước lỗi. Tra cứu dùng view ghép danh mục và chỉ mục họ tên. Benchmark hiện hành dùng bảng tạm 5.000 dòng; logical reads giảm từ 75 xuống 10 trên truy vấn fixture, không phải cam kết tốc độ toàn ứng dụng. Function ngày công của em được cài ở module 02 sau bảng chấm công; trigger chặn xóa nhân viên ở module 05. Payroll hiện COUNT trực tiếp ngày công. Phần thiết kế và hồ sơ nhân sự đã trình bày xong; mời Quân tiếp tục với quy trình chấm công.”

**Căn thời gian:** đọc thử lời mẫu thành tiếng để giữ mốc 2 phút. Nếu dài, rút ví dụ, nhưng giữ ý 3NF và giới hạn benchmark.

**Bố cục slide:** slide 1 thể hiện luồng từ hồ sơ đến lương; slide 2 ưu tiên ERD rõ, tô đậm thực thể TV1; slide 3 gom constraint, transaction, index. Mười bảng hiện hành: PHONGBAN, CHUCVU, NHANVIEN, TAIKHOAN, CHAMCONG, PHUCAPNHANVIEN, KHAUTRUNHANVIEN, BANGLUONG, CHITIETBANGLUONG, LICHSULUONG. Giữ chữ gọn và không trình bày số liệu fixture như kết quả live.

## 3. Demo nối tiếp chung — TV1 thao tác tối đa 30 giây

Sau phần slide 10 phút, TV1 mở đầu trên `gui_hr`, rồi bàn giao TV2; TV3, TV4, TV5 tiếp tục. Mật khẩu tra trong [demo_accounts.json](../database/demo_accounts.json).

1. Mở QA bằng `powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1`; xác nhận đúng database và fixture theo [GUI_TEST_GUIDE](GUI_TEST_GUIDE.md).
2. Vào **Hồ sơ nhân viên** → **Danh sách nhân viên**, chọn `TEST Giao dien A`, mở hồ sơ. Ghi lại địa chỉ hiện tại; giữ email `gui-a@example.invalid` và các trường khác.
3. Trong **Thông tin cá nhân**, đổi riêng địa chỉ thành `TV1 demo 10/10/2026`, bấm **Cập nhật**. Tải lại/mở lại hồ sơ để xác nhận địa chỉ đã lưu. Validator sau PR #38 chấp nhận đuôi miền 2–63 ký tự; ca `.invalid` đã kiểm tra lại thành công ngày 09/10.
4. Khôi phục đúng địa chỉ vừa ghi lại (fixture cuối đợt QA là `GUI QA`), vẫn giữ email; cập nhật và tải lại xác nhận. Không sửa ngày vào làm, lương, CCCD hoặc thêm hồ sơ.
5. Bàn giao TV2 để xem dòng công A đã có ngày 09/10/2026; TV3 xem khoản đã có. Nếu fixture khác minh chứng, báo số thực tế và dừng phần tính/chốt cho đến khi nhóm xác minh; không sửa nguồn để ép 730.000đ.

## 4. Bằng chứng và mở nhanh đúng nguồn

- Rubric: `README.md`, mục **Mục tiêu kỹ thuật theo rubric**.
- Ownership: [`Ke_hoach_phan_cong_Project_DBMS_Nhom06.md`](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md), các hàng Nguyễn Minh Trí ở tuần 1–3.
- ERD/3NF: [`Nhom_06_Phan_Tich_Thiet_Ke_He_Thong.docx`](Nhom_06_Phan_Tich_Thiet_Ke_He_Thong.docx); đối chiếu schema sau cập nhật với module SQL 01 và 05.
- Đối tượng TV1: [`TV1_Tien_Do_Thuc_Hien.md`](TV1_Tien_Do_Thuc_Hien.md), ma trận ownership và tình trạng.
- DDL, constraints, index, view, procedure: [`01_Module_NhanSu_TV1.sql`](../database/01_Module_NhanSu_TV1.sql); tìm `sp_ThemNhanVien`, `vw_NhanVien_PhongBan_ChucVu`, `IX_NHANVIEN_HoTen`.
- Function ngày công: [`02_Module_ChamCong_TV2.sql`](../database/02_Module_ChamCong_TV2.sql); tìm `fn_TinhSoNgayCong`.
- Trigger chặn xóa: [`05_Security_Payroll_TV5.sql`](../database/05_Security_Payroll_TV5.sql); tìm `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong`.
- Benchmark: [`test_benchmark_index_TV1.sql`](../database/test_benchmark_index_TV1.sql) và hàng T05 trong [`FIX_TASKLIST.md`](FIX_TASKLIST.md).
- Fixture/tài khoản và trình tự giao diện: [`DEMO_ACCOUNTS.md`](DEMO_ACCOUNTS.md), [`GUI_TEST_GUIDE.md`](GUI_TEST_GUIDE.md); mật khẩu chỉ đọc từ JSON lúc cần đăng nhập.
- SQL runner tích hợp 08/10 exit 0; tasklist ghi suite TV1, benchmark và actual plan `.sqlplan` trong build.
- Số 75→10 là logical reads của hai truy vấn trên cùng fixture benchmark; không suy ra phần trăm cải thiện cho toàn ứng dụng. Thời gian CPU/elapsed thay đổi theo máy và cache.
- Ảnh cũ với 428→4 là lịch sử, không dùng thay runtime hiện tại.
- [Báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md), G04/G05 và F01: mở đúng hồ sơ A; lần đầu cập nhật bị chặn bởi email `.invalid`, sau sửa cập nhật thành công và SQL xác nhận đã lưu; địa chỉ sau đó được khôi phục về `GUI QA`. Ba ảnh sau sửa được lưu trong Git tại `docs/screenshots/gui-qa-fixes`; log SQL trong `build/` chỉ có trên máy đã chạy.
- Hồi quy sau sửa: **224 assertions PASS** (GuiQa 55, Security 104, LightTheme 45, LoginChip 20), Java 11 biên dịch 54 nguồn. Đây là kiểm tra tự động; chưa kiểm thử thủ công mọi chức năng GUI.
- Nếu GUI không mở được, chuyển tiếp bằng slide; không gọi thao tác chưa chạy là PASS. Chỉ dùng SELECT khi cần kiểm tra fixture; không chạy script ghi dữ liệu để cứu demo.

## 5. Câu hỏi vấn đáp có thể gặp

1. **Vì sao tách PHONGBAN và CHUCVU?** Tên phòng, tên chức vụ và phụ cấp chức vụ phụ thuộc vào khóa danh mục. Tách bảng loại lặp và phụ thuộc bắc cầu.
2. **Vì sao thiết kế đạt 3NF?** Thuộc tính nguyên tử; khóa NHANVIEN đơn nên không có phụ thuộc bộ phận; thuộc tính ngoài khóa phụ thuộc vào khóa của đúng quan hệ, còn dữ liệu danh mục đã tách.
3. **Nếu đổi tên phòng ban thì sửa bao nhiêu hồ sơ?** Sửa một dòng PHONGBAN; NHANVIEN giữ MaPB làm khóa tham chiếu.
4. **Tại sao có LICHSULUONG?** MaNV + TuThang xác định mức lương hiệu lực dùng khi tính lại kỳ lịch sử. Lịch sử trước migration không thể tự phục hồi nếu chưa được lưu.
5. **Transaction của `sp_ThemNhanVien` bảo vệ gì?** Nếu thêm profile lỗi sau khi thêm nhân viên, procedure rollback toàn transaction để không còn hồ sơ dở dang.
6. **HR tạo tài khoản thì đăng nhập được ngay không?** Chưa. HR tạo profile Employee; DBA provision SQL login cá nhân và ánh xạ vào `TAIKHOAN.SqlLogin`.
7. **Có `sp_CapNhatNhanVien` không?** Không. `NhanVienDAO.update(...)` hiện chạy câu UPDATE qua JDBC.
8. **Function ngày công làm gì, payroll có gọi không?** `fn_TinhSoNgayCong(MaNV, Thang, Nam)` đếm các dòng `CO_MAT`, `DI_TRE`, `VE_SOM` của nhân viên trong kỳ, trả 0 khi không có công. TV1 sở hữu theo phân công; cài trong module 02 vì phụ thuộc CHAMCONG. Payroll hiện đếm trực tiếp trong transaction.
9. **Trigger chặn xóa nằm ở đâu?** Module 05 cài `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong` sau khi các bảng liên quan có mặt.
10. **Benchmark chứng minh gì?** Logical reads 75→10 cho truy vấn fixture tạm 5.000 dòng; không đại diện latency end-to-end hay mọi dữ liệu.
11. **Vì sao có INCLUDE trong index?** Các cột hiển thị được đưa vào tầng lá để benchmark có thể được cover; truy vấn tìm chuỗi con vẫn cần xem execution plan cụ thể.

## 6. Checklist trước giờ bảo vệ

- [ ] Slide 1–3 đúng vai trò, tên Nguyễn Minh Trí/MSSV 24110359; tổng lời nói không quá 2 phút.
- [ ] ERD Word đã đối chiếu SQL hiện tại: có LICHSULUONG, NHANVIEN.NgayNghiViec và TAIKHOAN.SqlLogin; không vẽ cột thành bảng riêng.
- [ ] Slide benchmark ghi fixture tạm 5.000 dòng và 75→10 logical reads; bỏ số cũ 428→4 khỏi kết luận hiện hành.
- [ ] QA mở đúng database; đăng nhập gui_hr bằng mật khẩu từ JSON; hồ sơ `TEST Giao dien A` tồn tại.
- [ ] Ghi lại địa chỉ trước demo, cập nhật rồi khôi phục và tải lại xác nhận; giữ email `.invalid`.
- [ ] Kiểm fixture 10/2026: A 730.000đ/B 0đ; TV2/TV3 chỉ xem công/khoản có sẵn.
- [ ] Có slide dự phòng nếu GUI không mở được.
- [ ] Câu chuyển slide: **“Phần thiết kế và hồ sơ nhân sự đã trình bày xong; mời Quân tiếp tục với quy trình chấm công.”**

- [Hướng dẫn hoàn thiện bộ nộp và đầu ra từng TV](HUONG_DAN_HOAN_THIEN_BO_NOP.md)
