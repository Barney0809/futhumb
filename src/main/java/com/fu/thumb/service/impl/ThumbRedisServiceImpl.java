package com.fu.thumb.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fu.thumb.constant.LuaScriptConstant;
import com.fu.thumb.constant.ThumbConstant;
import com.fu.thumb.enums.LuaStatusEnum;
import com.fu.thumb.exception.BusinessException;
import com.fu.thumb.exception.ExceptionCode;
import com.fu.thumb.mapper.ThumbMapper;
import com.fu.thumb.model.Thumb;
import com.fu.thumb.model.User;
import com.fu.thumb.request.DoThumbRequest;
import com.fu.thumb.service.ThumbService;
import com.fu.thumb.service.UserService;
import com.fu.thumb.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Service("ThumbService")
@ConditionalOnProperty(name = "app.thumb.strategy", havingValue = "redis", matchIfMissing = true)
@Slf4j
public class ThumbRedisServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest httpServletRequest) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new BusinessException(ExceptionCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(httpServletRequest);

        String timeSlice = getTimeSlice();
        if (timeSlice.isEmpty()) {
            log.warn("获取时间片失败，可能导致 Redis key 错误");
            throw new BusinessException(ExceptionCode.ERROR, "服务暂时不可用");
        }
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        //执行lua脚本
        long result;
        try {
            result = redisTemplate.execute(
                    LuaScriptConstant.DO_THUMB_SCRIPT,
                    Arrays.asList(tempThumbKey, userThumbKey),
                    loginUser.getId(),
                    doThumbRequest.getBlogId()
            );
        } catch (Exception e) {
            log.error("执行点赞 Lua 脚本异常，userId={}, blogId={}", loginUser.getId(), doThumbRequest.getBlogId(), e);
            throw new BusinessException(ExceptionCode.ERROR, "操作失败！");
        }

        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ExceptionCode.OPERATION_ERROR, "用户已经点赞！");
        }
        if(LuaStatusEnum.BLOG_NOT_EXIST.getValue() == result){
            throw new BusinessException(ExceptionCode.NOT_FOUND, "博客不存在！");
        }
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest httpServletRequest) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new BusinessException(ExceptionCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(httpServletRequest);

        Long blogId = doThumbRequest.getBlogId();
        // 计算时间片
        String timeSlice = getTimeSlice();
        if (timeSlice.isEmpty()) {
            log.warn("获取时间片失败，可能导致 Redis key 错误!");
            throw new BusinessException(ExceptionCode.ERROR, "服务暂时不可用");
        }
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        // 执行 Lua 脚本
        long result = 0;
        try {
            result = redisTemplate.execute(
                    LuaScriptConstant.UNDO_THUMB_SCRIPT,
                    Arrays.asList(tempThumbKey, userThumbKey),
                    loginUser.getId(),
                    blogId
            );
        } catch (Exception e) {
            log.error("执行点赞 Lua 脚本异常，userId={}, blogId={}", loginUser.getId(), doThumbRequest.getBlogId(), e);
            throw new BusinessException(ExceptionCode.ERROR, "操作失败！");        }
        // 根据返回值处理结果
        if (result == LuaStatusEnum.FAIL.getValue()) {
            throw new BusinessException(ExceptionCode.OPERATION_ERROR,"用户未点赞");
        }
        if(LuaStatusEnum.BLOG_NOT_EXIST.getValue() == result){
            throw new BusinessException(ExceptionCode.NOT_FOUND, "博客不存在！");
        }
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        if (blogId == null || userId == null) {
            throw new BusinessException(ExceptionCode.PARAMS_ERROR, "参数错误");
        }
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }

    private String getTimeSlice() {
        DateTime now = DateUtil.date();
        // 获取到当前时间前最近的整数秒，比如当前 11:20:23 ，获取到 11:20:20
        return DateUtil.format(now, "HH:mm:") + (DateUtil.second(now) / 10) * 10;
    }
}
