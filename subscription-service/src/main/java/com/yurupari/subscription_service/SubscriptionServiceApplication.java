package com.yurupari.subscription_service;

import com.yurupari.subscription_service.config.PropertiesPayloads;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.yurupari.subscription_service", "com.yurupari.common_data"})
@EnableConfigurationProperties(PropertiesPayloads.class)
public class SubscriptionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubscriptionServiceApplication.class, args);
	}

}
