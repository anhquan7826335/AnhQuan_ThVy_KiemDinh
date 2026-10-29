# Kiểm thử và đánh giá chất lượng hệ thống quản lý thư viện nhạc

| | |
|---|---|
| **Sinh viên** | Anh Quân - 23010680, Thảo Vy - 23010588 |
| **Môn học** | Kiểm định phần mềm |
| **Hệ thống được kiểm thử** | Music Library – quản lý thư viện nhạc online (Spring Boot 3.5, Spring Data JPA, Spring Security, Thymeleaf, MySQL) |
| **Công cụ** | JUnit 5, Mockito, MockMvc, H2, Postman, Apache JMeter 5.6.3, Selenium WebDriver |

## 1. Mục tiêu và phạm vi

Đề tài tập trung kiểm thử và đánh giá chất lượng hệ thống quản lý thư viện nhạc, nhằm phát hiện lỗi và đánh giá độ ổn định của hệ thống. Nội dung gồm kiểm thử các chức năng: **đăng ký, đăng nhập, tìm kiếm và phát nhạc, quản lý danh sách bài hát, quản lý playlist, danh sách yêu thích**, kết hợp kiểm thử hiệu năng cơ bản.

Đối chiếu với mã nguồn hiện tại:

| Chức năng theo đề tài | Hiện trạng trong hệ thống | Cách xử lý trong báo cáo |
|---|---|---|
| Đăng ký, đăng nhập, đăng xuất, phân quyền | Đã có (`AuthController`, `SecurityConfig`, mã hóa BCrypt, `/users` chỉ cho ADMIN) | Kiểm thử đầy đủ |
| Tìm kiếm bài hát | Đã có (`/songs/search`, theo tên, nghệ sĩ, thể loại) | Kiểm thử đầy đủ |
| Quản lý bài hát, nghệ sĩ, thể loại | Đã có (CRUD) | Kiểm thử đầy đủ |
| Phát nhạc | **Chưa có trình phát nhúng**; chỉ có liên kết mở file ở cột *Audio File Path* | Kiểm thử liên kết; ca Play/Pause ghi *Blocked* |
| Quản lý playlist | **Chưa triển khai** | Test case viết sẵn, ghi *Blocked* (lỗi/khoảng trống **D7**) |
| Danh sách yêu thích | **Chưa triển khai** | Test case viết sẵn, ghi *Blocked* (**D7**) |

## 2. Cấu trúc repo

```
.
├── app/MainProject/QuanLyThuVienNhacOnline/web/
│   ├── src/main/…                         # Mã nguồn hệ thống
│   ├── src/test/java/…                    # Test JUnit
│   └── src/test/jmeter/                   # Kịch bản JMeter + hướng dẫn chạy
├── docs/
│   ├── TestCase_QuanLyThuVienNhac.xlsx    # Bảng test case (242 ca, 11 sheet, có kết quả)
│   └── BaoCao_JMeter.md                   # Báo cáo chi tiết lần chạy JMeter
├── postman/MusicLibrary.postman_collection.json   # 41 request kiểm thử endpoint
├── selenium/                              # Kiểm thử giao diện tự động (Selenium + pytest)
└── README.md
```

## 3. Chiến lược kiểm thử

| Tầng | Công cụ | Đối tượng | Mục |
|---|---|---|---|
| Unit test | JUnit 5 + Mockito | `ArtistService`, `GenreService`, `SongService`, `UserService` | 5 |
| Controller / Integration | MockMvc standalone, `@DataJpaTest` + H2 | 5 controller, `GlobalException`, luồng CRUD | 5 |
| Endpoint / API | Postman (Newman) | Đăng ký, đăng nhập, phân quyền, CSRF, CRUD, đăng xuất | 6 |
| Hiệu năng | Apache JMeter | Đăng nhập và các trang đọc dữ liệu | 7 |
| Giao diện | Selenium WebDriver + pytest | Đăng ký, đăng nhập, phân quyền, tìm kiếm, quản lý bài hát/nghệ sĩ/thể loại trên chính hệ thống | 8 |

Mỗi test case có mã (ví dụ `TC_ART_13`), dữ liệu vào, kết quả mong đợi, loại (Positive/Negative) và mức ưu tiên. Test service ghi mã TC trong `@DisplayName`, nên truy vết được từ bảng test case đến code.

