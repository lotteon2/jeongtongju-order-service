package com.jeontongju.order.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SellerProductMonthDto extends MonthRankCommonDto{
    private String productId;
    private String productName;
    private Long totalCount;
}