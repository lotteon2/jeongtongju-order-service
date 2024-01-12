package com.jeontongju.order.dto.response.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DashboardResponseDtoForSeller {
    private Long order;
    private Long shipping;
    private Long completed;
    private Long confirmed;
    private Long cancel;
    private Long monthSales;
    private Long monthSettlement;
    private Long stockUnderFive;
    private Long trackingNumberNotEntered;
    private List<WeeklySales> weeklySales;
}