# KẾ HOẠCH THỰC HIỆN PROJECT CUỐI KỲ

**Đề tài:** Hệ thống quản lý nhân sự và tiền lương  
**Học phần:** Hệ quản trị cơ sở dữ liệu (DBMS330284)  
**GVHD:** TS. Phan Thị Thể  
**Thời gian:** 3 tuần — 21/09/2026 đến 11/10/2026

**Căn cứ:** “Hướng dẫn thực hiện Project cuối kỳ – DBMS330284”, “Nhóm 06 – Phân Tích Thiết Kế Hệ Thống” và “[Mẫu] Kế hoạch thực hiện TLCN”.

## 1. Thành viên nhóm

| STT | Họ và tên | MSSV | Mã phân công |
|---:|---|---|---|
| 1 | Nguyễn Minh Trí | 24110359 | TV1 |
| 2 | Phạm Minh Quân | 24110311 | TV2 |
| 3 | Trần Tiến Đạt | 24110198 | TV3 |
| 4 | Nguyễn Quang Vinh | 24110385 | TV4 |
| 5 | Trần Đức Anh | 24110155 | TV5 |

## 2. Nguyên tắc phân công

- Phân công theo mô hình “vertical slice”: mỗi thành viên có phần phân tích, SQL Server, Java/JDBC, kiểm thử, báo cáo và nội dung vấn đáp tương ứng.
- Giữ thống nhất tên bảng/cột, công thức lương, trạng thái nghiệp vụ và tên các đối tượng SQL giữa tài liệu, script và source code.
- SQL Server là trung tâm của project: logic dữ liệu quan trọng phải được thể hiện bằng constraint/trigger/view/index/stored procedure/function/transaction/role theo rubric.
- Mỗi thành viên phải tự kiểm thử và lưu minh chứng cho phần mình phụ trách; tuần 3 mới tổng hợp thành báo cáo, slide và kịch bản demo.
- Kết quả thực tế và chữ ký GVHD được để trống để cập nhật theo đúng mẫu kế hoạch.

## 3. Kế hoạch cụ thể theo tuần

### Tuần 1: 21/9–27/9

| Người chịu trách nhiệm | Nhiệm vụ | Sản phẩm | Kết quả thực tế | Chữ ký GVHD |
|---|---|---|---|---|
| **Nguyễn Minh Trí** | • Rà soát mục tiêu, phạm vi, tác nhân và Use Case của hệ thống quản lý nhân sự – tiền lương.<br>• Chốt ERD tổng thể, lược đồ quan hệ, PK/FK và chuẩn hóa dữ liệu đến 3NF.<br>• Chịu trách nhiệm chính các bảng PHONGBAN, CHUCVU, NHANVIEN và phối hợp rà soát TAIKHOAN.<br>• Chốt quy ước đặt tên bảng/cột, trạng thái nhân viên và quy tắc không xóa cứng khi đã phát sinh dữ liệu lương. | • ERD phiên bản chốt.<br>• Relational Schema.<br>• Tài liệu chuẩn hóa 1NF–3NF.<br>• Danh sách quy tắc/ràng buộc dữ liệu của module nhân sự. |  |  |
| **Phạm Minh Quân** | • Phân tích nghiệp vụ chấm công theo cặp nhân viên – ngày.<br>• Chốt cấu trúc bảng CHAMCONG và các quy tắc: không trùng ngày, giờ ra > giờ vào, nhân viên nghỉ việc không được chấm công mới.<br>• Đặc tả luồng xử lý ChamCongPanel → Service → DAO → SQL Server.<br>• Thiết kế bộ testcase cho dữ liệu chấm công hợp lệ/không hợp lệ. | • Đặc tả nghiệp vụ chấm công.<br>• Thiết kế bảng CHAMCONG.<br>• Danh sách constraint/trigger cần kiểm tra.<br>• Bộ testcase chấm công. |  |  |
| **Trần Tiến Đạt** | • Phân tích nghiệp vụ phụ cấp và khấu trừ theo nhân viên, tháng, năm.<br>• Chốt cấu trúc PHUCAPNHANVIEN và KHAUTRUNHANVIEN; quy tắc số tiền không âm, tháng 1–12, năm hợp lệ.<br>• Thiết kế luồng nhập/xem/tổng hợp phụ cấp – khấu trừ trên ứng dụng.<br>• Chuẩn bị dữ liệu mẫu phục vụ kiểm thử Function/View tổng hợp theo kỳ. | • Đặc tả nghiệp vụ phụ cấp/khấu trừ.<br>• Thiết kế 2 bảng nghiệp vụ.<br>• Dữ liệu mẫu kiểm thử.<br>• Đề xuất giao diện nhập và tra cứu khoản phát sinh. |  |  |
| **Nguyễn Quang Vinh** | • Phân tích luồng tính lương từ chấm công + phụ cấp – khấu trừ.<br>• Chốt công thức: Tiền công = (Lương cơ bản / số ngày công chuẩn) × ngày công thực tế; Thực nhận = Tiền công + phụ cấp – khấu trừ.<br>• Chốt cấu trúc BANGLUONG và CHITIETBANGLUONG, điều kiện một kỳ/tháng/năm và một chi tiết/nhân viên/kỳ.<br>• Thiết kế luồng transaction tính bảng lương theo nguyên tắc toàn bộ thành công hoặc rollback toàn bộ. | • Đặc tả nghiệp vụ tính lương.<br>• Thiết kế BANGLUONG/CHITIETBANGLUONG.<br>• Sơ đồ luồng tính lương.<br>• Kịch bản transaction/rollback. |  |  |
| **Trần Đức Anh** | • Chốt kiến trúc Java Swing → Service → DAO/JDBC → SQL Server và cấu trúc thư mục dự án.<br>• Phân tích TAIKHOAN, Session và 4 vai trò DB_Admin, HR_Manager, Payroll_Officer, Employee.<br>• Thiết kế ma trận phân quyền ứng dụng/CSDL và kế hoạch minh họa GRANT, REVOKE, DENY.<br>• Thiết kế tình huống concurrency khi hai Payroll_Officer cùng tính/chốt một kỳ lương.<br>• Chuẩn hóa repository, quy tắc branch/commit và checklist tích hợp. | • Architecture diagram.<br>• Role/permission matrix.<br>• Cấu trúc source/repository.<br>• Kịch bản concurrency và security demo. |  |  |

