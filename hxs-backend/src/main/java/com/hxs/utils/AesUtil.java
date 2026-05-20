package com.hxs.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

public final class AesUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_PATH = "D:\\code\\javas\\hxs-lite\\hxs-backend\\src\\main\\resources\\secret.key";
    private static volatile byte[] cachedKey;

    private AesUtil() {}

    private static byte[] getKey() {
        if (cachedKey != null) return cachedKey;
        synchronized (AesUtil.class) {
            if (cachedKey != null) return cachedKey;
            try { cachedKey = Files.readAllBytes(Paths.get(KEY_PATH)); }
            catch (Exception e) { throw new RuntimeException("无法读取密钥文件: " + KEY_PATH, e); }
            return cachedKey;
        }
    }

    public static String encrypt(String plainText) throws Exception {
        byte[] keyBytes = getKey();
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] enc = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[16 + enc.length];
        System.arraycopy(iv, 0, combined, 0, 16);
        System.arraycopy(enc, 0, combined, 16, enc.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String cipherText) throws Exception {
        byte[] keyBytes = getKey();
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        byte[] combined = Base64.getDecoder().decode(cipherText);
        byte[] iv = new byte[16];
        System.arraycopy(combined, 0, iv, 0, 16);
        byte[] enc = new byte[combined.length - 16];
        System.arraycopy(combined, 16, enc, 0, enc.length);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return new String(cipher.doFinal(enc), StandardCharsets.UTF_8);
    }

}
