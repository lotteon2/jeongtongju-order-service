package com.jeontongju.order.dto.response.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
public class ProductOrderConfirmResponseDto {
    private Long point;
}
