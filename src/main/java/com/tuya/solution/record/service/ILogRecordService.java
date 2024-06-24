package com.tuya.solution.record.service;


import com.tuya.solution.record.beans.LogRecordModel;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 日志记录实现方式接口
 * @since 2024/6/5 11:03
 */
public interface ILogRecordService {


    /**
     * 日志记录
     *
     * @param logRecord 日志信息
     */
    void logRecord(LogRecordModel logRecord);
}
