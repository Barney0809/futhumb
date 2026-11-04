package com.fu.thumb.controller;

import com.fu.thumb.common.CommonResponse;
import com.fu.thumb.constant.UserConstant;
import com.fu.thumb.exception.BusinessException;
import com.fu.thumb.exception.ExceptionCode;
import com.fu.thumb.model.User;
import com.fu.thumb.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public CommonResponse<User> login(Long userId, HttpServletRequest httpServletRequest){
        //校验参数
        Optional.ofNullable(userId).orElseThrow(() -> new BusinessException(ExceptionCode.PARAMS_ERROR,"用户ID不能为空"));
        return CommonResponse.success(userService.login(userId, httpServletRequest));
    }

    //获取当前登录用户
    @GetMapping("/current-user")
    public CommonResponse<User> getCurrentUser(HttpServletRequest httpServletRequest){
        return CommonResponse.success(userService.getLoginUser(httpServletRequest));
    }

}
