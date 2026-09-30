# Project Quản Lý Nhân Sự và Tiền Lương

Đồ án cuối kỳ học phần **Hệ quản trị cơ sở dữ liệu (DBMS330284)**.

Mục tiêu của dự án là xây dựng một ứng dụng quản lý nhân sự và tiền lương kết nối trực tiếp với **Microsoft SQL Server**, đồng thời thể hiện đầy đủ các nội dung trọng tâm của môn học như ràng buộc dữ liệu, Trigger, View, Index, Stored Procedure, Function, Transaction, phân quyền và xử lý đồng thời.

## Công nghệ sử dụng

- **Java Swing**: xây dựng giao diện desktop.
- **JDBC**: kết nối ứng dụng Java với SQL Server.
- **Microsoft SQL Server**: hệ quản trị cơ sở dữ liệu chính.
- **Git + GitHub**: quản lý mã nguồn, tài liệu và lịch sử đóng góp.

## Chức năng chính dự kiến

- Đăng nhập và phân quyền theo vai trò.
- Quản lý hồ sơ nhân viên.
- Quản lý phòng ban và chức vụ.
- Chấm công.
- Quản lý phụ cấp và khấu trừ.
- Tính bảng lương theo tháng.
- Chốt bảng lương.
- Tra cứu, thống kê và báo cáo.

## Mục tiêu kỹ thuật theo rubric

Dự án hướng tới đáp ứng các yêu cầu chính của học phần:

- Tối thiểu 8 bảng có quan hệ và chuẩn hóa đến ít nhất 3NF.
- Tối thiểu 5 Constraint có ý nghĩa nghiệp vụ.
- Tối thiểu 5 Trigger.
- Tối thiểu 5 View.
- Tối thiểu 5 Index và có minh chứng hiệu năng.
- Tối thiểu 5 Stored Procedure.
- Tối thiểu 5 User-Defined Function.
- Tối thiểu 5 nghiệp vụ sử dụng Transaction.
- Có minh họa xử lý concurrency hoặc recovery.
- Có ít nhất 4 Role/Login với chính sách GRANT/REVOKE/DENY.

> Lưu ý: Đây là các mục tiêu triển khai của dự án. Trạng thái hoàn thành thực tế sẽ được cập nhật theo tiến độ nhóm.

## Cấu trúc repository

```text
Project_QuanLyNhanSuTienLuong/
├── src/        # Mã nguồn Java
├── database/   # Script SQL Server
├── docs/       # Tài liệu, báo cáo, sơ đồ và kế hoạch
├── .github/    # CODEOWNERS, PR template và ruleset
├── README.md
└── CONTRIBUTING.md
```

## Tài liệu dự án

### 1. Kế hoạch & Kiến trúc chung
- [Kế hoạch phân công Project DBMS (Nhóm 06)](docs/Ke_hoach_phan_cong_Project_DBMS_Nhom06.md)
- [Kế hoạch phân công - bản Word](docs/Ke_hoach_phan_cong_Project_DBMS_Nhom06.docx)
- [Phân tích và thiết kế hệ thống - bản Word](docs/Nhom_06_Phan_Tich_Thiet_Ke_He_Thong.docx)
- [Hướng dẫn làm việc với Git/GitHub](docs/GIT_WORKFLOW.md)
- [Thiết kế Kiến trúc Hệ thống 4 tầng](docs/TV5_Architecture.md)
- [Checklist Tích hợp trước khi merge PR](docs/TV5_Integration_Checklist.md)

### 2. Báo cáo Tiến độ & Thiết kế 5 Phân hệ
- [TV1 – Quản lý Hồ sơ Nhân sự & Danh mục](docs/TV1_Tien_Do_Thuc_Hien.md)
- [TV2 – Quản lý Chấm công & Tổng hợp công](docs/TV2_Tien_Do_Thuc_Hien.md)
- [TV3 – Quản lý Phụ cấp & Khấu trừ](docs/TV3_Tien_Do_Thuc_Hien.md)
- [TV4 – Quản lý Tính lương & Bảng lương](docs/TV4_Tien_Do_Thuc_Hien.md)
- [TV5 – Bảo mật, Concurrency & Chốt kỳ lương](docs/TV5_Tien_Do_Thuc_Hien.md)

