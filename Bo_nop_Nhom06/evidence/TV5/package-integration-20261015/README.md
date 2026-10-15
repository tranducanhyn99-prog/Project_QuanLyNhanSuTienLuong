# TV5 kiểm package và tích hợp TV3 ngày 15 tháng 10

Thực hiện trên Windows hiện tại, JDK 21.0.12 với target Java 11, từ source package sao sang thư mục QA riêng không có Git. SQL/Java không đổi trong lượt tích hợp. [verification.json](verification.json) ghi snapshot `a98371d`, hash file và phạm vi; đường dẫn gốc trong JSON là metadata lịch sử, không phải dependency để mở package.

| Kiểm tra | Kết quả | File |
|---|---|---|
| Biên dịch | 54 source main/test, `javac --release 11`, thành công | Thông tin runner trong [verification.json](verification.json) |
| Security | 104 assertions, offline | [Java_Security.log](Java_Security.log) |
| GUI QA | 88 assertions, không kết nối SQL | [Java_GuiQa.log](Java_GuiQa.log) |
| Theme | 45 assertions, không cần database/display | [Java_LightTheme.log](Java_LightTheme.log) |
| Login chip | 20 assertions, frame ẩn, không kết nối SQL | [Java_LoginChip.log](Java_LoginChip.log) |
| Deck | 15 slide, bốn bảng native, không finding cấu trúc/layout/font; import đạt | [deck-validation.json](deck-validation.json) |

Tổng bốn suite là 257 assertions, không cộng thành kiểm thử SQL hoặc live GUI. 81 file source/resource/JDBC/database được kiểm không đổi so với package trước tích hợp. Bản sao độc lập chỉ chứng minh build/offline trên cùng máy; không thay kiểm cài SQL/provisioning/GUI trên máy nhận khác.

Đã xuất Word trước/sau bằng Microsoft Word: đều 61 trang vật lý, nội dung vẫn trang 5–59 (55 trang). Sáu trang đổi là 2, 53, 55, 58, 60, 61; đã xem từng trang, bảng/text không tràn hoặc mất nội dung. 55 trang còn lại trùng pixel. Hai bản Word repo/package đồng nhất byte. PDF/ảnh QA lưu trong `build/tv5-integrate-tv3/` của máy kiểm, chưa dùng làm PDF nộp cuối.

Đã mở và xuất cả hai deck bằng PowerPoint: 15/15 slide trước/sau trùng pixel. Trong PPTX chỉ ba notes parts 7–9 thay đổi; các phần slide, bảng, hình, theme, font và relationship giữ nguyên. Notes dùng bản Đạt bàn giao và đường dẫn package; preview nhóm hiện có vẫn đúng.

Chưa kiểm Windows DPI thật, máy Windows khác, chạy SQL/GUI live mới hoặc rehearsal tay. TV1 còn chờ ERD/log/plan cuối và xác nhận slide 1–3; TV2 còn xác nhận điểm TRY…CATCH trong kế hoạch. Không đánh dấu bộ nộp toàn nhóm đã hoàn tất.

Kiểm package: inventory 511/511 mục và manifest 537/537 mục đúng size/hash, bao phủ 538 file (manifest không tự hash); 273 liên kết nội bộ hợp lệ. 396 file evidence TV2/TV4/TV5 đã có trước lượt này giữ nguyên hash. Marker chờ TV3 đã được thay bằng biên nhận; TV1 vẫn giữ marker.
