# Kiểm thử và demo bằng giao diện Swing

Cập nhật 10/10/2026, đối chiếu source sau PR #38 (`0223f2f`). Demo chung dùng **10/2026** theo [báo cáo GUI 09/10](GUI_TEST_REPORT_20261009.md): 20 ca đã thao tác và ba ca kiểm tra lại sau sửa. Hướng dẫn này dùng cho lượt tiếp theo; chỉ ghi PASS khi đã thực hiện và lưu kết quả.

## 1. Mở QA và kiểm fixture trước demo

Tại thư mục repository:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\start_gui_qa.ps1
```

Launcher chọn `PRJ_Fix_QA_20261007_01`; máy khác dùng `-Database` theo [DEMO_ACCOUNTS](DEMO_ACCOUNTS.md). QA phải có schema và mapping trước khi đăng nhập. Mật khẩu tra `database/demo_accounts.json`, không chép vào slide/ảnh. Không chạy lại setup/seed trên fixture chỉ để tập demo.

Đăng nhập `gui_hr`, kiểm A/B bằng tên/email, không dựa vào mã số trên máy khác. Dữ liệu đã đối chiếu cuối đợt 09/10:

| Chỉ tiêu kỳ 10/2026 | A — `gui-a@example.invalid` | B — `gui-b@example.invalid` |
|---|---:|---:|
| Lương cơ bản áp dụng | 26.000.000 | 26.000.000 |
| Công chuẩn | 26 | 26 |
| Công thực tế | 1, ngày 09/10/2026, 08:00–17:00, `CO_MAT` | 0 |
| Phụ cấp | Một khoản Phụ cấp ăn trưa, 730.000 | 0 |
| Khấu trừ | Một khoản Tạm ứng lương, 1.000.000 | 0 |
| Tiền công | 1.000.000 | 0 |
| Thực nhận | **730.000** | **0** |

Kỳ cuối phiên là `CHUA_CHOT`, `NgayChot IS NULL`; hồ sơ A giữ email và địa chỉ `GUI QA`. Xem nhật ký/tổng hợp công, hai tab khoản và chi tiết lương để kiểm lại. Nếu nguồn khác bảng trên, xác minh và ghi số thực tế trước khi tính/chốt; không thêm trùng, xóa nguồn hoặc đổi lương để ép expected. Nếu kỳ đã chốt từ lượt tập, Payroll mở lại trước khi bắt đầu. Có thể dùng truy vấn SELECT dự phòng trong hướng dẫn [TV2](TV2_Huong_Dan_Thuyet_Trinh.md), [TV3](TV3_Huong_Dan_Thuyet_Trinh.md), [TV4](TV4_Huong_Dan_Thuyet_Trinh.md), trên đúng QA.

## 2. TV1 — hồ sơ nhân viên

1. `gui_hr` mở **Hồ sơ nhân viên**, chọn A và ghi lại địa chỉ hiện tại.
2. Đổi riêng địa chỉ thành `TV1 demo 10/10/2026`, giữ email `gui-a@example.invalid`, ngày vào làm, lương và các trường khác; bấm **Cập nhật**.
3. Tải lại/mở lại hồ sơ để xác nhận địa chỉ đã lưu. Validator đuôi miền 2–63 ký tự đã kiểm tra lại sau PR #38.
4. Khôi phục đúng địa chỉ đã ghi lại, vẫn giữ email; cập nhật và tải lại xác nhận. Không thêm nhân viên A/B hoặc nhập lại CCCD/email.

## 3. TV2 — công đã có và bài lỗi trùng ngày

1. `gui_hr` mở **Nhật ký chấm công**, lọc **10/2026**, xem A ngày `2026-10-09`, 08:00–17:00, `CO_MAT` đã có. Không bấm ghi mới trong demo chính.
2. Mở **Tổng hợp theo tháng**: A có 1 ngày làm/9 giờ, trễ/sớm/vắng 0; B không có công.
3. Nếu trình diễn **lỗi trùng ngày** riêng, kỳ phải đang `CHUA_CHOT`: gửi lại đúng A–09/10/2026, kỳ vọng bị từ chối và dòng gốc không đổi. Không gọi đây là bước ghi mới thành công hoặc đổi ngày để lách lỗi.

Nhập lô/rollback có log SQL và source service để giải thích; chưa kiểm tra nhập lô GUI trong đợt 09/10.

## 4. TV3 — khoản đã có

`gui_hr` mở **Phụ cấp & Khấu trừ**, chọn **10/2026**, xem Phụ cấp ăn trưa A 730.000đ và Tạm ứng lương A 1.000.000đ; tải lại/tổng hợp để đối chiếu. B không có khoản. Không bấm Thêm/Xóa trong lượt demo chính: mỗi lần Thêm hợp lệ có thể tạo thêm dòng và đổi tổng. Nếu thiếu/khác fixture, xác minh trước buổi diễn, không xóa hay ghi nguồn để ép kết quả.

## 5. TV4 — tính lương

Đăng xuất HR, vào `gui_payroll`, mở **Bảng lương**, chọn **10/2026**, **26** công chuẩn, bấm **Tính / cập nhật lương**, xác nhận một lần và chờ hoàn tất.

`26.000.000 / 26 × 1 + 730.000 − 1.000.000 = 730.000đ` cho A; B 0 công/0 khoản nên 0đ. Fixture đã kiểm có hai nhân viên và tổng 730.000đ. Nếu hiện có thêm nhân viên, tổng công ty có thể khác. Kiểm chi tiết từng người và một kỳ/một chi tiết mỗi nhân viên. Có thể tính lại kỳ nháp một lần; không gửi lặp khi đang chạy, không xóa kỳ để ép số.

## 6. TV5 — chốt, quyền và mở lại

1. `gui_payroll` mở **Báo cáo & Phiếu lương**, chọn **10/2026**, bấm **Chốt bảng lương**, xác nhận. Kỳ thành `DA_CHOT`, có ngày chốt, số tiền giữ nguyên.
2. Nếu minh họa khóa nguồn, `gui_hr` thử ghi A ngày **2026-10-10** (xác nhận ngày chưa có, kỳ đang chốt), 08:00–17:00. Phải bị từ chối bởi khóa kỳ và không phát sinh công mới. Hoặc dùng log/ảnh G15 đã PASS; không xóa dòng nguồn để tạo bài lỗi.
3. Đăng nhập `gui_a`, **Phiếu lương của tôi** chỉ hiện A 730.000đ. Đăng nhập `gui_b`, chỉ hiện B 0đ. Kiểm Employee không có màn hình quản lý hoặc lương người khác.
4. **Trước khi kết thúc**, Payroll mở lại 10/2026, tải lại xác nhận `CHUA_CHOT`, `NgayChot IS NULL`; A/B và công/khoản giữ nguyên. Lượt tập tiếp theo dùng lại fixture đó.

| Login | Điều cần xác minh |
|---|---|
| `gui_admin` | Có quản trị tài khoản và nghiệp vụ; quyền database không tự cấp quyền server |
| `gui_hr` | Sửa hồ sơ/công/khoản; không tính/chốt/mở lại/xóa kỳ hoặc cấp profile quản trị |
| `gui_payroll` | Đọc hồ sơ/công, quản lý khoản, tính/chốt/mở lại; không sửa hồ sơ/công |
| `gui_a`, `gui_b` | Chỉ đọc phiếu cá nhân theo danh tính SQL |

Sai mật khẩu phải báo câu ngắn, xóa mật khẩu và bật lại nút nhập. Reset/khóa SQL login cần quyền server bổ sung theo [SECURE_SETUP](SECURE_SETUP.md); không đổi mật khẩu/quyền giữa demo. Thử chuyển kỳ nhanh chỉ dùng kỳ đã có, không tạo thêm kỳ để kiểm dropdown. Các bài Admin, mất kết nối, nhập lô và nhiều phiên GUI chưa có minh chứng thủ công đầy đủ trong đợt này.

## 7. Ghi nhận và bàn giao

- [ ] Ghi database, phiên bản source, username/vai trò, kỳ, thao tác, expected/actual và ảnh/log cho lượt mới; không ghi mật khẩu.
- [ ] Kiểm A/B và nguồn trước demo; không thêm trùng hoặc reset fixture.
- [ ] TV1 khôi phục địa chỉ và giữ email; bài trùng ngày TV2 được gọi rõ là ca lỗi.
- [ ] Đối chiếu A 730.000đ/B 0đ; kết thúc 10/2026 `CHUA_CHOT`, `NgayChot IS NULL`, nguồn không đổi.
- [ ] Chỉ đánh dấu PASS cho bước đã thao tác. Dùng [FIX_TASKLIST](FIX_TASKLIST.md) cho test tự động và [GUI_TEST_REPORT_20261009](GUI_TEST_REPORT_20261009.md) cho thao tác GUI đã ghi nhận.
- [ ] Đóng gói minh chứng theo [HUONG_DAN_HOAN_THIEN_BO_NOP](HUONG_DAN_HOAN_THIEN_BO_NOP.md); log trong `build/` bị Git bỏ qua.
