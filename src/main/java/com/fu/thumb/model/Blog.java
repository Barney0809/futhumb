package com.fu.thumb.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 博客表
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@Data
@TableName("blog")
@Schema(name = "Blog", description = "博客表")
public class Blog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId("id")
    @Schema(description = "id")
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    @Schema(description = "用户id")
    private Long userId;

    /**
     * 标题
     */
    @TableField("title")
    @Schema(description = "标题")
    private String title;

    /**
     * 封面
     */
    @TableField("cover_img")
    @Schema(description = "封面")
    private String coverImg;

    /**
     * 内容
     */
    @TableField("content")
    @Schema(description = "内容")
    private String content;

    /**
     * 点赞数
     */
    @TableField("thumb_count")
    @Schema(description = "点赞数")
    private Integer thumbCount;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
