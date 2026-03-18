create database if not exists road_defects;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
    ) comment '用户' collate = utf8mb4_unicode_ci;

-- 切换到目标数据库
USE road_defects;

-- 创建 picture 表（含审核相关字段）
CREATE TABLE picture
(
    id             BIGINT AUTO_INCREMENT COMMENT '主键 ID' PRIMARY KEY,
    url            VARCHAR(512)                        NOT NULL COMMENT '图片 URL',
    name           VARCHAR(128)                        NOT NULL COMMENT '图片名称',
    longitude      DOUBLE                              NOT NULL COMMENT '经度',
    latitude       DOUBLE                              NOT NULL COMMENT '纬度',
    address        VARCHAR(512)                        NOT NULL COMMENT '地址',
#     processedUrl   varchar(512)                        null comment '处理图片url',
#     processedResult    int default 0                       null comment '处理结果',
    picSize        BIGINT                              NULL     COMMENT '图片体积（字节）',
    userId         BIGINT                              NOT NULL COMMENT '创建用户 ID',
    createTime     DATETIME DEFAULT CURRENT_TIMESTAMP  NOT NULL COMMENT '创建时间',
    updateTime     DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP       NOT NULL COMMENT '更新时间',
    isDelete       TINYINT  DEFAULT 0                  NOT NULL COMMENT '是否删除：0-未删除；1-已删除',
    reviewStatus   INT      DEFAULT 0                  NOT NULL COMMENT '审核状态：0-待审核；1-通过；2-拒绝',
    reviewMessage  VARCHAR(512)                        NULL     COMMENT '审核信息（如拒绝原因）',
    reviewerId     BIGINT                              NULL     COMMENT '审核人 ID',
    reviewTime     DATETIME                            NULL     COMMENT '审核时间',

    -- 普通索引
    INDEX idx_name (name),
    INDEX idx_userId (userId),
    INDEX idx_reviewStatus (reviewStatus)
) COMMENT '图片信息表'
  COLLATE = utf8mb4_unicode_ci;