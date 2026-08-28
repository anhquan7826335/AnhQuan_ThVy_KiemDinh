package com.musicmanager.web.service;

import com.musicmanager.web.entity.Genre;
import com.musicmanager.web.exception.BadRequestException;
import com.musicmanager.web.exception.ResourceNotFoundException;
import com.musicmanager.web.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService
{
    @Autowired
    private GenreRepository genreRepository;

    // LẤY RA DANH SÁCH THỂ LOẠI NHẠC
    public List<Genre> getGenres()
    {
        return genreRepository.findAll();
    }

    // LẤY RA THÔNG TIN THỂ LOẠI NHẠC THEO ID
    public Genre getGenre(String id)
    {

        return genreRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Genre with id: " + id + " not found!."));
    }

    // TÌM THỂ LOẠI NHẠC THEO TÊN (ĐẠI KHÁI)
    public List<Genre> searchGenresByName(String name)
    {

        return genreRepository.findByNameContainingIgnoreCase(name);
    }

    // TÌM THỂ LOẠI NHẠC THEO TÊN (CHÍNH XÁC)
    public Genre searchGenreByName(String name)
    {
        return genreRepository.findByNameIgnoreCase(name);
    }

    // TẠO THỂ LOẠI NHẠC MỚI
    // TODO: THÊM EXCEPTION
    public Genre createGenre(Genre request)
    {
        if (request.getName() == null || request.getName().isBlank())
        {
            throw new BadRequestException("Genre name cannot be blank!.");
        }
        if (searchGenreByName(request.getName()) != null)
        {
            throw new BadRequestException("Genre existed!/");
        }

        genreRepository.save(request);
        return request;
    }

    // SỬA THÔNG TIN THỂ LOẠI NHẠC
    public void updateGenre(String id, Genre request)
    {
        Genre genre = getGenre(id);
        Genre existingGenre = searchGenreByName(request.getName());

        if (existingGenre != null && !existingGenre.getId().equals(id))
        {
            throw new BadRequestException("Genre name existed!.");
        }


        if (request.getName() != null) genre.setName(request.getName());
        if (request.getInformations() != null) genre.setInformations(request.getInformations());

        genreRepository.save(genre);
    }

    // XÓA THỂ LOẠI NHẠC
    public void deleteGenre(String id)
    {
        if (!genreRepository.existsById(id))
        {
            throw new ResourceNotFoundException("Cannot find Genre with id: " + id);
        }

        genreRepository.deleteById(id);
    }

    // SỐ LƯỢNG THỂ LOẠI NHẠC CÓ TRONG DATABASE
    public long numberOfGenres()
    {
        return genreRepository.count();
    }
}
