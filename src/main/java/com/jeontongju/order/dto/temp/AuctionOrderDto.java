package com.jeontongju.order.dto.temp;

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

    // product_order를 만들기 위해 필요한 정보
    private String productId;
    private String productName;
    private Long productCount;
    private Long productPrice;
    private Long sellerId;
    private String sellerName;
    private String productImg;

    // delivery를 만들기 위해 필요한 정보
    private String recipientName;
    private String recipientPhoneNumber;
    private String basicAddress;
    private String addressDetail;
    private String zonecode;

}