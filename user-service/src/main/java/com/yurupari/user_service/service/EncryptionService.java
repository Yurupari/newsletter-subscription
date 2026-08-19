package com.yurupari.user_service.service;

public interface EncryptionService {

    String encrypt(String plainText);

    String decrypt(String encryptedBase64);
}
