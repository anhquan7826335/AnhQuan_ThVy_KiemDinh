package com.musicmanager.web.service;

import com.musicmanager.web.entity.Song;
import com.musicmanager.web.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SongServiceTest {
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private SongService songService;

    @BeforeEach
    void setUp() {
        songRepository = mock(SongRepository.class);
        songService = new SongService();
        songService.songRepository = songRepository;
    }

    @Test
    void testCreateSong_withValidData_shouldSave() {
        Song song = new Song();
        song.setTitle("Test Song");

        when(songRepository.save(song)).thenReturn(song);

        Song result = songService.createSong(song);
        assertEquals("Test Song", result.getTitle());
    }

    @Test
    void testCreateSong_withBlankTitle_shouldThrow() {
        Song song = new Song();
        song.setTitle("  ");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            songService.createSong(song);
        });
        assertEquals("400 BAD_REQUEST \"Song title cannot be blank\"", exception.getMessage());

    }

    @Test
    void testGetSong_withValidId_shouldReturnSong() {
        Song song = new Song();
        song.setId("123");
        song.setTitle("Test");

        when(songRepository.findById("123")).thenReturn(Optional.of(song));

        Song result = songService.getSong("123");
        assertEquals("Test", result.getTitle());
    }

    @Test
    void testGetSong_withInvalidId_shouldThrow() {
        when(songRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            songService.getSong("999");
        });
    }

    @Test
    void testSearchSongs_withKeyword_shouldReturnFilteredList() {
        Song s1 = new Song(); s1.setTitle("Love Song");
        Song s2 = new Song(); s2.setArtist("Taylor Swift");
        Song s3 = new Song(); s3.setGenre("Pop");

        when(songRepository.findAll()).thenReturn(Arrays.asList(s1, s2, s3));

        List<Song> result = songService.searchSongs("love");
        assertEquals(1, result.size());
        assertEquals("Love Song", result.get(0).getTitle());
    }

    @Test
    void testDeleteSong_withValidId_shouldDelete() {
        when(songRepository.existsById("123")).thenReturn(true);
        songService.deleteSong("123");
        verify(songRepository, times(1)).deleteById("123");
    }

    @Test
    void testDeleteSong_withInvalidId_shouldThrow() {
        when(songRepository.existsById("999")).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> {
            songService.deleteSong("999");
        });
    }

    @Test
    void testCountSongs_shouldReturnCorrectCount() {
        when(songRepository.count()).thenReturn(5L);
        assertEquals(5, songService.countSongs());
    }
}
