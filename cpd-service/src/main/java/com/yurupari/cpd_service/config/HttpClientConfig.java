package com.yurupari.cpd_service.config;

import com.yurupari.cpd_service.client.CPDClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
@EnableAspectJAutoProxy
@Slf4j
public class HttpClientConfig {

    private final ObjectMapper objectMapper;

    @Value("${services.cpd.url}")
    private String cpdUrl;

    @Value("${services.cpd.write-key}")
    private String cpdWriteKey;

    @Bean
    public CPDClient cpdClient() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        var basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((cpdWriteKey + ":").getBytes(StandardCharsets.UTF_8));

        var restClient = RestClient.builder()
                .baseUrl(URI.create(cpdUrl))
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    var statusCode = res.getStatusCode();
                    String responseBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    log.error("CDP API 4xx error: status={}, body={}", statusCode, responseBody);
                    throw new RuntimeException("CDP Client Error: " + statusCode + " - " + responseBody);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    var statusCode = res.getStatusCode();
                    String responseBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    log.error("CDP API 5xx error: status={}, body={}", statusCode, responseBody);
                    throw new RuntimeException("CDP Server Error: " + statusCode);
                })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(CPDClient.class);
    }
}
