# Hướng dẫn Git/GitHub cho Nhóm 06

Tài liệu này là hướng dẫn thao tác Git/GitHub thống nhất cho project **Quản lý nhân sự và tiền lương**.

## 1. Workflow chung

```text
main
  ↑
Pull Request
  ↑
branch của thành viên
```

Branch `main` đang được GitHub Ruleset bảo vệ. Thành viên không được push trực tiếp vào `main`.

Mỗi thay đổi phải đi theo quy trình:

1. Đồng bộ `main` mới nhất.
2. Tạo branch riêng.
3. Code và tự kiểm thử.
4. Commit thay đổi.
5. Push branch lên GitHub.
6. Tạo Pull Request vào `main`.
7. Chờ Code Owner review.
8. Sửa theo review nếu có.
9. Chỉ merge sau khi được approve.

## 2. Branch gợi ý theo phân công

| Thành viên | Phạm vi chính | Branch khởi đầu gợi ý |
|---|---|---|
| Nguyễn Minh Trí | Nhân viên, phòng ban, chức vụ | `feature/hr-core` |
| Phạm Minh Quân | Chấm công | `feature/attendance` |
| Trần Tiến Đạt | Phụ cấp, khấu trừ | `feature/allowance-deduction` |
| Nguyễn Quang Vinh | Tính lương | `feature/payroll` |
| Trần Đức Anh | Đăng nhập, phân quyền, integration | `feature/auth-security-integration` |

Không bắt buộc chỉ dùng một branch cho cả module. Với task nhỏ nên tạo branch riêng, ví dụ:

```text
feature/attendance-ui
db/attendance-trigger
fix/payroll-rollback
docs/database-design
test/payroll-transaction
```

## 3. Clone repository lần đầu

```powershell
git clone https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong.git
cd Project_QuanLyNhanSuTienLuong
```

Kiểm tra remote:

```powershell
git remote -v
```

## 4. Bắt đầu một task mới

Luôn cập nhật `main` trước khi tạo branch:

```powershell
git checkout main
git pull origin main
git checkout -b feature/ten-task
```

Kiểm tra branch hiện tại:

```powershell
git branch
```

## 5. Commit và push

Sau khi code xong:

```powershell
git status
git add .
git commit -m "feat: mô tả ngắn thay đổi"
git push -u origin feature/ten-task
```

Từ lần push tiếp theo trên cùng branch chỉ cần:

```powershell
git push
```

Không sử dụng `git push --force` nếu không có lý do đặc biệt và chưa trao đổi với Code Owner.

## 6. Tạo Pull Request

Trên GitHub, chọn branch vừa push và tạo Pull Request với:

```text
base: main
compare: branch của bạn
```

Điền đầy đủ Pull Request template. Với thay đổi liên quan database cần ghi rõ:

- Bảng/đối tượng SQL bị ảnh hưởng.
- Có thay đổi dữ liệu mẫu hay không.
- Có ảnh hưởng Stored Procedure/Function/View/Trigger khác hay không.
- Cách đã kiểm thử.

## 7. Khi reviewer yêu cầu sửa

Không đóng Pull Request và không tạo Pull Request mới. Chỉ cần sửa trên branch hiện tại:

```powershell
git add .
git commit -m "fix: xử lý góp ý review"
git push
```

Pull Request sẽ tự cập nhật. Ruleset yêu cầu review lại sau khi có commit mới.

## 8. Đồng bộ khi main đã thay đổi

Nếu trong lúc đang làm task có thay đổi mới được merge vào `main`:

```powershell
git checkout main
git pull origin main
git checkout feature/ten-task
git merge main
```

Nếu có conflict, xử lý conflict cẩn thận, test lại rồi mới commit.

## 9. Những việc không được làm

- Không commit file chứa password/token.
- Không commit file backup SQL Server dung lượng lớn nếu chưa thống nhất.
- Không tự ý đổi tên bảng/cột đã được cả nhóm thống nhất.
- Không merge Pull Request của chính mình khi chưa được Code Owner duyệt.
- Không xóa branch của thành viên khác.
- Không force push vào branch dùng chung.

## 10. Checklist trước khi mở Pull Request

- [ ] Branch được tạo từ `main` mới nhất.
- [ ] Code build/chạy được trên máy cá nhân.
- [ ] Đã test chức năng bị thay đổi.
- [ ] Nếu có SQL, script chạy được trên SQL Server.
- [ ] Không có password/token trong commit.
- [ ] Commit message đúng quy ước.
- [ ] Không có file tạm, log hoặc IDE artifact không cần thiết.
- [ ] Pull Request mô tả rõ nội dung và cách kiểm thử.

## 11. Quy trình dành cho Code Owner

Code Owner hiện tại: `@tranducanhyn99-prog`.

Khi review Pull Request:

1. Đọc mô tả và kiểm tra phạm vi thay đổi.
2. Xem `Files changed`.
3. Kiểm tra ảnh hưởng đến database và các module liên quan.
4. Yêu cầu sửa nếu phát hiện lỗi hoặc thiếu test.
5. Chỉ approve khi thay đổi đã đạt yêu cầu.
6. Merge vào `main` sau khi toàn bộ conversation đã được resolve.
7. Sau merge, thành viên có thể xóa branch đã hoàn tất.

## 12. Mục tiêu của quy trình

Quy trình này giúp `main` luôn là phiên bản ổn định, lịch sử đóng góp rõ ràng, giảm xung đột khi 5 thành viên làm song song và tạo bằng chứng đóng góp phục vụ phần báo cáo/vấn đáp của project.
