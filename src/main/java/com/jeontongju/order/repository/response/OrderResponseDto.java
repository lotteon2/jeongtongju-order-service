package com.jeontongju.order.repository.response;

import com.jeontongju.order.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderResponseDto {
    private String ordersId;
    private String orderDate;
    private OrderStatusEnum orderStatus;
    private Boolean isAuction;
    private Boolean isAbleToCancel;
}
