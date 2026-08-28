package com.musicmanager.web.controller;

import com.musicmanager.web.entity.Song;
import com.musicmanager.web.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/songs")
public class SongController {

    @Autowired
    private SongService songService;

    @GetMapping
    public String listSongs(Model model) {
        List<Song> songs = songService.getAllSongs();
        model.addAttribute("songs", songs);
        model.addAttribute("numberOfSongs", songs.size());
        model.addAttribute("songRequest", new Song()); // 
        return "songs"; // tên file HTML (songs.html)
    }

    // Tìm kiếm bài hát theo ten bai hat
    @GetMapping("/search")
    public String searchSongs(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Song> songs = songService.searchSongs(keyword);

        model.addAttribute("songs", songs);
        model.addAttribute("searchQuery", keyword);
        model.addAttribute("numberOfSongs", songs.size());
        model.addAttribute("songRequest", new Song());
        return "songs";
    }

    // Tạo mới bài hát
    @PostMapping("/create")
    public String createSong(@ModelAttribute("songRequest") Song request) {
        songService.createSong(request);
        return "redirect:/songs";
    }

    // Cập nhật bài hát
    @PostMapping("/update/{id}")
    public String updateSong(@PathVariable String id, @ModelAttribute("songRequest") Song request) {
        songService.updateSong(id, request);
        return "redirect:/songs";
    }

    // Xóa bài hát
    @PostMapping("/delete/{id}")
    public String deleteSong(@PathVariable String id) {
        songService.deleteSong(id);
        return "redirect:/songs";
    }


}