## 4. Bảng test case và thống kê

File: [`docs/TestCase_QuanLyThuVienNhac.xlsx`](docs/TestCase_QuanLyThuVienNhac.xlsx). Sheet **Tổng quan** tự đếm theo cột Trạng thái. Trạng thái Pass/Fail được ghi từ kết quả chạy thật (JUnit, JMeter); phần còn lại là *Chưa test* hoặc *Blocked*.

| Nhóm | Số ca | Pass | Fail | Blocked | Chưa test |
|---|---:|---:|---:|---:|---:|
| Artist (Service) | 27 | 24 | 0 | 0 | 3 |
| Genre (Service) | 27 | 24 | 0 | 0 | 3 |
| Song (Service) | 25 | 24 | 0 | 0 | 1 |
| User (Service) | 21 | 20 | 0 | 0 | 1 |
| Controller | 39 | 32 | 6 | 0 | 1 |
| Exception – Khác | 13 | 8 | 0 | 1 | 4 |
| Đăng ký – Đăng nhập | 26 | 26 | 0 | 0 | 0 |
| Phát nhạc – Tìm kiếm | 23 | 21 | 0 | 2 | 0 |
| Playlist | 17 | 0 | 0 | 17 | 0 |
| Yêu thích | 12 | 0 | 0 | 12 | 0 |
| Hiệu năng JMeter | 12 | 10 | 2 | 0 | 0 |
| **Tổng** | **242** | **190** | **8** | **32** | **12** |

Các nhóm *Đăng ký – Đăng nhập* và *Phát nhạc – Tìm kiếm* sẽ có kết quả sau khi chạy Postman (mục 6), Selenium (mục 8) hoặc kiểm thử thủ công trên giao diện. Các ca *Chưa test* ở nhóm Service là test repository, hiện chưa có trong repo.

## 5. Kiểm thử mã nguồn bằng JUnit

Test nằm tại `app/MainProject/QuanLyThuVienNhacOnline/web/src/test/java/com/musicmanager/web/`:

| Thư mục | Nội dung |
|---|---|
| `service/` | Unit test 4 service bằng Mockito: nhánh hợp lệ và không hợp lệ (tên rỗng, trùng tên, id không tồn tại, mật khẩu ngắn, mã hóa mật khẩu, `register`...) |
| `controller/` | MockMvc standalone cho 5 controller (`TestMvc` dựng sẵn, không nạp Spring context nên không phụ thuộc Security/DB) |
| `exception/` | `GlobalException`: ánh xạ 404, 400, 500 và tên view `error/...` |
| `integration/` | Luồng tạo → xem → sửa → tìm → xóa cho từng module trên H2 (`@DataJpaTest`, rollback sau mỗi test) |

`src/test/resources/application.properties` dùng **H2 trong bộ nhớ** để test không phụ thuộc MySQL trên cloud và không làm bẩn dữ liệu thật.

```bash
cd app/MainProject/QuanLyThuVienNhacOnline/web
mvn clean test
```

### Kết quả chạy (lần chạy gần nhất, `ketqua.txt`)

| Lớp test | Số test | Kết quả |
|---|---:|---|
| `ArtistServiceTest` | 23 | Pass |
| `GenreServiceTest` | 23 | Pass |
| `SongServiceTest` | 24 | Pass |
| `UserServiceTest` | 21 | Pass |
| `CrudFlowTest` | 4 | Pass |
| `GlobalExceptionTest` | 4 | Pass |
| `HomeControllerTest` | 1 | Pass |
| `ArtistControllerTest` | 10 | 9 Pass, 1 Fail (`searchMissingParam`) |
| `GenreControllerTest` | 10 | 9 Pass, 1 Fail (`searchMissingParam`) |
| `SongControllerTest` | 9 | 6 Pass, 3 Fail (`createInvalid`, `updateNotFound`, `deleteNotFound`) |
| `UserControllerTest` | 8 | 7 Pass, 1 Fail (`searchMissingParam`) |
| `WebApplicationTests` | 1 | Skipped (`@Disabled`: cần khởi động toàn bộ ứng dụng) |
| **Tổng** | **138** | **131 Pass, 6 Fail, 0 Error, 1 Skipped** |

