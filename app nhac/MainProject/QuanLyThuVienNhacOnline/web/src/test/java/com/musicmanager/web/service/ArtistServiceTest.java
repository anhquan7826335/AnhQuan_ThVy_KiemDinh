package com.musicmanager.web.service;

import com.musicmanager.web.entity.Artist;
import com.musicmanager.web.exception.ResourceNotFoundException;
import com.musicmanager.web.repository.ArtistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ArtistServiceTest
{
    @Autowired
    private ArtistService artistService;
    @Autowired
    private ArtistRepository artistRepository;

    @Test
    void createAndGetArtist()
    {
        Artist artist = new Artist();
        artist.setName("name");
        artist.setCountry("country");
        artist.setGender("gender");
        artist.setInformations("https://www.google.com/");

        Artist createdArtist = artistService.createArtist(artist);

        assertNotNull(createdArtist);
        assertNotNull(createdArtist.getId());
        assertEquals("name", createdArtist.getName());

        Artist foundInDatabase = artistRepository.findById(createdArtist.getId()).orElseThrow(() -> new ResourceNotFoundException("Artist not found!."));

        assertNotNull(foundInDatabase);
        assertEquals("name", foundInDatabase.getName());
    }

    @Test
    void deleteArtist()
    {
        Artist artist = new Artist();
        artist.setName("name");
        artist.setCountry("country");
        artist.setGender("gender");
        artist.setInformations("https://www.google.com/");

        artist = artistRepository.save(artist);

        String artistId = artist.getId();

        artistService.deleteArtist(artistId);

        boolean exists = artistRepository.existsById(artistId);
        assertFalse(exists);
    }
}