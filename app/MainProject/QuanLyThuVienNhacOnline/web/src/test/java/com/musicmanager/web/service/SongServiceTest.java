package com.musicmanager.web.service;

import com.musicmanager.web.entity.Song;
import com.musicmanager.web.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit test SongService (mock repository) – mã TC_SNG_xx */
class SongServiceTest {

    private SongRepository songRepository;
    private SongService songService;

    @BeforeEach
    void setUp() {
        songRepository = mock(SongRepository.class);
        songService = new SongService();
        songService.songRepository = songRepository;
    }

    private Song song(String title, String artist, String genre) {
        Song s = new Song();
        s.setTitle(title);
        s.setArtist(artist);
        s.setGenre(genre);
        return s;
    }

    private void assertStatus(HttpStatus expected, ResponseStatusException ex) {
        assertEquals(expected, ex.getStatusCode());
    }

    // ---------- createSong ----------
    @Test @DisplayName("TC_SNG_01 createSong - hợp lệ")
    void createSong_valid() {
        Song s = song("Test Song", "Artist", "Pop");
        when(songRepository.save(s)).thenReturn(s);
        Song result = songService.createSong(s);
        assertEquals("Test Song", result.getTitle());
        verify(songRepository).save(s);
    }

    @Test @DisplayName("TC_SNG_02 createSong - title null")
    void createSong_titleNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> songService.createSong(song(null, "A", "Pop")));
        assertStatus(HttpStatus.BAD_REQUEST, ex);
        verify(songRepository, never()).save(any());
    }

    @Test @DisplayName("TC_SNG_03 createSong - title khoảng trắng")
    void createSong_titleBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> songService.createSong(song("  ", "A", "Pop")));
        assertStatus(HttpStatus.BAD_REQUEST, ex);
    }

    @Test @DisplayName("TC_SNG_04 createSong - thiếu artist (null / rỗng)")
    void createSong_artistMissing() {
        assertStatus(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> songService.createSong(song("T", null, "Pop"))));
        assertStatus(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> songService.createSong(song("T", "", "Pop"))));
        verify(songRepository, never()).save(any());
    }

    @Test @DisplayName("TC_SNG_05 createSong - thiếu genre (null / rỗng)")
    void createSong_genreMissing() {
        assertStatus(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> songService.createSong(song("T", "A", null))));
        assertStatus(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> songService.createSong(song("T", "A", " "))));
        verify(songRepository, never()).save(any());
    }

    @Test @DisplayName("TC_SNG_06 createSong - chỉ có title")
    void createSong_onlyTitle() {
        Song s = new Song();
        s.setTitle("Only title");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> songService.createSong(s));
        assertStatus(HttpStatus.BAD_REQUEST, ex);
        assertEquals("không được để trống", ex.getReason());
    }

    // ---------- getAllSongs / getSong ----------
    @Test @DisplayName("TC_SNG_07 getAllSongs - có dữ liệu")
    void getAllSongs_hasData() {
        when(songRepository.findAll()).thenReturn(List.of(new Song(), new Song(), new Song()));
        assertEquals(3, songService.getAllSongs().size());
    }

    @Test @DisplayName("TC_SNG_08 getAllSongs - rỗng")
    void getAllSongs_empty() {
        when(songRepository.findAll()).thenReturn(List.of());
        assertTrue(songService.getAllSongs().isEmpty());
    }

    @Test @DisplayName("TC_SNG_09 getSong - id tồn tại")
    void getSong_found() {
        Song s = song("Test", "A", "Pop");
        s.setId("123");
        when(songRepository.findById("123")).thenReturn(Optional.of(s));
        assertEquals("Test", songService.getSong("123").getTitle());
    }

    @Test @DisplayName("TC_SNG_10 getSong - id không tồn tại")
    void getSong_notFound() {
        when(songRepository.findById("999")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> songService.getSong("999"));
        assertStatus(HttpStatus.NOT_FOUND, ex);
    }

    // ---------- updateSong ----------
    @Test @DisplayName("TC_SNG_11 updateSong - cập nhật đủ 6 trường")
    void updateSong_allFields() {
        Song old = song("Old", "OldA", "OldG");
        when(songRepository.findById("1")).thenReturn(Optional.of(old));
        Song req = new Song("New", "NewA", "NewG", 2024, "/new.mp3", "3:30");

        songService.updateSong("1", req);

        assertEquals("New", old.getTitle());
        assertEquals("NewA", old.getArtist());
        assertEquals("NewG", old.getGenre());
        assertEquals(2024, old.getReleaseYear());
        assertEquals("/new.mp3", old.getAudioFilePath());
        assertEquals("3:30", old.getDuration());
        verify(songRepository).save(old);
    }

    @Test @DisplayName("TC_SNG_12 updateSong - chỉ cập nhật một số trường")
    void updateSong_partial() {
        Song old = new Song("Old", "OldA", "OldG", 2000, "/old.mp3", "3:00");
        when(songRepository.findById("1")).thenReturn(Optional.of(old));
        Song req = new Song();
        req.setTitle("New");

        songService.updateSong("1", req);

        assertEquals("New", old.getTitle());
        assertEquals("OldA", old.getArtist());
        assertEquals("OldG", old.getGenre());
        assertEquals(2000, old.getReleaseYear());
        assertEquals("/old.mp3", old.getAudioFilePath());
        assertEquals("3:00", old.getDuration());
    }

    @Test @DisplayName("TC_SNG_13 updateSong - id không tồn tại")
    void updateSong_notFound() {
        when(songRepository.findById("999")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> songService.updateSong("999", new Song()));
        assertStatus(HttpStatus.NOT_FOUND, ex);
        verify(songRepository, never()).save(any());
    }

    // ---------- deleteSong ----------
    @Test @DisplayName("TC_SNG_14 deleteSong - tồn tại")
    void deleteSong_found() {
        when(songRepository.existsById("123")).thenReturn(true);
        songService.deleteSong("123");
        verify(songRepository, times(1)).deleteById("123");
    }

    @Test @DisplayName("TC_SNG_15 deleteSong - không tồn tại")
    void deleteSong_notFound() {
        when(songRepository.existsById("999")).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> songService.deleteSong("999"));
        assertStatus(HttpStatus.NOT_FOUND, ex);
        verify(songRepository, never()).deleteById(any());
    }

    // ---------- searchSongs ----------
    private void stubSearchData() {
        when(songRepository.findAll()).thenReturn(Arrays.asList(
                song("Love Song", "Someone", "Rock"),
                song("Shake", "Taylor Swift", "Country"),
                song("Beat", "DJ", "Pop")));
    }

    @Test @DisplayName("TC_SNG_16 searchSongs - theo title")
    void searchSongs_byTitle() {
        stubSearchData();
        List<Song> r = songService.searchSongs("love");
        assertEquals(1, r.size());
        assertEquals("Love Song", r.get(0).getTitle());
    }

    @Test @DisplayName("TC_SNG_17 searchSongs - theo artist")
    void searchSongs_byArtist() {
        stubSearchData();
        List<Song> r = songService.searchSongs("taylor");
        assertEquals(1, r.size());
        assertEquals("Taylor Swift", r.get(0).getArtist());
    }

    @Test @DisplayName("TC_SNG_18 searchSongs - theo genre")
    void searchSongs_byGenre() {
        stubSearchData();
        List<Song> r = songService.searchSongs("pop");
        assertEquals(1, r.size());
        assertEquals("Pop", r.get(0).getGenre());
    }

    @Test @DisplayName("TC_SNG_19 searchSongs - không phân biệt hoa/thường")
    void searchSongs_ignoreCase() {
        stubSearchData();
        assertEquals(1, songService.searchSongs("LOVE").size());
    }

    @Test @DisplayName("TC_SNG_20 searchSongs - keyword null")
    void searchSongs_keywordNull() {
        stubSearchData();
        assertEquals(3, songService.searchSongs(null).size());
    }

    @Test @DisplayName("TC_SNG_21 searchSongs - keyword rỗng/khoảng trắng")
    void searchSongs_keywordBlank() {
        stubSearchData();
        assertEquals(3, songService.searchSongs("").size());
        assertEquals(3, songService.searchSongs("   ").size());
    }

    @Test @DisplayName("TC_SNG_22 searchSongs - không có kết quả")
    void searchSongs_none() {
        stubSearchData();
        assertTrue(songService.searchSongs("zzz").isEmpty());
    }

    @Test @DisplayName("TC_SNG_23 searchSongs - bản ghi có trường null không gây NPE")
    void searchSongs_nullFields() {
        when(songRepository.findAll()).thenReturn(Arrays.asList(new Song(), song("Love", null, null)));
        List<Song> r = assertDoesNotThrow(() -> songService.searchSongs("love"));
        assertEquals(1, r.size());
    }

    @Test @DisplayName("TC_SNG_24 countSongs")
    void countSongs() {
        when(songRepository.count()).thenReturn(5L);
        assertEquals(5, songService.countSongs());
    }
}
