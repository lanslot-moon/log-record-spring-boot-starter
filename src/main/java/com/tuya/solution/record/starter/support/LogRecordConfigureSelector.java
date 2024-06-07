
package com.tuya.solution.record.starter.support;

import com.tuya.solution.record.starter.annotation.EnableLogRecord;
import com.tuya.solution.record.starter.autoconfigure.LogRecordProxyAutoConfiguration;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 日志配置选择器
 * @since 2024/6/5 14:22
 */
public class LogRecordConfigureSelector extends AdviceModeImportSelector<EnableLogRecord> {

    @Override
    public String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return new String[]{AutoProxyRegistrar.class.getName(), LogRecordProxyAutoConfiguration.class.getName()};
            case ASPECTJ:
                return new String[]{LogRecordProxyAutoConfiguration.class.toString()};
            default:
                return null;
        }
    }
}