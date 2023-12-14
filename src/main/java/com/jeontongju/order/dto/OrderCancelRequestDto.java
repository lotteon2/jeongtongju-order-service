package com.jeontongju.order.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class OrderCancelRequestDto {
    @NotNull(message = "취소 하고 싶은 주문을 선택해주세요")
    private String ordersId;
}