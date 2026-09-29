# TV4 - Test Case Transaction Module Tính Lương

**Tác giả:** Nguyễn Quang Vinh (TV4, MSSV 24110385)

**Phạm vi tự động:** Kiểm thử tầng SQL gồm `sp_TinhBangLuongThang`, `fn_TinhTienCong`, trigger `trg_BangLuong_KhongSuaKhiDaChot`, transaction/savepoint/rollback, concurrency cùng kỳ và các truy vấn nguồn/index khấu trừ. View `vw_TongKhauTruThang` và UI `BangLuongPanel` không được bộ test payroll tự động xác minh.

## Điều kiện chuẩn bị

1. Chạy các script database `01` → `05` theo đúng thứ tự trên **CSDL demo/test**, không chạy các kịch bản ghi/xóa thủ công trên CSDL đang dùng thật.
2. Ưu tiên chạy `run_payroll_tests.ps1`: runner tự tạo fixture cho E2E, cleanup sau chạy và rollback fixture benchmark. Runner chỉ chạy E2E + benchmark; **không chạy hai script concurrency**.
3. Với kiểm thử thủ công, chỉ dùng một kỳ demo chưa có dữ liệu quan trọng, ghi lại `MaBangLuong` vừa tạo và cleanup sau khi đối chiếu. Nếu session bị dừng đột ngột, phải kiểm tra fixture còn sót trước khi chạy lại.
4. Các testcase thủ công cần nhân viên `DANG_LAM_VIEC` và dữ liệu `CHAMCONG` của kỳ cần tính. Ngày công được đếm trực tiếp bằng khoảng ngày nửa mở; phụ cấp/khấu trừ/thực nhận dùng function tích hợp nếu có và fallback sang bảng/phép tính trực tiếp nếu thiếu. `vw_TongKhauTruThang` không được procedure sử dụng.
5. Runner cần tài khoản kiểm thử có quyền tạo/xóa trigger fixture, `IDENTITY_INSERT` và DML trên các bảng liên quan; đây không phải bài kiểm tra quyền của `Payroll_Officer`.
> Các lệnh TC-TV4-01 đến TC-TV4-04 dưới đây là kịch bản minh họa thủ công và có thay đổi dữ liệu. Với kiểm thử lặp lại, an toàn hơn là dùng `database/tests/TV4_Payroll_E2E.sql`, vì file đó chọn kỳ riêng và có finalizer cleanup.

## TC-TV4-01 - Tính lương thành công

```sql
DECLARE @MaBangLuong INT;
EXEC dbo.sp_TinhBangLuongThang
    @Thang = 9,
    @Nam = 2026,
    @NgayCongChuan = 26,
    @MaBangLuong = @MaBangLuong OUTPUT;

SELECT @MaBangLuong AS MaBangLuongMoi;
SELECT * FROM dbo.BANGLUONG WHERE MaBangLuong = @MaBangLuong;
SELECT * FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong = @MaBangLuong;
```

Kết quả mong đợi: thêm 1 dòng `BANGLUONG` trạng thái `CHUA_CHOT`, mỗi nhân viên đang làm có 1 dòng chi tiết, `ThucNhan = TienCong + TongPhuCap - TongKhauTru`.

## TC-TV4-02 - Tính lại kỳ lương chưa chốt

```sql
DECLARE @MaBangLuong INT;
EXEC dbo.sp_TinhBangLuongThang 9, 2026, 26, @MaBangLuong OUTPUT;
```

Kết quả mong đợi: giữ nguyên `MaBangLuong`, xóa chi tiết nháp cũ và nạp lại nguyên tử từ dữ liệu nguồn mới nhất. Nếu kỳ đã `DA_CHOT`, procedure phải từ chối.

## TC-TV4-03 - Kỳ lương đã chốt

```sql
DECLARE @MaBangLuongDaChot INT =
(
    SELECT MaBangLuong
    FROM dbo.BANGLUONG
    WHERE Thang = 9 AND Nam = 2026 AND TrangThai = 'CHUA_CHOT'
);

EXEC dbo.sp_ChotBangLuong @MaBangLuong = @MaBangLuongDaChot;

DECLARE @MaBangLuong INT;
EXEC dbo.sp_TinhBangLuongThang 9, 2026, 26, @MaBangLuong OUTPUT;
```

Kết quả mong đợi: procedure báo lỗi kỳ lương đã chốt và không tính lại.

## TC-TV4-04 - Trigger khóa kỳ đã chốt và cho phép mở lại có kiểm soát

```sql
UPDATE dbo.BANGLUONG
SET NgayCongChuan = 27
WHERE Thang = 9 AND Nam = 2026 AND TrangThai = 'DA_CHOT';

DELETE FROM dbo.BANGLUONG
WHERE Thang = 9 AND Nam = 2026 AND TrangThai = 'DA_CHOT';

-- Sau khi đã xác nhận hai thao tác trên bị chặn, mở lại qua procedure TV4.
DECLARE @MaBangLuong INT =
(
    SELECT MaBangLuong
    FROM dbo.BANGLUONG
    WHERE Thang = 9 AND Nam = 2026 AND TrangThai = 'DA_CHOT'
);

EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = @MaBangLuong;

SELECT TrangThai, NgayChot
FROM dbo.BANGLUONG
WHERE MaBangLuong = @MaBangLuong;
```

