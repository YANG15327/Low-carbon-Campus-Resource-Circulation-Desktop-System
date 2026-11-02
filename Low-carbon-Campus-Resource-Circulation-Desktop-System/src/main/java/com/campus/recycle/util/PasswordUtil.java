package com.campus.recycle.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具：SHA-256 + 随机盐。
 *
 * <p>密码绝不明文入库，采用「随机盐 + 摘要」方式存储，并使用常数时间比较
 * 防止时序攻击。</p>
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
        // 工具类，禁止实例化
    }

    /** 生成 16 字节随机盐并 Base64 编码。 */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** 使用给定盐对密码做 SHA-256 摘要，返回 Base64 字符串。 */
    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(Base64.getDecoder().decode(salt));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前环境不支持 " + ALGORITHM + " 算法", e);
        }
    }

    /** 校验密码是否与存储的哈希一致（常数时间比较）。 */
    public static boolean verify(String password, String salt, String expectedHash) {
        String actual = hash(password, salt);
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
