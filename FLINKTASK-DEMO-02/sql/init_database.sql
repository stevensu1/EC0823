-- 创建数据库
CREATE DATABASE IF NOT EXISTS `flink_demo` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `flink_demo`;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '用户状态：1-正常，0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：1-已删除，0-未删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入示例数据
INSERT INTO `user` (`username`, `email`, `age`, `status`, `deleted`) VALUES
('admin', 'admin@example.com', 30, 1, 0),
('user001', 'user001@example.com', 25, 1, 0),
('user002', 'user002@example.com', 28, 1, 0),
('user003', 'user003@example.com', 22, 0, 0),
('user004', 'user004@example.com', 35, 1, 0);

-- 创建产品表（可选，用于演示更多功能）
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '产品名称',
  `description` text COMMENT '产品描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存数量',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '产品状态：1-上架，0-下架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：1-已删除，0-未删除',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- 插入产品示例数据
INSERT INTO `product` (`name`, `description`, `price`, `stock`, `category_id`, `status`, `deleted`) VALUES
('MacBook Pro', 'Apple MacBook Pro 16英寸 M3芯片', 19999.00, 50, 1, 1, 0),
('iPhone 15', 'Apple iPhone 15 Pro Max 256GB', 9999.00, 100, 2, 1, 0),
('iPad Air', 'Apple iPad Air 第5代 64GB Wi-Fi版', 4399.00, 75, 3, 1, 0),
('AirPods Pro', 'Apple AirPods Pro 第2代 降噪耳机', 1999.00, 200, 4, 1, 0),
('Apple Watch', 'Apple Watch Series 9 GPS 45mm', 3199.00, 80, 5, 1, 0);

-- 创建订单表（可选）
CREATE TABLE IF NOT EXISTS `order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '订单状态：1-待付款，2-已付款，3-已发货，4-已完成，5-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：1-已删除，0-未删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 查看表结构
-- DESCRIBE `user`;
-- DESCRIBE `product`;
-- DESCRIBE `order`;

-- 查看示例数据
-- SELECT * FROM `user` WHERE `deleted` = 0;
-- SELECT * FROM `product` WHERE `deleted` = 0 LIMIT 5;