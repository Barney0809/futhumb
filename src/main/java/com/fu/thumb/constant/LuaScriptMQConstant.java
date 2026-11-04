package com.fu.thumb.constant;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class LuaScriptMQConstant {
    /**
     * 点赞 Lua 脚本
     * KEYS[1]       -- 用户点赞状态键
     * ARGV[1]       -- 博客 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> THUMB_SCRIPT_MQ = new DefaultRedisScript<>("""  
        local userThumbKey = KEYS[1]
        local blogId = ARGV[1]
        -- 0. 检查博客ID是否合法
        if redis.call('SISMEMBER','blogIdSet',blogId) ~= 1 then \s
            return -2
        end
        
        -- 判断是否已经点赞
        if redis.call("HEXISTS", userThumbKey, blogId) == 1 then
            return -1
        end
        
        -- 添加点赞记录
        redis.call("HSET", userThumbKey, blogId, 1)
        return 1
        """, Long.class);

    /**
     * 取消点赞 Lua 脚本
     * KEYS[1]       -- 用户点赞状态键
     * ARGV[1]       -- 博客 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> UNDO_THUMB_SCRIPT_MQ = new DefaultRedisScript<>("""  
        local userThumbKey = KEYS[1]
        local blogId = ARGV[1]
        
        -- 判断是否已点赞
        if redis.call("HEXISTS", userThumbKey, blogId) == 0 then
            return -1
        end
        
        -- 删除点赞记录
        redis.call("HDEL", userThumbKey, blogId)
        return 1
        """, Long.class);

}
