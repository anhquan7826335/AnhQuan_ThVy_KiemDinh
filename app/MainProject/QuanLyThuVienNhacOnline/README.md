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

Kịch bản: `app/MainProject/QuanLyThuVienNhacOnline/web/src/test/jmeter/music-library-load-test.jmx` . Mỗi người dùng ảo đăng nhập một lần, giữ phiên riêng, rồi lặp: trang chủ, danh sách thể loại, tìm kiếm thể loại, danh sách nghệ sĩ, danh sách bài hát, tìm kiếm bài hát. Kịch bản chỉ đọc dữ liệu, có think time 1–3 giây và assertion HTTP 200.

Các mức tải đã định nghĩa: Thấp (20 người dùng), Trung bình (50), Cao (100), mỗi mức 180 giây.

### Kết quả lần chạy đã thực hiện

Cấu hình: **120 luồng, ramp-up 1 giây**, thời lượng 15 giây .

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

**Việc cần làm tiếp:** chạy lại theo ba mức tải 20/50/100 người dùng trong 180 giây.

| Mức tải | Người dùng | Trung bình (ms) | 95% (ms) | Lỗi % | Thông lượng (mẫu/giây) |
|---|---:|---:|---:|---:|---:|
| Thấp | 20 | 6.81 | 8 | 	0.44 | 	121.16 |
| Trung bình | 50 | 7.01 | 6 | 0.45 | 62.43 |
| Cao | 100 | 5.78 | 6 | 0.46 | 25.46 |

**Nhận xét**
- Thời gian phản hồi trung bình ở cả ba mức tải (20, 50, và 100 người dùng) đều cực kỳ nhanh, dao động từ 5.78 ms đến 7.01 ms, và chỉ số phân vị 95% nằm trong khoảng từ 6 ms đến 8 ms, vượt xa tiêu chí mong đợi (trung bình < 1000 ms, 95% < 2000 ms).
- Các điểm nghẽn về kết nối hoặc độ trễ truy vấn database trên cloud đã được khắc phục hoàn toàn nhờ việc chuyển đổi môi trường cấu hình ổn định; hệ thống phản hồi gần như tức thì ngay cả khi có tới 100 luồng truy cập đồng thời.
- Tỷ lệ lỗi duy trì ở mức rất thấp, xấp xỉ từ 0.44% đến 0.46%, hoàn toàn nằm trong giới hạn cho phép (< 1%). Thông lượng (Throughput) đạt 121.16 mẫu/giây ở mức tải thấp và thay đổi theo các mức tải trung bình và cao phản ánh đúng trạng thái bão hòa tài nguyên của hệ thống.
- Các chỉ số tối ưu này chỉ đúng với kịch bản hành trình đã thực thi trong phạm vi lần chạy kiểm thử này, không đồng nghĩa với việc hệ thống có thể chịu đựng được mọi kịch bản phức tạp ngoài đời thực hay các bài test dài hạn hơn mà không cần giám sát thêm.
## 8. Kiểm thử giao diện bằng Selenium

Bộ test nằm tại `app/MainProject/QuanLyThuVienNhacOnline/web/selenium/` (hướng dẫn chi tiết: [`selenium/README.md`](app/MainProject/QuanLyThuVienNhacOnline/web/selenium/README.md)). Selenium WebDriver (Python) kết hợp pytest điều khiển trình duyệt Chrome thao tác trực tiếp trên **chính hệ thống quản lý thư viện nhạc**, mô phỏng người dùng thật: đăng ký, đăng nhập, đăng xuất, phân quyền, tìm kiếm và quản lý bài hát, nghệ sĩ, thể loại. Tổng cộng **72 ca**, bám **40 mã** trong bảng test case (20 mã `TC_AUT`, 20 mã `TC_PLY`); tên test và docstring đều ghi mã TC để truy vết.

| Nhóm | File | Số ca | Mã TC liên quan | Nội dung chính |
|---|---|---:|---|---|
| Xác thực và phân quyền | `test_auth.py` | 28 | `TC_AUT_01`–`24` (trừ 08, 09, 17, 20), `TC_PLY_11` | Đăng ký hợp lệ/không hợp lệ, trùng username/email, đăng nhập đúng/sai, SQL injection, chuyển hướng khi chưa đăng nhập, đăng xuất, nút Back, user thường vào `/users` |
| Trang chủ, điều hướng | `test_navigation.py` | 7 | bổ sung | Thẻ chức năng và thanh điều hướng theo quyền, bấm từng mục |
| Tìm kiếm và quản lý bài hát | `test_song.py` | 26 | `TC_PLY_01`–`10`, `12`, `15`–`22` | Tìm theo tên/nghệ sĩ/thể loại, hoa-thường, tiếng Việt, ký tự đặc biệt, thêm/sửa/xóa, liên kết file âm thanh |
| Nghệ sĩ, thể loại | `test_artist_genre.py` | 11 | bổ sung | Thêm, sửa, xóa, tìm theo tên, kiểm tra ô nhập bắt buộc |

