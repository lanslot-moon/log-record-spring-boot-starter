package com.tuya.solution.record.service;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe XXXXXX
 * @since 2024/6/4 18:34
 */
public interface IFunctionService {

    String apply(String functionName, Object value);

    boolean beforeFunction(String functionName);
}
