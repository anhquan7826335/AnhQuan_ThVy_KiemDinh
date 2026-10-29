"""Page Object Model cho ứng dụng Music Library (Spring Boot + Thymeleaf + Bootstrap 5).

Ghi chú về giao diện (rút ra từ các file trong src/main/resources/templates):
- Thêm/sửa/xóa đều nằm trong modal Bootstrap. Trong trang Artist/Genre, các modal sửa của mọi dòng
  dùng chung một `id` (vd. `newArtistName`), nên KHÔNG định vị theo id mà luôn khoanh vùng trong
  modal đang mở (`.modal.show`) rồi tìm ô theo thuộc tính `name`.
- Form đăng nhập/đăng ký dùng kiểm tra HTML5 (`required`, `minlength=8`) nên trình duyệt có thể
  chặn gửi form trước khi tới server. Hàm `submit_without_html5()` bỏ qua lớp này để kiểm thử
  riêng phần kiểm tra phía server.
"""
import re
from urllib.parse import parse_qs, urlparse

from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select, WebDriverWait


def xpath_literal(text: str) -> str:
    """Biến chuỗi bất kỳ thành literal XPath hợp lệ (xử lý cả dấu ' và ")."""
    if "'" not in text:
        return f"'{text}'"
    if '"' not in text:
        return f'"{text}"'
    return "concat(" + ", \"'\", ".join(f"'{part}'" for part in text.split("'")) + ")"


class BasePage:
    path = "/"

    # Thanh điều hướng dùng chung ở home/songs/artists/genres/users
    NAV_LINKS = (By.CSS_SELECTOR, "nav .navbar-nav a.nav-link")
    NAV_LOGOUT = (By.CSS_SELECTOR, "nav form[action$='/logout'] button")
    NAV_USERNAME = (By.CSS_SELECTOR, "nav span.text-muted")
    NAV_LOGIN = (By.CSS_SELECTOR, "nav a[href$='/login']")
    NAV_REGISTER = (By.CSS_SELECTOR, "nav a[href$='/register']")

    def __init__(self, driver, base_url, timeout=10):
        self.driver = driver
        self.base_url = base_url.rstrip("/")
        self.wait = WebDriverWait(driver, timeout)

    # ---------- điều hướng / URL ----------
    def open(self, path=None):
        self.driver.get(self.base_url + (path or self.path))
        return self

    def current_path(self):
        return urlparse(self.driver.current_url).path

    def current_query(self):
        return parse_qs(urlparse(self.driver.current_url).query, keep_blank_values=True)

    def wait_for_path(self, path):
        self.wait.until(lambda d: urlparse(d.current_url).path == path)

    def page_text(self):
        return self.driver.find_element(By.TAG_NAME, "body").text

    # ---------- thao tác phần tử ----------
    def find(self, locator):
        return self.wait.until(EC.visibility_of_element_located(locator))

    def exists(self, locator):
        return bool(self.driver.find_elements(*locator))

    def click_el(self, element):
        # Cuộn phần tử ra giữa màn hình để navbar `fixed-top` không che mất
        self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", element)
        self.wait.until(EC.element_to_be_clickable(element))
        element.click()

    def click(self, locator):
        self.click_el(self.wait.until(EC.presence_of_element_located(locator)))

    def type_into(self, element, text):
        element.clear()
        if text:
            element.send_keys(text)

    def is_invalid(self, element):
        """True nếu trình duyệt đánh dấu ô nhập không hợp lệ (required/minlength/type=email/url...)."""
        return not self.driver.execute_script("return arguments[0].checkValidity();", element)

    def submit_without_html5(self, form):
        """Gửi form nhưng bỏ qua kiểm tra HTML5 của trình duyệt để chạm tới kiểm tra phía server."""
        self.driver.execute_script("arguments[0].noValidate = true; arguments[0].submit();", form)

    # ---------- thanh điều hướng ----------
    def nav_link_texts(self):
        return [a.text.strip() for a in self.driver.find_elements(*self.NAV_LINKS)]

    def click_nav(self, text):
        for link in self.driver.find_elements(*self.NAV_LINKS):
            if link.text.strip() == text:
                self.click_el(link)
                return
        raise AssertionError(f"Không thấy liên kết '{text}' trên thanh điều hướng")

    def is_logged_in(self):
        return self.exists(self.NAV_LOGOUT)

    def wait_until_logged_in(self):
        self.find(self.NAV_LOGOUT)
        return self

    def nav_username(self):
        return self.find(self.NAV_USERNAME).text.strip()

    def logout(self):
        self.click(self.NAV_LOGOUT)
        self.wait.until(EC.url_contains("/login"))


