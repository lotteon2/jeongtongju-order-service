package com.jeontongju.order.controller;

import com.jeontongju.order.enums.ProductOrderStatusEnum;
import com.jeontongju.order.service.OrderService;
import com.jeontongju.payment.enums.temp.FeignFormat;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class OrderFeignController {
    private final OrderService orderService;
    @GetMapping("/product-orders/{productOrderId}/status")
    public FeignFormat<ProductOrderStatusEnum> isOrderProductConfirmed(@PathVariable long productOrderId){
        return FeignFormat.<ProductOrderStatusEnum>builder()
                .code(HttpStatus.SC_OK)
                .data(orderService.getDeliveryStatus(productOrderId))
        .build();
    }
}