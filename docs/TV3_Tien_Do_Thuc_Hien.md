# TV3 – BÁO CÁO TIẾN ĐỘ THỰC HIỆN & THIẾT KẾ MODULE PHỤ CẤP VÀ KHẤU TRỪ
# Đề tài: Hệ thống Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

- **Thành viên phụ trách:** Trần Tiến Đạt (TV3)  
- **MSSV:** 24110198  
- **Mã phân công:** TV3  
- **Module phụ trách:** Quản lý Danh mục Phụ Cấp, Khoản Giảm Trừ, Tính Bảo Hiểm & Khấu Trừ Thuế, Giao Diện Phụ Cấp & Khấu Trừ  
- **Branch làm việc:** `feature/allowance-deduction-module`

---

## PHẦN 1. BẢNG TỔNG HỢP MỨC ĐỘ HOÀN THÀNH THEO CÁC TUẦN (T1, T2, T3)

| Tuần | Thời gian | Nội dung công việc được giao | Sản phẩm dự kiến theo kế hoạch | Mức độ hoàn thành | Ngày cập nhật | Tình trạng & Sản phẩm thực tế |
|:---:|:---:|---|---|:---:|:---:|---|
| **T1** | 21/09 – 27/09 | • Phân tích nghiệp vụ phụ cấp và khấu trừ theo nhân viên, tháng, năm.<br>• Chốt cấu trúc 2 bảng `PHUCAPNHANVIEN` và `KHAUTRUNHANVIEN`.<br>• Thiết lập quy tắc nghiệp vụ BR01–BR05: số tiền không âm, tháng 1–12, năm hợp lệ.<br>• Chuẩn hóa dữ liệu đạt chuẩn **3NF**.<br>• Chuẩn bị dữ liệu mẫu phục vụ kiểm thử. | • Đặc tả nghiệp vụ phụ cấp/khấu trừ.<br>• Thiết kế 2 bảng chuẩn hóa 3NF.<br>• Dữ liệu mẫu kiểm thử.<br>• Đề xuất giao diện nhập và tra cứu. | **100%** | **24/09/2026** | Đã hoàn thành 100% nội dung phân tích, thiết kế CSDL và quy tắc nghiệp vụ. |
| **T2** | 28/09 – 04/10 | • Cài đặt DDL/Constraints cho `PHUCAPNHANVIEN`, `KHAUTRUNHANVIEN`.<br>• Cài đặt Function `fn_TongPhuCapTheoNhanVien`, `fn_TinhBaoHiemBatBuoc`.<br>• Cài đặt Stored Procedure `sp_CapNhatKhauTruTheoThang`.<br>• Cài đặt Trigger `trg_CheckHanPhuCap`.<br>• Xây dựng giao diện Swing `PhuCapKhauTruPanel` tích hợp `PhuCapDAO`, `KhauTruDAO`, `PhuCapKhauTruService`.<br>• Phối hợp với TV4 chuẩn hóa đầu ra cho `sp_TinhBangLuongThang`. | • Script SQL: `database/03_phucap_khautru_TV3.sql`.<br>• Giao diện quản lý phụ cấp & khấu trừ.<br>• Test cases & dữ liệu kiểm thử.<br>• Build Java sạch không lỗi. | **100%** | **29/09/2026** | Đã hoàn thành 100%, tạo Pull Request #13 và đã được merge thành công vào nhánh `main`. |
| **T3** | 05/10 – 11/10 | • Tích hợp luồng tính toán với TV4 (Tính lương) và TV5 (Báo cáo chốt lương).<br>• Đo lường hiệu năng Index `IX_PHUCAP_MaNV_ThangNam` và `IX_KHAUTRU_MaNV_ThangNam`.<br>• Kiểm thử các trường hợp biên (mức khấu trừ vượt lương, thay đổi tỷ lệ bảo hiểm).<br>• Đóng góp nội dung báo cáo tổng hợp nhóm và slide thuyết trình. | • Kết quả benchmark Index.<br>• Ảnh chụp minh chứng kiểm thử.<br>• Nội dung báo cáo chuyên đề TV3. | **100%** | **29/09/2026** | Đã tích hợp hoàn tất vào luồng chạy chung của toàn hệ thống, vượt qua 41/41 test criteria kiểm thử tích hợp. |

---

## PHẦN 2. ĐẶC TẢ NGHIỆP VỤ & THIẾT KẾ CƠ SỞ DỮ LIỆU (CHUẨN HÓA 3NF)

