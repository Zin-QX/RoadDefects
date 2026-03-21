package com.zinqx.roaddefectsbackend.model.dto.authorization;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuthorizationLoginDTO implements Serializable {


    private static final long serialVersionUID = 9015520808878010173L;
    //微信登录凭证
    private String code;

}