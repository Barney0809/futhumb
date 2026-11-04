package com.fu.thumb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fu.thumb.model.Thumb;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 点赞表 Mapper 接口
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@Mapper
public interface ThumbMapper extends BaseMapper<Thumb> {

}