### Tuần 2: 28/9–4/10

| Người chịu trách nhiệm | Nhiệm vụ | Sản phẩm | Kết quả thực tế | Chữ ký GVHD |
|---|---|---|---|---|
| **Nguyễn Minh Trí** | • Cài đặt DDL/constraint cho PHONGBAN, CHUCVU, NHANVIEN và các khóa ngoại liên quan.<br>• Cài đặt sp_ThemNhanVien; phối hợp kiểm thử sp_CapNhatNhanVien.<br>• Cài đặt trg_NhanVien_KhongXoaKhiDaPhatSinhLuong, vw_NhanVien_PhongBan_ChucVu, IX_NHANVIEN_HoTen.<br>• Cài đặt chức năng NhanVienPanel/DanhMucPanel, NhanVienService, NhanVienDAO.<br>• Thực hiện transaction “Tạo nhân viên + tài khoản” và kiểm tra rollback khi bước tạo tài khoản thất bại. | • SQL script module nhân sự.<br>• CRUD/tìm kiếm nhân viên hoạt động.<br>• Transaction tạo nhân viên + tài khoản.<br>• Testcase + ảnh minh chứng. |  |  |
| **Phạm Minh Quân** | • Cài đặt sp_CapNhatNhanVien theo phân công SQL chung và phối hợp TV1 kiểm thử liên kết dữ liệu.<br>• Cài đặt fn_TongPhuCap, trg_ChamCong_KiemTraNhanVien, vw_TongHopChamCongThang, IX_CHAMCONG_MaNV_Ngay.<br>• Xây dựng ChamCongPanel, ChamCongService, ChamCongDAO; dùng PreparedStatement/CallableStatement phù hợp.<br>• Cài đặt transaction nhập chấm công theo lô; kiểm thử rollback khi có một dòng không hợp lệ.<br>• Chuẩn bị minh chứng lỗi: nhân viên nghỉ việc, trùng ngày, giờ không hợp lệ. | • SQL objects theo phân công.<br>• Màn hình chấm công hoạt động.<br>• Transaction nhập chấm công theo lô.<br>• Bộ testcase lỗi chấm công. |  |  |
| **Trần Tiến Đạt** | • Cài đặt sp_GhiNhanChamCong và phối hợp TV2 tích hợp vào ứng dụng.<br>• Cài đặt fn_TongKhauTru, trg_ChamCong_KiemTraGio, vw_TongPhuCapThang, IX_PHUCAP_MaNV_ThangNam.<br>• Xây dựng giao diện/module quản lý phụ cấp và khấu trừ, gồm nhập dữ liệu, lọc theo kỳ, tải lại từ CSDL.<br>• Cài đặt và kiểm thử transaction xóa kỳ lương chưa chốt: xóa chi tiết trước, xóa kỳ sau, rollback khi lỗi.<br>• Chuẩn bị dữ liệu demo phụ cấp/khấu trừ đủ để phục vụ tính lương. | • SQL objects theo phân công.<br>• Module phụ cấp/khấu trừ hoạt động.<br>• Transaction xóa kỳ chưa chốt.<br>• Dữ liệu demo theo kỳ. |  |  |
| **Nguyễn Quang Vinh** | • Cài đặt sp_TinhBangLuongThang với TRY...CATCH, BEGIN TRAN/COMMIT/ROLLBACK.<br>• Cài đặt fn_TinhTienCong, trg_BangLuong_KhongSuaKhiDaChot, vw_TongKhauTruThang, IX_KHAUTRU_MaNV_ThangNam.<br>• Tích hợp phần “Tính lương” trong BangLuongPanel, PayrollService, BangLuongDAO.<br>• Kiểm thử kỳ đã tồn tại, kỳ đã chốt, lỗi insert chi tiết và rollback toàn bộ.<br>• Đối chiếu kết quả tính với công thức đã chốt ở tuần 1. | • Payroll Stored Procedure.<br>• Function/Trigger/View/Index theo phân công.<br>• BangLuongPanel phần tính lương.<br>• Testcase transaction tính lương. |  |  |
| **Trần Đức Anh** | • Cài đặt sp_ChotBangLuong, fn_TinhThucNhan, trg_ChiTietLuong_KhongSuaKhiDaChot, vw_BangLuongChiTiet, IX_NHANVIEN_MaPB_MaCV.<br>• Cài đặt LoginFrame, MainFrame, Session, ẩn/disable chức năng theo role và kiểm tra quyền ở SQL Server.<br>• Cài đặt 4 Role/Login, script GRANT/REVOKE/DENY và kiểm thử truy cập đúng/sai quyền.<br>• Tích hợp chức năng chốt bảng lương trong transaction.<br>• Thiết lập BaoCaoPanel đọc dữ liệu qua View/Stored Procedure thay vì SQL lặp lại trong giao diện. | • Login/phân quyền hoạt động.<br>• Security script.<br>• Chốt bảng lương hoạt động.<br>• BaoCaoPanel cơ bản.<br>• SQL objects theo phân công. |  |  |

