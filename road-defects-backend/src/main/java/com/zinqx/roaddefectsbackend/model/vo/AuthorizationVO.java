package com.zinqx.roaddefectsbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationVO implements Serializable {

    private static final long serialVersionUID = 276401074280527697L;
    // 用户id
    private Long id;
    // 微信 openid
    private String openid;
    // 令牌
    private String token;

}
