package com.bank.exception;

// 400 - Bad Request
public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message);
    }
}