### Tuần 3: 5/10–11/10

| Người chịu trách nhiệm | Nhiệm vụ | Sản phẩm | Kết quả thực tế | Chữ ký GVHD |
|---|---|---|---|---|
| **Nguyễn Minh Trí** | • Rà soát toàn bộ ERD, PK/FK, mô tả bảng và chuẩn hóa 3NF sau khi code thực tế đã ổn định.<br>• Kiểm tra consistency giữa tên bảng/cột trong tài liệu, script SQL và Java model/DAO.<br>• Hoàn thiện nội dung Chương 1 và phần Phân tích & thiết kế CSDL trong báo cáo cuối kỳ.<br>• Chuẩn bị phần trình bày/vấn đáp về ERD, 3NF, ràng buộc và module nhân sự. | • ERD và schema cuối.<br>• Chương phân tích & thiết kế CSDL hoàn chỉnh.<br>• Checklist consistency.<br>• Nội dung demo/vấn đáp cá nhân. |  |  |
| **Phạm Minh Quân** | • Test module chấm công với dữ liệu hợp lệ/không hợp lệ và transaction nhập lô.<br>• Thu thập screenshot/log minh chứng Trigger, rollback và View tổng hợp chấm công.<br>• Kiểm tra Index CHAMCONG bằng Actual Execution Plan, SET STATISTICS IO/TIME; ghi before/after.<br>• Hoàn thiện phần báo cáo nghiệp vụ chấm công, transaction và index liên quan. | • Test report chấm công.<br>• Minh chứng transaction/trigger.<br>• Benchmark index.<br>• Nội dung báo cáo và demo cá nhân. |  |  |
| **Trần Tiến Đạt** | • Test Function/View phụ cấp – khấu trừ trên nhiều kỳ và nhiều nhân viên.<br>• Kiểm tra các constraint số tiền/tháng/năm và xử lý dữ liệu sai.<br>• Đo hiệu năng index phụ cấp bằng Execution Plan + STATISTICS IO/TIME.<br>• Hoàn thiện phần báo cáo Stored Procedure/Function/View/Index được giao và chuẩn bị vấn đáp. | • Test report phụ cấp/khấu trừ.<br>• Benchmark index.<br>• Ảnh minh chứng Function/View.<br>• Nội dung báo cáo và demo cá nhân. |  |  |
| **Nguyễn Quang Vinh** | • Test end-to-end: dữ liệu nguồn → tính lương → tạo chi tiết → rollback khi lỗi → tính lại thành công.<br>• Kiểm tra khóa sửa dữ liệu sau khi chốt và phối hợp TV5 test concurrency cùng kỳ lương.<br>• Đo hiệu năng index khấu trừ và các truy vấn nguồn tính lương.<br>• Hoàn thiện phần báo cáo Stored Procedure/Function/Transaction tính lương và chuẩn bị demo. | • Test report payroll.<br>• Minh chứng rollback/khóa dữ liệu.<br>• Benchmark index.<br>• Nội dung báo cáo và demo cá nhân. |  |  |
| **Trần Đức Anh** | • Integration test toàn hệ thống: login → nhân sự → chấm công → phụ cấp/khấu trừ → tính lương → chốt → báo cáo.<br>• Test security: Role/Login, GRANT/REVOKE/DENY và quyền theo vai trò; test mất kết nối SQL Server.<br>• Thực hiện concurrency demo hai Payroll_Officer cùng tính/chốt một kỳ lương và ghi lại kết quả.<br>• Tổng hợp report 50–100 trang, README, script/backup CSDL, source code, slide ≤15 trang và package nộp bài.<br>• Điều phối rehearsal 10–15 phút thuyết trình + 5–10 phút vấn đáp; bảo đảm mọi thành viên nắm luồng toàn hệ thống. | • Bản release tích hợp.<br>• Security/concurrency evidence.<br>• README + package nộp.<br>• Report/slide cuối.<br>• Kịch bản demo và phân vai thuyết trình. |  |  |

