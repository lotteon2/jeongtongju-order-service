package com.jeontongju.order.dto.response.consumer;

import com.jeontongju.order.dto.response.common.PageInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ConsumerOrderListResponseDto extends PageInfoDto {
    private List<OrderListDto> content;
}