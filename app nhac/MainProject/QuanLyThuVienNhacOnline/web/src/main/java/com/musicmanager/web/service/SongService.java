package com.musicmanager.web.service;

import com.musicmanager.web.entity.Song;

import com.musicmanager.web.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongService {

    @Autowired SongRepository songRepository;

    // CREATE
    public Song createSong(Song song) {
        if (song.getTitle() == null || song.getTitle().isBlank()||
            song.getArtist() == null || song.getArtist().isBlank()||
            song.getGenre() == null || song.getGenre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "không được để trống");
        }
        return songRepository.save(song);
    }

    // READ - lấy tất cả
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    // READ - lấy 1 bài hát theo id
    public Song getSong(String id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Không tìm thấy bài hát với id: " + id));
    }

    // UPDATE
    public void updateSong(String id, Song request) {
        Song song = getSong(id);
        if (request.getTitle() != null) song.setTitle(request.getTitle());
        if (request.getArtist() != null) song.setArtist(request.getArtist());
        if (request.getGenre() != null) song.setGenre(request.getGenre());
        if (request.getReleaseYear() != null) song.setReleaseYear(request.getReleaseYear());
        if (request.getAudioFilePath() != null) song.setAudioFilePath(request.getAudioFilePath());
        if (request.getDuration() != null) song.setDuration(request.getDuration());

        songRepository.save(song);
    }

    // DELETE
    public void deleteSong(String id) {
        if (!songRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài hát với id: " + id);
        }
        songRepository.deleteById(id);
    }

    // SEARCH - tìm theo tiêu chí chi tiết
    public List<Song> searchSongs(String keyword) {
    if (keyword == null || keyword.isBlank()) {
        return songRepository.findAll();
    }

    String lowerKey = keyword.toLowerCase();

    return songRepository.findAll().stream()
            .filter(s ->
                    (s.getTitle() != null && s.getTitle().toLowerCase().contains(lowerKey)) ||
                    (s.getArtist() != null && s.getArtist().toLowerCase().contains(lowerKey)) ||
                    (s.getGenre() != null && s.getGenre().toLowerCase().contains(lowerKey))
            )
            .collect(Collectors.toList());
}

    

    // COUNT - đếm tổng số bài hát
    public long countSongs() {
        return songRepository.count();
    }
}
