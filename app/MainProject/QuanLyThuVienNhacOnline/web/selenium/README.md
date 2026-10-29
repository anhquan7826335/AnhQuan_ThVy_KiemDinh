# Kiểm thử giao diện tự động bằng Selenium

Bộ kiểm thử giao diện (Selenium WebDriver + Python + pytest) chạy trực tiếp trên **ứng dụng quản lý thư viện nhạc** của đề tài, mô phỏng người dùng thật thao tác trên trình duyệt Chrome: đăng ký, đăng nhập, đăng xuất, phân quyền, tìm kiếm và quản lý bài hát, nghệ sĩ, thể loại. Phần này là một thành phần của đề tài (xem [README gốc](../../../../../README.md), mục 8).

## Kỹ thuật sử dụng

| Kỹ thuật | Áp dụng trong bộ test |
|---|---|
| **Page Object Model** | `pages/pages.py` mô tả từng trang (`LoginPage`, `RegisterPage`, `HomePage`, `SongsPage`, `ArtistsPage`, `GenresPage`, `UsersPage`); kịch bản trong `tests/` chỉ gọi hàm nghiệp vụ như `songs.add_song(...)`, không chứa locator. Ba trang CRUD dùng chung lớp `CrudPage`. |
| **Explicit wait** | `WebDriverWait` + `expected_conditions`, không dùng `sleep`. Sau khi gửi form, test chờ trang cũ "stale" rồi mới kiểm tra trang mới. |
| **Xử lý modal Bootstrap** | Thêm, sửa, xóa đều nằm trong modal. Các modal sửa của mọi dòng trong trang Artist/Genre dùng chung một `id`, nên test luôn khoanh vùng trong modal đang mở (`.modal.show`) và tìm ô theo thuộc tính `name`. |
| **Kiểm tra hai lớp** | Form có ràng buộc HTML5 (`required`, `minlength=8`, `type=url`). Với mỗi ca dữ liệu sai, một test xác nhận **trình duyệt chặn** (`checkValidity()`), một test khác **bỏ qua HTML5** (`form.noValidate`) để kiểm tra riêng phần **server** từ chối. |
| **Dữ liệu độc lập** | Mọi bản ghi tạo ra có chuỗi ngẫu nhiên (`unique()`), nên không đụng dữ liệu có sẵn; fixture `created` tự xóa bài hát, nghệ sĩ, thể loại sau mỗi test. |
| **Fixture pytest** | Mỗi test một trình duyệt mới (không dính phiên cũ); tài khoản USER được đăng ký một lần cho cả phiên chạy; tự dừng và báo rõ nếu ứng dụng chưa chạy. |
| **Chụp ảnh khi fail** | Test fail tự lưu ảnh màn hình vào `screenshots/`. |
| **Truy vết** | Tên test và docstring ghi mã `TC_AUT_xx`, `TC_PLY_xx` của [`TestCase_QuanLyThuVienNhac.xlsx`](../docs/TestCase_QuanLyThuVienNhac.xlsx). |

## Cấu trúc

```
selenium/
├── pages/pages.py              # Page Object: các trang của ứng dụng
├── tests/
│   ├── settings.py             # BASE_URL, tài khoản admin, hàm unique()
│   ├── conftest.py             # driver, tài khoản test, dữ liệu + dọn dẹp, chụp ảnh khi fail
│   ├── test_auth.py            # đăng ký, đăng nhập, đăng xuất, phân quyền   (28 ca)
│   ├── test_navigation.py      # trang chủ và thanh điều hướng theo quyền     (7 ca)
│   ├── test_song.py            # tìm kiếm và quản lý bài hát                  (26 ca)
│   └── test_artist_genre.py    # quản lý nghệ sĩ và thể loại                 (11 ca)
├── requirements.txt
└── pytest.ini
```

## Chuẩn bị và chạy

**Yêu cầu:** Python 3.9+, Google Chrome (Selenium 4.6+ tự tải ChromeDriver), máy có Internet (giao diện nạp Bootstrap từ CDN).

**1. Chạy ứng dụng** (cổng mặc định 8080), ở thư mục `web/`:

```bash
mvn spring-boot:run
```

> Bộ test **ghi dữ liệu thật** vào cơ sở dữ liệu của ứng dụng (tạo rồi xóa bài hát, nghệ sĩ, thể loại). Nên trỏ ứng dụng tới một CSDL dùng để kiểm thử, không dùng CSDL chứa dữ liệu quan trọng. Tài khoản `sel…` do test đăng ký **không tự xóa được** (người dùng thường không có chức năng xóa tài khoản); quản trị viên có thể xóa ở trang `/users`.

**2. Cài đặt và chạy test**, ở thư mục `selenium/`:

```bash
python -m venv .venv
.venv\Scripts\activate          # Linux/Mac: source .venv/bin/activate
pip install -r requirements.txt
pytest                          # có mở trình duyệt
```

