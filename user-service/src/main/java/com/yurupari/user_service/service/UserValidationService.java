package com.yurupari.user_service.service;

import com.yurupari.user_service.model.entity.User;

import java.util.List;

public interface UserValidationService {

    void validateUser(String encryptedPassword, String password);

    void validateUserIdAndEmail(Long id, String email);

    void validateUsers(Long id, String email, List<User> users);
}