## 4. Ma trận ownership các đối tượng SQL và transaction

| Thành viên | Stored Procedure | Function | Trigger | View | Index | Transaction |
|---|---|---|---|---|---|---|
| TV1 – Nguyễn Minh Trí | sp_ThemNhanVien | fn_TinhSoNgayCong | trg_NhanVien_KhongXoaKhiDaPhatSinhLuong | vw_NhanVien_PhongBan_ChucVu | IX_NHANVIEN_HoTen | Tạo nhân viên + tài khoản |
| TV2 – Phạm Minh Quân | sp_CapNhatNhanVien | fn_TongPhuCap | trg_ChamCong_KiemTraNhanVien | vw_TongHopChamCongThang | IX_CHAMCONG_MaNV_Ngay | Nhập chấm công theo lô |
| TV3 – Trần Tiến Đạt | sp_GhiNhanChamCong | fn_TongKhauTru | trg_ChamCong_KiemTraGio | vw_TongPhuCapThang | IX_PHUCAP_MaNV_ThangNam | Xóa kỳ lương chưa chốt |
| TV4 – Nguyễn Quang Vinh | sp_TinhBangLuongThang | fn_TinhTienCong | trg_BangLuong_KhongSuaKhiDaChot | vw_TongKhauTruThang | IX_KHAUTRU_MaNV_ThangNam | Tính bảng lương tháng |
| TV5 – Trần Đức Anh | sp_ChotBangLuong | fn_TinhThucNhan | trg_ChiTietLuong_KhongSuaKhiDaChot | vw_BangLuongChiTiet | IX_NHANVIEN_MaPB_MaCV | Chốt bảng lương |

## 5. Checklist nghiệm thu theo Project/Rubric

