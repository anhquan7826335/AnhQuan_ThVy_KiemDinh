# Hướng dẫn chạy kiểm thử từ đầu (dành cho người mới)

Tài liệu này hướng dẫn từng bước trên **Windows (PowerShell)**: cài công cụ, chạy ứng dụng, rồi chạy lần lượt JUnit, Postman, JMeter và Selenium. Bạn không cần biết trước các công cụ này.

> Quy ước: `<repo>` là thư mục bạn tải/clone repo về, ví dụ `C:\Users\ADMIN\AnhQuan_ThVy_KiemDinh`. Thư mục dự án là:
> `<repo>\app\MainProject\QuanLyThuVienNhacOnline\web` (gọi tắt là **thư mục `web`**).

## 0. Tổng quan: cần chạy gì, theo thứ tự nào

| Bước | Công cụ | Cần chạy ứng dụng trước? | Cần DB thật? | Thời gian |
|---|---|---|---|---|
| 1 | JUnit (`mvn test`) | Không | Không (dùng H2 trong bộ nhớ) | 1–2 phút |
| 2 | Chạy ứng dụng | – | Không, nếu dùng cách A ở mục 2 | 1 phút |
| 3 | Postman | **Có** | theo mục 2 | 5 phút |
| 4 | JMeter | **Có** | theo mục 2 | 5–15 phút |
| 5 | Selenium | Không (test website demo) | Không | 3 phút |

Nên làm theo đúng thứ tự trên. Bước 1 và 5 độc lập, không cần ứng dụng.

## 1. Cài đặt công cụ (làm một lần)

| Công cụ | Phiên bản | Tải ở đâu | Kiểm tra đã cài |
|---|---|---|---|
| Java JDK | 17 trở lên | https://adoptium.net | `java -version` |
| Maven | 3.9 (không bắt buộc, dự án có sẵn `mvnw.cmd`) | https://maven.apache.org/download.cgi | `mvn -v` |
| Postman | bản mới nhất | https://www.postman.com/downloads/ | mở được ứng dụng |
| Apache JMeter | 5.6.3 | https://jmeter.apache.org/download_jmeter.cgi (tải file *Binary .zip*) | `jmeter -v` (sau khi thêm vào PATH) hoặc mở `bin\jmeter.bat` |
| Python | 3.9 trở lên | https://www.python.org/downloads/ (tick **Add Python to PATH** khi cài) | `python --version` |
| Google Chrome | bản mới nhất | https://www.google.com/chrome/ | mở được Chrome |
| Git (nếu clone repo) | bất kỳ | https://git-scm.com | `git --version` |

Mở PowerShell: bấm phím Windows, gõ `powershell`, Enter. Để vào thư mục `web`:

```powershell
cd C:\Users\ADMIN\AnhQuan_ThVy_KiemDinh\app\MainProject\QuanLyThuVienNhacOnline\web
dir pom.xml        # phải thấy file pom.xml
```

Nếu `mvn` báo *not recognized*, thay `mvn` bằng `.\mvnw.cmd` ở mọi lệnh bên dưới (Maven đi kèm dự án).

## 2. Chạy ứng dụng (cần cho Postman và JMeter)

Ứng dụng mặc định kết nối MySQL trên cloud (file `src\main\resources\application.properties`, mật khẩu là `YOUR_PASSWORD`). Có hai cách.

### Cách A – Không cần database (nhanh nhất, dùng để học và test)

Chạy với H2 trong bộ nhớ. Dữ liệu mất khi tắt ứng dụng, phù hợp để kiểm thử:

```powershell
cd <repo>\app\MainProject\QuanLyThuVienNhacOnline\web
mvn spring-boot:run "-Dspring-boot.run.useTestClasspath=true"
```

Khi thấy dòng `Started MusicManagerApplication` thì ứng dụng đã chạy. Mở trình duyệt vào http://localhost:8080 để kiểm tra.

### Cách B – Dùng MySQL thật

Đặt mật khẩu qua biến môi trường (không sửa vào file để tránh commit nhầm mật khẩu):

```powershell
$env:SPRING_DATASOURCE_PASSWORD = "mat-khau-that-cua-ban"
mvn spring-boot:run
```

Muốn dừng ứng dụng: bấm `Ctrl + C` trong cửa sổ PowerShell đó. **Giữ cửa sổ này mở** trong lúc chạy Postman/JMeter; hãy mở thêm cửa sổ PowerShell mới cho các lệnh khác.

### Tạo tài khoản dùng để kiểm thử

1. Vào http://localhost:8080/register.
2. Đăng ký tài khoản thường, ví dụ username `tester`, email `tester@test.com`, mật khẩu `Passw0rd123` (tối thiểu 8 ký tự).
3. Dùng tài khoản này cho JMeter. (Postman tự tạo tài khoản, không cần làm bước này.)

## 3. Chạy JUnit

JUnit là các bài test viết bằng code, nằm trong `src\test\java`. **Không cần chạy ứng dụng và không cần database.**

```powershell
cd <repo>\app\MainProject\QuanLyThuVienNhacOnline\web
mvn clean test
```

