package com.fu.thumb.job;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;


import cn.hutool.core.date.DateUtil;
import com.fu.thumb.constant.CommonConstant;
import com.fu.thumb.model.Blog;
import com.fu.thumb.service.BlogService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
@ConditionalOnExpression("'${app.thumb.strategy:redis}' != 'db'")
public class SyncBlogId2DBJob {

    private final BlogService blogService;

    private final RedisTemplate<String, Object> redisTemplate;

    static long lastUpdateTime = System.currentTimeMillis();
    @Scheduled(fixedRate = 1000 * 5)
    public void run() {
        long startTime = System.currentTimeMillis();
        log.info("开始检测博客ID并执行同步");
        LocalDateTime lastTime =  LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastUpdateTime),
                ZoneId.systemDefault()
        );
        //时间格式化
        String lastUpdateTimeStr = DateUtil.format(lastTime, "yyyy-MM-dd HH:mm:ss");

        try {
            // 1. 从数据库中获取博客数据(上次更新之后的)
            List<Blog> list = blogService.getUpdatedBlogList(lastUpdateTime);

            // 2. 空集合检查
            if (list == null || list.isEmpty()) {
                log.info("同步列表为空，跳过同步，上次同步时间：{}", lastUpdateTimeStr);
                return;
            }

            // 3. 将ID提取并组装定时放入Redis
            Long[] blogIds = list.stream().map(Blog::getId).toArray(Long[]::new);
            redisTemplate.opsForSet().add(CommonConstant.BLOG_ID_KEY, blogIds);

            long endTime = System.currentTimeMillis();
            log.info("博客ID同步完成，共处理{}条数据，耗时{}ms", blogIds.length, (endTime - startTime));

            log.info("上次更新时间：{}", lastUpdateTimeStr);
            lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("博客ID同步失败，耗时{}ms，错误信息：{}", (endTime - startTime), e.getMessage(), e);
        }
    }

}
