package com.jeontongju.order.exception;

public class DeliveryIdNotFoundException extends RuntimeException{
    public DeliveryIdNotFoundException() {
    }

    public DeliveryIdNotFoundException(String message) {
        super(message);
    }

    public DeliveryIdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeliveryIdNotFoundException(Throwable cause) {
        super(cause);
    }

    public DeliveryIdNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
