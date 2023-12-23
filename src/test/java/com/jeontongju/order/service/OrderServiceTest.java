package com.jeontongju.order.service;

import com.jeontongju.order.domain.Delivery;
import com.jeontongju.order.domain.Orders;
import com.jeontongju.order.domain.ProductOrder;
import com.jeontongju.order.enums.OrderStatusEnum;
import com.jeontongju.order.enums.ProductOrderStatusEnum;
import com.jeontongju.order.exception.CancelProductOrderException;
import com.jeontongju.order.exception.DeliveryStatusException;
import com.jeontongju.order.exception.DuplicateDeliveryCodeException;
import com.jeontongju.order.exception.InvalidOrderCancellationException;
import com.jeontongju.order.exception.OrderStatusException;
import com.jeontongju.order.feign.ConsumerFeignServiceClient;
import com.jeontongju.order.feign.PaymentFeignServiceClient;
import com.jeontongju.order.repository.DeliveryRepository;
import com.jeontongju.order.repository.OrdersRepository;
import com.jeontongju.order.repository.ProductOrderRepository;
import io.github.bitbox.bitbox.dto.AddressDto;
import io.github.bitbox.bitbox.dto.AuctionOrderDto;
import io.github.bitbox.bitbox.dto.FeignFormat;
import io.github.bitbox.bitbox.dto.KakaoPayMethod;
import io.github.bitbox.bitbox.dto.OrderConfirmDto;
import io.github.bitbox.bitbox.dto.OrderCreationDto;
import io.github.bitbox.bitbox.dto.OrderInfoDto;
import io.github.bitbox.bitbox.dto.PaymentInfoDto;
import io.github.bitbox.bitbox.dto.ProductInfoDto;
import io.github.bitbox.bitbox.dto.ProductUpdateDto;
import io.github.bitbox.bitbox.dto.UserCouponUpdateDto;
import io.github.bitbox.bitbox.dto.UserPointUpdateDto;
import io.github.bitbox.bitbox.enums.FailureTypeEnum;
import io.github.bitbox.bitbox.enums.PaymentMethodEnum;
import io.github.bitbox.bitbox.enums.PaymentTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@EmbeddedKafka( partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:7777"},
        ports = {7777})
public class OrderServiceTest {

    @MockBean
    private PaymentFeignServiceClient paymentFeignServiceClient;

    @MockBean
    private ConsumerFeignServiceClient consumerFeignServiceClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    public void before(){
        // feign mock
        when(paymentFeignServiceClient.approveKakaopay(any(OrderInfoDto.class)))
                .thenReturn(FeignFormat.<Void>builder().code(200).message("csh").detail("csh2").failure(FailureTypeEnum.DISABLED_MEMBER).build());
        when(paymentFeignServiceClient.getPaymentInfo(any(String.class)))
                .thenReturn(FeignFormat.<PaymentInfoDto>builder().code(200).data(PaymentInfoDto.builder().minusPointAmount(100L).build()).message("csh").detail("csh2").failure(FailureTypeEnum.DISABLED_MEMBER).build());
        when(consumerFeignServiceClient.getOrderConfirmPoint(any(OrderConfirmDto.class)))
                .thenReturn(FeignFormat.<Long>builder().code(200).data(100L).message("csh").detail("csh2").failure(FailureTypeEnum.DISABLED_MEMBER).build());

        UserPointUpdateDto userPointUpdateDto = UserPointUpdateDto.builder().consumerId(1L).point(100L).build();
        UserCouponUpdateDto userCouponUpdateDto = UserCouponUpdateDto.builder().consumerId(1L).couponCode("coupon").couponAmount(100L).totalAmount(20000L).build();
        List<ProductUpdateDto> productUpdateDtoList = new ArrayList<>();
        productUpdateDtoList.add(ProductUpdateDto.builder().productId("qwe1").productCount(100L).build());
        productUpdateDtoList.add(ProductUpdateDto.builder().productId("qwe2").productCount(10L).build());

        List<ProductInfoDto> productInfoDtoList = new ArrayList<>();
        productInfoDtoList.add(ProductInfoDto.builder().productId("qwe1").productName("테스트").productPrice(100L).productCount(100L)
                .sellerId(2L).sellerName("최성훈").productImg("img").build());
        productInfoDtoList.add(ProductInfoDto.builder().productId("qwe2").productName("테스트2").productPrice(1000L).productCount(10L)
                .sellerId(2L).sellerName("최성훈").productImg("img2").build());

        OrderCreationDto orderCreationDto = OrderCreationDto.builder().totalPrice(20000L).consumerId(1L)
                .orderDate(LocalDateTime.now()).orderId("test").productInfoDtoList(productInfoDtoList)
                .recipientName("최국일").recipientPhoneNumber("01012345678").basicAddress("주소1")
                .addressDetail("디테일").zoneCode("12345").paymentType(PaymentTypeEnum.ORDER)
                .paymentMethod(PaymentMethodEnum.KAKAO).paymentInfo(new KakaoPayMethod()).build();
        OrderInfoDto orderInfoDto = OrderInfoDto.builder().userPointUpdateDto(userPointUpdateDto).userCouponUpdateDto(userCouponUpdateDto)
                .productUpdateDto(productUpdateDtoList).orderCreationDto(orderCreationDto).build();
        orderService.createOrder(orderInfoDto);
        em.flush();
        em.clear();
    }

