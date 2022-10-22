package com.ducheng.rocket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DelayBasedRocketMQ {
    /**
     * RocketMQ topic
     * @return
     */
    String topic();

    /**
     * Tag
     * @return
     */
    String tag() default "*";


    /**
     * nameServer 配置
     * @return
     */
    String nameServer() default "${rocketmq.name-server:}";

    /**
     * 消费者组信息
     * @return
     */
    String consumerGroup();
}
