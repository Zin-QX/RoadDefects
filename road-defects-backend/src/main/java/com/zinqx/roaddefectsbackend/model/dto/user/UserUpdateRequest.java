package com.zinqx.roaddefectsbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = -3109930233837990440L;
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户角色：user/admin
     */
    private String userRole;
}
