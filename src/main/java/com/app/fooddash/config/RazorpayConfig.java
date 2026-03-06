package com.app.fooddash.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    // Injected from application.properties → env var
    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    /**
     * Creates a singleton RazorpayClient bean.
     * Spring manages its lifecycle — inject with @Autowired or constructor injection.
     */
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new IllegalStateException(
                "RAZORPAY_KEY_ID or RAZORPAY_KEY_SECRET is missing. Check environment variables."
            );
        }
        return new RazorpayClient(keyId, keySecret);
    }
}