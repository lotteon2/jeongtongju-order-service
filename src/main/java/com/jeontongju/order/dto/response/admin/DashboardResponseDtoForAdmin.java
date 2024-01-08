package com.jeontongju.order.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DashboardResponseDtoForAdmin {
    private Long totalSalesMonth;
    private Long commissionMonth;
    private SellerRank monthSellerRank;
    private ProductRank monthProductRank;
}