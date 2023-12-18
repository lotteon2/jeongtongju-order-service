package com.jeontongju.order.kafka;

import com.jeontongju.order.feign.ConsumerFeignServiceClient;
import com.jeontongju.order.service.OrderService;
import io.github.bitbox.bitbox.dto.AuctionOrderDto;
import io.github.bitbox.bitbox.dto.CartDeleteDto;
import io.github.bitbox.bitbox.dto.OrderInfoDto;
import io.github.bitbox.bitbox.dto.ProductUpdateDto;
import io.github.bitbox.bitbox.util.KafkaTopicNameInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KafkaListenerProcessor {
    private final KafkaProcessor<List<CartDeleteDto>> cartDeleteKafkaProcessor;
    private final KafkaProcessor<List<ProductUpdateDto>> productUpdateKafkaProcessor;
    private final KafkaProcessor<OrderInfoDto> rollbackKafkaProcessor;
    private final OrderService orderService;
    private final ConsumerFeignServiceClient consumerFeignServiceClient;

    @KafkaListener(topics = KafkaTopicNameInfo.CREATE_ORDER)
    public void createOrder(OrderInfoDto orderInfoDto) {
        try {
            orderService.createOrder(orderInfoDto);
        }catch(Exception e){
            rollbackKafkaProcessor.send(KafkaTopicNameInfo.ADD_STOCK, orderInfoDto); // 카프카를 쏜다.
            // [TODO] 알림을 발송한다.
        }

        // 장바구니 지우는 카프카를 보낸다
        cartDeleteKafkaProcessor.send(KafkaTopicNameInfo.DELETE_CART, orderInfoDto.getOrderCreationDto().getProductInfoDtoList()
                .stream().map(productInfoDto -> CartDeleteDto.builder().consumerId(orderInfoDto.getOrderCreationDto().getConsumerId()).productId(productInfoDto.getProductId())
                    .productCount(productInfoDto.getProductCount()).build()).collect(Collectors.toList()));

        // 판매로그를 엘라스틱 서치에 쌓는다
        productUpdateKafkaProcessor.send(KafkaTopicNameInfo.UPDATE_PRODUCT_SALES_COUNT, orderInfoDto.getProductUpdateDto().stream()
                .map(productUpdateDto -> ProductUpdateDto.builder().productId(productUpdateDto.getProductId())
                        .productCount(productUpdateDto.getProductCount()).build()).collect(Collectors.toList()));
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
}
