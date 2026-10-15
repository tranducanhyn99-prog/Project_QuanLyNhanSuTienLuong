# TV5 hiệu đính tài liệu bàn giao TV3

Trần Đức Anh (TV5), 15/10/2026. Nhận bộ bàn giao Trần Tiến Đạt từ [PR #43](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/43), head `72fbd952264870ee5654387899b5133a7384f2ee`, đã merge vào `main` tại `665113c`. Bản gốc truy lại được trong commit đó.

## Hai chỗ hiệu đính trong Word

Trong [báo cáo TV3](Minh_chung/TV3_Phan_Bao_Cao.docx), chỉ hai đoạn ở trang 4 được sửa:

- Đường dẫn ảnh `Ảnh_Chụp/` đổi thành `Minh_chung/Screenshots/`, khớp thư mục thực có và danh mục ở trang 5. Đường dẫn này tính từ thư mục bàn giao `Bo_nop_TV3_TranTienDat/`.
- Phần môi trường ghi rõ `b2e046c` là base của PR #43; chưa xác minh commit SQL đã chạy. Log module ghi thời gian hoàn tất nhưng không ghi source commit; build SQL Server được đọc từ actual plan. Không dùng base PR thay cho bằng chứng phiên bản DDL trên database của lượt chạy.

SHA-256 của Word trước/sau hiệu đính:

| Bản | SHA-256 |
|---|---|
| Gốc trong commit `665113c` | `b4e06195ddc087a7623fde3eaed2e34e2efebc51ef3766c39f1b1844eb457ed1` |
| TV5 hiệu đính | `7a81ed6ffc2d494962c1269556f2ed7e029690b019179a055b106fd6b807d127` |

## Kiểm chứng

Đối chiếu OOXML chỉ thấy `word/document.xml` thay đổi, đúng hai đoạn; các phần khác trong DOCX giữ nguyên. Đã xuất bản trước/sau bằng Microsoft Word: đều 5 trang, chỉ trang 4 đổi; bốn trang còn lại trùng pixel. Đã xem trang 4, bảng không tràn hoặc mất nội dung.

12 file còn lại (hai log, một actual plan, tám ảnh và PowerPoint) khớp nguyên byte với commit `665113c`. Quy tắc `.gitattributes` cho thư mục TV3 giữ nguyên byte khi checkout, tương tự bộ TV4; hai log trên Windows được trả về đúng dấu xuống dòng trong Git. Không thay nội dung hoặc số đo của bằng chứng gốc.

Lượt này chỉ hiệu đính bản bàn giao riêng. Thư mục `Bo_nop_Nhom06/evidence/TV3/` vẫn cần bước tích hợp vào bộ nộp nhóm; vì chưa có manifest nào bao phủ Word bàn giao riêng nên không cập nhật manifest nhóm. Không chạy lại SQL/GUI hoặc xác nhận rehearsal trong lượt sửa tài liệu này.
