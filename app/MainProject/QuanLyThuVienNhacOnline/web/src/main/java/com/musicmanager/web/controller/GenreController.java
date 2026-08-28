package com.musicmanager.web.controller;

import com.musicmanager.web.entity.Genre;
import com.musicmanager.web.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/genres")
public class GenreController
{
    @Autowired
    private GenreService genreService;

    @GetMapping
    public String getGenres(Model model)
    {
        model.addAttribute("genres", genreService.getGenres());
        model.addAttribute("genreRequest", new Genre());
        model.addAttribute("numberOfGenres", genreService.numberOfGenres());

        return "genres";
    }

    @GetMapping("/search")
    public String searchGenres(@RequestParam("name") String name, Model model)
    {
        if (name == null || name.trim().isEmpty())
        {
            return "redirect:/genres";
        }

        List<Genre> genres = genreService.searchGenresByName(name);

        model.addAttribute("genres", genres);
        model.addAttribute("searchQuery", name);
        model.addAttribute("genreRequest", new Genre());
        model.addAttribute("numberOfGenres", genres.size());
        return "genres";
    }

    @PostMapping("/create")
    public String createGenre(@ModelAttribute("genreRequest") Genre request)
    {
        genreService.createGenre(request);
        return "redirect:/genres";
    }

    @PostMapping("/update/{id}")
    public String updateGenre(@PathVariable String id, @ModelAttribute("genreRequest") Genre request)
    {
        genreService.updateGenre(id, request);
        return "redirect:/genres";
    }

    @PostMapping("/delete/{id}")
    public String deleteGenre(@PathVariable String id)
    {
        genreService.deleteGenre(id);
        return "redirect:/genres";
    }
}
