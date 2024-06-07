package com.tuya.solution.record.beans;

import lombok.Builder;
import lombok.Data;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe XXXXXX
 * @since 2024/6/5 14:15
 */
@Data
@Builder
public class LogRecordOps {
    /**
     * 成功日志记录模版
     */
    private String successLogTemplate;

    /**
     * 失败日志记录模版
     */
    private String failLogTemplate;

    /**
     * 操作人Id
     */
    private String operatorId;

    /**
     * 操作类型
     */
    private String type;

    /**
     * 业务Id
     */
    private String bizNo;
    private String subType;

    /**
     * 额外参数
     */
    private String extra;

    /**
     * 日志记录条件
     */
    private String condition;
}
