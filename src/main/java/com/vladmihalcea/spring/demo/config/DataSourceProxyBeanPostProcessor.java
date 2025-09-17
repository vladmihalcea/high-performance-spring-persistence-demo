package com.vladmihalcea.spring.demo.config;

import com.vladmihalcea.spring.util.logging.InlineQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@Component
public class DataSourceProxyBeanPostProcessor implements BeanPostProcessor {

    @Value("${logging.proxy.enabled:false}")
    private boolean loggingProxyEnabled;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource source && !(bean instanceof ProxyDataSource)) {
            final ProxyFactory factory = new ProxyFactory(bean);
            factory.setProxyTargetClass(true);
            factory.addAdvice(new DataSourceProxyAdvice(source, loggingProxyEnabled));
            return factory.getProxy();
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    private static class DataSourceProxyAdvice implements MethodInterceptor {

        private final DataSource dataSource;

        public DataSourceProxyAdvice(final DataSource dataSource, boolean loggingProxyEnabled) {
            ChainListener chainListener = new ChainListener();
            if (loggingProxyEnabled) {
                SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
                loggingListener.setQueryLogEntryCreator(new InlineQueryLogEntryCreator());
                chainListener.addListener(loggingListener);
            }
            chainListener.addListener(new DataSourceQueryCountListener());
            DataSource loggingDataSource = ProxyDataSourceBuilder
                .create(dataSource)
                .name("SpringPersistenceDemoApp")
                .listener(chainListener)
                .build();
            this.dataSource = loggingDataSource;
        }

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            final Method proxyMethod = ReflectionUtils.findMethod(
                this.dataSource.getClass(),
                invocation.getMethod().getName()
            );
            return proxyMethod != null ?
                proxyMethod.invoke(this.dataSource, invocation.getArguments()) :
                invocation.proceed();
        }
    }
}
