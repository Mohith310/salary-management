package com.acme.salary.common;

/** The caller sent something the API cannot act on (maps to HTTP 400). */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
