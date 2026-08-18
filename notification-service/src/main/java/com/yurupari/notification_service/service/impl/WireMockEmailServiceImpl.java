package com.yurupari.notification_service.service.impl;

import com.yurupari.notification_service.model.http.EmailRequest;
import com.yurupari.notification_service.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Service
@Primary
@Slf4j
public class WireMockEmailServiceImpl implements EmailService {

    private final RestClient restClient;

    public WireMockEmailServiceImpl(@Value("${email.service.url}") String emailServiceUrl) {
        var jdkHttpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(jdkHttpClient);

        this.restClient = RestClient.builder()
                .baseUrl(emailServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email: to={}, subject={}", to, subject);

        var payload = EmailRequest.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .build();

        try {
            restClient.post()
                    .uri("/api/v1/emails/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to send email: to={}", to, e);
        }
    }
}
