package com.jeontongju.order.repository;

import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.dto.response.admin.AllSellerSettlementDtoForAdmin;
import com.jeontongju.order.repository.response.MonthProductRankDto;
import com.jeontongju.order.repository.response.MonthSellerRankDto;
import com.jeontongju.order.repository.response.OrderStatusDtoForDashboard;
import com.jeontongju.order.repository.response.WeeklySalesDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.LinkedList;
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
            "WHERE p.sellerId = :sellerId AND d.deliveryCode IS NULL " +
            "AND p.productOrderStatus <> 'CANCEL'")
    Long countNullDeliveryCodesBySellerId(Long sellerId);

    @Query("SELECT COALESCE(SUM(p.productCount * p.productPrice), 0) " +
            "FROM ProductOrder p JOIN p.orders o " +
            "WHERE REPLACE(SUBSTRING(p.orderDate, 1, 7), '-', '') = :month "+
            "AND (p.productOrderStatus = 'CONFIRMED' OR o.isAuction = true) "+
            "AND p.sellerId = :sellerId")
    Long sumOrderTotalPriceByMonth(String month, Long sellerId);

    @Query("SELECT NEW com.jeontongju.order.repository.response.WeeklySalesDto(SUBSTRING(p.orderDate, 1, 10), SUM(p.productCount * p.productPrice)) " +
            "FROM ProductOrder p JOIN p.orders o " +
            "WHERE REPLACE(SUBSTRING(p.orderDate, 1, 10), '-', '') BETWEEN :startDate AND :endDate " +
            "AND p.sellerId = :sellerId "+
            "AND (p.productOrderStatus = 'CONFIRMED' OR o.isAuction = true) "+
            "GROUP BY SUBSTRING(p.orderDate, 1, 10)")
    List<WeeklySalesDto> sumOrderTotalPriceInDateRange(String startDate, String endDate, Long sellerId);

    @Query("SELECT COALESCE(SUM(p.productCount * p.productPrice), 0) " +
            "FROM ProductOrder p JOIN p.orders o " +
            "WHERE REPLACE(SUBSTRING(p.orderDate, 1, 7), '-', '') = :month " +
            "AND (p.productOrderStatus = 'CONFIRMED' OR o.isAuction = true)")
    Long sumOrderTotalPriceByMonthExternal(String month);

    @Query("SELECT DISTINCT p.consumerId FROM ProductOrder p WHERE p.sellerId = :sellerId")
    List<Long> findDistinctConsumersBySellerId(Long sellerId);

    @Query(value = "SELECT " +
            "seller_id AS sellerId, " +
            "seller_name AS sellerName, " +
            "sellerRank, " +
            "totalSales AS totalPrice " +
            "FROM ( " +
            "    SELECT " +
            "        seller_id, " +
            "        seller_name, " +
            "        RANK() OVER (ORDER BY totalSales DESC, seller_id ASC) AS sellerRank, " +
            "        totalSales " +
            "    FROM ( " +
            "        SELECT " +
            "            seller_id, " +
            "            seller_name, " +
            "            SUM(product_Count * product_Price) AS totalSales " +
            "        FROM " +
            "            product_order p JOIN orders o ON p.orders_id = o.orders_id " +
            "        WHERE " +
            "            REPLACE(SUBSTRING(p.order_Date, 1, 7), '-', '') = :orderDate " +
            "       AND (p.product_order_status = 'CONFIRMED' OR o.is_auction = true) "+
            "        GROUP BY " +
            "            seller_id, " +
            "            seller_name " +
            "    ) AS sellerSales " +
            ") AS rankedSellers " +
            "WHERE sellerRank <= 5 " +
            "ORDER BY " +
            "    sellerRank",
            nativeQuery = true)
    LinkedList<MonthSellerRankDto> getTop5MonthlySellerRanking(String orderDate);

    @Query(value = "SELECT p.product_id AS productId, p.seller_id AS sellerId, p.product_name AS productName, p.seller_name AS sellerName, SUM(p.product_count) AS totalCount, RANK() OVER (ORDER BY SUM(p.product_count) DESC, p.seller_id) AS productRank " +
            "FROM product_order p JOIN orders o ON p.orders_id = o.orders_id " +
            "WHERE REPLACE(SUBSTRING(p.order_Date, 1, 7), '-', '') = :orderDate AND (p.product_order_status = 'CONFIRMED' OR o.is_auction = true) " +
            "GROUP BY p.product_id, p.seller_id, p.product_name, p.seller_name " +
            "ORDER BY productRank LIMIT 5", nativeQuery = true)
    LinkedList<MonthProductRankDto> getTop5MonthlyProductRanking(String orderDate);

    @Query("SELECT " +
            "new com.jeontongju.order.dto.response.admin.AllSellerSettlementDtoForAdmin(" +
            "s.sellerId, s.sellerName, s.settlementYear, s.settlementMonth, s.totalAmount, s.settlementCommission) " +
            "FROM Settlement s " +
            "WHERE s.settlementYear = :year AND s.settlementMonth = :month"
    )
    List<AllSellerSettlementDtoForAdmin> getSettlementDataByYearAndMonth(Long year, Long month);
}