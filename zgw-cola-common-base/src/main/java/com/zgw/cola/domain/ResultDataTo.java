package com.zgw.cola.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResultDataTo<T> extends ResultMessageTo {
    private T data;

    public ResultDataTo(boolean isSuccess, String message, T data) {
        super(isSuccess, message);
        this.data = data;
    }
}
