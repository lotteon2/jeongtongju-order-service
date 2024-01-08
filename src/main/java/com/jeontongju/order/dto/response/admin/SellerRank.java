package com.jeontongju.order.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SellerRank {
    private SellerRankMonthDto one;
    private SellerRankMonthDto two;
    private SellerRankMonthDto three;
    private SellerRankMonthDto four;
    private SellerRankMonthDto five;
}