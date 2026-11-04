package com.fu.thumb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fu.thumb.model.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 * 博客表 Mapper 接口
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}
