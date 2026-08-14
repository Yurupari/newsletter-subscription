package com.yurupari.user_service.controller.v1;

import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationControllerV1 {

    @Autowired
    private UserService userService;

    @Operation(summary = "Login an user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        var userResponse = userService.login(loginRequest);
        return ResponseEntity.ok(userResponse);
    }
}
