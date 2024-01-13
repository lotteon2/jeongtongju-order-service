package com.jeontongju.order.exception;

public class AuctionStatusException extends RuntimeException{
    public AuctionStatusException() {
    }

    public AuctionStatusException(String message) {
        super(message);
    }

    public AuctionStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuctionStatusException(Throwable cause) {
        super(cause);
    }

    public AuctionStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
