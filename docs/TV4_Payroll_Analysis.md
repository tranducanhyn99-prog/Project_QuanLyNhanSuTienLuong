# TV4 – Phân tích & Thiết kế Module Tính Lương
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Nguyễn Quang Vinh (TV4, MSSV 24110385)  
**Ngày cập nhật:** 29/09/2026
**Phiên bản:** 1.1

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
| **Payroll_Officer** | `role_PayrollOfficer` | Tính bảng lương, xem bảng lương sau khi được cấp quyền tương ứng |
| **DB_Admin** | `role_DBAdmin` | Toàn quyền (bao gồm tính lương) |
| **HR_Manager** | `role_HRManager` | Không được tính lương; quyền/`DENY` do script bảo mật TV5 quản lý |
| **Employee** | `role_Employee` | **Không** được tính lương; chỉ xem phiếu lương cá nhân qua View |

> Tham chiếu: Ma trận phân quyền chi tiết tại [TV5_Security_Design.md](TV5_Security_Design.md) và [TV5_Architecture.md](TV5_Architecture.md#3.3).

### 1.3 Điều kiện tiên quyết trước khi tính lương

Trước khi `Payroll_Officer` thực hiện tính lương tháng M, năm Y, hệ thống phải đảm bảo:

1. Kỳ lương tháng M, năm Y **chưa tồn tại hoặc đang ở trạng thái `CHUA_CHOT`**. Kỳ `DA_CHOT` không được tính lại nếu chưa mở lại theo quy trình.
2. **Tồn tại dữ liệu chấm công** của tháng M, năm Y trong bảng `CHAMCONG`.
3. **Nhân viên phải có trạng thái `DANG_LAM_VIEC`** trong bảng `NHANVIEN`.
4. **Dữ liệu phụ cấp/khấu trừ** tháng M, năm Y đã được nhập đầy đủ (nếu có).

### 1.4 Luồng nghiệp vụ tổng quan

```
Payroll_Officer nhấn "Tính lương"
    │
    ├── Nhập: Tháng (1–12), Năm, Số ngày công chuẩn (mặc định 26)
    │
    ├── Khóa kỳ theo (Tháng, Năm) bằng UPDLOCK + HOLDLOCK
    │   ├── Nếu đã chốt → Thông báo "Không thể tính lại kỳ đã chốt"
    │   └── Nếu chưa chốt → Xóa chi tiết nháp cũ để tính lại trong cùng transaction
    │
    ├── Tạo header mới hoặc cập nhật header nháp BANGLUONG (`CHUA_CHOT`)
    │
    ├── Với MỖI nhân viên có TrangThai = 'DANG_LAM_VIEC':
    │   ├── Lấy LuongCoBan từ NHANVIEN
    │   ├── Đếm ngày công trực tiếp từ CHAMCONG bằng khoảng ngày nửa mở
    │   ├── Tính TienCong = fn_TinhTienCong(LuongCoBan, NgayCongChuẩn, NgayCongThucTe)
    │   ├── Lấy phụ cấp qua fn_TongPhuCap nếu có; nếu không thì SUM(PHUCAPNHANVIEN.SoTien)
    │   ├── Lấy khấu trừ qua fn_TongKhauTru nếu có; nếu không thì SUM(KHAUTRUNHANVIEN.SoTien)
    │   ├── Tính thực nhận qua fn_TinhThucNhan nếu có; nếu không thì TienCong + TongPhuCap - TongKhauTru
    │   └── INSERT vào CHITIETBANGLUONG
    │
    └── COMMIT → Hiển thị kết quả trên BangLuongPanel
```

---

## 2. Công thức tính lương chi tiết

### 2.1 Các đại lượng

| Ký hiệu | Đại lượng | Nguồn | Kiểu dữ liệu |
|---|---|---|---|
| `LCB` | Lương cơ bản | `NHANVIEN.LuongCoBan` | DECIMAL(18,2) |
| `NCC` | Số ngày công chuẩn | Tham số đầu vào (mặc định = 26) | INT |
| `NCT` | Ngày công thực tế | Đếm từ `CHAMCONG` theo MaNV/Tháng/Năm | INT |
| `TPC` | Tổng phụ cấp | Function tùy chọn hoặc tổng `PHUCAPNHANVIEN.SoTien` | DECIMAL(18,2) |
| `TKT` | Tổng khấu trừ | Function tùy chọn hoặc tổng `KHAUTRUNHANVIEN.SoTien` | DECIMAL(18,2) |

### 2.2 Công thức

```
Bước 1:  Tiền công = (LCB / NCC) × NCT
         → fn_TinhTienCong(@LuongCoBan, @NgayCongChuan, @NgayCongThucTe)
         → Kết quả DECIMAL(18,2), làm tròn 2 chữ số thập phân

Bước 2:  Thực nhận = Tiền công + TPC − TKT
         → fn_TinhThucNhan(@TienCong, @TongPhuCap, @TongKhauTru) nếu function tồn tại;
           nếu không, procedure dùng phép cộng/trừ trực tiếp
         → Kết quả DECIMAL(18,2)
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
    LuongCoBan    DECIMAL(18,2) NOT NULL
        CONSTRAINT CHK_CTBL_LuongCoBan CHECK (LuongCoBan >= 0),
    NgayCongThucTe INT          NOT NULL        DEFAULT 0
        CONSTRAINT CHK_CTBL_NgayCongThucTe CHECK (NgayCongThucTe >= 0),
    TienCong      DECIMAL(18,2) NOT NULL        DEFAULT 0
        CONSTRAINT CHK_CTBL_TienCong CHECK (TienCong >= 0),
    TongPhuCap    DECIMAL(18,2) NOT NULL        DEFAULT 0
        CONSTRAINT CHK_CTBL_TongPhuCap CHECK (TongPhuCap >= 0),
    TongKhauTru   DECIMAL(18,2) NOT NULL        DEFAULT 0
        CONSTRAINT CHK_CTBL_TongKhauTru CHECK (TongKhauTru >= 0),
    ThucNhan      DECIMAL(18,2) NOT NULL        DEFAULT 0,

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
| `LuongCoBan` | DECIMAL(18,2) | CHECK >= 0 | Lương cơ bản tại thời điểm tính (snapshot) |
| `NgayCongThucTe` | INT | CHECK >= 0, DEFAULT 0 | Số ngày công thực tế trong tháng |
| `TienCong` | DECIMAL(18,2) | CHECK >= 0, DEFAULT 0 | Kết quả tính: `(LCB / NCC) × NCT` |
| `TongPhuCap` | DECIMAL(18,2) | CHECK >= 0, DEFAULT 0 | Tổng phụ cấp của NV trong tháng |
| `TongKhauTru` | DECIMAL(18,2) | CHECK >= 0, DEFAULT 0 | Tổng khấu trừ của NV trong tháng |
| `ThucNhan` | DECIMAL(18,2) | DEFAULT 0, **không CHECK** | Thực nhận = TienCong + TongPhuCap − TongKhauTru (có thể âm) |

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
│  BEGIN TRANSACTION nếu độc lập│     │                         │
│  hoặc SAVE TRANSACTION nếu   │      │                         │
│  caller đã có transaction    │      │                         │
└─────────────┬────────────────┘      │                         │
              ▼                       │                         │
┌──────────────────────────────┐  DA_CHOT ┌──────────────────────┐
│  SELECT ... WITH             ├─────────►│ RAISERROR + ROLLBACK │
│  (UPDLOCK, HOLDLOCK)         │          └──────────────────────┘
│  WHERE Thang=M AND Nam=Y     │
└─────────────┬────────────────┘
              │ CHƯA CÓ / CHUA_CHOT
              ▼
┌──────────────────────────────┐
│  HOLDLOCK tập nguồn của kỳ:  │
│  NHANVIEN, CHAMCONG,         │
│  PHUCAPNHANVIEN,             │
│  KHAUTRUNHANVIEN             │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  Chưa có: INSERT BANGLUONG   │
│  CHUA_CHOT: DELETE chi tiết  │
│  cũ + UPDATE ngày công chuẩn │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  DECLARE CURSOR cho          │
│  SELECT MaNV, LuongCoBan     │
│  FROM NHANVIEN               │
│ WHERE TrangThai='DANG_LAM_VIEC'│
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  FETCH NEXT → @MaNV, @LCB   │◄─────────────────────────┐
└─────────────┬────────────────┘                          │
              ▼                                           │
┌──────────────────────────────┐                          │
│  @NCT = COUNT CHAMCONG       │                          │
│  theo MaNV + khoảng ngày     │  ← truy vấn sargable    │
│  @TC  = fn_TinhTienCong      │                          │
│         (@LCB, @NCC, @NCT)   │  ← TV4 sở hữu function  │
│  @TPC = function nếu có;     │                          │
│         nếu không, SUM bảng  │  ← PHUCAPNHANVIEN       │
│  @TKT = function nếu có;     │                          │
│         nếu không, SUM bảng  │  ← KHAUTRUNHANVIEN      │
│  @TN  = function nếu có;     │                          │
│         nếu không, TC+TPC-TKT│  ← fallback số học       │
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
│  COMMIT nếu procedure tự mở  │
│  transaction; không commit   │
│  transaction của caller      │
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

Các khối Java/UI trong sơ đồ mô tả điểm tích hợp mong đợi. Bộ test payroll tự động chỉ xác minh tầng SQL/transaction/index; không tự động kiểm thử `BangLuongPanel`, DAO/Service hoặc hành vi hiển thị.

### 5.2 Luồng xử lý lỗi (Exception Flow)

```
┌──────────────────────────────┐
│  Bất kỳ lỗi nào xảy ra      │
│  trong BEGIN TRY block       │
└─────────────┬────────────────┘
              ▼
┌──────────────────────────────┐
│  BEGIN CATCH                 │
│  Tự mở transaction: rollback │
│  Caller sở hữu: rollback về  │
│  savepoint nếu còn committable│
│  Phát lại lỗi cho caller     │
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

Các tình huống 1–4 dưới đây mô tả lời gọi độc lập (`@@TRANCOUNT = 0`). Nếu procedure được gọi trong transaction ngoài, quy tắc ở mục 6.5 được áp dụng.

### 6.1 Tình huống 1: Tính lương thành công

```
Đầu vào:   Tháng=9, Năm=2026, NgayCongChuẩn=26
Tiên quyết: Chưa có kỳ lương tháng 9/2026; có 5 nhân viên DANG_LAM_VIEC; chấm công đã nhập đủ.

Bước 1:  sp_TinhBangLuongThang(@Thang=9, @Nam=2026, @NgayCongChuan=26)
Bước 2:  Procedure tự BEGIN TRANSACTION
Bước 3:  SELECT → không tìm thấy kỳ tháng 9/2026 → tiếp tục
Bước 4:  INSERT vào BANGLUONG → @MaBangLuong = SCOPE_IDENTITY()
Bước 5:  Lặp 5 nhân viên → mỗi NV: tính công, phụ cấp, khấu trừ, thực nhận → INSERT CHITIETBANGLUONG
Bước 6:  COMMIT TRANSACTION ✓
Bước 7:  Java nhận thành công → hiển thị bảng lương tháng 9/2026

Kết quả: BANGLUONG có 1 dòng mới (Tháng=9, Năm=2026, TrangThai='CHUA_CHOT')
         CHITIETBANGLUONG có 5 dòng mới (1 dòng/nhân viên)
```

### 6.2 Tình huống 2: Tính lại kỳ lương chưa chốt

```
Đầu vào:   Tháng=9, Năm=2026 (đã tính trước đó, trạng thái CHUA_CHOT)

Bước 1:  sp_TinhBangLuongThang(@Thang=9, @Nam=2026, @NgayCongChuan=26)
Bước 2:  Procedure tự BEGIN TRANSACTION
Bước 3:  SELECT WITH (UPDLOCK, HOLDLOCK) → khóa đúng kỳ tháng 9/2026
Bước 4:  Xóa chi tiết nháp cũ, cập nhật NgayCongChuan/NgayTao
Bước 5:  Tính lại toàn bộ chi tiết từ dữ liệu nguồn mới nhất
Bước 6:  COMMIT TRANSACTION

Kết quả: Giữ nguyên một dòng BANGLUONG và một chi tiết/nhân viên; dữ liệu nháp được thay thế nguyên tử.
```

### 6.3 Tình huống 3: Kỳ lương đã chốt

```
Đầu vào:   Tháng=8, Năm=2026 (đã chốt trước đó)

Bước 1:  sp_TinhBangLuongThang(@Thang=8, @Nam=2026, @NgayCongChuan=26)
Bước 2:  Procedure tự BEGIN TRANSACTION
Bước 3:  SELECT → tìm thấy kỳ tháng 8/2026 với TrangThai = 'DA_CHOT'
         → báo lỗi kỳ đã chốt, không thể tính lại
Bước 4:  CATCH → ROLLBACK TRANSACTION

Kết quả: Không có thay đổi dữ liệu. Java hiển thị thông báo lỗi.

GHI CHÚ: Trigger trg_BangLuong_KhongSuaKhiDaChot (TV4 sở hữu) chặn sửa/xóa ở tầng DB;
         ngoại lệ duy nhất là transition mở lại chính xác do sp_HuyChotBangLuong dùng.
```

### 6.4 Tình huống 4: Lỗi giữa chừng khi insert chi tiết (Rollback toàn bộ)

```
Đầu vào:   Một kỳ fixture riêng, trạng thái CHUA_CHOT
Giả lập:   Trigger test chỉ cho session E2E ném lỗi khi INSERT CHITIETBANGLUONG

Bước 1:  sp_TinhBangLuongThang(@Thang=10, @Nam=2026, @NgayCongChuan=26)
Bước 2:  Procedure tự BEGIN TRANSACTION
Bước 3:  Khóa kỳ, xóa chi tiết nháp và cập nhật header trong transaction
Bước 4:  INSERT CHITIETBANGLUONG → trigger fixture ném lỗi
Bước 5:  CATCH → ROLLBACK transaction do procedure sở hữu → phát lại lỗi

Kết quả: ROLLBACK toàn bộ → header và toàn bộ chi tiết khớp snapshot trước lời gọi;
         không còn bất kỳ thay đổi dở dang nào của lần tính lỗi.
         Java hiển thị thông báo lỗi chi tiết.
```

### 6.5 Procedure tham gia transaction của caller

```
Bước 1:  Caller BEGIN TRANSACTION và đang dùng XACT_ABORT ON
Bước 2:  Procedure ghi nhận @@TRANCOUNT, tạm dùng XACT_ABORT OFF và SAVE TRANSACTION
Bước 3a: Thành công → không COMMIT transaction của caller
Bước 3b: Có lỗi, XACT_STATE() = 1 → ROLLBACK về savepoint, phát lại lỗi
Bước 4:  Trở về caller với @@TRANCOUNT và cấu hình XACT_ABORT ban đầu
```

E2E xác minh transaction ngoài vẫn ở `XACT_STATE() = 1`, số transaction không đổi và dữ liệu của caller không bị rollback ngoài ý muốn. Nếu SQL Server đã đưa transaction vào trạng thái `XACT_STATE() = -1`, savepoint không thể cứu transaction; caller vẫn là nơi chịu trách nhiệm rollback toàn bộ.

### 6.6 Bảng tổng hợp kịch bản

| # | Tình huống | Transaction kết quả | BANGLUONG | CHITIETBANGLUONG | Thông báo |
|---|---|---|---|---|---|
| 1 | Thành công | COMMIT | +1 dòng mới | +N dòng mới | "Tính lương thành công" |
| 2 | Tính lại kỳ `CHUA_CHOT` | COMMIT | Giữ nguyên 1 kỳ | Thay thế nguyên tử | "Cập nhật thành công" |
| 3 | Kỳ đã chốt | ROLLBACK | Không đổi | Không đổi | "Kỳ lương đã chốt" |
| 4 | Lỗi giữa insert | ROLLBACK | Không đổi | Không đổi | "Lỗi khi tạo chi tiết…" |
| 5 | Lỗi khi tham gia transaction ngoài | ROLLBACK savepoint | Caller quyết định commit/rollback | Caller quyết định commit/rollback | Phát lại lỗi cho caller |

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
  1. Khóa phạm vi kỳ theo (Thang, Nam) bằng UPDLOCK + HOLDLOCK
  2. Nếu DA_CHOT → RAISERROR; nếu CHUA_CHOT → xóa chi tiết cũ để tính lại
  3. Nếu chưa tồn tại → INSERT BANGLUONG và lấy @MaBangLuong = SCOPE_IDENTITY()
  4. CURSOR qua NHANVIEN WHERE TrangThai = 'DANG_LAM_VIEC'
     - Ngày công: đếm trực tiếp CHAMCONG theo MaNV và khoảng ngày nửa mở
     - Phụ cấp: ưu tiên fn_TongPhuCap; fallback SUM(PHUCAPNHANVIEN.SoTien)
     - Khấu trừ: ưu tiên fn_TongKhauTru; fallback SUM(KHAUTRUNHANVIEN.SoTien)
     - Tiền công luôn dùng fn_TinhTienCong
     - Thực nhận: ưu tiên fn_TinhThucNhan; fallback TienCong + TongPhuCap - TongKhauTru
     - INSERT CHITIETBANGLUONG
  5. Nếu tự mở transaction: COMMIT khi thành công, ROLLBACK toàn bộ khi lỗi
     Nếu caller đã có transaction: không COMMIT; khi lỗi chỉ ROLLBACK savepoint nếu transaction còn committable

Dependency bắt buộc:
  - NHANVIEN, CHAMCONG, BANGLUONG, CHITIETBANGLUONG
  - fn_TinhTienCong (TV4)

Dependency tùy chọn có fallback:
  - fn_TongPhuCap (TV2; chưa có trong script hiện tại nên dùng bảng PHUCAPNHANVIEN)
  - fn_TongKhauTru (TV3; chưa có trong script hiện tại nên dùng bảng KHAUTRUNHANVIEN)
  - fn_TinhThucNhan (TV5)

vw_TongKhauTruThang không được gọi trong sp_TinhBangLuongThang.
```

### 7.2 Function: `fn_TinhTienCong`

```
Tên:        fn_TinhTienCong
Mục đích:   Tính tiền công = (LuongCoBan / NgayCongChuan) × NgayCongThucTe
Loại:       Scalar-valued Function

Tham số:
  @LuongCoBan       DECIMAL(18,2)    – Lương cơ bản của nhân viên
  @NgayCongChuan     INT              – Số ngày công chuẩn (> 0)
  @NgayCongThucTe    INT              – Số ngày công thực tế (>= 0)

Trả về:     DECIMAL(18,2) – Tiền công

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
Mục đích:   Giữ bất biến kỳ DA_CHOT, ngoại trừ transition mở lại có kiểm soát
            DA_CHOT -> CHUA_CHOT do sp_HuyChotBangLuong thực hiện.

Logic:
  - Với UPDATE: Kiểm tra bản ghi trong bảng deleted (giá trị cũ).
    Nếu trạng thái cũ = DA_CHOT, chỉ cho phép đúng transition DA_CHOT -> CHUA_CHOT,
    đặt NgayChot = NULL và giữ nguyên Thang, Nam, NgayCongChuan, NgayTao.
    Mọi UPDATE khác trên kỳ DA_CHOT đều bị chặn.
    UPDATE từ CHUA_CHOT -> DA_CHOT vẫn được phép để sp_ChotBangLuong chốt kỳ.
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
            Phục vụ tra cứu/báo cáo; phiên bản hiện tại của sp_TinhBangLuongThang
            không đọc view này mà dùng function tùy chọn hoặc SUM trực tiếp trên bảng.

Cột trả về:
  - MaNV       INT
  - HoTen      NVARCHAR
  - Thang      INT
  - Nam        INT
  - TongKhauTru DECIMAL(18,2)

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
  - Benchmark payroll dùng Execution Plan + SET STATISTICS IO, TIME tại
    `database/tests/TV4_Payroll_Benchmark.sql`.
```

---

## 8. Liên kết phối hợp (Dependency Map)

### 8.1 TV4 phụ thuộc vào

| Thành viên | Đối tượng TV4 cần dùng | Mục đích |
|---|---|---|
| TV1 – Nguyễn Minh Trí | Bảng `NHANVIEN` | Dependency bắt buộc: lấy nhân viên đang làm và lương cơ bản |
| TV1 – Nguyễn Minh Trí | `fn_TinhSoNgayCong(MaNV, Thang, Nam)` | Không còn nằm trên đường thực thi TV4; procedure đếm trực tiếp để dùng predicate ngày sargable |
| TV2 – Phạm Minh Quân | Bảng `CHAMCONG` | Dependency bắt buộc: kiểm tra kỳ có dữ liệu và làm nguồn tính ngày công |
| TV2 – Phạm Minh Quân | `fn_TongPhuCap(MaNV, Thang, Nam)` | Tùy chọn; script hiện tại chưa có nên fallback sang `SUM(PHUCAPNHANVIEN.SoTien)` |
| TV3 – Trần Tiến Đạt | Bảng `PHUCAPNHANVIEN`, `KHAUTRUNHANVIEN` | Nguồn trực tiếp cho các nhánh fallback phụ cấp/khấu trừ |
| TV3 – Trần Tiến Đạt | `fn_TongKhauTru(MaNV, Thang, Nam)` | Tùy chọn; script hiện tại chưa có nên fallback sang `SUM(KHAUTRUNHANVIEN.SoTien)` |
| TV5 – Trần Đức Anh | `fn_TinhThucNhan(TienCong, TongPhuCap, TongKhauTru)` | Tùy chọn; nếu thiếu thì procedure tính trực tiếp `TienCong + TongPhuCap - TongKhauTru` |
`vw_TongKhauTruThang` là object TV4 phục vụ tra cứu/báo cáo, không phải dependency thực thi của `sp_TinhBangLuongThang`.

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
TV1 (NHANVIEN)
        │
TV2 (CHAMCONG, fn_TongPhuCap tùy chọn)────┐
        │                            │
TV3 (PHUCAPNHANVIEN, KHAUTRUNHANVIEN,     │
     fn_TongKhauTru tùy chọn)─────────────┤
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
