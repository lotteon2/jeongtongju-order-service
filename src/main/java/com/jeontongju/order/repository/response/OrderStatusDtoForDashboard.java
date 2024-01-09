package com.jeontongju.order.repository.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderStatusDtoForDashboard {
    private Long ordered;
    private Long shipping;
    private Long completed;
    private Long confirmed;
    private Long cancel;

    public void setNullToZero(){
        if(ordered == null) ordered= 0L;
        if(shipping == null) shipping= 0L;
        if(completed == null) completed= 0L;
        if(confirmed == null) confirmed= 0L;
        if(cancel == null) cancel= 0L;
    }
}