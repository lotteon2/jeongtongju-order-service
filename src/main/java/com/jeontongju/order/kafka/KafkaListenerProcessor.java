package com.jeontongju.order.kafka;

import com.jeontongju.order.service.OrderService;
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
    private final String ORDER_CREATION_TOPIC = "create-order";
    private final String ROLLBACK_STOCK_TOPIC = "add-stock";
    private final String CART_DELETE_TOPIC = "delete-cart";

    private final KafkaProcessor<List<CartDeleteDto>> cartDeleteKafkaProcessor;
    private final KafkaProcessor<OrderInfoDto> rollbackKafkaProcessor;
    private final OrderService orderService;

    @KafkaListener(topics = ORDER_CREATION_TOPIC)
    public void createOrder(OrderInfoDto orderInfoDto) {
        try {
            orderService.createOrder(orderInfoDto);
        }catch(Exception e){
            rollbackKafkaProcessor.send(ROLLBACK_STOCK_TOPIC, orderInfoDto); // 카프카를 쏜다.
            // [TODO] 알림을 발송한다.
        }

        // 장바구니 지우는 카프카를 보낸다
        long consumerId = orderInfoDto.getOrderCreationDto().getConsumerId();
        cartDeleteKafkaProcessor.send(CART_DELETE_TOPIC, orderInfoDto.getOrderCreationDto().getProductInfoDtoList()
                .stream()
                .map(productInfoDto -> CartDeleteDto.builder()
                        .consumerId(consumerId)
                        .productId(productInfoDto.getProductId())
                        .productCount(productInfoDto.getProductCount())
        .build()).collect(Collectors.toList()));
    }
}
