package com.jeontongju.order.feign;

import com.jeontongju.payment.dto.temp.OrderInfoDto;
import com.jeontongju.payment.enums.temp.FeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentFeignServiceClient {
    @PostMapping("/pay-approve")
    FeignFormat<Void> approveKakaopay(@RequestBody OrderInfoDto orderInfoDto);
}
