package com.jeontongju.order.kafka;

import com.jeontongju.order.dto.temp.AuctionOrderDto;
import com.jeontongju.order.feign.ConsumerFeignServiceClient;
import com.jeontongju.order.service.OrderService;
import com.jeontongju.order.util.KafkaTopicNameInfo;
import com.jeontongju.payment.dto.temp.CartDeleteDto;
import com.jeontongju.payment.dto.temp.OrderInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KafkaListenerProcessor {
    private final KafkaProcessor<List<CartDeleteDto>> cartDeleteKafkaProcessor;
    private final KafkaProcessor<OrderInfoDto> rollbackKafkaProcessor;
    private final OrderService orderService;
    private final ConsumerFeignServiceClient consumerFeignServiceClient;

    @KafkaListener(topics = KafkaTopicNameInfo.ORDER_CREATION_TOPIC)
    public void createOrder(OrderInfoDto orderInfoDto) {
        try {
            orderService.createOrder(orderInfoDto);
        }catch(Exception e){
            rollbackKafkaProcessor.send(KafkaTopicNameInfo.ROLLBACK_STOCK_TOPIC, orderInfoDto); // 카프카를 쏜다.
            // [TODO] 알림을 발송한다.
        }

        // 장바구니 지우는 카프카를 보낸다
        long consumerId = orderInfoDto.getOrderCreationDto().getConsumerId();
        cartDeleteKafkaProcessor.send(KafkaTopicNameInfo.CART_DELETE_TOPIC, orderInfoDto.getOrderCreationDto().getProductInfoDtoList()
                .stream()
                .map(productInfoDto -> CartDeleteDto.builder()
                        .consumerId(consumerId)
                        .productId(productInfoDto.getProductId())
                        .productCount(productInfoDto.getProductCount())
        .build()).collect(Collectors.toList()));
    }

    @KafkaListener(topics = KafkaTopicNameInfo.AUCTION_ORDER_TOPIC)
    public void createAuctionOrder(AuctionOrderDto auctionOrderDto) {
        try {
            orderService.createAuctionOrder(auctionOrderDto, consumerFeignServiceClient.getConsumerAddress(auctionOrderDto.getConsumerId()).getData());
        }catch(Exception e){
            // TODO 해당 카프카를 처리하다가 예외 발생시 어떻게 해야하는가?
        }
    }
}
