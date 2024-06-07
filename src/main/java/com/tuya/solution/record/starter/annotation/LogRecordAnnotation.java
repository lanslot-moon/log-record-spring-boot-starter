
package com.tuya.solution.record.starter.annotation;

import java.lang.annotation.*;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 日志操作注解
 * @since 2024/6/4 17:35
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecordAnnotation {

    /**
     * 操作日志的文本模板
     */
    String success();

    /**
     * 操作日志的执行人
     */
    String fail() default "";

    /**
     * 操作日志的执行人
     */
    String operator() default "";

    /**
     * 操作日志绑定的业务对象标识
     */
    String bizNo();

    /**
     * 操作日志的种类
     */
    String category() default "";

    /**
     * 扩展参数，记录操作日志的修改详情
     */
    String detail() default "";

    /**
     * 记录日志的条件
     */
    String condition() default "";
}