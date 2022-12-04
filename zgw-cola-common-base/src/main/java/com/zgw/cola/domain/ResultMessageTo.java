package com.zgw.cola.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简单信息传递对象
 *
 * @author 赵高文
 * @since 2022-09-03
 */
@Data
@NoArgsConstructor
public class ResultMessageTo {
    private boolean isSuccess;

    private String message;

    public ResultMessageTo(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public static ResultMessageTo buildFail(String message) {
        ResultMessageTo resultMessageTo = new ResultMessageTo();
        resultMessageTo.setSuccess(false);
        resultMessageTo.setMessage(message);
        return resultMessageTo;
    }

    public static ResultMessageTo defaultSuccess() {
        ResultMessageTo resultMessageTo = new ResultMessageTo();
        resultMessageTo.setSuccess(true);
        return resultMessageTo;
    }

    public static ResultMessageTo buildSuccess(String message) {
        ResultMessageTo resultMessageTo = new ResultMessageTo();
        resultMessageTo.setSuccess(true);
        resultMessageTo.setMessage(message);
        return resultMessageTo;
    }
}
