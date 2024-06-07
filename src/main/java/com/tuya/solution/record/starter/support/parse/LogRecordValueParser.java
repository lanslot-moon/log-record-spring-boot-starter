package com.tuya.solution.record.starter.support.parse;

import com.tuya.solution.record.beans.MethodExecuteResult;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 解析需要存储的日志里面的SpeEL表达式
 * @since 2024/6/4 18:26
 */
@Setter
public class LogRecordValueParser implements BeanFactoryAware {

    private static final Pattern PATTERN = Pattern.compile("\\{\\s*(\\w*)\\s*\\{(.*?)}}");

    private final LogRecordExpressionEvaluator expressionEvaluator = new LogRecordExpressionEvaluator();

    protected BeanFactory beanFactory;

    private LogFunctionParser logFunctionParser;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public String singleProcessTemplate(MethodExecuteResult methodExecuteResult,
                                        String templates,
                                        Map<String, String> beforeFunctionNameAndReturnMap) {
        Map<String, String> stringStringMap = processTemplate(Collections.singletonList(templates), methodExecuteResult, beforeFunctionNameAndReturnMap);
        return stringStringMap.get(templates);
    }


    /**
     * 模版执行
     *
     * @param templates                      el表达式模版
     * @param methodExecuteResult            方法执行结果
     * @param beforeFunctionNameAndReturnMap 函数调用标志和方法执行前对应解析返回值
     * @return el表达式模版和最终解析数据映射
     */
    public Map<String, String> processTemplate(Collection<String> templates, MethodExecuteResult methodExecuteResult,
                                               Map<String, String> beforeFunctionNameAndReturnMap) {
        Map<String, String> expressionValues = new HashMap<>();
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(methodExecuteResult.getMethod(),
                methodExecuteResult.getArgs(), methodExecuteResult.getTargetClass(), methodExecuteResult.getResult(),
                methodExecuteResult.getErrorMsg(), beanFactory);

        for (String expressionTemplate : templates) {
            // 不包含{说明是个静态文本数据，原样输出即可
            if (!expressionTemplate.contains("{")) {
                expressionValues.put(expressionTemplate, expressionTemplate);
                continue;
            }

            Matcher matcher = PATTERN.matcher(expressionTemplate);
            StringBuilder parsedResultMsg = new StringBuilder();
            AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(methodExecuteResult.getMethod(), methodExecuteResult.getTargetClass());
            while (matcher.find()) {
                String expression = matcher.group(2);
                // 获取到自定义函数名
                String functionName = matcher.group(1);
                // 对el表达式进行解析
                Object value = expressionEvaluator.parseExpression(expression, annotatedElementKey, evaluationContext);
                // 获取根据自定义解析方法后的参数，例如通过el表达式获取到user.id为1111,在通过自定义解析函数转义1111为用户信息为张三
                String functionReturnValue = logFunctionParser.getFunctionReturnValue(beforeFunctionNameAndReturnMap, value, expression, functionName);
                // 用解析后的value值替换掉el表达式
                matcher.appendReplacement(parsedResultMsg, Matcher.quoteReplacement(functionReturnValue == null ? "" : functionReturnValue));
            }
            matcher.appendTail(parsedResultMsg);
            expressionValues.put(expressionTemplate, parsedResultMsg.toString());
        }
        return expressionValues;
    }


    /**
     * 解析接口执行前的的处理类
     *
     * @param templates   方法执行链路上所有模版
     * @param targetClass 目标方法
     * @param method      执行方法
     * @param args        执行方法上的参数
     * @return 函数调用标志和方法执行前对应解析返回值
     */
    public Map<String, String> processBeforeExecuteFunctionTemplate(Collection<String> templates, Class<?> targetClass,
                                                                    Method method, Object[] args) {
        Map<String, String> functionNameAndReturnValueMap = new HashMap<>();
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(method, args, targetClass, null, null, beanFactory);

        // extra = "{{#order.toString()}}"  success = "更新了订单{ORDER{#orderId}},更新内容为..."
        for (String expressionTemplate : templates) {
            // 如果存在el表达式则进行解析
            if (!expressionTemplate.contains("{")) {
                continue;
            }

            Matcher matcher = PATTERN.matcher(expressionTemplate);
            while (matcher.find()) {
                String expression = matcher.group(2);
                if (expression.contains("#_ret") || expression.contains("#_errorMsg")) {
                    continue;
                }
                AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(method, targetClass);
                String functionName = matcher.group(1);

                //  el表达式存在自定义解析器并且实在方法执行前开启则进行解析
                boolean existFunction = StringUtils.isNotBlank(functionName) && logFunctionParser.beforeFunction(functionName);
                if (Objects.equals(Boolean.FALSE, existFunction)) {
                    continue;
                }

                // 对el表达式进行解析
                Object value = expressionEvaluator.parseExpression(expression, annotatedElementKey, evaluationContext);
                // 获取根据自定义解析方法后的参数，例如通过el表达式获取到user.id为1111,在通过自定义解析函数转义1111为用户信息为张三
                String functionReturnValue = logFunctionParser.getFunctionReturnValue(null, value, expression, functionName);
                // 方法执行之前换成函数的结果，此时函数调用的唯一标志：函数名+参数表达式
                String functionCallInstanceKey = logFunctionParser.getFunctionCallInstanceKey(functionName, expression);
                // 把当前函数调用标志和解析返回值放入Map中返回
                functionNameAndReturnValueMap.put(functionCallInstanceKey, functionReturnValue);
            }
        }
        return functionNameAndReturnValueMap;
    }
}
