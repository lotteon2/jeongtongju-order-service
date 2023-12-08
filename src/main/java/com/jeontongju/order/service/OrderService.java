package com.jeontongju.order.service;

import com.jeontongju.order.domain.Delivery;
import com.jeontongju.order.domain.Orders;
import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.dto.temp.AuctionOrderDto;
import com.jeontongju.order.dto.temp.OrderCancelDto;
import com.jeontongju.order.dto.temp.OrderConfirmDto;
import com.jeontongju.order.dto.temp.PaymentInfoDto;
import com.jeontongju.order.enums.ProductOrderStatusEnum;
import com.jeontongju.order.exception.CancelProductOrderException;
import com.jeontongju.order.exception.DeliveryIdNotFoundException;
import com.jeontongju.order.exception.DeliveryStatusException;
import com.jeontongju.order.exception.DuplicateDeliveryCodeException;
import com.jeontongju.order.exception.FeignServerNotAvailableException;
import com.jeontongju.order.exception.InvalidOrderCancellationException;
import com.jeontongju.order.exception.OrderIdNotFoundException;
import com.jeontongju.order.exception.OrderStatusException;
import com.jeontongju.order.exception.ProductOrderIdNotFoundException;
import com.jeontongju.order.feign.ConsumerFeignServiceClient;
import com.jeontongju.order.feign.PaymentFeignServiceClient;
import com.jeontongju.order.kafka.KafkaProcessor;
import com.jeontongju.order.repository.DeliveryRepository;
import com.jeontongju.order.repository.OrdersRepository;
import com.jeontongju.order.repository.ProductOrderRepository;
import com.jeontongju.order.util.KafkaTopicNameInfo;
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
import java.util.UUID;

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
    private final KafkaProcessor<OrderCancelDto> orderCancelDtoKafkaTemplate;

    @Transactional
    public void createOrder(OrderInfoDto orderInfoDto) {
        long point = orderInfoDto.getUserPointUpdateDto().getPoint() != null ? orderInfoDto.getUserPointUpdateDto().getPoint() : 0L;
        long couponAmount = orderInfoDto.getUserCouponUpdateDto().getCouponAmount() != null ? orderInfoDto.getUserCouponUpdateDto().getCouponAmount() : 0L;
        OrderCreationDto orderCreationDto = orderInfoDto.getOrderCreationDto();

        // 주문 테이블 생성
        Orders orders = Orders.builder().ordersId(orderInfoDto.getOrderCreationDto().getOrderId()).consumerId(orderCreationDto.getConsumerId())
                .orderDate(orderCreationDto.getOrderDate()).totalPrice(orderCreationDto.getTotalPrice()).build();
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
            throw new FeignServerNotAvailableException("결제 서버에서 예외가 발생했습니다.");
        }
    }

    @Transactional
    public void addDeliveryCode(Long deliveryId, String deliveryCode){
        Delivery delivery = getDelivery(deliveryId);
        if(delivery.getDeliveryCode() != null){
            throw new DuplicateDeliveryCodeException("이미 운송장 번호가 존재합니다.");
        }
        delivery.addDeliveryCode(deliveryCode);
    }

    @Transactional
    public void confirmDelivery(Long deliveryId){
        Delivery delivery = getDelivery(deliveryId);
        if(delivery.getDeliveryStatus() != ProductOrderStatusEnum.SHIPPING){
            throw new DeliveryStatusException("배송 완료 설정은 배송중인 상품만 가능합니다.");
        }
        delivery.changeDeliveryConfirmStatus();
    }

    @Transactional
    public long confirmProductOrder(Long productOrderId){
        ProductOrder productOrder = productOrderRepository.findById(productOrderId).orElseThrow(() -> new ProductOrderIdNotFoundException("해당 상품코드가 존재하지 않습니다."));
        if(getDelivery(productOrder.getDelivery().getDeliveryId()).getDeliveryStatus() != ProductOrderStatusEnum.COMPLETED){ throw new DeliveryStatusException("구매 확정은 배송완료 상품에 대해서만 가능합니다."); }
        productOrder.changeOrderStatusToConfirmStatus();
        FeignFormat<Long> orderConfirmPoint = consumerFeignServiceClient.getOrderConfirmPoint(OrderConfirmDto.builder().productAmount(productOrder.getProductRealAmount()).build());
        if(orderConfirmPoint.getCode() != 200){ throw new FeignServerNotAvailableException("유저 서버에서 예외가 발생했습니다."); }
        return orderConfirmPoint.getData();
    }

    @Transactional
    public void createAuctionOrder(AuctionOrderDto auctionOrderDto){
        Orders orders = Orders.builder().ordersId(UUID.randomUUID().toString()).consumerId(auctionOrderDto.getConsumerId()).orderDate(auctionOrderDto.getOrderDate()).
                totalPrice(auctionOrderDto.getTotalPrice()) .isAuction(true).build();
        ordersRepository.save(orders);

        ProductOrder productOrder = ProductOrder.builder().orders(orders).productId(auctionOrderDto.getProductId()).productName(auctionOrderDto.getProductName())
                .productCount(auctionOrderDto.getProductCount()).productPrice(auctionOrderDto.getProductPrice()).productRealAmount(auctionOrderDto.getProductPrice())
                .sellerId(auctionOrderDto.getSellerId()).sellerName(auctionOrderDto.getSellerName()).productImg(auctionOrderDto.getProductImg()).build();
        productOrderRepository.save(productOrder);

        Delivery delivery = Delivery.builder().productOrder(productOrder).recipientName(auctionOrderDto.getRecipientName())
                .recipientPhoneNumber(auctionOrderDto.getRecipientPhoneNumber()).basicAddress(auctionOrderDto.getBasicAddress())
                .addressDetail(auctionOrderDto.getAddressDetail()).zonecode(auctionOrderDto.getZonecode()).build();
        deliveryRepository.save(delivery);
    }

    @Transactional
    public void cancelOrder(String orderId){
        Orders orders = ordersRepository.findById(orderId).orElseThrow(() -> new OrderIdNotFoundException("해당 주문이 존재하지 않습니다."));
        if(orders.isCancelledOrAuction()){ throw new OrderStatusException("취소된 주문이거나 경매 주문 입니다."); }
        orders.changeProductOrderStatusToCancelStatus();

        for(ProductOrder productOrder : orders.getProductOrders()){
            if(productOrder.getProductOrderStatus() != ProductOrderStatusEnum.ORDER){
                throw new InvalidOrderCancellationException("주문 취소는 모든 상품들이 주문완료 상태여야 합니다.");
            }
            productOrder.changeOrderStatusToCancelStatus();
        }

        PaymentInfoDto paymentInfo = getPaymentInfo(orders);

        orderCancelDtoKafkaTemplate.send(getOrderCancelTopicName(paymentInfo.getMinusPointAmount(), paymentInfo.getCouponCode()),
                OrderCancelDto.builder().consumerId(orders.getConsumerId()).ordersId(orders.getOrdersId())
                        .couponCode(paymentInfo.getCouponCode()).point(paymentInfo.getMinusPointAmount()).cancelAmount(null).build());
    }

    @Transactional
    public void cancelProductOrder(Long productOrderId){
        ProductOrder productOrder = productOrderRepository.findById(productOrderId).orElseThrow(() -> new ProductOrderIdNotFoundException("해당 상품이 존재하지 않습니다."));

        if(productOrder.getProductOrderStatus() != ProductOrderStatusEnum.ORDER){
            throw new CancelProductOrderException("주문완료 상태인 상품만 취소할 수 있습니다.");
        }
        productOrder.changeOrderStatusToCancelStatus();

        Orders orders = productOrder.getOrders();
        if(orders.isCancelledOrAuction()){ throw new OrderStatusException("취소된 주문이거나 경매 주문 입니다."); }

        String couponCode = null;

        if(orders.getProductOrders().stream().allMatch(product -> product.getProductOrderStatus() == ProductOrderStatusEnum.CANCEL)){
            PaymentInfoDto paymentInfo = getPaymentInfo(orders);
            couponCode = paymentInfo.getCouponCode();
        }

        OrderCancelDto orderCancelDto = OrderCancelDto.builder().consumerId(orders.getConsumerId()).ordersId(orders.getOrdersId())
                .couponCode(couponCode).point(productOrder.getProductRealPointAmount()).cancelAmount(productOrder.getProductRealAmount()).build();

        orderCancelDtoKafkaTemplate.send(getOrderCancelTopicName(productOrder.getProductRealPointAmount(), couponCode), orderCancelDto);
    }

    private PaymentInfoDto getPaymentInfo(Orders orders) {
        FeignFormat<PaymentInfoDto> paymentInfo = paymentFeignServiceClient.getPaymentInfo(orders.getOrdersId());
        if(paymentInfo.getCode() != 200){
            throw new FeignServerNotAvailableException("결제 서버에서 예외가 발생했습니다.");
        }
        return paymentInfo.getData();
    }

    private String getOrderCancelTopicName(Long point, String couponCode) {
        String topicName;
        if(point!=null && point>0){
            topicName = KafkaTopicNameInfo.CANCEL_ORDER_POINT;
        }else if(couponCode!=null){
            topicName = KafkaTopicNameInfo.CANCEL_ORDER_COUPON;
        }else{
            topicName = KafkaTopicNameInfo.CANCEL_PAYMENT;
        }
        return topicName;
    }

    private Delivery getDelivery(Long deliveryId){
        return deliveryRepository.findById(deliveryId).orElseThrow(() -> new DeliveryIdNotFoundException("잘못된 요청입니다."));
    }

    public ProductOrderStatusEnum getDeliveryStatus(Long productOrderId){
        ProductOrder productOrder = productOrderRepository.findById(productOrderId).orElseThrow(() -> new RuntimeException(""));
        Orders orders = ordersRepository.findById(productOrder.getOrders().getOrdersId()).orElseThrow(()->new RuntimeException(""));
        if(orders.getIsAuction()){
            throw new RuntimeException("");
        }

        return productOrder.getDelivery().getDeliveryStatus();
    }
}