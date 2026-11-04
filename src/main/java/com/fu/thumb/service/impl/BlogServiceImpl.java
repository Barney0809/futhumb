package com.fu.thumb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.fu.thumb.constant.ThumbConstant;
import com.fu.thumb.exception.BusinessException;
import com.fu.thumb.exception.ExceptionCode;
import com.fu.thumb.model.Blog;
import com.fu.thumb.mapper.BlogMapper;
import com.fu.thumb.model.Thumb;
import com.fu.thumb.model.User;
import com.fu.thumb.response.BlogResponse;
import com.fu.thumb.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fu.thumb.service.ThumbService;
import com.fu.thumb.service.UserService;
import com.fu.thumb.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 博客表 服务实现类
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ThumbService thumbService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public BlogResponse getBlogResponseById(Long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        Optional.ofNullable(blog).orElseThrow(() -> new BusinessException(ExceptionCode.PARAMS_ERROR, "博客不存在"));
        User loginUser = userService.getLoginUser(request);
        return buildBlogResponse(blog, loginUser);
    }

    @Override
    public List<BlogResponse> getBlogResponseList(List<Blog> blogList, HttpServletRequest request) {
        if (blogList == null || blogList.isEmpty()) {
            return new ArrayList<>();
        }
        User loginUser = userService.getLoginUser(request);
        List<Object> blogIdList = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toList());
        //获取是否点赞
//        List<Thumb> thumbList = thumbService.lambdaQuery()
//                .eq(Thumb::getUserId, loginUser.getId())
//                .in(Thumb::getBlogId, blogIdSet)
//                .list();

        List<Object> thumbList = redisTemplate.opsForHash().multiGet(RedisKeyUtil.getUserThumbKey(loginUser.getId()), blogIdList);


//        Map<Long, Boolean> hasThumbMapByBlogId = thumbList.stream()
//                .collect(Collectors.toMap(Thumb::getBlogId, thumb -> true));

        Map<Long, Boolean> hasThumbMapByBlogId = new HashMap<>();
        for (int i = 0; i < thumbList.size(); i++) {
            if (thumbList.get(i) != null) {
                hasThumbMapByBlogId.put(Long.valueOf(blogIdList.get(i).toString()), true);
            } else {
                hasThumbMapByBlogId.put(Long.valueOf(blogIdList.get(i).toString()), false);
            }
        }
        return blogList.stream().map(blog -> {
            BlogResponse blogResponse = new BlogResponse();
            BeanUtil.copyProperties(blog, blogResponse);
            blogResponse.setHasThumb(hasThumbMapByBlogId.getOrDefault(blog.getId(), false));
            return blogResponse;
        }).collect(Collectors.toList());
    }

    @Override
    //@Cacheable(value = "updatedBlogs", key = "#lastUpdateTime")
    public List<Blog> getUpdatedBlogList(Long lastUpdateTime) {
        // 参数校验
        if (lastUpdateTime == null) {
            throw new IllegalArgumentException("lastUpdateTime cannot be null");
        }
        log.info("执行数据库查询，lastUpdateTime: {}", lastUpdateTime); // 如果看到这条日志，说明未走缓存
        // 将long时间戳转换为LocalDateTime
        LocalDateTime time = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastUpdateTime),
                ZoneId.systemDefault()
        );
        return this.lambdaQuery()
                .gt(Blog::getUpdateTime, time)
                .list();
    }

    public BlogResponse buildBlogResponse(Blog blog, User loginUser) {
        BlogResponse blogResponse = new BlogResponse();
        BeanUtil.copyProperties(blog, blogResponse);
        // 如果loginUser是null，则直接返回blogResponse
        if (loginUser == null) {
            return blogResponse;
        }
//        Thumb thumb = thumbService.lambdaQuery()
//                .eq(Thumb::getUserId, loginUser.getId())
//                .eq(Thumb::getBlogId, blog.getId())
//                .one();
        Boolean exist = thumbService.hasThumb(blog.getId(), loginUser.getId());
        blogResponse.setHasThumb(exist);
        return blogResponse;
    }
}
