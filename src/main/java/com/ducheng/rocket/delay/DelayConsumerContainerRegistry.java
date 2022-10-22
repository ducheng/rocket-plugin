package com.ducheng.rocket.delay;

import com.ducheng.rocket.annotation.DelayRocketMQ;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.List;

@Slf4j
public class DelayConsumerContainerRegistry implements BeanPostProcessor {

    private Environment environment;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public DelayConsumerContainerRegistry(Environment environment) {
        this.environment = environment;
    }

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object proxy, String beanName) throws BeansException {
        // 1. 获取 @DelayBasedRocketMQ 注解方法
        Class targetCls = AopUtils.getTargetClass(proxy);
        List<Method> methodsListWithAnnotation = MethodUtils.getMethodsListWithAnnotation(targetCls, DelayRocketMQ.class);
        // 2. 为每个 @DelayBasedRocketMQ 注解方法 注册 RocketMQConsumerContainer
        for(Method method : methodsListWithAnnotation){
            DelayRocketMQ annotation = AnnotatedElementUtils.findMergedAnnotation(method, DelayRocketMQ.class);
            //获取真实的代理对象
            Object bean = AopProxyUtils.getSingletonTarget(proxy);
            DelayConsumerContainer delayConsumerContainer =
                    new DelayConsumerContainer(
                            annotation,
                            bean,
                            method, environment);
        }
        return proxy;
    }

}
