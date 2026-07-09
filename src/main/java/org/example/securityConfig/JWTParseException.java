package org.example.securityConfig;

/**
 * JWT 解析失败时抛出的自定义异常。
 */
public class JWTParseException extends RuntimeException {
    public JWTParseException(String message) {
        super(message);
    }
}
