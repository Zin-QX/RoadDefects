package com.zinqx.roaddefectsbackend.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinqx.roaddefectsbackend.common.util.HttpClientUtil;
import com.zinqx.roaddefectsbackend.config.WeChatProperties;
import com.zinqx.roaddefectsbackend.exception.BusinessException;
import com.zinqx.roaddefectsbackend.exception.ErrorCode;
import com.zinqx.roaddefectsbackend.mapper.AuthorizationMapper;
import com.zinqx.roaddefectsbackend.model.dto.authorization.AuthorizationLoginDTO;
import com.zinqx.roaddefectsbackend.model.entity.Authorization;
import com.zinqx.roaddefectsbackend.model.entity.User;
import com.zinqx.roaddefectsbackend.model.enums.UserRoleEnum;
import com.zinqx.roaddefectsbackend.service.AuthorizationService;
import com.zinqx.roaddefectsbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
* @author 23378
* @description 针对表【wx_user(用户信息)】的数据库操作Service实现
* @createDate 2026-03-20 13:59:22
*/
@Slf4j
@Service
public class AuthorizationServiceImpl extends ServiceImpl<AuthorizationMapper, Authorization>
    implements AuthorizationService {

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private AuthorizationMapper authorizationMapper;

    @Autowired
    private UserService userService;

    /**
     * 微信登录
     * @param AuthorizationLoginDTO
     * @return
     */
    @Override
    public Authorization wxLogin(AuthorizationLoginDTO AuthorizationLoginDTO) {

        //调用微信接口，获取openid和session_key
        String openid = getOpenid(AuthorizationLoginDTO.getCode());

        //判断openid是否为空,如果为空，则登录失败
        if (openid==null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"登录失败");
        }

        //判断当前用户是否为新用户
        QueryWrapper<Authorization> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        Authorization authorization = this.baseMapper.selectOne(queryWrapper);
        if (authorization == null){
            // 新用户，创建用户账号
            User user = new User();
            user.setUserAccount("wx_" + openid);  // 使用微信 openid 作为账号
            user.setUserPassword("wx_default_password");  // 默认密码（实际使用中可能需要特殊处理）
            user.setUserName("微信用户_" + openid.substring(0, Math.min(8, openid.length())));
            user.setUserAvatar("");
            user.setUserProfile("");
            user.setUserRole(UserRoleEnum.USER.getValue());
            user.setEditTime(new Date());
            user.setCreateTime(new Date());
            boolean userSaveResult = userService.save(user);
            
            if (!userSaveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户创建失败");
            }
            
            // 创建授权记录
            authorization = new Authorization();
            authorization.setUserId(user.getId());
            authorization.setIdentityType("WECHAT");  // 身份类型：微信
            authorization.setOpenid(openid);
            authorization.setCredential(null);  // 密码凭证，微信登录不需要
            boolean authSaveResult = this.save(authorization);
            
            if (!authSaveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "授权记录创建失败");
            }
        }
        //返回登录用户
        return authorization;
    }

    /**
     * 调用微信接口，获取openid
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}




