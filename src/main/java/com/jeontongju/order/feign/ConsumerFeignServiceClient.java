package com.jeontongju.order.feign;

import com.jeontongju.order.dto.temp.AddressDto;
import com.jeontongju.order.dto.temp.OrderConfirmDto;
import com.jeontongju.payment.enums.temp.FeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "consumer-service")
public interface ConsumerFeignServiceClient {
    @PostMapping("/orders-confirm")
    FeignFormat<Long> getOrderConfirmPoint(@RequestBody OrderConfirmDto orderConfirmDto);

    @GetMapping("/consumer/{consumerId}/address")
    FeignFormat<AddressDto> getConsumerAddress(@PathVariable Long consumerId);
}