Cả 6 test fail đều cùng nguyên nhân: lỗi thật **D1** của ứng dụng (mục 9), không phải lỗi của test. Test được giữ nguyên để làm bằng chứng cho đến khi lỗi được sửa.

## 6. Kiểm thử endpoint bằng Postman

Ứng dụng render HTML (Thymeleaf), nên mỗi request dùng `form-urlencoded` và kiểm tra **mã trạng thái** cùng **header `Location` (redirect)** thay vì JSON.

**Cách dùng**

1. Postman → Import → chọn `postman/MusicLibrary.postman_collection.json`.
2. Kiểm tra biến collection `baseUrl` (mặc định `http://localhost:8080`).
3. Chạy ứng dụng, sau đó chạy collection theo thứ tự thư mục (Runner). Không cần tài khoản admin: collection tự đăng ký một tài khoản thường, đăng nhập và dùng tài khoản đó.
4. Dòng lệnh (tùy chọn): `newman run postman/MusicLibrary.postman_collection.json`.

Request `00` và `GET /songs` sau đăng nhập tự lấy `_csrf` token (Spring Security đổi token sau khi đăng nhập).

| Thư mục | Số request | Nội dung chính |
|---|---:|---|
| 00 – Chuẩn bị | 1 | lấy CSRF token |
| 01 – Đăng ký | 7 | hợp lệ, trùng username, mật khẩu 7 và 8 ký tự, thiếu email/username, cố gửi `role=Admin` |
| 02 – Đăng nhập và phân quyền | 11 | truy cập khi chưa login, thiếu CSRF (403), sai mật khẩu, SQL injection, đăng nhập đúng, user thường vào `/users` (403) |
| 03 – Bài hát | 8 | danh sách, tìm kiếm, ký tự đặc biệt, tạo, tạo thiếu dữ liệu, sửa/xóa id không tồn tại |
| 04 – Nghệ sĩ | 7 | danh sách, tìm kiếm, tạo, tên rỗng, xóa id không tồn tại |
| 05 – Thể loại | 5 | tương tự nghệ sĩ |
| 06 – Đăng xuất | 2 | `POST /logout`, sau đó truy cập lại bị chuyển về `/login` |

Một số request cố ý mong `400/404` để làm bằng chứng cho lỗi **D1**; các request này sẽ báo Fail cho đến khi lỗi được sửa. Playlist và yêu thích không có trong collection vì hệ thống chưa có các endpoint này.

### Kết quả chạy


| Chỉ số | Giá trị |
|---|---|
| Số request / assertion | 40 |
| Pass / Fail | 35 / 5|

## 7. Kiểm thử hiệu năng bằng JMeter

Kịch bản: `app/MainProject/QuanLyThuVienNhacOnline/web/src/test/jmeter/music-library-load-test.jmx` (hướng dẫn chạy trong `README.md` cùng thư mục). Mỗi người dùng ảo đăng nhập một lần, giữ phiên riêng, rồi lặp: trang chủ, danh sách thể loại, tìm kiếm thể loại, danh sách nghệ sĩ, danh sách bài hát, tìm kiếm bài hát. Kịch bản chỉ đọc dữ liệu, có think time 1–3 giây và assertion HTTP 200.

Các mức tải đã định nghĩa: Thấp (20 người dùng), Trung bình (50), Cao (100), mỗi mức 180 giây.

### Kết quả lần chạy đã thực hiện

Cấu hình: **120 luồng, ramp-up 1 giây**, thời lượng 15 giây (chi tiết trong [`docs/BaoCao_JMeter.md`](docs/BaoCao_JMeter.md)).

| Request | Số mẫu | Trung bình (ms) | Min (ms) | Max (ms) | Lỗi % |
|---|---:|---:|---:|---:|---:|
| GET login page | 120 | 4 | 2 | 8 | 0.00 |
| POST login | 120 | 282 | 226 | 492 | 0.00 |
| R01 Home page | 120 | 4 | 2 | 8 | 0.00 |
| R02 Genre list | 120 | 9,291 | 1,482 | 16,757 | 0.00 |
| R03 Search genres | 31 | 13,656 | 5,427 | 15,228 | 0.00 |
| **Tổng** | **511** | **3,078** | 2 | 16,757 | **0.00** |

