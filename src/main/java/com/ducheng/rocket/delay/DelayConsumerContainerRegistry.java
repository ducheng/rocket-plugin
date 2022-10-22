package com.ducheng.rocket.delay;

import com.ducheng.rocket.annotation.DelayBasedRocketMQ;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by taoli on 2022/9/4.
 * gitee : https://gitee.com/litao851025/lego
 * 编程就像玩 Lego
 */
public class DelayConsumerContainerRegistry
    extends AbstractConsumerContainerRegistry {

    public DelayConsumerContainerRegistry(Environment environment) {
        super(environment);
    }

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object proxy, String beanName) throws BeansException {
        // 1. 获取 @DelayBasedRocketMQ 注解方法
        Class targetCls = AopUtils.getTargetClass(proxy);
        List<Method> methodsListWithAnnotation = MethodUtils.getMethodsListWithAnnotation(targetCls, DelayBasedRocketMQ.class);

        // 2. 为每个 @DelayBasedRocketMQ 注解方法 注册 RocketMQConsumerContainer
        for(Method method : methodsListWithAnnotation){
            DelayBasedRocketMQ annotation = AnnotatedElementUtils.findMergedAnnotation(method, DelayBasedRocketMQ.class);
            String consumerProfile = annotation.consumerProfile();
            if (!isActiveProfile(consumerProfile)){
                continue;
            }

            Object bean = AopProxyUtils.getSingletonTarget(proxy);
            DelayConsumerContainer delayConsumerContainer =
                    new DelayConsumerContainer(this.getEnvironment(),
                            annotation,
                            bean,
                            method);
            delayConsumerContainer.afterPropertiesSet();

            this.getConsumerContainers().add(delayConsumerContainer);
        }
        return proxy;
    }
}
