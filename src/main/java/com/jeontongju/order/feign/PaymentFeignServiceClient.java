package com.jeontongju.order.feign;

import io.github.bitbox.bitbox.dto.FeignFormat;
import io.github.bitbox.bitbox.dto.OrderInfoDto;
import io.github.bitbox.bitbox.dto.PaymentInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service")
public interface PaymentFeignServiceClient {
    @PostMapping("/pay-approve")
    FeignFormat<Void> approveKakaopay(@RequestBody OrderInfoDto orderInfoDto);

    @GetMapping("/payment-info")
    FeignFormat<PaymentInfoDto> getPaymentInfo(@RequestParam String orderId);
}
