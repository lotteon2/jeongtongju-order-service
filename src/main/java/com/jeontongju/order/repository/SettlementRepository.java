package com.jeontongju.order.repository;

import com.jeontongju.order.domain.Settlement;
import com.jeontongju.order.dto.response.admin.SettlementForAdmin;
import com.jeontongju.order.dto.response.seller.SettlementForSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    @Query("SELECT new com.jeontongju.order.dto.response.admin.SettlementForAdmin(s.settlementYear, s.settlementMonth, s.totalAmount, s.settlementCommission, s.settlementAmount, s.settlementImgUrl) FROM Settlement s WHERE s.sellerId = :sellerId AND s.settlementYear = :settlementYear")
    List<SettlementForAdmin> findBySellerIdAndSettlementYear(Long sellerId, Long settlementYear);

    @Query("SELECT new com.jeontongju.order.dto.response.seller.SettlementForSeller(s.settlementImgUrl) FROM Settlement s WHERE s.sellerId = :sellerId AND s.settlementYear = :settlementYear AND s.settlementMonth = :settlementMonth")
    SettlementForSeller findBySellerIdAndSettlementYearAndSettlementMonth(Long sellerId, Long settlementYear, Long settlementMonth);
}