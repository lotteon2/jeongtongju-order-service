package com.jeontongju.order.dto.response.consumer;

import com.jeontongju.order.dto.response.common.OrderResponseCommonDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsumerOrderListDto extends OrderResponseCommonDto {
    private Boolean isAuction;
}
