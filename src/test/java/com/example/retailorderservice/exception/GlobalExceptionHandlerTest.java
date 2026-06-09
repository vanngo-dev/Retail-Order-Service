package com.example.retailorderservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.retailorderservice.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedServerErrorsReturnStandardErrorResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/boom");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(
                new RuntimeException("Database unavailable"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().path()).isEqualTo("/boom");
        assertThat(response.getBody().validationErrors()).isNull();
    }
}
