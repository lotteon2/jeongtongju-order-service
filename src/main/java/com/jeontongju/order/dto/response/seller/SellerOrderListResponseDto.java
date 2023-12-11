package com.jeontongju.order.dto.response.seller;

import com.jeontongju.order.dto.response.common.PageInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerOrderListResponseDto extends PageInfoDto {
    private List<SellerOrderListDto> content;
}