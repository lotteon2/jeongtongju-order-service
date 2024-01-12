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

    @Query("SELECT COALESCE(SUM(p.productCount * p.productPrice), 0) " +
            "FROM ProductOrder p " +
            "WHERE REPLACE(SUBSTRING(p.orderDate, 1, 7), '-', '') = :month")
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
            "            product_order " +
            "        WHERE " +
            "            REPLACE(SUBSTRING(order_Date, 1, 7), '-', '') = :orderDate " +
            "       AND product_order_status <> 'CANCEL' "+
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

    @Query(value = "SELECT product_id AS productId, seller_id AS sellerId, product_name AS productName, seller_name AS sellerName, SUM(product_count) AS totalCount, RANK() OVER (ORDER BY SUM(product_count) DESC, seller_id) AS productRank " +
            "FROM product_order " +
            "WHERE REPLACE(SUBSTRING(order_Date, 1, 7), '-', '') = :orderDate AND product_order_status <> 'CANCEL' " +
            "GROUP BY product_id, seller_id, product_name, seller_name " +
            "ORDER BY productRank LIMIT 5", nativeQuery = true)
    LinkedList<MonthProductRankDto> getTop5MonthlyProductRanking(String orderDate);

    @Query("SELECT " +
            "new com.jeontongju.order.dto.response.admin.AllSellerSettlementDtoForAdmin(" +
            "s.sellerId, s.sellerName, s.settlementYear, s.settlementMonth, s.settlementAmount, s.settlementCommission) " +
            "FROM Settlement s " +
            "WHERE s.settlementYear = :year AND s.settlementMonth = :month"
    )
    List<AllSellerSettlementDtoForAdmin> getSettlementDataByYearAndMonth(Long year, Long month);
}