| Tùy chọn | Windows (cmd) | Linux / Mac |
|---|---|---|
| Chạy ẩn trình duyệt | `set HEADLESS=1` | `HEADLESS=1 pytest` |
| Đổi địa chỉ ứng dụng | `set BASE_URL=http://host:8080` | `BASE_URL=http://host:8080 pytest` |
| Bật các test cần quyền Admin | `set ADMIN_USER=...` và `set ADMIN_PASSWORD=...` | `ADMIN_USER=... ADMIN_PASSWORD=... pytest` |
| Chạy một file / một test | `pytest tests/test_song.py` | `pytest tests/test_song.py::test_tc_ply_22_delete_song` |
| Chỉ chạy các ca theo mã (vd. nhóm đăng nhập) | `pytest -k tc_aut` | `pytest -k tc_aut` |

Tài khoản **Admin** không thể tạo qua form đăng ký (hệ thống ép role về `User`), nên 2 test cần quyền Admin chỉ chạy khi bạn truyền tài khoản Admin có sẵn trong CSDL; nếu không, chúng tự được bỏ qua (*skipped*).

## Các ca kiểm thử

Tổng **72 ca** (đã tính các ca tham số hóa), bám **40 mã** trong bảng test case (20 mã `TC_AUT`, 20 mã `TC_PLY`).

### Đăng ký, đăng nhập, đăng xuất, phân quyền (`test_auth.py`)

| Mã TC | Nội dung | Kết quả mong đợi |
|---|---|---|
| TC_AUT_01 | Đăng ký hợp lệ | Chuyển về `/login?registered`, hiện "Account created" |
| TC_AUT_02/03/04 | Bỏ trống username, bỏ trống email, mật khẩu 7 ký tự | (1) Trình duyệt chặn, ở lại `/register`; (2) bỏ qua HTML5 thì server trả `/register?error` |
| TC_AUT_05 | Mật khẩu đúng 8 ký tự (biên) | Đăng ký thành công |
| TC_AUT_06, 07 | Trùng username / email khác hoa-thường | `/register?error`, báo "already registered" |
| TC_AUT_10 | Username chứa `<script>` | Hiển thị như văn bản, không hiện hộp thoại alert |
| TC_AUT_11 | Đăng nhập đúng; vào `/songs` khi chưa đăng nhập rồi đăng nhập | Về trang chủ, hiện tên người dùng và nút Logout; quay lại đúng `/songs` |
| TC_AUT_12, 13 | Sai mật khẩu; username không tồn tại | `/login?error`, **cùng một thông báo** (không lộ tài khoản có tồn tại) |
| TC_AUT_14 | Username viết hoa | Vẫn đăng nhập được |
| TC_AUT_15 | Bỏ trống username/mật khẩu | Trình duyệt chặn; bỏ qua HTML5 thì `/login?error` |
| TC_AUT_16 | SQL injection `' OR '1'='1` | `/login?error`, không đăng nhập |
| TC_AUT_18, TC_PLY_11 | Vào `/songs`, `/artists`, `/genres`, `/songs/search`, `/users` khi chưa đăng nhập | Chuyển về `/login` |
| TC_AUT_19 | Trang chủ khi chưa đăng nhập | Truy cập được; hiện Login/Register, không có Logout |
| TC_AUT_21 | Đăng xuất | `/login?logout`, hiện thông báo; vào `/songs` bị chặn |
| TC_AUT_22 | Back sau khi đăng xuất rồi tải lại | Bị chuyển về `/login` |
| TC_AUT_23 | User thường vào `/users` | Trang lỗi 403; thanh điều hướng không có mục User |
| TC_AUT_24 | Admin vào `/users` *(cần tài khoản Admin)* | Hiện trang User Management |

### Tìm kiếm và quản lý bài hát (`test_song.py`)

| Mã TC | Nội dung | Kết quả mong đợi |
|---|---|---|
| TC_PLY_01, 02, 03 | Tìm theo tên, nghệ sĩ, thể loại | Chỉ hiện đúng bài khớp, số đếm đúng |
| TC_PLY_04 | Hoa/thường | Kết quả như nhau |
| TC_PLY_05 | Từ khóa rỗng | Hiện toàn bộ bài hát |
| TC_PLY_06 | Từ khóa không có kết quả | Danh sách rỗng, "Number of Songs: 0" |
| TC_PLY_07 | Ký tự đặc biệt `%` `_` `'` `"` | Không rơi vào trang lỗi |
| TC_PLY_08 | Tiếng Việt có dấu ("Mưa") | Tìm đúng bài |
| TC_PLY_09 | Từ khóa dài 1000 ký tự | Không lỗi, kết quả rỗng |
| TC_PLY_10 | Sau khi tìm | Ô tìm kiếm giữ nguyên từ khóa |
| TC_PLY_19 | Thêm bài hợp lệ | Xuất hiện trong danh sách, số lượng +1, các cột đúng |
| TC_PLY_20 | Thiếu tên / nghệ sĩ / thể loại | Trình duyệt chặn, modal vẫn mở |
| TC_PLY_16 | Để trống Audio File Path | Trình duyệt chặn gửi |
| TC_PLY_21 | Sửa tên bài hát | Danh sách hiện tên mới, tên cũ biến mất |
| TC_PLY_22 | Xóa bài hát (xác nhận) | Biến khỏi danh sách, số lượng -1 |
| (bổ sung) | Bấm Cancel ở hộp xác nhận xóa | Bài hát vẫn còn |
| TC_PLY_12 | Liên kết Audio File Path | `target="_blank"`, `href` đúng đường dẫn đã nhập |

