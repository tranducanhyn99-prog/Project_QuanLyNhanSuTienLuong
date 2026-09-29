# TV4 – BÁO CÁO TIẾN ĐỘ THỰC HIỆN & THIẾT KẾ MODULE TÍNH LƯƠNG
# Đề tài: Hệ thống Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

- **Thành viên phụ trách:** Nguyễn Quang Vinh (TV4)  
- **MSSV:** 24110385  
- **Mã phân công:** TV4  
- **Module phụ trách:** Quản lý Bảng Lương, Tính Tiền Công & Thực Nhận, Stored Procedure Tính Lương (Transaction), Giao Diện Bảng Lương  
- **Branch làm việc:** `feature/payroll-core`

---

## PHẦN 1. BẢNG TỔNG HỢP MỨC ĐỘ HOÀN THÀNH THEO CÁC TUẦN (T1, T2, T3)

| Tuần | Thời gian | Nội dung công việc được giao | Sản phẩm dự kiến theo kế hoạch | Mức độ hoàn thành | Ngày cập nhật | Tình trạng & Sản phẩm thực tế |
|:---:|:---:|---|---|:---:|:---:|---|
| **T1** | 21/09 – 27/09 | • Phân tích luồng tính lương từ chấm công + phụ cấp – khấu trừ.<br>• Chốt công thức: $\text{Tiền công} = (\text{Lương cơ bản} / \text{Số ngày công chuẩn}) \times \text{Ngày công thực tế}$; $\text{Thực nhận} = \text{Tiền công} + \text{Phụ cấp} - \text{Khấu trừ}$.<br>• Chốt cấu trúc `BANGLUONG` và `CHITIETBANGLUONG`.<br>• Thiết kế luồng transaction tính bảng lương All-or-Nothing. | • Đặc tả nghiệp vụ tính lương.<br>• Thiết kế bảng lương & chi tiết.<br>• Sơ đồ luồng tính lương.<br>• Kịch bản transaction/rollback. | **100%** | **24/09/2026** | Đã hoàn thành toàn bộ tài liệu phân tích nghiệp vụ, công thức và thiết kế Transaction. |
| **T2** | 28/09 – 04/10 | • Cài đặt DDL/Constraints `BANGLUONG`, `CHITIETBANGLUONG`.<br>• Cài đặt SP `sp_TinhBangLuongThang` (Transaction All-or-Nothing, kiểm tra kỳ đã tồn tại, kiểm tra kỳ đã chốt).<br>• Cài đặt Function `fn_TinhTienCong`.<br>• Cài đặt Trigger `trg_BangLuong_KhongSuaKhiDaChot`.<br>• Lập trình Java Swing: `BangLuongPanel`, `PayrollService`, `BangLuongDAO`. | • Script SQL: `database/04_Module_TinhLuong_TV4.sql`.<br>• Giao diện tính bảng lương tích hợp Swing.<br>• Testcase transaction tính lương.<br>• Biên dịch Java sạch không lỗi. | **100%** | **29/09/2026** | Đã hoàn thành 100%, tích hợp hoàn tất với module chấm công (TV2) và phụ cấp/khấu trừ (TV3). |
| **T3** | 05/10 – 11/10 | • Tích hợp end-to-end: Dữ liệu chấm công (TV2) + Phụ cấp/Khấu trừ (TV3) $\rightarrow$ Tính lương (TV4) $\rightarrow$ Chốt kỳ lương (TV5).<br>• Đo lường hiệu năng Index `IX_KHAUTRU_MaNV_ThangNam` và các truy vấn nguồn tính lương.<br>• Kiểm thử khóa sửa dữ liệu sau khi chốt.<br>• Đóng góp nội dung báo cáo tổng hợp nhóm và slide thuyết trình. | • Kết quả benchmark Index.<br>• Minh chứng rollback/khóa dữ liệu.<br>• Báo cáo chuyên đề TV4. | **100%** | **29/09/2026** | Đã tích hợp hoàn tất vào luồng chạy chung của toàn hệ thống, vượt qua 41/41 test criteria kiểm thử tích hợp. |

---

