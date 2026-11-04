package com.fu.thumb.constant;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class LuaScriptConstant {

    /**
     * 点赞 Lua 脚本
     * KEYS[1]       -- 临时计数键
     * KEYS[2]       -- 用户点赞状态键
     * ARGV[1]       -- 用户 ID
     * ARGV[2]       -- 博客 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> DO_THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]       -- 临时计数键（如 thumb:temp:{timeSlice}） \s
            local userThumbKey = KEYS[2]       -- 用户点赞状态键（如 thumb:{userId}） \s
            local userId = ARGV[1]             -- 用户 ID \s
            local blogId = ARGV[2]             -- 博客 ID \s
            
            -- 0. 检查博客ID是否合法
            if redis.call('SISMEMBER','blogIdSet',blogId) ~= 1 then \s
                return -2
            end
            
            -- 1. 检查是否已点赞（避免重复操作） \s
            if redis.call('HEXISTS', userThumbKey, blogId) == 1 then \s
                return -1  -- 已点赞，返回 -1 表示失败 \s
            end \s
            
            -- 2. 获取旧值（不存在则默认为 0） \s
            local hashKey = userId .. ':' .. blogId \s
            local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0) \s
            
            -- 3. 计算新值 \s
            local newNumber = oldNumber + 1 \s
            
            -- 4. 原子性更新：写入临时计数 + 标记用户已点赞 \s
            redis.call('HSET', tempThumbKey, hashKey, newNumber) \s
            redis.call('HSET', userThumbKey, blogId, 1) \s
            
            return 1  -- 返回 1 表示成功 \s
            """, Long.class);


    /**
     * 取消点赞 Lua 脚本
     * 参数同上
     * 返回：
     * -1: 未点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> UNDO_THUMB_SCRIPT = new DefaultRedisScript<>("""  
        local tempThumbKey = KEYS[1]      -- 临时计数键（如 thumb:temp:{timeSlice}）
        local userThumbKey = KEYS[2]      -- 用户点赞状态键（如 thumb:{userId}）
        local userId = ARGV[1]            -- 用户 ID
        local blogId = ARGV[2]            -- 博客 ID
        
        -- 0. 检查博客ID是否合法
           if redis.call('SISMEMBER','blogIdSet',blogId) ~= 1 then \s
               return -2
           end
        
        -- 1. 检查用户是否已点赞（若未点赞，直接返回失败）
        if redis.call('HEXISTS', userThumbKey, blogId) ~= 1 then
            return -1  -- 未点赞，返回 -1 表示失败
        end
        
        -- 2. 获取当前临时计数（若不存在则默认为 0）
        local hashKey = userId .. ':' .. blogId
        local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)
        
        -- 3. 计算新值并更新
        local newNumber = oldNumber - 1
        
        -- 4. 原子性操作：更新临时计数 + 删除用户点赞标记
        redis.call('HSET', tempThumbKey, hashKey, newNumber)
        redis.call('HDEL', userThumbKey, blogId)
        
        return 1  -- 返回 1 表示成功
        """, Long.class);

}
