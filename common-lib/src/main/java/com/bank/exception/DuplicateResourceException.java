package com.bank.exception;

// 409 - Conflict (Duplicate)
public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException( String field, String  value) {
        super(String.format("User already exists with email :- %s or with mobile number:- %s", field, value));
    }


}
