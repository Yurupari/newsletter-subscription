package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.service.EncryptionService;
import com.yurupari.user_service.service.UserValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserValidationServiceImpl implements UserValidationService {

    private final EncryptionService encryptionService;

    @Override
    public void validateUser(String encryptedPassword, String password) {
        if (encryptedPassword == null) {
            throw new IllegalArgumentException("Password from DB is null");
        }

        if (password == null) {
            throw new IllegalArgumentException("Password to validate is null");
        }

        var decryptedPassword = encryptionService.decrypt(encryptedPassword);

        if (!decryptedPassword.equals(password)) {
            throw new BadCredentialsException("Passwords do not match");
        }
    }

    @Override
    public void validateUserIdAndEmail(Long id, String email) {
        if (id == null && email == null) {
            throw new IllegalArgumentException("Either id or email must be provided");
        }
    }
}
