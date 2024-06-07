package com.tuya.solution.record.starter.autoconfigure;

import com.tuya.solution.record.service.IFunctionService;
import com.tuya.solution.record.service.ILogRecordService;
import com.tuya.solution.record.service.IOperatorGetService;
import com.tuya.solution.record.service.IParseFunction;
import com.tuya.solution.record.starter.annotation.EnableLogRecord;
import com.tuya.solution.record.starter.support.aop.BeanFactoryLogRecordAdvisor;
import com.tuya.solution.record.starter.support.aop.LogRecordInterceptor;
import com.tuya.solution.record.service.impl.DefaultFunctionServiceImpl;
import com.tuya.solution.record.service.impl.DefaultLogRecordServiceImpl;
import com.tuya.solution.record.starter.support.aop.LogRecordOperationSource;
import com.tuya.solution.record.service.impl.DefaultParseFunction;
import com.tuya.solution.record.service.impl.ParseFunctionFactory;
import com.tuya.solution.record.service.impl.DefaultOperatorGetServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 日志代理自动配置类
 * @since 2024/6/4 18:42
 */
@Configuration
@Slf4j
public class LogRecordProxyAutoConfiguration implements ImportAware {

    private AnnotationAttributes enableLogRecord;

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordOperationSource logRecordOperationSource() {
        return new LogRecordOperationSource();
    }

    @Bean
    @ConditionalOnMissingBean(IFunctionService.class)
    public IFunctionService functionService(ParseFunctionFactory parseFunctionFactory) {
        return new DefaultFunctionServiceImpl(parseFunctionFactory);
    }

    @Bean
    public ParseFunctionFactory parseFunctionFactory(@Autowired List<IParseFunction> parseFunctions) {
        return new ParseFunctionFactory(parseFunctions);
    }

    @Bean
    @ConditionalOnMissingBean(IParseFunction.class)
    public DefaultParseFunction parseFunction() {
        return new DefaultParseFunction();
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryLogRecordAdvisor logRecordAdvisor() {
        BeanFactoryLogRecordAdvisor advisor = new BeanFactoryLogRecordAdvisor();
        advisor.setLogRecordOperationSource(logRecordOperationSource());
        advisor.setAdvice(logRecordInterceptor());
        advisor.setOrder(enableLogRecord.getNumber("order"));
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordInterceptor logRecordInterceptor() {
        LogRecordInterceptor interceptor = new LogRecordInterceptor();
        interceptor.setLogRecordOperationSource(logRecordOperationSource());
        interceptor.setTenantId(enableLogRecord.getString("tenant"));
        interceptor.setJoinTransaction(enableLogRecord.getBoolean("joinTransaction"));
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean(IOperatorGetService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public IOperatorGetService operatorGetService() {
        return new DefaultOperatorGetServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ILogRecordService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public ILogRecordService recordService() {
        return new DefaultLogRecordServiceImpl();
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableLogRecord = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableLogRecord.class.getName(), false));
        if (this.enableLogRecord == null) {
            log.error("@EnableCaching注解没有进行导入......");
        }
    }
}
