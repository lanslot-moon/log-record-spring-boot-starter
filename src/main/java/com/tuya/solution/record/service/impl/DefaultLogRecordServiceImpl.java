package com.tuya.solution.record.service.impl;

import com.tuya.solution.record.beans.LogRecordModel;
import com.tuya.solution.record.service.ILogRecordService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 默认日志记录实现方式
 * @since 2024/6/5 11:22
 */
@Slf4j
public class DefaultLogRecordServiceImpl implements ILogRecordService {
    @Override
    public void record(LogRecordModel logRecord) {
        log.info("进入默认日志实现接口......");
    }
}
