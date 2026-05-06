package com.zinqx.roaddefectsbackend.service;

import com.zinqx.roaddefectsbackend.model.dto.authorization.AuthorizationLoginDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zinqx.roaddefectsbackend.model.entity.Authorization;

/**
* @author 23378
* @description 针对表【wx_user(用户信息)】的数据库操作Service
* @createDate 2026-03-20 13:59:22
*/
public interface AuthorizationService extends IService<Authorization> {


    /**
     * 微信登录
     * @param AuthorizationLoginDTO
     * @return
     */
    Authorization wxLogin(AuthorizationLoginDTO AuthorizationLoginDTO);
}
