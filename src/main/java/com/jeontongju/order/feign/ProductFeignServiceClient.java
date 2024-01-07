package com.jeontongju.order.feign;

import io.github.bitbox.bitbox.dto.FeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductFeignServiceClient {
    @GetMapping("/seller/{sellerId}/product/stockUnderFive")
    FeignFormat<Long> getStockUnderFive(@PathVariable Long sellerId);
}