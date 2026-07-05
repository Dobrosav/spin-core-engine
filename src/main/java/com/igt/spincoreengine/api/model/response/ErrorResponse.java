package com.igt.spincoreengine.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "Response returned when an error occurs")
public class ErrorResponse implements Serializable {
    @Schema(description = "HTTP status code", example = "404")
    private int statusCode;
    @Schema(description = "Human-readable error message", example = "Player not found.")
    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
