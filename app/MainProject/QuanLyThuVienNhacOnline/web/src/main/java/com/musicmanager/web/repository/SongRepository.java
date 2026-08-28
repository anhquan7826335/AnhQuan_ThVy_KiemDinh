package com.musicmanager.web.repository;

import com.musicmanager.web.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, String> {
    // Tìm bài hát theo tiêu đề
    List<Song> findByTitleContainingIgnoreCase(String title);

    // Tìm bài hát theo nghệ sĩ
    List<Song> findByArtistContainingIgnoreCase(String artist);

    // Tìm bài hát theo thể loại
    List<Song> findByGenreContainingIgnoreCase(String genre);
}
