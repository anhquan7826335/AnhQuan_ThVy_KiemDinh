package com.musicmanager.web.controller;

import com.musicmanager.web.exception.GlobalException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;

/** Dựng MockMvc standalone: không nạp Spring context, không phụ thuộc Security/DB/Thymeleaf. */
final class TestMvc {
    private TestMvc() {}

    static MockMvc build(Object controller, String serviceField, Object service) {
        if (serviceField != null) {
            ReflectionTestUtils.setField(controller, serviceField, service);
        }
        ViewResolver resolver = (name, locale) -> name.startsWith("redirect:")
                ? new RedirectView(name.substring("redirect:".length()), true)
                : (View) (model, request, response) -> { };
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalException())
                .setViewResolvers(resolver)
                .build();
    }
}
