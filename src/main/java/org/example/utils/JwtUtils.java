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
 *  * 作者：Leo
 * 生成token以及校验token相关方法
 */
public class JwtUtils {

    private static final String JWT_PAYLOAD_USER_KEY = "user";

    /**
     * 私钥加密token
     * @param userInfo   载荷中的数据
     * @param privateKey 私钥
     * @param expire     过期时间，单位分钟
     * @return JWT
     */
    public static String generateTokenExpireInMinutes(Object userInfo, PrivateKey privateKey, int expire) {
        //计算过期时间
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE,expire);

        return Jwts.builder()
                .claim(JWT_PAYLOAD_USER_KEY, JSONUtil.toJsonStr(userInfo))
                .setId(new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes())))
                .setExpiration(c.getTime())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 私钥加密token
     * @param userInfo   载荷中的数据
     * @param privateKey 私钥
     * @param expire     过期时间，单位秒
     * @return JWT
     */
    public static String generateTokenExpireInSeconds(Object userInfo, PrivateKey privateKey, int expire) {
        //计算过期时间
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND,expire);

        return Jwts.builder()  //{"user":"{id:12,name:jack}"}
                .claim(JWT_PAYLOAD_USER_KEY, JSONUtil.toJsonStr(userInfo))
                .setId(new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes())))
                .setExpiration(c.getTime())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }






    /**
     * 获取token中的用户信息
     *
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     */
    public static  Object getInfoFromToken(String token, PublicKey publicKey, Class userType)  {
        //解析token
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);

            Claims body = claimsJws.getBody();
            String userInfoJson = body.get(JWT_PAYLOAD_USER_KEY).toString();
            return JSONUtil.toBean(userInfoJson, userType);
        }catch (Exception e){
            // 令牌解析异常就抛自定义的异常
            throw new JWTParseException("非法令牌");
        }

    }



}


