package com.jeontongju.order.feign;

import io.github.bitbox.bitbox.dto.FeignFormat;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductFeignServiceClient {
    @CircuitBreaker(
            name = "productFeignServiceClient@getStockUnderFive",
            fallbackMethod = "getDefaultValue"
    )
    @GetMapping("/seller/{sellerId}/product/stockUnderFive")
    FeignFormat<Long> getStockUnderFive(@PathVariable Long sellerId);

    default FeignFormat<Long> getDefaultValue(@PathVariable Long sellerId, Throwable t) {
        return FeignFormat.<Long>builder()
                .code(HttpStatus.OK.value())
                .data(0L)
        .build();

    }
}