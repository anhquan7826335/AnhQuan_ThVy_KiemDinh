"""Trang chủ và thanh điều hướng theo quyền."""
import pytest

from pages.pages import ArtistsPage, GenresPage, SongsPage


def test_home_shows_modules_for_normal_user(user_home):
    """User thường thấy 3 thẻ Artist/Song/Genre và KHÔNG thấy thẻ User Management."""
    assert user_home.card_titles() == ["Artist Management", "Song Management", "Genre Management"]


def test_navbar_for_normal_user(user_home):
    """Thanh điều hướng của User thường: Home, Artist, Song, Genre (không có User)."""
    assert user_home.nav_link_texts() == ["Home", "Artist", "Song", "Genre"]


@pytest.mark.parametrize("link,page_cls", [
    ("Artist", ArtistsPage),
    ("Song", SongsPage),
    ("Genre", GenresPage),
])
def test_navbar_links_open_the_right_page(driver, user_home, link, page_cls):
    """Bấm từng mục trên thanh điều hướng -> mở đúng trang."""
    user_home.click_nav(link)
    user_home.wait_for_path(page_cls.path)
    assert driver.title == page_cls.TITLE


def test_home_link_returns_to_home(driver, songs_page):
    songs_page.click_nav("Home")
    songs_page.wait_for_path("/")
    assert driver.title == "Home"


@pytest.mark.admin
def test_admin_sees_user_module(admin_home):
    """Admin thấy thêm mục User trên thanh điều hướng và thẻ User Management."""
    assert "User" in admin_home.nav_link_texts()
    assert "User Management" in admin_home.card_titles()
