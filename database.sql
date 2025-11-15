create database if not exists speed_calendar;
CREATE TABLE `users` (
                         `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
                         `phone` VARCHAR(20) NOT NULL COMMENT '手机号，作为主要登录凭据',
                         `username` VARCHAR(50) NULL COMMENT '用户名，可后续设置',
                         `avatar` VARCHAR(255) NULL COMMENT '用户头像URL',
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账户创建时间',
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '账户最后更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';