package com.jeontongju.order.dto.response.seller;

import com.jeontongju.order.dto.response.common.OrderResponseCommonDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SellerOrderListDto extends OrderResponseCommonDto {
    private Long deliveryId;
    private String deliveryCode;
    private Long sellerId;
    private String sellerName;
}