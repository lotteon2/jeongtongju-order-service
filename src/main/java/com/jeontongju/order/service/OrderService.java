package com.jeontongju.order.service;

import com.jeontongju.order.domain.Delivery;
import com.jeontongju.order.domain.Orders;
import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.dto.temp.AuctionOrderDto;
import com.jeontongju.order.dto.temp.OrderConfirmDto;
import com.jeontongju.order.enums.DeliveryStatusEnum;
import com.jeontongju.order.exception.DeliveryIdNotFoundException;
import com.jeontongju.order.exception.DuplicateDeliveryCodeException;
import com.jeontongju.order.exception.ProductOrderIdNotFoundException;
import com.jeontongju.order.feign.ConsumerFeignServiceClient;
import com.jeontongju.order.feign.PaymentFeignServiceClient;
import com.jeontongju.order.repository.DeliveryRepository;
import com.jeontongju.order.repository.OrdersRepository;
import com.jeontongju.order.repository.ProductOrderRepository;
import com.jeontongju.payment.dto.temp.OrderCreationDto;
import com.jeontongju.payment.dto.temp.OrderInfoDto;
import com.jeontongju.payment.dto.temp.ProductInfoDto;
import com.jeontongju.payment.enums.temp.FeignFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final DeliveryRepository deliveryRepository;
    private final OrdersRepository ordersRepository;
    private final ProductOrderRepository productOrderRepository;
    private final PaymentFeignServiceClient paymentFeignServiceClient;
    private final ConsumerFeignServiceClient consumerFeignServiceClient;

    @Transactional
    public void createOrder(OrderInfoDto orderInfoDto) {
        long point = orderInfoDto.getUserPointUpdateDto().getPoint() != null ? orderInfoDto.getUserPointUpdateDto().getPoint() : 0L;
        long couponAmount = orderInfoDto.getUserCouponUpdateDto().getCouponAmount() != null ? orderInfoDto.getUserCouponUpdateDto().getCouponAmount() : 0L;
        OrderCreationDto orderCreationDto = orderInfoDto.getOrderCreationDto();

        // 주문 테이블 생성
        Orders orders = Orders.builder().consumerId(orderCreationDto.getConsumerId()).orderDate(orderCreationDto.getOrderDate()).totalPrice(orderCreationDto.getTotalPrice()).build();
        ordersRepository.save(orders);

        List<ProductOrder> productOrderList = new ArrayList<>();
        List<Delivery> deliveryList = new ArrayList<>();

        for (ProductInfoDto productInfoDto : orderCreationDto.getProductInfoDtoList()) {
            long productPrice = productInfoDto.getProductCount() * productInfoDto.getProductPrice();
            double percent = (double) productPrice / orderCreationDto.getTotalPrice();
            long minusPoint = Math.round(point * percent);
            long minusCoupon = Math.round(couponAmount * percent);

            // 주문 상세 테이블 생성
            ProductOrder productOrder = ProductOrder.builder().orders(orders).productId(productInfoDto.getProductId()).productName(productInfoDto.getProductName())
                    .productCount(productInfoDto.getProductCount()).productPrice(productInfoDto.getProductPrice()).productRealAmount(productPrice - minusPoint - minusCoupon)
                    .productRealPointAmount(minusPoint).productRealCouponAmount(minusCoupon).sellerId(productInfoDto.getSellerId())
                    .sellerName(productInfoDto.getSellerName()).productImg(productInfoDto.getProductImg()).build();
            productOrderList.add(productOrder);

            // 배달 테이블 생성
            Delivery delivery = Delivery.builder().productOrder(productOrder).recipientName(orderCreationDto.getRecipientName())
                    .recipientPhoneNumber(orderCreationDto.getRecipientPhoneNumber()).basicAddress(orderCreationDto.getBasicAddress())
                    .addressDetail(orderCreationDto.getAddressDetail()).zonecode(orderCreationDto.getZoneCode()).build();
            deliveryList.add(delivery);
        }

        productOrderRepository.saveAll(productOrderList);
        deliveryRepository.saveAll(deliveryList);

        // 결제 Feign
        FeignFormat<Void> productInfo = paymentFeignServiceClient.approveKakaopay(orderInfoDto);
        if(productInfo.getCode() != 200){
            throw new RuntimeException("결제 서버에서 예외가 발생했습니다.");
        }
    }

    @Transactional
    public void addDeliveryCode(long deliveryId, String deliveryCode){
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(() -> new DeliveryIdNotFoundException("잘못된 요청입니다."));
        if(delivery.getDeliveryCode() != null){
            throw new DuplicateDeliveryCodeException("이미 운송장 번호가 존재합니다.");
        }
        delivery.addDeliveryCode(deliveryCode);
    }

    @Transactional
    public long confirmProductOrder(long productOrderId){
        ProductOrder productOrder = productOrderRepository.findById(productOrderId).orElseThrow(() -> new ProductOrderIdNotFoundException("해당 상품코드가 존재하지 않습니다."));
        productOrder.changeOrderedStatusToConfirmStatus();
        FeignFormat<Long> orderConfirmPoint = consumerFeignServiceClient.getOrderConfirmPoint(OrderConfirmDto.builder().productAmount(productOrder.getProductRealAmount()).build());
        if(orderConfirmPoint.getCode() != 200){
            throw new RuntimeException("유저 서버에서 예외가 발생했습니다.");
        }
        return orderConfirmPoint.getData();
    }

    @Transactional
    public void createAuctionOrder(AuctionOrderDto auctionOrderDto){
        Orders orders = Orders.builder().consumerId(auctionOrderDto.getConsumerId()).orderDate(auctionOrderDto.getOrderDate()).
                totalPrice(auctionOrderDto.getTotalPrice()) .isAuction(true).build();

        ProductOrder productOrder = ProductOrder.builder().orders(orders).productId(auctionOrderDto.getProductId()).productName(auctionOrderDto.getProductName())
                .productCount(auctionOrderDto.getProductCount()).productPrice(auctionOrderDto.getProductPrice()).productRealAmount(auctionOrderDto.getProductPrice())
                .sellerId(auctionOrderDto.getSellerId()).sellerName(auctionOrderDto.getSellerName()).productImg(auctionOrderDto.getProductImg()).build();

        Delivery delivery = Delivery.builder().productOrder(productOrder).recipientName(auctionOrderDto.getRecipientName())
                .recipientPhoneNumber(auctionOrderDto.getRecipientPhoneNumber()).basicAddress(auctionOrderDto.getBasicAddress())
                .addressDetail(auctionOrderDto.getAddressDetail()).zonecode(auctionOrderDto.getZonecode()).build();

        ordersRepository.save(orders);
        productOrderRepository.save(productOrder);
        deliveryRepository.save(delivery);
    }

    public DeliveryStatusEnum getDeliveryStatus(long productOrderId){
        ProductOrder productOrder = productOrderRepository.findById(productOrderId).orElseThrow(() -> new RuntimeException(""));
        Orders orders = ordersRepository.findById(productOrder.getOrders().getOrdersId()).orElseThrow(()->new RuntimeException(""));
        if(orders.getIsAuction()){
            throw new RuntimeException("");
        }

        return productOrder.getDelivery().getDeliveryStatus();
    }
}