    @Test
    public void 주문및_부가정보들이_정상적으로_저장된다() {
        // order 검증
        Orders orders = ordersRepository.findById("test").orElseThrow(() -> new RuntimeException("주문번호가 없습니다."));
        Assertions.assertEquals(orders.getTotalPrice(), 20000L);
        Assertions.assertEquals(orders.getPaymentMethod(), PaymentMethodEnum.KAKAO);
        Assertions.assertEquals(orders.getOrderStatus(), OrderStatusEnum.NORMAL);
        Assertions.assertEquals(orders.getIsAuction(), false);

        // product-order 검증
        List<ProductOrder> productOrders = productOrderRepository.findAll();
        Assertions.assertEquals(productOrders.size(), 2);

        Assertions.assertEquals(productOrders.get(0).getProductCount()+productOrders.get(1).getProductCount(), 110L);
        Assertions.assertEquals(productOrders.get(0).getProductPrice()+productOrders.get(1).getProductPrice(), 1100L);
        Assertions.assertEquals(productOrders.get(0).getProductRealAmount()+productOrders.get(1).getProductRealAmount(), 19800L);

        Assertions.assertEquals(productOrders.get(0).getProductRealPointAmount()+productOrders.get(1).getProductRealPointAmount(), 100L);

        Assertions.assertEquals(productOrders.get(0).getProductRealCouponAmount()+productOrders.get(1).getProductRealCouponAmount(), 100L);

        Assertions.assertEquals(productOrders.get(0).getOrderDate(), orders.getOrderDate());
        Assertions.assertEquals(productOrders.get(1).getOrderDate(), orders.getOrderDate());

        Assertions.assertEquals(productOrders.get(0).getConsumerId(), orders.getConsumerId());
        Assertions.assertEquals(productOrders.get(1).getConsumerId(), orders.getConsumerId());

        long item1 = productOrders.get(0).getProductCount() * productOrders.get(0).getProductPrice();
        long item2 = productOrders.get(1).getProductCount() * productOrders.get(1).getProductPrice();
        Assertions.assertEquals(orders.getTotalPrice(), item1+item2);

        Delivery delivery1 = productOrders.get(0).getDelivery();
        Delivery delivery2 = productOrders.get(1).getDelivery();

        Assertions.assertNull(delivery1.getDeliveryCode());
        Assertions.assertNull(delivery1.getDeliveryStatus());
        Assertions.assertNull(delivery2.getDeliveryCode());
        Assertions.assertNull(delivery2.getDeliveryStatus());
    }

    @Test
    public void 운송장_번호를_등록하면_배송상태는_SHIPPING이다(){
        ProductOrder productOrder = productOrderRepository.findAll().get(0);
        Long deliveryId = productOrder.getDelivery().getDeliveryId();

        orderService.addDeliveryCode(deliveryId, "100");

        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(() -> new RuntimeException("주문번호가 없습니다."));
        Assertions.assertEquals(delivery.getDeliveryCode(), "100");
        Assertions.assertEquals(delivery.getDeliveryStatus(), ProductOrderStatusEnum.SHIPPING);
    }

    @Test
    public void 운송장_번호를_또_등록하려하면_DuplicateDeliveryCodeException이_발생한다(){
        ProductOrder productOrder = productOrderRepository.findAll().get(0);
        Long deliveryId = productOrder.getDelivery().getDeliveryId();
        orderService.addDeliveryCode(deliveryId, "100");
        em.flush();
        em.clear();

        Assertions.assertThrows(DuplicateDeliveryCodeException.class, () -> orderService.addDeliveryCode(deliveryId, "100"));
    }

    @Test
    public void 배송완료하면_배송상태는_COMPLETED이다(){
        ProductOrder productOrder = productOrderRepository.findAll().get(0);
        Long deliveryId = productOrder.getDelivery().getDeliveryId();

        orderService.addDeliveryCode(deliveryId, "100");
        em.flush();
        em.clear();

        orderService.confirmDelivery(deliveryId);
        em.flush();
        em.clear();

        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(() -> new RuntimeException("주문번호가 없습니다."));
        Assertions.assertEquals(delivery.getDeliveryStatus(), ProductOrderStatusEnum.COMPLETED);
    }

