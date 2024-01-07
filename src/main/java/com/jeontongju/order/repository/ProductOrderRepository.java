package com.jeontongju.order.repository;

import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.repository.response.OrderStatusDtoForDashboard;
import com.jeontongju.order.repository.response.WeeklySalesDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long>, JpaSpecificationExecutor<ProductOrder> {
    Page<ProductOrder> findByConsumerId(Long consumerId, Pageable pageable);

    @Query("SELECT " +
            "NEW com.jeontongju.order.repository.response.OrderStatusDtoForDashboard(" +
            "SUM(CASE WHEN p.productOrderStatus = 'ORDER' AND d.deliveryCode IS NULL THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN d.deliveryStatus = 'SHIPPING' AND p.productOrderStatus = 'ORDER' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN d.deliveryStatus = 'COMPLETED' AND p.productOrderStatus = 'ORDER' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.productOrderStatus = 'CONFIRMED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.productOrderStatus = 'CANCEL' THEN 1 ELSE 0 END)) " +
            "FROM ProductOrder p JOIN p.delivery d " +
            "WHERE REPLACE(SUBSTRING(p.orderDate, 1, 10), '-', '') BETWEEN :startDate AND :endDate " +
            "AND p.sellerId = :sellerId"
    )
    OrderStatusDtoForDashboard getOrderStatsInDateRange(String startDate, String endDate, Long sellerId);

    @Query("SELECT COUNT(d) " +
            "FROM ProductOrder p JOIN p.delivery d " +
            "WHERE p.sellerId = :sellerId AND d.deliveryCode IS NULL")
    Long countNullDeliveryCodesBySellerId(Long sellerId);

    @Query("SELECT COALESCE(SUM(p.productCount * p.productPrice), 0) " +
            "FROM ProductOrder p " +
            "WHERE REPLACE(SUBSTRING(p.orderDate, 1, 7), '-', '') = :month "+
            "AND p.sellerId = :sellerId")
    Long sumOrderTotalPriceByMonth(String month, Long sellerId);

    @Query("SELECT NEW com.jeontongju.order.repository.response.WeeklySalesDto(SUBSTRING(p.orderDate, 1, 10), SUM(p.productCount * p.productPrice)) " +
            "FROM ProductOrder p " +
            "WHERE REPLACE(SUBSTRING(p.orderDate, 1, 10), '-', '') BETWEEN :startDate AND :endDate " +
            "AND p.sellerId = :sellerId "+
            "GROUP BY SUBSTRING(p.orderDate, 1, 10)")
    List<WeeklySalesDto> sumOrderTotalPriceInDateRange(String startDate, String endDate, Long sellerId);
}