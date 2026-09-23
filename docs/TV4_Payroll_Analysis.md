# TV4 – Phân tích & Thiết kế Module Tính Lương
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Nguyễn Quang Vinh (TV4, MSSV 24110385)  
**Ngày:** 23/09/2026  
**Phiên bản:** 1.0

---

## 1. Đặc tả nghiệp vụ tính lương

### 1.1 Mô tả tổng quan

Module tính lương chịu trách nhiệm **tính toán tiền lương hằng tháng** cho toàn bộ nhân viên đang làm việc. Luồng xử lý tính lương **thu thập dữ liệu nguồn** từ ba module:

| Nguồn dữ liệu | Bảng SQL Server | Module phụ trách | Thông tin cần lấy |
|---|---|---|---|
| Hồ sơ nhân viên | `NHANVIEN` | TV1 – Nguyễn Minh Trí | Lương cơ bản (`LuongCoBan`), trạng thái (`TrangThai`) |
| Chấm công | `CHAMCONG` | TV2 – Phạm Minh Quân | Số ngày công thực tế trong tháng |
| Phụ cấp | `PHUCAPNHANVIEN` | TV3 – Trần Tiến Đạt | Tổng phụ cấp theo nhân viên – tháng – năm |
| Khấu trừ | `KHAUTRUNHANVIEN` | TV3 – Trần Tiến Đạt | Tổng khấu trừ theo nhân viên – tháng – năm |

### 1.2 Tác nhân liên quan

| Tác nhân | Vai trò SQL Server | Hành động cho phép |
|---|---|---|
| **Payroll_Officer** | `role_PayrollOfficer` | Tính bảng lương, xem bảng lương |
| **DB_Admin** | `role_DBAdmin` | Toàn quyền (bao gồm tính lương) |
| **HR_Manager** | `role_HRManager` | **Không** được tính lương (DENY) |
| **Employee** | `role_Employee` | **Không** được tính lương; chỉ xem phiếu lương cá nhân qua View |

