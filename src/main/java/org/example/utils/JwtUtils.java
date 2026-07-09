package org.example.utils;

import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.securityConfig.JWTParseException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Calendar;
import java.util.UUID;

/**
 * JWT 工具类。
 * <p>
 * 使用 RSA 私钥签发 token，使用 RSA 公钥解析 token。
 */
public class JwtUtils {
    private static final String JWT_PAYLOAD_USER_KEY = "user";

    /**
     * 生成分钟级过期时间的 JWT。
     */
    public static String generateTokenExpireInMinutes(Object userInfo, PrivateKey privateKey, int expire) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expire);

        return Jwts.builder()
                .claim(JWT_PAYLOAD_USER_KEY, JSONUtil.toJsonStr(userInfo))
                .setId(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()))
                .setExpiration(calendar.getTime())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 生成秒级过期时间的 JWT，适合短期 token 场景。
     */
    public static String generateTokenExpireInSeconds(Object userInfo, PrivateKey privateKey, int expire) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, expire);

        return Jwts.builder()
                .claim(JWT_PAYLOAD_USER_KEY, JSONUtil.toJsonStr(userInfo))
                .setId(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()))
                .setExpiration(calendar.getTime())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 从 JWT 中解析用户信息。
     *
     * @param token     请求中的 token
     * @param publicKey RSA 公钥
     * @param userType  要转换成的用户类型
     */
    public static Object getInfoFromToken(String token, PublicKey publicKey, Class<?> userType) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);

            Claims body = claimsJws.getBody();
            String userInfoJson = body.get(JWT_PAYLOAD_USER_KEY).toString();
            return JSONUtil.toBean(userInfoJson, userType);
        } catch (Exception e) {
            throw new JWTParseException("非法令牌");
        }
    }
}
