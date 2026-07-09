package org.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 接口错误码枚举。
 * <p>
 * 业务层、安全层和全局异常处理都从这里取错误码，避免同一个错误在不同位置返回不同格式。
 */
@AllArgsConstructor
@Getter
public enum ApiErrorEnum {
    LOGIN_USERNAME_NULL("200001", "用户名或密码不能为空"),
    LOGIN_CODE_UUID_NULL("200002", "验证码或 uuid 不能为空"),
    LOGIN_USERNAME_PASSWORD_INVALID("200003", "用户名或密码错误"),
    LOGIN_UUID_INVALID("200004", "验证码已过期"),
    LOGIN_CODE_INVALID("200005", "验证码不正确"),

    TOKEN_ILLEGALITY_INVALID("999998", "非法令牌"),
    TOKEN_ILLEGALITY_NULL("999999", "令牌不能为空"),
    AUTHENTICATION_REQUIRED("401000", "请先登录"),
    ACCESS_DENIED("403000", "没有访问权限"),

    PARAM_ERROR("400000", "请求参数错误"),
    SYSTEM_ERROR("500000", "系统异常");

    private final String code;
    private final String message;
}
