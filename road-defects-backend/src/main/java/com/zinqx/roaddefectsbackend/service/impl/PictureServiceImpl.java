package com.zinqx.roaddefectsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zinqx.roaddefectsbackend.exception.ErrorCode;
import com.zinqx.roaddefectsbackend.exception.ThrowUtils;
import com.zinqx.roaddefectsbackend.manager.FileManager;
import com.zinqx.roaddefectsbackend.manager.distance.DistanceCalculateOfVincentyUtil;
import com.zinqx.roaddefectsbackend.model.dto.file.UploadPictureResult;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureUploadRequest;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import com.zinqx.roaddefectsbackend.model.entity.User;
import com.zinqx.roaddefectsbackend.model.vo.PictureVO;
import com.zinqx.roaddefectsbackend.service.PictureService;
import com.zinqx.roaddefectsbackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author 23378
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2026-03-11 22:48:13
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

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
            // 如果存在经纬度，使用Vincenty公式计算距离
            if (existingPicture.getLatitude() != null && existingPicture.getLongitude() != null 
                    && pictureUploadRequest.getLatitude() != null && pictureUploadRequest.getLongitude() != null) {
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
        }
        // 上传图片，得到信息
        // 按照用户 id 划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
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
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVo(picture);
    }


}




