"""Quản lý nghệ sĩ và thể loại qua giao diện (thêm, sửa, xóa, tìm kiếm).

Các ca này bổ sung cho bảng test case (chức năng CRUD Artist/Genre đã có ở sheet Controller/Service
nhưng chưa có ca giao diện). Dữ liệu test có tiền tố ngẫu nhiên và được xóa sau khi chạy.
"""
from tests.settings import unique


# ======================= NGHỆ SĨ =======================
def test_artist_add(artists_page, new_artist):
    before = artists_page.count()
    name = new_artist(country="Viet Nam", gender="Female")
    artists_page.open()
    assert artists_page.count() == before + 1
    row = next(r for r in artists_page.rows() if r[1] == name)
    assert row[2:4] == ["Viet Nam", "Female"]


def test_artist_edit(artists_page, new_artist, created):
    old = new_artist()
    new_name = unique("Renamed ")
    created.artists.append(new_name)
    artists_page.edit(old, name=new_name, country="Japan", gender="Female")
    assert artists_page.has_row(new_name) and not artists_page.has_row(old)
    row = next(r for r in artists_page.rows() if r[1] == new_name)
    assert row[2:4] == ["Japan", "Female"]


def test_artist_delete(artists_page, new_artist):
    name = new_artist()
    before = artists_page.count()
    artists_page.delete(name)
    assert not artists_page.has_row(name)
    assert artists_page.count() == before - 1


def test_artist_search_by_name(artists_page, new_artist):
    name = new_artist()
    artists_page.search(name)
    assert [r[1] for r in artists_page.rows()] == [name]
    assert artists_page.search_box_value() == name


def test_artist_blank_name_blocked_in_browser(artists_page):
    modal = artists_page.add_artist("", save=False)
    artists_page.save(modal)
    assert artists_page.is_invalid(artists_page.field(modal, "name"))
    assert artists_page.modal_is_open()


def test_artist_invalid_url_blocked_in_browser(artists_page):
    """Ô Informations là type=url: giá trị không phải URL bị trình duyệt chặn."""
    modal = artists_page.add_artist(unique("Artist "), informations="khong-phai-url", save=False)
    artists_page.save(modal)
    assert artists_page.is_invalid(artists_page.field(modal, "informations"))
    assert artists_page.modal_is_open()


# ======================= THỂ LOẠI =======================
def test_genre_add(genres_page, new_genre):
    before = genres_page.count()
    name = new_genre()
    genres_page.open()
    assert genres_page.count() == before + 1
    assert genres_page.has_row(name)


def test_genre_edit(genres_page, new_genre, created):
    old = new_genre()
    new_name = unique("Renamed ")
    created.genres.append(new_name)
    genres_page.edit(old, name=new_name)
    assert genres_page.has_row(new_name) and not genres_page.has_row(old)


def test_genre_delete(genres_page, new_genre):
    name = new_genre()
    before = genres_page.count()
    genres_page.delete(name)
    assert not genres_page.has_row(name)
    assert genres_page.count() == before - 1


def test_genre_search_by_name(genres_page, new_genre):
    name = new_genre()
    genres_page.search(name)
    assert [r[1] for r in genres_page.rows()] == [name]


def test_genre_blank_name_blocked_in_browser(genres_page):
    modal = genres_page.add_genre("", save=False)
    genres_page.save(modal)
    assert genres_page.is_invalid(genres_page.field(modal, "name"))
    assert genres_page.modal_is_open()
