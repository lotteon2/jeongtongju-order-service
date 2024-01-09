package com.jeontongju.order.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class Resilience4JConfig {

  @Bean
  public CircuitBreakerConfig circuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        // 요청 실패한 횟수를 기준으로 실패율 계산
        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
        // 요청의 응답 시간이 이 값 이상일 때 slow call 상태로 계산
        .slowCallDurationThreshold(Duration.ofSeconds(2))
        // slow call 요청의 비율이 (이 값)% 이상이 되면 회로 차단(Open state)
        .slowCallRateThreshold(50)
        // 요청 실패 비율이 (이 값)% 이상이 되면 회로 차단(Open state)
        .failureRateThreshold(50)
        // 요청의 실패와 slow call의 비율을 계산할 최소 요청 수 지정
        .minimumNumberOfCalls(5)
        // 회로가 닫혀있을 때 기록할 슬라이딩 윈도우 개수
        .slidingWindowSize(10)
        // 회로가 반 개방 상태일 때 열림 상태가 되기까지 대기시간
        .maxWaitDurationInHalfOpenState(Duration.ofMillis(1000))
        // 열린 상태로 10초 유지 후 반 개방상태로 전환
        .waitDurationInOpenState(Duration.ofMillis(10000))
        // 회로가 반 개방 상태일 때 받아들일 요청의 개수
        .permittedNumberOfCallsInHalfOpenState(3)
        .build();
  }

  @Bean
  public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {

    // future supplier의 time limit 지정: 6초 동안 응답이 오지 않으면 Open state
    TimeLimiterConfig timeLimiterConfig =
        TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(6)).build();
    return factory ->
        factory.configureDefault(
            id ->
                new Resilience4JConfigBuilder(id)
                    .timeLimiterConfig(timeLimiterConfig)
                    .circuitBreakerConfig(circuitBreakerConfig())
                    .build());
  }
}
