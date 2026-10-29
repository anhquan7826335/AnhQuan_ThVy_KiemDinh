import os
import urllib.request
from dataclasses import dataclass, field
from pathlib import Path

import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from pages.pages import ArtistsPage, GenresPage, HomePage, LoginPage, RegisterPage, SongsPage
from tests.settings import ADMIN_PASSWORD, ADMIN_USER, BASE_URL, PASSWORD, unique


# ---------------------------------------------------------------------
# Trình duyệt
# ---------------------------------------------------------------------
def make_driver():
    opts = Options()
    if os.getenv("HEADLESS", "0") == "1":
        opts.add_argument("--headless=new")
    opts.add_argument("--window-size=1366,900")
    opts.add_argument("--disable-notifications")
    # Tắt popup lưu/đổi mật khẩu của Chrome (làm che giao diện khi chạy có mở trình duyệt)
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
    drv.implicitly_wait(0)  # chỉ dùng explicit wait
    return drv


@pytest.fixture
def driver():
    """Mỗi test một trình duyệt mới -> không dính cookie/phiên của test trước."""
    drv = make_driver()
    yield drv
    drv.quit()


@pytest.fixture(scope="session", autouse=True)
def app_is_up():
    """Dừng sớm với thông báo rõ ràng nếu ứng dụng chưa chạy."""
    try:
        urllib.request.urlopen(f"{BASE_URL}/login", timeout=5)
    except Exception as exc:  # noqa: BLE001
        pytest.exit(f"Không kết nối được tới {BASE_URL}/login ({exc}). "
                    "Hãy chạy ứng dụng trước: mvn spring-boot:run", returncode=2)


# ---------------------------------------------------------------------
# Tài khoản
# ---------------------------------------------------------------------
@dataclass
class Account:
    username: str
    email: str
    password: str = PASSWORD


@pytest.fixture(scope="session")
def account(app_is_up):
    """Một tài khoản USER thường, đăng ký qua giao diện, dùng chung cho cả phiên chạy."""
    name = unique("sel")
    acc = Account(username=name, email=f"{name}@example.com")
    drv = make_driver()
    try:
        RegisterPage(drv, BASE_URL).open().register(acc.username, acc.email, acc.password)
        WebDriverWait(drv, 10).until(EC.url_contains("registered"))
    finally:
        drv.quit()
    return acc


@pytest.fixture
def login_page(driver):
    return LoginPage(driver, BASE_URL).open()


@pytest.fixture
def register_page(driver):
    return RegisterPage(driver, BASE_URL).open()


@pytest.fixture
def user_home(driver, account):
    """Đã đăng nhập bằng tài khoản USER thường; trả về trang chủ."""
    LoginPage(driver, BASE_URL).open().login(account.username, account.password)
    return HomePage(driver, BASE_URL).wait_until_logged_in()


@pytest.fixture
def admin_home(driver):
    if not (ADMIN_USER and ADMIN_PASSWORD):
        pytest.skip("Chưa đặt ADMIN_USER / ADMIN_PASSWORD")
    LoginPage(driver, BASE_URL).open().login(ADMIN_USER, ADMIN_PASSWORD)
    return HomePage(driver, BASE_URL).wait_until_logged_in()


# ---------------------------------------------------------------------
# Các trang CRUD (đã đăng nhập)
# ---------------------------------------------------------------------
@pytest.fixture
def songs_page(driver, user_home):
    return SongsPage(driver, BASE_URL).open()


@pytest.fixture
def artists_page(driver, user_home):
    return ArtistsPage(driver, BASE_URL).open()


@pytest.fixture
def genres_page(driver, user_home):
    return GenresPage(driver, BASE_URL).open()


# ---------------------------------------------------------------------
# Dữ liệu test: tạo qua giao diện, tự xóa sau khi test xong
# ---------------------------------------------------------------------
@dataclass
class Created:
    songs: list = field(default_factory=list)
    artists: list = field(default_factory=list)
    genres: list = field(default_factory=list)


@pytest.fixture
def created(driver):
    """Ghi lại tên bản ghi test đã tạo và xóa chúng ở bước dọn dẹp (không làm hỏng kết quả test)."""
    registry = Created()
    yield registry
    try:
        for page_cls, names in ((SongsPage, registry.songs),
                                (ArtistsPage, registry.artists),
                                (GenresPage, registry.genres)):
            if not names:
                continue
            page = page_cls(driver, BASE_URL).open()
            for name in names:
                if page.has_row(name):
                    page.delete(name)
    except Exception as exc:  # noqa: BLE001
        print(f"\n[cleanup] không xóa hết dữ liệu test: {exc}")


@dataclass
class SongData:
    title: str
    artist: str
    genre: str


@pytest.fixture
def new_song(songs_page, created):
    def _make(title=None, artist=None, genre=None, **kwargs):
        data = SongData(title or unique("Song "), artist or unique("Artist "), genre or unique("Genre "))
        created.songs.append(data.title)
        songs_page.open()
        songs_page.add_song(data.title, data.artist, data.genre, **kwargs)
        songs_page.wait_for_row(data.title)
        return data
    return _make


@pytest.fixture
def new_artist(artists_page, created):
    def _make(name=None, **kwargs):
        name = name or unique("Artist ")
        created.artists.append(name)
        artists_page.open()
        artists_page.add_artist(name, **kwargs)
        artists_page.wait_for_row(name)
        return name
    return _make


@pytest.fixture
def new_genre(genres_page, created):
    def _make(name=None, **kwargs):
        name = name or unique("Genre ")
        created.genres.append(name)
        genres_page.open()
        genres_page.add_genre(name, **kwargs)
        genres_page.wait_for_row(name)
        return name
    return _make


# ---------------------------------------------------------------------
# Chụp ảnh màn hình khi test fail -> thư mục screenshots/
# ---------------------------------------------------------------------
@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    outcome = yield
    rep = outcome.get_result()
    if rep.when == "call" and rep.failed:
        drv = item.funcargs.get("driver")
        if drv:
            Path("screenshots").mkdir(exist_ok=True)
            safe = item.name.replace("/", "_").replace("[", "_").replace("]", "")
            drv.save_screenshot(f"screenshots/{safe}.png")
