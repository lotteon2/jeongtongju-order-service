package com.jeontongju;

import io.github.bitbox.bitbox.dto.OrderCancelDto;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

@TestConfiguration
public class TestKafkaConfig {
    @Bean
    @Primary
    public KafkaTemplate<String, OrderCancelDto> kafkaTemplateMock() {
        return new KafkaTemplate<String, OrderCancelDto>(null) {
            @Override
            public ListenableFuture<SendResult<String, OrderCancelDto>> sendDefault(OrderCancelDto data) {
                SettableListenableFuture<SendResult<String, OrderCancelDto>> future = new SettableListenableFuture<>();
                future.set(new SendResult<>(new ProducerRecord<>("yourTopicName", "yourKey", data), null));
                return future;
            }
        };
    }
}