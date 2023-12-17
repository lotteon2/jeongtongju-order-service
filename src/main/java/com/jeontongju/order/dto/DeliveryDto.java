package com.jeontongju.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DeliveryDto {
    @NotNull(message = "운송장 번호는 필수 입력 값 입니다.")
    private String deliveryCode;
}
