package com.yurupari.user_service.service;

import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(LoginRequest loginRequest);
}
