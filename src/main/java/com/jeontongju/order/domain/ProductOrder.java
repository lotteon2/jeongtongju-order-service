package com.jeontongju.order.domain;

import com.jeontongju.order.domain.common.BaseEntity;
import com.jeontongju.order.enums.ProductOrderStatusEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Getter
@Entity
@DynamicInsert
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productOrderId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id")
    private Orders orders;

    @NotNull
    private String productId;

    @NotNull
    private String productName;

    @NotNull
    private Long productCount;

    @NotNull
    private Long productPrice;

    @NotNull
    private Long productRealAmount;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long productRealPointAmount;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long productRealCouponAmount;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'ORDER'")
    private ProductOrderStatusEnum productOrderStatus;

    @NotNull
    private Long sellerId;

    @NotNull
    private String sellerName;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String productImg;

    @OneToOne(mappedBy = "productOrder")
    private Delivery delivery;

    /**
     * 주문확정 상태로 변경
     */
    public void changeOrderedStatusToConfirmStatus(){
        this.productOrderStatus = ProductOrderStatusEnum.CONFIRMED;
    }
}
