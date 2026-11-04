package com.fu.thumb.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author FU
 * @since 2025-10-28
 */
@Data
@TableName("user")
@Schema(name = "User", description = "用户表")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId("id")
    @Schema(description = "id")
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    @Schema(description = "用户名")
    private String username;
}
