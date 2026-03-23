package com.trustbridge.Features.Payments.Config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeConfig {

    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
        System.out.println("Stripe key initialised successfully");
    }
}
