package com.hxs.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RsaUtil 工具类测试")
class RsaUtilTest {

    @Test
    @DisplayName("加密后结果非空且与原文不同")
    void shouldEncryptSuccessfully() throws Exception {
        // 生成 RSA 密钥对用于测试
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        String modulusBase64 = Base64.getEncoder().encodeToString(publicKey.getModulus().toByteArray());
        String exponentBase64 = Base64.getEncoder().encodeToString(publicKey.getPublicExponent().toByteArray());

        String plainText = "testPassword123";
        String encrypted = RsaUtil.encrypt(plainText, modulusBase64, exponentBase64);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        assertFalse(encrypted.isEmpty());
    }

    @Test
    @DisplayName("相同输入产生相同长度的密文")
    void shouldProduceConsistentLength() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        String modulusBase64 = Base64.getEncoder().encodeToString(publicKey.getModulus().toByteArray());
        String exponentBase64 = Base64.getEncoder().encodeToString(publicKey.getPublicExponent().toByteArray());

        String encrypted1 = RsaUtil.encrypt("password", modulusBase64, exponentBase64);
        String encrypted2 = RsaUtil.encrypt("password", modulusBase64, exponentBase64);

        // RSA 加密包含随机填充，密文长度应一致
        assertEquals(encrypted1.length(), encrypted2.length());
    }

    @Test
    @DisplayName("不同密码产生不同的密文")
    void shouldProduceDifferentCipherForDifferentPassword() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        String modulusBase64 = Base64.getEncoder().encodeToString(publicKey.getModulus().toByteArray());
        String exponentBase64 = Base64.getEncoder().encodeToString(publicKey.getPublicExponent().toByteArray());

        String encrypted1 = RsaUtil.encrypt("password1", modulusBase64, exponentBase64);
        String encrypted2 = RsaUtil.encrypt("password2", modulusBase64, exponentBase64);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    @DisplayName("无效 modulus 抛异常")
    void shouldThrowOnInvalidModulus() {
        assertThrows(RuntimeException.class, () ->
                RsaUtil.encrypt("test", "invalid====", "AQAB"));
    }
}
