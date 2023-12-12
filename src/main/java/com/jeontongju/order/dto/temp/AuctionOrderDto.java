package com.jeontongju.order.dto.temp;

import com.jeontongju.payment.enums.temp.PaymentMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class AuctionOrderDto {
    // orders 테이블을 만들기 위해 필요한 정보
    private Long consumerId;
    private LocalDateTime orderDate;
    private Long totalPrice;
    private PaymentMethodEnum paymentMethod;

    // product_order를 만들기 위해 필요한 정보
    private String productId;
    private String productName;
    private Long productCount;
    private Long productPrice;
    private Long sellerId;
    private String sellerName;
    private String productImg;


    public static AuctionOrderDto of(Long consumerId, Long totalPrice, String productId,
                                     String productName, Long productPrice, Long sellerId, String sellerName, String productImg) {
        return AuctionOrderDto.builder()
                .consumerId(consumerId)
                .orderDate(LocalDateTime.now())
                .totalPrice(totalPrice)
                .paymentMethod(PaymentMethodEnum.CREDIT)
                .productId(productId)
                .productName(productName)
                .productPrice(productPrice)
                .sellerId(sellerId)
                .sellerName(sellerName)
                .productImg(productImg)
                .build();
    }
}