### Trang chủ, nghệ sĩ, thể loại (`test_navigation.py`, `test_artist_genre.py`)

Các ca này **bổ sung** cho bảng test case (CRUD Artist/Genre trước đây mới có ca ở tầng Service/Controller, chưa có ca giao diện).

| Nội dung | Kết quả mong đợi |
|---|---|
| Trang chủ, thanh điều hướng của User thường | Có 3 thẻ Artist/Song/Genre; menu Home, Artist, Song, Genre; không có User |
| Bấm từng mục trên thanh điều hướng | Mở đúng trang |
| Admin thấy mục User *(cần tài khoản Admin)* | Có mục User và thẻ User Management |
| Nghệ sĩ: thêm, sửa, xóa, tìm theo tên | Danh sách và số đếm cập nhật đúng |
| Nghệ sĩ: bỏ trống tên, Informations không phải URL | Trình duyệt chặn |
| Thể loại: thêm, sửa, xóa, tìm theo tên, bỏ trống tên | Như trên |

### Ca ghi nhận lỗi đã biết và ca chưa thực hiện được

| Loại | Test | Ý nghĩa |
|---|---|---|
| `xfail` | `test_d1_blank_title_should_not_show_500_page` | **Lỗi D1** (mục 9 README gốc): gửi tên bài hát trống (bỏ qua HTML5) phải ra lỗi 400/thông báo thân thiện, nhưng hệ thống hiện ra trang "Lỗi 500". Test mong đợi hành vi đúng nên **đang được đánh dấu lỗi đã biết**. |
| `xfail` | `test_tc_ply_15_javascript_audio_path_is_rejected` | **Lỗi D8**: đường dẫn `javascript:alert(1)` đáng lẽ bị từ chối nhưng vẫn được lưu. |
| `skip` | `test_tc_ply_17`, `test_tc_ply_18` | **Blocked (D7)**: hệ thống chưa có trình phát nhúng (Play/Pause, chuyển bài), nên chỉ giữ chỗ. |

Cách đọc kết quả: `XFAIL` nghĩa là lỗi đã biết **vẫn còn** (đúng kỳ vọng). Nếu lỗi được sửa, test chuyển thành `XPASS`; khi đó nên bỏ dấu `xfail` để test trở thành ca hồi quy bình thường.

## Kết quả chạy

> Điền sau khi chạy `pytest` trên máy của bạn với ứng dụng thật. Dòng cuối của `pytest` có dạng `N passed, N failed, N skipped, N xfailed`.

| Nhóm | Số ca | Pass | Fail | XFail | Skipped |
|---|---:|---:|---:|---:|---:|
| Đăng ký, đăng nhập, phân quyền (`test_auth.py`) | 28 | | | | |
| Trang chủ, điều hướng (`test_navigation.py`) | 7 | | | | |
| Bài hát (`test_song.py`) | 26 | | | | |
| Nghệ sĩ, thể loại (`test_artist_genre.py`) | 11 | | | | |
| **Tổng** | **72** | | | | |

Nếu có test fail, ảnh màn hình nằm ở `screenshots/`. Dán ảnh kết quả `pytest` vào `web/docs/images/` và chèn tại đây.

## Giới hạn

- Selenium không đọc được **mã trạng thái HTTP**, nên các ca "400/403/404/500" được xác định qua nội dung trang (vd. "Lỗi 500", "403"). Kiểm tra mã trạng thái chính xác đã có ở Postman (mục 6 README gốc).
- Không kiểm thử qua giao diện các ca: ép role `Admin` khi đăng ký (`TC_AUT_08`), mật khẩu lưu BCrypt (`TC_AUT_09`), POST thiếu CSRF (`TC_AUT_20`), xóa người dùng bằng GET (`TC_AUT_26`, lỗi D2), vì form không cho thao tác này; các ca đó thuộc phạm vi JUnit/Postman.
- Playlist, danh sách yêu thích và trình phát nhúng chưa có trong hệ thống (D7) nên chưa có kịch bản giao diện.

## Tài liệu tham khảo

- Selenium Documentation: https://www.selenium.dev/documentation/
- Selenium Python API: https://selenium-python.readthedocs.io/
- pytest: https://docs.pytest.org/
- Bootstrap 5 Modal: https://getbootstrap.com/docs/5.3/components/modal/
- MDN – Constraint validation (`checkValidity`): https://developer.mozilla.org/en-US/docs/Web/HTML/Constraint_validation
