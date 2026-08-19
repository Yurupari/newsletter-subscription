package com.yurupari.subscription_service.client;

import com.yurupari.subscription_service.model.http.response.UserResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = {MediaType.APPLICATION_JSON_VALUE})
public interface UserServiceClient {

    @Retry(name = "userService")
    @GetExchange("/api/v1/user")
    UserResponse getUser(@RequestParam("id") Long userId);
}