Lần đầu Maven tải thư viện nên hơi lâu. Đọc kết quả ở cuối màn hình:

```
Tests run: 138, Failures: 6, Errors: 0, Skipped: 1
```

| Từ khóa | Ý nghĩa |
|---|---|
| `Failures` | Test chạy được nhưng kết quả sai so với mong đợi |
| `Errors` | Test bị lỗi khi chạy (ví dụ không khởi động được) – **cần xử lý** |
| `Skipped` | Test bị bỏ qua có chủ đích (`@Disabled`) |

**Kết quả đúng của dự án hiện tại:** 138 test, **6 failures**, 0 errors, 1 skipped. 6 failures là lỗi thật của ứng dụng (lỗi D1 trong README), không phải do bạn chạy sai.

Mẹo:

```powershell
mvn test "-Dtest=UserServiceTest"                 # chạy một lớp test
mvn test "-Dtest=SongControllerTest#createInvalid" # chạy một test
```

Báo cáo chi tiết (từng test pass/fail, thông báo lỗi) nằm ở `target\surefire-reports\`. Mở file `.txt` trong thư mục đó để xem lý do fail.

Chạy trong IDE: mở thư mục `web` bằng IntelliJ IDEA hoặc VS Code (cài *Extension Pack for Java*), chuột phải vào thư mục `src\test\java` → *Run Tests*.

## 4. Chạy Postman

Postman gửi các request HTTP vào ứng dụng đang chạy rồi tự kiểm tra kết quả. **Phải chạy ứng dụng trước (mục 2).**

1. Mở Postman → **Import** → chọn file `postman\MusicLibrary.postman_collection.json`.
2. Bấm vào collection **Music Library – API Tests** → tab **Variables** → kiểm tra `baseUrl` là `http://localhost:8080` (đổi nếu ứng dụng chạy cổng khác) → **Save**.
3. Bấm dấu `…` cạnh collection → **Run collection** (hoặc nút **Run**).
4. Giữ nguyên thứ tự thư mục 00 → 06, bấm **Run Music Library – API Tests**.
5. Xem kết quả: màu xanh là pass, đỏ là fail; bấm vào từng request để xem lý do.

Lưu ý:

- Phải chạy **cả collection theo thứ tự**, vì các bước sau dùng token và tài khoản tạo ở các bước trước. Chạy lẻ từng request có thể bị lỗi 403 (thiếu CSRF).
- Collection tự đăng ký một tài khoản mới mỗi lần chạy, nên chạy lại nhiều lần vẫn được.
- Một số request **cố ý** mong `400/404` để chứng minh lỗi D1; chúng sẽ **fail** cho đến khi lỗi được sửa. Đó là kết quả bình thường.
- Chụp ảnh màn hình trang kết quả Runner để dán vào README (mục 6).

Chạy bằng dòng lệnh (tùy chọn, cần Node.js): 

```powershell
npm install -g newman
newman run postman\MusicLibrary.postman_collection.json
```

## 5. Chạy JMeter

JMeter mô phỏng nhiều người dùng truy cập cùng lúc để đo thời gian phản hồi. **Phải chạy ứng dụng trước (mục 2)** và đã có tài khoản kiểm thử (mục 2, phần cuối).

Kịch bản: `app\MainProject\QuanLyThuVienNhacOnline\web\src\test\jmeter\music-library-load-test.jmx`.

**Bước 1 – chạy thử nhẹ (1 người dùng, 15 giây)** để chắc kịch bản đúng:

```powershell
cd <thư-mục-giải-nén-jmeter>\bin
.\jmeter.bat -n -t "<repo>\app\MainProject\QuanLyThuVienNhacOnline\web\src\test\jmeter\music-library-load-test.jmx" -Jthreads=1 -Jrampup=1 -Jduration=15 -Jusername=tester -Jpassword=Passw0rd123 -l "$HOME\smoke.jtl"
```

Cuối lệnh sẽ có bảng tổng kết. Nếu `Err` là `0` thì kịch bản chạy đúng. Nếu 100% lỗi, thường do sai `username/password` hoặc ứng dụng chưa chạy.

**Bước 2 – chạy 3 mức tải** (mỗi mức 180 giây ≈ 3 phút), thay đổi tên file kết quả và thư mục báo cáo cho mỗi lần:

```powershell
# Mức thấp: 20 người dùng
.\jmeter.bat -n -t "<đường-dẫn-.jmx>" -Jthreads=20 -Jrampup=10 -Jduration=180 -Jusername=tester -Jpassword=Passw0rd123 -l "$HOME\low.jtl" -e -o "$HOME\bao-cao-low"

# Mức trung bình: 50 người dùng
.\jmeter.bat -n -t "<đường-dẫn-.jmx>" -Jthreads=50 -Jrampup=20 -Jduration=180 -Jusername=tester -Jpassword=Passw0rd123 -l "$HOME\medium.jtl" -e -o "$HOME\bao-cao-medium"

# Mức cao: 100 người dùng
.\jmeter.bat -n -t "<đường-dẫn-.jmx>" -Jthreads=100 -Jrampup=30 -Jduration=180 -Jusername=tester -Jpassword=Passw0rd123 -l "$HOME\high.jtl" -e -o "$HOME\bao-cao-high"
```

