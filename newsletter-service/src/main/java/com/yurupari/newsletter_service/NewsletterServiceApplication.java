package com.yurupari.newsletter_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.yurupari.user_service", "com.yurupari.common_data"})
public class NewsletterServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsletterServiceApplication.class, args);
	}

}
