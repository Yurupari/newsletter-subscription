package com.yurupari.subscription_service.config;

import com.yurupari.common_data.exception.ApiServerException;
import com.yurupari.common_data.model.http.ErrorResponse;
import com.yurupari.subscription_service.client.UserServiceClient;
import com.yurupari.subscription_service.exception.UserServiceClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@EnableAspectJAutoProxy
@Slf4j
public class HttpClientConfig {

    private final ObjectMapper objectMapper;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Bean
    public UserServiceClient userServiceClient() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        var restClient = RestClient.builder()
                .baseUrl(URI.create(userServiceUrl))
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    var identifier = Optional.ofNullable(req.getAttributes().get("userId"))
                            .map(Object::toString)
                            .orElse("unknown");
                    var statusCode = res.getStatusCode();

                    String errorMessage = "User service error";
                    try {
                        var errorResponse = objectMapper.readValue(res.getBody(), ErrorResponse.class);
                        if (errorResponse != null && errorResponse.message() != null) {
                            errorMessage = errorResponse.message();
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse ErrorResponse body for identifier={}", identifier, e);
                    }

                    log.error("User service 4xx error: identifier={}, status={}, message={}",
                            identifier, statusCode, errorMessage);

                    throw new UserServiceClientException(statusCode, identifier, errorMessage);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    var identifier = Optional.ofNullable(req.getAttributes().get("userId"))
                            .map(Object::toString)
                            .orElse("unknown");
                    var responseBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    var statusCode = res.getStatusCode();

                    log.error("User service 5xx error: identifier={}, status={}, body={}",
                            identifier, statusCode, responseBody);

                    throw new ApiServerException(String.format("Error calling user service: userId=%s", identifier));
                })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(UserServiceClient.class);
    }
}
