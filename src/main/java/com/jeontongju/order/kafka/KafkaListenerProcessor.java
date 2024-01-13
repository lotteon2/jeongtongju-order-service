package com.jeontongju.order.kafka;

import com.jeontongju.order.feign.ConsumerFeignServiceClient;
import com.jeontongju.order.service.OrderService;
import io.github.bitbox.bitbox.dto.AuctionOrderDto;
import io.github.bitbox.bitbox.dto.CartDeleteDto;
import io.github.bitbox.bitbox.dto.CartDeleteListDto;
import io.github.bitbox.bitbox.dto.OrderCancelDto;
import io.github.bitbox.bitbox.dto.OrderInfoDto;
import io.github.bitbox.bitbox.dto.ProductUpdateDto;
import io.github.bitbox.bitbox.dto.ProductUpdateListDto;
import io.github.bitbox.bitbox.dto.ServerErrorForNotificationDto;
import io.github.bitbox.bitbox.enums.NotificationTypeEnum;
import io.github.bitbox.bitbox.enums.RecipientTypeEnum;
import io.github.bitbox.bitbox.util.KafkaTopicNameInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@Component
@RequiredArgsConstructor
public class KafkaListenerProcessor {
    private final KafkaProcessor<CartDeleteListDto> cartDeleteKafkaProcessor;
    private final KafkaProcessor<ProductUpdateListDto> productUpdateKafkaProcessor;
    private final KafkaProcessor<OrderInfoDto> rollbackKafkaProcessor;
    private final KafkaProcessor<ServerErrorForNotificationDto> serverErrorForNotificationDtoKafkaProcessor;
    private final OrderService orderService;
    private final ConsumerFeignServiceClient consumerFeignServiceClient;

    @KafkaListener(topics = KafkaTopicNameInfo.CREATE_ORDER)
    public void createOrder(OrderInfoDto orderInfoDto) {
        try {
            orderService.createOrder(orderInfoDto);
        }catch(Exception e){
            rollbackKafkaProcessor.send(KafkaTopicNameInfo.ADD_STOCK, orderInfoDto); // 카프카를 쏜다.
            serverErrorForNotificationDtoKafkaProcessor.send(KafkaTopicNameInfo.SEND_ERROR_NOTIFICATION,
                    ServerErrorForNotificationDto.builder()
                            .recipientId(orderInfoDto.getOrderCreationDto().getConsumerId())
                            .recipientType(RecipientTypeEnum.ROLE_CONSUMER)
                            .notificationType(NotificationTypeEnum.INTERNAL_ORDER_SERVER_ERROR)
                            .createdAt(now())
                            .error(orderInfoDto)
                    .build());
        }

        if(orderInfoDto.getOrderCreationDto().getCart()) {
            // 장바구니 지우는 카프카를 보낸다
            cartDeleteKafkaProcessor.send(KafkaTopicNameInfo.DELETE_CART, CartDeleteListDto.builder()
                    .cartDeleteDtoList(orderInfoDto.getOrderCreationDto().getProductInfoDtoList().stream().map(productInfoDto -> CartDeleteDto.builder()
                            .consumerId(orderInfoDto.getOrderCreationDto().getConsumerId()).productId(productInfoDto.getProductId())
                            .productCount(productInfoDto.getProductCount()).build()).collect(Collectors.toList()))
                    .build());
        }

        // 판매로그를 엘라스틱 서치에 쌓는다
        productUpdateKafkaProcessor.send(KafkaTopicNameInfo.UPDATE_PRODUCT_SALES_COUNT, ProductUpdateListDto.builder()
                .productUpdateDtoList(orderInfoDto.getProductUpdateDto().stream()
                        .map(productUpdateDto -> ProductUpdateDto.builder().productId(productUpdateDto.getProductId())
                        .productCount(productUpdateDto.getProductCount()).build()).collect(Collectors.toList()))
        .build());
    }

    @KafkaListener(topics = KafkaTopicNameInfo.CREATE_AUCTION_ORDER)
    public void createAuctionOrder(AuctionOrderDto auctionOrderDto) {
        try {
            orderService.createAuctionOrder(auctionOrderDto, consumerFeignServiceClient.getConsumerAddress(auctionOrderDto.getConsumerId()).getData());
        }catch(Exception e){
            // TODO 해당 카프카를 처리하다가 예외 발생시 어떻게 해야하는가?
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopicNameInfo.RECOVER_CANCEL_ORDER)
    public void createAuctionOrder(OrderCancelDto orderCancelDto) {
        orderService.revertOrderStatus(orderCancelDto.getCancelOrderId(), orderCancelDto.getCancelProductOrderId());
    }

    @KafkaListener(topics = KafkaTopicNameInfo.PRODUCT_ORDER_REVIEW_WRITE_STATUS_UPDATE)
    public void updateProductOrderReviewStatus(Long productOrderId){
        orderService.updateProductOrderReviewStatus(productOrderId);
    }
}
