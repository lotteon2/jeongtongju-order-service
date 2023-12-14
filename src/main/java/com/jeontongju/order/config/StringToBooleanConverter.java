package com.jeontongju.order.config;
import org.springframework.core.convert.converter.Converter;

public class StringToBooleanConverter implements Converter<String, Boolean> {
    @Override
    public Boolean convert(String source) {
        if ("null".equalsIgnoreCase(source)) {
            return null;
        }
        return Boolean.valueOf(source);
    }
}