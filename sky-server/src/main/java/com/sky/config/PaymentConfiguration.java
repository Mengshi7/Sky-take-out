package com.sky.config;

import com.sky.properties.PaymentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Configuration
public class PaymentConfiguration {

    @Autowired
    private PaymentProperties paymentProperties;
    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkPaymentMode() {
        if (paymentProperties.isMockEnabled()) {
            String[] profiles = environment.getActiveProfiles();
            //只允许独立的dev环境开启，禁止与生产环境混用
            if (profiles.length != 1 || !Arrays.asList(profiles).contains("dev")) {
                throw new IllegalStateException("模拟支付只能在dev环境开启");
            }
            if (!"127.0.0.1".equals(environment.getProperty("server.address"))) {
                throw new IllegalStateException("模拟支付必须绑定本机地址127.0.0.1");
            }
        }
    }
}
