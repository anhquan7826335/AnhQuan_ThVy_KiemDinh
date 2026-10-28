package com.musicmanager.web.service;

import com.musicmanager.web.entity.Artist;
import com.musicmanager.web.exception.BadRequestException;
import com.musicmanager.web.exception.ResourceNotFoundException;
import com.musicmanager.web.repository.ArtistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit test ArtistService (mock repository) – mã TC_ART_xx */
@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock private ArtistRepository artistRepository;
    @InjectMocks private ArtistService artistService;

    private Artist artist(String id, String name) {
        Artist a = new Artist();
        a.setId(id);
        a.setName(name);
        a.setCountry("VN");
        a.setGender("Nam");
        a.setInformations("info");
        return a;
    }

    @Test @DisplayName("TC_ART_01 getArtists - có dữ liệu")
    void getArtists_hasData() {
        when(artistRepository.findAll()).thenReturn(List.of(artist("1", "A"), artist("2", "B")));
        assertEquals(2, artistService.getArtists().size());
    }

    @Test @DisplayName("TC_ART_02 getArtists - rỗng")
    void getArtists_empty() {
        when(artistRepository.findAll()).thenReturn(List.of());
        assertTrue(artistService.getArtists().isEmpty());
    }

    @Test @DisplayName("TC_ART_03 getArtist - id tồn tại")
    void getArtist_found() {
        when(artistRepository.findById("1")).thenReturn(Optional.of(artist("1", "A")));
        assertEquals("A", artistService.getArtist("1").getName());
    }

    @Test @DisplayName("TC_ART_04 getArtist - id không tồn tại")
    void getArtist_notFound() {
        when(artistRepository.findById("999")).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> artistService.getArtist("999"));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test @DisplayName("TC_ART_05 searchArtistsByName - có kết quả")
    void searchArtistsByName_found() {
        when(artistRepository.findByNameContainingIgnoreCase("abc")).thenReturn(List.of(artist("1", "ABC Band")));
        List<Artist> result = artistService.searchArtistsByName("abc");
        assertEquals(1, result.size());
        assertEquals("ABC Band", result.get(0).getName());
    }

    @Test @DisplayName("TC_ART_06 searchArtistsByName - không có kết quả")
    void searchArtistsByName_none() {
        when(artistRepository.findByNameContainingIgnoreCase("zzz")).thenReturn(List.of());
        assertTrue(artistService.searchArtistsByName("zzz").isEmpty());
    }

    @Test @DisplayName("TC_ART_07 searchArtistByName - chính xác")
    void searchArtistByName_found() {
        when(artistRepository.findByNameIgnoreCase("abc")).thenReturn(artist("1", "ABC"));
        assertEquals("ABC", artistService.searchArtistByName("abc").getName());
    }

    @Test @DisplayName("TC_ART_08 searchArtistByName - không tồn tại trả null")
    void searchArtistByName_null() {
        when(artistRepository.findByNameIgnoreCase("zzz")).thenReturn(null);
        assertNull(artistService.searchArtistByName("zzz"));
    }

    @Test @DisplayName("TC_ART_09 createArtist - hợp lệ")
    void createArtist_valid() {
        Artist req = artist(null, "New");
        when(artistRepository.findByNameIgnoreCase("New")).thenReturn(null);
        Artist result = artistService.createArtist(req);
        assertEquals("New", result.getName());
        verify(artistRepository).save(req);
    }

    @Test @DisplayName("TC_ART_10 createArtist - name null")
    void createArtist_nameNull() {
        assertThrows(BadRequestException.class, () -> artistService.createArtist(artist(null, null)));
        verify(artistRepository, never()).save(any());
    }

    @Test @DisplayName("TC_ART_11 createArtist - name rỗng")
    void createArtist_nameEmpty() {
        assertThrows(BadRequestException.class, () -> artistService.createArtist(artist(null, "")));
        verify(artistRepository, never()).save(any());
    }

    @Test @DisplayName("TC_ART_12 createArtist - name toàn khoảng trắng")
    void createArtist_nameBlank() {
        assertThrows(BadRequestException.class, () -> artistService.createArtist(artist(null, "   ")));
        verify(artistRepository, never()).save(any());
    }

    @Test @DisplayName("TC_ART_13 createArtist - tên đã tồn tại")
    void createArtist_duplicate() {
        when(artistRepository.findByNameIgnoreCase("ABC")).thenReturn(artist("1", "ABC"));
        assertThrows(BadRequestException.class, () -> artistService.createArtist(artist(null, "ABC")));
        verify(artistRepository, never()).save(any());
    }

    @Test @DisplayName("TC_ART_14 createArtist - trùng tên khác hoa/thường")
    void createArtist_duplicateDifferentCase() {
        // repo dùng IgnoreCase nên "abc" trả về bản ghi "ABC"
        when(artistRepository.findByNameIgnoreCase("abc")).thenReturn(artist("1", "ABC"));
        assertThrows(BadRequestException.class, () -> artistService.createArtist(artist(null, "abc")));
    }

    @Test @DisplayName("TC_ART_15 updateArtist - cập nhật đầy đủ")
    void updateArtist_full() {
        Artist old = artist("1", "Old");
        when(artistRepository.findById("1")).thenReturn(Optional.of(old));
        when(artistRepository.findByNameIgnoreCase("New")).thenReturn(null);

        Artist req = new Artist();
        req.setName("New"); req.setCountry("US"); req.setGender("Nữ"); req.setInformations("new info");
        artistService.updateArtist("1", req);

        assertEquals("New", old.getName());
        assertEquals("US", old.getCountry());
        assertEquals("Nữ", old.getGender());
        assertEquals("new info", old.getInformations());
        verify(artistRepository).save(old);
    }

    @Test @DisplayName("TC_ART_16 updateArtist - chỉ cập nhật trường khác null")
    void updateArtist_partial() {
        Artist old = artist("1", "Old");
        when(artistRepository.findById("1")).thenReturn(Optional.of(old));
        Artist req = new Artist();
        req.setName("Old");               // trùng chính nó
        req.setInformations("only info");
        when(artistRepository.findByNameIgnoreCase("Old")).thenReturn(old);

        artistService.updateArtist("1", req);

        assertEquals("only info", old.getInformations());
        assertEquals("VN", old.getCountry());
        assertEquals("Nam", old.getGender());
    }

    @Test @DisplayName("TC_ART_17 updateArtist - giữ nguyên tên của chính nó")
    void updateArtist_sameNameOfItself() {
        Artist old = artist("1", "Same");
        when(artistRepository.findById("1")).thenReturn(Optional.of(old));
        when(artistRepository.findByNameIgnoreCase("Same")).thenReturn(old);
        Artist req = artist(null, "Same");
        assertDoesNotThrow(() -> artistService.updateArtist("1", req));
        verify(artistRepository).save(old);
    }

    @Test @DisplayName("TC_ART_18 updateArtist - id không tồn tại")
    void updateArtist_notFound() {
        when(artistRepository.findById("999")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> artistService.updateArtist("999", artist(null, "X")));
        verify(artistRepository, never()).save(any());
    }

    @Test @DisplayName("TC_ART_19 updateArtist - đổi sang tên của bản ghi khác")
    void updateArtist_duplicateOfOther() {
        when(artistRepository.findById("1")).thenReturn(Optional.of(artist("1", "A")));
        when(artistRepository.findByNameIgnoreCase("B")).thenReturn(artist("2", "B"));
        assertThrows(BadRequestException.class, () -> artistService.updateArtist("1", artist(null, "B")));
        verify(artistRepository, never()).save(any());
    }

    @Test @DisplayName("TC_ART_20 updateArtist - name null không ném NPE, giữ tên cũ")
    void updateArtist_nameNull() {
        Artist old = artist("1", "Old");
        when(artistRepository.findById("1")).thenReturn(Optional.of(old));
        Artist req = new Artist();
        req.setCountry("US");
        assertDoesNotThrow(() -> artistService.updateArtist("1", req));
        assertEquals("Old", old.getName());
        assertEquals("US", old.getCountry());
    }

    @Test @DisplayName("TC_ART_21 deleteArtist - tồn tại")
    void deleteArtist_found() {
        when(artistRepository.existsById("1")).thenReturn(true);
        artistService.deleteArtist("1");
        verify(artistRepository, times(1)).deleteById("1");
    }

    @Test @DisplayName("TC_ART_22 deleteArtist - không tồn tại")
    void deleteArtist_notFound() {
        when(artistRepository.existsById("999")).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> artistService.deleteArtist("999"));
        verify(artistRepository, never()).deleteById(any());
    }

    @Test @DisplayName("TC_ART_23/24 numberOfArtists")
    void numberOfArtists() {
        when(artistRepository.count()).thenReturn(5L, 0L);
        assertEquals(5L, artistService.numberOfArtists());
        assertEquals(0L, artistService.numberOfArtists());
    }
}
