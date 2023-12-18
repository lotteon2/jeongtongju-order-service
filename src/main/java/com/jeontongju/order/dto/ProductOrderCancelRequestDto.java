package com.jeontongju.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOrderCancelRequestDto {
    @NotNull(message = "취소 하고 싶은 상품을 선택해주세요")
    private Long productOrderId;
}