    @Test
    public void 배송완료는_배송중인상품만_가능하다(){
        ProductOrder productOrder = productOrderRepository.findAll().get(0);
        Long deliveryId = productOrder.getDelivery().getDeliveryId();
        Assertions.assertThrows(DeliveryStatusException.class, () -> orderService.confirmDelivery(deliveryId));
    }
    
    @Test
    public void 배송완료_상품은_주문확정이_가능하다(){
        ProductOrder productOrder = productOrderRepository.findAll().get(0);
        Long deliveryId = productOrder.getDelivery().getDeliveryId();

        orderService.addDeliveryCode(deliveryId, "100");
        em.flush();
        em.clear();

        orderService.confirmDelivery(deliveryId);
        em.flush();
        em.clear();

        orderService.confirmProductOrder(productOrder.getProductOrderId());
    }

    @Test
    public void 배송완료가_아니면_구매확정이_불가능하다(){
        when(consumerFeignServiceClient.getOrderConfirmPoint(OrderConfirmDto.builder().productAmount(1000L).build()))
                .thenReturn(FeignFormat.<Long>builder().code(200).data(100L).message("csh").detail("csh2").failure(FailureTypeEnum.DISABLED_MEMBER).build());
        ProductOrder productOrder = productOrderRepository.findAll().get(0);
        Assertions.assertThrows(DeliveryStatusException.class, () -> orderService.confirmProductOrder(productOrder.getDelivery().getDeliveryId()));
    }

    @Test
    public void 경매상품과_관련정보가_정상적으로_저장된다(){
        deliveryRepository.deleteAll();
        productOrderRepository.deleteAll();
        ordersRepository.deleteAll();

        AuctionOrderDto auctionOrderDto = AuctionOrderDto.builder()
                .consumerId(1L).orderDate(LocalDateTime.now()).totalPrice(20000L)
                .paymentMethod(PaymentMethodEnum.CREDIT).productId("csh").productName("아이템")
                .productCount(1L).productPrice(20000L).sellerId(123L)
                .sellerName("최성훈").productImg("test").build();
        AddressDto addressDto = AddressDto.builder()
                .recipientName("최국일").recipientPhoneNumber("01012345678")
                .basicAddress("주소1").addressDetail("디테일1").zonecode("12345").build();

        orderService.createAuctionOrder(auctionOrderDto, addressDto);
        em.flush();
        em.clear();

        List<Orders> ordersList = ordersRepository.findAll();
        Orders orders = ordersList.get(0);

        Assertions.assertEquals(ordersList.size(), 1);
        Assertions.assertEquals(orders.getConsumerId(), 1L);
        Assertions.assertEquals(orders.getOrderStatus(), OrderStatusEnum.NORMAL);
        Assertions.assertEquals(orders.getTotalPrice(),20000L);
        Assertions.assertEquals(orders.getIsAuction(),true);
        Assertions.assertEquals(orders.getPaymentMethod(), PaymentMethodEnum.CREDIT);

        List<ProductOrder> productOrderList = productOrderRepository.findAll();
        ProductOrder productOrder = productOrderList.get(0);

        Assertions.assertEquals(productOrderList.size(), 1);
        Assertions.assertEquals(productOrder.getProductCount(), 1);
        Assertions.assertEquals(productOrder.getProductPrice(), 20000L);
        Assertions.assertEquals(productOrder.getProductRealAmount(), 20000L);
        Assertions.assertEquals(productOrder.getProductRealPointAmount(),0L);
        Assertions.assertEquals(productOrder.getProductRealCouponAmount(),0L);

        Delivery delivery = productOrder.getDelivery();
        Assertions.assertEquals(delivery.getRecipientName(), "최국일");
        Assertions.assertEquals(delivery.getRecipientPhoneNumber(),"01012345678");
        Assertions.assertNull(delivery.getDeliveryCode());
        Assertions.assertNull(delivery.getDeliveryStatus());
    }

    @Test
    public void 주문을_취소하면_상품들의_모든상태가_CANCEL상태이다(){
        Orders orders = ordersRepository.findAll().get(0);
        orderService.cancelOrder(orders.getOrdersId());
        em.flush();
        em.clear();

        for(ProductOrder productOrder : productOrderRepository.findAll()){
            Assertions.assertEquals(productOrder.getProductOrderStatus(),ProductOrderStatusEnum.CANCEL);
        }

    }

