# Danh sách các điểm cần chỉnh sửa trong Báo cáo cuối kỳ (Bản làm lại)

> **Ghi chú phạm vi rà soát:**
> Danh sách dưới đây đã **loại trừ** phần tỷ lệ % hoàn thành tại Phụ lục B (Phần 3) và tên trường trên trang bìa (Phần 5) theo yêu cầu của nhóm.
> Các mục còn lại tập trung vào tính chính xác của Mục lục, tính đúng đắn của Hình ảnh giao diện và tính chuẩn hóa của Danh mục Bảng biểu.

---

## 1. Cập nhật và sửa lỗi thiếu mục trong Mục lục (TOC)

Hiện tại, phần **MỤC LỤC** (trang v–vii) đang bị sót 3 mục con cấp 2 mặc dù trong phần nội dung chính văn vẫn có đầy đủ:

- [ ] **Bổ sung mục `3.4` trong Mục lục (Chương 3):**
  * *Hiện trạng:* Mục lục đang nhảy thẳng từ `3.3. Stored procedure ghi nhận chấm công` (trang 23) sang `3.5. Stored procedure tính bảng lương tháng` (trang 24).
  * *Cần sửa:* Bổ sung dòng:
    ```text
    3.4. Stored procedure xóa kỳ chưa chốt	24
    ```
- [ ] **Bổ sung mục `4.4` trong Mục lục (Chương 4):**
  * *Hiện trạng:* Mục lục đang nhảy từ `4.3. Tính lại và rollback về savepoint` (trang 33) sang `4.5. Khóa cùng kỳ và cạnh tranh nguồn` (trang 35).
  * *Cần sửa:* Bổ sung dòng:
    ```text
    4.4. Xóa nháp chốt và quy trình mở lại	34
    ```
- [ ] **Bổ sung mục `4.6` trong Mục lục (Chương 4):**
  * *Hiện trạng:* Mục lục đang nhảy từ `4.5. Khóa cùng kỳ và cạnh tranh nguồn` (trang 35) sang `4.7. Kiến trúc, xác thực và phân quyền` (trang 37).
  * *Cần sửa:* Bổ sung dòng:
    ```text
    4.6. Khôi phục lỗi và giới hạn kiểm chứng	36
    ```

> **Cách khắc phục nhanh trong Microsoft Word:**
> 1. Kiểm tra lại các tiêu đề 3.4, 4.4, 4.6 trong chính văn xem đã được gán style **Heading 2** chưa (nếu đang ở style *Normal*, Word sẽ không nhận diện được).
> 2. Nhấp chuột phải vào khối **MỤC LỤC** > chọn **Update Field** > chọn **Update entire table** để Word tự động nạp lại đầy đủ các mục và đánh lại số trang chính xác.

---

## 2. Thay thế và sửa lỗi Hình ảnh minh họa giao diện (Chương 5)

- [ ] **Sửa Hình 5.5 — Màn hình Quản lý hồ sơ nhân viên (trang 44):**
  * *Hiện trạng:* Chú thích ghi là `Hình 5.5. Quản lý hồ sơ nhân viên`, nhưng hình ảnh thực tế lại là màn hình `DANH MỤC / Phòng ban & Chức vụ` (`DanhMucPanel.java` gồm Mã PB, Mã CV, Tên phòng ban, Tên chức vụ).
  * *Cần sửa:* Thay thế bằng ảnh chụp màn hình thực tế của phân hệ **Hồ sơ nhân viên** (`NhanVienPanel.java` gồm các trường: Họ tên, Mã nhân viên, CCCD, Ngày sinh, Số điện thoại, Email, Lương cơ bản, Trạng thái làm việc).

- [ ] **Sửa Hình 5.9 — Minh họa từ chối thao tác không được phân quyền (trang 46):**
  * *Hiện trạng:* Hình 5.9 đang bị **chèn trùng lặp 100% với Hình 5.8** (cùng là ảnh Báo cáo tiền lương với tài khoản `tv5f_d54a94da_admin [Quản trị viên]`, tổng thực nhận 1,230,000 đ, có nút "Mở lại bảng lương"). Ảnh không thể hiện bất kỳ hành vi bị chặn quyền nào.
  * *Cần sửa:* Thay thế bằng ảnh chụp minh họa hành vi phân quyền thực tế, chọn 1 trong 2 phương án:
    * *Phương án A (Khuyên dùng):* Chụp hộp thoại thông báo lỗi / từ chối thao tác (Permission Denied / Access Denied) khi một tài khoản cố thực hiện thao tác vượt quyền (ví dụ: `HR_Manager` cố bấm nút tính lương, hoặc `Employee` cố truy cập chức năng quản trị).
    * *Phương án B:* Chụp giao diện đăng nhập của tài khoản vai trò `Employee` (`gui_emp_a` hoặc `gui_emp_b`), cho thấy phân hệ bị thu hẹp chỉ hiển thị "Báo cáo & Phiếu lương" cá nhân, không có các nút nghiệp vụ của Admin/HR.

---

## 3. Chuẩn hóa tên và ký hiệu Bảng biểu ở Phụ lục

- [ ] **Chuẩn hóa ký hiệu `Bảng B` thành `Bảng B.1` (Phụ lục B - trang 61):**
  * *Hiện trạng:* Đang để tiêu đề `Bảng B. Bảng phân công thực hiện đề tài`.
  * *Cần sửa:* Đổi thành:
    ```text
    Bảng B.1. Bảng phân công thực hiện đề tài
    ```
- [ ] **Chuẩn hóa ký hiệu `Bảng C5` thành `Bảng C.1` (hoặc `Bảng C.5`) (Phụ lục C - trang 65):**
  * *Hiện trạng:* Đang để tiêu đề `Bảng C5. Bảng các thành phần chính của hệ thống`.
  * *Cần sửa:* Đổi thành:
    ```text
    Bảng C.1. Bảng các thành phần chính của hệ thống
    ```
    *(hoặc `Bảng C.5` nếu muốn đánh số tương ứng theo tiểu mục C.5)*.
- [ ] **Đồng bộ lại `DANH MỤC BẢNG` ở đầu tài liệu (trang ix):**
  * Cập nhật lại 2 dòng tham chiếu tương ứng:
    * `Bảng B.1. Phân công thực hiện đề tài`
    * `Bảng C.1. Các thành phần chính của hệ thống`
  * Nhấp chuột phải vào khối **DANH MỤC BẢNG** > chọn **Update Field** để số trang khớp tuyệt đối với bản in.
