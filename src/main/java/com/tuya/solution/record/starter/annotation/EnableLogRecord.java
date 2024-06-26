package com.tuya.solution.record.starter.annotation;

import com.tuya.solution.record.starter.support.LogRecordConfigureSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe XXXXXX
 * @since 2024/6/5 11:24
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LogRecordConfigureSelector.class)
public @interface EnableLogRecord {

    String tenant();

    /**
     * ！不要删掉，为 null 就不代理了哦
     * true 都使用 CGLIB 代理
     * false 目标对象实现了接口 – 使用JDK动态代理机制(代理所有实现了的接口) 目标对象没有接口(只有实现类) – 使用CGLIB代理机制
     *
     * @return 不强制 cglib
     */
    boolean proxyTargetClass() default false;

    /**
     * Indicate how caching advice should be applied. The default is
     * {@link AdviceMode#PROXY}.
     *
     * @return 代理方式
     * @see AdviceMode
     */
    AdviceMode mode() default AdviceMode.PROXY;

    /**
     * 记录日志日志与业务日志是否同一个事务
     *
     * @return 默认独立
     */
    boolean joinTransaction() default false;

    /**
     * Indicate the ordering of the execution of the transaction advisor
     * when multiple advices are applied at a specific joinpoint.
     * <p>The default is {@link Ordered#LOWEST_PRECEDENCE}.
     *
     * @return 事务 advisor 的优先级
     */
    int order() default Ordered.LOWEST_PRECEDENCE;


    /**
     * 自定义解析默认超时告警时间
     * @return 超时告警时间 单位/ms
     */
    long alarmTime() default 500L;
}
