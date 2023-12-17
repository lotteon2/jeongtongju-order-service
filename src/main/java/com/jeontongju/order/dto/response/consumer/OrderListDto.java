package com.jeontongju.order.dto.response.consumer;

import com.jeontongju.order.repository.response.OrderResponseDto;
import com.jeontongju.order.repository.response.ProductResponseDto;
import io.github.bitbox.bitbox.dto.PaymentInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderListDto {
    private OrderResponseDto order;
    private List<ProductResponseDto> product;
    private PaymentInfoDto payment;
    private DeliveryResponseDto delivery;
}
