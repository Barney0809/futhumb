package com.fu.thumb.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "BlogResponse", description = "返回博客结果")
public class BlogResponse {

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 封面
     */
    @Schema(description = "封面")
    private String coverImg;

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Integer thumbCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 是否点赞
     */
    @Schema(description = "是否点赞")
    private Boolean hasThumb;
}
