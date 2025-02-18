package com.example.Shares.auth.exception;


public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String str) {
        super(str);
    }
}