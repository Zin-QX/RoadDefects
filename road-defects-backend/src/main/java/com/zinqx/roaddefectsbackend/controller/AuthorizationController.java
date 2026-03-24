package com.zinqx.roaddefectsbackend.controller;

import com.zinqx.roaddefectsbackend.common.*;
import com.zinqx.roaddefectsbackend.common.util.JwtClaimsConstant;
import com.zinqx.roaddefectsbackend.common.util.JwtProperties;
import com.zinqx.roaddefectsbackend.common.util.JwtUtil;
import com.zinqx.roaddefectsbackend.model.dto.authorization.AuthorizationLoginDTO;
import com.zinqx.roaddefectsbackend.model.entity.Authorization;
import com.zinqx.roaddefectsbackend.model.vo.AuthorizationVO;
import com.zinqx.roaddefectsbackend.service.AuthorizationService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/wxuser")
@Slf4j
public class AuthorizationController {

    @Autowired
    private AuthorizationService AuthorizationService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public BaseResponse<AuthorizationVO> login(@RequestBody AuthorizationLoginDTO authorizationLoginDTO) {
        log.info("微信用户登录：{}", authorizationLoginDTO);
        // 调用 service 完成微信登录
        Authorization authorization = AuthorizationService.wxLogin(authorizationLoginDTO);

        // 生成 jwt 令牌，记录 userId 而不是 authorization 的 id
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, authorization.getUserId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        // 返回登录信息
        AuthorizationVO authorizationVO = AuthorizationVO.builder()
                .id(authorization.getId())
                .openid(authorization.getOpenid())
                .token(token)
                .build();

        return ResultUtils.success(authorizationVO);
    }
}

