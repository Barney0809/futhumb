package com.fu.thumb.service;

import com.fu.thumb.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
public interface UserService extends IService<User> {
    User login(Long userId, HttpServletRequest httpServletRequest);
    User getLoginUser(HttpServletRequest request);
}
