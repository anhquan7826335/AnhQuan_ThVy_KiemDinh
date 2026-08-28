package com.musicmanager.web.service;

import com.musicmanager.web.entity.Artist;
import com.musicmanager.web.exception.BadRequestException;
import com.musicmanager.web.exception.ResourceNotFoundException;
import com.musicmanager.web.repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistService
{
    @Autowired
    private ArtistRepository artistRepository;

    // LẤY RA DANH SÁCH NGHỆ SĨ
    public List<Artist> getArtists()
    {
        return artistRepository.findAll();
    }

    // LẤY THÔNG TIN CỦA NGHỆ SĨ THEO ID
    public Artist getArtist(String id)
    {
        return artistRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Artist with id: " + id + " not found!."));
    }

    // TÌM NGHỆ SĨ THEO TÊN (ĐẠI KHÁI)
    public List<Artist> searchArtistsByName(String name)
    {
        return artistRepository.findByNameContainingIgnoreCase(name);
    }

    // TÌM NGHỆ SĨ THEO TÊN (CHÍNH XÁC)
    public Artist searchArtistByName(String name)
    {
        return artistRepository.findByNameIgnoreCase(name);
    }

    // TẠO NGHỆ SĨ MỚI
    public Artist createArtist(Artist request)
    {
        if (request.getName() == null || request.getName().isBlank())
        {
            throw new BadRequestException("Artist name cannot be blank!.");
        }

        if (searchArtistByName(request.getName()) != null)
        {
            throw new BadRequestException("Artist name existed!.");
        }

        artistRepository.save(request);

        return request;
    }

    // SỬA THÔNG TIN NGHỆ SĨ
    public void updateArtist(String id, Artist request)
    {
        Artist artist = getArtist(id);

        Artist existingArtist = searchArtistByName(request.getName());
        if (existingArtist != null && !existingArtist.getId().equals(id))
        {
            throw new BadRequestException("Artist name existed!.");
        }

        if (request.getName() != null) artist.setName(request.getName());
        if (request.getCountry() != null) artist.setCountry(request.getCountry());
        if (request.getGender() != null) artist.setGender(request.getGender());
        if (request.getInformations() != null) artist.setInformations(request.getInformations());

        artistRepository.save(artist);
    }

    // XÓA NGHỆ SĨ
    public void deleteArtist(String id)
    {
        if (!artistRepository.existsById(id))
        {
            throw new ResourceNotFoundException("Cannot find Artist with id: " + id);
        }

        artistRepository.deleteById(id);
    }

    // SỐ LƯỢNG NGHỆ SĨ CÓ TRONG DATABASE
    public long numberOfArtists()
    {
        return artistRepository.count();
    }
}
