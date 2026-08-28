package com.musicmanager.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalException
{
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFoundException(ResourceNotFoundException ex)
    {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("404");

        modelAndView.addObject("errorMessage", ex.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadRequestException(BadRequestException ex)
    {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("500");

        modelAndView.addObject("errorMessage", "Yêu cầu không hợp lệ: " + ex.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGlobalException(Exception ex)
    {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("500");

        modelAndView.addObject("errorMessage", "Đã có lỗi không mong muốn xảy ra: " + ex.getMessage());

        return modelAndView;
    }
}