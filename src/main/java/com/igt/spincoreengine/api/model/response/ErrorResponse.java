package com.igt.spincoreengine.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatusCode;

import java.io.Serializable;

//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse  implements Serializable {
    private HttpStatusCode statusCode;
    private String message;

    public ErrorResponse(HttpStatusCode statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
