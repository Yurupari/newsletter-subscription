package com.yurupari.user_service.service;

import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.request.UserRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.model.http.response.UserResponse;

public interface UserService {
    UserResponse registerUser(UserRequest userRequest);

    AuthenticationResponse login(LoginRequest loginRequest);

    UserResponse getUser(Long id, String email);
}
