package com.musicmanager.web.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.*;

/** Test trực tiếp GlobalException (không qua view engine). */
class GlobalExceptionTest {

    private final GlobalException handler = new GlobalException();

    @Test @DisplayName("ResourceNotFoundException → view error/404 + errorMessage")
    void notFound() {
        ModelAndView mv = handler.handleResourceNotFoundException(new ResourceNotFoundException("x"));
        assertEquals("error/404", mv.getViewName());
        assertEquals("x", mv.getModel().get("errorMessage"));
    }

    @Test @DisplayName("BadRequestException → view error/500 + tiền tố 'Yêu cầu không hợp lệ'")
    void badRequest() {
        ModelAndView mv = handler.handleBadRequestException(new BadRequestException("x"));
        assertEquals("error/500", mv.getViewName());
        assertEquals("Yêu cầu không hợp lệ: x", mv.getModel().get("errorMessage"));
    }

    @Test @DisplayName("Exception bất kỳ → view error/500 + tiền tố 'Đã có lỗi không mong muốn xảy ra'")
    void generic() {
        ModelAndView mv = handler.handleGlobalException(new RuntimeException("x"));
        assertEquals("error/500", mv.getViewName());
        assertEquals("Đã có lỗi không mong muốn xảy ra: x", mv.getModel().get("errorMessage"));
    }

    @Test @DisplayName("Exception class mang đúng message và @ResponseStatus")
    void exceptionClasses() {
        assertEquals("msg", new ResourceNotFoundException("msg").getMessage());
        assertEquals("msg", new BadRequestException("msg").getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ResourceNotFoundException.class
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
        assertEquals(HttpStatus.BAD_REQUEST, BadRequestException.class
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }
}
