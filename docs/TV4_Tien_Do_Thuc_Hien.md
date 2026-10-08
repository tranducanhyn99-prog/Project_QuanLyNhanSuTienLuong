# TV4 – BÁO CÁO TIẾN ĐỘ THỰC HIỆN & THIẾT KẾ MODULE TÍNH LƯƠNG
# Đề tài: Hệ thống Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

- **Thành viên phụ trách:** Nguyễn Quang Vinh (TV4)  
- **MSSV:** 24110385  
- **Mã phân công:** TV4  
- **Module phụ trách:** Quản lý Bảng Lương, Tính Tiền Công & Thực Nhận, Stored Procedure Tính Lương (Transaction), Giao Diện Bảng Lương  
- **Branch làm việc:** `feature/tv4-payroll-validation`
- **Ngày rà soát tài liệu/source:** 08/10/2026

---

## PHẦN 1. BẢNG TỔNG HỢP MỨC ĐỘ HOÀN THÀNH THEO CÁC TUẦN (T1, T2, T3)

| Tuần | Thời gian | Nội dung công việc được giao | Sản phẩm dự kiến theo kế hoạch | Mức độ hoàn thành | Ngày cập nhật | Tình trạng & Sản phẩm thực tế |
|:---:|:---:|---|---|:---:|:---:|---|
| **T1** | 21/09 – 27/09 | • Phân tích luồng tính lương từ chấm công + phụ cấp – khấu trừ.<br>• Chốt công thức: $\text{Tiền công} = (\text{Lương cơ bản} / \text{Số ngày công chuẩn}) \times \text{Ngày công thực tế}$; $\text{Thực nhận} = \text{Tiền công} + \text{Phụ cấp} - \text{Khấu trừ}$.<br>• Chốt cấu trúc `BANGLUONG` và `CHITIETBANGLUONG`.<br>• Thiết kế luồng transaction tính bảng lương All-or-Nothing. | • Đặc tả nghiệp vụ tính lương.<br>• Thiết kế bảng lương & chi tiết.<br>• Sơ đồ luồng tính lương.<br>• Kịch bản transaction/rollback. | **100%** | **24/09/2026** | Đã hoàn thành toàn bộ tài liệu phân tích nghiệp vụ, công thức và thiết kế Transaction. |
| **T2** | 28/09 – 04/10 | • Cài đặt DDL/Constraints `BANGLUONG`, `CHITIETBANGLUONG`.<br>• Cài đặt SP `sp_TinhBangLuongThang` (Transaction All-or-Nothing, kiểm tra kỳ đã tồn tại, kiểm tra kỳ đã chốt).<br>• Cài đặt Function `fn_TinhTienCong`.<br>• Cài đặt Trigger `trg_BangLuong_KhongSuaKhiDaChot`.<br>• Lập trình Java Swing: `BangLuongPanel`, `PayrollService`, `BangLuongDAO`. | • Script SQL: `database/04_Module_TinhLuong_TV4.sql`.<br>• Giao diện tính bảng lương tích hợp Swing.<br>• Testcase transaction tính lương.<br>• Biên dịch Java sạch không lỗi. | **100%** | **29/09/2026** | Đã hoàn thành 100%, tích hợp hoàn tất với module chấm công (TV2) và phụ cấp/khấu trừ (TV3). |
| **T3** | 05/10 – 11/10 | • Tích hợp dữ liệu nguồn → tính lương → chốt kỳ.<br>• Kiểm tra transaction, khóa sau chốt và benchmark. | • SQL modules, runner và log QA được dẫn tại báo cáo kiểm thử TV4. | Có bằng chứng theo phạm vi | **08/10/2026** | QA tích hợp PASS E2E 10/10, benchmark và 4 race đóng kỳ. Concurrency hai session tính cùng kỳ có log cũ ngày 07/10; không chạy trong lần tích hợp 08/10. Số đo chỉ áp dụng cho fixture/lần chạy tương ứng. |

---

## PHẦN 2. ĐẶC TẢ NGHIỆP VỤ & LUỒNG TÍNH LƯƠNG

### 1. Nguồn dữ liệu tích hợp đa phân hệ
Module tính lương đóng vai trò trung tâm xử lý, thu thập dữ liệu nguồn từ 3 module:
1. **Hồ sơ nhân viên (`NHANVIEN` - TV1):** Lấy nhân viên đủ điều kiện trong kỳ cùng mức lương áp dụng. Bao gồm người đang làm, người nghỉ từ giữa kỳ trở đi, hoặc người có dữ liệu công/phụ cấp/khấu trừ trong kỳ; không chỉ dựa vào trạng thái hiện tại.
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
3. **Nhân viên đủ điều kiện trong kỳ:** Procedure xét ngày vào làm, ngày nghỉ việc và dữ liệu nghiệp vụ. Trạng thái `NGHI_VIEC` hiện tại không tự loại người đã làm việc trong kỳ.

