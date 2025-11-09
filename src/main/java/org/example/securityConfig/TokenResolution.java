package org.example.securityConfig;

/**
 * 作者：Leo
 * 描述：token验证过滤器
 * 每一个servlet请求，只会执行一次
 */
import org.example.entity.User;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.Vo.ApiRest;
import org.example.enums.ApiErrorEnum;
import org.example.utils.JwtUtils;
import org.example.utils.RsaUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.io.ClassPathResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.util.ArrayList;

@Component
public class TokenResolution extends OncePerRequestFilter {
    /**
     *
     * @param request
     * @param response
     * @param filterChain  过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        // 判断如果是白名单接口就直接放行
        if (requestURI.equals("/user/captchaImage")) {
            filterChain.doFilter(request,response);
            return;
        }
        // 判断如果是白名单接口就直接放行
        if (requestURI.equals("/user/login")) {
            filterChain.doFilter(request,response);
            return;
        }
        // 1.从请求头中的Authorization获取token
        String token = request.getHeader("auto-token");
        // 如果token为空
        if (StringUtils.isEmpty(token)) {
            // 响应客户端
            responseClient(ApiRest.failure(ApiErrorEnum.TOKEN_ILLEGALITY_NULL, null), response);
            return;
        }
        // 校验令牌是否合法
        try {
            // 获取公钥
            ClassPathResource classPathResource = new ClassPathResource("key/pub_rsa");
            InputStream is = classPathResource.getInputStream();
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int rc;
            while ((rc = is.read(buff, 0, 1024)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            byte[] keyBytes = swapStream.toByteArray();
            PublicKey publicKey = RsaUtils.getPublicKey(keyBytes);
            // 拿到公钥后开始解密
            User users = (User) JwtUtils.getInfoFromToken(token, publicKey, User.class);
            // 封装用户信息
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(users, null, new ArrayList<>());
            // 将用户信息放入security的上下文
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            // 令牌合法后放行
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 如果抛出的异常是 令牌校验不合法的异常
            if (e instanceof JWTParseException) {
                // 非法令牌 状态码 405  返回的是 900001
                responseClient(ApiRest.failure(ApiErrorEnum.TOKEN_ILLEGALITY_INVALID, null), response);
                return;
            } else {
                // 如果是获取公钥的异常，那就是后端报错  状态码：500
                responseClient(ApiRest.failure(e.getMessage()), response);
                return;
            }
        }
    }
    // 校验令牌返回参数
    private void responseClient(ApiRest apiRest, HttpServletResponse response) {
        // 设置响应数据为json格式
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.write(JSONUtil.toJsonStr(apiRest));
        // 关闭流
        writer.close();
    }
}
