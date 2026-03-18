package com.zinqx.roaddefectsbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zinqx.roaddefectsbackend.exception.BusinessException;
import com.zinqx.roaddefectsbackend.exception.ErrorCode;
import com.zinqx.roaddefectsbackend.exception.ThrowUtils;
import com.zinqx.roaddefectsbackend.manager.FileManager;
import com.zinqx.roaddefectsbackend.manager.distance.DistanceCalculateOfVincentyUtil;
import com.zinqx.roaddefectsbackend.model.dto.file.UploadPictureResult;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureQueryRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureReviewRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureUploadRequest;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import com.zinqx.roaddefectsbackend.model.entity.User;
import com.zinqx.roaddefectsbackend.model.enums.PictureReviewStatusEnum;
import com.zinqx.roaddefectsbackend.model.enums.UserRoleEnum;
import com.zinqx.roaddefectsbackend.model.vo.PictureVO;
import com.zinqx.roaddefectsbackend.model.vo.UserVO;
import com.zinqx.roaddefectsbackend.service.PictureService;
import com.zinqx.roaddefectsbackend.mapper.PictureMapper;
import com.zinqx.roaddefectsbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 23378
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2026-03-11 22:48:13
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private UserService userService;
    @Resource
    private FileManager fileManager;

    @Resource
    private DistanceCalculateOfVincentyUtil vincentyStrategy;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 用于判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            Picture existingPicture = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .one();
            ThrowUtils.throwIf(existingPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可重新上传
            if (!existingPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 使用Vincenty公式计算距离
            double distance = vincentyStrategy.calculateDistance(
                    existingPicture.getLongitude(), 
                    existingPicture.getLatitude(), 
                    pictureUploadRequest.getLongitude(), 
                    pictureUploadRequest.getLatitude());
            // 距离小于5米，认为是同一地方的图片图片，执行更新操作
            if (distance < 5.0) {
                pictureId = existingPicture.getId();
            }else {
                // 如果不是同一位置
                ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "图片上传失败，请在5m范围内上传");
            }
        }
        // 上传图片，得到信息
        // 按照用户 id 划分目录
        String uploadPathPrefix = String.format("RoadDefects/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        // 设置经纬度
        if (pictureUploadRequest.getLatitude() != null && pictureUploadRequest.getLongitude() != null) {
            picture.setLatitude(pictureUploadRequest.getLatitude());
            picture.setLongitude(pictureUploadRequest.getLongitude());
        }else {
            ThrowUtils.throwIf(pictureUploadRequest.getLatitude() == null, ErrorCode.PARAMS_ERROR, "经纬度不能为空");
        }
        if (pictureUploadRequest.getAddress() != null){
            picture.setAddress(pictureUploadRequest.getAddress());
        }else {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "地址不能为空");
        }
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setUserId(loginUser.getId());
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，先查询原有记录
            Picture existingPicture = this.getById(pictureId);
            // 更新图片时，设置审核状态为待审核，并清除原有审核信息
            existingPicture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
            existingPicture.setReviewMessage(" ");
            existingPicture.setReviewerId(0L);
            // 更新其他字段
            existingPicture.setLatitude(pictureUploadRequest.getLatitude());
            existingPicture.setLongitude(pictureUploadRequest.getLongitude());
            existingPicture.setAddress(pictureUploadRequest.getAddress());
            existingPicture.setUrl(uploadPictureResult.getUrl());
            existingPicture.setName(uploadPictureResult.getPicName());
            existingPicture.setPicSize(uploadPictureResult.getPicSize());
            existingPicture.setUpdateTime(new Date());
            boolean result = this.updateById(existingPicture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        } else {
            // 新增图片
            boolean result = this.save(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        }
        return PictureVO.objToVo(picture);
    }


    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }


    /**
     * 判断用户是否是管理员
    */
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 获取查询条件
     * @param pictureQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        Long userId = pictureQueryRequest.getUserId();
        String address = pictureQueryRequest.getAddress();
        Date startTime = pictureQueryRequest.getStartTime();
        Date endTime = pictureQueryRequest.getEndTime();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.likeLeft( ObjUtil.isNotEmpty(address),"address", address);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.le(ObjUtil.isNotEmpty(endTime), "createTime", endTime);

        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 获取图片封装类
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
    }

    /**
     * 图片审核
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 已是该状态
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        // 更新审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }


}




