package com.musicmanager.web.integration;

import com.musicmanager.web.entity.*;
import com.musicmanager.web.exception.ResourceNotFoundException;
import com.musicmanager.web.repository.*;
import com.musicmanager.web.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: service + repository + H2 thật (@DataJpaTest, rollback sau mỗi test).
 * Chỉ nạp tầng JPA và các service cần thiết nên không phụ thuộc cấu hình Security.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ArtistService.class, GenreService.class, SongService.class, UserService.class, CrudFlowTest.TestBeans.class})
class CrudFlowTest {

    @TestConfiguration
    static class TestBeans {
        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    }

    @Autowired ArtistService artistService;
    @Autowired GenreService genreService;
    @Autowired SongService songService;
    @Autowired UserService userService;
    @Autowired GenreRepository genreRepository;
    @Autowired SongRepository songRepository;
    @Autowired UserRepository userRepository;

    @Test @DisplayName("Luồng Artist: create → get → update → delete")
    void artistFlow() {
        Artist a = new Artist(); a.setName("Flow Artist"); a.setCountry("VN");
        Artist created = artistService.createArtist(a);
        assertNotNull(created.getId());
        assertEquals("Flow Artist", artistService.getArtist(created.getId()).getName());

        Artist req = new Artist(); req.setName("Renamed"); req.setCountry("US");
        artistService.updateArtist(created.getId(), req);
        assertEquals("Renamed", artistService.getArtist(created.getId()).getName());
        assertEquals("US", artistService.getArtist(created.getId()).getCountry());
        assertEquals(1, artistService.searchArtistsByName("rename").size());

        artistService.deleteArtist(created.getId());
        assertThrows(ResourceNotFoundException.class, () -> artistService.getArtist(created.getId()));
    }

    @Test @DisplayName("Luồng Genre: create → get → update → delete")
    void genreFlow() {
        Genre g = new Genre(); g.setName("Flow Genre");
        Genre created = genreService.createGenre(g);
        assertEquals("Flow Genre", genreService.getGenre(created.getId()).getName());

        Genre req = new Genre(); req.setName("Renamed"); req.setInformations("info");
        genreService.updateGenre(created.getId(), req);
        assertEquals("Renamed", genreService.getGenre(created.getId()).getName());

        genreService.deleteGenre(created.getId());
        assertFalse(genreRepository.existsById(created.getId()));
    }

    @Test @DisplayName("Luồng Song: create → get → update → search → delete")
    void songFlow() {
        Song created = songService.createSong(new Song("Flow Song", "Flow Artist", "Pop", 2020, "/f.mp3", "3:00"));
        assertNotNull(created.getId());
        assertEquals("Flow Song", songService.getSong(created.getId()).getTitle());

        Song req = new Song(); req.setTitle("Updated Song");
        songService.updateSong(created.getId(), req);
        assertEquals("Updated Song", songService.getSong(created.getId()).getTitle());
        assertEquals("Flow Artist", songService.getSong(created.getId()).getArtist());

        assertTrue(songService.searchSongs("updated").stream().anyMatch(s -> s.getId().equals(created.getId())));

        songService.deleteSong(created.getId());
        assertFalse(songRepository.existsById(created.getId()));
    }

    @Test @DisplayName("Luồng User: create → update → search → delete")
    void userFlow() {
        User u = new User("flowuser", "flow@x.com", "USER");
        u.setPassword("password123");
        userService.createUser(u);
        User saved = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("flowuser", "flowuser").get(0);
        assertNotEquals("password123", saved.getPassword()); // đã mã hóa

        userService.updateUser(saved.getId(), new User("flowuser2", "flow2@x.com", "ADMIN"));
        assertEquals(1, userService.searchUsers("FLOW2").size());
        assertEquals("ADMIN", userService.searchUsers("flowuser2").get(0).getRole());

        userService.deleteUser(saved.getId());
        assertTrue(userService.searchUsers("flowuser2").isEmpty());
    }
}
