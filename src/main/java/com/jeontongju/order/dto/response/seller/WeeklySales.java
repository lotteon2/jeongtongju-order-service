package com.jeontongju.order.dto.response.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@NotNull
@AllArgsConstructor
public class WeeklySales {
    private Long num;
    private String name;
}
