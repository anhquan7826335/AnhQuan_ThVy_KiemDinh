package com.musicmanager.web.service;

import com.musicmanager.web.entity.Genre;
import com.musicmanager.web.exception.ResourceNotFoundException;
import com.musicmanager.web.repository.GenreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class GenreServiceTest
{
    @Autowired
    private GenreService genreService;
    @Autowired
    private GenreRepository genreRepository;

    @Test
    void createAndGetGenre()
    {
        Genre genre = new Genre();
        genre.setName("name");
        genre.setInformations("https://www.google.com/");

        Genre createdGenre = genreService.createGenre(genre);

        assertNotNull(createdGenre);
        assertNotNull(createdGenre.getId());
        assertEquals("name", createdGenre.getName());

        Genre foundInDatabase = genreRepository.findById(createdGenre.getId()).orElseThrow(() -> new ResourceNotFoundException("Genre not found!."));

        assertNotNull(foundInDatabase);
        assertEquals("name", foundInDatabase.getName());
    }

    @Test
    void deleteGenre()
    {
        Genre genre = new Genre();
        genre.setName("name");
        genre.setInformations("https://www.google.com/");

        genre = genreRepository.save(genre);

        String genreId = genre.getId();

        genreService.deleteGenre(genreId);

        boolean exists = genreRepository.existsById(genreId);
        assertFalse(exists);
    }
}