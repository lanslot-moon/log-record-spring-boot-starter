package com.tuya.solution.record.service.impl;

import com.tuya.solution.record.service.IFunctionService;
import com.tuya.solution.record.service.IParseFunction;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 默认自定义方法实现
 * @since 2024/6/4 18:31
 */
public class DefaultFunctionServiceImpl implements IFunctionService {

    private final ParseFunctionFactory parseFunctionFactory;

    public DefaultFunctionServiceImpl(ParseFunctionFactory parseFunctionFactory) {
        this.parseFunctionFactory = parseFunctionFactory;
    }

    @Override
    public String apply(String functionName, Object value) {
        IParseFunction function = parseFunctionFactory.getFunction(functionName);
        if (function == null) {
            return value.toString();
        }
        return function.apply(value);
    }

    @Override
    public boolean beforeFunction(String functionName) {
        return parseFunctionFactory.isBeforeFunction(functionName);
    }
}
