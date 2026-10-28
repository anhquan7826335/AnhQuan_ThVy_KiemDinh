"""
Bộ test tự động bằng Selenium cho https://www.saucedemo.com
(website demo thương mại điện tử dùng để luyện kiểm thử).
"""
from pages.pages import InventoryPage
from tests.conftest import BASE_URL, LOCKED_USER, PASSWORD, VALID_USER

BACKPACK = "sauce-labs-backpack"
BIKE_LIGHT = "sauce-labs-bike-light"


def test_tc01_login_success(driver, login_page):
    """TC01 - Đăng nhập thành công với tài khoản hợp lệ."""
    login_page.login(VALID_USER, PASSWORD)
    assert "inventory" in driver.current_url
    page = InventoryPage(driver, BASE_URL)
    assert page.title() == "Products"
    assert page.item_count() == 6


def test_tc02_login_wrong_password(login_page):
    """TC02 - Đăng nhập thất bại khi sai mật khẩu -> hiện thông báo lỗi."""
    login_page.login(VALID_USER, "sai_mat_khau")
    assert "Username and password do not match" in login_page.error_text()


def test_tc03_login_locked_user(login_page):
    """TC03 - Tài khoản bị khóa không đăng nhập được."""
    login_page.login(LOCKED_USER, PASSWORD)
    assert "this user has been locked out" in login_page.error_text()


def test_tc04_login_empty_username(login_page):
    """TC04 - Để trống tên đăng nhập -> báo 'Username is required'."""
    login_page.login("", PASSWORD)
    assert "Username is required" in login_page.error_text()


def test_tc05_add_product_to_cart(inventory):
    """TC05 - Thêm sản phẩm vào giỏ: biểu tượng giỏ hàng tăng số lượng."""
    assert inventory.cart_count() == 0
    inventory.add_to_cart(BACKPACK)
    assert inventory.cart_count() == 1
    inventory.add_to_cart(BIKE_LIGHT)
    assert inventory.cart_count() == 2


def test_tc06_cart_shows_added_product(inventory, cart_page):
    """TC06 - Mở giỏ hàng thấy đúng sản phẩm đã thêm."""
    inventory.add_to_cart(BACKPACK)
    inventory.open_cart()
    assert cart_page.item_count() == 1
    assert cart_page.names() == ["Sauce Labs Backpack"]


def test_tc07_remove_product_from_cart(inventory):
    """TC07 - Bỏ sản phẩm khỏi giỏ: số lượng trên biểu tượng giỏ trở về 0."""
    inventory.add_to_cart(BACKPACK)
    assert inventory.cart_count() == 1
    inventory.remove_from_cart(BACKPACK)
    assert inventory.cart_count() == 0


def test_tc08_sort_price_low_to_high(inventory):
    """TC08 - Sắp xếp giá từ thấp đến cao cho kết quả tăng dần."""
    inventory.sort_by("lohi")
    prices = inventory.prices()
    assert prices == sorted(prices)


def test_tc09_sort_name_z_to_a(inventory):
    """TC09 - Sắp xếp tên Z đến A cho kết quả giảm dần."""
    inventory.sort_by("za")
    names = inventory.names()
    assert names == sorted(names, reverse=True)


def test_tc10_logout(driver, inventory, login_page):
    """TC10 - Đăng xuất quay về trang đăng nhập."""
    inventory.logout()
    assert login_page.is_displayed()
    assert "inventory" not in driver.current_url
