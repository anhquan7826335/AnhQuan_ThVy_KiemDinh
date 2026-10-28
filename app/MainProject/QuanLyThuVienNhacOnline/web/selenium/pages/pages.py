"""Page Object Model cho https://www.saucedemo.com"""
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select, WebDriverWait


class BasePage:
    def __init__(self, driver, base_url, timeout=10):
        self.driver = driver
        self.base_url = base_url.rstrip("/")
        self.wait = WebDriverWait(driver, timeout)

    def find(self, locator):
        return self.wait.until(EC.visibility_of_element_located(locator))

    def click(self, locator):
        self.wait.until(EC.element_to_be_clickable(locator)).click()


class LoginPage(BasePage):
    USERNAME = (By.ID, "user-name")
    PASSWORD = (By.ID, "password")
    LOGIN_BTN = (By.ID, "login-button")
    ERROR = (By.CSS_SELECTOR, "[data-test='error']")

    def open(self):
        self.driver.get(self.base_url + "/")
        return self

    def login(self, username, password):
        self.find(self.USERNAME).clear()
        self.find(self.USERNAME).send_keys(username)
        self.find(self.PASSWORD).clear()
        self.find(self.PASSWORD).send_keys(password)
        self.click(self.LOGIN_BTN)

    def error_text(self):
        return self.find(self.ERROR).text

    def is_displayed(self):
        return self.find(self.LOGIN_BTN).is_displayed()


class InventoryPage(BasePage):
    TITLE = (By.CSS_SELECTOR, "[data-test='title']")
    ITEMS = (By.CSS_SELECTOR, ".inventory_item")
    ITEM_NAMES = (By.CSS_SELECTOR, ".inventory_item_name")
    ITEM_PRICES = (By.CSS_SELECTOR, ".inventory_item_price")
    CART_BADGE = (By.CSS_SELECTOR, ".shopping_cart_badge")
    CART_LINK = (By.CSS_SELECTOR, ".shopping_cart_link")
    MENU_BTN = (By.ID, "react-burger-menu-btn")
    LOGOUT_LINK = (By.ID, "logout_sidebar_link")
    SORT = (By.CSS_SELECTOR, "[data-test='product-sort-container']")

    def title(self):
        return self.find(self.TITLE).text

    def item_count(self):
        self.find(self.ITEMS)
        return len(self.driver.find_elements(*self.ITEMS))

    def add_to_cart(self, product_slug):
        self.click((By.ID, f"add-to-cart-{product_slug}"))

    def remove_from_cart(self, product_slug):
        self.click((By.ID, f"remove-{product_slug}"))

    def cart_count(self):
        badges = self.driver.find_elements(*self.CART_BADGE)
        return int(badges[0].text) if badges else 0

    def open_cart(self):
        self.click(self.CART_LINK)

    def sort_by(self, value):
        Select(self.find(self.SORT)).select_by_value(value)

    def prices(self):
        self.find(self.ITEMS)
        return [float(e.text.replace("$", "")) for e in self.driver.find_elements(*self.ITEM_PRICES)]

    def names(self):
        self.find(self.ITEMS)
        return [e.text for e in self.driver.find_elements(*self.ITEM_NAMES)]

    def logout(self):
        self.click(self.MENU_BTN)
        self.click(self.LOGOUT_LINK)


class CartPage(BasePage):
    CART_ITEMS = (By.CSS_SELECTOR, ".cart_item")
    ITEM_NAMES = (By.CSS_SELECTOR, ".inventory_item_name")

    def names(self):
        self.wait.until(EC.url_contains("cart"))
        return [e.text for e in self.driver.find_elements(*self.ITEM_NAMES)]

    def item_count(self):
        self.wait.until(EC.url_contains("cart"))
        return len(self.driver.find_elements(*self.CART_ITEMS))
