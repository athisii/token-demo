package com.cdac.tokendemo.exception;

public class NoReaderOrCardException extends RuntimeException {
    public NoReaderOrCardException(String message) {
        super(message);
    }
}
