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

## Tài liệu hiện có

- [Kế hoạch phân công Project DBMS](docs/Ke_hoach_phan_cong_Project_DBMS_Nhom06.md)
- [Kế hoạch phân công - bản Word](docs/Ke_hoach_phan_cong_Project_DBMS_Nhom06.docx)
- [Phân tích và thiết kế hệ thống - bản Word](docs/Nhom_06_Phan_Tich_Thiet_Ke_He_Thong.docx)
- [Hướng dẫn làm việc với Git/GitHub](docs/GIT_WORKFLOW.md)

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
