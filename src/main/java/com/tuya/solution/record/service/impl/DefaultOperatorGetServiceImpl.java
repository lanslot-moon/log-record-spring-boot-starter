package com.tuya.solution.record.service.impl;

import com.tuya.solution.record.beans.Operator;
import com.tuya.solution.record.service.IOperatorGetService;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 默认获取操作者Service
 * @since 2024/6/5 14:04
 */
public class DefaultOperatorGetServiceImpl implements IOperatorGetService {

    @Override
    public Operator getUser() {
        return new Operator("1111111111");
    }
}
