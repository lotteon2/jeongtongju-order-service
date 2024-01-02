package com.jeontongju;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeontongju.order.controller.OrderController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {OrderController.class})
public class ControllerTestUtil {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}