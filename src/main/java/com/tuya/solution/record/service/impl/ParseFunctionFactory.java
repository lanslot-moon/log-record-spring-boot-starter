package com.tuya.solution.record.service.impl;

import com.tuya.solution.record.service.IParseFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 自定义函数解析工厂
 * @since 2024/6/4 18:31
 */
public class ParseFunctionFactory {

    private Map<String, IParseFunction> allFunctionMap = new HashMap<>();

    public ParseFunctionFactory(List<IParseFunction> parseFunctions) {
        if (CollectionUtils.isEmpty(parseFunctions)) {
            return;
        }

        parseFunctions.stream()
                .filter(item -> StringUtils.isNotBlank(item.functionName()))
                .forEach(itemEntity -> allFunctionMap.put(itemEntity.functionName(), itemEntity));
    }

    public IParseFunction getFunction(String functionName) {
        return allFunctionMap.get(functionName);
    }

    public boolean isBeforeFunction(String functionName) {
        return allFunctionMap.get(functionName) != null && allFunctionMap.get(functionName).executeBefore();
    }
}
