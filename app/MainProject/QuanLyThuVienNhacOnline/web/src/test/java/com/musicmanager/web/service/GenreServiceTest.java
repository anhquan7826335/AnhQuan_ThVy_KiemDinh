package com.musicmanager.web.service;

import com.musicmanager.web.entity.Genre;
import com.musicmanager.web.exception.BadRequestException;
import com.musicmanager.web.exception.ResourceNotFoundException;
import com.musicmanager.web.repository.GenreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit test GenreService (mock repository) – mã TC_GEN_xx */
@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock private GenreRepository genreRepository;
    @InjectMocks private GenreService genreService;

    private Genre genre(String id, String name) {
        Genre g = new Genre();
        g.setId(id);
        g.setName(name);
        g.setInformations("info");
        return g;
    }

    @Test @DisplayName("TC_GEN_01 getGenres - có dữ liệu")
    void getGenres_hasData() {
        when(genreRepository.findAll()).thenReturn(List.of(genre("1", "Pop"), genre("2", "Rock")));
        assertEquals(2, genreService.getGenres().size());
    }

    @Test @DisplayName("TC_GEN_02 getGenres - rỗng")
    void getGenres_empty() {
        when(genreRepository.findAll()).thenReturn(List.of());
        assertTrue(genreService.getGenres().isEmpty());
    }

    @Test @DisplayName("TC_GEN_03 getGenre - id tồn tại")
    void getGenre_found() {
        when(genreRepository.findById("1")).thenReturn(Optional.of(genre("1", "Pop")));
        assertEquals("Pop", genreService.getGenre("1").getName());
    }

    @Test @DisplayName("TC_GEN_04 getGenre - id không tồn tại")
    void getGenre_notFound() {
        when(genreRepository.findById("999")).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> genreService.getGenre("999"));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test @DisplayName("TC_GEN_05 searchGenresByName - có kết quả")
    void searchGenresByName_found() {
        when(genreRepository.findByNameContainingIgnoreCase("po")).thenReturn(List.of(genre("1", "Pop")));
        assertEquals(1, genreService.searchGenresByName("po").size());
    }

    @Test @DisplayName("TC_GEN_06 searchGenresByName - không có kết quả")
    void searchGenresByName_none() {
        when(genreRepository.findByNameContainingIgnoreCase("zzz")).thenReturn(List.of());
        assertTrue(genreService.searchGenresByName("zzz").isEmpty());
    }

    @Test @DisplayName("TC_GEN_07 searchGenreByName - chính xác")
    void searchGenreByName_found() {
        when(genreRepository.findByNameIgnoreCase("pop")).thenReturn(genre("1", "Pop"));
        assertEquals("Pop", genreService.searchGenreByName("pop").getName());
    }

    @Test @DisplayName("TC_GEN_08 searchGenreByName - không tồn tại trả null")
    void searchGenreByName_null() {
        when(genreRepository.findByNameIgnoreCase("zzz")).thenReturn(null);
        assertNull(genreService.searchGenreByName("zzz"));
    }

    @Test @DisplayName("TC_GEN_09 createGenre - hợp lệ")
    void createGenre_valid() {
        Genre req = genre(null, "New");
        Genre result = genreService.createGenre(req);
        assertEquals("New", result.getName());
        verify(genreRepository).save(req);
    }

    @Test @DisplayName("TC_GEN_10 createGenre - name null")
    void createGenre_nameNull() {
        assertThrows(BadRequestException.class, () -> genreService.createGenre(genre(null, null)));
        verify(genreRepository, never()).save(any());
    }

    @Test @DisplayName("TC_GEN_11 createGenre - name rỗng")
    void createGenre_nameEmpty() {
        assertThrows(BadRequestException.class, () -> genreService.createGenre(genre(null, "")));
    }

    @Test @DisplayName("TC_GEN_12 createGenre - name toàn khoảng trắng")
    void createGenre_nameBlank() {
        assertThrows(BadRequestException.class, () -> genreService.createGenre(genre(null, "   ")));
    }

    @Test @DisplayName("TC_GEN_13 createGenre - tên đã tồn tại")
    void createGenre_duplicate() {
        when(genreRepository.findByNameIgnoreCase("Pop")).thenReturn(genre("1", "Pop"));
        assertThrows(BadRequestException.class, () -> genreService.createGenre(genre(null, "Pop")));
        verify(genreRepository, never()).save(any());
    }

    @Test @DisplayName("TC_GEN_14 createGenre - trùng tên khác hoa/thường")
    void createGenre_duplicateDifferentCase() {
        when(genreRepository.findByNameIgnoreCase("pop")).thenReturn(genre("1", "Pop"));
        assertThrows(BadRequestException.class, () -> genreService.createGenre(genre(null, "pop")));
    }

    @Test @DisplayName("TC_GEN_15 updateGenre - cập nhật đầy đủ")
    void updateGenre_full() {
        Genre old = genre("1", "Old");
        when(genreRepository.findById("1")).thenReturn(Optional.of(old));
        Genre req = genre(null, "New");
        req.setInformations("new info");
        genreService.updateGenre("1", req);
        assertEquals("New", old.getName());
        assertEquals("new info", old.getInformations());
        verify(genreRepository).save(old);
    }

    @Test @DisplayName("TC_GEN_16 updateGenre - chỉ cập nhật trường khác null")
    void updateGenre_partial() {
        Genre old = genre("1", "Old");
        when(genreRepository.findById("1")).thenReturn(Optional.of(old));
        Genre req = new Genre();
        req.setInformations("only info");
        genreService.updateGenre("1", req);
        assertEquals("Old", old.getName());
        assertEquals("only info", old.getInformations());
    }

    @Test @DisplayName("TC_GEN_17 updateGenre - giữ nguyên tên của chính nó")
    void updateGenre_sameNameOfItself() {
        Genre old = genre("1", "Same");
        when(genreRepository.findById("1")).thenReturn(Optional.of(old));
        when(genreRepository.findByNameIgnoreCase("Same")).thenReturn(old);
        assertDoesNotThrow(() -> genreService.updateGenre("1", genre(null, "Same")));
        verify(genreRepository).save(old);
    }

    @Test @DisplayName("TC_GEN_18 updateGenre - id không tồn tại")
    void updateGenre_notFound() {
        when(genreRepository.findById("999")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> genreService.updateGenre("999", genre(null, "X")));
    }

    @Test @DisplayName("TC_GEN_19 updateGenre - đổi sang tên của bản ghi khác")
    void updateGenre_duplicateOfOther() {
        when(genreRepository.findById("1")).thenReturn(Optional.of(genre("1", "A")));
        when(genreRepository.findByNameIgnoreCase("B")).thenReturn(genre("2", "B"));
        assertThrows(BadRequestException.class, () -> genreService.updateGenre("1", genre(null, "B")));
        verify(genreRepository, never()).save(any());
    }

    @Test @DisplayName("TC_GEN_20 updateGenre - name null không ném NPE")
    void updateGenre_nameNull() {
        Genre old = genre("1", "Old");
        when(genreRepository.findById("1")).thenReturn(Optional.of(old));
        Genre req = new Genre();
        req.setInformations("x");
        assertDoesNotThrow(() -> genreService.updateGenre("1", req));
        assertEquals("Old", old.getName());
    }

    @Test @DisplayName("TC_GEN_21 deleteGenre - tồn tại")
    void deleteGenre_found() {
        when(genreRepository.existsById("1")).thenReturn(true);
        genreService.deleteGenre("1");
        verify(genreRepository, times(1)).deleteById("1");
    }

    @Test @DisplayName("TC_GEN_22 deleteGenre - không tồn tại")
    void deleteGenre_notFound() {
        when(genreRepository.existsById("999")).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> genreService.deleteGenre("999"));
        verify(genreRepository, never()).deleteById(any());
    }

    @Test @DisplayName("TC_GEN_23/24 numberOfGenres")
    void numberOfGenres() {
        when(genreRepository.count()).thenReturn(5L, 0L);
        assertEquals(5L, genreService.numberOfGenres());
        assertEquals(0L, genreService.numberOfGenres());
    }
}
