package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.request.UserRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.model.http.response.UserResponse;
import com.yurupari.user_service.repository.UserRepository;
import com.yurupari.user_service.service.AuthService;
import com.yurupari.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final AuthService authService;

    @Override
    public UserResponse registerUser(UserRequest userRequest) {
        return null;
    }

    @Override
    public AuthenticationResponse login(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public UserResponse getUser(Long id, String email) {
        return null;
    }
}
