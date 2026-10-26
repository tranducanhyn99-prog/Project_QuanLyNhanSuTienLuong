# Hướng dẫn sử dụng Trợ lý SSMS

Trợ lý sinh và chèn T-SQL vào cửa sổ Query trong SSMS. SQL được viết ngắn gọn, từ khóa viết thường (`select`, `from`, `where`, `join`, `exec`...), giữ nguyên tên bảng/cột và giá trị chuỗi. Bạn đọc lại SQL và tự nhấn **F5** để thực thi.

1. Nhấp đúp **`BAT_TRO_LY_SSMS.vbs`** ở thư mục gốc dự án. Trợ lý chạy nền, không mở cửa sổ Terminal.
2. Mở **SSMS → New Query**, kết nối server và chọn database đã có các bảng/hàm/thủ tục của dự án, chẳng hạn `QuanLyNhanSuTienLuong`.
3. Gõ một dòng yêu cầu bắt đầu bằng `--`. Đặt con trỏ ở cuối dòng đó hoặc ở dòng trống ngay bên dưới, rồi nhấn **F8** hoặc **Alt + Q**. Không cần bôi đen hay thêm `#`.
4. Đợi SQL được chèn xong, kiểm tra nội dung, chọn phần cần chạy rồi nhấn **F5**. Giữ con trỏ trong vùng soạn thảo; tránh chuyển cửa sổ hoặc đổi kết nối/database khi trợ lý đang xử lý.

File `.vbs` ưu tiên **`pyw -3`** (Python Launcher không có cửa sổ), sau đó thử `pythonw.exe` đã xác minh chạy Python 3 cho Windows. File `.bat` cũng chuyển sang cách chạy nền này rồi thoát, nhưng cửa sổ console có thể lóe lên khi nhấp đúp `.bat`; dùng trực tiếp `.vbs` để không xuất hiện console.

Thông báo hoạt động và lỗi được ghi vào **`tools/ssms_assistant/assistant.log`**. Nếu cần chạy có console để kiểm tra, dùng PowerShell tại thư mục gốc dự án:

```powershell
py -3 tools\ssms_assistant\ssms_daemon.py
```

Nếu báo không tìm thấy Python, kiểm tra `py -3 --version`. Nếu lệnh này cũng thất bại, cài Python 3 cho Windows kèm Python Launcher. **Sau khi cập nhật mã nguồn, nhấn Ctrl + Alt + Q để tắt trợ lý cũ rồi mở lại file `.vbs`.** Nếu phiên cũ được mở trước khi có phím tắt thoát này, đóng cửa sổ Terminal của phiên cũ một lần rồi mở `.vbs`.

| Phím | Chức năng |
| --- | --- |
| **F8 / Alt + Q** | Ưu tiên mẫu offline; yêu cầu chưa có mẫu sẽ thử Gemini. |
| **Ctrl + Q** | Gọi Gemini cho yêu cầu hiện tại. Nếu lỗi, chèn thông báo nguyên nhân; không thay bằng mẫu offline. |
| **F9** | Tăng tốc khi đang chèn SQL. |
| **Esc** | Dừng chèn và mở khóa bàn phím, kể cả khi đang chờ metadata/API. Tác vụ mạng có thể còn chờ trả về, nhưng kết quả đã hủy sẽ không được chèn. |
| **Ctrl + Alt + Q** | Tắt hẳn trợ lý nền và gỡ các phím tắt của trợ lý. Mở lại file `.vbs` khi cần dùng tiếp. |

Bàn phím thường tạm khóa trong lúc trợ lý xử lý và chèn SQL. **Esc chỉ hủy lần chèn hiện tại; Ctrl + Alt + Q tắt chương trình.** Dòng bắt đầu bằng `---` hoặc kết thúc bằng `.` / `?` cũng ép gọi Gemini, kể cả khi nhấn F8.

Các ví dụ dùng được với F8, không cần API key hay mạng:

