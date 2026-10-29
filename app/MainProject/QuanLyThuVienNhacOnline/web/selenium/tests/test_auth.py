"""Đăng ký, đăng nhập, đăng xuất, phân quyền - kiểm thử qua giao diện.

Mã TC_AUT_xx lấy từ sheet "Đăng ký - Đăng nhập" của docs/TestCase_QuanLyThuVienNhac.xlsx.
Form đăng ký/đăng nhập có kiểm tra HTML5 (required, minlength=8), nên với các ca "để trống / mật khẩu
ngắn" test chia hai lớp: (1) trình duyệt chặn ngay; (2) bỏ qua HTML5 để kiểm tra riêng server.
"""
import pytest
from selenium.common.exceptions import NoAlertPresentException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC

from pages.pages import HomePage, LoginPage, SongsPage, UsersPage
from tests.settings import BASE_URL, PASSWORD, unique

LOGIN_ERROR = "Invalid username or password."


def _creds():
    name = unique("sel")
    return name, f"{name}@example.com"


# Mỗi phần tử: (username, email, password, ô bị coi là không hợp lệ)
INVALID_REGISTRATIONS = [
    pytest.param("", "{n}@example.com", PASSWORD, "username", id="TC_AUT_02-bo-trong-username"),
    pytest.param("{n}", "", PASSWORD, "email", id="TC_AUT_03-bo-trong-email"),
    pytest.param("{n}", "{n}@example.com", "1234567", "password", id="TC_AUT_04-mat-khau-7-ky-tu"),
]


# ======================= ĐĂNG KÝ =======================
def test_tc_aut_01_register_success(driver, register_page):
    """TC_AUT_01 - Đăng ký hợp lệ -> chuyển về /login?registered và hiện thông báo thành công."""
    name, email = _creds()
    register_page.register(name, email, PASSWORD)
    register_page.wait.until(EC.url_contains("/login"))
    assert "registered" in register_page.current_query()
    assert "Account created" in LoginPage(driver, BASE_URL).success_text()


@pytest.mark.parametrize("username,email,password,bad_field", INVALID_REGISTRATIONS)
def test_tc_aut_02_03_04_browser_blocks_invalid_register(register_page, username, email, password, bad_field):
    """TC_AUT_02/03/04 (lớp 1) - Trình duyệt chặn gửi form, ở lại trang /register, không gọi server."""
    n = unique("sel")
    register_page.register(username.format(n=n), email.format(n=n), password)
    assert register_page.is_invalid(register_page.field(bad_field))
    assert register_page.current_path() == "/register"
    assert "error" not in register_page.current_query()


@pytest.mark.parametrize("username,email,password,bad_field", INVALID_REGISTRATIONS)
def test_tc_aut_02_03_04_server_rejects_invalid_register(register_page, username, email, password, bad_field):
    """TC_AUT_02/03/04 (lớp 2) - Bỏ qua HTML5, server vẫn từ chối: /register?error + thông báo."""
    n = unique("sel")
    register_page.register_bypassing_html5(username.format(n=n), email.format(n=n), password)
    register_page.wait.until(EC.url_contains("/register?error"))
    assert "Invalid details" in register_page.error_text()


def test_tc_aut_05_register_password_exactly_8_chars(register_page):
    """TC_AUT_05 - Mật khẩu đúng 8 ký tự (biên) -> đăng ký thành công."""
    name, email = _creds()
    register_page.register(name, email, "Abcd1234")
    register_page.wait.until(EC.url_contains("/login?registered"))


def test_tc_aut_06_register_duplicate_username_case_insensitive(register_page):
    """TC_AUT_06 - Trùng username khác hoa/thường -> bị từ chối."""
    name, email = _creds()
    register_page.register(name, email, PASSWORD)
    register_page.wait.until(EC.url_contains("/login?registered"))

    register_page.open()
    register_page.register(name.upper(), f"other_{email}", PASSWORD)
    register_page.wait.until(EC.url_contains("/register?error"))
    assert "already registered" in register_page.error_text()


def test_tc_aut_07_register_duplicate_email_case_insensitive(register_page):
    """TC_AUT_07 - Trùng email khác hoa/thường -> bị từ chối."""
    name, email = _creds()
    register_page.register(name, email, PASSWORD)
    register_page.wait.until(EC.url_contains("/login?registered"))

    register_page.open()
    register_page.register(unique("sel"), email.upper(), PASSWORD)
    register_page.wait.until(EC.url_contains("/register?error"))


def test_tc_aut_10_script_in_username_is_escaped(driver, register_page):
    """TC_AUT_10 - Username chứa <script> được hiển thị như văn bản, không thực thi."""
    name = f"<script>alert(1)</script>{unique('sel')}"
    register_page.register(name, f"{unique('sel')}@example.com", PASSWORD)
    register_page.wait.until(EC.url_contains("/login?registered"))

    LoginPage(driver, BASE_URL).login(name, PASSWORD)
    home = HomePage(driver, BASE_URL).wait_until_logged_in()
    assert home.nav_username() == name
    assert not driver.find_elements(By.CSS_SELECTOR, "nav script")
    with pytest.raises(NoAlertPresentException):
        driver.switch_to.alert


# ======================= ĐĂNG NHẬP =======================
def test_tc_aut_11_login_success(driver, login_page, account):
    """TC_AUT_11 - Đăng nhập đúng -> về trang chủ, thanh điều hướng hiện tên người dùng và nút Logout."""
    login_page.login(account.username, account.password)
    home = HomePage(driver, BASE_URL).wait_until_logged_in()
    assert home.current_path() == "/"
    assert home.nav_username() == account.username
    assert not home.exists(home.NAV_LOGIN) and not home.exists(home.NAV_REGISTER)


