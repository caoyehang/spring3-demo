package org.example.Vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.ApiErrorEnum;

/**
 * 统一接口响应体。
 * <p>
 * 控制器、全局异常处理、安全拦截失败都返回这个结构，前端只需要判断 code/message/data。
 */
@Data
@NoArgsConstructor
public class ApiRest<T> {
    /**
     * 成功默认状态。
     */
    private static final String CODE_SUCCESS = "000000";
    private static final String MSG_SUCCESS = "操作成功";

    /**
     * 通用失败默认状态。
     */
    private static final String CODE_FAILURE = "900000";
    private static final String MSG_FAILURE = "请求失败";

    /**
     * 业务状态码。
     */
    private String code;

    /**
     * 响应提示信息。
     */
    private String message;

    /**
     * 响应数据。
     */
    protected T data;

    /**
     * 按指定状态码、消息和数据构建响应。
     */
    public static <T> ApiRest<T> message(String code, String message, T data) {
        ApiRest<T> response = new ApiRest<>();
        response.setCode(code);
        response.setMessage(message);
        if (data != null) {
            response.setData(data);
        }
        return response;
    }

    /**
     * 无数据的成功响应。
     */
    public static <T> ApiRest<T> success() {
        return message(CODE_SUCCESS, MSG_SUCCESS, null);
    }

    /**
     * 自定义成功消息和数据。
     */
    public static <T> ApiRest<T> success(String message, T data) {
        return message(CODE_SUCCESS, message, data);
    }

    /**
     * 使用默认成功消息返回数据。
     */
    public static <T> ApiRest<T> success(T data) {
        return message(CODE_SUCCESS, MSG_SUCCESS, data);
    }

    /**
     * 无数据的通用失败响应。
     */
    public static <T> ApiRest<T> failure() {
        return message(CODE_FAILURE, MSG_FAILURE, null);
    }

    /**
     * 自定义失败消息和数据。
     */
    public static <T> ApiRest<T> failure(String message, T data) {
        return message(CODE_FAILURE, message, data);
    }

    /**
     * 自定义失败状态码、消息和数据。
     */
    public static <T> ApiRest<T> failure(String code, String message, T data) {
        return message(code, message, data);
    }

    /**
     * 使用默认失败消息返回数据。
     */
    public static <T> ApiRest<T> failure(T data) {
        return message(CODE_FAILURE, MSG_FAILURE, data);
    }

    /**
     * 根据错误枚举构建失败响应。
     */
    public static <T> ApiRest<T> failure(ApiErrorEnum apiErrorEnum) {
        return message(apiErrorEnum.getCode(), apiErrorEnum.getMessage(), null);
    }

    /**
     * 根据错误枚举构建带数据的失败响应。
     */
    public static <T> ApiRest<T> failure(ApiErrorEnum apiErrorEnum, T data) {
        return message(apiErrorEnum.getCode(), apiErrorEnum.getMessage(), data);
    }
}
