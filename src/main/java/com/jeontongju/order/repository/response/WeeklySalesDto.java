package com.jeontongju.order.repository.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class WeeklySalesDto{
    private String orderDay;
    private Long totalAmount;
}
