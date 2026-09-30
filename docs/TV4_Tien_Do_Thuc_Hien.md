# TV4 – BÁO CÁO TIẾN ĐỘ THỰC HIỆN & THIẾT KẾ MODULE TÍNH LƯƠNG
# Đề tài: Hệ thống Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

- **Thành viên phụ trách:** Nguyễn Quang Vinh (TV4)  
- **MSSV:** 24110385  
- **Mã phân công:** TV4  
- **Module phụ trách:** Quản lý Bảng Lương, Tính Tiền Công & Thực Nhận, Stored Procedure Tính Lương (Transaction), Giao Diện Bảng Lương  
- **Branch làm việc:** `feature/tv4-payroll-validation`

---

## PHẦN 1. BẢNG TỔNG HỢP MỨC ĐỘ HOÀN THÀNH THEO CÁC TUẦN (T1, T2, T3)

| Tuần | Thời gian | Nội dung công việc được giao | Sản phẩm dự kiến theo kế hoạch | Mức độ hoàn thành | Ngày cập nhật | Tình trạng & Sản phẩm thực tế |
|:---:|:---:|---|---|:---:|:---:|---|
| **T1** | 21/09 – 27/09 | • Phân tích luồng tính lương từ chấm công + phụ cấp – khấu trừ.<br>• Chốt công thức: $\text{Tiền công} = (\text{Lương cơ bản} / \text{Số ngày công chuẩn}) \times \text{Ngày công thực tế}$; $\text{Thực nhận} = \text{Tiền công} + \text{Phụ cấp} - \text{Khấu trừ}$.<br>• Chốt cấu trúc `BANGLUONG` và `CHITIETBANGLUONG`.<br>• Thiết kế luồng transaction tính bảng lương All-or-Nothing. | • Đặc tả nghiệp vụ tính lương.<br>• Thiết kế bảng lương & chi tiết.<br>• Sơ đồ luồng tính lương.<br>• Kịch bản transaction/rollback. | **100%** | **24/09/2026** | Đã hoàn thành toàn bộ tài liệu phân tích nghiệp vụ, công thức và thiết kế Transaction. |
| **T2** | 28/09 – 04/10 | • Cài đặt DDL/Constraints `BANGLUONG`, `CHITIETBANGLUONG`.<br>• Cài đặt SP `sp_TinhBangLuongThang` (Transaction All-or-Nothing, kiểm tra kỳ đã tồn tại, kiểm tra kỳ đã chốt).<br>• Cài đặt Function `fn_TinhTienCong`.<br>• Cài đặt Trigger `trg_BangLuong_KhongSuaKhiDaChot`.<br>• Lập trình Java Swing: `BangLuongPanel`, `PayrollService`, `BangLuongDAO`. | • Script SQL: `database/04_Module_TinhLuong_TV4.sql`.<br>• Giao diện tính bảng lương tích hợp Swing.<br>• Testcase transaction tính lương.<br>• Biên dịch Java sạch không lỗi. | **100%** | **29/09/2026** | Đã hoàn thành 100%, tích hợp hoàn tất với module chấm công (TV2) và phụ cấp/khấu trừ (TV3). |
| **T3** | 05/10 – 11/10 | • Tích hợp end-to-end: Dữ liệu chấm công (TV2) + Phụ cấp/Khấu trừ (TV3) $\rightarrow$ Tính lương (TV4) $\rightarrow$ Chốt kỳ lương (TV5).<br>• Đo lường hiệu năng Index `IX_KHAUTRU_MaNV_ThangNam` và các truy vấn nguồn tính lương.<br>• Kiểm thử khóa sửa dữ liệu sau khi chốt.<br>• Đóng góp nội dung báo cáo tổng hợp nhóm và slide thuyết trình. | • Kết quả benchmark Index.<br>• Minh chứng rollback/khóa dữ liệu.<br>• Báo cáo chuyên đề TV4. | **100%** | **30/09/2026** | E2E đạt 10/10; kiểm tra concurrency không sinh kỳ/chi tiết trùng; benchmark covering seek khấu trừ giảm từ 547 xuống 4 logical reads trong lần đo tham khảo. |

---

## PHẦN 2. ĐẶC TẢ NGHIỆP VỤ & LUỒNG TÍNH LƯƠNG

### 1. Nguồn dữ liệu tích hợp đa phân hệ
Module tính lương đóng vai trò trung tâm xử lý, thu thập dữ liệu nguồn từ 3 module:
1. **Hồ sơ nhân viên (`NHANVIEN` - TV1):** Lấy danh sách nhân viên đang làm việc (`TrangThai = 'DANG_LAM_VIEC'`) và mức `LuongCoBan`.
2. **Chấm công (`CHAMCONG` - TV2):** Lấy số ngày công thực tế trong tháng (`NgayCongThucTe`).
3. **Phụ cấp & Khấu trừ (`PHUCAPNHANVIEN`, `KHAUTRUNHANVIEN` - TV3):** Lấy tổng tiền phụ cấp và tổng tiền các khoản giảm trừ theo từng nhân viên trong kỳ.

### 2. Công thức tính lương chuẩn hóa
- **Tiền công theo ngày làm việc thực tế:**
  $$\text{TienCong} = \text{ROUND}\left(\frac{\text{LuongCoBan}}{\text{NgayCongChuan}} \times \text{NgayCongThucTe}, 2\right)$$
- **Lương thực nhận:**
  $$\text{ThucNhan} = \text{TienCong} + \text{TongPhuCap} - \text{TongKhauTru}$$

