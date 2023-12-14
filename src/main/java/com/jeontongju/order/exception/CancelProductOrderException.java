package com.jeontongju.order.exception;

public class CancelProductOrderException extends RuntimeException{
    public CancelProductOrderException() {
    }

    public CancelProductOrderException(String message) {
        super(message);
    }

    public CancelProductOrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelProductOrderException(Throwable cause) {
        super(cause);
    }

    public CancelProductOrderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
