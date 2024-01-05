package com.jeontongju.order.repository.criteria;

import com.jeontongju.order.domain.Delivery;
import com.jeontongju.order.domain.Orders;
import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.enums.ProductOrderStatusEnum;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {
    public static Specification<Orders> buildConsumerOrderSpecification(Long consumerId, Boolean isAuction) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("consumerId"), consumerId));

            if (isAuction != null) {
                predicates.add(isAuction ? criteriaBuilder.isTrue(root.get("isAuction")) : criteriaBuilder.isFalse(root.get("isAuction")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<ProductOrder> buildSellerProductOrdersSpecification(Long sellerId, String startDate, String endDate , String productId, ProductOrderStatusEnum productStatus) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("sellerId"), sellerId));

            if (!startDate.equals("null") && !endDate.equals("null")) {
                LocalDateTime from = parseOrderDate(startDate);
                LocalDateTime to = parseOrderDate(endDate).plusDays(1).minusSeconds(1);

                predicates.add(criteriaBuilder.between(root.get("orderDate"), from, to));
            }

            if (!productId.equals("null")) {
                predicates.add(criteriaBuilder.equal(root.get("productId"), productId));
            }

            if (productStatus!=null) {
                if(productStatus == ProductOrderStatusEnum.ORDER){
                    predicates.add(criteriaBuilder.equal(root.get("productOrderStatus"), ProductOrderStatusEnum.ORDER));
                    predicates.add(isDeliveryCodeNullCondition(criteriaBuilder, root));
                }else if(productStatus == ProductOrderStatusEnum.CANCEL){
                    predicates.add(criteriaBuilder.equal(root.get("productOrderStatus"), ProductOrderStatusEnum.CANCEL));
                }else if(productStatus == ProductOrderStatusEnum.COMPLETED){
                    predicates.add(criteriaBuilder.equal(root.get("productOrderStatus"), ProductOrderStatusEnum.ORDER));
                    predicates.add(isDeliveryStatusCondition(criteriaBuilder, root, ProductOrderStatusEnum.COMPLETED));
                }else if(productStatus == ProductOrderStatusEnum.CONFIRMED){
                    predicates.add(criteriaBuilder.equal(root.get("productOrderStatus"), ProductOrderStatusEnum.CONFIRMED));
                }else if(productStatus == ProductOrderStatusEnum.SHIPPING){
                    predicates.add(criteriaBuilder.equal(root.get("productOrderStatus"), ProductOrderStatusEnum.ORDER));
                    predicates.add(isDeliveryStatusCondition(criteriaBuilder, root, ProductOrderStatusEnum.SHIPPING));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static LocalDateTime parseOrderDate(String orderDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(orderDate, formatter);
        return localDate.atStartOfDay();
    }

    private static Predicate isDeliveryCodeNullCondition(CriteriaBuilder criteriaBuilder, Root<ProductOrder> root) {
        Join<ProductOrder, Delivery> deliveryJoin = root.join("delivery", JoinType.LEFT);
        return criteriaBuilder.isNull(deliveryJoin.get("deliveryCode"));
    }

    private static Predicate isDeliveryStatusCondition(CriteriaBuilder criteriaBuilder, Root<ProductOrder> root, ProductOrderStatusEnum status) {
        Join<ProductOrder, Delivery> deliveryJoin = root.join("delivery", JoinType.LEFT);
        return criteriaBuilder.equal(deliveryJoin.get("deliveryStatus"), status);
    }
}