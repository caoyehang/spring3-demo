package org.example.securityConfig;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

/**
 * 作者：Leo
 * 描述：过滤器的主要配置：拦截什么放行什么
 */
// 配置类注解
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * 用来创建ProviderManager（AuthenticationManager的实现）
     */
    @Resource
    private AuthenticationConfiguration authenticationConfiguration;
    // 引入自定义过滤器
    @Resource
    private TokenResolution tokenResolution;
    // 配置security
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  //关闭 CSRF 机制
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) //配置跨域
                //配置请求的拦截方式
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/user/captchaImage", "user/login") // 放行接口
                        .permitAll()
                        .anyRequest()
                        .authenticated()              // 其他路径需要认证
                );
        // 添加tokne校验过滤器到过滤器链中，并配置执行顺序
        http.addFilterBefore(tokenResolution, UsernamePasswordAuthenticationFilter.class);
        // 允许跨域
        return http.build();
    }
    /**
     * AuthenticationManager实现，用于管理所有AuthenticationProvider实现的一个管理器。
     * 使用自定义密码登录时需要的一个Bean
     *
     * @return {@link AuthenticationManager}
     * @throws Exception 例外
     */
    @Bean
    public AuthenticationManager getAuthenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    //配置跨域
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));  // 设置允许的源
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
