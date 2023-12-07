package com.jeontongju.order.exception;

public class FeignServerNotAvailableException extends RuntimeException{
    public FeignServerNotAvailableException() {
    }

    public FeignServerNotAvailableException(String message) {
        super(message);
    }

    public FeignServerNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeignServerNotAvailableException(Throwable cause) {
        super(cause);
    }

    public FeignServerNotAvailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
