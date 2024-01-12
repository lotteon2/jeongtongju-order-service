package com.jeontongju.order.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class AllSellerSettlementDtoForAdmin {
    private Long sellerId;
    private String sellerName;
    private Long settlementYear;
    private Long settlementMonth;
    private Long settlementAmount;
    private Long settlementCommission;
}