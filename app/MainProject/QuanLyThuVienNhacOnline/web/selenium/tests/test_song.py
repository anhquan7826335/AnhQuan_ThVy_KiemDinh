"""Tìm kiếm và quản lý bài hát qua giao diện.

Mã TC_PLY_xx lấy từ sheet "Phát nhạc - Tìm kiếm" của docs/TestCase_QuanLyThuVienNhac.xlsx.
Test tự tạo bài hát có chuỗi duy nhất (uid) rồi tự xóa, nên không phụ thuộc/không làm bẩn dữ liệu có sẵn.
"""
import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC

from tests.settings import unique


def titles(page):
    return [row[1] for row in page.rows()]


@pytest.fixture
def two_songs(new_song):
    """Hai bài hát có từ khóa duy nhất theo tên / nghệ sĩ / thể loại (b có tiếng Việt có dấu)."""
    uid = unique("")
    a = new_song(title=f"Love {uid}", artist=f"ArtistA{uid}", genre=f"Pop{uid}")
    b = new_song(title=f"Mưa {uid}", artist=f"ArtistB{uid}", genre=f"Rock{uid}")
    return uid, a, b


# ======================= TÌM KIẾM =======================
def test_tc_ply_01_search_by_title(songs_page, two_songs):
    uid, a, _ = two_songs
    songs_page.search(f"Love {uid}")
    assert titles(songs_page) == [a.title]
    assert songs_page.count() == 1


def test_tc_ply_02_search_by_artist(songs_page, two_songs):
    uid, a, _ = two_songs
    songs_page.search(f"ArtistA{uid}")
    assert titles(songs_page) == [a.title]


def test_tc_ply_03_search_by_genre(songs_page, two_songs):
    uid, a, _ = two_songs
    songs_page.search(f"Pop{uid}")
    assert titles(songs_page) == [a.title]


def test_tc_ply_04_search_is_case_insensitive(songs_page, two_songs):
    _, a, _ = two_songs
    songs_page.search(a.title.upper())
    upper = titles(songs_page)
    songs_page.search(a.title.lower())
    assert upper == titles(songs_page) == [a.title]


def test_tc_ply_05_blank_keyword_lists_everything(songs_page, two_songs):
    total = songs_page.count()
    songs_page.search("")
    assert songs_page.count() == total
    assert len(songs_page.rows()) == total


def test_tc_ply_06_no_result(songs_page, two_songs):
    uid, _, _ = two_songs
    songs_page.search(f"zzzz{uid}")
    assert songs_page.rows() == []
    assert songs_page.count() == 0


@pytest.mark.parametrize("keyword", ["%", "_", "'", '"'])
def test_tc_ply_07_special_characters_do_not_break_search(driver, songs_page, keyword):
    songs_page.search(keyword)
    assert driver.title == songs_page.TITLE  # không rơi vào trang lỗi 500
    assert songs_page.count() >= 0


def test_tc_ply_08_search_vietnamese_with_diacritics(songs_page, two_songs):
    uid, _, b = two_songs
    songs_page.search(f"Mưa {uid}")
    assert titles(songs_page) == [b.title]


def test_tc_ply_09_very_long_keyword(driver, songs_page):
    songs_page.search("a" * 1000)
    assert driver.title == songs_page.TITLE
    assert songs_page.count() == 0


def test_tc_ply_10_keyword_stays_in_search_box(songs_page):
    songs_page.search("giu-tu-khoa")
    assert songs_page.search_box_value() == "giu-tu-khoa"


# ======================= QUẢN LÝ BÀI HÁT =======================
def test_tc_ply_19_add_song(songs_page, new_song):
    """Thêm bài hợp lệ -> xuất hiện trong danh sách, số lượng +1, các cột đúng dữ liệu nhập."""
    before = songs_page.count()
    song = new_song(year="2021", duration="4:05")
    songs_page.open()
    assert songs_page.count() == before + 1
    row = next(r for r in songs_page.rows() if r[1] == song.title)
    assert row[2:6] == [song.artist, song.genre, "2021", "4:05"]


