package com.jeontongju.order.dto.temp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInfoDto {
    private Long minusPointAmount;
    private Long minusCouponAmount;
    private Long totalPrice;
    private String couponCode;
    private Long realPrice;
}