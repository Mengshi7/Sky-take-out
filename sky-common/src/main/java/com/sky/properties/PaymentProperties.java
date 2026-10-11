package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.payment")
@Data
public class PaymentProperties {

    //本地模拟支付，默认关闭
    private boolean mockEnabled = false;
}
