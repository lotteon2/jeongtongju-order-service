package com.jeontongju.order.dto.response.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DashboardResponseDto {
    private Long order;
    private Long shipping;
    private Long completed;
    private Long confirmed;
    private Long cancel;
    private Long monthSales;
    private Long monthSettlement;
    private Long stockUnderFive;
    private Long trackingNumberNotEntered;
    private Long monday;
    private Long tuesday;
    private Long wednesday;
    private Long thursday;
    private Long friday;
    private Long saturday;
    private Long sunday;
}