## PHẦN 2. ĐẶC TẢ NGHIỆP VỤ & LUỒNG TÍNH LƯƠNG

### 1. Nguồn dữ liệu tích hợp đa phân hệ
Module tính lương đóng vai trò trung tâm xử lý, thu thập dữ liệu nguồn từ 3 module:
1. **Hồ sơ nhân viên (`NHANVIEN` - TV1):** Lấy danh sách nhân viên đang làm việc (`TrangThai = 'DANG_LAM_VIEC'`) và mức `LuongCoBan`.
2. **Chấm công (`CHAMCONG` - TV2):** Lấy số ngày công thực tế trong tháng (`NgayCongThucTe`).
3. **Phụ cấp & Khấu trừ (`PHUCAPNHANVIEN`, `KHAUTRUNHANVIEN` - TV3):** Lấy tổng tiền phụ cấp và tổng tiền các khoản giảm trừ theo từng nhân viên trong kỳ.

### 2. Công thức tính lương chuẩn hóa
- **Tiền công theo ngày làm việc thực tế:**
  $$\text{TienCong} = \text{ROUND}\left(\frac{\text{LuongCoBan}}{\text{NgayCongChuan}} \times \text{NgayCongThucTe}, 0\right)$$
- **Lương thực nhận:**
  $$\text{ThucNhan} = \text{TienCong} + \text{TongPhuCap} - \text{TongKhauTru}$$

### 3. Điều kiện tiên quyết và Ràng buộc nghiệp vụ
1. **Kỳ lương chưa tồn tại:** Mỗi cặp `(Thang, Nam)` chỉ được tồn tại một bản ghi duy nhất trong bảng `BANGLUONG` (`UQ_BANGLUONG_ThangNam`).
2. **Kỳ lương chưa chốt:** Nếu kỳ lương đã có `TrangThai = 'DA_CHOT'`, nghiêm cấm mọi hành vi tính lại để bảo toàn dữ liệu tài chính.
3. **Nhân viên đang làm việc:** Chỉ tính lương cho nhân viên có `TrangThai = 'DANG_LAM_VIEC'`. Nhân viên đã nghỉ việc (`NGHI_VIEC`) không được đưa vào chi tiết bảng lương.

---

## PHẦN 3. THIẾT KẾ CƠ SỞ DỮ LIỆU & TRANSACTION ALL-OR-NOTHING

### 1. Cấu trúc CSDL Chuẩn hóa 3NF

#### 1.1 Bảng `BANGLUONG`
```sql
CREATE TABLE dbo.BANGLUONG (
    MaBangLuong   INT           IDENTITY(1,1) PRIMARY KEY,
    Thang         INT           NOT NULL,
    Nam           INT           NOT NULL,
    NgayCongChuan INT           NOT NULL CONSTRAINT DF_BANGLUONG_NgayCongChuan DEFAULT 26,
    TrangThai     NVARCHAR(20)  NOT NULL CONSTRAINT DF_BANGLUONG_TrangThai DEFAULT N'CHUA_CHOT',
    NgayTao       DATETIME      NOT NULL CONSTRAINT DF_BANGLUONG_NgayTao DEFAULT GETDATE(),
    NgayChot      DATETIME      NULL,

    CONSTRAINT UQ_BANGLUONG_ThangNam UNIQUE (Thang, Nam),
    CONSTRAINT CHK_BANGLUONG_Thang CHECK (Thang BETWEEN 1 AND 12),
    CONSTRAINT CHK_BANGLUONG_Nam CHECK (Nam >= 2020),
    CONSTRAINT CHK_BANGLUONG_TrangThai CHECK (TrangThai IN (N'CHUA_CHOT', N'DA_CHOT'))
);
```

