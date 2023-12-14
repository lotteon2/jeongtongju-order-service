package com.jeontongju.order.feign;

import com.jeontongju.order.dto.temp.PaymentInfoDto;
import com.jeontongju.payment.dto.temp.OrderInfoDto;
import com.jeontongju.payment.enums.temp.FeignFormat;
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
