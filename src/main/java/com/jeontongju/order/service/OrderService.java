package com.jeontongju.order.service;

import com.jeontongju.order.domain.Delivery;
import com.jeontongju.order.domain.Orders;
import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.dto.response.admin.AllSellerSettlementDtoForAdmin;
import com.jeontongju.order.dto.response.admin.DashboardResponseDtoForAdmin;
import com.jeontongju.order.dto.response.admin.ProductRank;
import com.jeontongju.order.dto.response.admin.SellerProductMonthDto;
import com.jeontongju.order.dto.response.admin.SellerRank;
import com.jeontongju.order.dto.response.admin.SellerRankMonthDto;
import com.jeontongju.order.dto.response.admin.SettlementForAdmin;
import com.jeontongju.order.dto.response.common.OrderResponseCommonDto;
import com.jeontongju.order.dto.response.common.PageInfoDto;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDto;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDtoForAdmin;
import com.jeontongju.order.dto.response.consumer.DeliveryResponseDto;
import com.jeontongju.order.dto.response.consumer.OrderListDto;
import com.jeontongju.order.dto.response.seller.DashboardResponseDtoForSeller;
import com.jeontongju.order.dto.response.seller.SellerOrderListDto;
import com.jeontongju.order.dto.response.seller.SellerOrderListResponseDto;
import com.jeontongju.order.dto.response.seller.SettlementForSeller;
import com.jeontongju.order.dto.response.seller.WeeklySales;
import com.jeontongju.order.enums.ProductOrderStatusEnum;
import com.jeontongju.order.exception.AuctionStatusException;
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
import com.jeontongju.order.repository.SettlementRepository;
import com.jeontongju.order.repository.criteria.OrderSpecifications;
import com.jeontongju.order.repository.response.MonthProductRankDto;
import com.jeontongju.order.repository.response.MonthSellerRankDto;
import com.jeontongju.order.repository.response.OrderResponseDto;
import com.jeontongju.order.repository.response.OrderStatusDtoForDashboard;
import com.jeontongju.order.repository.response.ProductResponseDto;
import com.jeontongju.order.repository.response.WeeklySalesDto;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final DeliveryRepository deliveryRepository;
    private final OrdersRepository ordersRepository;
    private final ProductOrderRepository productOrderRepository;
    private final SettlementRepository settlementRepository;
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
        if(productOrder.getOrders().getIsAuction()) { throw new AuctionStatusException("옥션 상품은 구매확정 할 수 없습니다."); }
        if(getDelivery(productOrder.getDelivery().getDeliveryId()).getDeliveryStatus() != ProductOrderStatusEnum.COMPLETED ||
                productOrder.getProductOrderStatus() == ProductOrderStatusEnum.CONFIRMED ){ throw new DeliveryStatusException("구매 확정은 배송완료 상품에 대해서만 가능합니다."); }
        productOrder.changeOrderStatusToConfirmStatus();
        FeignFormat<Long> orderConfirmPoint = consumerFeignServiceClient.getOrderConfirmPoint(OrderConfirmDto.builder().consumerId(productOrder.getConsumerId()).productAmount(productOrder.getProductRealAmount()).build());
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
        List<Long> productOrderIds = new ArrayList<>();
        for(ProductOrder productOrder : orders.getProductOrders()){
            if(productOrder.getProductOrderStatus() != ProductOrderStatusEnum.ORDER){
                throw new InvalidOrderCancellationException("주문 취소는 모든 상품들이 주문완료 상태여야 합니다.");
            }
            if(productOrder.getDelivery().getDeliveryStatus() != null){
                throw new InvalidOrderCancellationException("주문 취소를 하려면 모든 상품들은 배송상태가 비어있어야 합니다.");
            }

            productOrder.changeOrderStatusToCancelStatus();
            productOrderIds.add(productOrder.getProductOrderId());
            productUpdateDtoList.add(ProductUpdateDto.builder().productId(productOrder.getProductId()).productCount(productOrder.getProductCount()).build());
        }

        PaymentInfoDto paymentInfo = getPaymentInfo(orders);

        orderCancelDtoKafkaTemplate.send(getOrderCancelTopicName(paymentInfo.getMinusPointAmount(), paymentInfo.getCouponCode()),
                        OrderCancelDto.builder().consumerId(orders.getConsumerId()).ordersId(orders.getOrdersId())
                        .couponCode(paymentInfo.getCouponCode()).point(paymentInfo.getMinusPointAmount()).cancelAmount(null)
                        .cancelOrderId(orderId).cancelProductOrderId(productOrderIds).productUpdateDtoList(productUpdateDtoList).build());
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
        String orderId = null;
        if(orders.getProductOrders().stream().allMatch(product -> product.getProductOrderStatus() == ProductOrderStatusEnum.CANCEL)){
            couponCode = getPaymentInfo(orders).getCouponCode();
            orders.changeProductOrderStatusToCancelStatus();
            orderId = orders.getOrdersId();
        }

        OrderCancelDto orderCancelDto = OrderCancelDto.builder().consumerId(orders.getConsumerId()).ordersId(orders.getOrdersId())
                .couponCode(couponCode).point(productOrder.getProductRealPointAmount()).cancelAmount(productOrder.getProductRealAmount())
                .cancelOrderId(orderId).cancelProductOrderId(List.of(productOrderId))
                .productUpdateDtoList(List.of(new ProductUpdateDto(productOrder.getProductId(), productOrder.getProductCount()))).build();

        orderCancelDtoKafkaTemplate.send(getOrderCancelTopicName(productOrder.getProductRealPointAmount(), couponCode), orderCancelDto);
    }

    @Transactional
    public void revertOrderStatus(String orderId, List<Long> productOrderId){
        if(orderId != null){
            ordersRepository.findById(orderId).orElseThrow(()->new OrderIdNotFoundException("존재하지 않는 주문번호")).changeProductOrderStatusToNormalStatus();
        }

        for(Long id : productOrderId){
            productOrderRepository.findById(id).orElseThrow(()->new ProductOrderIdNotFoundException("존재하지 않는 상품 주문번호")).changeOrderStatusToOrderStatus();
        }
    }

    @Transactional
    public void updateProductOrderReviewStatus(Long productOrderId){
        productOrderRepository.findById(productOrderId).orElseThrow(()->new ProductOrderIdNotFoundException("존재하지 않는 상품 번호")).changeReviewWriteFlagToTrue();
    }

    public ConsumerOrderListResponseDto getConsumerOrderList(Long consumerId, Boolean isAuction, Pageable pageable){
        Page<Orders> ordersWithPage = ordersRepository.findAll(OrderSpecifications.buildConsumerOrderSpecification(consumerId, isAuction), pageable);

        List<OrderListDto> orderListDtos = new ArrayList<>();
        for(Orders orders : ordersWithPage.getContent()){
            boolean isAbleToCancel = true;
            List<ProductResponseDto> productResponseDtoList = new ArrayList<>();
            DeliveryResponseDto deliveryResponseDto = null;

            for(ProductOrder productOrder : orders.getProductOrders()){
                Delivery delivery = productOrder.getDelivery();
                ProductOrderStatusEnum productOrderStatusEnum = getProductOrderStatusEnum(productOrder, delivery);
                if(productOrderStatusEnum != ProductOrderStatusEnum.ORDER || orders.getIsAuction()){isAbleToCancel = false;}
                productResponseDtoList.add(ProductResponseDto.productOrderToProductResponseDto(orders, productOrder, productOrderStatusEnum));
                if(deliveryResponseDto == null){ deliveryResponseDto = DeliveryResponseDto.DeliveryToDeliveryResponseDto(delivery); }
            }

            OrderResponseDto orderResponseDto = OrderResponseDto.builder().ordersId(orders.getOrdersId()).orderDate(String.valueOf(orders.getOrderDate()))
                    .isAbleToCancel(isAbleToCancel).orderStatus(orders.getOrderStatus()).isAuction(orders.getIsAuction()).build();
            orderListDtos.add(OrderListDto.builder().order(orderResponseDto).product(productResponseDtoList).delivery(deliveryResponseDto).payment(getPaymentInfo(orders)).build());
        }

        ConsumerOrderListResponseDto consumerOrderListResponseDto = ConsumerOrderListResponseDto.builder().content(orderListDtos).build();
        setPageableInfo(consumerOrderListResponseDto, ordersWithPage);
        return consumerOrderListResponseDto;
    }

    public Boolean getDeliveryStatus(Long productOrderId){
        ProductOrder productOrder = productOrderRepository.findById(productOrderId).orElseThrow(() -> new RuntimeException(""));
        Orders orders = ordersRepository.findById(productOrder.getOrders().getOrdersId()).orElseThrow(()->new RuntimeException(""));
        if(orders.getIsAuction()){ throw new RuntimeException("");}

        LocalDateTime orderDate = productOrder.getOrderDate();
        LocalDateTime now = LocalDateTime.now();
        long secondsBetween = ChronoUnit.SECONDS.between(orderDate, now);
        boolean is14DaysPassed = secondsBetween >= (14 * 24 * 60 * 60);

        return (productOrder.getProductOrderStatus() == ProductOrderStatusEnum.CONFIRMED && !is14DaysPassed) && !productOrder.getReviewWriteFlag() && !orders.getIsAuction();
    }

    public SellerOrderListResponseDto getSellerOrderList(Long sellerId, String startDate, String endDate, String productId, ProductOrderStatusEnum productStatus,boolean isDeliveryCodeNull, Pageable pageable){
        Page<ProductOrder> productOrdersWithPage = productOrderRepository.findAll(OrderSpecifications.buildSellerProductOrdersSpecification(sellerId, startDate, endDate, productId, productStatus,isDeliveryCodeNull), pageable);
        List<ProductOrder> productOrderList = productOrdersWithPage.getContent();

        List<SellerOrderListDto> sellerOrderListDtoList = new ArrayList<>();
        for(ProductOrder productOrder : productOrderList){
            Orders orders = productOrder.getOrders();
            Delivery delivery = productOrder.getDelivery();

            SellerOrderListDto sellerOrderDto = SellerOrderListDto.builder().deliveryId(delivery.getDeliveryId())
                    .deliveryCode(delivery.getDeliveryCode()).sellerId(productOrder.getSellerId()).sellerName(productOrder.getSellerName()).build();
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

    public List<SettlementForAdmin> getSettlementForAdmin(Long sellerId, Long year){
        return settlementRepository.findBySellerIdAndSettlementYear(sellerId,year);
    }

    public SettlementForSeller getSettlementForSeller(Long sellerId, Long year, Long month){
        return settlementRepository.findBySellerIdAndSettlementYearAndSettlementMonth(sellerId,year, month);
    }

    public DashboardResponseDtoForSeller getDashboardForSeller(Long sellerId, String date, Long stockUnderFive){
        OrderStatusDtoForDashboard orderStatsInDateRange = productOrderRepository.getOrderStatsInDateRange(convertDate(date,30L), date, sellerId);
        orderStatsInDateRange.setNullToZero();

        Long trackingNumberNotEntered = productOrderRepository.countNullDeliveryCodesBySellerId(sellerId);

        Long monthSales = productOrderRepository.sumOrderTotalPriceByMonth(date.substring(0,6), sellerId);
        Long monthSettlement = (long) (monthSales*0.90);

        Map<String, Long> week = new HashMap<>();
        week.put("monday",0L);week.put("tuesday",0L);week.put("wednesday",0L);week.put("thursday",0L);
        week.put("friday",0L);week.put("saturday",0L);week.put("sunday",0L);

        for(WeeklySalesDto weeklySalesDto : productOrderRepository.sumOrderTotalPriceInDateRange(convertDate(date, 7L), date, sellerId)){
            week.put(getDayOfWeek(weeklySalesDto.getOrderDay()), weeklySalesDto.getTotalAmount());
        }

        List<WeeklySales> weeklySalesList = new LinkedList<>();
        weeklySalesList.add(WeeklySales.builder().num(week.get("monday")).name("월").build());
        weeklySalesList.add(WeeklySales.builder().num(week.get("tuesday")).name("화").build());
        weeklySalesList.add(WeeklySales.builder().num(week.get("wednesday")).name("수").build());
        weeklySalesList.add(WeeklySales.builder().num(week.get("thursday")).name("목").build());
        weeklySalesList.add(WeeklySales.builder().num(week.get("friday")).name("금").build());
        weeklySalesList.add(WeeklySales.builder().num(week.get("saturday")).name("토").build());
        weeklySalesList.add(WeeklySales.builder().num(week.get("sunday")).name("일").build());

        return DashboardResponseDtoForSeller.builder().order(orderStatsInDateRange.getOrdered()).shipping(orderStatsInDateRange.getShipping())
                .completed(orderStatsInDateRange.getCompleted()).confirmed(orderStatsInDateRange.getConfirmed()).cancel(orderStatsInDateRange.getCancel())
                .monthSales(monthSales).monthSettlement(monthSettlement).stockUnderFive(stockUnderFive).trackingNumberNotEntered(trackingNumberNotEntered)
        .weeklySales(weeklySalesList).build();
    }

    public DashboardResponseDtoForAdmin getDashboardForAdmin(String date){
        Long totalPrice = productOrderRepository.sumOrderTotalPriceByMonthExternal(date);
        List<MonthSellerRankDto> monthlySellerRanking = productOrderRepository.getTop5MonthlySellerRanking(date);

        List<SellerRankMonthDto> sellerRankList = new LinkedList<>();
        IntStream.range(0, 5)
                .forEach(i -> {
                    Long sellerId = null;
                    String sellerName = null;
                    Long price = null;

                    if (i < monthlySellerRanking.size()) {
                        sellerId = monthlySellerRanking.get(i).getSellerId();
                        sellerName = monthlySellerRanking.get(i).getSellerName();
                        price = monthlySellerRanking.get(i).getTotalPrice();
                    }

                    sellerRankList.add(SellerRankMonthDto.builder()
                            .sellerId(sellerId)
                            .sellerName(sellerName)
                            .totalPrice(price)
                            .build());
                });

        List<MonthProductRankDto> top5MonthlyProductRanking = productOrderRepository.getTop5MonthlyProductRanking(date);

        List<SellerProductMonthDto> sellerProductMonthDtoList = new LinkedList<>();
        IntStream.range(0, 5)
                .forEach(i -> {
                    Long sellerId = null;
                    String sellerName = null;
                    String productId = null;
                    String productName = null;
                    Long totalCount = null;

                    if (i < top5MonthlyProductRanking.size()) {
                        sellerId = top5MonthlyProductRanking.get(i).getSellerId();
                        sellerName = top5MonthlyProductRanking.get(i).getSellerName();
                        productId = top5MonthlyProductRanking.get(i).getProductId();
                        productName = top5MonthlyProductRanking.get(i).getProductName();
                        totalCount = top5MonthlyProductRanking.get(i).getTotalCount();
                    }

                    sellerProductMonthDtoList.add(SellerProductMonthDto.builder()
                            .sellerId(sellerId)
                            .sellerName(sellerName)
                            .productId(productId)
                            .productName(productName)
                            .totalCount(totalCount)
                    .build());
                });


        List<SellerRankMonthDto> sellerRankMonthDtoLinkedList = new LinkedList<>();
        for(SellerRankMonthDto sellerRankMonthDto : sellerRankList){
            if(sellerRankMonthDto.getSellerId()==null) break;
            sellerRankMonthDtoLinkedList.add(sellerRankMonthDto);
        }

        List<SellerProductMonthDto> sellerProductMonthDtoLinkedList = new LinkedList<>();
        for(SellerProductMonthDto sellerProductMonthDto : sellerProductMonthDtoList){
            if(sellerProductMonthDto.getSellerId()==null) break;
            sellerProductMonthDtoLinkedList.add(sellerProductMonthDto);
        }

        return DashboardResponseDtoForAdmin.builder()
                .totalSalesMonth(totalPrice)
                .commissionMonth((long) (totalPrice * 0.10))
                .monthSellerRank(sellerRankMonthDtoLinkedList)
                .monthProductRank(sellerProductMonthDtoLinkedList)
        .build();
    }

    public List<Long> getConsumerOrderIdsBySellerId(long sellerId){
        return productOrderRepository.findDistinctConsumersBySellerId(sellerId);
    }

    public List<AllSellerSettlementDtoForAdmin> getAllSellerSettlement(Long year, Long month){
        return productOrderRepository.getSettlementDataByYearAndMonth(year,month);
    }

    private PaymentInfoDto getPaymentInfo(Orders orders) {
        if(orders.getIsAuction()){
            return PaymentInfoDto.builder().minusPointAmount(0L).minusCouponAmount(0L).couponCode(null).totalPrice(orders.getTotalPrice()).realPrice(orders.getTotalPrice()).build();
        }

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

    private String convertDate(String dateString, Long days){
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate thirtyDaysAgo = date.minusDays(days);
        return thirtyDaysAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String getDayOfWeek(String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE).getDayOfWeek().name().toLowerCase();
    }

}