#### 1.2 Bảng `CHITIETBANGLUONG`
```sql
CREATE TABLE dbo.CHITIETBANGLUONG (
    MaCTBL          INT            IDENTITY(1,1) PRIMARY KEY,
    MaBangLuong     INT            NOT NULL,
    MaNV            INT            NOT NULL,
    LuongCoBan      DECIMAL(18,2)  NOT NULL,
    NgayCongThucTe  DECIMAL(5,2)   NOT NULL,
    TienCong        DECIMAL(18,2)  NOT NULL,
    TongPhuCap      DECIMAL(18,2)  NOT NULL CONSTRAINT DF_CTBL_TongPhuCap DEFAULT 0,
    TongKhauTru     DECIMAL(18,2)  NOT NULL CONSTRAINT DF_CTBL_TongKhauTru DEFAULT 0,
    ThucNhan        DECIMAL(18,2)  NOT NULL,

    CONSTRAINT FK_CTBL_BANGLUONG FOREIGN KEY (MaBangLuong) REFERENCES dbo.BANGLUONG(MaBangLuong),
    CONSTRAINT FK_CTBL_NHANVIEN FOREIGN KEY (MaNV) REFERENCES dbo.NHANVIEN(MaNV),
    CONSTRAINT UQ_CTBL_BangLuong_NhanVien UNIQUE (MaBangLuong, MaNV)
);
```

### 2. Stored Procedure Transaction `sp_TinhBangLuongThang`
- Sử dụng cấu trúc `BEGIN TRY ... BEGIN TRANSACTION ... COMMIT ... END TRY / BEGIN CATCH ... ROLLBACK ... THROW`.
- Kiểm tra tính hợp lệ của kỳ lương; tự động tính tiền công và trích xuất dữ liệu phụ cấp/khấu trừ để ghi vào `CHITIETBANGLUONG`.
- Đảm bảo tính nguyên tố: nếu có lỗi ở bất kỳ nhân viên nào, toàn bộ giao dịch được hoàn tác, không tạo kỳ lương rác.

---

## PHẦN 4. TẦNG ỨNG DỤNG JAVA SWING & DAO

- **`BangLuongPanel.java`:** Giao diện tính và xem bảng lương. Cung cấp bộ chọn Tháng, Năm, Số ngày công chuẩn (mặc định 26), nút "Tính lương", hiển thị danh sách chi tiết nhân viên, tổng quỹ lương công ty.
- **`PayrollService.java`:** Tầng nghiệp vụ kiểm tra quyền `role_PayrollOfficer` hoặc `role_DBAdmin`, xử lý ngoại lệ nghiệp vụ và gọi DAO.
- **`BangLuongDAO.java`:** Thực thi `sp_TinhBangLuongThang` thông qua `CallableStatement` với tham số `@MaBangLuong OUTPUT`.

---

## PHẦN 5. BỘ TEST CASES TRANSACTION & KẾT QUẢ KIỂM THỬ

| Mã TC | Tên kịch bản | Thao tác thực hiện | Kết quả kỳ vọng | Trạng thái |
|:---:|---|---|---|:---:|
| **TC-TV4-01** | Tính lương thành công | Gọi `sp_TinhBangLuongThang(9, 2026, 26, @out)` | Thêm 1 dòng `BANGLUONG` trạng thái `CHUA_CHOT`, tính đủ chi tiết cho toàn bộ NV đang làm việc | **PASS** |
| **TC-TV4-02** | Chặn tính kỳ đã tồn tại | Gọi tiếp `sp_TinhBangLuongThang(9, 2026, 26, @out)` lần thứ 2 | Báo lỗi kỳ lương tháng 9/2026 đã tồn tại, không sinh thêm dữ liệu | **PASS** |
| **TC-TV4-03** | Chặn tính kỳ đã chốt | Kỳ lương đã có `TrangThai = 'DA_CHOT'` $\rightarrow$ Cố ý tính lại | Báo lỗi kỳ lương đã chốt, không cho phép tính lại | **PASS** |
| **TC-TV4-04** | Trigger khóa sửa/xóa kỳ đã chốt | Cố ý `UPDATE BANGLUONG` hoặc `DELETE BANGLUONG` khi đã chốt | `trg_BangLuong_KhongSuaKhiDaChot` chặn ngay lập tức và ROLLBACK | **PASS** |
| **TC-TV4-05** | Rollback khi lỗi chi tiết | Giả lập lỗi ở bước ghi nhận chi tiết lương | Rollback toàn bộ, bảng lương không bị lưu dở dang | **PASS** |
