package com.musicmanager.web.controller;

import com.musicmanager.web.entity.User;
import com.musicmanager.web.exception.GlobalException;
import com.musicmanager.web.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test UserController bằng MockMvc standalone (không nạp Spring context,
 * nên không phụ thuộc cấu hình Security / DB).
 */
class UserControllerTest {

    private UserService userService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);

        // View giả: không render template, chỉ xử lý redirect
        ViewResolver resolver = (name, locale) -> name.startsWith("redirect:")
                ? new RedirectView(name.substring("redirect:".length()), true)
                : (View) (model, request, response) -> { };

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalException())
                .setViewResolvers(resolver)
                .build();
    }

    @Test @DisplayName("GET /users - hiển thị danh sách")
    void list() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(new User("a", "a@x.com", "USER")));
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users", "userRequest"));
    }

    @Test @DisplayName("GET /users/search - có từ khóa")
    void search() throws Exception {
        when(userService.searchUsers("john")).thenReturn(List.of(new User("john", "j@x.com", "USER")));
        mvc.perform(get("/users/search").param("keyword", "john"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users", "userRequest"))
                .andExpect(model().attribute("searchQuery", "john"));
    }

    @Test @DisplayName("GET /users/search - từ khóa rỗng")
    void searchEmpty() throws Exception {
        when(userService.searchUsers("")).thenReturn(List.of(new User(), new User()));
        mvc.perform(get("/users/search").param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("users"));
        verify(userService).searchUsers("");
    }

    @Test @DisplayName("GET /users/search - thiếu tham số (mong 400)")
    void searchMissingParam() throws Exception {
        mvc.perform(get("/users/search")).andExpect(status().isBadRequest());
    }

    @Test @DisplayName("POST /users/create - hợp lệ")
    void create() throws Exception {
        mvc.perform(post("/users/create").param("username", "u").param("email", "u@x.com").param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService).createUser(any(User.class));
    }

    @Test @DisplayName("POST /users/update/{id} - hợp lệ")
    void update() throws Exception {
        mvc.perform(post("/users/update/1").param("username", "u"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService).updateUser(eq("1"), any(User.class));
    }

    @Test @DisplayName("POST /users/update/{id} - id không tồn tại → 500")
    void updateNotFound() throws Exception {
        doThrow(new RuntimeException("User not found")).when(userService).updateUser(eq("999"), any());
        mvc.perform(post("/users/update/999").param("username", "u"))
                .andExpect(status().isInternalServerError());
    }

    @Test @DisplayName("GET /users/delete/{id} - hợp lệ")
    void delete() throws Exception {
        mvc.perform(get("/users/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService).deleteUser("1");
    }
}
