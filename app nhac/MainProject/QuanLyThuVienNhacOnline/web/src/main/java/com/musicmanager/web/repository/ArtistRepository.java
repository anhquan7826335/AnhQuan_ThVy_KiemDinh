package com.musicmanager.web.repository;

import com.musicmanager.web.entity.Artist;
import com.musicmanager.web.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, String>
{
    List<Artist> findByNameContainingIgnoreCase(String name);

    Artist findByNameIgnoreCase(String name);
}
