package com.example.userservice.exceptions;

public class PasswordMisMatchException extends RuntimeException{
    public PasswordMisMatchException(String message) {
        super(message);
    }
}