Ý nghĩa tham số: `-n` chạy không giao diện; `-t` file kịch bản; `-J...` tham số; `-l` file lưu kết quả thô; `-e -o` xuất báo cáo HTML vào thư mục (**thư mục phải chưa tồn tại hoặc rỗng**).

**Đọc kết quả:** mở file `index.html` trong thư mục báo cáo bằng trình duyệt. Các cột cần lấy cho README (mục 7):

| Cột | Ý nghĩa |
|---|---|
| Average | Thời gian phản hồi trung bình (ms) |
| 95th pct | 95% yêu cầu nhanh hơn mức này (ms) |
| Error % | Tỉ lệ yêu cầu lỗi |
| Throughput | Số yêu cầu xử lý mỗi giây |

Tiêu chí đặt ra: lỗi < 1%, trung bình < 1000 ms, 95% < 2000 ms.

Muốn xem bằng giao diện: chạy `bin\jmeter.bat`, chọn *File → Open* → mở file `.jmx`, vào *User Defined Variables/Thread Group* để xem, bấm nút ▶ (Start). **Không dùng giao diện để chạy tải lớn** vì làm sai lệch kết quả.

## 6. Chạy Selenium

Selenium điều khiển trình duyệt Chrome thật để kiểm thử giao diện. Phần này test website demo https://www.saucedemo.com, **không cần chạy ứng dụng nhạc, nhưng cần Internet**.

```powershell
cd <repo>\selenium
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
pytest
```

Chrome sẽ tự mở và thao tác. Kết quả cuối cùng dạng `10 passed`.

| Tình huống | Cách xử lý |
|---|---|
| `Activate.ps1 cannot be loaded because running scripts is disabled` | Chạy `Set-ExecutionPolicy -Scope Process Bypass` rồi kích hoạt lại |
| `python is not recognized` | Cài lại Python và tick *Add Python to PATH*; mở PowerShell mới |
| Muốn chạy ẩn trình duyệt | `$env:HEADLESS = "1"; pytest` |
| Chạy riêng một test | `pytest tests/test_saucedemo.py::test_tc10_logout` |
| Test fail | Xem ảnh chụp tự động trong thư mục `screenshots\` |
| Lỗi tải ChromeDriver | Kiểm tra Internet; cập nhật Chrome; chạy `pip install -U selenium` |

## 7. Lỗi thường gặp

| Lỗi | Nguyên nhân thường gặp | Cách xử lý |
|---|---|---|
| `mvn is not recognized` | Chưa cài Maven | Dùng `.\mvnw.cmd` thay cho `mvn` |
| `JAVA_HOME is not defined` hoặc phiên bản Java sai | Chưa cài hoặc sai JDK | Cài JDK 17+; đặt biến môi trường `JAVA_HOME` trỏ tới thư mục JDK |
| `Port 8080 was already in use` | Có chương trình khác đang dùng cổng 8080 | Tắt chương trình đó, hoặc chạy với `--server.port=9090` và đổi `baseUrl` cho khớp |
| `Communications link failure` hoặc `Access denied` khi chạy ứng dụng (cách B) | Sai mật khẩu hoặc không có mạng tới DB | Kiểm tra `SPRING_DATASOURCE_PASSWORD`; hoặc dùng cách A |
| Postman trả 403 | Thiếu CSRF token | Chạy cả collection theo thứ tự từ thư mục 00 |
| Postman trả 302 về `/login` | Chưa đăng nhập | Chạy từ đầu, không chạy lẻ |
| JMeter báo 100% lỗi | Sai tài khoản/mật khẩu hoặc ứng dụng chưa chạy | Đăng ký lại tài khoản; kiểm tra http://localhost:8080 |
| JMeter báo thư mục báo cáo đã tồn tại | `-o` yêu cầu thư mục rỗng | Xóa hoặc đổi tên thư mục báo cáo |
| `mvn test` báo `Errors` khác 0 | Lỗi biên dịch hoặc cấu hình | Xóa thư mục `target` rồi chạy lại `mvn clean test`; gửi nội dung lỗi đầu tiên cho người hỗ trợ |

## 8. Danh sách việc cần làm khi nộp bài

1. Chạy `mvn clean test` → chụp màn hình dòng `Tests run:` → điền README mục 5.
2. Chạy Postman Runner → chụp màn hình kết quả → điền README mục 6.
3. Chạy JMeter 3 mức tải → lấy Average, 95th pct, Error %, Throughput → điền bảng README mục 7.
4. Chạy `pytest` trong thư mục `selenium` → chụp kết quả → điền `selenium\README.md`.
5. Cập nhật cột *Trạng thái* trong `docs\TestCase_QuanLyThuVienNhac.xlsx` cho các ca vừa chạy (Pass/Fail).
6. Đặt ảnh chụp vào thư mục `docs\images\` và chèn vào README bằng `![mô tả](docs/images/ten-anh.png)`.
7. Đẩy lên GitHub:

```powershell
cd <repo>
git add .
git commit -m "Them bao cao kiem thu"
git push
```