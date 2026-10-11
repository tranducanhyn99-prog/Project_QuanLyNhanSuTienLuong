# Đánh giá hiện trạng dự án — 11/10/2026

Phạm vi: source `main f2a41f7` (logic SQL/Java sau PR #38 ở `0223f2f`), tasklist đã xử lý, rubric README, báo cáo GUI và bộ TV5 vừa đóng gói. Đọc lại luồng AuthService/DatabaseConnection, đối chiếu tài liệu quyền/chốt với SQL và kiểm package. Đây không phải lần audit độc lập mọi dòng source hoặc một lần chạy SQL/GUI mới.

## Kết luận

Dự án có đủ nền tảng để tiếp tục tập bảo vệ: schema và các module tích hợp, danh tính SQL cá nhân, quyền role, transaction/trigger bảo vệ kỳ lương, giao diện Swing và bộ hồi quy. Bộ TV5 có thể commit. Bộ nộp toàn nhóm chưa sẵn sàng nghiệm thu vì còn thiếu một số đầu ra bàn giao và kiểm trên môi trường mới.

## Những phần có căn cứ kiểm chứng

- Source trong package đã biên dịch 54 file Java với `--release 11`; bốn suite offline đạt 224 assertions (104 + 45 + 20 + 55). Các log chạy ngày 11/10 được lưu trong package; chúng không thay live SQL/GUI.
- Log SQL/identity/concurrency đã chọn là kết quả 07–08/10: quyền object 22 checks và ba lệnh bị từ chối; identity 50 checks; hai phiên tính cùng kỳ không tạo trùng; bốn race chốt/nguồn bị từ chối và cleanup PASS. Giới hạn từng fixture/ngày/source ghi trong [README evidence](../Bo_nop_Nhom06/evidence/TV5/README_EVIDENCE.md).
- Chuỗi đăng nhập dùng SQL login cá nhân, profile theo `ORIGINAL_LOGIN()`, kiểm hash/trạng thái rồi tạo Session. Sai danh tính hoặc lỗi đăng nhập xóa Session/credentials; không dùng tài khoản `sa` chung để chạy ứng dụng.
- Word có 55 trang nội dung/61 trang tổng theo bản render trước; phần TV5 và số đo đã đối chiếu. Ba slide TV5 có notes và preview đã xem; chưa phải deck nhóm đầy đủ.
- Package có source, JDBC, SQL, launcher, hướng dẫn, Word, slide và evidence. Hash file gốc và liên kết nội bộ được kiểm; việc biên dịch trên máy hiện tại không chứng minh cài được trên máy khác.

## Việc cần hoàn tất trước khi nộp

| Mức cần thiết | Việc / người phụ trách | Hệ quả nếu bỏ qua |
|---|---|---|
| Bắt buộc | TV2 đo benchmark CHAMCONG hiện hành: Messages, Results, bốn actual plans và summary | Số 574 → 5 chỉ là lịch sử; chưa đủ bằng chứng hiện hành cho phần index TV2 |
| Bắt buộc | TV1 chốt ERD đủ 10 bảng, gồm LICHSULUONG; TV1–TV4 rà chương và chọn log/plan bàn giao | Ảnh ERD cũ thiếu bảng và evidence nhóm vẫn chưa đầy đủ |
| Bắt buộc | Nhận slide 1–12, ghép với 13–15 của TV5, kiểm deck ≤15 slide | Ba slide TV5 không đủ sản phẩm thuyết trình toàn nhóm |
| Bắt buộc | Cài package trên máy khác; kiểm login/mapping và fixture 10/2026; tập demo thật, mở lại kỳ cuối lượt | Fresh QA chưa tự có A 730.000đ/B 0đ; demo có thể không chạy dù regression offline PASS |
| Trước nộp | Kiểm mục lục/pagination trên máy nộp và xuất PDF từ Word cuối nếu yêu cầu | Render cũ không thay việc kiểm bản cuối trên máy trình bày |

SQL Server local được kiểm chỉ đọc ngày 11/10 đang dừng; chưa chạy live trong lượt đánh giá. Các ca GUI chưa thử (Admin, mất kết nối SQL thật, nhiều phiên/DPI...) được ghi trong [GUI_TEST_REPORT_20261009](GUI_TEST_REPORT_20261009.md); không suy rộng kết quả đã có thành mọi ca đều PASS. Không tự sửa công thức/source hoặc đổi số đo để bù minh chứng còn thiếu.

## Phần lưu vào Git

- Bộ `Bo_nop_Nhom06/`: source snapshot có provenance, hướng dẫn, Word, slide, demo, evidence gốc và manifest. Đây là snapshot bàn giao tại mốc trên; khi source đổi phải cập nhật package và manifest, không sửa hai bản Java/SQL độc lập.
- Hai guide TV5/checklist và bản đánh giá này.
- Ngoại lệ gitignore chỉ cho log đã chọn trong evidence TV5; không thêm toàn bộ `build/`. Git attributes giữ byte package để hash còn đúng sau checkout. Timestamp gốc nằm trong inventory vì Git không lưu LastWriteTime.
- ZIP local là bản sao tiện chuyển máy, không commit. Không đưa class/build sinh ra, cấu hình local hoặc credential DBA/Git vào Git. Mật khẩu fixture môn học vẫn được giữ theo lựa chọn của nhóm.

Theo dõi bàn giao tại [HUONG_DAN_HOAN_THIEN_BO_NOP](HUONG_DAN_HOAN_THIEN_BO_NOP.md). Chưa kiểm cài mới/SQL/GUI/rehearsal; đây là giới hạn cần xử lý trước khi xác nhận bộ nộp hoàn tất.