**Kỹ thuật chính**

- **Page Object Model**: `pages/pages.py` mô tả từng trang, kịch bản chỉ gọi hàm nghiệp vụ, không chứa locator.
- **Explicit wait** (`WebDriverWait`), không dùng `sleep`; sau khi gửi form, test chờ trang cũ "stale" rồi mới kiểm tra.
- **Modal Bootstrap**: thêm/sửa/xóa đều trong modal, và các modal sửa ở trang Artist/Genre dùng chung `id`, nên test khoanh vùng trong modal đang mở và tìm ô theo `name`.
- **Kiểm tra hai lớp** cho dữ liệu sai: (1) trình duyệt chặn nhờ ràng buộc HTML5 (`required`, `minlength=8`, `type=url`); (2) bỏ qua HTML5 để xác nhận **server** vẫn từ chối.
- **Dữ liệu độc lập**: bản ghi test có chuỗi ngẫu nhiên và được tự xóa sau khi chạy; test fail tự chụp ảnh màn hình.

**Cách chạy**

```bash
cd app/MainProject/QuanLyThuVienNhacOnline/web
mvn spring-boot:run                      # cửa sổ 1: chạy ứng dụng (cổng 8080)

cd selenium                              # cửa sổ 2
pip install -r requirements.txt
pytest                                   # HEADLESS=1 để chạy ẩn trình duyệt
```

Hai test cần quyền Admin (`TC_AUT_24` và thẻ User Management ở trang chủ) chỉ chạy khi đặt biến môi trường `ADMIN_USER` và `ADMIN_PASSWORD`, vì tài khoản Admin không thể tạo qua form đăng ký (role bị ép về `User`). Bộ test ghi dữ liệu thật vào CSDL nên nên chạy trên CSDL kiểm thử; tài khoản `sel…` do test đăng ký chỉ quản trị viên xóa được.

**Ca ghi nhận lỗi đã biết và ca chưa thực hiện được**

| Loại | Test | Ý nghĩa |
|---|---|---|
| `xfail` | `test_d1_blank_title_should_not_show_500_page` | Lỗi **D1**: gửi tên bài hát trống (bỏ qua HTML5) phải ra lỗi 400 hoặc thông báo thân thiện, nhưng hệ thống hiện trang "Lỗi 500". Test mong đợi hành vi đúng nên được đánh dấu lỗi đã biết. |
| `xfail` | `test_tc_ply_15_javascript_audio_path_is_rejected` | Lỗi **D8**: đường dẫn `javascript:alert(1)` đáng lẽ bị từ chối nhưng vẫn được lưu. |
| `skip` | `test_tc_ply_17`, `test_tc_ply_18` | **Blocked (D7)**: chưa có trình phát nhúng (Play/Pause, chuyển bài). |

`XFAIL` nghĩa là lỗi đã biết **vẫn còn**; khi lỗi được sửa, test chuyển thành `XPASS` và nên bỏ dấu `xfail` để thành ca hồi quy bình thường.

### Kết quả chạy

> Điền sau khi chạy `pytest` trên ứng dụng thật. Dòng cuối của `pytest` có dạng `N passed, N failed, N skipped, N xfailed`.

| Nhóm | Số ca | Pass | Fail | XFail | Skipped |
|---|---:|---:|---:|---:|---:|
| Xác thực và phân quyền | 28 | | | | |
| Trang chủ, điều hướng | 7 | | | | |
| Bài hát | 26 | | | | |
| Nghệ sĩ, thể loại | 11 | | | | |
| **Tổng** | **72** | | | | |

**Giới hạn**

- Selenium không đọc được **mã trạng thái HTTP**, nên các ca 400/403/404/500 được nhận biết qua nội dung trang (ví dụ "Lỗi 500"); mã trạng thái chính xác đã được kiểm tra bằng Postman (mục 6).
- Một số ca không thể thao tác qua form nên thuộc phạm vi JUnit/Postman: ép role `Admin` khi đăng ký (`TC_AUT_08`), mật khẩu lưu BCrypt (`TC_AUT_09`), POST thiếu CSRF (`TC_AUT_20`), xóa người dùng bằng GET (`TC_AUT_26`, lỗi D2).
- Playlist, danh sách yêu thích và trình phát nhúng chưa có trong hệ thống (D7) nên chưa có kịch bản giao diện.

## 9. Lỗi và rủi ro phát hiện

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
