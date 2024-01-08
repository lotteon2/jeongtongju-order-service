package com.jeontongju.order.repository.response;

public interface MonthProductRankDto {
    String getProductId();
    Long getSellerId();
    String getProductName();
    String getSellerName();
    Long getTotalCount();
    Long getProductRank();
}