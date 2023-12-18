package com.jeontongju.order.service;

import com.jeontongju.order.domain.Delivery;
import com.jeontongju.order.domain.Orders;
import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.dto.response.common.OrderResponseCommonDto;
import com.jeontongju.order.dto.response.common.PageInfoDto;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDto;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDtoForAdmin;
import com.jeontongju.order.dto.response.consumer.DeliveryResponseDto;
import com.jeontongju.order.dto.response.consumer.OrderListDto;
import com.jeontongju.order.dto.response.seller.SellerOrderListDto;
import com.jeontongju.order.dto.response.seller.SellerOrderListResponseDto;
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
import com.jeontongju.order.repository.criteria.OrderSpecifications;
import com.jeontongju.order.repository.response.OrderResponseDto;
import com.jeontongju.order.repository.response.ProductResponseDto;
import io.github.bitbox.bitbox.dto.AddressDto;
import io.github.bitbox.bitbox.dto.AuctionOrderDto;
import io.github.bitbox.bitbox.dto.FeignFormat;
import io.github.bitbox.bitbox.dto.OrderCancelDto;
import io.github.bitbox.bitbox.dto.OrderConfirmDto;
import io.github.bitbox.bitbox.dto.OrderCreationDto;
import io.github.bitbox.bitbox.dto.OrderInfoDto;
import io.github.bitbox.bitbox.dto.PaymentInfoDto;
import io.github.bitbox.bitbox.dto.ProductInfoDto;
import io.github.bitbox.bitbox.dto.ProductUpdateDto;
import io.github.bitbox.bitbox.util.KafkaTopicNameInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                .orderDate(orderCreationDto.getOrderDate()).totalPrice(orderCreationDto.getTotalPrice()).paymentMethod(orderInfoDto.getOrderCreationDto().getPaymentMethod()).build();
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
                    .productRealPointAmount(minusPoint).productRealCouponAmount(minusCoupon).sellerId(productInfoDto.getSellerId()).orderDate(orderCreationDto.getOrderDate())
                    .sellerName(productInfoDto.getSellerName()).productThumbnailImageUrl(productInfoDto.getProductImg()).consumerId(orderCreationDto.getConsumerId()).build();
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
    public void createAuctionOrder(AuctionOrderDto auctionOrderDto, AddressDto addressDto){
        Orders orders = Orders.builder().ordersId(UUID.randomUUID().toString()).consumerId(auctionOrderDto.getConsumerId()).orderDate(auctionOrderDto.getOrderDate())
                        .totalPrice(auctionOrderDto.getTotalPrice()).isAuction(true).paymentMethod(auctionOrderDto.getPaymentMethod()).build();
        ordersRepository.save(orders);

        ProductOrder productOrder = ProductOrder.builder().orders(orders).productId(auctionOrderDto.getProductId()).productName(auctionOrderDto.getProductName())
                .productCount(auctionOrderDto.getProductCount()).productPrice(auctionOrderDto.getProductPrice()).productRealAmount(auctionOrderDto.getProductPrice())
                .sellerId(auctionOrderDto.getSellerId()).sellerName(auctionOrderDto.getSellerName()).productThumbnailImageUrl(auctionOrderDto.getProductImg()).consumerId(auctionOrderDto.getConsumerId())
                .orderDate(auctionOrderDto.getOrderDate()).build();
        productOrderRepository.save(productOrder);

        Delivery delivery = Delivery.builder().productOrder(productOrder).recipientName(addressDto.getRecipientName())
                .recipientPhoneNumber(addressDto.getRecipientPhoneNumber()).basicAddress(addressDto.getBasicAddress())
                .addressDetail(addressDto.getAddressDetail()).zonecode(addressDto.getZonecode()).build();
        deliveryRepository.save(delivery);
    }

    @Transactional
    public void cancelOrder(String orderId){
        Orders orders = ordersRepository.findById(orderId).orElseThrow(() -> new OrderIdNotFoundException("해당 주문이 존재하지 않습니다."));
        if(orders.isCancelledOrAuction()){ throw new OrderStatusException("취소된 주문이거나 경매 주문 입니다."); }
        orders.changeProductOrderStatusToCancelStatus();

        List<ProductUpdateDto> productUpdateDtoList = new ArrayList<>();
        for(ProductOrder productOrder : orders.getProductOrders()){
            if(productOrder.getProductOrderStatus() != ProductOrderStatusEnum.ORDER){
                throw new InvalidOrderCancellationException("주문 취소는 모든 상품들이 주문완료 상태여야 합니다.");
            }
            if(productOrder.getDelivery().getDeliveryStatus() != null){
                throw new InvalidOrderCancellationException("주문 취소를 하려면 모든 상품들은 배송상태가 비어있어야 합니다.");
            }
            productOrder.changeOrderStatusToCancelStatus();
            productUpdateDtoList.add(ProductUpdateDto.builder().productId(productOrder.getProductId()).productCount(productOrder.getProductCount()).build());
        }

        PaymentInfoDto paymentInfo = getPaymentInfo(orders);

        orderCancelDtoKafkaTemplate.send(getOrderCancelTopicName(paymentInfo.getMinusPointAmount(), paymentInfo.getCouponCode()),
                        OrderCancelDto.builder().consumerId(orders.getConsumerId()).ordersId(orders.getOrdersId())
                        .couponCode(paymentInfo.getCouponCode()).point(paymentInfo.getMinusPointAmount()).cancelAmount(null).productUpdateDtoList(productUpdateDtoList).build());
    }

    @Transactional
    public void cancelProductOrder(Long productOrderId){
        ProductOrder productOrder = productOrderRepository.findById(productOrderId).orElseThrow(() -> new ProductOrderIdNotFoundException("해당 상품이 존재하지 않습니다."));

        if(productOrder.getProductOrderStatus() != ProductOrderStatusEnum.ORDER){ throw new CancelProductOrderException("주문완료 상태인 상품만 취소할 수 있습니다.");}
        if(productOrder.getDelivery().getDeliveryStatus() != null){throw new CancelProductOrderException("배송중인 상품은 취소가 불가능합니다.");}
        productOrder.changeOrderStatusToCancelStatus();

        Orders orders = productOrder.getOrders();
        if(orders.isCancelledOrAuction()){ throw new OrderStatusException("취소된 주문이거나 경매 주문 입니다."); }

        String couponCode = null;

        if(orders.getProductOrders().stream().allMatch(product -> product.getProductOrderStatus() == ProductOrderStatusEnum.CANCEL)){
            couponCode = getPaymentInfo(orders).getCouponCode();
            orders.changeProductOrderStatusToCancelStatus();
        }

        OrderCancelDto orderCancelDto = OrderCancelDto.builder().consumerId(orders.getConsumerId()).ordersId(orders.getOrdersId())
                .couponCode(couponCode).point(productOrder.getProductRealPointAmount()).cancelAmount(productOrder.getProductRealAmount())
                .productUpdateDtoList(List.of(new ProductUpdateDto(productOrder.getProductId(), productOrder.getProductCount()))).build();

        orderCancelDtoKafkaTemplate.send(getOrderCancelTopicName(productOrder.getProductRealPointAmount(), couponCode), orderCancelDto);
    }

    public ConsumerOrderListResponseDto getConsumerOrderList(Long consumerId, Boolean isAuction, Pageable pageable){
        Page<Orders> ordersWithPage = ordersRepository.findAll(OrderSpecifications.buildConsumerOrderSpecification(consumerId, isAuction), pageable);

        List<OrderListDto> orderListDtos = new ArrayList<>();
        for(Orders orders : ordersWithPage.getContent()){
            OrderResponseDto orderResponseDto = OrderResponseDto.builder().ordersId(orders.getOrdersId()).orderDate(String.valueOf(orders.getOrderDate()))
                    .orderStatus(orders.getOrderStatus()).isAuction(orders.getIsAuction()).build();

            List<ProductResponseDto> productResponseDtoList = new ArrayList<>();
            DeliveryResponseDto deliveryResponseDto = null;

            for(ProductOrder productOrder : orders.getProductOrders()){
                Delivery delivery = productOrder.getDelivery();
                productResponseDtoList.add(ProductResponseDto.productOrderToProductResponseDto(productOrder, getProductOrderStatusEnum(productOrder, delivery)));
                if(deliveryResponseDto == null){ deliveryResponseDto = DeliveryResponseDto.DeliveryToDeliveryResponseDto(delivery); }
            }

            orderListDtos.add(OrderListDto.builder().order(orderResponseDto).product(productResponseDtoList).delivery(deliveryResponseDto).payment(getPaymentInfo(orders)).build());
        }

        ConsumerOrderListResponseDto consumerOrderListResponseDto = ConsumerOrderListResponseDto.builder().orderLists(orderListDtos).build();
        setPageableInfo(consumerOrderListResponseDto, ordersWithPage);
        return consumerOrderListResponseDto;
    }

    public ProductOrderStatusEnum getDeliveryStatus(Long productOrderId){
        ProductOrder productOrder = productOrderRepository.findById(productOrderId).orElseThrow(() -> new RuntimeException(""));
        Orders orders = ordersRepository.findById(productOrder.getOrders().getOrdersId()).orElseThrow(()->new RuntimeException(""));
        if(orders.getIsAuction()){
            throw new RuntimeException("");
        }

        return productOrder.getDelivery().getDeliveryStatus();
    }

    public SellerOrderListResponseDto getSellerOrderList(Long sellerId, String orderDate, String productId, boolean isDeliveryCodeNull, Pageable pageable){
        Page<ProductOrder> productOrdersWithPage = productOrderRepository.findAll(OrderSpecifications.buildSellerProductOrdersSpecification(sellerId, orderDate, productId, isDeliveryCodeNull), pageable);
        List<ProductOrder> productOrderList = productOrdersWithPage.getContent();

        List<SellerOrderListDto> sellerOrderListDtoList = new ArrayList<>();
        for(ProductOrder productOrder : productOrderList){
            Orders orders = productOrder.getOrders();
            Delivery delivery = productOrder.getDelivery();

            SellerOrderListDto sellerOrderDto = SellerOrderListDto.builder().deliveryId(delivery.getDeliveryId())
                    .deliveryCode(delivery.getDeliveryCode()).build();
            setOrderResponseInfo(sellerOrderDto, orders, productOrder, delivery);
            sellerOrderListDtoList.add(sellerOrderDto);
        }

        SellerOrderListResponseDto sellerOrderListResponseDto = SellerOrderListResponseDto.builder().content(sellerOrderListDtoList).build();
        setPageableInfo(sellerOrderListResponseDto, productOrdersWithPage);

        return sellerOrderListResponseDto;
    }

    public ConsumerOrderListResponseDtoForAdmin getConsumerOrderList(Long consumerId, Pageable pageable){
        Page<ProductOrder> productOrdersWithPage = productOrderRepository.findByConsumerId(consumerId, pageable);

        List<OrderResponseCommonDto> orderResponseCommonDtoList = new ArrayList<>();
        for(ProductOrder productOrder: productOrdersWithPage.getContent()){
            OrderResponseCommonDto orderResponseCommonDto = new OrderResponseCommonDto();
            setOrderResponseInfo(orderResponseCommonDto, productOrder.getOrders(), productOrder, productOrder.getDelivery());
            orderResponseCommonDtoList.add(orderResponseCommonDto);
        }

        ConsumerOrderListResponseDtoForAdmin consumerOrderListResponseDtoForAdmin = ConsumerOrderListResponseDtoForAdmin.builder().content(orderResponseCommonDtoList).build();
        setPageableInfo(consumerOrderListResponseDtoForAdmin, productOrdersWithPage);

        return consumerOrderListResponseDtoForAdmin;
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
            topicName = KafkaTopicNameInfo.CANCEL_ORDER_PAYMENT;
        }
        return topicName;
    }

    private Delivery getDelivery(Long deliveryId){
        return deliveryRepository.findById(deliveryId).orElseThrow(() -> new DeliveryIdNotFoundException("잘못된 요청입니다."));
    }

    private ProductOrderStatusEnum getProductOrderStatusEnum(ProductOrder productOrder, Delivery delivery) {
        ProductOrderStatusEnum productOrderStatus = productOrder.getProductOrderStatus();
        if(productOrderStatus == ProductOrderStatusEnum.ORDER && delivery.getDeliveryCode() != null){productOrderStatus = delivery.getDeliveryStatus();}
        return productOrderStatus;
    }

    private void setPageableInfo(PageInfoDto pageableInfo, Page<?> page){
        pageableInfo.setLast(page.isLast());
        pageableInfo.setTotalElements(page.getTotalElements());
        pageableInfo.setTotalPages(page.getTotalPages());
        pageableInfo.setSize(page.getSize());
        pageableInfo.setNumber(page.getNumber());
        pageableInfo.setSort(page.getSort());
        pageableInfo.setFirst(page.isFirst());
        pageableInfo.setNumberOfElements(page.getNumberOfElements());
        pageableInfo.setEmpty(page.isEmpty());
        pageableInfo.setPageable(page.getPageable());
    }

    private void setOrderResponseInfo(OrderResponseCommonDto orderResponseInfo, Orders orders, ProductOrder productOrder, Delivery delivery){
        orderResponseInfo.setOrdersId(orders.getOrdersId());
        orderResponseInfo.setProductId(productOrder.getProductId());
        orderResponseInfo.setProductName(productOrder.getProductName());
        orderResponseInfo.setProductCount(productOrder.getProductCount());
        orderResponseInfo.setProductTotalAmount(productOrder.getProductCount()*productOrder.getProductPrice());
        orderResponseInfo.setOrderDate(String.valueOf(productOrder.getOrderDate()));
        orderResponseInfo.setPaymentType(orders.getPaymentMethod());
        orderResponseInfo.setOrderStatus(getProductOrderStatusEnum(productOrder, delivery));
        orderResponseInfo.setIsAuction(orders.getIsAuction());
    }
}