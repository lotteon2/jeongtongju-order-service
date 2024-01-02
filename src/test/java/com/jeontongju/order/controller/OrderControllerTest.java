package com.jeontongju.order.controller;

import com.jeontongju.ControllerTestUtil;
import com.jeontongju.order.dto.DeliveryDto;
import com.jeontongju.order.dto.OrderCancelRequestDto;
import com.jeontongju.order.dto.ProductOrderCancelRequestDto;
import com.jeontongju.order.service.OrderService;
import io.github.bitbox.bitbox.enums.MemberRoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerTest extends ControllerTestUtil {
    @MockBean
    private OrderService orderService;

    @Test
    public void 고객_주문내역조회는_consumer는_할수있다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/consumer")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 고객_주문내역조회는_admin은_할수없다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/consumer")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_ADMIN))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 고객_주문내역조회는_seller는_할수없다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/consumer")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_SELLER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 특정_고객_주문내역조회는_consumer는_할수없다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/consumer/1")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 특정_고객_주문내역조회는_seller는_할수없다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/consumer/1")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_SELLER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 특정_고객_주문내역조회는_admin은_할수있다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/consumer/1")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_ADMIN))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 셀러_주문내역은_consumer는_할수없다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/seller")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)
                                .param("orderDate","1")
                                .param("productId","1")
                                .param("isDeliveryCodeNull","true"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 셀러의_주문내역은_셀러는_할수있다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/seller")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_SELLER)
                                .param("orderDate","1")
                                .param("productId","1")
                                .param("isDeliveryCodeNull","true"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 셀러의_주문내역은_admin은_할수없다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/seller")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_ADMIN)
                                .param("orderDate","1")
                                .param("productId","1")
                                .param("isDeliveryCodeNull","true"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 특정셀러_주문내역은_consumer는_할수없다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/seller/1")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)
                                .param("orderDate","1")
                                .param("productId","1"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 특정셀러의_주문내역은_셀러는_할수있다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/seller/1")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_SELLER)
                                .param("orderDate","1")
                                .param("productId","1"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 셀러의_주문내역은_admin은_할수있다() throws Exception{
        mockMvc
                .perform(
                        get("/api/order/seller/1")
                                .header("memberId",1L)
                                .header("memberRole", MemberRoleEnum.ROLE_ADMIN)
                                .param("orderDate","1")
                                .param("productId","1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 운송장번호_등록은_컨슈머는_불가능하다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/delivery/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(DeliveryDto.builder().deliveryCode("test").build()))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 운송장번호_등록을_할때는_운송장_번호는_필수이다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/delivery/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(DeliveryDto.builder().build()))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    public void 운송장번호_등록은_셀러는_가능하다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/delivery/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_SELLER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(DeliveryDto.builder().deliveryCode("test").build()))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 운송장번호_등록은_어드민은_불가능하다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/delivery/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_ADMIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(DeliveryDto.builder().deliveryCode("test").build()))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 배송확정은_컨슈머는_불가하다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/delivery-confirm/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)
                                
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 배송확정은_셀러는_가능하다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/delivery-confirm/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_SELLER)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 배송확정은_관리자는_불가능하다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/delivery-confirm/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_ADMIN)

                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 주문확정은_컨슈머는_가능하다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/product-order-confirm/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 주문확정은_셀러는_불가능하다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/product-order-confirm/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_SELLER)

                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 주문확정은_관리자는_불가능하다() throws Exception{
        mockMvc
                .perform(
                        patch("/api/product-order-confirm/1")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_ADMIN)

                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 주문취소는_컨슈머는_가능하다() throws Exception{
        mockMvc
                .perform(
                        post(
                                "/api/order-cancel")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(OrderCancelRequestDto.builder().ordersId("test").build()))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 주문취소시_주문번호는_필수이다() throws Exception{
        mockMvc
                .perform(
                        post("/api/order-cancel")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void 주문취소는_셀러는_불가능하다() throws Exception{
        mockMvc
                .perform(
                        post(
                                "/api/order-cancel")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_SELLER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(OrderCancelRequestDto.builder().ordersId("test").build()))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 주문취소는_관리자는_불가능하다() throws Exception{
        mockMvc
                .perform(
                        post(
                                "/api/order-cancel")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_ADMIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(OrderCancelRequestDto.builder().ordersId("test").build()))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 상품취소는_컨슈머는_가능하다() throws Exception{
        mockMvc
                .perform(
                        post(
                                "/api/product-order-cancel")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ProductOrderCancelRequestDto.builder().productOrderId(1L).build()))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void 취소할_상품번호는_필수이다() throws Exception{
        mockMvc
                .perform(
                        post(
                                "/api/product-order-cancel")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_CONSUMER)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void 상품취소는_셀러는_불가능하다() throws Exception{
        mockMvc
                .perform(
                        post(
                                "/api/product-order-cancel")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_SELLER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ProductOrderCancelRequestDto.builder().productOrderId(1L).build()))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void 상품취소는_관리자는_불가능하다() throws Exception{
        mockMvc
                .perform(
                        post(
                                "/api/product-order-cancel")
                                .header("memberId", 1L)
                                .header("memberRole", MemberRoleEnum.ROLE_ADMIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ProductOrderCancelRequestDto.builder().productOrderId(1L).build()))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

}
