# TV5 — kịch bản tập demo và phiếu ghi kết quả

**Trần Đức Anh · 24110155 · Nhóm 06**

Tài liệu chuẩn bị ngày 11/10/2026. Chưa ghi nhận lượt tập mới. Kết quả lịch sử và kết quả lượt tập phải ghi riêng. Phần thuyết trình TV5 gồm đúng [3 slide 13–15](slides/TV5_Slide_13-15.pptx), khoảng 08:00–10:00, để ghép vào deck nhóm tối đa 15 slide. Notes trong PPTX chứa lời nói và nguồn minh chứng. Đây chưa phải deck toàn nhóm. Cài đặt và đường dẫn launcher trong package theo [README bộ nộp](README_BO_NOP.md).

## Điều kiện trước khi tập

- [ ] SQL Server hoạt động, kết nối được đúng QA, schema 01–05 khớp source. Kiểm tra chỉ đọc của phiên đóng gói 11/10 cho thấy `MSSQLSERVER` đang dừng và localhost:1433 chưa mở. Phải xử lý điều kiện này trước khi thao tác demo.
- [ ] Mở ứng dụng bằng launcher QA, xác nhận tên database QA đã chuẩn bị. Không chạy installer/seed lên database nguồn hoặc tự tạo lại login để sửa demo.
- [ ] `gui_hr`, `gui_payroll`, `gui_a`, `gui_b` đã provision login/user/role và mapping profile. Lấy mật khẩu tham chiếu từ [database/demo_accounts.json](database/demo_accounts.json), đối chiếu tài khoản thực trên QA; không đưa mật khẩu lên slide, notes hoặc phiếu này.
- [ ] Kiểm fixture 10/2026 theo `docs/GUI_TEST_GUIDE.md`: A (fixture lịch sử MaNV 1008) có lương cơ bản 26.000.000đ, 26 công chuẩn, một công 09/10/2026 08:00–17:00, phụ cấp 730.000đ, khấu trừ 1.000.000đ. B (1009) không có công/khoản. Xác minh mapping thực tế, không mặc định ID trên QA khác.
- [ ] TV4 đã tính kỳ 10/2026: A thực nhận 730.000đ, B 0đ. Kỳ ban đầu `CHUA_CHOT`, `NgayChot IS NULL`. Dùng lại dòng nguồn hiện có, không thêm trùng hoặc xóa khoản để ép số.
- [ ] Ngày 10/10/2026 của A chưa có công, để bài lỗi HR thử thêm chứng minh khóa kỳ thay vì lỗi trùng ngày. Nếu ngày này đã có dòng, chọn ngày khác chưa tồn tại trong tháng 10 và ghi lại.
- [ ] Chuẩn bị mở log gốc quyền, identity, concurrency và báo cáo GUI trong package. Các ảnh/SQL log cũ phải giữ ngày và phạm vi của lượt gốc.

## Mạch demo cả nhóm — dự kiến 5 phút

| Thành viên | Thao tác chính | Kết quả cần thấy |
|---|---|---|
| TV1 / HR | Sửa địa chỉ A rồi khôi phục, giữ email `.invalid` | Hồ sơ cập nhật được và về giá trị ban đầu |
| TV2 / HR | Đọc công tháng 10 hiện có | A một công; không ghi trùng để tạo fixture |
| TV3 / HR | Đọc phụ cấp/khấu trừ A hiện có | 730.000đ / 1.000.000đ |
| TV4 / Payroll | Tính hoặc cập nhật lương 10/2026, 26 công chuẩn | A 730.000đ, B 0đ |
| TV5 | Chốt, thử sửa nguồn, đọc phiếu A/B, mở lại | Quyền theo login và trạng thái cuối đúng |

## Phần TV5 — dự kiến 120 giây