def test_tc_aut_11_login_redirects_back_to_requested_page(driver, account):
    """TC_AUT_11 - Vào /songs khi chưa đăng nhập -> sang /login; đăng nhập xong quay lại /songs."""
    driver.get(f"{BASE_URL}/songs")
    login = LoginPage(driver, BASE_URL)
    login.wait_for_path("/login")
    login.login(account.username, account.password)
    login.wait_for_path("/songs")
    assert driver.title == SongsPage.TITLE


def test_tc_aut_12_login_wrong_password(login_page, account):
    """TC_AUT_12 - Sai mật khẩu -> /login?error, hiện cảnh báo, không đăng nhập."""
    login_page.login(account.username, "sai-mat-khau-123")
    login_page.wait.until(EC.url_contains("/login?error"))
    assert login_page.error_text() == LOGIN_ERROR
    assert not login_page.is_logged_in()


def test_tc_aut_13_login_unknown_user_same_message(login_page, account):
    """TC_AUT_13 - Username không tồn tại -> cùng thông báo như sai mật khẩu (không lộ tài khoản có tồn tại)."""
    login_page.login(unique("khongtontai"), "bat-ky-mat-khau")
    login_page.wait.until(EC.url_contains("/login?error"))
    unknown_msg = login_page.error_text()

    login_page.open()
    login_page.login(account.username, "sai-mat-khau-123")
    login_page.wait.until(EC.url_contains("/login?error"))
    assert unknown_msg == login_page.error_text() == LOGIN_ERROR


def test_tc_aut_14_login_username_case_insensitive(driver, login_page, account):
    """TC_AUT_14 - Username viết hoa vẫn đăng nhập được."""
    login_page.login(account.username.upper(), account.password)
    home = HomePage(driver, BASE_URL).wait_until_logged_in()
    assert home.nav_username().lower() == account.username.lower()


def test_tc_aut_15_login_blank_fields(login_page):
    """TC_AUT_15 - Để trống: trình duyệt chặn; bỏ qua HTML5 thì server trả /login?error."""
    login_page.login("", "")
    assert login_page.is_invalid(login_page.find(login_page.USERNAME))
    assert login_page.current_path() == "/login" and not login_page.has_error()

    login_page.login_bypassing_html5("", "")
    login_page.wait.until(EC.url_contains("/login?error"))
    assert login_page.error_text() == LOGIN_ERROR


def test_tc_aut_16_login_sql_injection(login_page):
    """TC_AUT_16 - Chuỗi SQL injection ở username không đăng nhập được."""
    login_page.login("' OR '1'='1", "' OR '1'='1")
    login_page.wait.until(EC.url_contains("/login?error"))
    assert login_page.error_text() == LOGIN_ERROR
    assert not login_page.is_logged_in()


@pytest.mark.parametrize("path", ["/songs", "/artists", "/genres", "/songs/search?keyword=love", "/users"])
def test_tc_aut_18_protected_pages_redirect_to_login(driver, path):
    """TC_AUT_18 (+ TC_PLY_11) - Chưa đăng nhập mà vào trang cần quyền -> chuyển về /login."""
    driver.get(BASE_URL + path)
    page = LoginPage(driver, BASE_URL)
    page.wait_for_path("/login")
    assert driver.title == "Login"


def test_tc_aut_19_home_is_public(driver):
    """TC_AUT_19 - Trang chủ truy cập được khi chưa đăng nhập; hiện nút Login/Register, không có Logout."""
    home = HomePage(driver, BASE_URL).open()
    assert home.current_path() == "/" and driver.title == "Home"
    assert home.exists(home.NAV_LOGIN) and home.exists(home.NAV_REGISTER)
    assert not home.is_logged_in()


# ======================= ĐĂNG XUẤT =======================
def test_tc_aut_21_logout(driver, user_home):
    """TC_AUT_21 - Đăng xuất -> /login?logout, hiện thông báo, phiên bị hủy (vào /songs bị chặn)."""
    user_home.logout()
    login = LoginPage(driver, BASE_URL)
    assert "logout" in login.current_query()
    assert "logged out" in login.success_text()

    driver.get(f"{BASE_URL}/songs")
    login.wait_for_path("/login")


def test_tc_aut_22_back_button_after_logout(driver, user_home):
    """TC_AUT_22 - Sau khi đăng xuất, bấm Back rồi tải lại /songs -> bị chuyển về /login."""
    songs = SongsPage(driver, BASE_URL).open()
    songs.logout()
    driver.back()
    driver.refresh()
    songs.wait.until(EC.url_contains("/login"))
    assert driver.title == "Login"


# ======================= PHÂN QUYỀN =======================
def test_tc_aut_23_normal_user_cannot_open_users_page(driver, user_home):
    """TC_AUT_23 - Role User vào /users -> 403, và thanh điều hướng không có mục User."""
    assert "User" not in user_home.nav_link_texts()
    UsersPage(driver, BASE_URL).open()
    text = driver.find_element(By.TAG_NAME, "body").text
    assert driver.title != "User Management"
    assert "403" in text or "forbidden" in text.lower()


@pytest.mark.admin
def test_tc_aut_24_admin_can_open_users_page(driver, admin_home):
    """TC_AUT_24 - Role Admin vào /users -> hiển thị trang quản lý người dùng."""
    assert "User" in admin_home.nav_link_texts()
    UsersPage(driver, BASE_URL).open()
    assert driver.title == "User Management"
