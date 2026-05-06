package com.zinqx.roaddefectsbackend.model.dto.user;

import com.zinqx.roaddefectsbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -5164761063432133938L;
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;
}