# =====================================================================
# Đăng nhập / đăng ký
# =====================================================================
class LoginPage(BasePage):
    path = "/login"
    USERNAME = (By.ID, "username")
    PASSWORD = (By.ID, "password")
    SUBMIT = (By.CSS_SELECTOR, "form button[type='submit']")
    FORM = (By.CSS_SELECTOR, "main form")
    ERROR = (By.CSS_SELECTOR, ".alert-danger")
    SUCCESS = (By.CSS_SELECTOR, ".alert-success")

    def fill(self, username, password):
        self.type_into(self.find(self.USERNAME), username)
        self.type_into(self.find(self.PASSWORD), password)
        return self

    def login(self, username, password):
        self.fill(username, password)
        self.click(self.SUBMIT)

    def login_bypassing_html5(self, username, password):
        self.fill(username, password)
        self.submit_without_html5(self.find(self.FORM))

    def error_text(self):
        return self.find(self.ERROR).text.strip()

    def success_text(self):
        return self.find(self.SUCCESS).text.strip()

    def has_error(self):
        return self.exists(self.ERROR)


class RegisterPage(BasePage):
    path = "/register"
    USERNAME = (By.ID, "username")
    EMAIL = (By.ID, "email")
    PASSWORD = (By.ID, "password")
    SUBMIT = (By.CSS_SELECTOR, "form button[type='submit']")
    FORM = (By.CSS_SELECTOR, "main form")
    ERROR = (By.CSS_SELECTOR, ".alert-danger")

    def fill(self, username, email, password):
        self.type_into(self.find(self.USERNAME), username)
        self.type_into(self.find(self.EMAIL), email)
        self.type_into(self.find(self.PASSWORD), password)
        return self

    def register(self, username, email, password):
        self.fill(username, email, password)
        self.click(self.SUBMIT)

    def register_bypassing_html5(self, username, email, password):
        self.fill(username, email, password)
        self.submit_without_html5(self.find(self.FORM))

    def field(self, name):
        return self.find({"username": self.USERNAME, "email": self.EMAIL, "password": self.PASSWORD}[name])

    def error_text(self):
        return self.find(self.ERROR).text.strip()


# =====================================================================
# Trang chủ
# =====================================================================
class HomePage(BasePage):
    path = "/"
    CARD_TITLES = (By.CSS_SELECTOR, ".card-title")

    def card_titles(self):
        self.find(self.CARD_TITLES)
        return [e.text.strip() for e in self.driver.find_elements(*self.CARD_TITLES)]


