package com.yurupari.user_service.controller.v1;

import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.request.UserRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.model.http.response.UserResponse;
import com.yurupari.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserControllerV1 {

    @Autowired
    private UserService userService;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest userRequest) {
        var createdUser = userService.registerUser(userRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Get an user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User obtained successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    public ResponseEntity<UserResponse> getUser(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "email", required = false) String email
    ) {
        var userResponse = userService.getUser(id, email);
        return ResponseEntity.ok(userResponse);
    }
}
