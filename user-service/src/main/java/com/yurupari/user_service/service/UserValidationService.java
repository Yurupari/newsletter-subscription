package com.yurupari.user_service.service;

public interface UserValidationService {

    void validateUser(String encryptedPassword, String password);

    void validateUserIdAndEmail(Long id, String email);
}
