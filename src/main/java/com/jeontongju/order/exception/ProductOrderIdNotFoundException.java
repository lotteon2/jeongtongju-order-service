package com.jeontongju.order.exception;

public class ProductOrderIdNotFoundException extends RuntimeException{
    public ProductOrderIdNotFoundException() {
    }

    public ProductOrderIdNotFoundException(String message) {
        super(message);
    }

    public ProductOrderIdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductOrderIdNotFoundException(Throwable cause) {
        super(cause);
    }

    public ProductOrderIdNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
