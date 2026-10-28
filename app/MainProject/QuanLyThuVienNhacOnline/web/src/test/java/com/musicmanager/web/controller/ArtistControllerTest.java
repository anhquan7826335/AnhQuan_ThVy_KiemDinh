package com.musicmanager.web.controller;

import com.musicmanager.web.entity.Artist;
import com.musicmanager.web.exception.BadRequestException;
import com.musicmanager.web.exception.ResourceNotFoundException;
import com.musicmanager.web.service.ArtistService;
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

/** Test ArtistController (MockMvc) – nhóm TC_CTL */
class ArtistControllerTest {

    MockMvc mvc;
    ArtistService artistService = mock(ArtistService.class);

    @BeforeEach
    void setUp() {
        mvc = TestMvc.build(new ArtistController(), "artistService", artistService);
    }

    @Test @DisplayName("GET /artists - hiển thị danh sách")
    void list() throws Exception {
        when(artistService.getArtists()).thenReturn(List.of(new Artist()));
        when(artistService.numberOfArtists()).thenReturn(1L);
        mvc.perform(get("/artists"))
                .andExpect(status().isOk())
                .andExpect(view().name("artists"))
                .andExpect(model().attributeExists("artists", "artistRequest", "numberOfArtists"));
    }

    @Test @DisplayName("GET /artists/search - có từ khóa")
    void search() throws Exception {
        when(artistService.searchArtistsByName("abc")).thenReturn(List.of(new Artist(), new Artist()));
        mvc.perform(get("/artists/search").param("name", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("artists"))
                .andExpect(model().attribute("searchQuery", "abc"))
                .andExpect(model().attribute("numberOfArtists", 2));
    }

    @Test @DisplayName("GET /artists/search - từ khóa rỗng → redirect")
    void searchBlank() throws Exception {
        mvc.perform(get("/artists/search").param("name", "  "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/artists"));
        verify(artistService, never()).searchArtistsByName(any());
    }

    @Test @DisplayName("GET /artists/search - thiếu tham số → 400")
    void searchMissingParam() throws Exception {
        mvc.perform(get("/artists/search")).andExpect(status().isBadRequest());
    }

    @Test @DisplayName("POST /artists/create - hợp lệ")
    void create() throws Exception {
        mvc.perform(post("/artists/create").param("name", "New").param("country", "VN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/artists"));
        verify(artistService).createArtist(any(Artist.class));
    }

    @Test @DisplayName("POST /artists/create - dữ liệu không hợp lệ → 400")
    void createInvalid() throws Exception {
        doThrow(new BadRequestException("Artist name existed!.")).when(artistService).createArtist(any());
        mvc.perform(post("/artists/create").param("name", "Dup")).andExpect(status().isBadRequest());
    }

    @Test @DisplayName("POST /artists/update/{id} - hợp lệ")
    void update() throws Exception {
        mvc.perform(post("/artists/update/1").param("name", "X"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/artists"));
        verify(artistService).updateArtist(eq("1"), any(Artist.class));
    }

    @Test @DisplayName("POST /artists/update/{id} - id không tồn tại → 404")
    void updateNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("nf")).when(artistService).updateArtist(eq("999"), any());
        mvc.perform(post("/artists/update/999").param("name", "X")).andExpect(status().isNotFound());
    }

    @Test @DisplayName("POST /artists/delete/{id} - hợp lệ")
    void delete() throws Exception {
        mvc.perform(post("/artists/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/artists"));
        verify(artistService).deleteArtist("1");
    }

    @Test @DisplayName("POST /artists/delete/{id} - id không tồn tại → 404")
    void deleteNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("nf")).when(artistService).deleteArtist("999");
        mvc.perform(post("/artists/delete/999")).andExpect(status().isNotFound());
    }
}