@pytest.mark.parametrize("blank", ["title", "artist", "genre"])
def test_tc_ply_20_blank_required_field_blocked_in_browser(songs_page, blank):
    """Thiếu tên/nghệ sĩ/thể loại -> trình duyệt chặn, modal vẫn mở, không gửi lên server."""
    values = dict(title=unique("Song "), artist=unique("Artist "), genre=unique("Genre "))
    values[blank] = ""
    modal = songs_page.add_song(save=False, **values)
    songs_page.save(modal)
    assert songs_page.is_invalid(songs_page.field(modal, blank))
    assert songs_page.modal_is_open()
    assert songs_page.current_path() == "/songs"


def test_tc_ply_16_audio_path_is_required(songs_page):
    """Để trống Audio File Path trên form thêm -> trình duyệt chặn gửi."""
    modal = songs_page.add_song(unique("Song "), unique("Artist "), unique("Genre "), audio="", save=False)
    songs_page.save(modal)
    assert songs_page.is_invalid(songs_page.field(modal, "audioFilePath"))
    assert songs_page.modal_is_open()


@pytest.mark.xfail(reason="D1: lỗi nhập liệu (400) bị GlobalException trả thành trang 'Lỗi 500'", strict=False)
def test_d1_blank_title_should_not_show_500_page(songs_page):
    """TC_PLY_20 (lớp server) - Bỏ qua HTML5, gửi tên trống: mong đợi lỗi 400/thông báo thân thiện, không phải 500."""
    modal = songs_page.add_song(title="", artist=unique("Artist "), genre=unique("Genre "), save=False)
    songs_page.submit_without_html5(modal.find_element(By.CSS_SELECTOR, "form"))
    songs_page.wait.until(EC.staleness_of(modal))
    assert "Lỗi 500" not in songs_page.page_text()


def test_tc_ply_21_edit_song(songs_page, new_song, created):
    """Sửa tên bài hát -> danh sách cập nhật tên mới, tên cũ biến mất."""
    song = new_song()
    new_title = unique("Edited ")
    created.songs.append(new_title)
    songs_page.edit(song.title, title=new_title)
    assert songs_page.has_row(new_title)
    assert not songs_page.has_row(song.title)


def test_tc_ply_22_delete_song(songs_page, new_song):
    """Xóa bài hát (xác nhận trong modal) -> biến khỏi danh sách, số lượng giảm 1."""
    song = new_song()
    before = songs_page.count()
    songs_page.delete(song.title)
    assert not songs_page.has_row(song.title)
    assert songs_page.count() == before - 1


def test_delete_modal_cancel_keeps_song(songs_page, new_song):
    """Bấm Cancel ở hộp xác nhận xóa -> bài hát vẫn còn."""
    song = new_song()
    modal = songs_page.open_delete(song.title)
    songs_page.click_el(modal.find_element(By.CSS_SELECTOR, "button[data-bs-dismiss='modal'].btn-secondary"))
    songs_page.wait.until(lambda d: not songs_page.modal_is_open())
    assert songs_page.has_row(song.title)


# ======================= LIÊN KẾT FILE ÂM THANH =======================
def test_tc_ply_12_audio_link_opens_in_new_tab(songs_page, new_song):
    url = f"https://example.com/audio/{unique('')}.mp3"
    song = new_song(audio=url)
    link = songs_page.audio_link(song.title)
    assert link.get_attribute("target") == "_blank"
    assert link.get_attribute("href") == url


@pytest.mark.xfail(reason="D8: hệ thống không kiểm tra audioFilePath, vẫn lưu đường dẫn 'javascript:'", strict=False)
def test_tc_ply_15_javascript_audio_path_is_rejected(songs_page, created):
    title = unique("Song ")
    created.songs.append(title)
    songs_page.add_song(title, unique("Artist "), unique("Genre "), audio="javascript:alert(1)")
    assert not songs_page.has_row(title)


# ======================= CHƯA CÓ CHỨC NĂNG (D7) =======================
@pytest.mark.blocked
@pytest.mark.skip(reason="Blocked (D7): chưa có trình phát nhúng - TC_PLY_17 Play/Pause")
def test_tc_ply_17_embedded_player_play_pause():
    pass


@pytest.mark.blocked
@pytest.mark.skip(reason="Blocked (D7): chưa có trình phát nhúng - TC_PLY_18 chuyển bài khi đang phát")
def test_tc_ply_18_embedded_player_switch_track():
    pass
