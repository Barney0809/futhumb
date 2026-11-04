package com.fu.thumb.service.impl;

import com.fu.thumb.constant.UserConstant;
import com.fu.thumb.exception.BusinessException;
import com.fu.thumb.exception.ExceptionCode;
import com.fu.thumb.model.User;
import com.fu.thumb.mapper.UserMapper;
import com.fu.thumb.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(Long userId, HttpServletRequest httpServletRequest) {
        User user = this.getById(userId);
        Optional.ofNullable(user)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_EXIST,"用户不存在"));
        httpServletRequest.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,user);
        return user;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        Optional.ofNullable(user)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_LOGIN,"用户未登录"));
        return user;
    }
}
