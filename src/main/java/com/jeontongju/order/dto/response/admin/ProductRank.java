package com.jeontongju.order.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProductRank {
    private SellerProductMonthDto one;
    private SellerProductMonthDto two;
    private SellerProductMonthDto three;
    private SellerProductMonthDto four;
    private SellerProductMonthDto five;
}