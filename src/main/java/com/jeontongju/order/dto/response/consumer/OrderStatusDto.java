package com.jeontongju.order.dto.response.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderStatusDto {
    private String code;
    private String name;

    public static List<OrderStatusDto> getOrderStatus() {
        return Arrays.asList(
                createOrderStatus("ORDER", "주문완료"),
                createOrderStatus("CANCEL", "주문취소"),
                createOrderStatus("CONFIRMED", "주문확정"),
                createOrderStatus("SHIPPING", "배송중"),
                createOrderStatus("COMPLETED", "배송완료")
        );
    }

    private static OrderStatusDto createOrderStatus(String code, String name) {
        return OrderStatusDto.builder().code(code).name(name).build();
    }
}
