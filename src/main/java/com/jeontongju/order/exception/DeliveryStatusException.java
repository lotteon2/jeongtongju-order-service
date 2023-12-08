package com.jeontongju.order.exception;

public class DeliveryStatusException extends RuntimeException{
    public DeliveryStatusException() {
    }

    public DeliveryStatusException(String message) {
        super(message);
    }

    public DeliveryStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeliveryStatusException(Throwable cause) {
        super(cause);
    }

    public DeliveryStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
