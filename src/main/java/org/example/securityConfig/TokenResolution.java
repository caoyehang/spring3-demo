package org.example.securityConfig; // 当前类所在的安全配置包

import jakarta.annotation.PostConstruct; // 用于在 Bean 初始化完成后执行公钥加载逻辑
import jakarta.servlet.FilterChain; // Servlet 过滤器链对象，用于把请求继续交给后续过滤器或控制器
import jakarta.servlet.ServletException; // 过滤器处理请求时可能抛出的 Servlet 异常
import jakarta.servlet.http.HttpServletRequest; // HTTP 请求对象，用于读取请求路径、请求方法和请求头
import jakarta.servlet.http.HttpServletResponse; // HTTP 响应对象，用于在认证失败时直接写出 JSON
import org.example.Vo.ApiRest; // 项目统一接口返回结构
import org.example.entity.User; // token 中保存并解析出来的用户对象
import org.example.enums.ApiErrorEnum; // 项目统一错误码枚举
import org.example.utils.JwtUtils; // JWT 生成和解析工具类
import org.example.utils.RsaUtils; // RSA 公钥和私钥解析工具类
import org.springframework.core.io.ClassPathResource; // 用于从 classpath 读取 resources 目录下的公钥文件
import org.springframework.http.HttpHeaders; // Spring 提供的标准 HTTP 请求头常量
import org.springframework.http.HttpMethod; // Spring 提供的 HTTP 方法枚举
import org.springframework.http.HttpStatus; // Spring 提供的 HTTP 状态码枚举
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Spring Security 认证对象
import org.springframework.security.core.context.SecurityContextHolder; // Spring Security 上下文持有器
import org.springframework.stereotype.Component; // 标记当前类为 Spring Bean
import org.springframework.util.AntPathMatcher; // 用于匹配白名单接口路径
import org.springframework.util.StringUtils; // Spring 字符串工具，用于判断 token 是否有内容
import org.springframework.web.filter.OncePerRequestFilter; // 确保每个请求只执行一次的过滤器基类

import java.io.IOException; // 过滤器写响应或继续过滤器链时可能抛出的 IO 异常
import java.security.PublicKey; // RSA 公钥类型，用于校验 JWT 签名
import java.util.Collections; // 用于创建空权限集合

/**
 * JWT Token 解析过滤器。
 * <p>
 * 白名单接口直接放行，其他接口必须携带合法 token 才能继续访问。
 */
@Component // 交给 Spring 容器管理，SecurityConfig 中可以直接注入使用
public class TokenResolution extends OncePerRequestFilter { // 继承 OncePerRequestFilter，保证同一次请求只过滤一次
    private static final String LEGACY_TOKEN_HEADER = "auto-token"; // 兼容当前前端使用的自定义 token 请求头
    private static final String BEARER_PREFIX = "Bearer "; // 标准 Authorization 请求头中的 token 前缀

    private final AntPathMatcher pathMatcher = new AntPathMatcher(); // 路径匹配工具，用来判断当前请求是否命中白名单
    private PublicKey publicKey; // 缓存 RSA 公钥，避免每次请求都读取文件

