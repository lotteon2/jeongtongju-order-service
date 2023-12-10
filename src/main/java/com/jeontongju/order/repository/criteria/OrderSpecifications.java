package com.jeontongju.order.repository.criteria;

import com.jeontongju.order.domain.Delivery;
import com.jeontongju.order.domain.Orders;
import com.jeontongju.order.domain.ProductOrder;
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

    public static Specification<ProductOrder> buildSellerProductOrdersSpecification(Long sellerId, String orderDate, String productId, Boolean isDeliveryCodeNull) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("sellerId"), sellerId));

            if (!orderDate.equals("null")) {
                LocalDateTime startDate = parseOrderDate(orderDate);
                LocalDateTime endDate = startDate.plusDays(1).minusSeconds(1);
                predicates.add(criteriaBuilder.between(root.get("orderDate"), startDate, endDate));
            }

            if (!productId.equals("null")) {
                predicates.add(criteriaBuilder.equal(root.get("productId"), productId));
            }

            if (isDeliveryCodeNull != null) {
                predicates.add(isDeliveryCodeNullCondition(criteriaBuilder, root, isDeliveryCodeNull));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static LocalDateTime parseOrderDate(String orderDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(orderDate, formatter);
        return localDate.atStartOfDay();
    }

    private static Predicate isDeliveryCodeNullCondition(CriteriaBuilder criteriaBuilder, Root<ProductOrder> root, Boolean isDeliveryCodeNull) {
        Join<ProductOrder, Delivery> deliveryJoin = root.join("delivery", JoinType.LEFT);
        return isDeliveryCodeNull ? criteriaBuilder.isNull(deliveryJoin.get("deliveryCode")) : criteriaBuilder.isNotNull(deliveryJoin.get("deliveryCode"));
    }
}