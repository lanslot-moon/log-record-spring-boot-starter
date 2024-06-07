package com.tuya.solution.record.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe XXXXXX
 * @since 2024/6/6 15:38
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogRecordModel {
    /**
     * id
     */
    private Serializable id;
    /**
     * 租户
     */
    private String tenant;

    /**
     * 保存的操作日志的类型，比如：订单类型、商品类型
     *
     */
    private String type;
    /**
     * 日志的子类型，比如订单的C端日志，和订单的B端日志，type都是订单类型，但是子类型不一样
     * @since 2.0.0 从 category 修改为 subtype
     */
    private String subType;

    /**
     * 日志绑定的业务标识
     */
    private String bizNo;
    /**
     * 操作人
     */
    private String operator;

    /**
     * 日志内容
     */
    private String action;
    /**
     * 记录是否是操作失败的日志
     */
    private boolean fail;
    /**
     * 日志的创建时间
     */
    private Long createTime;
    /**
     * 日志的额外信息
     *
     * @since 2.0.0 从detail 修改为extra
     */
    private String extra;

    /**
     * 打印日志的代码信息
     * CodeVariableType 日志记录的ClassName、MethodName
     */
    private Map<CodeVariableType, Object> codeVariable;
}

