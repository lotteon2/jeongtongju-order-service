package com.jeontongju.order.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PercentLogicTest {
    @Test
    public void 총금액이_3000원이고_포인트가240원이고_쿠폰이30원인데_구매할_아이템이_2개인_경우(){
        long a = 1000;
        long b = 2000;
        long totalPrice = 3000;
        long point = 240;
        long coupon = 30;

        double percentA = (double) a / totalPrice;
        double percentB = (double) b / totalPrice;

        long minusPointA  = (long) (point*percentA);
        long minusPointB = (long)(point*percentB);
        long couponA = (long) (coupon*percentA);
        long couponB = (long) (coupon*percentB);

        Assertions.assertEquals(minusPointA + minusPointB, point);
        Assertions.assertEquals(couponA + couponB, coupon);
    }

    @Test
    public void 총금액이_6000원이고_포인트가2311원이고_쿠폰이1250원인데_구매할_아이템이_3개인_경우(){
        long a = 1000;
        long b = 3500;
        long c = 1500;
        long totalPrice = 6000;
        long point = 2311;
        long coupon = 1250;

        double percentA = (double) a / totalPrice;
        double percentB = (double) b / totalPrice;
        double percentC = (double) c / totalPrice;

        long minusPointA = Math.round(point * percentA);
        long minusPointB = Math.round(point * percentB);
        long minusPointC = Math.round(point * percentC);

        long couponA = Math.round(coupon * percentA);
        long couponB = Math.round(coupon * percentB);
        long couponC = Math.round(coupon * percentC);

        Assertions.assertEquals(minusPointA + minusPointB + minusPointC, point);
        Assertions.assertEquals(couponA + couponB + couponC, coupon);
    }

    @Test
    public void 총금액이_7500원이고_포인트가100원이고_쿠폰이200원인데_구매할_아이템이_4개인_경우(){
        long a = 1000;
        long b = 3500;
        long c = 2000;
        long d = 1000;
        long totalPrice = 7500;
        long point = 100;
        long coupon = 200;

        double percentA = (double) a / totalPrice;
        double percentB = (double) b / totalPrice;
        double percentC = (double) c / totalPrice;
        double percentD = (double) d / totalPrice;

        long minusPointA = Math.round(point * percentA);
        long minusPointB = Math.round(point * percentB);
        long minusPointC = Math.round(point * percentC);
        long minusPointD = Math.round(point * percentD);

        long couponA = Math.round(coupon * percentA);
        long couponB = Math.round(coupon * percentB);
        long couponC = Math.round(coupon * percentC);
        long couponD = Math.round(coupon * percentD);

        Assertions.assertEquals(minusPointA + minusPointB + minusPointC + minusPointD, point);
        Assertions.assertEquals(couponA + couponB + couponC + couponD, coupon);
    }

}