| Thời gian | Thao tác | Kết quả mong đợi |
|---|---|---|
| 0–25s | `gui_payroll`, **Báo cáo & Phiếu lương**, kỳ **10/2026**, **Chốt bảng lương**, xác nhận | Hiển thị **Đã chốt** (`DA_CHOT`), có `NgayChot`, A/B giữ 730.000đ/0đ |
| 25–65s | Đổi `gui_hr`, **Nhật ký chấm công**, thử thêm A ngày **10/10/2026**, **Có mặt**, 08:00–17:00 | Báo lỗi kỳ đã chốt; không phát sinh dòng công. Ghi nguyên văn lỗi nếu khác kỳ vọng |
| 65–95s | Đổi `gui_a`, **Phiếu lương của tôi**, kỳ 10/2026 | Chỉ phiếu A, 730.000đ; không thấy B hoặc báo cáo toàn công ty |
| 95–120s | Đổi `gui_b`, xem phiếu cùng kỳ. Sau đối chiếu, đổi Payroll và **Mở lại bảng lương** | B chỉ phiếu 0đ. Kỳ cuối **Bản nháp** (`CHUA_CHOT`), `NgayChot IS NULL`, nguồn không đổi |

Lời nói gợi ý: “Payroll chốt kỳ đã tính. HR không thể ghi công vào kỳ chốt. Employee truy vấn view theo login thật nên A và B chỉ thấy phiếu của mình. Cuối lượt, Payroll mở lại để nhóm dùng tiếp fixture.” Chỉ đọc câu tương ứng với bước thực sự làm được.

Chốt lại không phải tính lương. `sp_ChotBangLuong` của TV5 dùng `UPDLOCK, HOLDLOCK`, kiểm kỳ tồn tại/chưa chốt/có chi tiết trong transaction và rollback khi lỗi. `sp_TinhBangLuongThang` và `sp_HuyChotBangLuong` thuộc TV4. Trigger bảo vệ chi tiết và nguồn kiểm cả `inserted`/`deleted`, bao gồm thêm/sửa/xóa/chuyển kỳ. Quyền `GRANT EXECUTE` cho SP, `REVOKE` view tổng khỏi Employee, `DENY` bảng gốc và `GRANT SELECT` view cá nhân nằm trong module 05.

## Dự phòng và phục hồi cuối lượt

- **SQL/service chưa sẵn sàng:** dừng thao tác ghi, trình bày source và log lịch sử có ngày. Ghi “chưa chạy” trong phiếu; không gọi đây là demo trực tiếp PASS.
- **A/B không ra số dự kiến:** kiểm lại đúng QA, kỳ, mapping và nguồn. Không thêm/xóa dữ liệu để ép 730.000đ. Ghi số thật, tạm dừng luồng chốt khi chưa tìm nguyên nhân.
- **Đổi tài khoản chậm:** ưu tiên chốt + chặn nguồn + Employee A. Ghi B chưa làm nếu thiếu thời gian. Dành thời gian sau phần diễn để Payroll mở lại; không bỏ phục hồi.
- **GUI lỗi nhưng SQL kết nối được:** SSMS kết nối thật lần lượt bằng `gui_a` và `gui_b` để đọc đoạn SQL dưới đây. Phiên DBA có `WHERE MaNV=A` không chứng minh quyền Employee. Phân biệt dự phòng SQL với thao tác GUI.
- **Bài lỗi ghi nguồn lại thành công:** ghi FAIL, kiểm dòng vừa tạo và trạng thái kỳ, dừng lượt. Người quản lý QA xử lý đúng dòng phát sinh sau khi xác minh; không xóa rộng dữ liệu nguồn. Không tuyên bố nguồn không đổi khi chưa đối chiếu.
- **Mở lại không thành công:** ghi trạng thái còn `DA_CHOT`, bàn giao tên QA/kỳ/lỗi cho Payroll hoặc DBA. Lượt chưa hoàn tất phục hồi. Không chạy test concurrency tạo fixture vào kỳ demo chính.

SQL đọc dự phòng Employee, dùng connection thật và đúng QA:

```sql
SELECT DB_NAME() AS DatabaseDangKetNoi,
       ORIGINAL_LOGIN() AS SqlLoginDangKetNoi,
       USER_NAME() AS DatabaseUser,
       IS_ROLEMEMBER('role_Employee') AS LaEmployee;

SELECT MaNV, HoTen, Thang, Nam, TrangThaiBangLuong, ThucNhan
FROM dbo.vw_PhieuLuongCaNhan
WHERE Thang = 10 AND Nam = 2026;
```

Employee có thể chạy riêng `SELECT * FROM dbo.vw_BangLuongChiTiet;` khi cần minh họa permission denied. Không coi việc client tự lọc `MaNV` là bảo vệ danh tính.

