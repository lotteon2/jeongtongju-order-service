package com.jeontongju.order.controller;

import com.jeontongju.order.dto.DeliveryDto;
import com.jeontongju.order.dto.OrderCancelRequestDto;
import com.jeontongju.order.dto.ProductOrderCancelRequestDto;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDto;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDtoForAdmin;
import com.jeontongju.order.dto.response.consumer.ProductOrderConfirmResponseDto;
import com.jeontongju.order.dto.response.seller.SellerOrderListResponseDto;
import com.jeontongju.order.dto.temp.ResponseFormat;
import com.jeontongju.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/order/consumer")
    public ResponseEntity<ResponseFormat<ConsumerOrderListResponseDto>> getConsumerOrderList(
            @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable, @RequestHeader Long memberId,
            @RequestParam(required = false) Boolean isAuction){
        // TODO 유저만 사용 가능한 API임
        return ResponseEntity.ok().body(ResponseFormat.<ConsumerOrderListResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 내역 조회 성공")
                .data(orderService.getConsumerOrderList(memberId, isAuction, pageable))
        .build());
    }

    @GetMapping("/order/consumer/{consumerId}")
    public ResponseEntity<ResponseFormat<ConsumerOrderListResponseDtoForAdmin>> getConsumerOrderListForAdmin(
            @PathVariable Long consumerId, @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable){
        // TODO 유저만 사용 가능한 API임
        return ResponseEntity.ok().body(ResponseFormat.<ConsumerOrderListResponseDtoForAdmin>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문내역 조회 완료")
                .data(orderService.getConsumerOrderList(consumerId, pageable))
        .build());
    }

    @GetMapping("/order/seller")
    public ResponseEntity<ResponseFormat<SellerOrderListResponseDto>> getSellerOrderList(
            @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestHeader Long memberId, @RequestParam String orderDate, @RequestParam String productId, @RequestParam(required = false) Boolean isDeliveryCodeNull){
        // TODO 셀러만 사용 가능한 API임
        return ResponseEntity.ok().body(ResponseFormat.<SellerOrderListResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문내역 조회 완료")
                .data(orderService.getSellerOrderList(memberId, orderDate, productId, isDeliveryCodeNull, pageable))
        .build());
    }

    @GetMapping("/order/seller/{sellerId}")
    public ResponseEntity<ResponseFormat<SellerOrderListResponseDto>> getSellerOrderListForAdmin(
            @PathVariable Long sellerId, @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestParam String orderDate, @RequestParam String productId){
        // TODO 관리자만 사용 가능한 API임
        return ResponseEntity.ok().body(ResponseFormat.<SellerOrderListResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문내역 조회 완료")
                .data(orderService.getSellerOrderList(sellerId, orderDate, productId, null, pageable))
        .build());
    }


    @PatchMapping("/delivery/{deliveryId}")
    public ResponseEntity<ResponseFormat<Void>> addDeliveryCode(@PathVariable long deliveryId, @RequestBody DeliveryDto deliveryDto){
        // TODO 셀러만 사용 가능한 API임
        orderService.addDeliveryCode(deliveryId,deliveryDto.getDeliveryCode());
        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("운송장 등록완료")
        .build());
    }

    @PatchMapping("/delivery-confirm/{deliveryId}")
    public ResponseEntity<ResponseFormat<Void>> confirmDelivery(@PathVariable Long deliveryId){
        // TODO 셀러만 사용 가능한 API임
        orderService.confirmDelivery(deliveryId);
        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("배송 완료 상태 변경 완료")
        .build());
    }

    @PatchMapping("/product-order-confirm/{productOrderId}")
    public ResponseEntity<ResponseFormat<ProductOrderConfirmResponseDto>> confirmProductOrder(@PathVariable Long productOrderId){
        // TODO 유저만 사용 가능한 API임
        return ResponseEntity.ok().body(ResponseFormat.<ProductOrderConfirmResponseDto>builder()
                .code(org.springframework.http.HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 확정 완료")
                .data(
                        ProductOrderConfirmResponseDto.builder()
                                .point(orderService.confirmProductOrder(productOrderId))
                        .build()
                )
        .build());
    }

    @PostMapping("/order-cancel")
    public ResponseEntity<ResponseFormat<Void>> cancelProductOrder(@RequestBody OrderCancelRequestDto orderCancelRequestDto){
        // TODO 유저만 사용 가능한 API임
        orderService.cancelOrder(orderCancelRequestDto.getOrdersId());

        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("상품주문 취소 완료")
        .build());
    }

    @PostMapping("/product-order-cancel")
    public ResponseEntity<ResponseFormat<Void>> cancelOrder(@RequestBody ProductOrderCancelRequestDto productOrderCancelRequestDto){
        // TODO 유저만 사용 가능한 API임
        orderService.cancelProductOrder(productOrderCancelRequestDto.getProductOrderId());

        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 취소 완료")
        .build());
    }
}