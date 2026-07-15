package com.hxs.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;

public final class JwtUtil {

    private JwtUtil() {}

    /**
     * 将配置的密钥字符串通过 SHA-256 哈希转为固定 256 位密钥，
     * 同时兼容短密钥（如 "hxs"）和 jjwt 0.12.x 的长度要求。
     */
    private static SecretKey toKey(String secretKey) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashed = sha256.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hashed, "HmacSHA256");
        } catch (Exception e) {
            throw new RuntimeException("JWT key derivation failed", e);
        }
    }

    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        SecretKey key = toKey(secretKey);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMillis))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public static Claims parseJWT(String secretKey, String token) {
        SecretKey key = toKey(secretKey);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