Sau khi Payroll mở lại, tải lại UI. Người có quyền đọc QA xác nhận chỉ đọc:

```sql
SELECT MaBangLuong, Thang, Nam, TrangThai, NgayChot
FROM dbo.BANGLUONG
WHERE Thang = 10 AND Nam = 2026;

SELECT MaNV, ThucNhan
FROM dbo.vw_BangLuongChiTiet
WHERE Thang = 10 AND Nam = 2026;
```

Đối chiếu số dòng công/khoản trước và sau, nhất là bài lỗi HR. Không chỉ nhìn trạng thái nút để kết luận dữ liệu đã phục hồi.

## Bằng chứng lịch sử để trả lời vấn đáp

| Nguồn gốc | Kết quả có thể nói | Phạm vi / giới hạn |
|---|---|---|
| `Concurrency_A.log`, `Concurrency_B.log`, timestamp trong log **07/10/2026 22:33** | B chờ **20631 ms** (20,631 giây), cùng một header, không trùng chi tiết, cleanup PASS | A chủ động giữ transaction khoảng 20s. Đây là case blocking SQL, không phải tốc độ truy vấn bình thường hoặc nhiều phiên GUI |
| `Closed_Source_Concurrency.log`, bộ SQL **08/10/2026** | Bốn race công/phụ cấp/khấu trừ/chi tiết PASS, bị từ chối, nguồn không đổi, cleanup PASS | Case và fixture đã chạy, không bao quát mọi workload hoặc backup/restore |
| `test_security_roles_TV5.log`, bộ SQL **08/10/2026** | 22 permission checks, 3 actual denied operations, cleanup PASS | Kiểm quyền object; `EXECUTE AS USER` không tự chứng minh lọc `ORIGINAL_LOGIN()` |
| `identity.log`, file local **07/10/2026**, và G17/G18 GUI **09/10** | Login thật A/B, view cá nhân, chặn view tổng và forged Session | Identity log 41 Java/business checks là bộ riêng; không cộng vào 224 assertions |
| `docs/GUI_TEST_REPORT_20261009.md` | 20 ca thao tác và 3 kiểm tra lại sau sửa; fixture A 730.000đ/B 0đ; cuối kỳ mở lại | Có ba lỗi ban đầu, không phải 20/20 PASS từ đầu. Chưa thử Admin, tắt SQL thật hay nhiều phiên GUI trong đợt này |
| Java hồi quy **09/10/2026** | GuiQa 55 + Security 104 + LightTheme 45 + LoginChip 20 = **224 assertions PASS** | LoginChip frame ẩn, 0 SQL connections; timeout dùng exception giả lập |

Đường dẫn evidence cuối cùng theo `evidence/TV5/` và manifest package. Nếu file gốc thiếu, ghi thiếu; không thay log bằng số nhớ lại. Nguồn nội dung: `docs/TV5_Huong_Dan_Thuyet_Trinh.md`, `docs/HUONG_DAN_HOAN_THIEN_BO_NOP.md`, module 05 và `AuthService.java`. Hướng dẫn nguồn có thể đọc từ bản repo, không phải evidence của một lượt tập mới.

## Phiếu ghi lượt tập mới — để trống đến khi chạy thật

Ngày/giờ: ____________________  Người thao tác: ____________________

Source commit: ____________________  SQL Server / QA: ____________________

Fixture trước lượt (kỳ/trạng thái/A/B/số dòng nguồn): ____________________

| Bước | Kết quả thực tế | PASS / FAIL / chưa chạy | File minh chứng |
|---|---|---|---|
| Kiểm dịch vụ, login/mapping, fixture | | | |
| Payroll tính và đối chiếu A/B | | | |
| Payroll chốt, ngày chốt | | | |
| HR bị chặn ghi nguồn, không thêm dòng | | | |
| Employee A chỉ thấy phiếu A | | | |
| Employee B chỉ thấy phiếu B | | | |
| Payroll mở lại: CHUA_CHOT, NgayChot NULL | | | |
| Số tiền và nguồn cuối lượt giữ nguyên | | | |

Tổng thời gian: __________  Bước chưa làm / lỗi / xử lý: ____________________

Người xác nhận phục hồi: ____________________  Thời điểm: ____________________
