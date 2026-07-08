package com.igt.spincoreengine.exception;

import com.igt.spincoreengine.api.model.response.ErrorResponse;
import com.igt.spincoreengine.utils.ErrorMessages;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalApiErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalApiErrorHandler.class);

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException ex) {
        logger.error("ServiceException occurred: {}", ex.getShortMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getHttpStatus().value(), ex.getShortMessage()), ex.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("Validation failed: {}", message);
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        logger.warn("Constraint violation: {}", message);
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        logger.error("NoHandlerFoundException occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ErrorMessages.RESOURCE_NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("Exception occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorMessages.GENERAL_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

