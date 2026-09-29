# BÁO CÁO TIẾN ĐỘ HOÀN THIỆN TUẦN 2 - THÀNH VIÊN 3

**Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu  
**Dự án:** Hệ Thống Quản Lý Nhân Sự & Tiền Lương  
**Phân hệ phụ trách:** Module Phụ cấp & Khấu trừ (TV3)  
**Người thực hiện:** Thành viên 3 (Nhóm 06)

---

## 1. Mục tiêu công việc Tuần 2 của TV3

* Hoàn thiện và chuẩn hóa cấu trúc dữ liệu cho danh mục Phụ cấp (`PHUCAP`), Khấu trừ (`KHAUTRU`) và bảng liên kết nhân viên (`NHANVIEN_PHUCAP`, `NHANVIEN_KHAUTRU`).
* Xây dựng các hàm (Functions), Thủ tục lưu trữ (Stored Procedures) và Ràng buộc/Trigger nghiệp vụ để tính toán chính xác tổng phụ cấp và các khoản trích theo lương (BHXH, BHYT, BHTN, Thuế TNCN, Phí công đoàn).
* Phối hợp với TV4 (Tính lương) để tích hợp dữ liệu phụ cấp/khấu trừ vào quy trình tính lương tự động (`sp_TinhBangLuongThang`).
* Viết kịch bản kiểm thử (Test Cases & Test Data) cho các trường hợp đặc biệt (nhân viên có nhiều khoản phụ cấp, mức khấu trừ vượt lương, thay đổi mức đóng bảo hiểm).

---

## 2. Chi tiết các hạng mục đã hoàn thành

### 2.1. Hoàn thiện Cơ sở dữ liệu SQL (`03_phucap_khautru_TV3.sql`)

1. **Ràng buộc toàn vẹn & Kiểm tra nghiệp vụ:**
    * Thêm ràng buộc `CHECK` đảm bảo số tiền phụ cấp, khấu trừ luôn không âm:
      $$\text{SoTien} \ge 0$$
    * Thiết lập giá trị mặc định và logic trạng thái hiệu lực (`TrangThai`, `TuNgay`, `DenNgay`) cho từng quyết định phụ cấp theo nhân viên.

2. **Xây dựng Function tính tổng phụ cấp:**
    * `fn_TongPhuCapTheoNhanVien(@MaNV INT, @Thang INT, @Nam INT)`: Trả về tổng giá trị các khoản phụ cấp đang có hiệu lực trong tháng tính lương của nhân viên cụ thể.

3. **Xây dựng Function & Procedure tính toán khấu trừ bắt buộc:**
    * `fn_TinhBaoHiemBatBuoc(@LuongDongBH DECIMAL(18,2))`: Áp dụng tỷ lệ đóng bảo hiểm theo luật hiện hành:
        * BHXH: $8\%$
        * BHYT: $1.5\%$
        * BHTN: $1\%$
        * Tổng tỷ lệ trích trừ lương: $10.5\%$
    * `sp_CapNhatKhauTruTheoThang(@Thang INT, @Nam INT)`: Tự động quét và lập danh sách các khoản khấu trừ định kỳ cho toàn bộ nhân sự đang hoạt động.

4. **Trigger kiểm soát tính hợp lệ:**
    * `trg_CheckHanPhuCap`: Chặn thao tác chèn/cập nhật nếu `TuNgay > DenNgay`.
    * `trg_AuditThayDoiPhuCap`: Ghi nhật ký vào bảng kiểm toán khi có sự điều chỉnh định mức hoặc chính sách phụ cấp đặc biệt.

---

### 2.2. Tích hợp liên module (TV3 $\leftrightarrow$ TV4 & Tầng Java)

1. **Giao tiếp với Module Tính lương (TV4):**
    * Chuẩn hóa đầu ra (output) để thủ tục `sp_TinhBangLuongThang` của TV4 có thể gọi trực tiếp `fn_TongPhuCapTheoNhanVien` và tính toán khấu trừ thuế/bảo hiểm cho từng dòng `ChiTietBangLuong`.
    * Đảm bảo cấu trúc dữ liệu không bị xung đột khóa ngoại (`FK`) khi TV4 chốt bảng lương tháng.

2. **Hỗ trợ Tầng ứng dụng Java:**
    * Chuẩn bị dữ liệu mẫu và câu truy vấn cho màn hình quản lý phụ cấp, khấu trừ trong ứng dụng Swing.
    * Xử lý kiểu dữ liệu tiền tệ `DECIMAL(18,2)` đồng bộ với `BigDecimal` trong Java để tránh sai số dấu phẩy động.

---

## 3. Kết quả kiểm thử & Nghiệm thu chức năng

| Mã Test Case | Tình huống kiểm thử | Dữ liệu kiểm tra | Kết quả kỳ vọng | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC_PC_01** | Tính tổng phụ cấp nhân viên có 2 khoản (Ăn trưa + Xăng xe) | MaNV: 101, Ăn trưa: 730.000, Xăng xe: 500.000 | Tổng phụ cấp = 1.230.000 | **PASS** |
| **TC_PC_02** | Phụ cấp đã hết hạn hiệu lực | `DenNgay < 01/03/2026`, kiểm tra tháng 03/2026 | Không được cộng vào tổng phụ cấp tháng 3 | **PASS** |
| **TC_KT_01** | Tính tỷ lệ trích bảo hiểm bắt buộc | Lương đóng BH = 10.000.000 VNĐ | Trừ BHXH (800k), BHYT (150k), BHTN (100k) | **PASS** |
| **TC_KT_02** | Thêm khoản khấu trừ giá trị âm | Số tiền = -200.000 VNĐ | Trigger / CHECK constraint chặn lại | **PASS** |
| **TC_INT_01** | Tích hợp vào thủ tục tính bảng lương của TV4 | Chạy `sp_TinhBangLuongThang` cho 50 nhân viên | Bảng lương nhận đủ tổng phụ cấp & khấu trừ | **PASS** |

---

## 4. Khó khăn gặp phải & Hướng xử lý

* **Vấn đề:** Ban đầu thủ tục tính phụ cấp chưa xét điều kiện thời gian (`TuNgay`, `DenNgay`), dẫn đến nhân viên nghỉ việc hoặc phụ cấp dự án đã kết thúc vẫn bị tính tiền.
    * **Giải pháp:** Bổ sung mệnh đề lọc `BETWEEN` theo chu kỳ ngày đầu tháng và cuối tháng của kỳ trả lương.
* **Vấn đề:** Khi một nhân viên không có bất kỳ khoản phụ cấp nào, hàm trả về giá trị `NULL` làm phép tính lương tổng bị `NULL`.
    * **Giải pháp:** Sử dụng `ISNULL(SUM(SoTien), 0)` và `COALESCE` trong toàn bộ các hàm tổng hợp phụ cấp và khấu trừ.

---

## 5. Kế hoạch công việc Tuần 3 của TV3

1. Hỗ trợ hoàn thiện giao diện Swing cho phép Kế toán thêm/sửa/xóa chính sách phụ cấp linh hoạt trực tiếp từ phần mềm.
2. Thiết lập Index tối ưu tốc độ truy vấn trên các bảng `NHANVIEN_PHUCAP(MaNV, MaPC)` và `NHANVIEN_KHAUTRU(MaNV, MaKT)`.
3. Hoàn tất báo cáo tổng hợp chi phí phụ cấp/khấu trừ để xuất ra file Excel hoặc báo cáo dạng bảng trong module Thống kê.