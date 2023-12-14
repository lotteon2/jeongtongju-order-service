package com.jeontongju.order.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class ProductOrderCancelRequestDto {
    @NotNull(message = "취소 하고 싶은 상품을 선택해주세요")
    private Long productOrderId;
}