> Tham chiếu: Ma trận phân quyền chi tiết tại [TV5_Security_Design.md](TV5_Security_Design.md) và [TV5_Architecture.md](TV5_Architecture.md#3.3).

### 1.3 Điều kiện tiên quyết trước khi tính lương

Trước khi `Payroll_Officer` thực hiện tính lương tháng M, năm Y, hệ thống phải đảm bảo:

1. **Chưa tồn tại kỳ lương** cho tháng M, năm Y trong bảng `BANGLUONG`.
2. **Tồn tại dữ liệu chấm công** của tháng M, năm Y trong bảng `CHAMCONG`.
3. **Nhân viên phải có trạng thái `DANG_LAM`** trong bảng `NHANVIEN`.
4. **Dữ liệu phụ cấp/khấu trừ** tháng M, năm Y đã được nhập đầy đủ (nếu có).

### 1.4 Luồng nghiệp vụ tổng quan

```
Payroll_Officer nhấn "Tính lương"
    │
    ├── Nhập: Tháng (1–12), Năm, Số ngày công chuẩn (mặc định 26)
    │
    ├── Kiểm tra kỳ lương đã tồn tại? → Nếu có → Thông báo "Kỳ lương đã tồn tại"
    │
    ├── Kiểm tra kỳ lương đã chốt? → Nếu đã chốt → Thông báo "Không thể tính lại kỳ đã chốt"
    │
    ├── Tạo bản ghi BANGLUONG (TrangThai = 'CHUA_CHOT')
    │
    ├── Với MỖI nhân viên có TrangThai = 'DANG_LAM':
    │   ├── Lấy LuongCoBan từ NHANVIEN
    │   ├── Đếm số ngày công thực tế từ CHAMCONG (tháng M, năm Y)
    │   ├── Tính TienCong = fn_TinhTienCong(LuongCoBan, NgayCongChuẩn, NgayCongThucTe)
    │   ├── Lấy TongPhuCap = fn_TongPhuCap(MaNV, Thang, Nam)     ← TV2 sở hữu
    │   ├── Lấy TongKhauTru = fn_TongKhauTru(MaNV, Thang, Nam)   ← TV3 sở hữu
    │   ├── Tính ThucNhan = fn_TinhThucNhan(TienCong, TongPhuCap, TongKhauTru) ← TV5 sở hữu
    │   └── INSERT vào CHITIETBANGLUONG
    │
    └── COMMIT → Hiển thị kết quả trên BangLuongPanel
```

---

## 2. Công thức tính lương chi tiết

### 2.1 Các đại lượng

| Ký hiệu | Đại lượng | Nguồn | Kiểu dữ liệu |
|---|---|---|---|
| `LCB` | Lương cơ bản | `NHANVIEN.LuongCoBan` | DECIMAL(15,2) |
| `NCC` | Số ngày công chuẩn | Tham số đầu vào (mặc định = 26) | INT |
| `NCT` | Ngày công thực tế | Đếm từ `CHAMCONG` theo MaNV/Tháng/Năm | INT |
| `TPC` | Tổng phụ cấp | `fn_TongPhuCap(MaNV, Thang, Nam)` | DECIMAL(15,2) |
| `TKT` | Tổng khấu trừ | `fn_TongKhauTru(MaNV, Thang, Nam)` | DECIMAL(15,2) |

### 2.2 Công thức

```
Bước 1:  Tiền công = (LCB / NCC) × NCT
         → fn_TinhTienCong(@LuongCoBan, @NgayCongChuan, @NgayCongThucTe)
         → Kết quả DECIMAL(15,2), làm tròn 2 chữ số thập phân

Bước 2:  Thực nhận = Tiền công + TPC − TKT
         → fn_TinhThucNhan(@TienCong, @TongPhuCap, @TongKhauTru)
         → Kết quả DECIMAL(15,2)
```

### 2.3 Ví dụ minh họa

| Nhân viên | LCB | NCC | NCT | Tiền công | TPC | TKT | Thực nhận |
|---|---|---|---|---|---|---|---|
| NV001 | 15.000.000 | 26 | 24 | 13.846.153,85 | 2.000.000 | 500.000 | 15.346.153,85 |
| NV002 | 10.000.000 | 26 | 26 | 10.000.000,00 | 1.500.000 | 300.000 | 11.200.000,00 |
| NV003 | 12.000.000 | 26 | 20 | 9.230.769,23 | 0 | 1.000.000 | 8.230.769,23 |
| NV004 | 8.000.000 | 26 | 0 | 0,00 | 500.000 | 200.000 | 300.000,00 |

### 2.4 Ràng buộc nghiệp vụ

- `LCB > 0` – bắt buộc tại bảng `NHANVIEN` (CHECK constraint do TV1).
- `NCC > 0` – phải lớn hơn 0 để tránh chia cho 0.
- `NCT >= 0` – có thể bằng 0 nếu nhân viên không đi làm.
- `TPC >= 0`, `TKT >= 0` – không âm (CHECK constraint do TV3).
- `ThucNhan` **có thể âm** nếu khấu trừ lớn hơn tiền công + phụ cấp → hệ thống vẫn ghi nhận, Payroll_Officer sẽ xem xét trước khi chốt.

---

## 3. Thiết kế bảng BANGLUONG

### 3.1 DDL

```sql
CREATE TABLE BANGLUONG (
    MaBangLuong   INT           IDENTITY(1,1)  PRIMARY KEY,
    Thang         INT           NOT NULL
        CONSTRAINT CHK_BANGLUONG_Thang CHECK (Thang BETWEEN 1 AND 12),
    Nam           INT           NOT NULL
        CONSTRAINT CHK_BANGLUONG_Nam CHECK (Nam BETWEEN 2020 AND 2100),
    NgayCongChuan INT           NOT NULL        DEFAULT 26
        CONSTRAINT CHK_BANGLUONG_NgayCongChuan CHECK (NgayCongChuan > 0),
    TrangThai     VARCHAR(15)   NOT NULL        DEFAULT 'CHUA_CHOT'
        CONSTRAINT CHK_BANGLUONG_TrangThai CHECK (TrangThai IN ('CHUA_CHOT', 'DA_CHOT')),
    NgayTao       DATETIME      NOT NULL        DEFAULT GETDATE(),
    NgayChot      DATETIME      NULL,

    CONSTRAINT UQ_BANGLUONG_ThangNam UNIQUE (Thang, Nam)
);
```

### 3.2 Mô tả từng cột

| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `MaBangLuong` | INT IDENTITY | PK, auto-increment | Khóa chính, tự tăng |
| `Thang` | INT | CHECK 1–12, NOT NULL | Tháng của kỳ lương |
| `Nam` | INT | CHECK 2020–2100, NOT NULL | Năm của kỳ lương |
| `NgayCongChuan` | INT | CHECK > 0, DEFAULT 26 | Số ngày công chuẩn của kỳ (thường 26) |
| `TrangThai` | VARCHAR(15) | CHECK, DEFAULT `CHUA_CHOT` | `CHUA_CHOT` hoặc `DA_CHOT` |
| `NgayTao` | DATETIME | DEFAULT GETDATE() | Thời điểm tạo bảng lương |
| `NgayChot` | DATETIME | NULL | Thời điểm chốt (NULL nếu chưa chốt) |

### 3.3 Constraint đặc biệt

| Constraint | Loại | Mục đích |
|---|---|---|
| `UQ_BANGLUONG_ThangNam` | UNIQUE(Thang, Nam) | Mỗi tháng/năm chỉ có đúng 1 kỳ lương → ngăn tạo trùng |
| `CHK_BANGLUONG_Thang` | CHECK | Tháng hợp lệ 1–12 |
| `CHK_BANGLUONG_Nam` | CHECK | Năm hợp lệ 2020–2100 |
| `CHK_BANGLUONG_NgayCongChuan` | CHECK | Tránh chia cho 0 khi tính tiền công |
| `CHK_BANGLUONG_TrangThai` | CHECK | Chỉ cho phép 2 trạng thái hợp lệ |

---

## 4. Thiết kế bảng CHITIETBANGLUONG

### 4.1 DDL

```sql
CREATE TABLE CHITIETBANGLUONG (
    MaChiTiet     INT           IDENTITY(1,1)  PRIMARY KEY,
    MaBangLuong   INT           NOT NULL,
    MaNV          INT           NOT NULL,
    LuongCoBan    DECIMAL(15,2) NOT NULL
        CONSTRAINT CHK_CTBL_LuongCoBan CHECK (LuongCoBan >= 0),
    NgayCongThucTe INT          NOT NULL        DEFAULT 0
        CONSTRAINT CHK_CTBL_NgayCongThucTe CHECK (NgayCongThucTe >= 0),
    TienCong      DECIMAL(15,2) NOT NULL        DEFAULT 0
        CONSTRAINT CHK_CTBL_TienCong CHECK (TienCong >= 0),
    TongPhuCap    DECIMAL(15,2) NOT NULL        DEFAULT 0
        CONSTRAINT CHK_CTBL_TongPhuCap CHECK (TongPhuCap >= 0),
    TongKhauTru   DECIMAL(15,2) NOT NULL        DEFAULT 0
        CONSTRAINT CHK_CTBL_TongKhauTru CHECK (TongKhauTru >= 0),
    ThucNhan      DECIMAL(15,2) NOT NULL        DEFAULT 0,

    CONSTRAINT FK_CTBL_BANGLUONG
        FOREIGN KEY (MaBangLuong) REFERENCES BANGLUONG(MaBangLuong),
    CONSTRAINT FK_CTBL_NHANVIEN
        FOREIGN KEY (MaNV) REFERENCES NHANVIEN(MaNV),
    CONSTRAINT UQ_CTBL_BangLuong_NhanVien
        UNIQUE (MaBangLuong, MaNV)
);
```

### 4.2 Mô tả từng cột

| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| `MaChiTiet` | INT IDENTITY | PK, auto-increment | Khóa chính, tự tăng |
| `MaBangLuong` | INT | FK → BANGLUONG, NOT NULL | Tham chiếu kỳ lương |
| `MaNV` | INT | FK → NHANVIEN, NOT NULL | Tham chiếu nhân viên |
| `LuongCoBan` | DECIMAL(15,2) | CHECK >= 0 | Lương cơ bản tại thời điểm tính (snapshot) |
| `NgayCongThucTe` | INT | CHECK >= 0, DEFAULT 0 | Số ngày công thực tế trong tháng |
| `TienCong` | DECIMAL(15,2) | CHECK >= 0, DEFAULT 0 | Kết quả tính: `(LCB / NCC) × NCT` |
| `TongPhuCap` | DECIMAL(15,2) | CHECK >= 0, DEFAULT 0 | Tổng phụ cấp của NV trong tháng |
| `TongKhauTru` | DECIMAL(15,2) | CHECK >= 0, DEFAULT 0 | Tổng khấu trừ của NV trong tháng |
| `ThucNhan` | DECIMAL(15,2) | DEFAULT 0, **không CHECK** | Thực nhận = TienCong + TongPhuCap − TongKhauTru (có thể âm) |

### 4.3 Constraint đặc biệt

| Constraint | Loại | Mục đích |
|---|---|---|
| `UQ_CTBL_BangLuong_NhanVien` | UNIQUE(MaBangLuong, MaNV) | Mỗi nhân viên chỉ có đúng 1 chi tiết trong 1 kỳ lương |
| `FK_CTBL_BANGLUONG` | FOREIGN KEY | Đảm bảo chi tiết thuộc về kỳ lương hợp lệ |
| `FK_CTBL_NHANVIEN` | FOREIGN KEY | Đảm bảo chi tiết thuộc về nhân viên hợp lệ |
| `CHK_CTBL_LuongCoBan` | CHECK >= 0 | Snapshot lương cơ bản không âm |

### 4.4 Lý do snapshot LuongCoBan vào CHITIETBANGLUONG

Khi tính lương, giá trị `LuongCoBan` được **sao chép (snapshot)** từ `NHANVIEN` vào `CHITIETBANGLUONG`. Lý do:

- Nếu sau khi tính lương, nhân viên được điều chỉnh lương → kỳ lương cũ vẫn phản ánh đúng mức lương **tại thời điểm tính**.
- Sau khi chốt (`TrangThai = 'DA_CHOT'`), dữ liệu trong `CHITIETBANGLUONG` trở thành bất biến, phục vụ tra cứu và báo cáo.

---

## 5. Sơ đồ luồng tính lương

### 5.1 Luồng xử lý chính (Flowchart)

```
┌──────────────────────────────┐
│  Payroll_Officer nhấn        │
│  "Tính lương tháng"          │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  Nhập: Tháng, Năm,          │
│  Số ngày công chuẩn (=26)   │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐      ┌─────────────────────────┐
│  sp_TinhBangLuongThang       │      │                         │
│  BEGIN TRY                   │      │                         │
│  BEGIN TRANSACTION           │      │                         │
└─────────────┬────────────────┘      │                         │
              ▼                       │                         │
┌──────────────────────────────┐  CÓ  │  RAISERROR:             │
│  SELECT FROM BANGLUONG       ├──────►  "Kỳ lương tháng M/Y    │
│  WHERE Thang=M AND Nam=Y     │      │   đã tồn tại"           │
│  → Kỳ lương đã tồn tại?     │      │  ROLLBACK               │
└─────────────┬────────────────┘      └─────────────────────────┘
              │ CHƯA CÓ
              ▼
┌──────────────────────────────┐
│  INSERT INTO BANGLUONG       │
│  (Thang, Nam, NgayCongChuan, │
│   TrangThai='CHUA_CHOT')     │
│  → Lấy @MaBangLuong (SCOPE_ │
│    IDENTITY)                 │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  DECLARE CURSOR cho          │
│  SELECT MaNV, LuongCoBan     │
│  FROM NHANVIEN               │
│  WHERE TrangThai = 'DANG_LAM'│
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  FETCH NEXT → @MaNV, @LCB   │◄─────────────────────────┐
└─────────────┬────────────────┘                          │
              ▼                                           │
┌──────────────────────────────┐                          │
│  @NCT = fn_TinhSoNgayCong   │                          │
│         (@MaNV, @Thang,@Nam) │  ← TV1 sở hữu function  │
│  @TC  = fn_TinhTienCong      │                          │
│         (@LCB, @NCC, @NCT)   │  ← TV4 sở hữu function  │
│  @TPC = fn_TongPhuCap        │                          │
│         (@MaNV, @Thang,@Nam) │  ← TV2 sở hữu function  │
│  @TKT = fn_TongKhauTru       │                          │
│         (@MaNV, @Thang,@Nam) │  ← TV3 sở hữu function  │
│  @TN  = fn_TinhThucNhan      │                          │
│         (@TC, @TPC, @TKT)    │  ← TV5 sở hữu function  │
└─────────────┬────────────────┘                          │
              ▼                                           │
┌──────────────────────────────┐                          │
│  INSERT INTO CHITIETBANGLUONG│                          │
│  (MaBangLuong, MaNV,         │                          │
│   LuongCoBan, NgayCongThucTe,│                          │
│   TienCong, TongPhuCap,      │                          │
│   TongKhauTru, ThucNhan)     │                          │
└─────────────┬────────────────┘                          │
              ▼                                           │
┌──────────────────────────────┐  CÒN NV                  │
│  FETCH NEXT                  ├──────────────────────────┘
│  → Hết nhân viên?           │
└─────────────┬────────────────┘
              │ HẾT
              ▼
┌──────────────────────────────┐
│  CLOSE CURSOR                │
│  DEALLOCATE CURSOR           │
│  COMMIT TRANSACTION ✓        │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  Java: PayrollService nhận   │
│  kết quả → cập nhật          │
│  BangLuongPanel              │
│  → Hiển thị JOptionPane      │
│    "Tính lương thành công"   │
└──────────────────────────────┘
```

### 5.2 Luồng xử lý lỗi (Exception Flow)

```
┌──────────────────────────────┐
│  Bất kỳ lỗi nào xảy ra      │
│  trong BEGIN TRY block       │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  BEGIN CATCH                 │
│  IF @@TRANCOUNT > 0          │
│      ROLLBACK TRANSACTION    │
│  RAISERROR(message, 16, 1)   │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  Java DAO: catch SQLException│
│  → ném lên PayrollService    │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  PayrollService: catch       │
│  Exception → ném lên UI      │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  BangLuongPanel:             │
│  JOptionPane.showMessageDlg  │
│  ("Lỗi: " + e.getMessage()) │
└──────────────────────────────┘
```

---

## 6. Kịch bản Transaction / Rollback

### 6.1 Tình huống 1: Tính lương thành công

```
Đầu vào:   Tháng=9, Năm=2026, NgayCongChuẩn=26
Tiên quyết: Chưa có kỳ lương tháng 9/2026; có 5 nhân viên DANG_LAM; chấm công đã nhập đủ.

Bước 1:  sp_TinhBangLuongThang(@Thang=9, @Nam=2026, @NgayCongChuan=26)
Bước 2:  BEGIN TRANSACTION
Bước 3:  SELECT → không tìm thấy kỳ tháng 9/2026 → tiếp tục
Bước 4:  INSERT vào BANGLUONG → @MaBangLuong = SCOPE_IDENTITY()
Bước 5:  Lặp 5 nhân viên → mỗi NV: tính công, phụ cấp, khấu trừ, thực nhận → INSERT CHITIETBANGLUONG
Bước 6:  COMMIT TRANSACTION ✓
Bước 7:  Java nhận thành công → hiển thị bảng lương tháng 9/2026

Kết quả: BANGLUONG có 1 dòng mới (Tháng=9, Năm=2026, TrangThai='CHUA_CHOT')
         CHITIETBANGLUONG có 5 dòng mới (1 dòng/nhân viên)
```

### 6.2 Tình huống 2: Kỳ lương đã tồn tại

```
Đầu vào:   Tháng=9, Năm=2026 (đã tính trước đó)

Bước 1:  sp_TinhBangLuongThang(@Thang=9, @Nam=2026, @NgayCongChuan=26)
Bước 2:  BEGIN TRANSACTION
Bước 3:  SELECT → tìm thấy kỳ tháng 9/2026 đã tồn tại
         → RAISERROR(N'Kỳ lương tháng 9/2026 đã tồn tại. Vui lòng xóa kỳ cũ nếu muốn tính lại.', 16, 1)
Bước 4:  CATCH → ROLLBACK TRANSACTION

Kết quả: Không có thay đổi dữ liệu. Java hiển thị thông báo lỗi.
```

### 6.3 Tình huống 3: Kỳ lương đã chốt

```
Đầu vào:   Tháng=8, Năm=2026 (đã chốt trước đó)

Bước 1:  sp_TinhBangLuongThang(@Thang=8, @Nam=2026, @NgayCongChuan=26)
Bước 2:  BEGIN TRANSACTION
Bước 3:  SELECT → tìm thấy kỳ tháng 8/2026 với TrangThai = 'DA_CHOT'
         → RAISERROR(N'Kỳ lương tháng 8/2026 đã được chốt. Không thể tính lại.', 16, 1)
Bước 4:  CATCH → ROLLBACK TRANSACTION

Kết quả: Không có thay đổi dữ liệu. Java hiển thị thông báo lỗi.

GHI CHÚ: Trigger trg_BangLuong_KhongSuaKhiDaChot (TV4 sở hữu) sẽ chặn ở tầng DB
         nếu có bất kỳ attempt nào UPDATE/DELETE bản ghi BANGLUONG đã chốt.
```

### 6.4 Tình huống 4: Lỗi giữa chừng khi insert chi tiết (Rollback toàn bộ)

```
Đầu vào:   Tháng=10, Năm=2026, NgayCongChuẩn=26
Giả lập:   Có 5 nhân viên; insert thành công 3 dòng chi tiết; dòng thứ 4 gặp lỗi
           (ví dụ: FK violation vì MaNV bị xóa giữa chừng, hoặc constraint violation)

Bước 1:  sp_TinhBangLuongThang(@Thang=10, @Nam=2026, @NgayCongChuan=26)
Bước 2:  BEGIN TRANSACTION
Bước 3:  INSERT vào BANGLUONG → @MaBangLuong = SCOPE_IDENTITY()
Bước 4:  INSERT CHITIETBANGLUONG cho NV001 → ✓
Bước 5:  INSERT CHITIETBANGLUONG cho NV002 → ✓
Bước 6:  INSERT CHITIETBANGLUONG cho NV003 → ✓
Bước 7:  INSERT CHITIETBANGLUONG cho NV004 → ✗ LỖI!
Bước 8:  CATCH → IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION
         → RAISERROR(N'Lỗi khi tạo chi tiết lương cho nhân viên NV004. Toàn bộ kỳ lương đã bị hủy.', 16, 1)

Kết quả: ROLLBACK toàn bộ → BANGLUONG không có dòng mới, CHITIETBANGLUONG cũng không có dòng mới.
         Dữ liệu trở về trạng thái trước khi gọi SP.
         Java hiển thị thông báo lỗi chi tiết.
```

### 6.5 Bảng tổng hợp kịch bản

| # | Tình huống | Transaction kết quả | BANGLUONG | CHITIETBANGLUONG | Thông báo |
|---|---|---|---|---|---|
| 1 | Thành công | COMMIT | +1 dòng mới | +N dòng mới | "Tính lương thành công" |
| 2 | Kỳ đã tồn tại | ROLLBACK | Không đổi | Không đổi | "Kỳ lương đã tồn tại" |
| 3 | Kỳ đã chốt | ROLLBACK | Không đổi | Không đổi | "Kỳ lương đã chốt" |
| 4 | Lỗi giữa insert | ROLLBACK | Không đổi | Không đổi | "Lỗi khi tạo chi tiết…" |

---

## 7. Đặc tả SQL Objects TV4 sở hữu

Theo [ma trận ownership](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md#4-ma-trận-ownership), TV4 chịu trách nhiệm:

### 7.1 Stored Procedure: `sp_TinhBangLuongThang`

```
Tên:        sp_TinhBangLuongThang
Mục đích:   Tính bảng lương cho toàn bộ nhân viên đang làm việc trong 1 kỳ (tháng/năm).
Loại:       Stored Procedure với TRY…CATCH + Transaction

Tham số INPUT:
  @Thang         INT         – Tháng cần tính (1–12)
  @Nam           INT         – Năm cần tính
  @NgayCongChuan INT = 26    – Số ngày công chuẩn (mặc định 26)

Tham số OUTPUT:
  @MaBangLuong   INT OUTPUT  – Mã bảng lương vừa tạo (để Java lấy kết quả hiển thị)

Logic:
  1. Kiểm tra kỳ đã tồn tại → RAISERROR nếu có
  2. Kiểm tra kỳ đã chốt → RAISERROR nếu DA_CHOT
  3. INSERT BANGLUONG → lấy @MaBangLuong = SCOPE_IDENTITY()
  4. CURSOR qua NHANVIEN WHERE TrangThai = 'DANG_LAM'
     - Với mỗi NV: gọi fn_TinhTienCong, fn_TongPhuCap, fn_TongKhauTru, fn_TinhThucNhan
     - INSERT CHITIETBANGLUONG
  5. COMMIT nếu thành công; ROLLBACK trong CATCH nếu lỗi

Dependency:
  - fn_TinhSoNgayCong (TV1)
  - fn_TinhTienCong (TV4 – chính mình)
  - fn_TongPhuCap (TV2)
  - fn_TongKhauTru (TV3)
  - fn_TinhThucNhan (TV5)
```

### 7.2 Function: `fn_TinhTienCong`

```
Tên:        fn_TinhTienCong
Mục đích:   Tính tiền công = (LuongCoBan / NgayCongChuan) × NgayCongThucTe
Loại:       Scalar-valued Function

Tham số:
  @LuongCoBan       DECIMAL(15,2)    – Lương cơ bản của nhân viên
  @NgayCongChuan     INT              – Số ngày công chuẩn (> 0)
  @NgayCongThucTe    INT              – Số ngày công thực tế (>= 0)

Trả về:     DECIMAL(15,2) – Tiền công

Logic:
  IF @NgayCongChuan <= 0 RETURN 0    -- Phòng trường hợp chia 0
  IF @NgayCongThucTe <= 0 RETURN 0   -- Không đi làm → tiền công = 0
  RETURN ROUND((@LuongCoBan / @NgayCongChuan) * @NgayCongThucTe, 2)
```

### 7.3 Trigger: `trg_BangLuong_KhongSuaKhiDaChot`

```
Tên:        trg_BangLuong_KhongSuaKhiDaChot
Bảng:       BANGLUONG
Sự kiện:    INSTEAD OF UPDATE, DELETE
Mục đích:   Ngăn chặn mọi thao tác UPDATE hoặc DELETE trên bản ghi BANGLUONG đã có
            TrangThai = 'DA_CHOT'.

Logic:
  - Với UPDATE: Kiểm tra bản ghi trong bảng deleted (giá trị cũ).
    Nếu TrangThai cũ = 'DA_CHOT' → RAISERROR, không cho UPDATE.
    Ngoại lệ: Cho phép UPDATE từ 'CHUA_CHOT' → 'DA_CHOT' (tức là thao tác chốt lương do sp_ChotBangLuong thực hiện).
  - Với DELETE: Kiểm tra bản ghi trong bảng deleted.
    Nếu TrangThai = 'DA_CHOT' → RAISERROR, không cho DELETE.
    Nếu TrangThai = 'CHUA_CHOT' → cho phép DELETE (xóa kỳ chưa chốt – nghiệp vụ TV3).

GHI CHÚ: Trigger trg_ChiTietLuong_KhongSuaKhiDaChot (TV5 sở hữu) sẽ bảo vệ
         bảng CHITIETBANGLUONG theo logic tương tự.
```

### 7.4 View: `vw_TongKhauTruThang`

```
Tên:        vw_TongKhauTruThang
Mục đích:   Tổng hợp tổng khấu trừ theo nhân viên – tháng – năm.
            Phục vụ tra cứu nhanh và dùng trong sp_TinhBangLuongThang.

Cột trả về:
  - MaNV       INT
  - HoTen      NVARCHAR
  - Thang      INT
  - Nam        INT
  - TongKhauTru DECIMAL(15,2)

Logic:
  SELECT ktn.MaNV, nv.HoTen, ktn.Thang, ktn.Nam,
         SUM(ktn.SoTien) AS TongKhauTru
  FROM KHAUTRUNHANVIEN ktn
  JOIN NHANVIEN nv ON ktn.MaNV = nv.MaNV
  GROUP BY ktn.MaNV, nv.HoTen, ktn.Thang, ktn.Nam
```

### 7.5 Index: `IX_KHAUTRU_MaNV_ThangNam`

```
Tên:        IX_KHAUTRU_MaNV_ThangNam
Bảng:       KHAUTRUNHANVIEN
Mục đích:   Tối ưu truy vấn tổng khấu trừ theo nhân viên + tháng + năm
            (truy vấn thường xuyên nhất trong quá trình tính lương).

DDL:
  CREATE NONCLUSTERED INDEX IX_KHAUTRU_MaNV_ThangNam
  ON KHAUTRUNHANVIEN (MaNV, Thang, Nam)
  INCLUDE (SoTien);

Lý do INCLUDE(SoTien):
  - Truy vấn SUM(SoTien) WHERE MaNV=@MaNV AND Thang=@Thang AND Nam=@Nam
    sẽ được phục vụ hoàn toàn từ index (covering index), không cần key lookup.
  - Đo hiệu năng bằng Execution Plan + SET STATISTICS IO, TIME ở tuần 3.
```

---

## 8. Liên kết phối hợp (Dependency Map)

### 8.1 TV4 phụ thuộc vào

| Thành viên | Đối tượng TV4 cần dùng | Mục đích |
|---|---|---|
| TV1 – Nguyễn Minh Trí | Bảng `NHANVIEN` (cấu trúc, TrangThai, LuongCoBan) | Lấy danh sách NV đang làm, lương cơ bản |
| TV1 – Nguyễn Minh Trí | `fn_TinhSoNgayCong(MaNV, Thang, Nam)` | Đếm số ngày công thực tế |
| TV2 – Phạm Minh Quân | Bảng `CHAMCONG` (dữ liệu chấm công) | Dữ liệu nguồn cho fn_TinhSoNgayCong |
| TV2 – Phạm Minh Quân | `fn_TongPhuCap(MaNV, Thang, Nam)` | Tổng phụ cấp để tính thực nhận |
| TV3 – Trần Tiến Đạt | Bảng `KHAUTRUNHANVIEN` (dữ liệu khấu trừ) | Dữ liệu nguồn cho vw_TongKhauTruThang |
| TV3 – Trần Tiến Đạt | `fn_TongKhauTru(MaNV, Thang, Nam)` | Tổng khấu trừ để tính thực nhận |
| TV5 – Trần Đức Anh | `fn_TinhThucNhan(TienCong, TongPhuCap, TongKhauTru)` | Tính thực nhận cuối cùng |

### 8.2 Các thành viên phụ thuộc vào TV4

| Thành viên | Đối tượng của TV4 được dùng | Mục đích |
|---|---|---|
| TV5 – Trần Đức Anh | Bảng `BANGLUONG` (cấu trúc, TrangThai) | `sp_ChotBangLuong` cần UPDATE TrangThai |
| TV5 – Trần Đức Anh | Bảng `CHITIETBANGLUONG` | `vw_BangLuongChiTiet` đọc chi tiết lương |
| TV5 – Trần Đức Anh | `sp_TinhBangLuongThang` phải chạy trước | `sp_ChotBangLuong` chỉ chốt kỳ đã tính |
| TV3 – Trần Tiến Đạt | Bảng `BANGLUONG` | Transaction xóa kỳ chưa chốt cần tham chiếu |
| TV4 + TV5 | Concurrency demo | Hai Payroll_Officer cùng tính/chốt 1 kỳ |

### 8.3 Sơ đồ dependency

```
TV1 (NHANVIEN, fn_TinhSoNgayCong)
        │
TV2 (CHAMCONG, fn_TongPhuCap)───────┐
        │                            │
TV3 (KHAUTRUNHANVIEN, fn_TongKhauTru)┤
        │                            ▼
        └───────────────────► TV4 (sp_TinhBangLuongThang)
                                     │
                              ┌──────┴──────┐
                              ▼             ▼
                     BANGLUONG        CHITIETBANGLUONG
                              │             │
                              └──────┬──────┘
                                     ▼
                              TV5 (sp_ChotBangLuong,
                                   vw_BangLuongChiTiet)
```

---

## Phụ lục – Tham chiếu tài liệu liên quan

| Tài liệu | Tác giả | Nội dung liên quan |
|---|---|---|
| [Ke_hoach_phan_cong_Project_DBMS_Nhom06.md](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md) | Nhóm | Ma trận ownership, timeline |
| [TV5_Architecture.md](TV5_Architecture.md) | TV5 | Kiến trúc phân lớp, quy tắc DAO/Service |
| [TV5_Security_Design.md](TV5_Security_Design.md) | TV5 | Role/Login, GRANT/DENY cho PayrollOfficer |
| [TV5_Concurrency_Security_Demo.md](TV5_Concurrency_Security_Demo.md) | TV5 | Kịch bản concurrency tính/chốt lương cùng kỳ |
| [TV5_Integration_Checklist.md](TV5_Integration_Checklist.md) | TV5 | Checklist transaction/rollback, tích hợp DAO |
