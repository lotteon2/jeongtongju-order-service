package com.jeontongju.order.util;

public class KafkaTopicNameInfo {
    public static final String ORDER_CREATION_TOPIC = "create-order";
    public static final String ROLLBACK_STOCK_TOPIC = "add-stock";
    public static final String CART_DELETE_TOPIC = "delete-cart";
    public static final String AUCTION_ORDER_TOPIC = "create-auction-order";
    public static final String CANCEL_ORDER_POINT = "cancel-order-point";
    public static final String CANCEL_ORDER_COUPON = "cancel-order-coupon";
    public static final String CANCEL_PAYMENT = "cancel-order-payment";
}