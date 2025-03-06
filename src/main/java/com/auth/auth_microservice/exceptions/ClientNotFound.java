package com.auth.auth_microservice.exceptions;

public class ClientNotFound extends RuntimeException {
    public ClientNotFound(String message) {
        super(message);
    }
}
