package org.example.securityConfig;

/**
 * 作者：Leo
 * 描述：令牌解析异常抛出此异常
 */

public class JWTParseException extends RuntimeException{
    public JWTParseException(String message) {
        super(message);
    }
}