Thông lượng khoảng 18.3 mẫu/giây.

**Nhận xét**

- Không có request nào lỗi, nhưng danh sách và tìm kiếm thể loại rất chậm (trung bình 9.3 giây và 13.7 giây), vượt xa tiêu chí < 1 giây đã đặt ra. Trang chủ và đăng nhập nhanh.
- Trang chủ không truy vấn DB còn các trang chậm đều truy vấn DB, nên điểm nghẽn nhiều khả năng nằm ở truy vấn hoặc kết nối tới MySQL trên cloud (độ trễ mạng, số kết nối tối đa của pool mặc định) khi 120 luồng vào cùng lúc. Đây là **giả thuyết, chưa kiểm chứng**; cần đo thêm (ví dụ bật log thời gian truy vấn, tăng `spring.datasource.hikari.maximum-pool-size`, chạy lại).
- Lần chạy chỉ ghi nhận 31 mẫu tìm kiếm thể loại và chưa có mẫu cho nghệ sĩ, bài hát, tìm kiếm bài hát, nên chưa đủ để kết luận cho toàn bộ hành trình. Chưa có percentile 90/95.
- Lỗi 0.00% chỉ đúng với lần chạy này, không suy ra hệ thống chịu được mọi mức tải.

**Việc cần làm tiếp:** chạy lại theo ba mức tải 20/50/100 người dùng trong 180 giây, xuất báo cáo HTML (`-e -o`) để có percentile, và bổ sung bảng kết quả bên dưới.

| Mức tải | Người dùng | Trung bình (ms) | 95% (ms) | Lỗi % | Thông lượng (mẫu/giây) |
|---|---:|---:|---:|---:|---:|
| Thấp | 20 | 6.81 | 8 | 	0.44 | 	121.16 |
| Trung bình | 50 | 7.01 | 6 | 0.45 | 62.43 |
| Cao | 100 | 5.78 | 6 | 0.46 | 25.46 |

## 8. Lỗi và rủi ro phát hiện

Nguồn: kết quả chạy JUnit/JMeter và rà soát mã nguồn.

| Mã | Mô tả | Mức độ | Nguồn |
|---|---|---|---|
| **D1** | `GlobalException` có handler bắt `Exception` chung nên `ResponseStatusException` (Song) và thiếu tham số bắt buộc bị trả **500** thay vì 400/404. Làm fail 6 test: `searchMissingParam` (Artist, Genre, User), `createInvalid`, `updateNotFound`, `deleteNotFound` (Song). Cách sửa: thêm handler cho `ResponseStatusException` và `MissingServletRequestParameterException`. | Trung bình | Đã chạy test |
| **D2** | `UserController` xóa người dùng bằng `GET /users/delete/{id}`. GET không được bảo vệ bởi CSRF, nên một liên kết hoặc thẻ `<img>` trên trang khác có thể khiến quản trị viên xóa tài khoản vô ý. Nên dùng `POST`. | Cao | Rà soát code |
| **D3** | `UserService.deleteUser` không kiểm tra id tồn tại; `updateUser` ghi đè `null` lên dữ liệu cũ (khác Artist/Song). | Thấp | Rà soát code |
| **D4** | `Song` lưu nghệ sĩ và thể loại dạng chuỗi, không liên kết với bảng `Artist`/`Genre`, nên dữ liệu có thể không nhất quán (nghệ sĩ bị xóa vẫn còn trong bài hát). | Trung bình | Rà soát code |
| **D5** | Hiệu năng: danh sách và tìm kiếm thể loại trung bình 9.3 s và 13.7 s với 120 người dùng. Ngoài ra `SongService.searchSongs` tải **toàn bộ** bài hát rồi lọc trong bộ nhớ dù `SongRepository` đã có `findBy...ContainingIgnoreCase`, sẽ chậm khi dữ liệu lớn. | Cao | Đã chạy JMeter, rà soát code |
| **D6** | `application.properties` ghi cứng host và username DB trên cloud; mật khẩu để placeholder `YOUR_PASSWORD` nên dễ bị commit mật khẩu thật khi chạy. Nên dùng biến môi trường (ví dụ `${DB_PASSWORD}`). | Trung bình | Rà soát code |
| **D7** | Hệ thống **chưa có** playlist, danh sách yêu thích và trình phát nhúng, dù là yêu cầu của đề tài. Các test case tương ứng ghi *Blocked*. | Cao (về yêu cầu) | Đối chiếu yêu cầu |
| **D8** | `audioFilePath` không được kiểm tra: có thể lưu `javascript:...` hoặc đường dẫn sai, và liên kết được hiển thị nguyên trong `href` (rủi ro XSS khi bấm). | Trung bình | Rà soát code |
| **D9** | Test cũ `SongServiceTest` sai so với code, và test `@SpringBootTest` phụ thuộc DB thật. Đã sửa: dùng H2, viết lại test. | Thấp | Đã chạy test |

