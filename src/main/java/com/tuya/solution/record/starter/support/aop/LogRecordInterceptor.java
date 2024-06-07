package com.tuya.solution.record.starter.support.aop;

import com.tuya.solution.record.beans.LogRecordOps;
import com.tuya.solution.record.beans.MethodExecuteResult;
import com.tuya.solution.record.beans.CodeVariableType;
import com.tuya.solution.record.beans.LogRecordModel;
import com.tuya.solution.record.service.IFunctionService;
import com.tuya.solution.record.context.LogRecordContext;
import com.tuya.solution.record.starter.support.parse.LogFunctionParser;
import com.tuya.solution.record.service.ILogRecordService;
import com.tuya.solution.record.starter.support.parse.LogRecordValueParser;
import com.tuya.solution.record.service.IOperatorGetService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.CollectionUtils;


import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 日志记录注解切面
 * @since 2024/6/5 10:53
 */
@Setter
@Slf4j
public class LogRecordInterceptor extends LogRecordValueParser implements MethodInterceptor, Serializable, SmartInitializingSingleton {

    private LogRecordOperationSource logRecordOperationSource;

    private String tenantId;

    private ILogRecordService bizLogService;

    private IOperatorGetService operatorGetService;

    private boolean joinTransaction;

    @Override
    public void afterSingletonsInstantiated() {
        bizLogService = beanFactory.getBean(ILogRecordService.class);
        operatorGetService = beanFactory.getBean(IOperatorGetService.class);
        this.setLogFunctionParser(new LogFunctionParser(beanFactory.getBean(IFunctionService.class)));
    }


    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        //记录日志
        return execute(methodInvocation, methodInvocation.getThis(), method, methodInvocation.getArguments());
    }

    private Object execute(MethodInvocation invoker, Object target, Method method, Object[] args) throws Throwable {
        //如果是代理接口则不拦截
        if (AopUtils.isAopProxy(target)) {
            return invoker.proceed();
        }
        Class<?> targetClass = AopUtils.getTargetClass(target);
        Object ret = null;
        MethodExecuteResult methodExecuteResult = new MethodExecuteResult(method, args, targetClass);
        LogRecordContext.putEmptySpan();
        Collection<LogRecordOps> operations = new ArrayList<>();
        Map<String, String> functionNameAndReturnMap = new HashMap<>();
        try {
            // 找到方法链路上所有携带LogRecordAnnotation的日志记录操作
            operations = logRecordOperationSource.computeLogRecordOperations(method, targetClass);
            // 找到方法链路上所有的El表达式模版
            List<String> spElTemplates = getBeforeExecuteFunctionTemplate(operations);
            // //业务逻辑执行前的自定义函数解析
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(spElTemplates, targetClass, method, args);
        } catch (Exception e) {
            log.error("LogRecordInterceptor invoke 日志记录方法执行前自定义函数解析出现异常:", e);
        }

        try {
            ret = invoker.proceed();
            methodExecuteResult.setResult(ret);
            methodExecuteResult.setSuccess(true);
        } catch (Exception e) {
            methodExecuteResult.setSuccess(false);
            methodExecuteResult.setThrowable(e);
            methodExecuteResult.setErrorMsg(e.getMessage());
        }
        try {
            if (!CollectionUtils.isEmpty(operations)) {
                recordExecute(methodExecuteResult, functionNameAndReturnMap, operations);
            }
        } catch (Exception t) {
            log.error("LogRecordInterceptor invoke 日志记录出现异常:", t);
            throw t;
        } finally {
            LogRecordContext.clear();
        }

        if (methodExecuteResult.getThrowable() != null) {
            throw methodExecuteResult.getThrowable();
        }
        return ret;
    }

    private List<String> getBeforeExecuteFunctionTemplate(Collection<LogRecordOps> operations) {
        List<String> spElTemplates = new ArrayList<>();
        for (LogRecordOps operation : operations) {
            //执行之前的函数，失败模版不解析
            List<String> templates = getSpElTemplates(operation, operation.getSuccessLogTemplate());
            if (!CollectionUtils.isEmpty(templates)) {
                spElTemplates.addAll(templates);
            }
        }

        return spElTemplates.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }


    private void recordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                               Collection<LogRecordOps> operations) {
        for (LogRecordOps operation : operations) {
            try {
                // 成功和失败模版都没有直接返回
                if (StringUtils.isAllBlank(operation.getSuccessLogTemplate(), operation.getFailLogTemplate())) {
                    continue;
                }

                // 判断记录日志的条件是否满足
                if (exitsCondition(methodExecuteResult, functionNameAndReturnMap, operation)) {
                    continue;
                }

                // 根据业务是否执行成功执行对应日志解析模版
                if (methodExecuteResult.isSuccess()) {
                    successRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
                } else {
                    failRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
                }
            } catch (Exception t) {
                log.error("LogRecordInterceptor recordExecute 日志记录出现异常:", t);
                // 如果日志记录失败
                if (joinTransaction) {
                    throw t;
                }
            }
        }
    }

    /**
     * 保存成功条件模版
     *
     * @param methodExecuteResult      方法执行结果
     * @param functionNameAndReturnMap 方法执行前的函数解析
     * @param operation                当前方法上的注解参数
     */
    private void successRecordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                                      LogRecordOps operation) {
        if (StringUtils.isEmpty(operation.getSuccessLogTemplate())) {
            log.warn("LogRecordInterceptor successRecordExecute 不存在成功模版...");
            return;
        }
        String action = operation.getSuccessLogTemplate();
        List<String> spElTemplates = getSpElTemplates(operation, action);
        String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);
        Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
        saveLog(methodExecuteResult.getMethod(), true, operation, operatorIdFromService, action, expressionValues);
    }

    /**
     * 保存失败模版处理器
     *
     * @param methodExecuteResult      方法执行结果
     * @param functionNameAndReturnMap 方法执行前的函数解析
     * @param operation                当前方法上的注解参数
     */
    private void failRecordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                                   LogRecordOps operation) {
        if (StringUtils.isEmpty(operation.getFailLogTemplate())) {
            log.warn("LogRecordInterceptor failRecordExecute 不存在失败模版...");
            return;
        }

        String action = operation.getFailLogTemplate();
        List<String> spElTemplates = getSpElTemplates(operation, action);
        String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);

        Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
        saveLog(methodExecuteResult.getMethod(), true, operation, operatorIdFromService, action, expressionValues);
    }

    /**
     * 判断记录日志的条件是否满足
     *
     * @param methodExecuteResult      方法执行结果
     * @param functionNameAndReturnMap
     * @param operation
     * @return
     */
    private boolean exitsCondition(MethodExecuteResult methodExecuteResult,
                                   Map<String, String> functionNameAndReturnMap, LogRecordOps operation) {
        if (!StringUtils.isEmpty(operation.getCondition())) {
            String condition = singleProcessTemplate(methodExecuteResult, operation.getCondition(), functionNameAndReturnMap);
            return StringUtils.endsWithIgnoreCase(condition, "false");
        }
        return false;
    }

    /**
     * 获取操作用户信息
     *
     * @param operation     接口注解信息
     * @param spElTemplates el表达式模版
     * @return 真实操作用户信息
     */
    private String getOperatorIdFromServiceAndPutTemplate(LogRecordOps operation, List<String> spElTemplates) {
        // 如果存在则使用对应的用户解析器
        if (StringUtils.isNotBlank(operation.getOperatorId())) {
            spElTemplates.add(operation.getOperatorId());
            return null;
        }

        // 不存在则使用默认实现接口获取用户Id
        String realOperatorId = operatorGetService.getUser().getOperatorId();
        if (StringUtils.isEmpty(realOperatorId)) {
            throw new IllegalArgumentException("LogRecordInterceptor getOperatorIdFromServiceAndPutTemplate 获取用户Id出现异常......");
        }
        return realOperatorId;
    }

    /**
     * 获取真实操作用户信息
     * @param operation 方法注解信息
     * @param operatorIdFromService 真实用户信息
     * @param expressionValues 表达式参数信息
     * @return
     */
    private String getRealOperatorId(LogRecordOps operation, String operatorIdFromService, Map<String, String> expressionValues) {
        return StringUtils.isNotBlank(operatorIdFromService) ? operatorIdFromService : expressionValues.get(operation.getOperatorId());
    }

    private void saveLog(Method method, boolean flag, LogRecordOps operation, String operatorIdFromService,
                         String action, Map<String, String> expressionValues) {
        if (StringUtils.isEmpty(expressionValues.get(action)) || Objects.equals(action, expressionValues.get(action))) {
            return;
        }
        LogRecordModel logRecord = LogRecordModel.builder()
                .tenant(tenantId)
                .type(expressionValues.get(operation.getType()))
                .bizNo(expressionValues.get(operation.getBizNo()))
                .operator(getRealOperatorId(operation, operatorIdFromService, expressionValues))
                .subType(expressionValues.get(operation.getSubType()))
                .extra(expressionValues.get(operation.getExtra()))
                .codeVariable(getCodeVariable(method))
                .action(expressionValues.get(action))
                .fail(flag)
                .createTime(System.currentTimeMillis())
                .build();

        bizLogService.record(logRecord);
    }

    private Map<CodeVariableType, Object> getCodeVariable(Method method) {
        Map<CodeVariableType, Object> map = new HashMap<>();
        map.put(CodeVariableType.ClassName, method.getDeclaringClass());
        map.put(CodeVariableType.MethodName, method.getName());
        return map;
    }

    private List<String> getSpElTemplates(LogRecordOps operation, String... actions) {
        List<String> spElTemplates = new ArrayList<>();
        spElTemplates.add(operation.getType());
        spElTemplates.add(operation.getBizNo());
        spElTemplates.add(operation.getSubType());
        spElTemplates.add(operation.getExtra());
        spElTemplates.addAll(Arrays.asList(actions));
        return spElTemplates.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }
}
