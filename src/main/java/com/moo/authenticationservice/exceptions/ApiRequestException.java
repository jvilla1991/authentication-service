package com.moo.authenticationservice.exceptions;

import java.util.function.Supplier;

public class ApiRequestException extends RuntimeException {

    public ApiRequestException(String message) {
        super(message);
    }

}
