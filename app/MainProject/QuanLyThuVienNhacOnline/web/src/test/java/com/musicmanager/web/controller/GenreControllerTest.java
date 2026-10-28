package com.musicmanager.web.controller;

import com.musicmanager.web.entity.Genre;
import com.musicmanager.web.exception.BadRequestException;
import com.musicmanager.web.exception.ResourceNotFoundException;
import com.musicmanager.web.service.GenreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Test GenreController (MockMvc) – nhóm TC_CTL */
class GenreControllerTest {

    MockMvc mvc;
    GenreService genreService = mock(GenreService.class);

    @BeforeEach
    void setUp() {
        mvc = TestMvc.build(new GenreController(), "genreService", genreService);
    }

    @Test @DisplayName("GET /genres - hiển thị danh sách")
    void list() throws Exception {
        when(genreService.getGenres()).thenReturn(List.of(new Genre()));
        when(genreService.numberOfGenres()).thenReturn(1L);
        mvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres"))
                .andExpect(model().attributeExists("genres", "genreRequest", "numberOfGenres"));
    }

    @Test @DisplayName("GET /genres/search - có từ khóa")
    void search() throws Exception {
        when(genreService.searchGenresByName("abc")).thenReturn(List.of(new Genre(), new Genre()));
        mvc.perform(get("/genres/search").param("name", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres"))
                .andExpect(model().attribute("searchQuery", "abc"))
                .andExpect(model().attribute("numberOfGenres", 2));
    }

    @Test @DisplayName("GET /genres/search - từ khóa rỗng → redirect")
    void searchBlank() throws Exception {
        mvc.perform(get("/genres/search").param("name", "  "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/genres"));
        verify(genreService, never()).searchGenresByName(any());
    }

    @Test @DisplayName("GET /genres/search - thiếu tham số → 400")
    void searchMissingParam() throws Exception {
        mvc.perform(get("/genres/search")).andExpect(status().isBadRequest());
    }

    @Test @DisplayName("POST /genres/create - hợp lệ")
    void create() throws Exception {
        mvc.perform(post("/genres/create").param("name", "New"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/genres"));
        verify(genreService).createGenre(any(Genre.class));
    }

    @Test @DisplayName("POST /genres/create - dữ liệu không hợp lệ → 400")
    void createInvalid() throws Exception {
        doThrow(new BadRequestException("Genre name existed!.")).when(genreService).createGenre(any());
        mvc.perform(post("/genres/create").param("name", "Dup")).andExpect(status().isBadRequest());
    }

    @Test @DisplayName("POST /genres/update/{id} - hợp lệ")
    void update() throws Exception {
        mvc.perform(post("/genres/update/1").param("name", "X"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/genres"));
        verify(genreService).updateGenre(eq("1"), any(Genre.class));
    }

    @Test @DisplayName("POST /genres/update/{id} - id không tồn tại → 404")
    void updateNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("nf")).when(genreService).updateGenre(eq("999"), any());
        mvc.perform(post("/genres/update/999").param("name", "X")).andExpect(status().isNotFound());
    }

    @Test @DisplayName("POST /genres/delete/{id} - hợp lệ")
    void delete() throws Exception {
        mvc.perform(post("/genres/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/genres"));
        verify(genreService).deleteGenre("1");
    }

    @Test @DisplayName("POST /genres/delete/{id} - id không tồn tại → 404")
    void deleteNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("nf")).when(genreService).deleteGenre("999");
        mvc.perform(post("/genres/delete/999")).andExpect(status().isNotFound());
    }
}