### 3. Điều kiện tiên quyết và Ràng buộc nghiệp vụ
1. **Một header cho mỗi kỳ:** Mỗi cặp `(Thang, Nam)` chỉ có một bản ghi `BANGLUONG`; kỳ `CHUA_CHOT` được tính lại nguyên tử và giữ nguyên `MaBangLuong`.
2. **Kỳ lương chưa chốt:** Nếu kỳ lương có `TrangThai = 'DA_CHOT'`, thủ tục tính lương từ chối tính lại để bảo toàn dữ liệu tài chính.
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
    TrangThai     VARCHAR(15)   NOT NULL CONSTRAINT DF_BANGLUONG_TrangThai DEFAULT 'CHUA_CHOT',
    NgayTao       DATETIME      NOT NULL CONSTRAINT DF_BANGLUONG_NgayTao DEFAULT GETDATE(),
    NgayChot      DATETIME      NULL,

    CONSTRAINT UQ_BANGLUONG_ThangNam UNIQUE (Thang, Nam),
    CONSTRAINT CHK_BANGLUONG_Thang CHECK (Thang BETWEEN 1 AND 12),
    CONSTRAINT CHK_BANGLUONG_Nam CHECK (Nam BETWEEN 2020 AND 2100),
    CONSTRAINT CHK_BANGLUONG_TrangThai CHECK (TrangThai IN ('CHUA_CHOT', 'DA_CHOT'))
);
```

#### 1.2 Bảng `CHITIETBANGLUONG`
```sql
CREATE TABLE dbo.CHITIETBANGLUONG (
    MaChiTiet       INT            IDENTITY(1,1) PRIMARY KEY,
    MaBangLuong     INT            NOT NULL,
    MaNV            INT            NOT NULL,
    LuongCoBan      DECIMAL(18,2)  NOT NULL,
    NgayCongThucTe  INT            NOT NULL,
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
- Khi chạy độc lập, thủ tục tự mở và kết thúc transaction; khi được gọi trong transaction có sẵn, thủ tục dùng savepoint và không commit/rollback transaction của caller.
- Khóa kỳ lương và tập dữ liệu nguồn trong cùng transaction, sau đó tính tiền công và tổng hợp phụ cấp/khấu trừ vào `CHITIETBANGLUONG`.
- Nếu có lỗi, thủ tục rollback toàn bộ transaction do nó sở hữu hoặc chỉ rollback về savepoint; lần retry sau đó vẫn có thể thực hiện an toàn.

---

## PHẦN 4. TẦNG ỨNG DỤNG JAVA SWING & DAO

- **`BangLuongPanel.java`:** Giao diện tính và xem bảng lương. Cung cấp bộ chọn Tháng, Năm, Số ngày công chuẩn (mặc định 26), nút "Tính lương", hiển thị danh sách chi tiết nhân viên, tổng quỹ lương công ty.
- **`PayrollService.java`:** Tầng nghiệp vụ kiểm tra quyền `role_PayrollOfficer` hoặc `role_DBAdmin`, xử lý ngoại lệ nghiệp vụ và gọi DAO.
- **`BangLuongDAO.java`:** Thực thi `sp_TinhBangLuongThang` thông qua `CallableStatement` với tham số `@MaBangLuong OUTPUT`.

---

## PHẦN 5. BỘ TEST CASES TRANSACTION & KẾT QUẢ KIỂM THỬ

| Mã TC | Tên kịch bản | Thao tác thực hiện | Kết quả kỳ vọng | Trạng thái |
|:---:|---|---|---|:---:|
| **TC-TV4-01** | Tính lương thành công | Script chọn động một kỳ quá khứ chưa dùng rồi gọi `sp_TinhBangLuongThang` | Thêm 1 dòng `BANGLUONG` trạng thái `CHUA_CHOT`, tính đủ chi tiết cho toàn bộ NV đang làm việc | **PASS** |
| **TC-TV4-02** | Tính lại kỳ chưa chốt | Gọi lại cùng kỳ động khi đang `CHUA_CHOT` | Giữ nguyên `MaBangLuong`, thay chi tiết nguyên tử và không sinh header trùng | **PASS** |
| **TC-TV4-03** | Chặn tính kỳ đã chốt | Chốt kỳ động rồi cố ý tính lại | Báo lỗi kỳ lương đã chốt, không cho phép tính lại | **PASS** |
| **TC-TV4-04** | Trigger khóa sửa/xóa kỳ đã chốt | Cố ý `UPDATE`/`DELETE` kỳ đã chốt, sau đó gọi `sp_HuyChotBangLuong` | Chặn sửa/xóa trực tiếp; chỉ cho phép transition mở lại có kiểm soát | **PASS** |
| **TC-TV4-05** | Rollback và retry | Giả lập lỗi ở bước ghi chi tiết, đối chiếu snapshot rồi chạy lại | Header/chi tiết được khôi phục đầy đủ; retry thành công | **PASS** |
| **TC-TV4-06** | Concurrency cùng kỳ | Hai session cùng tính một tháng/năm | Session sau bị block; chỉ có một header và không có chi tiết trùng | **PASS** |
| **TC-TV4-07** | Benchmark truy vấn nguồn | Đo truy vấn chấm công, phụ cấp và khấu trừ | Kết quả đúng; index khấu trừ có covering seek hiệu quả | **PASS** |

Chi tiết môi trường, số đo và kịch bản demo nằm tại `docs/TV4_Payroll_Test_Report.md`.
