-- =====================================================================
--  futhumb 建库脚本
--  MySQL 8.0+
--  用法：mysql -uroot -p < sql/schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `thumb_db`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `thumb_db`;

DROP TABLE IF EXISTS `thumb`;
DROP TABLE IF EXISTS `blog`;
DROP TABLE IF EXISTS `user`;

-- 用户（简化版：本示例只用来确认「谁点了赞」）
CREATE TABLE `user`
(
    `id`       bigint      NOT NULL COMMENT 'ID',
    `username` varchar(64) NOT NULL COMMENT '用户名',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户';

-- 博客：thumb_count 是冗余的点赞数，需要与 thumb 表异步保持一致
CREATE TABLE `blog`
(
    `id`          bigint       NOT NULL COMMENT 'ID',
    `user_id`     bigint       NOT NULL COMMENT '作者 ID',
    `title`       varchar(256) NOT NULL COMMENT '标题',
    `cover_img`   varchar(512)  DEFAULT NULL COMMENT '封面图',
    `content`     text COMMENT '正文',
    `thumb_count` int          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `create_time` datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_blog_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='博客';

-- 点赞记录：一条 = 某个用户给某篇博客点过赞
CREATE TABLE `thumb`
(
    `id`          bigint   NOT NULL COMMENT 'ID',
    `user_id`     bigint   NOT NULL COMMENT '点赞用户 ID',
    `blog_id`     bigint   NOT NULL COMMENT '被点赞的博客 ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_blog` (`user_id`, `blog_id`),
    KEY `idx_thumb_blog` (`blog_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='点赞记录';

-- ---------- 演示数据 ----------
INSERT INTO `user` (`id`, `username`)
VALUES (1, 'demo');

INSERT INTO `blog` (`id`, `user_id`, `title`, `cover_img`, `content`, `thumb_count`)
VALUES (1, 1, '第一篇博客', NULL, '用来说明点赞系统的示例内容。', 0),
       (2, 1, '第二篇博客', NULL, '换一篇继续测。', 0);