# =====================================================================
# Trang CRUD dùng chung: Song / Artist / Genre
# =====================================================================
class CrudPage(BasePage):
    ADD_MODAL_ID = ""          # vd. "addSongModal"
    HEADING = ""               # vd. "Song"
    TITLE = ""                 # <title> của trang

    ROWS = (By.CSS_SELECTOR, "table tbody tr")
    COUNT = (By.CSS_SELECTOR, "p.fw-semibold")
    SEARCH_INPUT = (By.CSS_SELECTOR, "form.search-bar input")
    OPEN_MODAL = (By.CSS_SELECTOR, ".modal.show .modal-content")

    # ---------- danh sách ----------
    def _row_locator(self, value):
        # Cột thứ 2 là tên/tiêu đề (cột 1 là ID)
        return (By.XPATH, f"//table//tbody//tr[td[2][normalize-space()={xpath_literal(value)}]]")

    def has_row(self, value):
        return self.exists(self._row_locator(value))

    def row(self, value):
        return self.wait.until(EC.presence_of_element_located(self._row_locator(value)))

    def wait_for_row(self, value):
        self.row(value)

    def wait_row_gone(self, value):
        self.wait.until(lambda d: not d.find_elements(*self._row_locator(value)))

    def rows(self):
        """Mỗi dòng là list text của các ô (ID, cột 2, cột 3, ...)."""
        self.find(self.COUNT)
        return [[td.text.strip() for td in tr.find_elements(By.TAG_NAME, "td")]
                for tr in self.driver.find_elements(*self.ROWS)]

    def count(self):
        """Số đếm hiển thị trên trang (vd. 'Number of Songs: 12' -> 12)."""
        return int(re.search(r"(\d+)\s*$", self.find(self.COUNT).text).group(1))

    # ---------- tìm kiếm ----------
    def search(self, term):
        box = self.find(self.SEARCH_INPUT)
        self.type_into(box, term)
        box.send_keys(Keys.ENTER)
        self.wait.until(EC.staleness_of(box))
        self.find(self.COUNT)
        return self

    def search_box_value(self):
        return self.find(self.SEARCH_INPUT).get_attribute("value")

    # ---------- modal ----------
    def modal(self):
        """Modal Bootstrap đang mở (trả về phần `.modal-content`)."""
        return self.wait.until(EC.visibility_of_element_located(self.OPEN_MODAL))

    def modal_is_open(self):
        return any(e.is_displayed() for e in self.driver.find_elements(*self.OPEN_MODAL))

    def fill(self, modal, **values):
        """Điền các ô trong modal theo thuộc tính `name`; giá trị None thì bỏ qua."""
        for name, value in values.items():
            if value is None:
                continue
            el = modal.find_element(By.CSS_SELECTOR, f"[name='{name}']")
            if el.tag_name == "select":
                Select(el).select_by_visible_text(value)
            else:
                self.type_into(el, str(value))

    def field(self, modal, name):
        return modal.find_element(By.CSS_SELECTOR, f"[name='{name}']")

    def save(self, modal):
        """Bấm nút gửi trong modal; chờ trang tải lại (nếu form được gửi đi)."""
        self.click_el(modal.find_element(By.CSS_SELECTOR, "button[type='submit']"))

    def save_and_wait_reload(self, modal):
        self.save(modal)
        self.wait.until(EC.staleness_of(modal))

    def open_add(self):
        self.click((By.CSS_SELECTOR, f"button[data-bs-target='#{self.ADD_MODAL_ID}']"))
        return self.modal()

    def _open_row_modal(self, value, button_css):
        self.click_el(self.row(value).find_element(By.CSS_SELECTOR, button_css))
        return self.modal()

    def open_edit(self, value):
        return self._open_row_modal(value, "button.btn-warning")

    def open_delete(self, value):
        return self._open_row_modal(value, "button.btn-danger")

    def edit(self, value, **new_values):
        modal = self.open_edit(value)
        self.fill(modal, **new_values)
        self.save_and_wait_reload(modal)

    def delete(self, value):
        modal = self.open_delete(value)
        self.save_and_wait_reload(modal)
        self.wait_row_gone(value)


class SongsPage(CrudPage):
    path = "/songs"
    ADD_MODAL_ID = "addSongModal"
    TITLE = "Song Management"

    def add_song(self, title, artist, genre, year="2020", duration="3:30",
                 audio="https://example.com/audio.mp3", save=True):
        modal = self.open_add()
        self.fill(modal, title=title, artist=artist, genre=genre,
                  releaseYear=year, duration=duration, audioFilePath=audio)
        if save:
            self.save_and_wait_reload(modal)
        return modal

    def audio_link(self, title):
        return self.row(title).find_element(By.CSS_SELECTOR, "a[target='_blank']")

    def song_cells(self):
        """[(title, artist, genre), ...] của các dòng đang hiển thị."""
        return [(r[1], r[2], r[3]) for r in self.rows()]


class ArtistsPage(CrudPage):
    path = "/artists"
    ADD_MODAL_ID = "addArtistModal"
    TITLE = "Artist Management"

    def add_artist(self, name, country="Vietnam", gender="Male",
                   informations="https://example.com/artist", save=True):
        modal = self.open_add()
        self.fill(modal, name=name, country=country, gender=gender, informations=informations)
        if save:
            self.save_and_wait_reload(modal)
        return modal


class GenresPage(CrudPage):
    path = "/genres"
    ADD_MODAL_ID = "addGenreModal"
    TITLE = "Genre Management"

    def add_genre(self, name, informations="https://example.com/genre", save=True):
        modal = self.open_add()
        self.fill(modal, name=name, informations=informations)
        if save:
            self.save_and_wait_reload(modal)
        return modal


class UsersPage(BasePage):
    """Chỉ ADMIN truy cập được."""
    path = "/users"
    HEADING = (By.CSS_SELECTOR, "h2")

    def heading(self):
        return self.find(self.HEADING).text.strip()
