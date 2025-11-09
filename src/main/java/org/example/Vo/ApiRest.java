package org.example.Vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.ApiErrorEnum;

/**
 * 作者：Leo
 * 描述：返回结果的封装
 */
@Data
@NoArgsConstructor
public class ApiRest<T> {
    /**
     * 成功默认消息
     */
    private static final String CODE_SUCCESS = "000000";
    private static final String MSG_SUCCESS = "操作成功";
    /**
     * 失败默认消息
     */
    private static final String CODE_FAILURE = "900000";
    private static final String MSG_FAILURE = "请求失败";
    /**
     * 响应代码
     */
    private String code;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 请求或响应body
     */
    protected T data;

    /**
     * 完成消息构造
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
     * 请求成功空数据
     * 如果请求成功但是没有数据就直接调用这个方法
     */
    public static <T> ApiRest<T> success() {
        return message(CODE_SUCCESS, MSG_SUCCESS, null);
    }

    /**
     * 请求成功，通用代码
     *
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ApiRest<T> success(String message, T data) {
        return message(CODE_SUCCESS, message, data);
    }
    /**
     * 请求成功，仅内容
     * @param data
     * @param <T>
     * @return
     */
    public static  <T> ApiRest<T> success(T data){
        return message(CODE_SUCCESS, MSG_SUCCESS, data);
    }
    /**
     * 请求失败
     */
    public static  <T> ApiRest<T> failure(){
        return message(CODE_FAILURE, MSG_FAILURE, null);
    }
    /**
     * 请求失败，消息加内容
     */
    public static <T> ApiRest<T> failure(String message, T data) {
        return  message(CODE_FAILURE, message, data);
    }
    /**
     * 请求失败，状态加消息加内容
     */
    public static <T> ApiRest<T> failure(String code, String message, T data) {
        return message(code, message, data);
    }
    /**
     * 请求失败，仅内容
     * @param data
     * @param <T>
     * @return
     */
    public static  <T> ApiRest<T> failure(T data){
        return message(CODE_FAILURE, MSG_FAILURE, data);
    }
    /**
     * 请求失败 ，枚举加内容为空
     */
    public static <T> ApiRest<T> failure(ApiErrorEnum apiErrorEnum) {
        return message(apiErrorEnum.getCode(), apiErrorEnum.getMessage(), null);
    }
    /**
     * 请求失败 ，枚举加内容
     */
    public static <T> ApiRest<T> failure(ApiErrorEnum apiErrorEnum, T data) {
        return message(apiErrorEnum.getCode(), apiErrorEnum.getMessage(), data);
    }
}
