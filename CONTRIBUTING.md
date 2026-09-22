# Quy ước đóng góp cho dự án

Tài liệu này quy định cách 5 thành viên làm việc với Git/GitHub để tránh xung đột và bảo vệ branch `main`.

## 1. Nguyên tắc bắt buộc

- Không làm việc trực tiếp trên `main`.
- Không push trực tiếp vào `main`.
- Mỗi task phải có branch riêng.
- Mỗi branch chỉ nên tập trung vào một nhóm thay đổi liên quan.
- Mọi thay đổi vào `main` phải thông qua Pull Request.
- Pull Request phải được Code Owner review và approve trước khi merge.
- Không commit mật khẩu, token, connection string thật hoặc dữ liệu nhạy cảm.

## 2. Quy ước tên branch

- `feature/<ten-chuc-nang>`: tính năng mới.
- `fix/<ten-loi>`: sửa lỗi.
- `db/<noi-dung>`: thay đổi database.
- `docs/<noi-dung>`: tài liệu.
- `test/<noi-dung>`: kiểm thử.
- `refactor/<noi-dung>`: tái cấu trúc code.

Ví dụ:

```text
feature/attendance
feature/payroll
fix/payroll-rollback
db/add-payroll-procedures
docs/update-erd
```

## 3. Quy ước commit

Các prefix được sử dụng:

- `feat:` tính năng mới.
- `fix:` sửa lỗi.
- `db:` thay đổi cơ sở dữ liệu.
- `docs:` thay đổi tài liệu.
- `test:` kiểm thử.
- `refactor:` tái cấu trúc code.
- `chore:` cấu hình và công việc phụ trợ.

Ví dụ:

```text
feat: thêm màn hình chấm công
db: thêm trigger kiểm tra giờ chấm công
fix: ngăn sửa bảng lương đã chốt
test: bổ sung testcase transaction tính lương
```

## 4. Workflow dành cho thành viên

Trước khi bắt đầu task:

```powershell
git checkout main
git pull origin main
git checkout -b feature/ten-task
```

Sau khi hoàn thành thay đổi:

```powershell
git status
git add .
git commit -m "feat: mô tả ngắn thay đổi"
git push -u origin feature/ten-task
```

Sau đó vào GitHub và tạo Pull Request:

```text
feature/ten-task -> main
```

Trong Pull Request cần:

- Mô tả nội dung đã làm.
- Đánh dấu module bị ảnh hưởng.
- Nêu rõ có thay đổi database hay không.
- Ghi cách đã kiểm thử.
- Thêm ảnh minh chứng nếu có thay đổi UI hoặc SQL quan trọng.

Nếu reviewer yêu cầu sửa, tiếp tục commit trên cùng branch và push lại. Pull Request sẽ tự cập nhật.

## 5. Quy trình review và merge

Code Owner hiện tại là `@tranducanhyn99-prog`.

1. Thành viên mở Pull Request vào `main`.
2. Code Owner đọc phần `Files changed` và kiểm tra logic.
3. Nếu chưa đạt, reviewer chọn **Request changes**.
4. Thành viên sửa và push commit mới.
5. Approval cũ sẽ bị hủy khi có thay đổi mới.
6. Khi đạt yêu cầu, Code Owner approve và merge.

