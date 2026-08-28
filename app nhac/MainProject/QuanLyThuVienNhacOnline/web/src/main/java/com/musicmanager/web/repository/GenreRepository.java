package com.musicmanager.web.repository;

import com.musicmanager.web.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<Genre, String>
{
    List<Genre> findByNameContainingIgnoreCase(String name);

    Genre findByNameIgnoreCase(String name);
}
