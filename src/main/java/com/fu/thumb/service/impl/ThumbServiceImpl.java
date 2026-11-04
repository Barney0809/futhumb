package com.fu.thumb.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.fu.thumb.constant.ThumbConstant;
import com.fu.thumb.exception.BusinessException;
import com.fu.thumb.exception.ExceptionCode;
import com.fu.thumb.model.Blog;
import com.fu.thumb.model.Thumb;
import com.fu.thumb.mapper.ThumbMapper;
import com.fu.thumb.model.User;
import com.fu.thumb.request.DoThumbRequest;
import com.fu.thumb.service.BlogService;
import com.fu.thumb.service.ThumbService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fu.thumb.service.UserService;
import com.fu.thumb.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 点赞表 服务实现类
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@Service("thumbServiceDB")
@ConditionalOnProperty(name = "app.thumb.strategy", havingValue = "db")
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 点赞
     *
     * @param doThumbRequest     请求参数 blogId
     * @param httpServletRequest 请求
     * @return 是否成功
     */
    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest httpServletRequest) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new BusinessException(ExceptionCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        Blog blog = blogService.getById(doThumbRequest.getBlogId());
        Optional.ofNullable(blog).orElseThrow(() -> new BusinessException(ExceptionCode.PARAMS_ERROR,"博客不存在"));

        //加锁
        synchronized (loginUser.getId().toString().intern()){
            //编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
//                boolean exist = this.lambdaQuery()
//                        .eq(Thumb::getUserId, loginUser.getId())
//                        .eq(Thumb::getBlogId, blogId)
//                        .exists();
                Boolean exist = this.hasThumb(blogId, loginUser.getId());
                if(exist){
                    throw new BusinessException(ExceptionCode.ERROR,"用户已经点赞！");
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumb_count = thumb_count + 1")
                        .update();

                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);

                //更新成功才执行
                boolean success = update && this.save(thumb);
                if (success){
                    redisTemplate.opsForHash().put(RedisKeyUtil.getUserThumbKey(loginUser.getId()), blogId.toString(), thumb.getId());
                }
                return success;
            });
        }
    }

    /*
      我们先进行了参数校验和用户登录态的校验，之后加锁执行点赞逻辑。注意是否已点赞的逻辑要在锁里执行，否则会出现锁不住的情况。
      为了方便控制事务的起始，这里使用了编程式事务，保证事务在锁里开始和结束，避免因为事务导致锁失效。
      我们这里的锁是本地锁，在多实例的分布式场景下有可能会失效，所以也可以使用分布式锁替代，比如 Redisson 。
     */


    /**
     * 取消点赞
     *
     * @param doThumbRequest     请求参数 blogId
     * @param httpServletRequest 请求
     * @return 是否成功
     */
    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest httpServletRequest) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new BusinessException(ExceptionCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(httpServletRequest);

        Blog blog = blogService.getById(doThumbRequest.getBlogId());
        Optional.ofNullable(blog).orElseThrow(() -> new BusinessException(ExceptionCode.PARAMS_ERROR,"博客不存在"));
        //加锁
        synchronized (loginUser.getId().toString().intern()){
            //编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
//                Thumb thumb = this.lambdaQuery()
//                        .eq(Thumb::getUserId, loginUser.getId())
//                        .eq(Thumb::getBlogId, blogId)
//                        .one();
                Object thumbIdObj = redisTemplate.opsForHash().get(RedisKeyUtil.getUserThumbKey(loginUser.getId()), blogId.toString());
                Optional.ofNullable(thumbIdObj).orElseThrow(() -> new BusinessException(ExceptionCode.ERROR,"用户未点赞！"));
                Long thumbId = Long.valueOf(thumbIdObj.toString());

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumb_count = thumb_count - 1")
                        .update();
                //更新成功才执行
                boolean success = update && this.removeById(thumbId);
                if (success){
                    redisTemplate.opsForHash().delete(RedisKeyUtil.getUserThumbKey(loginUser.getId()), blogId.toString());
                }
                return success;
            });
        }
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        if (blogId == null || userId == null) {
            throw new BusinessException(ExceptionCode.PARAMS_ERROR, "参数错误");
        }
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }


}
