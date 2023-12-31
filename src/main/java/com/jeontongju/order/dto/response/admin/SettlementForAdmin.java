package com.jeontongju.order.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SettlementForAdmin {
    private Long settlementYear;
    private Long settlementMonth;
    private Long totalAmount;
    private Long settlementCommision;
    private Long settlementAmount;
    private String settlementImgUrl;
}