| Hạng mục | Yêu cầu tối thiểu | Kế hoạch đáp ứng |
|---|---|---|
| Thiết kế dữ liệu | ≥ 8 bảng, ERD, relational schema, PK/FK, chuẩn hóa ≥ 3NF | 9 bảng; ERD/3NF do TV1 điều phối, cả nhóm rà soát |
| Constraint | ≥ 5 CHECK/UNIQUE/DEFAULT có ý nghĩa | Phân bổ theo module; kiểm thử ở tuần 2–3 |
| Trigger | ≥ 5 | Mỗi thành viên sở hữu 1 trigger chính |
| View | ≥ 5 | Mỗi thành viên sở hữu 1 view chính |
| Index | ≥ 5 + minh chứng hiệu năng | Mỗi thành viên sở hữu 1 index; đo Execution Plan + STATISTICS IO/TIME |
| Stored Procedure | ≥ 5 + TRY...CATCH | 5 SP chia đều 5 thành viên |
| Function | ≥ 5 | 5 function chia đều 5 thành viên |
| Transaction | ≥ 5 | 5 nghiệp vụ transaction chia đều 5 thành viên |
| Concurrency/Recovery | Ít nhất 1 tình huống minh họa | TV5 điều phối, TV4 phối hợp tình huống cùng tính/chốt kỳ lương |
| Security | ≥ 4 Role/Login + GRANT/REVOKE/DENY | TV5 chịu trách nhiệm chính; toàn nhóm kiểm thử theo role |
| Ứng dụng | Login/phân quyền, CRUD, search/report, gọi SP/Function, xử lý lỗi | Java Swing + Service + DAO/JDBC + SQL Server |
| Báo cáo & nộp bài | Word/PDF 50–100 trang, slide ≤15 trang, backup/script, source, README | TV5 tổng hợp; từng TV chịu trách nhiệm nội dung/module và minh chứng của mình |

## 6. Sản phẩm cuối cùng cần nộp

- Báo cáo Word/PDF theo cấu trúc yêu cầu, mục tiêu 50–100 trang (không tính bìa, mục lục, phụ lục).
- Slide thuyết trình không quá 15 trang.
- File backup SQL Server (.bak) hoặc bộ script .sql có thể tạo lại toàn bộ CSDL.
- Toàn bộ source code ứng dụng Java Swing/JDBC và README hướng dẫn cài đặt/chạy.
- Bộ dữ liệu demo, test case, ảnh/log minh chứng Trigger, View, Index, Stored Procedure, Function, Transaction, Security và Concurrency.
- Bảng phân công chi tiết từng thành viên và lịch sử commit để chứng minh mức độ đóng góp.

## 7. Kịch bản kiểm thử tích hợp tối thiểu trước khi bảo vệ

1. Đăng nhập đúng/sai; tài khoản bị khóa; menu hiển thị theo role.
2. CRUD nhân viên/phòng ban/chức vụ; email/mã nhân viên trùng; lương cơ bản không hợp lệ.
3. Chấm công trùng ngày; nhân viên nghỉ việc; giờ ra không hợp lệ; nhập lô và rollback.
4. Nhập phụ cấp/khấu trừ; số tiền âm; tổng hợp theo tháng/năm.
5. Tính bảng lương; kỳ đã tồn tại/chốt; lỗi giữa transaction phải rollback toàn bộ.
6. Chốt bảng lương; sau chốt không sửa/xóa chi tiết trái quy trình.
7. Hai Payroll_Officer cùng tính/chốt một kỳ để minh họa concurrency.
8. GRANT/REVOKE/DENY trên role; người dùng không được truy cập đối tượng ngoài quyền.
9. Benchmark từng index trước/sau bằng Actual Execution Plan và SET STATISTICS IO, TIME.
10. Mất kết nối SQL Server và dữ liệu nhập sai phải được ứng dụng xử lý ngoại lệ rõ ràng.

## 8. Ghi chú phối hợp và cập nhật tiến độ

- Mỗi ngày hoàn thành task phải commit/push với message rõ ràng; không gom toàn bộ thay đổi vào một commit cuối.
- Thay đổi ảnh hưởng ERD, công thức lương hoặc quyền phải được báo cho cả nhóm trước khi merge.
- Tuần 2 ưu tiên “SQL chạy đúng trước, UI chạy sau”; không đặt logic tính lương trọng yếu hoàn toàn ở Java.
- Tuần 3 không mở rộng chức năng lớn mới; tập trung test, sửa lỗi, benchmark, tài liệu và rehearsal.
- Mỗi thành viên phải hiểu luồng tổng thể dù chỉ phụ trách một module, vì giảng viên có thể vấn đáp chéo.

---

**Đại diện nhóm:** ____________________  
**GVHD:** ____________________