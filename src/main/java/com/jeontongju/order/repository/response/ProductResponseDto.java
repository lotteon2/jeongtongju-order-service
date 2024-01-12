package com.jeontongju.order.repository.response;

import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.enums.ProductOrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProductResponseDto {
    private Long productOrderId;
    private String productId;
    private String productName;
    private Long productCount;
    private Long productPrice;
    private Long productTotalAmount;
    private LocalDateTime orderDate;
    private ProductOrderStatusEnum productOrderStatus;
    private String productThumbnailImageUrl;
    private Long sellerId;
    private String sellerName;
    private Boolean isReviewAllowed;

    public static ProductResponseDto productOrderToProductResponseDto(ProductOrder productOrder, ProductOrderStatusEnum productOrderStatus){
        LocalDateTime orderDate = productOrder.getOrderDate();
        LocalDateTime now = LocalDateTime.now();
        long secondsBetween = ChronoUnit.SECONDS.between(orderDate, now);
        boolean is14DaysPassed = secondsBetween >= (14 * 24 * 60 * 60);


        return ProductResponseDto.builder().productOrderId(productOrder.getProductOrderId()).productId(productOrder.getProductId())
                .productName(productOrder.getProductName()).productCount(productOrder.getProductCount()).productPrice(productOrder.getProductPrice())
                .productTotalAmount(productOrder.getProductCount()*productOrder.getProductPrice()).orderDate(productOrder.getOrderDate()).productOrderStatus(productOrderStatus)
                .productThumbnailImageUrl(productOrder.getProductThumbnailImageUrl()).sellerId(productOrder.getSellerId()).sellerName(productOrder.getSellerName())
                .isReviewAllowed( (productOrderStatus == ProductOrderStatusEnum.CONFIRMED && !is14DaysPassed)
                && !productOrder.getReviewWriteFlag() ).build();
    }
}
