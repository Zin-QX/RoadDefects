package com.zinqx.roaddefectsbackend.controller;

import com.zinqx.roaddefectsbackend.common.DeleteRequest;
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
import com.zinqx.roaddefectsbackend.model.dto.picture.UploadTrendRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.DefectStatisticsRequest;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import com.zinqx.roaddefectsbackend.model.entity.User;
import com.zinqx.roaddefectsbackend.model.enums.PictureReviewStatusEnum;
import com.zinqx.roaddefectsbackend.model.vo.PictureVO;
import com.zinqx.roaddefectsbackend.model.vo.ApprovedTrendVO;
import com.zinqx.roaddefectsbackend.model.vo.DefectStatisticsVO;
import com.zinqx.roaddefectsbackend.model.vo.StatisticsVO;
import com.zinqx.roaddefectsbackend.model.vo.UploadTrendVO;
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
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
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
        // 上传图片
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 道路处理后删除图片
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, @RequestHeader("Authorization") String token) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 通过 token 获取用户
        User loginUser = userService.getUserByToken(token);
        long id = deleteRequest.getId();
        // 判断是否存在
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

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

    /**
     * 获取统计数据
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/statistics")
    public BaseResponse<StatisticsVO> getStatistics() {
        StatisticsVO statisticsVO = pictureService.getStatistics();
        return ResultUtils.success(statisticsVO);
    }

    /**
     * 获取上传趋势
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload/trend")
    public BaseResponse<UploadTrendVO> getUploadTrend(@RequestBody(required = false) UploadTrendRequest uploadTrendRequest) {
        if (uploadTrendRequest == null) {
            uploadTrendRequest = new UploadTrendRequest();
        }
        UploadTrendVO uploadTrendVO = pictureService.getUploadTrend(uploadTrendRequest);
        return ResultUtils.success(uploadTrendVO);
    }

    /**
     * 获取审核通过趋势
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/approved/trend")
    public BaseResponse<ApprovedTrendVO> getApprovedTrend(@RequestBody(required = false) UploadTrendRequest uploadTrendRequest) {
        if (uploadTrendRequest == null) {
            uploadTrendRequest = new UploadTrendRequest();
        }
        ApprovedTrendVO approvedTrendVO = pictureService.getApprovedTrend(uploadTrendRequest);
        return ResultUtils.success(approvedTrendVO);
    }

    /**
     * 获取缺陷统计数据
     */
    @PostMapping("/defect/statistics")
    public BaseResponse<DefectStatisticsVO> getDefectStatistics(@RequestBody DefectStatisticsRequest defectStatisticsRequest) {
        if (defectStatisticsRequest == null) {
            defectStatisticsRequest = new DefectStatisticsRequest();
        }
        DefectStatisticsVO defectStatisticsVO = pictureService.getDefectStatistics(defectStatisticsRequest);
        return ResultUtils.success(defectStatisticsVO);
    }

    /**
     * 分页获取当前用户上传的图片
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<PictureVO>> getMyPictureVOByPage(@RequestBody(required = false) PictureQueryRequest pictureQueryRequest,
                                                              HttpServletRequest request,
                                                              @RequestHeader("Authorization") String token) {
        // 处理 Bearer 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 通过 token 获取用户
        User loginUser = userService.getUserByToken(token);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        
        if (pictureQueryRequest == null) {
            pictureQueryRequest = new PictureQueryRequest();
        }
        
        // 获取当前用户上传的图片
        Page<PictureVO> pictureVOPage = pictureService.getMyPictureVOPage(pictureQueryRequest, loginUser, request);
        return ResultUtils.success(pictureVOPage);
    }



}
