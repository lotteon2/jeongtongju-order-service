package com.jeontongju.order.exception;

public class DuplicateDeliveryCodeException extends RuntimeException{
    public DuplicateDeliveryCodeException() {
    }

    public DuplicateDeliveryCodeException(String message) {
        super(message);
    }

    public DuplicateDeliveryCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateDeliveryCodeException(Throwable cause) {
        super(cause);
    }

    public DuplicateDeliveryCodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
