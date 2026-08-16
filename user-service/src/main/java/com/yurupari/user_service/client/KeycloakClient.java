package com.yurupari.user_service.client;

import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.net.URI;

@HttpExchange(accept = {MediaType.APPLICATION_JSON_VALUE})
public interface KeycloakClient {

    @Retry(name = "keycloakAuth")
    @PostExchange(contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AuthenticationResponse authenticate(@RequestBody MultiValueMap<String, String> formData);
}
