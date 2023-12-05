package com.jeontongju.order.controller;

import com.jeontongju.order.dto.DeliveryDto;
import com.jeontongju.order.dto.response.ProductOrderConfirmResponseDto;
import com.jeontongju.order.dto.temp.ResponseFormat;
import com.jeontongju.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;

    @PatchMapping("delivery/{deliveryId}")
    public ResponseEntity<ResponseFormat<Void>> addDeliveryCode(@PathVariable long deliveryId, @RequestBody DeliveryDto deliveryDto){
        orderService.addDeliveryCode(deliveryId,deliveryDto.getDeliveryCode());
        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(org.springframework.http.HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("운송장 등록완료")
        .build());
    }

    @PatchMapping("/product-order/{productOrderId}")
    public ResponseEntity<ResponseFormat<ProductOrderConfirmResponseDto>> confirmProductOrder(@PathVariable long productOrderId){
        return ResponseEntity.ok().body(ResponseFormat.<ProductOrderConfirmResponseDto>builder()
                .code(org.springframework.http.HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 확정 완료")
                .data(
                        ProductOrderConfirmResponseDto.builder()
                                .point(orderService.confirmProductOrder(productOrderId))
                        .build()
                )
        .build());
    }
}