package com.musicmanager.web.controller;

import com.musicmanager.web.entity.Song;
import com.musicmanager.web.service.SongService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Test SongController (MockMvc) – nhóm TC_CTL */
class SongControllerTest {

    MockMvc mvc;
    SongService songService = mock(SongService.class);

    @BeforeEach
    void setUp() {
        mvc = TestMvc.build(new SongController(), "songService", songService);
    }

    @Test @DisplayName("GET /songs - hiển thị danh sách")
    void list() throws Exception {
        when(songService.getAllSongs()).thenReturn(List.of(new Song(), new Song()));
        mvc.perform(get("/songs"))
                .andExpect(status().isOk())
                .andExpect(view().name("songs"))
                .andExpect(model().attributeExists("songs", "songRequest"))
                .andExpect(model().attribute("numberOfSongs", 2));
    }

    @Test @DisplayName("GET /songs/search - có keyword")
    void search() throws Exception {
        when(songService.searchSongs("love")).thenReturn(List.of(new Song()));
        mvc.perform(get("/songs/search").param("keyword", "love"))
                .andExpect(status().isOk())
                .andExpect(view().name("songs"))
                .andExpect(model().attribute("searchQuery", "love"))
                .andExpect(model().attribute("numberOfSongs", 1));
    }

    @Test @DisplayName("GET /songs/search - không truyền keyword")
    void searchNoKeyword() throws Exception {
        when(songService.searchSongs(null)).thenReturn(List.of(new Song(), new Song(), new Song()));
        mvc.perform(get("/songs/search"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("numberOfSongs", 3));
    }

    @Test @DisplayName("POST /songs/create - hợp lệ")
    void create() throws Exception {
        mvc.perform(post("/songs/create").param("title", "T").param("artist", "A").param("genre", "G"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/songs"));
        verify(songService).createSong(any(Song.class));
    }

    @Test @DisplayName("POST /songs/create - dữ liệu không hợp lệ → 400")
    void createInvalid() throws Exception {
        when(songService.createSong(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "không được để trống"));
        mvc.perform(post("/songs/create").param("title", "")).andExpect(status().isBadRequest());
    }

    @Test @DisplayName("POST /songs/update/{id} - hợp lệ")
    void update() throws Exception {
        mvc.perform(post("/songs/update/1").param("title", "X"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/songs"));
        verify(songService).updateSong(eq("1"), any(Song.class));
    }

    @Test @DisplayName("POST /songs/update/{id} - id không tồn tại → 404")
    void updateNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(songService).updateSong(eq("999"), any());
        mvc.perform(post("/songs/update/999").param("title", "X")).andExpect(status().isNotFound());
    }

    @Test @DisplayName("POST /songs/delete/{id} - hợp lệ")
    void delete() throws Exception {
        mvc.perform(post("/songs/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/songs"));
        verify(songService).deleteSong("1");
    }

    @Test @DisplayName("POST /songs/delete/{id} - id không tồn tại → 404")
    void deleteNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(songService).deleteSong("999");
        mvc.perform(post("/songs/delete/999")).andExpect(status().isNotFound());
    }
}
