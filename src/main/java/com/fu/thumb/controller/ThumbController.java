package com.fu.thumb.controller;

import com.fu.thumb.common.CommonResponse;
import com.fu.thumb.request.DoThumbRequest;
import com.fu.thumb.service.ThumbService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 点赞表 前端控制器
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@RestController
@RequestMapping("/thumb")
public class ThumbController {

    @Resource
    private ThumbService thumbService;

    @PostMapping("/do")
    public CommonResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean result = thumbService.doThumb(doThumbRequest, request);
        return CommonResponse.success(result);
    }

    @PostMapping("/undo")
    public CommonResponse<Boolean> undoThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean result = thumbService.undoThumb(doThumbRequest, request);
        return CommonResponse.success(result);
    }

}
