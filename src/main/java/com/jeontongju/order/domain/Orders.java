package com.jeontongju.order.domain;

import com.jeontongju.order.domain.common.BaseEntity;
import com.jeontongju.order.enums.OrderStatusEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@DynamicInsert
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders extends BaseEntity {
    @Id
    @GeneratedValue(generator = "uuid-hibernate-generator")
    @GenericGenerator(name = "uuid-hibernate-generator", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    private String ordersId;

    @NotNull
    private Long consumerId;

    @NotNull
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'NORMAL'")
    private OrderStatusEnum orderStatus;

    @NotNull
    private Long totalPrice;

    @Column(columnDefinition = "boolean default false")
    private boolean isAuction;

    @OneToMany(mappedBy = "orders")
    private List<ProductOrder> productOrders;
}