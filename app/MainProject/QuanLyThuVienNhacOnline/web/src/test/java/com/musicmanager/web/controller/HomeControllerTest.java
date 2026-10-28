package com.musicmanager.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HomeControllerTest {
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = TestMvc.build(new HomeController(), null, null);
    }

    @Test @DisplayName("GET / - trang chủ")
    void home() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
    }
}
