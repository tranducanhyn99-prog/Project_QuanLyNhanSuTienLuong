# Deck chung và slide bàn giao

[Nhom06_ThuyetTrinh_15_Slides.pptx](Nhom06_ThuyetTrinh_15_Slides.pptx) là deck chung chính thức của Nhóm 06, được nâng cấp hoàn chỉnh thành cấu trúc **18 slide** chuẩn mực (giữ nguyên tên file để bảo đảm tương thích manifest nộp bài và các liên kết tài liệu). Deck kết hợp bố cục 2 cột trực quan (thẻ kỹ thuật và ảnh minh chứng UI/ERD/benchmark độ phân giải cao), chuẩn hóa đồng bộ speaker notes cho toàn bộ các slide.

| Slide | Người trình bày | Phân hệ & Nội dung chi tiết | Minh chứng trực quan (Right Column) |
|---|---|---|---|
| 1 | Toàn nhóm (TV1 đại diện) | **Trang bìa chính thức**: Trường ĐH SPKT TP.HCM, Đề tài PeopleOS, GVHD TS. Phan Thị Thể, 5 thành viên & phân công | Logo HCMUTE chính thức |
| 2 | Toàn nhóm (TV1 đại diện) | **Tổng quan Đề tài & Kiến trúc Hệ thống**: Mục tiêu bài toán, Kiến trúc 3 tầng (Swing - Service - SQL Server 10 bảng) | Dashboard Tổng quan PeopleOS |
| 3 | Nguyễn Minh Trí — 24110359 | **TV1.1: Mô hình Dữ liệu 10 Bảng & Chuẩn hóa 3NF**: Khử phụ thuộc hàm, chuẩn hóa 3NF, snapshot lịch sử | Sơ đồ ERD 10 bảng quan hệ |
| 4 | Nguyễn Minh Trí — 24110359 | **TV1.2: Ràng buộc Toàn vẹn & Transaction Hồ sơ**: CHECK tuổi/CCCD/SĐT, SP phân tầng, TRY...CATCH rollback | Giao diện Quản lý Hồ sơ Nhân sự |
| 5 | Nguyễn Minh Trí — 24110359 | **TV1.3: Tối ưu hóa Truy vấn & Nonclustered Index**: Đo lường STATISTICS IO/TIME, Index tìm kiếm nhân sự | Kế hoạch thực thi Execution Plan TV1 |
| 6 | Phạm Minh Quân — 24110311 | **TV2.1: Phân hệ Chấm công theo ngày & Ràng buộc**: Toàn vẹn giờ làm, ca kíp, kiểm tra trùng lặp | Giao diện Bảng Chấm công Chi tiết |
| 7 | Phạm Minh Quân — 24110311 | **TV2.2: Xử lý lô Batch JDBC & Tổng hợp tháng**: Quy trình tổng hợp công tháng, Batch processing hiệu năng cao | Giao diện Tổng hợp Công tháng |
| 8 | Phạm Minh Quân — 24110311 | **TV2.3: Tối ưu hóa Truy vấn Chấm công & Covering Index**: IX_CHAMCONG_Ngay_MaNV, loại bỏ Key Lookup | Kế hoạch thực thi Execution Plan TV2 |
| 9 | Trần Tiến Đạt — 24110198 | **TV3.1: Phân hệ Phụ cấp & Khấu trừ theo chức vụ/nhân viên**: Quản lý phụ cấp chức vụ, khấu trừ cá nhân | Giao diện Phụ cấp & Khấu trừ |
| 10 | Trần Tiến Đạt — 24110198 | **TV3.2: Nghiệp vụ Xóa kỳ nháp & Trigger Khóa kỳ đã chốt**: Quy trình xóa an toàn, Trigger chặn can thiệp | Minh chứng Trigger Khóa kỳ đã chốt |
| 11 | Trần Tiến Đạt — 24110198 | **TV3.3: Tối ưu hóa Truy vấn Phụ cấp & Composite Index**: IX_PHUCAPNHANVIEN composite index, giảm logical reads | Thống kê STATISTICS IO TV3 |
| 12 | Nguyễn Quang Vinh — 24110385 | **TV4.1: Phân hệ Tính toán Tiền lương & Công thức**: Công thức lương gộp, khấu trừ, thực lĩnh, UDF tính toán | Giao diện Bảng lương & Tính lương |
| 13 | Nguyễn Quang Vinh — 24110385 | **TV4.2: Snapshot Lịch sử lương & Transaction với Savepoint**: sp_TinhLuongThang, Savepoint xử lý lỗi từng phần | Hộp thoại Điều chỉnh Lương & Công |
| 14 | Nguyễn Quang Vinh — 24110385 | **TV4.3: Kiểm thử tự động E2E Payroll & Toàn vẹn**: Kịch bản kiểm thử E2E 10 bước, đối soát số liệu | Kết quả Kiểm thử E2E 10/10 PASS |
| 15 | Trần Đức Anh — 24110155 | **TV5.1: Kiến trúc 3 tầng, Giao diện PeopleOS & Bảo mật 4 Roles**: Phân quyền Admin/HR/Payroll/Employee, PBKDF2 | Giao diện Quản trị & Phân quyền |
| 16 | Trần Đức Anh — 24110155 | **TV5.2: Quy trình Chốt kỳ lương & Giao dịch Reopen có điều kiện**: sp_ChotLuongThang, sp_MoLaiKyLuong an toàn | Giao diện Báo cáo & Chốt kỳ lương |
| 17 | Trần Đức Anh — 24110155 | **TV5.3: Kiểm thử Xử lý Đồng thời & Khóa Dữ liệu**: Thử nghiệm 2 phiên đồng thời, đối soát isolation level | Minh chứng Xử lý đồng thời 2 Sessions |
| 18 | Toàn nhóm | **Tổng kết Đề tài, Định hướng phát triển & Q&A Hội đồng**: 100% yêu cầu học phần, hạn chế và mở rộng | Khung Lời cảm ơn & Sẵn sàng Demo |

