package com.musicmanager.web.service;

import com.musicmanager.web.entity.User;
import com.musicmanager.web.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit test UserService (mock repository + passwordEncoder) – mã TC_USR_xx */
class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenAnswer(i -> "ENC(" + i.getArgument(0) + ")");
        userService = new UserService();
        ReflectionTestUtils.setField(userService, "userRepository", userRepository);
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
    }

    private User user(String username, String email, String role, String password) {
        User u = new User(username, email, role);
        u.setPassword(password);
        return u;
    }

    // ---------- getUsers ----------
    @Test @DisplayName("TC_USR_01 getUsers - có dữ liệu")
    void getUsers_hasData() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
        assertEquals(2, userService.getUsers().size());
    }

    @Test @DisplayName("TC_USR_02 getUsers - rỗng")
    void getUsers_empty() {
        when(userRepository.findAll()).thenReturn(List.of());
        assertTrue(userService.getUsers().isEmpty());
    }

    // ---------- createUser ----------
    @Test @DisplayName("TC_USR_03 createUser - hợp lệ, mật khẩu được mã hóa")
    void createUser_valid() {
        User u = user("john", "john@x.com", "USER", "secret123");
        userService.createUser(u);
        assertEquals("ENC(secret123)", u.getPassword());
        verify(userRepository).save(u);
    }

    @Test @DisplayName("TC_USR_04 createUser - password null / rỗng → IllegalArgumentException")
    void createUser_noPassword() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user("a", "a@x.com", "USER", null)));
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user("a", "a@x.com", "USER", "  ")));
        verify(userRepository, never()).save(any());
    }

    // ---------- register ----------
    @Test @DisplayName("TC_USR_05 register - hợp lệ → true, role = User, mật khẩu mã hóa")
    void register_valid() {
        User u = user("newuser", "new@x.com", "ADMIN", "password1");
        assertTrue(userService.register(u));
        assertEquals("User", u.getRole());
        assertEquals("ENC(password1)", u.getPassword());
        verify(userRepository).save(u);
    }

    @Test @DisplayName("TC_USR_05b register - thiếu username / email → false")
    void register_missingFields() {
        assertFalse(userService.register(user(null, "a@x.com", null, "password1")));
        assertFalse(userService.register(user(" ", "a@x.com", null, "password1")));
        assertFalse(userService.register(user("abc", null, null, "password1")));
        assertFalse(userService.register(user("abc", "", null, "password1")));
        verify(userRepository, never()).save(any());
    }

    @Test @DisplayName("TC_USR_05c register - mật khẩu null hoặc ngắn hơn 8 ký tự → false")
    void register_weakPassword() {
        assertFalse(userService.register(user("abc", "a@x.com", null, null)));
        assertFalse(userService.register(user("abc", "a@x.com", null, "1234567")));
        verify(userRepository, never()).save(any());
    }

    @Test @DisplayName("TC_USR_05d register - mật khẩu đúng 8 ký tự → true (biên)")
    void register_passwordBoundary() {
        assertTrue(userService.register(user("abc", "a@x.com", null, "12345678")));
    }

    @Test @DisplayName("TC_USR_05e register - trùng username → false")
    void register_duplicateUsername() {
        when(userRepository.existsByUsernameIgnoreCase("dup")).thenReturn(true);
        assertFalse(userService.register(user("dup", "a@x.com", null, "password1")));
        verify(userRepository, never()).save(any());
    }

    @Test @DisplayName("TC_USR_05f register - trùng email → false")
    void register_duplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("dup@x.com")).thenReturn(true);
        assertFalse(userService.register(user("abc", "dup@x.com", null, "password1")));
        verify(userRepository, never()).save(any());
    }

    // ---------- updateUser ----------
    @Test @DisplayName("TC_USR_06 updateUser - hợp lệ, không đổi mật khẩu khi password trống")
    void updateUser_valid() {
        User old = user("old", "old@x.com", "USER", "OLDHASH");
        when(userRepository.findById("1")).thenReturn(Optional.of(old));
        userService.updateUser("1", new User("new", "new@x.com", "ADMIN"));
        assertEquals("new", old.getUsername());
        assertEquals("new@x.com", old.getEmail());
        assertEquals("ADMIN", old.getRole());
        assertEquals("OLDHASH", old.getPassword());
        verify(userRepository).save(old);
    }

    @Test @DisplayName("TC_USR_06b updateUser - có password mới thì mã hóa lại")
    void updateUser_newPassword() {
        User old = user("old", "old@x.com", "USER", "OLDHASH");
        when(userRepository.findById("1")).thenReturn(Optional.of(old));
        userService.updateUser("1", user("old", "old@x.com", "USER", "newpass99"));
        assertEquals("ENC(newpass99)", old.getPassword());
    }

    @Test @DisplayName("TC_USR_07 updateUser - id không tồn tại")
    void updateUser_notFound() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.updateUser("999", new User("n", "n@x.com", "USER")));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test @DisplayName("TC_USR_08 updateUser - trường null ghi đè dữ liệu cũ (ghi nhận hành vi)")
    void updateUser_nullOverwrites() {
        User old = user("old", "old@x.com", "USER", "H");
        when(userRepository.findById("1")).thenReturn(Optional.of(old));
        userService.updateUser("1", new User("old", null, "USER"));
        assertNull(old.getEmail());
    }

    // ---------- deleteUser ----------
    @Test @DisplayName("TC_USR_09 deleteUser")
    void deleteUser() {
        userService.deleteUser("1");
        verify(userRepository).deleteById("1");
    }

    @Test @DisplayName("TC_USR_10 deleteUser - id không tồn tại (hành vi hiện tại: không báo lỗi)")
    void deleteUser_notExist() {
        assertDoesNotThrow(() -> userService.deleteUser("999"));
        verify(userRepository).deleteById("999");
    }

    // ---------- searchUsers ----------
    @Test @DisplayName("TC_USR_11 searchUsers - theo username")
    void searchUsers_byUsername() {
        User u = new User("john", "j@x.com", "USER");
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("john", "john")).thenReturn(List.of(u));
        assertEquals(List.of(u), userService.searchUsers("john"));
    }

    @Test @DisplayName("TC_USR_12 searchUsers - theo email")
    void searchUsers_byEmail() {
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("@gmail", "@gmail"))
                .thenReturn(List.of(new User("x", "x@gmail.com", "USER")));
        assertEquals(1, userService.searchUsers("@gmail").size());
    }

    @Test @DisplayName("TC_USR_13 searchUsers - keyword null")
    void searchUsers_null() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
        assertEquals(2, userService.searchUsers(null).size());
    }

    @Test @DisplayName("TC_USR_14 searchUsers - keyword rỗng/khoảng trắng")
    void searchUsers_blank() {
        when(userRepository.findAll()).thenReturn(List.of(new User()));
        assertEquals(1, userService.searchUsers("").size());
        assertEquals(1, userService.searchUsers("   ").size());
    }

    @Test @DisplayName("TC_USR_15 searchUsers - không có kết quả")
    void searchUsers_none() {
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("zzz", "zzz")).thenReturn(List.of());
        assertTrue(userService.searchUsers("zzz").isEmpty());
    }
}