### 4. Lịch sử mức lương và chi tiết kỳ cũ

Khi tính kỳ trước, procedure ưu tiên mức `LuongCoBan` đã lưu trong chi tiết của chính kỳ đó; nếu chưa có thì lấy bản ghi gần nhất trong `LICHSULUONG` có hiệu lực trước ngày đầu kỳ, rồi mới fallback về lương hiện tại. Lịch sử lương được đọc trong transaction nguồn. Điều này hỗ trợ tính lại khi dữ liệu lịch sử có sẵn; không thể khôi phục mức lương cũ chưa từng được lưu trước migration.

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

### Quyền SQL Server thực tế theo module 05

| Role | Quyền liên quan | Từ chối/giới hạn |
|---|---|---|
| `role_HRManager` | SELECT/INSERT/UPDATE/DELETE nhân sự, phòng ban, chức vụ, attendance, allowance và deduction; đọc bảng lương/chi tiết. | DENY `EXECUTE` trên `sp_ChotBangLuong`; DENY mọi quyền trên `TAIKHOAN`. Trigger từ chối sửa nguồn thuộc kỳ đã chốt. |
| `role_PayrollOfficer` | SELECT/INSERT payroll header/detail; gọi procedure tính/chốt/hủy chốt/xóa kỳ nháp; CRUD allowance/deduction. | Chỉ SELECT `NHANVIEN`, `CHAMCONG`, `PHONGBAN`, `CHUCVU`; DENY ghi các bảng này và mọi quyền trên `TAIKHOAN`. Không được direct UPDATE/DELETE payroll header/detail. |

Ở tầng Java, `PhuCapKhauTruService` cho phép `DB_Admin`, `HR_Manager`, `Payroll_Officer` xem/thêm/xóa phụ cấp và khấu trừ; thao tác xóa kỳ chỉ cho `DB_Admin` và `Payroll_Officer`. Chi tiết SQL nằm trong `database/05_Security_Payroll_TV5.sql`; menu/service không thay thế quyền SQL. Role suite của lần tích hợp 08/10 có 22 permission checks, 3 operation bị chặn và cleanup PASS: `build/sql-verification/20261008_101955_11b4e24c/test_security_roles_TV5.log`.

| Mã TC | Tên kịch bản | Thao tác thực hiện | Kết quả kỳ vọng | Trạng thái |
|:---:|---|---|---|:---:|
| **TC-TV4-01** | Tính lương thành công | Script chọn động một kỳ quá khứ chưa dùng rồi gọi `sp_TinhBangLuongThang` | Tạo header và chi tiết theo nguồn kỳ | **PASS (08/10)** |
| **TC-TV4-02** | Tính lại kỳ chưa chốt | Gọi lại cùng kỳ động khi đang `CHUA_CHOT` | Giữ nguyên `MaBangLuong`, thay chi tiết nguyên tử | **PASS (08/10)** |
| **TC-TV4-03** | Chặn tính kỳ đã chốt | Chốt kỳ động rồi cố ý tính lại | Báo lỗi; không tính lại | **PASS (08/10)** |
| **TC-TV4-04** | Trigger khóa sửa/xóa kỳ đã chốt | Cố ý `UPDATE`/`DELETE` kỳ đã chốt, sau đó gọi `sp_HuyChotBangLuong` | Chặn sửa/xóa trực tiếp; mở lại chỉ qua transition | **PASS (08/10)** |
| **TC-TV4-05** | Rollback và retry | Giả lập lỗi khi ghi chi tiết, đối chiếu snapshot rồi retry | Khôi phục header/chi tiết và retry thành công | **PASS (08/10)** |
| **TC-TV4-06** | Concurrency cùng kỳ | Hai session cùng tính một tháng/năm | Một header, cùng `MaBangLuong`, không duplicate | **PASS (07/10; log lịch sử)** |
| **TC-TV4-07** | Benchmark truy vấn nguồn | Đo truy vấn chấm công, phụ cấp và khấu trừ | Kết quả nguồn đúng; scan/seek khấu trừ tương đương | **PASS (08/10; fixture)** |

Chi tiết môi trường, số đo và kịch bản demo nằm tại `docs/TV4_Payroll_Test_Report.md`.
