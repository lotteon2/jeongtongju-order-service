package com.jeontongju.order.dto.response.common;

import com.jeontongju.order.enums.ProductOrderStatusEnum;
import com.jeontongju.payment.enums.temp.PaymentMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderResponseCommonDto {
    private String ordersId;
    private String productId;
    private String productName;
    private Long productCount;
    private Long productTotalAmount;
    private String orderDate;
    private PaymentMethodEnum paymentType;
    private ProductOrderStatusEnum orderStatus;
    private Boolean isAuction;
}
