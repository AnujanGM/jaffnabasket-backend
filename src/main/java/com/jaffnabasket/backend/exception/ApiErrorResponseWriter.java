package com.jaffnabasket.backend.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Used by Spring Security entry points/handlers, which run outside the
 * DispatcherServlet and so can't be reached by {@link GlobalExceptionHandler}.
 */
public final class ApiErrorResponseWriter {

    private ApiErrorResponseWriter() {
    }

    public static void write(HttpServletResponse response, ObjectMapper objectMapper,
                              HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = ApiErrorResponse.of(code, message, null, CorrelationIdHolder.current());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
