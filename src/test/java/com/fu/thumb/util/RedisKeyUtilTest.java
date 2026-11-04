package com.fu.thumb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Redis key 生成规则的契约测试。
 *
 * <p>点赞数据落在哪个 key，由这三个方法决定；Lua 脚本、定时任务、Service 都按同样的规则取数，
 * 改动必须三处同步，所以单独钉住。
 */
class RedisKeyUtilTest {

    @Test
    void userThumbKey_prefixedWithThumb() {
        assertEquals("thumb:42", RedisKeyUtil.getUserThumbKey(42L));
    }

    @Test
    void userThumbKey_differsPerUser() {
        assertNotEquals(RedisKeyUtil.getUserThumbKey(1L), RedisKeyUtil.getUserThumbKey(2L));
    }

    @Test
    void tempThumbKey_embedsTimeBucket() {
        assertEquals("thumb:temp:20251105", RedisKeyUtil.getTempThumbKey("20251105"));
    }
}
