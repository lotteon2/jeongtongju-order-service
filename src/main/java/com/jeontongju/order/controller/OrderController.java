package com.jeontongju.order.controller;

import com.jeontongju.order.dto.DeliveryDto;
import com.jeontongju.order.dto.OrderCancelRequestDto;
import com.jeontongju.order.dto.ProductOrderCancelRequestDto;
import com.jeontongju.order.dto.response.admin.DashboardResponseDtoForAdmin;
import com.jeontongju.order.dto.response.admin.SettlementForAdmin;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDto;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDtoForAdmin;
import com.jeontongju.order.dto.response.consumer.OrderStatusDto;
import com.jeontongju.order.dto.response.consumer.ProductOrderConfirmResponseDto;
import com.jeontongju.order.dto.response.seller.DashboardResponseDtoForSeller;
import com.jeontongju.order.dto.response.seller.SellerOrderListResponseDto;
import com.jeontongju.order.dto.response.seller.SettlementForSeller;
import com.jeontongju.order.enums.ProductOrderStatusEnum;
import com.jeontongju.order.exception.InvalidPermissionException;
import com.jeontongju.order.feign.ProductFeignServiceClient;
import com.jeontongju.order.service.OrderService;
import com.jeontongju.order.util.ExcelWriterUtil;
import io.github.bitbox.bitbox.dto.ResponseFormat;
import io.github.bitbox.bitbox.enums.MemberRoleEnum;
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

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;
    private final ProductFeignServiceClient productFeignServiceClient;
    private final ExcelWriterUtil excelWriterUtil;

    @GetMapping("/order/consumer")
    public ResponseEntity<ResponseFormat<ConsumerOrderListResponseDto>> getConsumerOrderList(
            @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestHeader Long memberId, @RequestHeader MemberRoleEnum memberRole, @RequestParam(required = false) String isAuction){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_CONSUMER);
        Boolean auctionFlag = null;
        if(isAuction.equals("true")){
            auctionFlag = true;
        }else if(isAuction.equals("false")){
            auctionFlag = false;
        }

        return ResponseEntity.ok().body(ResponseFormat.<ConsumerOrderListResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 내역 조회 성공")
                .data(orderService.getConsumerOrderList(memberId, auctionFlag, pageable))
        .build());
    }

    @GetMapping("/order/status")
    public ResponseEntity<ResponseFormat<List<OrderStatusDto>>> getConsumerStatus(){
        return ResponseEntity.ok().body(ResponseFormat.<List<OrderStatusDto>>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 내역 조회 성공")
                .data(OrderStatusDto.getOrderStatus())
        .build());
    }

    @GetMapping("/order/consumer/{consumerId}")
    public ResponseEntity<ResponseFormat<ConsumerOrderListResponseDtoForAdmin>> getConsumerOrderListForAdmin(
            @RequestHeader MemberRoleEnum memberRole,
            @PathVariable Long consumerId, @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_ADMIN);
        return ResponseEntity.ok().body(ResponseFormat.<ConsumerOrderListResponseDtoForAdmin>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문내역 조회 완료")
                .data(orderService.getConsumerOrderList(consumerId, pageable))
        .build());
    }

    @GetMapping("/order/seller")
    public ResponseEntity<ResponseFormat<SellerOrderListResponseDto>> getSellerOrderList(
            @PageableDefault(sort = {"sellerId", "orderDate"}, direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader MemberRoleEnum memberRole,
            @RequestHeader Long memberId, @RequestParam String startDate, @RequestParam String endDate,
            @RequestParam String productId, @RequestParam String productStatus, @RequestParam boolean isDeliveryCodeNull){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_SELLER);
        ProductOrderStatusEnum productOrderStatusEnum = null;
        if(!productStatus.equals("null")){
            productOrderStatusEnum = ProductOrderStatusEnum.valueOf(productStatus);
        }

        return ResponseEntity.ok().body(ResponseFormat.<SellerOrderListResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문내역 조회 완료")
                .data(orderService.getSellerOrderList(memberId, startDate, endDate , productId, productOrderStatusEnum,isDeliveryCodeNull, pageable))
        .build());
    }

    @GetMapping("/order/seller/{sellerId}")
    public ResponseEntity<ResponseFormat<SellerOrderListResponseDto>> getSellerOrderListForAdmin(
            @PathVariable Long sellerId,@PageableDefault(sort = {"sellerId", "orderDate"}, direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader MemberRoleEnum memberRole, @RequestParam String productStatus, @RequestParam String startDate, @RequestParam String endDate){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_ADMIN);
        ProductOrderStatusEnum productOrderStatusEnum = null;
        if(!productStatus.equals("null")){
            productOrderStatusEnum = ProductOrderStatusEnum.valueOf(productStatus);
        }
        return ResponseEntity.ok().body(ResponseFormat.<SellerOrderListResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문내역 조회 완료")
                .data(orderService.getSellerOrderList(sellerId, startDate, endDate , "null",productOrderStatusEnum,false, pageable))
        .build());
    }

    @GetMapping("/settlement/seller/{sellerId}")
    public ResponseEntity<ResponseFormat<List<SettlementForAdmin>>> getSettlementForAdmin(@PathVariable Long sellerId, @RequestParam Long year,
                                                                                         @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_ADMIN);
        return ResponseEntity.ok().body(ResponseFormat.<List<SettlementForAdmin>>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("특정 셀러 정산 내역 조회 완료")
                .data(orderService.getSettlementForAdmin(sellerId,year))
        .build());
    }

    @GetMapping("/settlement/seller/year/{year}/month/{month}")
    public ResponseEntity<ResponseFormat<SettlementForSeller>> getSettlementForSeller(@PathVariable Long year, @PathVariable Long month,
                                                                                      @RequestHeader Long memberId, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_SELLER);
        return ResponseEntity.ok().body(ResponseFormat.<SettlementForSeller>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("나의 정산 내역 조회 완료")
                .data(orderService.getSettlementForSeller(memberId,year,month))
        .build());
    }

    @GetMapping("/seller/dashboard")
    public ResponseEntity<ResponseFormat<DashboardResponseDtoForSeller>> getDashboardForSeller(@RequestHeader Long memberId, @RequestHeader MemberRoleEnum memberRole, @RequestParam String date){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_SELLER);

        return ResponseEntity.ok().body(ResponseFormat.<DashboardResponseDtoForSeller>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("대시보드 조회 완료")
                .data(orderService.getDashboardForSeller(memberId,date, productFeignServiceClient.getStockUnderFive(memberId).getData()))
        .build());
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<ResponseFormat<DashboardResponseDtoForAdmin>> getDashboardForAdmin(@RequestHeader MemberRoleEnum memberRole, @RequestParam String date){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_ADMIN);

        return ResponseEntity.ok().body(ResponseFormat.<DashboardResponseDtoForAdmin>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("대시보드 조회 완료")
                .data(orderService.getDashboardForAdmin(date))
        .build());
    }

    @GetMapping("/all-seller-settlement")
    public ResponseEntity<ResponseFormat<String>> downloadSettlementFile(@RequestParam Long year, @RequestParam Long month){
        return ResponseEntity.ok().body(ResponseFormat.<String>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("모든 셀러 정산내역 액셀 다운로드 성공")
                .data(excelWriterUtil.createExcelFileResponse(orderService.getAllSellerSettlement(year,month)))
        .build());
    }

    @PatchMapping("/delivery/{deliveryId}")
    public ResponseEntity<ResponseFormat<Void>> addDeliveryCode(@PathVariable long deliveryId, @RequestHeader MemberRoleEnum memberRole, @Valid @RequestBody DeliveryDto deliveryDto){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_SELLER);
        orderService.addDeliveryCode(deliveryId,deliveryDto.getDeliveryCode());
        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("운송장 등록완료")
        .build());
    }

    @PatchMapping("/delivery-confirm/{deliveryId}")
    public ResponseEntity<ResponseFormat<Void>> confirmDelivery(@PathVariable Long deliveryId, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_SELLER);
        orderService.confirmDelivery(deliveryId);
        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("배송 완료 상태 변경 완료")
        .build());
    }

    @PatchMapping("/product-order-confirm/{productOrderId}")
    public ResponseEntity<ResponseFormat<ProductOrderConfirmResponseDto>> confirmProductOrder(@PathVariable Long productOrderId, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_CONSUMER);
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
    public ResponseEntity<ResponseFormat<Void>> cancelProductOrder(@Valid @RequestBody OrderCancelRequestDto orderCancelRequestDto, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_CONSUMER);
        orderService.cancelOrder(orderCancelRequestDto.getOrdersId());

        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 취소 완료")
        .build());
    }

    @PostMapping("/product-order-cancel")
    public ResponseEntity<ResponseFormat<Void>> cancelOrder(@Valid @RequestBody ProductOrderCancelRequestDto productOrderCancelRequestDto, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_CONSUMER);
        orderService.cancelProductOrder(productOrderCancelRequestDto.getProductOrderId());

        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("상품주문 취소 완료")
        .build());
    }

    private void checkMemberRole(MemberRoleEnum currentRole, MemberRoleEnum targetRole) {
        if(currentRole != targetRole){
            throw new InvalidPermissionException("권한이 부족 합니다.");
        }
    }
}