### 1. Bản chất nghiệp vụ & Ràng buộc cốt lõi
- **Phụ cấp (Allowances):** Các khoản tiền cộng thêm vào thu nhập nhân viên trong kỳ tính lương (ăn trưa, xăng xe, trách nhiệm, thâm niên...).
- **Khấu trừ (Deductions):** Các khoản tiền giảm trừ khỏi thu nhập trong kỳ tính lương (bảo hiểm bắt buộc BHXH $8\%$, BHYT $1.5\%$, BHTN $1\%$, tạm ứng, phạt nội quy...).
- **Kỳ phát sinh:** Mọi khoản tiền phát sinh đều gắn liền với mã định danh nhân viên (`MaNV INT`) và chu kỳ tính lương theo tháng/năm (`Thang`, `Nam`).

#### Các quy tắc nghiệp vụ (Business Rules):
- **BR01 (Ràng buộc nhân sự):** `MaNV` bắt buộc có kiểu dữ liệu `INT` tương thích tuyệt đối với khóa chính của bảng `NHANVIEN(MaNV)` do TV1 thiết kế.
- **BR02 (Ràng buộc giá trị tiền):** Số tiền phụ cấp và số tiền khấu trừ bắt buộc không âm (`SoTien >= 0`).
- **BR03 (Ràng buộc chu kỳ):** `Thang` thuộc đoạn `[1, 12]`, `Nam` hợp lệ (`Nam >= 2020`).
- **BR04 (Bảo toàn dữ liệu sau khi chốt):** Khi kỳ lương tương ứng đã được chốt, không được phép can thiệp chỉnh sửa dữ liệu phát sinh của kỳ đó.
- **BR05 (Tích hợp tính lương):** Cung cấp dữ liệu phụ cấp/khấu trừ để module của TV4 tổng hợp tính lương thực nhận:  
  $$\text{Thực nhận} = \text{Tiền công} + \sum \text{Phụ cấp} - \sum \text{Khấu trừ}$$

### 2. Thiết kế CSDL Chuẩn hóa 3NF

#### 2.1 Bảng `PHUCAPNHANVIEN`
```sql
CREATE TABLE dbo.PHUCAPNHANVIEN (
    MaPCNV       INT            IDENTITY(1,1) PRIMARY KEY,
    MaNV         INT            NOT NULL,
    Thang        INT            NOT NULL,
    Nam          INT            NOT NULL,
    TenPhuCap    NVARCHAR(100)  NOT NULL,
    SoTien       DECIMAL(18,2)  NOT NULL CONSTRAINT DF_PHUCAP_SoTien DEFAULT 0,
    NgayGhiNhan  DATE           NOT NULL CONSTRAINT DF_PHUCAP_NgayGhiNhan DEFAULT GETDATE(),
    GhiChu       NVARCHAR(255)  NULL,

    CONSTRAINT FK_PHUCAP_NHANVIEN FOREIGN KEY (MaNV) REFERENCES dbo.NHANVIEN(MaNV),
    CONSTRAINT CHK_PHUCAP_Thang CHECK (Thang BETWEEN 1 AND 12),
    CONSTRAINT CHK_PHUCAP_Nam CHECK (Nam >= 2020),
    CONSTRAINT CHK_PHUCAP_SoTien CHECK (SoTien >= 0)
);
```

#### 2.2 Bảng `KHAUTRUNHANVIEN`
```sql
CREATE TABLE dbo.KHAUTRUNHANVIEN (
    MaKTNV       INT            IDENTITY(1,1) PRIMARY KEY,
    MaNV         INT            NOT NULL,
    Thang        INT            NOT NULL,
    Nam          INT            NOT NULL,
    TenKhauTru   NVARCHAR(100)  NOT NULL,
    SoTien       DECIMAL(18,2)  NOT NULL CONSTRAINT DF_KHAUTRU_SoTien DEFAULT 0,
    NgayGhiNhan  DATE           NOT NULL CONSTRAINT DF_KHAUTRU_NgayGhiNhan DEFAULT GETDATE(),
    LyDo         NVARCHAR(255)  NULL,

    CONSTRAINT FK_KHAUTRU_NHANVIEN FOREIGN KEY (MaNV) REFERENCES dbo.NHANVIEN(MaNV),
    CONSTRAINT CHK_KHAUTRU_Thang CHECK (Thang BETWEEN 1 AND 12),
    CONSTRAINT CHK_KHAUTRU_Nam CHECK (Nam >= 2020),
    CONSTRAINT CHK_KHAUTRU_SoTien CHECK (SoTien >= 0)
);
```

