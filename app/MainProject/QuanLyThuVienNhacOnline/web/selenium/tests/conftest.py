import os
from pathlib import Path

import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service

from pages.pages import CartPage, InventoryPage, LoginPage

BASE_URL = os.getenv("BASE_URL", "https://www.saucedemo.com")
VALID_USER = "standard_user"
LOCKED_USER = "locked_out_user"
PASSWORD = "secret_sauce"


@pytest.fixture
def driver():
    opts = Options()
    if os.getenv("HEADLESS", "0") == "1":
        opts.add_argument("--headless=new")
    opts.add_argument("--window-size=1366,900")
    opts.add_argument("--disable-notifications")
    # Tắt popup "đổi mật khẩu" của Chrome trên saucedemo
    opts.add_experimental_option("prefs", {
        "credentials_enable_service": False,
        "profile.password_manager_enabled": False,
        "profile.password_manager_leak_detection": False,
    })
    if os.getenv("CHROME_BIN"):
        opts.binary_location = os.environ["CHROME_BIN"]
    if os.getenv("CI") or os.getenv("NO_SANDBOX") == "1":
        opts.add_argument("--no-sandbox")
        opts.add_argument("--disable-dev-shm-usage")
    service = Service(os.environ["CHROMEDRIVER"]) if os.getenv("CHROMEDRIVER") else Service()
    drv = webdriver.Chrome(service=service, options=opts)
    drv.implicitly_wait(0)
    yield drv
    drv.quit()


@pytest.fixture
def login_page(driver):
    return LoginPage(driver, BASE_URL).open()


@pytest.fixture
def inventory(driver, login_page):
    """Đăng nhập sẵn bằng tài khoản hợp lệ và trả về trang sản phẩm."""
    login_page.login(VALID_USER, PASSWORD)
    page = InventoryPage(driver, BASE_URL)
    assert page.title() == "Products"
    return page


@pytest.fixture
def cart_page(driver):
    return CartPage(driver, BASE_URL)


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    """Chụp ảnh màn hình khi test fail -> thư mục screenshots/."""
    outcome = yield
    rep = outcome.get_result()
    if rep.when == "call" and rep.failed:
        drv = item.funcargs.get("driver")
        if drv:
            Path("screenshots").mkdir(exist_ok=True)
            drv.save_screenshot(f"screenshots/{item.name}.png")
