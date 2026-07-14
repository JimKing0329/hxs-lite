package com.hxs.utils;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public final class RsaUtil {

    private RsaUtil() {}

    /**
     * RSA 加密（用于教务系统登录密码加密）
     * @param plainText 明文
     * @param modulusBase64 Base64 编码的 modulus
     * @param exponentBase64 Base64 编码的 exponent
     * @return Base64 编码的密文
     */
    public static String encrypt(String plainText, String modulusBase64, String exponentBase64) {
        try {
            byte[] nBytes = Base64.getDecoder().decode(modulusBase64);
            byte[] eBytes = Base64.getDecoder().decode(exponentBase64);
            String nHex = bytesToHex(nBytes);
            String eHex = bytesToHex(eBytes);

            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
                    new BigInteger(nHex, 16),
                    new BigInteger(eHex, 16));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }
}