### 3. Báo cáo Chuyên đề Tổng kết & Minh chứng Thực nghiệm
- [TV1 – Thiết kế CSDL Chuyên sâu, Chuẩn hóa 3NF & Benchmark Index](docs/TV1_BaoCao_ChuyenDe_NhanSu_CuoiKy.md)
- [TV1 – Bảng Yêu cầu Minh chứng Thực nghiệm Rubric](docs/TV1_Yeu_Cau_Bo_Sung_Minh_Chung.md)
- [TV1 – Bộ 20 Test Cases Kiểm thử Module Nhân sự](docs/TV1_NhanSu_Test_Cases.md)
- [TV5 – Chương 3 – Kiến trúc Bảo mật 2 tầng, Concurrency & Benchmark Index](docs/Chuong3_Bao_Mat_Va_Concurrency_TV5.md)

### 4. Scripts Kiểm thử & Tự động hóa
- `run_tuan3_tv1.ps1`: Chạy toàn bộ kiểm thử Unit/Integration, Benchmark và Chụp màn hình cho **TV1 (Nguyễn Minh Trí)**.
- `run_tuan3_tv5.ps1`: Chạy toàn bộ kiểm thử Tích hợp hệ thống, RBAC và Chụp 10 màn hình cho **TV5 (Trần Đức Anh)**.
- `database/test_benchmark_index_TV1.sql`: Đo kiểm hiệu năng Index `IX_NHANVIEN_HoTen` (Scan vs Seek).
- `database/test_module_nhansu_TV1.sql`: Kiểm thử tự động 6 đối tượng CSDL của TV1 (SP, Transaction, Trigger, Function, View, Index).

## Quy trình Git/GitHub

Branch `main` đã được bảo vệ bằng GitHub Ruleset.

- Không được push trực tiếp vào `main`.
- Mỗi công việc phải thực hiện trên một branch riêng.
- Thành viên push branch lên GitHub và tạo Pull Request vào `main`.
- Pull Request phải được Code Owner review trước khi merge.
- Code Owner của repository: **@tranducanhyn99-prog**.
- Chỉ thay đổi đã qua review mới được đưa vào `main`.

Quy ước branch:

- `feature/<ten-chuc-nang>`: tính năng mới.
- `fix/<ten-loi>`: sửa lỗi.
- `db/<noi-dung>`: thay đổi cơ sở dữ liệu.
- `docs/<noi-dung>`: tài liệu.
- `test/<noi-dung>`: kiểm thử.

Xem chi tiết tại [CONTRIBUTING.md](CONTRIBUTING.md).

## Quy ước commit

Sử dụng commit message ngắn gọn, mô tả đúng nội dung thay đổi:

- `feat:` thêm tính năng mới.
- `fix:` sửa lỗi.
- `db:` thay đổi liên quan đến database.
- `docs:` cập nhật tài liệu.
- `test:` thêm hoặc cập nhật kiểm thử.
- `refactor:` tái cấu trúc code nhưng không thay đổi hành vi.
- `chore:` cấu hình hoặc công việc phụ trợ.

Ví dụ:

```text
feat: thêm màn hình quản lý nhân viên
db: thêm stored procedure tính bảng lương
fix: rollback transaction khi tính lương thất bại
docs: cập nhật ERD
```

## Bảo mật cấu hình

Không commit mật khẩu, connection string thật hoặc file chứa thông tin nhạy cảm. Các file như `.env`, `config.properties` và `*.bak` đã được đưa vào `.gitignore`.

---

**Repository:** `tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong`
