package com.jeontongju.order.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DashboardResponseDtoForAdmin {
    private Long totalSalesMonth;
    private Long commissionMonth;
    private List<SellerRankMonthDto> monthSellerRank;
    private List<SellerProductMonthDto> monthProductRank;
}