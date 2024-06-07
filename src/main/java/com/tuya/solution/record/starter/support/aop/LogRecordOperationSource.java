package com.tuya.solution.record.starter.support.aop;

import com.tuya.solution.record.beans.LogRecordOps;
import com.tuya.solution.record.starter.annotation.LogRecordAnnotation;
import com.tuya.solution.record.starter.annotation.LogRecordAnnotations;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;


/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 日志记录操作来源
 * @since 2024/6/4 18:38
 */
public class LogRecordOperationSource {


    public Collection<LogRecordOps> computeLogRecordOperations(Method method, Class<?> targetClass) {
        // Don't allow no-public methods as required.
        if (!Modifier.isPublic(method.getModifiers())) {
            return Collections.emptyList();
        }

        // The method may be on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        // If we are dealing with method with generic parameters, find the original method.
        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

        // First try is the method in the target class.
        Collection<LogRecordOps> logRecordOps = parseLogRecordAnnotations(specificMethod);
        Collection<LogRecordOps> logRecordsOps = parseLogRecordsAnnotations(specificMethod);
        Collection<LogRecordOps> abstractLogRecordOps = parseLogRecordAnnotations(ClassUtils.getInterfaceMethodIfPossible(method));
        Collection<LogRecordOps> abstractLogRecordsOps = parseLogRecordsAnnotations(ClassUtils.getInterfaceMethodIfPossible(method));
        HashSet<LogRecordOps> result = new HashSet<>();
        result.addAll(logRecordOps);
        result.addAll(abstractLogRecordOps);
        result.addAll(logRecordsOps);
        result.addAll(abstractLogRecordsOps);
        return result;
    }

    private Collection<LogRecordOps> parseLogRecordsAnnotations(AnnotatedElement ae) {
        Collection<LogRecordOps> res = new ArrayList<>();
        Collection<LogRecordAnnotations> logRecordAnnotationAnnotations = AnnotatedElementUtils.findAllMergedAnnotations(ae, LogRecordAnnotations.class);
        if (!logRecordAnnotationAnnotations.isEmpty()) {
            logRecordAnnotationAnnotations.forEach(logRecords -> {
                LogRecordAnnotation[] value = logRecords.value();
                for (LogRecordAnnotation logRecord : value) {
                    res.add(parseLogRecordAnnotation(ae, logRecord));
                }
            });
        }
        return res;
    }

    private Collection<LogRecordOps> parseLogRecordAnnotations(AnnotatedElement ae) {
        Collection<LogRecordAnnotation> logRecordAnnotationAnnotations = AnnotatedElementUtils.findAllMergedAnnotations(ae, LogRecordAnnotation.class);
        Collection<LogRecordOps> ret = new ArrayList<>();
        if (!logRecordAnnotationAnnotations.isEmpty()) {
            for (LogRecordAnnotation recordAnnotation : logRecordAnnotationAnnotations) {
                ret.add(parseLogRecordAnnotation(ae, recordAnnotation));
            }
        }
        return ret;
    }

    private LogRecordOps parseLogRecordAnnotation(AnnotatedElement ae, LogRecordAnnotation recordAnnotation) {
        LogRecordOps recordOps = LogRecordOps.builder()
                .successLogTemplate(recordAnnotation.success())
                .failLogTemplate(recordAnnotation.fail())
                .type(recordAnnotation.category())
                .bizNo(recordAnnotation.bizNo())
                .operatorId(recordAnnotation.operator())
                .extra(recordAnnotation.detail())
                .condition(recordAnnotation.condition())
                .build();
        validateLogRecordOperation(ae, recordOps);
        return recordOps;
    }


    private void validateLogRecordOperation(AnnotatedElement ae, LogRecordOps recordOps) {
        if (!StringUtils.hasText(recordOps.getSuccessLogTemplate()) && !StringUtils.hasText(recordOps.getFailLogTemplate())) {
            throw new IllegalStateException("Invalid logRecord annotation configuration on '" + ae.toString()
                    + "'. 'one of successTemplate and failLogTemplate' attribute must be set.");
        }
    }
}
