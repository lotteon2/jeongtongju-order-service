package com.jeontongju.order.dto.response.consumer;

import com.jeontongju.order.domain.Delivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DeliveryResponseDto {
    private String recipientName;
    private String recipientPhoneNumber;
    private String basicAddress;
    private String addressDetail;
    private String zonecode;

    public static DeliveryResponseDto DeliveryToDeliveryResponseDto(Delivery delivery){
        return DeliveryResponseDto.builder().recipientName(delivery.getRecipientName()).recipientPhoneNumber(delivery.getRecipientPhoneNumber())
                .basicAddress(delivery.getBasicAddress()).addressDetail(delivery.getAddressDetail()).zonecode(delivery.getZonecode()).build();
    }
}