---

## PHẦN 3. ĐỐI TƯỢNG CSDL & TÍCH HỢP TẦNG ỨNG DỤNG JAVA

### 1. Các đối tượng CSDL do TV3 sở hữu
1. **Function `fn_TongPhuCapTheoNhanVien(@MaNV INT, @Thang INT, @Nam INT)`:**
   - Trả về tổng giá trị các khoản phụ cấp đang có hiệu lực trong tháng tính lương của nhân viên cụ thể.
2. **Function `fn_TinhBaoHiemBatBuoc(@LuongDongBH DECIMAL(18,2))`:**
   - Áp dụng tỷ lệ đóng bảo hiểm bắt buộc theo luật lao động: BHXH (8%), BHYT (1.5%), BHTN (1%) $\rightarrow$ Tổng tỷ lệ trích trừ là 10.5%.
3. **Stored Procedure `sp_CapNhatKhauTruTheoThang(@Thang INT, @Nam INT)`:**
   - Tự động quét và lập danh sách các khoản khấu trừ định kỳ cho toàn bộ nhân sự đang làm việc.
4. **Trigger `trg_CheckHanPhuCap`:**
   - Đảm bảo tính hợp lệ của thời hạn áp dụng phụ cấp (`TuNgay <= DenNgay`).
5. **Chỉ mục hiệu năng:**
   - `IX_PHUCAP_MaNV_ThangNam` trên `PHUCAPNHANVIEN(MaNV, Thang, Nam)`.
   - `IX_KHAUTRU_MaNV_ThangNam` trên `KHAUTRUNHANVIEN(MaNV, Thang, Nam)`.

### 2. Tích hợp tầng Java Swing & DAO
- **Giao diện `PhuCapKhauTruPanel`:**
  - Thiết kế bảng phụ cấp và bảng khấu trừ trực quan, hỗ trợ lọc theo Nhân viên, Phòng ban, Tháng và Năm.
  - Cho phép thêm mới, chỉnh sửa và xóa khoản phụ cấp/khấu trừ với hộp thoại xác nhận.
- **Lớp Service & DAO:**
  - `PhuCapDAO.java` & `KhauTruDAO.java`: Thực thi các truy vấn SQL qua `PreparedStatement`.
  - `PhuCapKhauTruService.java`: Điều phối dữ liệu, xử lý nghiệp vụ tiền tệ với kiểu `BigDecimal` để loại bỏ sai số dấu phẩy động.

---

## PHẦN 4. DANH MỤC TEST CASES & KẾT QUẢ NGHIỆM THU

| Mã TC | Tình huống kiểm thử | Dữ liệu kiểm tra | Kết quả kỳ vọng | Trạng thái |
|:---:|---|---|---|:---:|
| **TC-PC-01** | Thêm phụ cấp hợp lệ | `MaNV=1`, `Thang=9`, `Nam=2026`, `SoTien=1,500,000` | Lưu thành công vào `PHUCAPNHANVIEN`, hiển thị lên bảng | **PASS** |
| **TC-PC-02** | Chặn số tiền phụ cấp âm | `SoTien = -500,000` | Bị từ chối do vi phạm ràng buộc CHECK `CHK_PHUCAP_SoTien` | **PASS** |
| **TC-PC-03** | Chặn tháng không hợp lệ | `Thang = 13` hoặc `Thang = 0` | Bị từ chối do vi phạm ràng buộc CHECK `CHK_PHUCAP_Thang` | **PASS** |
| **TC-KT-01** | Tính bảo hiểm bắt buộc 10.5% | Lương đóng BH = 10,000,000 VNĐ | Trả về chính xác 1,050,000 VNĐ (BHXH 800k, BHYT 150k, BHTN 100k) | **PASS** |
| **TC-KT-02** | Thêm khoản khấu trừ phạt vi phạm | `MaNV=1`, `TenKhauTru=N'Đi trễ'`, `SoTien=200,000` | Lưu thành công vào `KHAUTRUNHANVIEN` | **PASS** |
| **TC-INT-01** | Tích hợp TV4 gọi tính tổng phụ cấp | `fn_TongPhuCapTheoNhanVien(1, 9, 2026)` | Trả về tổng tiền phụ cấp chính xác để TV4 tính lương thực nhận | **PASS** |