    /**
     * Bean 初始化完成后读取一次 RSA 公钥。
     */
    @PostConstruct // Spring 创建并注入当前 Bean 后自动调用该方法
    public void initPublicKey() { // 初始化公钥，异常交给 Spring 启动流程暴露出来
        try {
            ClassPathResource classPathResource = new ClassPathResource("key/pub_rsa"); // 定位 resources/key/pub_rsa 公钥文件
            this.publicKey = RsaUtils.getPublicKey(classPathResource.getContentAsByteArray()); // 读取公钥内容并转换成 PublicKey 对象
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }

    /**
     * 判断当前请求是否不需要执行 token 过滤。
     */
    @Override // 重写 OncePerRequestFilter 的跳过过滤判断方法
    protected boolean shouldNotFilter(HttpServletRequest request) { // 返回 true 表示本次请求跳过 doFilterInternal
        if (HttpMethod.OPTIONS.matches(request.getMethod())) { // 浏览器跨域预检请求没有业务 token，需要直接放行
            return true; // 跳过 token 校验
        }
        String requestURI = request.getRequestURI(); // 获取当前请求路径，例如 /user/login
        for (String url : SecurityConfig.AUTH_WHITELIST) { // 遍历 SecurityConfig 中配置的白名单接口
            if (pathMatcher.match(url, requestURI)) { // 判断请求路径是否匹配某个白名单规则
                return true; // 命中白名单，跳过 token 校验
            }
        }
        return false; // 未命中白名单，继续执行 token 校验
    }

    /**
     * 从请求中解析 token，校验成功后把用户信息写入 Spring Security 上下文。
     */
    @Override // 重写 OncePerRequestFilter 的核心过滤逻辑
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) // request 是请求，response 是响应，filterChain 用于继续向后执行
            throws ServletException, IOException { // 方法签名保留 Servlet 过滤器可能抛出的异常
        String token = resolveToken(request); // 从 auto-token 或 Authorization 请求头中解析 token
        if (!StringUtils.hasText(token)) { // token 为空时说明用户没有登录凭证
            SecurityJsonResponse.write( // 过滤器内不能走 Controller，需要直接写出统一 JSON
                    response, // 当前 HTTP 响应对象
                    HttpStatus.UNAUTHORIZED.value(), // HTTP 状态码使用 401，表示未认证
                    ApiRest.failure(ApiErrorEnum.TOKEN_ILLEGALITY_NULL) // 业务响应体使用 ApiRest，提示令牌不能为空
            );
            return; // 已经写出错误响应，停止继续访问后续接口
        }

        try { // 捕获 token 解析和后续过滤器执行过程中的异常
            User user = (User) JwtUtils.getInfoFromToken(token, publicKey, User.class); // 使用公钥解析 token 并还原用户信息
            UsernamePasswordAuthenticationToken authentication = // 构建 Spring Security 可识别的认证对象
                    new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()); // principal 放用户对象，credentials 不保存密码，authorities 暂为空
            SecurityContextHolder.getContext().setAuthentication(authentication); // 把认证对象放入上下文，后续接口会认为当前请求已登录
            filterChain.doFilter(request, response); // token 合法，继续执行后续过滤器和目标 Controller
        } catch (JWTParseException e) { // JWT 工具类解析失败时会抛出这个自定义异常
            SecurityContextHolder.clearContext(); // 清理安全上下文，避免残留认证信息影响当前请求
            SecurityJsonResponse.write( // 返回统一 JSON 错误响应
                    response, // 当前 HTTP 响应对象
                    HttpStatus.UNAUTHORIZED.value(), // HTTP 状态码使用 401，表示 token 无效或未认证
                    ApiRest.failure(ApiErrorEnum.TOKEN_ILLEGALITY_INVALID) // 业务响应体提示非法令牌
            );
        } catch (Exception e) { // 兜底捕获其他异常，例如公钥异常或后续链路异常
            SecurityContextHolder.clearContext(); // 发生异常时清理安全上下文
            SecurityJsonResponse.write( // 返回统一 JSON 错误响应
                    response, // 当前 HTTP 响应对象
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), // HTTP 状态码使用 500，表示服务端异常
                    ApiRest.failure(ApiErrorEnum.SYSTEM_ERROR, e.getMessage()) // 业务响应体返回系统异常和异常消息
            );
        }
    }

    /**
     * 从请求头中解析 token。
     */
    private String resolveToken(HttpServletRequest request) { // 封装 token 获取逻辑，主流程只关心结果
        String token = request.getHeader(LEGACY_TOKEN_HEADER); // 优先读取旧版前端传入的 auto-token 请求头
        if (StringUtils.hasText(token)) { // 如果 auto-token 有内容，就直接使用它
            return token; // 返回自定义请求头中的 token
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION); // 读取标准 Authorization 请求头
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) { // 判断是否是 Bearer token 格式
            return authorization.substring(BEARER_PREFIX.length()); // 去掉 Bearer 前缀，只返回真正的 token 字符串
        }
        return null; // 两种请求头都没有 token 时返回 null
    }
}
