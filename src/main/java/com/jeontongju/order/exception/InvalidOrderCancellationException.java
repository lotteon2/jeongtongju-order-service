package com.jeontongju.order.exception;

public class InvalidOrderCancellationException extends RuntimeException{
    public InvalidOrderCancellationException() {
    }

    public InvalidOrderCancellationException(String message) {
        super(message);
    }

    public InvalidOrderCancellationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidOrderCancellationException(Throwable cause) {
        super(cause);
    }

    public InvalidOrderCancellationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
