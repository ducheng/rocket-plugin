package com.ducheng.rocket.delay;

import com.ducheng.rocket.utils.SerializeUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Created by taoli on 2022/9/4.
 * gitee : https://gitee.com/litao851025/lego
 * 编程就像玩 Lego
 */
public class AbstractRocketMQSendInterceptor {
    @Getter(AccessLevel.PROTECTED)
    protected final RocketMQTemplate rocketMQTemplate;
    @Getter(AccessLevel.PROTECTED)
    protected final Environment environment;

    public AbstractRocketMQSendInterceptor(RocketMQTemplate rocketMQTemplate, Environment environment) {
        Preconditions.checkArgument(environment != null);
        Preconditions.checkArgument(rocketMQTemplate != null);

        this.rocketMQTemplate = rocketMQTemplate;
        this.environment = environment;
    }

    /**
     * 序列化操作
     * @param arguments
     * @return
     */
    protected String serialize(Object[] arguments) {
        Map<String, String> result = Maps.newHashMapWithExpectedSize(arguments.length);
        for (int i = 0; i < arguments.length; i++){
            result.put(String.valueOf(i), SerializeUtil.serialize(arguments[i]));
        }
        return SerializeUtil.serialize(result);
    }

    /**
     * 解析表达式，获取最终配置信息
     * @param value
     * @return
     */
    protected String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }

    /**
     * 创建 destination
     * @param topic
     * @param tag
     * @return
     */
    protected String createDestination(String topic, String tag) {
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(tag)){
            return topic + ":" + tag;
        }else {
            return topic;
        }
    }

    protected RocketMQTemplate getRocketMQTemplate() {
        return this.rocketMQTemplate;
    }

    protected Environment getEnvironment() {
        return this.environment;
    }
}
