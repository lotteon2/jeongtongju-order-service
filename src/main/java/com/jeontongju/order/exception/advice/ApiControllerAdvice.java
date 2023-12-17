package com.jeontongju.order.exception.advice;

import com.jeontongju.order.exception.DeliveryIdNotFoundException;
import com.jeontongju.order.exception.DuplicateDeliveryCodeException;
import com.jeontongju.order.exception.InvalidPermissionException;
import com.jeontongju.order.exception.ProductOrderIdNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiControllerAdvice {
    @ExceptionHandler(DeliveryIdNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDeliveryIdNotFoundException(DeliveryIdNotFoundException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
        .build();
    }

    @ExceptionHandler(DuplicateDeliveryCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDuplicateDeliveryCodeException(DuplicateDeliveryCodeException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
        .build();
    }

    @ExceptionHandler(ProductOrderIdNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleProductOrderIdNotFoundException(ProductOrderIdNotFoundException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
        .build();
    }

    @ExceptionHandler(InvalidPermissionException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidPermissionException(InvalidPermissionException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
        .build();
    }

}