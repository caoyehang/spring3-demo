package org.example.enums;
import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * 作者：Leo
 * 描述：永无bug
 */


@AllArgsConstructor
@Getter
public enum ApiErrorEnum {
    LOGIN_USERNAME_NULL("200001", "用户名不能为空"),
    LOGIN_CODE_UUID_NULL("200002", "验证码或者uuid不能为空"),
    LOGIN_USERNAME_PASSWORD_INVALID("200003", "用户名或者密码错误"),
    LOGIN_UUID_INVALID("200004", "验证码已过期"),
    LOGIN_CODE_INVALID("200005", "验证码不正确"),
    TOKEN_ILLEGALITY_INVALID("999999", "非法令牌"),
    TOKEN_ILLEGALITY_NULL("999999", "令牌为空");
    private final String code;
    private final String message;
}
