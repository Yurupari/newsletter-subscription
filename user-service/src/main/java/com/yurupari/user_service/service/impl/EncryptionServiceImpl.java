package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionServiceImpl implements EncryptionService {

    private final SecureRandom secureRandom;

    private final SecretKey secretKey;

    private final String algorithm;
    private final int tagLengthBit;
    private final int ivLengthByte;

    public EncryptionServiceImpl(
            @Value("${encryption.secret-key}") String base64SecretKey,
            @Value("${encryption.algorithm-instance}") String algorithmInstance,
            @Value("${encryption.algorithm}") String algorithm,
            @Value("${encryption.tag-length-bit}") int tagLengthBit,
            @Value("${encryption.iv-length-byte}") int ivLengthByte) {
        this.secureRandom = new SecureRandom();
        this.algorithm = algorithm;
        this.tagLengthBit = tagLengthBit;
        this.ivLengthByte = ivLengthByte;

        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64SecretKey);
            this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithmInstance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode base64 encryption key", e);
        }
    }

    @Override
    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[ivLengthByte];
            secureRandom.nextBytes(iv);

            var cipher = Cipher.getInstance(algorithm);
            var parameterSpec = new GCMParameterSpec(tagLengthBit, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedData = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String encryptedBase64) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);

            byte[] iv = new byte[ivLengthByte];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[decoded.length - iv.length];
            System.arraycopy(decoded, iv.length, cipherText, 0, cipherText.length);

            var cipher = Cipher.getInstance(algorithm);
            var parameterSpec = new GCMParameterSpec(tagLengthBit, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
