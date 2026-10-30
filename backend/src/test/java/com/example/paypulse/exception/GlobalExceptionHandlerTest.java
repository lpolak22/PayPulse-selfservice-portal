package com.example.paypulse.exception;

import com.example.paypulse.dto.common.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundExceptionReturns404WithCode() {
        ResponseEntity<ErrorResponse> response =
                handler.handleApiException(new ResourceNotFoundException("Merchant", 42));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).contains("Merchant").contains("42");
        assertThat(response.getBody().fieldErrors()).isEmpty();
    }

    @Test
    void conflictExceptionReturns409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleApiException(new ConflictException("Email already exists."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("CONFLICT");
    }

    @Test
    void lastAdminExceptionReturns409WithCodeLastAdmin() {
        ResponseEntity<ErrorResponse> response = handler.handleApiException(new LastAdminException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("LAST_ADMIN");
        assertThat(response.getBody().message()).contains("last active admin");
    }

    @Test
    void validationErrorReturns400WithFieldErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new org.springframework.validation.FieldError(
                "request", "email", "must be a well-formed email address"));
        MethodParameter parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class), 0);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(new MethodArgumentNotValidException(parameter, bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().fieldErrors())
                .containsExactly(new ErrorResponse.FieldError("email", "must be a well-formed email address"));
    }

    @Test
    void accessDeniedReturns403() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo("FORBIDDEN");
    }

    @Test
    void unsupportedMethodReturns405AndNamesTheAllowedMethods() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMethodNotAllowed(new HttpRequestMethodNotSupportedException("GET", List.of("POST")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().code()).isEqualTo("METHOD_NOT_ALLOWED");
        assertThat(response.getHeaders().getAllow()).containsExactly(HttpMethod.POST);
    }

    @Test
    void unsupportedMediaTypeReturns415AndNamesTheAcceptedTypes() {
        ResponseEntity<ErrorResponse> response = handler.handleUnsupportedMediaType(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON),
                        HttpMethod.POST));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody().code()).isEqualTo("UNSUPPORTED_MEDIA_TYPE");
        assertThat(response.getHeaders().getAccept()).containsExactly(MediaType.APPLICATION_JSON);
    }

    @Test
    void notAcceptableReturns406() {
        ResponseEntity<ErrorResponse> response = handler.handleNotAcceptable(
                new HttpMediaTypeNotAcceptableException(List.of(MediaType.APPLICATION_JSON)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
        assertThat(response.getBody().code()).isEqualTo("NOT_ACCEPTABLE");
    }

    @Test
    void everyErrorIsMarkedAsJson() {
        assertThat(handler.handleAccessDenied(new AccessDeniedException("denied")).getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(handler.handleMethodNotAllowed(new HttpRequestMethodNotSupportedException("GET", List.of("POST")))
                .getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void unexpectedExceptionReturns500WithoutLeakingDetails() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new IllegalStateException("db password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).doesNotContain("db password");
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }
}
