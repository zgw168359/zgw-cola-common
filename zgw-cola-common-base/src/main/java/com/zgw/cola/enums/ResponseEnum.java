package com.zgw.cola.enums;

/**
 * 响应码枚举
 *
 * @author zgw
 * @since 2021-03-31
 */
public enum ResponseEnum {
    SUCCESS("0", "成功"),
    PARAM_CHECK_ERROR("1001", "参数校验失败"),
    FAILURE("9999", "失败");

    private final String code;
    private final String message;

    ResponseEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
