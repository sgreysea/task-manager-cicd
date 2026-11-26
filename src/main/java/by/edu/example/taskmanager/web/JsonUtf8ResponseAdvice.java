package by.edu.example.taskmanager.web;

import java.nio.charset.StandardCharsets;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Provides UTF-8 charset information in JSON HTTP responses.
 *
 * This is especially useful for legacy HTTP clients,
 * including Windows PowerShell 5.1.
 *
 * The actual JSON response body is still encoded as UTF-8.
 */
@RestControllerAdvice
public class JsonUtf8ResponseAdvice
        implements ResponseBodyAdvice<Object> {

    /**
     * Determines whether this advice should process
     * the selected HTTP message converter.
     *
     * We apply the advice only to Jackson JSON responses.
     */
    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {

        return MappingJackson2HttpMessageConverter.class
                .isAssignableFrom(converterType);
    }

    /**
     * Adds an explicit UTF-8 charset to application/json responses.
     *
     * This improves compatibility with older clients
     * that do not correctly assume UTF-8 when the charset
     * parameter is absent.
     */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // Process only application/json responses.
        if (selectedContentType != null
                && MediaType.APPLICATION_JSON
                .isCompatibleWith(selectedContentType)) {

            // Set the response Content-Type explicitly.
            response.getHeaders().setContentType(
                    new MediaType(
                            "application",
                            "json",
                            StandardCharsets.UTF_8
                    )
            );
        }

        // Do not modify the response object or its data.
        return body;
    }
}
