package com.igt.spincoreengine.exception;

import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {
    private String shortMessage;
    private HttpStatus httpStatus;

    public ServiceException(String shortMessage, HttpStatus httpStatus) {
        this.shortMessage = shortMessage;
        this.httpStatus = httpStatus;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
