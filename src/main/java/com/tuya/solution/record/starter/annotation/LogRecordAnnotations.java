
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
public @interface LogRecordAnnotations {

    LogRecordAnnotation[] value();
}