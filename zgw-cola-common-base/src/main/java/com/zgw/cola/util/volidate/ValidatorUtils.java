package com.zgw.cola.util.volidate;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.hibernate.validator.HibernateValidator;

/**
 * 参数校验工具类
 *
 * @author zgw
 * @since 2021-03-21
 */
public class ValidatorUtils {

    private static final Validator VALIDATOR;

    static {
        VALIDATOR = Validation.byProvider(HibernateValidator.class)
                .configure()
                // 快速返回模式，有一个验证失败立即返回错误信息
                .failFast(true)
                .buildValidatorFactory()
                .getValidator();
    }

    public static <T> boolean validate(T object, Class<?>... groups) {
        if (object == null) {
            return false;
        }
        Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(object, groups);
        return constraintViolations.isEmpty();
    }

}
