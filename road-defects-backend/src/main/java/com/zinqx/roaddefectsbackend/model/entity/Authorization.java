package com.zinqx.roaddefectsbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户授权
 * @TableName authorization
 */
@TableName(value ="authorization")
@Data
public class Authorization {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 身份类型
     */
    private String identityType;

    /**
     * 用户唯一标识
     */
    private String openid;

    /**
     * 密码凭证
     */
    private String credential;
}