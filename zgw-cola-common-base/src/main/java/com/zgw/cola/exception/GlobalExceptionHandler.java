package com.zgw.cola.exception;

import com.alibaba.cola.dto.Response;
import com.zgw.cola.enums.ResponseEnum;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理工具
 *
 * @author zgw
 * @since 2021-03-31
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Response handleException() {
        return Response.buildFailure(ResponseEnum.FAILURE.getCode(), ResponseEnum.FAILURE.getMessage());
    }

    @ExceptionHandler(BizException.class)
    public Response handleBizException(BizException e) {
        return Response.buildFailure(e.getErrCode(), e.getErrMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response handleMethodArgumentNotValidException() {
        return Response.buildFailure(ResponseEnum.PARAM_CHECK_ERROR.getCode(), ResponseEnum.PARAM_CHECK_ERROR.getMessage());
    }

}
