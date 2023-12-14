package com.jeontongju.order.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageInfoDto {
    private Pageable pageable;
    private Boolean last;
    private Long totalElements;
    private Integer totalPages;
    private Integer size;
    private Integer number;
    private Sort sort;
    private Boolean first;
    private Integer numberOfElements;
    private Boolean empty;
}