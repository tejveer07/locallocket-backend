package com.locallocket.backend.exception;

public class VendorConflictException extends RuntimeException {
    public VendorConflictException(String message) {
        super(message);
    }
}
