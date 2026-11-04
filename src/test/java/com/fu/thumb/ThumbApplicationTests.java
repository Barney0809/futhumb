package com.fu.thumb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 启动上下文冒烟测试 —— 验证整个 Spring 容器能装配起来。
 *
 * <p>它会真实连接 MySQL 与 Redis（strategy=mq 时还包括 Pulsar），所以默认跳过：
 * 没有起依赖的人或 CI 直接 {@code mvn test} 不会因为它变红。
 *
 * <p>要执行它：
 * <pre>
 * # 先建库（sql/schema.sql）、起 Redis，mq 策略还要起 Pulsar，然后：
 * THUMB_IT=true mvn test -Dtest=ThumbApplicationTests
 * </pre>
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "THUMB_IT", matches = "true")
class ThumbApplicationTests {

    @Test
    void contextLoads() {
    }

}
