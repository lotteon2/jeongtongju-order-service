package com.jeontongju.order.repository;

import com.jeontongju.order.domain.ProductOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long>, JpaSpecificationExecutor<ProductOrder> {
    Page<ProductOrder> findByConsumerId(Long consumerId, Pageable pageable);
}