## 10. Đánh giá chất lượng

| Tiêu chí | Đánh giá |
|---|---|
| **Tính đúng đắn chức năng** | Tầng service đạt toàn bộ 91 test; luồng CRUD qua H2 đạt. Phần còn lỗi tập trung ở xử lý lỗi ở tầng controller (D1). |
| **Đầy đủ so với yêu cầu** | Đạt phần tài khoản, tìm kiếm và quản lý bài hát/nghệ sĩ/thể loại. **Thiếu** playlist, yêu thích, trình phát nhúng (D7). |
| **Xử lý lỗi** | Mã lỗi chưa đúng ngữ nghĩa: lỗi người dùng bị trả 500 (D1). |
| **Bảo mật** | Tốt: mật khẩu mã hóa BCrypt, bật CSRF, phân quyền `/users` chỉ cho ADMIN, role bị ép về User khi đăng ký. Cần xử lý: xóa bằng GET (D2), đường dẫn file không kiểm tra (D8), thông tin DB trong cấu hình (D6). Các điểm này mới ở mức rà soát, sẽ được kiểm chứng bằng Postman. |
| **Hiệu năng và độ ổn định** | Không lỗi ở lần chạy 120 người dùng, nhưng thời gian phản hồi các trang đọc dữ liệu rất cao (D5). Chưa kết luận được cho các mức tải khác. |
| **Khả năng bảo trì** | Cấu trúc tách tầng rõ; test có truy vết mã TC; còn trùng lặp code giữa Artist và Genre. |

## 11. Kết luận và hướng phát triển

Qua kiểm thử, hệ thống chạy ổn định về mặt chức năng cho các thao tác cơ bản (không có lỗi ở 131/138 test JUnit; không lỗi trong lần chạy JMeter), nhưng còn 3 nhóm vấn đề cần xử lý: (1) xử lý lỗi trả sai mã trạng thái (D1) – cả 6 test fail đều từ nguyên nhân này; (2) hiệu năng các trang thể loại rất chậm khi nhiều người dùng truy cập đồng thời (D5); (3) thiếu playlist, yêu thích và trình phát nhúng so với yêu cầu đề tài (D7).

Hướng phát triển:

1. Sửa D1 rồi chạy lại toàn bộ test (kiểm thử hồi quy); các test fail sẽ chuyển sang Pass.
2. Hoàn thiện playlist, yêu thích và trình phát nhúng, rồi kích hoạt các test case đang *Blocked*.
3. Phân tích và tối ưu truy vấn thể loại; chạy lại JMeter ở 3 mức tải và xuất báo cáo có percentile.
4. Đổi xóa người dùng sang `POST`, kiểm tra `audioFilePath`, đưa thông tin DB ra biến môi trường.
5. Đo độ phủ mã bằng JaCoCo; chạy test tự động trên GitHub Actions.

## 12. Tài liệu tham khảo

- JUnit 5: https://junit.org/junit5/docs/current/user-guide/
- Mockito: https://site.mockito.org/
- Spring Boot Testing: https://docs.spring.io/spring-boot/reference/testing/index.html
- Spring Security (CSRF): https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
- Postman Learning Center: https://learning.postman.com/
- Apache JMeter User Manual: https://jmeter.apache.org/usermanual/index.html
- Selenium Documentation: https://www.selenium.dev/documentation/
- pytest: https://docs.pytest.org/
