package com.jeontongju.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProductOrderStatusEnum {
    ORDER,
    CANCEL,
    CONFIRMED,
    SHIPPING,
    COMPLETED
}
