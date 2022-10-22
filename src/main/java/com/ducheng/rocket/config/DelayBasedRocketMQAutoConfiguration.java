package com.ducheng.rocket.config;

import com.ducheng.rocket.delay.DelayConsumerContainerRegistry;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Configuration
@AutoConfigureAfter(RocketMQAutoConfiguration.class)
public class DelayBasedRocketMQAutoConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public DelayConsumerContainerRegistry delayConsumerContainerRegistry(){
        return new DelayConsumerContainerRegistry(environment);
    }
}