```sql
-- metadata
-- xem code sp_ChotBangLuong
-- test trigger chấm công kỳ đã chốt
-- tìm 3 nhân viên lương cao nhất tháng 10 năm 2026
-- thống kê quỹ lương theo phòng ban tháng 10 năm 2026
-- test hàm tính thực nhận
-- tv4
-- đo hiệu năng index
```

Chạy từng yêu cầu riêng. `tv4` đọc dữ liệu lương hiện có và tính lại tiền công để đối chiếu. Truy vấn lương không nêu kỳ sẽ chọn kỳ gần nhất có dữ liệu phù hợp; có nêu tháng/năm thì lọc đúng kỳ đó.

SQL thông thường dùng **database đang chọn trong Query**, không thêm `use` hoặc `go` dư thừa. Truy vấn đọc dùng trực tiếp bảng/view hiện có, không thêm khối khai báo biến và xử lý lỗi khi không cần; không có dữ liệu thì trả kết quả rỗng. Thao tác thay đổi dữ liệu vẫn kiểm tra mục tiêu và báo lỗi nếu thiếu dòng phù hợp, không tự tạo database, object hoặc dữ liệu mẫu. Kiểm tra trigger dùng dòng có sẵn và rollback thay đổi; yêu cầu hoàn tất transaction hiện tại trước khi chạy. Benchmark chỉ đo dữ liệu/index hiện có, không sinh thêm 10.000 dòng giả.

Yêu cầu sửa định nghĩa rõ ràng, như `-- sửa hàm thực nhận âm thì về 0`, sinh `ALTER` cho object đã có và giữ `GO` cần thiết để tách batch. Các lệnh chốt/mở kỳ, phân quyền, bật/tắt trigger hoặc sửa định nghĩa sẽ thay đổi database nếu bạn thực thi chúng.

Muốn gọi thủ tục tính lại một kỳ **đã tồn tại**, dùng:

```sql
-- chạy sp_TinhBangLuongThang tháng 10 năm 2026
```

Trợ lý chọn kỳ chưa chốt phù hợp rồi sinh lời gọi thủ tục đã cài đặt. **Nhấn F5 với SQL này sẽ tính lại dữ liệu lương thực tế của kỳ đó.** Nếu kỳ không tồn tại hoặc không phù hợp, chương trình báo lỗi; không chuẩn bị dữ liệu mẫu.

Để dùng Gemini, đặt key trong **`tools/ssms_assistant/api_key.txt`**: chỉ dán API key vào một dòng, không thêm dấu nháy. Hoặc đặt biến môi trường **`GEMINI_API_KEY`**, được ưu tiên hơn file. File key đã có trong `.gitignore`; không chia sẻ key. Có thể đặt **`GEMINI_MODEL`** để chọn model nếu cần, rồi khởi động lại trợ lý.

Chế độ AI đọc cấu trúc thực tế bằng **`sqlcmd`** với Windows Authentication (`-E`). Cần có `sqlcmd` trong PATH và tài khoản Windows đang chạy trợ lý phải kết nối được, có quyền đọc metadata của database. Quyền này độc lập với tài khoản SQL Authentication bạn dùng trong SSMS. Chỉ metadata được đọc; không gửi các dòng dữ liệu hay ID mẫu làm ngữ cảnh schema.

Khi dùng phím tắt, server/database được lấy từ tiêu đề cửa sổ Query SSMS hiện tại. Nếu không nhận diện được Query đã kết nối, trợ lý yêu cầu chọn lại cửa sổ đó. Metadata được cache theo server/database tối đa **60 giây**. Khi gọi bộ sinh SQL bằng mã riêng, có thể đặt **`SSMS_SQL_SERVER`** và **`SSMS_SQL_DATABASE`** làm đích dự phòng; mặc định là `localhost` và `QuanLyNhanSuTienLuong`.

Nếu Ctrl + Q báo lỗi, đọc thông báo được chèn vào Query để kiểm tra API key, kết nối, model hoặc quyền đọc metadata. Các mẫu offline của F8 vẫn dùng được khi không có Gemini; chúng cần đúng schema dự án và dữ liệu thực tế phù hợp khi bạn nhấn F5.
