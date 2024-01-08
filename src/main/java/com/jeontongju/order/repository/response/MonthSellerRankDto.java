package com.jeontongju.order.repository.response;

public interface MonthSellerRankDto {
    Long getSellerId();
    String getSellerName();
    Long getSellerRank();
    Long getTotalPrice();
}