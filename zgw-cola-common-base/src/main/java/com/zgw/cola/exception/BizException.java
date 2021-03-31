package com.zgw.cola.exception;

import com.zgw.cola.enums.ResponseEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义业务异常
 *
 * @author zgw
 * @since 2021-03-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String errCode;
    private String errMessage;

    public BizException(String errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public BizException(ResponseEnum responseEnum) {
        this.errCode = responseEnum.getCode();
        this.errMessage = responseEnum.getMessage();
    }
}
