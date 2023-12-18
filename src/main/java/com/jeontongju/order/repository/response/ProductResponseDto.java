package com.jeontongju.order.repository.response;

import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.enums.ProductOrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    public static ProductResponseDto productOrderToProductResponseDto(ProductOrder productOrder, ProductOrderStatusEnum productOrderStatus){
        return ProductResponseDto.builder().productOrderId(productOrder.getProductOrderId()).productId(productOrder.getProductId())
                .productName(productOrder.getProductName()).productCount(productOrder.getProductCount()).productPrice(productOrder.getProductPrice())
                .productTotalAmount(productOrder.getProductCount()*productOrder.getProductPrice()).orderDate(productOrder.getOrderDate()).productOrderStatus(productOrderStatus)
                .productThumbnailImageUrl(productOrder.getProductThumbnailImageUrl()).sellerId(productOrder.getSellerId()).sellerName(productOrder.getSellerName()).build();
    }
}
