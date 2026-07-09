package org.example.securityConfig;

import jakarta.annotation.Resource;
import org.example.Vo.ApiRest;
import org.example.enums.ApiErrorEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 主配置。
 * <p>
 * 这里统一定义放行接口、接口拦截规则、跨域策略和认证/权限失败时的 ApiRest 响应。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    /**
     * 不需要登录就可以访问的接口。
     */
    public static final String[] AUTH_WHITELIST = {
            "/user/captchaImage",
            "/user/login"
    };

    /**
     * 用来获取 Spring Security 创建的 AuthenticationManager。
     */
    @Resource
    private AuthenticationConfiguration authenticationConfiguration;

    /**
     * 自定义 JWT 校验过滤器，负责从请求头解析 token 并写入安全上下文。
     */
    @Resource
    private TokenResolution tokenResolution;

    /**
     * 配置安全过滤器链。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离项目通常使用 token 认证，不使用 CSRF Token。
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // JWT 是无状态认证，服务端不创建 Session。
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        // 未登录或认证失败时，统一返回 ApiRest。
                        .authenticationEntryPoint((request, response, authException) ->
                                SecurityJsonResponse.write(
                                        response,
                                        HttpStatus.UNAUTHORIZED.value(),
                                        ApiRest.failure(ApiErrorEnum.AUTHENTICATION_REQUIRED)
                                ))
                        // 已登录但权限不足时，统一返回 ApiRest。
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                SecurityJsonResponse.write(
                                        response,
                                        HttpStatus.FORBIDDEN.value(),
                                        ApiRest.failure(ApiErrorEnum.ACCESS_DENIED)
                                )))
                .authorizeHttpRequests(authz -> authz
                        // 浏览器跨域预检请求直接放行。
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        // 其他接口都需要通过 TokenResolution 完成认证。
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(tokenResolution, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 暴露 AuthenticationManager，后续如需接入 Spring Security 登录认证可直接注入。
     */
    @Bean
    public AuthenticationManager getAuthenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 跨域配置。
     * <p>
     * allowedOriginPatterns 支持通配符；当前没有携带 Cookie，所以不启用 allowCredentials。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("auto-token", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
