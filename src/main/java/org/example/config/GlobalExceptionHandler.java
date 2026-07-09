package org.example.config;

import org.example.Vo.ApiRest;
import org.example.enums.ApiErrorEnum;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理。
 * <p>
 * Controller 中没有手动捕获的异常会进入这里，保证异常响应也统一使用 ApiRest。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 请求缺少参数、参数类型不正确或 JSON 体格式错误时，返回参数错误。
     */
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ApiRest<String> handleParamException(Exception e) {
        return ApiRest.failure(ApiErrorEnum.PARAM_ERROR, e.getMessage());
    }

    /**
     * 未知异常兜底，避免把默认错误页或非统一 JSON 返回给前端。
     */
    @ExceptionHandler(Exception.class)
    public ApiRest<String> handleException(Exception e) {
        return ApiRest.failure(ApiErrorEnum.SYSTEM_ERROR, e.getMessage());
    }
}
