package com.yurupari.user_service.config;

import com.yurupari.user_service.client.KeycloakClient;
import com.yurupari.user_service.exception.KeycloakClientException;
import com.yurupari.user_service.exception.ApiServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.URI;
import java.time.Duration;

@Configuration
@EnableAspectJAutoProxy
public class HttpClientConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Bean
    public KeycloakClient keycloakClient() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        var restClient = RestClient.builder()
                .baseUrl(URI.create("%s/realms/%s/protocol/openid-connect/token".formatted(keycloakUrl, realm)))
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    var email = req.getAttributes().get("username").toString();
                    throw new KeycloakClientException(email);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    var email = req.getAttributes().get("username").toString();
                    throw new ApiServerException(String.format("Error calling Keycloak API: email=%s", email));
                })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(KeycloakClient.class);
    }
}