Nền sáng (`#FFFFFF`), chữ navy (`#17243B`), primary (`#4F46E5`) và font chuẩn Segoe UI theo PeopleOS Design System. Mọi slide đều có thẻ đặc tả kỹ thuật bên trái và ảnh minh chứng sắc nét kèm chú thích bên phải. Speaker notes được chuẩn hóa đồng bộ 5 phần: Thời lượng, Người trình bày, Ý chính, Lời dẫn đề xuất, Căn cứ kỹ thuật.

SHA-256 deck chung (18 slide):

```text
9cf630b0256cc754eefc72625ecda4841b6e1a81c7c600b5493707dd6cd366bc
```

## Bản bàn giao gốc được giữ riêng

- [TV2 slide 4–6](../evidence/TV2/presentation/final/TV2_Slides_4_6.pptx), đúng Quân và notes, nhận nguyên byte từ PR #44 head 7efd9dd. Dùng [biên nhận và hiệu đính TV5](../evidence/TV2/README_TV5_RECEIPT.md) theo canonical v3; biên bản v2 gốc được giữ nguyên để truy vết.
- [TV3 slide 7–9](../evidence/TV3/Minh_chung/TV3_Slides_7-9.pptx), đúng Đạt, nguyên byte PR #43; [biên nhận](../evidence/TV3/README_EVIDENCE.md) phân biệt log/plan 13/10 với ảnh lịch sử.
- [TV4 slide 10–12](../evidence/TV4/TV4_Slides_10-12.pptx), đúng Vinh, từ PR #41. Giữ nguyên cạnh `Minh_chung/` để nguồn trong notes truy được.
- [TV5 slide 13–15](TV5_Slide_13-15.pptx) là deck từng phần dựng 11/10. Notes/số liệu ghi phạm vi lịch sử, không dùng nó thay deck chung cập nhật 13/10. [Ba preview cũ](preview/TV5_Slide_13.png) giữ nguồn cũ.

[Kịch bản và phiếu ghi lượt tập](../TV5_TAP_DEMO.md) vẫn để kết quả trống đến khi chạy thật. Hướng dẫn 1–5 nằm trong docs của repo; chúng không thay PPTX. Nguồn dựng/QA deck chung lưu riêng `build/tv5-final-docs-20261013/`, không phải log thực thi nghiệp vụ.
