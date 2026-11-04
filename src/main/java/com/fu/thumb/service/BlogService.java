package com.fu.thumb.service;

import com.fu.thumb.model.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fu.thumb.response.BlogResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 * 博客表 服务类
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
public interface BlogService extends IService<Blog> {

    BlogResponse getBlogResponseById(Long blogId, HttpServletRequest request);

    List<BlogResponse> getBlogResponseList(List<Blog> blogList,HttpServletRequest request);

    List<Blog> getUpdatedBlogList(Long lastUpdateTime);
}
