package com.igt.spincoreengine.exception;

import com.igt.spincoreengine.api.model.response.ErrorResponse;
import com.igt.spincoreengine.utils.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalApiErrorHandlerTest {

    private final GlobalApiErrorHandler errorHandler = new GlobalApiErrorHandler();

    @Test
    void handleNoResourceFoundException_returns404() {
        NoResourceFoundException ex = org.mockito.Mockito.mock(NoResourceFoundException.class);
        org.mockito.Mockito.when(ex.getMessage()).thenReturn("Not found");
        ResponseEntity<ErrorResponse> response = errorHandler.handleNoResourceFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatusCode());
        assertEquals(ErrorMessages.RESOURCE_NOT_FOUND, response.getBody().getMessage());
    }
}
