package com.jeontongju.order.domain;

import com.jeontongju.order.domain.common.BaseEntity;
import com.jeontongju.order.enums.ProductOrderStatusEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Getter
@Entity
@DynamicInsert
@DynamicUpdate
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_order_id")
    private ProductOrder productOrder;

    @NotNull
    private String recipientName;

    @NotNull
    private String recipientPhoneNumber;

    @NotNull
    private String basicAddress;

    private String addressDetail;

    @NotNull
    private String zonecode;

    private String deliveryCode;

    @Enumerated(EnumType.STRING)
    private ProductOrderStatusEnum deliveryStatus;

    /**
        운송장 번호를 추가하는 메소드(운송장을 추가하면 해당 상품의 배송 상태는 배송중이 된다)
     */
    public void addDeliveryCode(String deliveryCode){
        this.deliveryCode = deliveryCode;
        this.deliveryStatus = ProductOrderStatusEnum.SHIPPING;
    }

    /**
     * 배송 완료 상태로 변경하는 메소드
     */
    public void changeDeliveryConfirmStatus(){
        this.deliveryStatus = ProductOrderStatusEnum.COMPLETED;
    }
}
