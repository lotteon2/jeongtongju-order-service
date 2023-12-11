package com.jeontongju.order.dto.response.consumer;

import com.jeontongju.order.dto.response.common.OrderResponseCommonDto;
import com.jeontongju.order.dto.response.common.PageInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsumerOrderListResponseDtoForAdmin extends PageInfoDto {
    private List<OrderResponseCommonDto> content;
}