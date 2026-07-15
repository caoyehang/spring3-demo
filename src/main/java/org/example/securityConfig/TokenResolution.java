package org.example.securityConfig;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.Vo.ApiRest;
import org.example.entity.User;
import org.example.enums.ApiErrorEnum;
import org.example.utils.JwtUtils;
import org.example.utils.RsaUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Collections;

/**
 * JWT Token 解析过滤器。
 * <p>
 * 白名单接口直接放行，其他接口必须携带合法 token 才能继续访问。
 */
@Component
public class TokenResolution extends OncePerRequestFilter {
    /**
     * 兼容当前前端使用的自定义 token 请求头。
     */
    private static final String LEGACY_TOKEN_HEADER = "auto-token";

    /**
     * 标准 Authorization 请求头中的 Bearer token 前缀。
     */
    private static final String BEARER_PREFIX = "Bearer ";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private PublicKey publicKey;

    /**
     * Bean 初始化完成后读取一次 RSA 公钥。
     */
    @PostConstruct
    public void initPublicKey() {
        try {
            ClassPathResource classPathResource = new ClassPathResource("key/pub_rsa");
            this.publicKey = RsaUtils.getPublicKey(classPathResource.getContentAsByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }

    /**
     * 判断当前请求是否不需要执行 token 过滤。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 浏览器跨域预检请求没有业务 token，需要直接放行。
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();
        for (String url : SecurityConfig.AUTH_WHITELIST) {
            if (pathMatcher.match(url, requestURI)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求中解析 token，校验成功后把用户信息写入 Spring Security 上下文。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            // 过滤器内不会进入 Controller，认证失败时直接写出统一 JSON。
            SecurityJsonResponse.write(
                    response,
                    HttpStatus.UNAUTHORIZED.value(),
                    ApiRest.failure(ApiErrorEnum.TOKEN_ILLEGALITY_NULL)
            );
            return;
        }

        try {
            User user = (User) JwtUtils.getInfoFromToken(token, publicKey, User.class);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JWTParseException e) {
            SecurityContextHolder.clearContext();
            SecurityJsonResponse.write(
                    response,
                    HttpStatus.UNAUTHORIZED.value(),
                    ApiRest.failure(ApiErrorEnum.TOKEN_ILLEGALITY_INVALID)
            );
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            SecurityJsonResponse.write(
                    response,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    ApiRest.failure(ApiErrorEnum.SYSTEM_ERROR, e.getMessage())
            );
        }
    }

    /**
     * 从请求头中解析 token。
     */
    private String resolveToken(HttpServletRequest request) {
        // 优先兼容旧版 auto-token；没有时再读取标准 Authorization: Bearer xxx。
        String token = request.getHeader(LEGACY_TOKEN_HEADER);
        if (StringUtils.hasText(token)) {
            return token;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
