package com.fu.thumb.service;

import com.fu.thumb.model.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fu.thumb.request.DoThumbRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 * 点赞表 服务类
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
public interface ThumbService extends IService<Thumb> {

    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest httpServletRequest);
    Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest httpServletRequest);

    Boolean hasThumb(Long blogId, Long userId);
}
