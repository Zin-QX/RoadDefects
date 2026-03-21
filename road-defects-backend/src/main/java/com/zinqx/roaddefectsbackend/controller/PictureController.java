package com.zinqx.roaddefectsbackend.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zinqx.roaddefectsbackend.annotation.AuthCheck;
import com.zinqx.roaddefectsbackend.common.BaseResponse;
import com.zinqx.roaddefectsbackend.common.ResultUtils;
import com.zinqx.roaddefectsbackend.constant.UserConstant;
import com.zinqx.roaddefectsbackend.exception.BusinessException;
import com.zinqx.roaddefectsbackend.exception.ErrorCode;
import com.zinqx.roaddefectsbackend.exception.ThrowUtils;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureQueryRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureReviewRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureUpdateRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureUploadRequest;
import com.zinqx.roaddefectsbackend.model.entity.Authorization;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import com.zinqx.roaddefectsbackend.model.entity.User;
import com.zinqx.roaddefectsbackend.model.enums.PictureReviewStatusEnum;
import com.zinqx.roaddefectsbackend.model.vo.PictureVO;
import com.zinqx.roaddefectsbackend.service.AuthorizationService;
import com.zinqx.roaddefectsbackend.service.PictureService;
import com.zinqx.roaddefectsbackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/picture")
@Slf4j
@Api(tags = "图片接口")
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private AuthorizationService authorizationService;

    /**
     * 上传图片（仅微信用户可用）
     */
    @PostMapping("/upload")
    @ApiOperation("上传图片")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
            @RequestHeader("Authorization") String token, PictureUploadRequest pictureUploadRequest) {
        // 处理 Bearer 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 通过 token 获取用户
        User loginUser = userService.getUserByToken(token);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        
        // 校验是否为微信用户（检查 authorization 表是否有记录）
        Authorization authorization = authorizationService.lambdaQuery()
                .eq(Authorization::getUserId, loginUser.getId())
                .one();
        
        if (authorization == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "非微信用户，无权上传");
        }
        
        // 上传图片
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

//    @PostMapping("/upload")
//    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
//                                                 PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
//        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest);
//        return ResultUtils.success(pictureVO);
//    }
    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody(required = false) PictureQueryRequest pictureQueryRequest) {
        if (pictureQueryRequest == null) {
            pictureQueryRequest = new PictureQueryRequest();
        }
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（已审核）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 图片审核
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 @RequestHeader("Authorization") String token) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        // 处理 Bearer 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        User loginUser = userService.getUserByToken(token);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }



}
