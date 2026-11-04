package com.fu.thumb.controller;

import com.fu.thumb.common.CommonResponse;
import com.fu.thumb.exception.BusinessException;
import com.fu.thumb.exception.ExceptionCode;
import com.fu.thumb.response.BlogResponse;
import com.fu.thumb.service.BlogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 博客表 前端控制器
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @GetMapping("/get")
    public CommonResponse<BlogResponse> getBlogResponseById(Long blogId, HttpServletRequest request) {
        Optional.ofNullable(blogId).orElseThrow(() -> new BusinessException(ExceptionCode.PARAMS_ERROR, "参数错误"));
        return CommonResponse.success(blogService.getBlogResponseById(blogId, request));
    }

    @GetMapping("/list")
    public CommonResponse<List<BlogResponse>> listBlogResponse(HttpServletRequest request) {
        List<BlogResponse> blogResponseList = blogService.getBlogResponseList(blogService.list(), request);
        return CommonResponse.success(blogResponseList);
    }
}
