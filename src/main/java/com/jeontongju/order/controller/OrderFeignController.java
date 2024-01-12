package com.jeontongju.order.controller;

import com.jeontongju.order.service.OrderService;
import io.github.bitbox.bitbox.dto.FeignFormat;
import io.github.bitbox.bitbox.dto.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class OrderFeignController {
    private final OrderService orderService;

    @GetMapping("/product-orders/{productOrderId}/review-verify")
    public FeignFormat<Boolean> isOrderProductConfirmed(@PathVariable long productOrderId){
        return FeignFormat.<Boolean>builder()
                .code(HttpStatus.SC_OK)
                .data(orderService.getDeliveryStatus(productOrderId))
        .build();
    }

    @GetMapping("/sellers/{sellerId}/orders-consumer/ids")
    public FeignFormat<List<Long>> getConsumerOrderIdsBySellerId(@PathVariable long sellerId){
        return FeignFormat.<List<Long>>builder()
                .code(HttpStatus.SC_OK)
                .data(orderService.getConsumerOrderIdsBySellerId(sellerId))
        .build();
    }
}