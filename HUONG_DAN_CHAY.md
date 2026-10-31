# Hướng dẫn chạy dự án Music Library

## 1. Chuẩn bị

- Cài JDK 17 và cấu hình `JAVA_HOME`, `PATH`.
- Cài và khởi động MySQL; chuẩn bị tài khoản có quyền tạo bảng trong cơ sở dữ liệu dùng cho dự án.
- Cần Internet trong lần chạy đầu để Maven tải các thư viện.

Kiểm tra Java trong PowerShell:

```powershell
java -version
```

## 2. Mở đúng thư mục ứng dụng

Mở PowerShell tại thư mục gốc dự án (nơi chứa file hướng dẫn này), rồi chạy:

```powershell
cd .\app\MainProject\QuanLyThuVienNhacOnline\web
```

Thư mục `web` chứa `pom.xml`, `mvnw.cmd` và mã nguồn Spring Boot. Các lệnh chạy ứng dụng bên dưới đều thực hiện tại đây. Có thể dùng Maven Wrapper có sẵn, không cần cài Maven riêng.

## 3. Tạo cơ sở dữ liệu và cấu hình kết nối

Trong MySQL Workbench hoặc MySQL client, chạy:

```sql
CREATE DATABASE IF NOT EXISTS music_library
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Bản dự án hiện tại chưa có `src/main/resources/application.properties`. Đặt cấu hình qua biến môi trường trong cửa sổ PowerShell dùng để chạy ứng dụng:

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://localhost:3306/music_library'
$env:SPRING_DATASOURCE_USERNAME = 'TEN_TAI_KHOAN_MYSQL'
$env:SPRING_DATASOURCE_PASSWORD = 'MAT_KHAU_MYSQL'
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'update'
$env:SERVER_PORT = '8080'
```

Thay tên tài khoản và mật khẩu bằng thông tin MySQL trên máy. Nếu MySQL chạy ở máy chủ hoặc cổng khác, sửa URL tương ứng. Hibernate sẽ tạo/cập nhật bảng khi khởi động; không cần tự viết các bảng trước. Các biến trên chỉ áp dụng trong phiên PowerShell hiện tại; khi mở cửa sổ mới cần đặt lại.

## 4. Khởi động ứng dụng

```powershell
.\mvnw.cmd spring-boot:run
```

Nếu máy đã cài Maven, có thể dùng `mvn spring-boot:run` thay thế.

Khi log báo ứng dụng đã khởi động thành công, truy cập:

- Trang chủ: <http://localhost:8080/>
- Đăng ký: <http://localhost:8080/register>
- Đăng nhập: <http://localhost:8080/login>

Đăng ký tài khoản mới rồi đăng nhập để truy cập bài hát (`/songs`), nghệ sĩ (`/artists`) và thể loại (`/genres`). Tài khoản đăng ký qua giao diện có quyền User; trang `/users` yêu cầu quyền ADMIN. Không có tài khoản quản trị mặc định được khai báo trong hướng dẫn này.

Nhấn `Ctrl+C` trong PowerShell để dừng ứng dụng.

## 5. Chạy kiểm thử (tùy chọn)

Tại thư mục `web`, chạy JUnit:

```powershell
.\mvnw.cmd test
```

Kết quả được lưu trong `target/surefire-reports/`. README của dự án ghi nhận một số test thất bại do lỗi xử lý mã trạng thái tại controller; cần xem kết quả lần chạy thực tế để đánh giá.

Lưu ý: bản hiện tại chưa có thư mục `src/test/resources` chứa cấu hình H2 như README mô tả. Không mặc định rằng mọi test đều đã được cấu hình chạy độc lập với MySQL. Nếu chạy test tích hợp với cấu hình kết nối ở trên, hãy dùng cơ sở dữ liệu dành riêng cho kiểm thử.

Các hướng dẫn kiểm thử khác:

- Selenium: [web/selenium/README.md](app/MainProject/QuanLyThuVienNhacOnline/web/selenium/README.md).
- JMeter: [src/test/jmeter/README.md](app/MainProject/QuanLyThuVienNhacOnline/web/src/test/jmeter/README.md).
- Postman: các request hiện có nằm trong thư mục `postman/collections/` tại gốc dự án.

## 6. Lỗi thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| Không nhận lệnh `java` hoặc sai phiên bản Java | Kiểm tra JDK 17, `JAVA_HOME` và `PATH`, sau đó mở lại PowerShell. |
| Maven báo không tìm thấy POM | Chuyển vào đúng thư mục `app/MainProject/QuanLyThuVienNhacOnline/web`. |
| `Failed to configure a DataSource` | Đặt đủ biến `SPRING_DATASOURCE_*` trong chính cửa sổ đang chạy ứng dụng. |
| `Access denied for user` | Kiểm tra tên tài khoản, mật khẩu và quyền MySQL. |
| `Unknown database` | Tạo cơ sở dữ liệu `music_library` hoặc sửa tên trong URL kết nối. |
| `Communications link failure` | Kiểm tra dịch vụ MySQL đang chạy, địa chỉ máy chủ và cổng kết nối. |
| Cổng 8080 đang được sử dụng | Đặt `$env:SERVER_PORT = '8081'`, chạy lại và mở `http://localhost:8081/`. |
| Không tải được thư viện Maven | Kiểm tra Internet và cấu hình proxy nếu có, sau đó chạy lại. |
| User truy cập `/users` nhận 403 | Đăng nhập bằng tài khoản có quyền ADMIN. |
