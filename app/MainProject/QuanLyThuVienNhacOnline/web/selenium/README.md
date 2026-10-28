# Kiểm thử giao diện tự động bằng Selenium

Bài thực hành Selenium WebDriver (Python + pytest) trên website demo [Sauce Demo](https://www.saucedemo.com): 10 test case gồm đăng nhập, giỏ hàng, sắp xếp, đăng xuất. Phần này là một thành phần của đề tài kiểm thử hệ thống quản lý thư viện nhạc (xem [README gốc](../README.md)).

## Kiến thức đã thực hành

- **Selenium WebDriver**: điều khiển trình duyệt thật (click, nhập liệu, đọc nội dung).
- **Locator**: `By.ID`, `By.CSS_SELECTOR`.
- **Explicit wait** (`WebDriverWait` + `expected_conditions`) thay cho `sleep`, giúp test ổn định.
- **Page Object Model**: tách mô tả trang (`pages/pages.py`) khỏi kịch bản (`tests/`).
- **Fixture pytest**: mở/đóng trình duyệt, đăng nhập sẵn; **chụp ảnh khi test fail** vào `screenshots/`.

## Cấu trúc

```
selenium/
├── pages/pages.py          # LoginPage, InventoryPage, CartPage
├── tests/conftest.py       # fixture driver, đăng nhập sẵn, chụp ảnh khi fail
├── tests/test_saucedemo.py # 10 test case
├── requirements.txt
└── pytest.ini
```

## Cài đặt và chạy

Yêu cầu: Python 3.9+, Google Chrome (Selenium 4.6+ tự tải ChromeDriver).

```bash
cd selenium
python -m venv .venv
.venv\Scripts\activate        # Linux/Mac: source .venv/bin/activate
pip install -r requirements.txt
pytest                        # có mở trình duyệt
```

| Tùy chọn | Ý nghĩa |
|---|---|
| `set HEADLESS=1` (Windows) / `HEADLESS=1 pytest` | Chạy ẩn trình duyệt |
| `pytest tests/test_saucedemo.py::test_tc05_add_product_to_cart` | Chạy một test |
| `set BASE_URL=http://...` | Đổi website cần kiểm thử |

## Test case

Tài khoản demo do website cung cấp: `standard_user`, `locked_out_user` / mật khẩu `secret_sauce`.

| Mã | Chức năng | Kết quả mong đợi |
|---|---|---|
| TC01 | Đăng nhập đúng | Vào trang Products, có 6 sản phẩm |
| TC02 | Sai mật khẩu | Báo "Username and password do not match..." |
| TC03 | Tài khoản bị khóa | Báo "this user has been locked out" |
| TC04 | Bỏ trống username | Báo "Username is required" |
| TC05 | Thêm sản phẩm vào giỏ | Biểu tượng giỏ hiện 1 rồi 2 |
| TC06 | Xem giỏ hàng | Giỏ có đúng "Sauce Labs Backpack" |
| TC07 | Xóa sản phẩm khỏi giỏ | Số lượng trên giỏ về 0 |
| TC08 | Sắp xếp giá thấp → cao | Giá tăng dần |
| TC09 | Sắp xếp tên Z → A | Tên giảm dần |
| TC10 | Đăng xuất | Quay về trang đăng nhập |

## Kết quả chạy

> Điền sau khi chạy `pytest` trên máy của bạn.

| Mã | Pass/Fail | Ghi chú |
|---|---|---|
| TC01–TC10 | | |

Tổng: ___ / 10 đạt. _(Dán ảnh kết quả `pytest` vào `docs/images/` và chèn tại đây.)_

## Tài liệu tham khảo

- Selenium Documentation: https://www.selenium.dev/documentation/
- Selenium Python API: https://selenium-python.readthedocs.io/
- pytest: https://docs.pytest.org/
- _(Thêm link video hướng dẫn Selenium mà bạn đã học)_