Kết quả mong đợi: UPDATE nghiệp vụ và DELETE đều bị `trg_BangLuong_KhongSuaKhiDaChot` chặn. Riêng transition chính xác `DA_CHOT -> CHUA_CHOT`, `NgayChot -> NULL`, không đổi `Thang`, `Nam`, `NgayCongChuan`, `NgayTao` được phép khi gọi `sp_HuyChotBangLuong`; chi tiết lương vẫn được giữ nguyên.

Sau khi đối chiếu trên CSDL demo, có thể cleanup kỳ vừa mở lại bằng `sp_XoaBangLuongChuaChot`. Không dùng bước cleanup này với kỳ lương thật.

## TC-TV4-05 - Rollback khi lỗi insert chi tiết

Kịch bản thực thi nằm trong `database/tests/TV4_Payroll_E2E.sql`. Trigger giả lập lỗi chỉ kích hoạt cho session test bằng `SESSION_CONTEXT`; finalizer luôn xóa trigger kể cả khi assertion thất bại.

Kết quả mong đợi: header và toàn bộ chi tiết khớp chính xác snapshot trước lỗi; dữ liệu nguồn mới vẫn còn; lần tính lại ngay sau rollback thành công.

## TC-TV4-06 - Đối chiếu công thức

```sql
SELECT
    ct.MaNV,
    ct.LuongCoBan,
    bl.NgayCongChuan,
    ct.NgayCongThucTe,
    ct.TienCong,
    CAST(ROUND(
        (ct.LuongCoBan / CAST(bl.NgayCongChuan AS DECIMAL(18,2))) * ct.NgayCongThucTe,
        2
    ) AS DECIMAL(18,2)) AS TienCongTinhDocLap,
    ct.TongPhuCap,
    ct.TongKhauTru,
    ct.ThucNhan,
    ct.TienCong + ct.TongPhuCap - ct.TongKhauTru AS ThucNhanTinhLai
FROM dbo.CHITIETBANGLUONG ct
INNER JOIN dbo.BANGLUONG bl ON ct.MaBangLuong = bl.MaBangLuong
WHERE bl.Thang = 9 AND bl.Nam = 2026;
```

Kết quả mong đợi: `TienCong` khớp phép tính độc lập `(LuongCoBan / NgayCongChuan) × NgayCongThucTe` ở kiểu `DECIMAL(18,2)`; `ThucNhan` khớp công thức đã chốt.

## TC-TV4-07 - Concurrency cùng kỳ lương

Test này **không nằm trong** `run_payroll_tests.ps1`. Chỉ chạy trên CSDL demo/test, xác nhận kỳ `01/2020` chưa có bảng lương và mở hai session chạy lần lượt:

1. `database/tests/TV4_Payroll_Concurrency_SessionA.sql`
2. Khi Session A báo đang giữ lock, chạy `database/tests/TV4_Payroll_Concurrency_SessionB.sql`.

Kết quả mong đợi: Session B chờ khóa của A, hai session trả cùng `MaBangLuong`, kỳ chỉ có một header và không có cặp `(MaBangLuong, MaNV)` trùng. Session A cleanup fixture khi luồng hoàn tất hoặc đi vào CATCH; nếu đóng/kill session đột ngột thì phải kiểm tra và cleanup thủ công trước lần chạy sau.

## TC-TV4-08 - Benchmark index khấu trừ và truy vấn nguồn

Chạy `database/tests/TV4_Payroll_Benchmark.sql` hoặc runner payroll. Script tạo fixture trong transaction cho `CHAMCONG`, `PHUCAPNHANVIEN`, `KHAUTRUNHANVIEN`; đo các truy vấn nguồn thực tế, so sánh clustered scan với covering seek của index khấu trừ, bật `STATISTICS IO/TIME/XML`, kiểm tra kết quả rồi rollback. Logical reads/thời gian là số đo phụ thuộc máy, cache và dữ liệu tại thời điểm chạy; cần đọc log/Execution Plan của chính lần đo, không coi số liệu tham khảo bên dưới là ngưỡng cố định.

## Ghi nhận một lần thực thi tham khảo ngày 29/09/2026

Các số liệu dưới đây chỉ mô tả một lần chạy trên môi trường phát triển, không phải tiêu chí pass cố định cho máy khác.

| Nhóm kiểm thử | Kết quả |
|---|---|
| E2E, công thức, tính lại, rollback/retry, khóa sau chốt, mở lại, cleanup | PASS 10/10 |
| Concurrency hai session cùng kỳ (chạy thủ công, ngoài runner) | PASS trong một lần đo; Session B mất 20.070 ms, vượt assertion blocking ≥ 15.000 ms; không duplicate |
| Benchmark nguồn/index | PASS trong một lần đo; 2.464 chấm công + 30.000 phụ cấp + 30.000 khấu trừ; khấu trừ scan 547 logical reads, seek 4 logical reads |
| Cleanup fixture | PASS; không còn dữ liệu test |

Chi tiết và kịch bản demo: `docs/TV4_Payroll_Test_Report.md`.
