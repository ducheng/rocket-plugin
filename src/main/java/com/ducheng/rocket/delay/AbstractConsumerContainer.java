package com.ducheng.rocket.delay;

import com.ducheng.rocket.utils.SerializeUtil;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by taoli on 2022/9/4.
 * gitee : https://gitee.com/litao851025/lego
 * 编程就像玩 Lego
 */
@Slf4j
public abstract class AbstractConsumerContainer implements InitializingBean, SmartLifecycle {
    protected final Environment environment;
    protected final Object bean;
    protected final Method method;
    private boolean running;
    private DefaultMQPushConsumer consumer;
    @Getter
    @Setter
    private int delayLevelWhenNextConsume = 1;

    public AbstractConsumerContainer(Environment environment, Object bean, Method method) {
        Preconditions.checkArgument(environment != null);
        Preconditions.checkArgument(bean != null);
        Preconditions.checkArgument(method != null);

        this.environment = environment;
        this.bean = bean;
        this.method = method;
    }

    protected void invokeMethod(MessageExt message) throws IllegalAccessException, InvocationTargetException {
        long now = System.currentTimeMillis();

        // 从 Message 中反序列化数据，获得方法调用参数
        byte[] body = message.getBody();

        invokeMethod(body);

        long costTime = System.currentTimeMillis() - now;
        log.info("consume message {}, cost: {} ms",  message.getMsgId(), costTime);
    }

    private void invokeMethod(byte[] body) throws IllegalAccessException, InvocationTargetException {
        String bodyAsStr = new String(body, StandardCharsets.UTF_8);

        // 先恢复 Map
        Map deserialize = SerializeUtil.deserialize(bodyAsStr, Map.class);
        Object[] params = new Object[getMethod().getParameterCount()];

        // 根据类型对每个参数进行反序列化
        for (int i = 0; i < getMethod().getParameterCount(); i++) {
            String o = (String) deserialize.get(String.valueOf(i));
            if (o == null) {
                params[i] = null;
            } else {
                params[i] = SerializeUtil.deserialize(o, getMethod().getParameterTypes()[i]);
            }
        }

        log.debug("bean is {}, method is {}, param is {}", getBean(), getMethod(), params);
        // 执行业务方法
        getMethod().invoke(getBean(), params);
    }

    protected String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }

    protected boolean skipWhenException(){
        return this.environment.getProperty("async.consumer.skipWhenError", Boolean.TYPE, false);
    }

    protected void doStart() {
        try {
            this.consumer.start();
            log.info("success to start consumer {}", this.consumer);
        } catch (MQClientException e) {
            log.error("failed to start rocketmq consumer {}", this.consumer);
        }
    }

    protected void doShutdown() {
        this.consumer.shutdown();
        log.info("success to shutdown consumer {}", this.consumer);
    }

    protected abstract DefaultMQPushConsumer createConsumer() throws Exception;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 构建 DefaultMQPushConsumer
        this.consumer = createConsumer();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable runnable) {
        stop();
    }

    @Override
    public void start() {
        if (this.running) {
            return;
        }

        doStart();
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        doShutdown();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public Object getBean() {
        return this.bean;
    }

    public Method getMethod() {
        return this.method;
    }
}
