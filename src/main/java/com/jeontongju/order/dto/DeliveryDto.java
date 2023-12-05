package com.jeontongju.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class DeliveryDto {
    @NotNull(message = "운송장 번호는 널일 수 없습니다")
    private String deliveryCode;
}