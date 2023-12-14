package com.jeontongju.order.exception;

public class OrderIdNotFoundException extends RuntimeException{
    public OrderIdNotFoundException() {
    }

    public OrderIdNotFoundException(String message) {
        super(message);
    }

    public OrderIdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderIdNotFoundException(Throwable cause) {
        super(cause);
    }

    public OrderIdNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
