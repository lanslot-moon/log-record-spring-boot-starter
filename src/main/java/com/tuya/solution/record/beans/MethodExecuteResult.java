package com.tuya.solution.record.beans;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe XXXXXX
 * @since 2024/6/5 14:13
 */

@Data
public class MethodExecuteResult {

    private boolean success;
    private Throwable throwable;
    private String errorMsg;

    private Object result;
    private final Method method;
    private final Object[] args;
    private final Class<?> targetClass;

    public MethodExecuteResult(Method method, Object[] args, Class<?> targetClass) {
        this.method = method;
        this.args = args;
        this.targetClass = targetClass;
    }
}
