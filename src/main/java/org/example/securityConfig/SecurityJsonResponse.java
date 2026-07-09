package org.example.securityConfig;

import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.example.Vo.ApiRest;

import java.io.IOException;

/**
 * Security 过滤器链内的统一 JSON 响应工具。
 * <p>
 * 过滤器、认证失败处理器和权限失败处理器不经过 Controller，所以需要在这里直接写出 ApiRest。
 */
public final class SecurityJsonResponse {
    private SecurityJsonResponse() {
    }

    /**
     * 将 ApiRest 按 JSON 格式写入响应，并同步设置 HTTP 状态码。
     */
    public static void write(HttpServletResponse response, int httpStatus, ApiRest<?> apiRest) throws IOException {
        response.setStatus(httpStatus);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(apiRest));
    }
}