    @Test
    public void 경매주문은_취소할수_없다(){
        deliveryRepository.deleteAll();
        productOrderRepository.deleteAll();
        ordersRepository.deleteAll();

        AuctionOrderDto auctionOrderDto = AuctionOrderDto.builder()
                .consumerId(1L).orderDate(LocalDateTime.now()).totalPrice(20000L)
                .paymentMethod(PaymentMethodEnum.CREDIT).productId("csh").productName("아이템")
                .productCount(1L).productPrice(20000L).sellerId(123L)
                .sellerName("최성훈").productImg("test").build();
        AddressDto addressDto = AddressDto.builder()
                .recipientName("최국일").recipientPhoneNumber("01012345678")
                .basicAddress("주소1").addressDetail("디테일1").zonecode("12345").build();

        orderService.createAuctionOrder(auctionOrderDto, addressDto);
        em.flush();
        em.clear();

        List<Orders> ordersList = ordersRepository.findAll();

        Orders orders = ordersList.get(0);
        Assertions.assertEquals(ordersList.size(),1);
        Assertions.assertThrows(OrderStatusException.class, () -> orderService.cancelOrder(orders.getOrdersId()));
    }

    @Test
    public void 취소된주문의경우_취소할수없다(){
        Orders orders = ordersRepository.findAll().get(0);
        orderService.cancelOrder(orders.getOrdersId());
        Assertions.assertThrows(OrderStatusException.class, () -> orderService.cancelOrder(orders.getOrdersId()));
    }

    @Test
    public void 상품이_하나라도_배송상태면_취소할수없다(){
        ProductOrder productOrder = productOrderRepository.findAll().get(0);
        Long deliveryId = productOrder.getDelivery().getDeliveryId();
        orderService.addDeliveryCode(deliveryId, "100");
        em.flush();
        em.clear();

        Orders orders = ordersRepository.findAll().get(0);

        Assertions.assertThrows(InvalidOrderCancellationException.class, () -> orderService.cancelOrder(orders.getOrdersId()));
    }

    @Test
    public void 주문완료_상태가아니면_취소할수없다(){
        ProductOrder productOrder = productOrderRepository.findAll().get(0);
        Long deliveryId = productOrder.getDelivery().getDeliveryId();

        orderService.addDeliveryCode(deliveryId, "100");
        em.flush();
        em.clear();

        orderService.confirmDelivery(deliveryId);
        em.flush();
        em.clear();

        orderService.confirmProductOrder(productOrder.getProductOrderId());
        em.flush();
        em.clear();

        Assertions.assertThrows(CancelProductOrderException.class, () -> orderService.cancelProductOrder(productOrderRepository.findAll().get(0).getProductOrderId()));
    }

    @Test
    public void 배송중인_상품은_취소할수없다(){
        ProductOrder productOrder = productOrderRepository.findAll().get(0);
        Long deliveryId = productOrder.getDelivery().getDeliveryId();

        orderService.addDeliveryCode(deliveryId, "100");
        em.flush();
        em.clear();
        Assertions.assertThrows(CancelProductOrderException.class, () -> orderService.cancelProductOrder(productOrderRepository.findAll().get(0).getProductOrderId()));
    }

    @Test
    public void 경매상품은_취소할수_없다(){
        deliveryRepository.deleteAll();
        productOrderRepository.deleteAll();
        ordersRepository.deleteAll();

        AuctionOrderDto auctionOrderDto = AuctionOrderDto.builder()
                .consumerId(1L).orderDate(LocalDateTime.now()).totalPrice(20000L)
                .paymentMethod(PaymentMethodEnum.CREDIT).productId("csh").productName("아이템")
                .productCount(1L).productPrice(20000L).sellerId(123L)
                .sellerName("최성훈").productImg("test").build();
        AddressDto addressDto = AddressDto.builder()
                .recipientName("최국일").recipientPhoneNumber("01012345678")
                .basicAddress("주소1").addressDetail("디테일1").zonecode("12345").build();

        orderService.createAuctionOrder(auctionOrderDto, addressDto);
        em.flush();
        em.clear();


        Assertions.assertThrows(OrderStatusException.class, () -> orderService.cancelProductOrder(productOrderRepository.findAll().get(0).getProductOrderId()));
    }

    @Test
    public void 모든상품이_취소되었으면_주문은_취소상태이다(){
        for(ProductOrder productOrder : productOrderRepository.findAll()){
            orderService.cancelProductOrder(productOrder.getProductOrderId());
        }
        em.flush();
        em.clear();

        Assertions.assertEquals(ordersRepository.findAll().get(0).getOrderStatus(), OrderStatusEnum.CANCEL);
    }

}