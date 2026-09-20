package com.thnky.ai;

/** Wraps any failure talking to Nebius — timeout, non-2xx, malformed response. */
public class NebiusRequestException extends RuntimeException {

    public NebiusRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public NebiusRequestException(String message) {
        super(message);
    }
}
