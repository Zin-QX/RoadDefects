package com.zinqx.roaddefectsbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import com.zinqx.roaddefectsbackend.model.dto.picture.UploadTrendRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.DefectStatisticsRequest;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import com.zinqx.roaddefectsbackend.model.entity.User;
import com.zinqx.roaddefectsbackend.model.enums.PictureReviewStatusEnum;
import com.zinqx.roaddefectsbackend.model.enums.UserRoleEnum;
import com.zinqx.roaddefectsbackend.model.vo.PictureVO;
import com.zinqx.roaddefectsbackend.model.vo.ApprovedTrendVO;
import com.zinqx.roaddefectsbackend.model.vo.DefectStatisticsVO;
import com.zinqx.roaddefectsbackend.model.vo.StatisticsVO;
import com.zinqx.roaddefectsbackend.model.vo.UploadTrendVO;
import com.zinqx.roaddefectsbackend.model.vo.UserVO;
import com.zinqx.roaddefectsbackend.service.PictureService;
import com.zinqx.roaddefectsbackend.mapper.PictureMapper;
import com.zinqx.roaddefectsbackend.service.UserService;
import com.zinqx.roaddefectsbackend.manager.PythonServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 23378
* @description 针对表【picture(图片)】的数据库操作 Service 实现
* @createDate 2026-03-11 22:48:13
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private UserService userService;
    @Resource
    private FileManager fileManager;

    @Resource
    private DistanceCalculateOfVincentyUtil vincentyStrategy;

    @Resource
    private PythonServiceClient pythonServiceClient;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
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
            // 距离小于3米，认为是同一地方的图片图片，执行更新操作
            if (distance < 5.0) {
                pictureId = existingPicture.getId();
            }else {
                // 如果不是同一位置
                ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "图片上传失败，同一地方请在5m范围内上传");
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
        // 填充用户电话
        picture.setPhone(loginUser.getPhone());
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
            // 更新用户电话（如果用户电话有变化）
            existingPicture.setPhone(loginUser.getPhone());
            boolean result = this.updateById(existingPicture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            
            // 调用 Python 接口处理图片
            if (existingPicture.getId() != null && existingPicture.getUrl() != null) {
                try {
                    PythonServiceClient.PythonProcessResult processResult = 
                            pythonServiceClient.processPicture(existingPicture.getId(), existingPicture.getUrl(), existingPicture.getUserId());
                    if (processResult != null) {
                        // 更新处理结果到数据库
                        existingPicture.setProcessedUrl(processResult.getProcessedUrl());
                        existingPicture.setProcessedResult(processResult.getProcessedResult());
                        this.updateById(existingPicture);
                        // 同步到返回对象
                        picture = existingPicture;
                    }
                } catch (Exception e) {
                    log.error("调用 Python 图片处理接口失败，pictureId: {}", existingPicture.getId(), e);
                }
            }
        } else {
            // 新增图片
            boolean result = this.save(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            
            // 调用 Python 接口处理图片
            if (picture.getId() != null && picture.getUrl() != null) {
                try {
                    PythonServiceClient.PythonProcessResult processResult = 
                            pythonServiceClient.processPicture(picture.getId(), picture.getUrl(), picture.getUserId());
                    if (processResult != null) {
                        // 更新处理结果到数据库
                        picture.setProcessedUrl(processResult.getProcessedUrl());
                        picture.setProcessedResult(processResult.getProcessedResult());
                        this.updateById(picture);
                    }
                } catch (Exception e) {
                    log.error("调用 Python 图片处理接口失败，pictureId: {}", picture.getId(), e);
                }
            }
        }
        return PictureVO.objToVo(picture);
    }


    /**
     * 填充审核参数
     * @param picture
     * @param loginUser
     */
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
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.likeRight( ObjUtil.isNotEmpty(address),"address", address);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.le(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

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

    @Override
    public StatisticsVO getStatistics() {
        // 获取今日开始和结束时间
        java.util.Calendar todayStart = java.util.Calendar.getInstance();
        todayStart.set(java.util.Calendar.HOUR_OF_DAY, 0);
        todayStart.set(java.util.Calendar.MINUTE, 0);
        todayStart.set(java.util.Calendar.SECOND, 0);
        todayStart.set(java.util.Calendar.MILLISECOND, 0);
        
        java.util.Calendar todayEnd = java.util.Calendar.getInstance();
        todayEnd.set(java.util.Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(java.util.Calendar.MINUTE, 59);
        todayEnd.set(java.util.Calendar.SECOND, 59);
        todayEnd.set(java.util.Calendar.MILLISECOND, 999);
        
        // 获取昨日开始和结束时间
        java.util.Calendar yesterdayStart = java.util.Calendar.getInstance();
        yesterdayStart.add(java.util.Calendar.DAY_OF_MONTH, -1);
        yesterdayStart.set(java.util.Calendar.HOUR_OF_DAY, 0);
        yesterdayStart.set(java.util.Calendar.MINUTE, 0);
        yesterdayStart.set(java.util.Calendar.SECOND, 0);
        yesterdayStart.set(java.util.Calendar.MILLISECOND, 0);
        
        java.util.Calendar yesterdayEnd = java.util.Calendar.getInstance();
        yesterdayEnd.add(java.util.Calendar.DAY_OF_MONTH, -1);
        yesterdayEnd.set(java.util.Calendar.HOUR_OF_DAY, 23);
        yesterdayEnd.set(java.util.Calendar.MINUTE, 59);
        yesterdayEnd.set(java.util.Calendar.SECOND, 59);
        yesterdayEnd.set(java.util.Calendar.MILLISECOND, 999);
        
        // 1. 今日上传数
        long todayUploads = this.lambdaQuery()
                .ge(Picture::getCreateTime, todayStart.getTime())
                .le(Picture::getCreateTime, todayEnd.getTime())
                .count();
        
        // 2. 昨日上传数
        long yesterdayUploads = this.lambdaQuery()
                .ge(Picture::getCreateTime, yesterdayStart.getTime())
                .le(Picture::getCreateTime, yesterdayEnd.getTime())
                .count();
        
        // 3. 总上传数
        long totalUploads = this.count();
        
        // 4. 活跃用户数（今日上传过图片的用户数）
        long activeUsers = this.lambdaQuery()
                .ge(Picture::getCreateTime, todayStart.getTime())
                .le(Picture::getCreateTime, todayEnd.getTime())
                .select(Picture::getUserId)
                .groupBy(Picture::getUserId)
                .list()
                .size();
        
        // 5. 昨日活跃用户数
        long yesterdayActiveUsers = this.lambdaQuery()
                .ge(Picture::getCreateTime, yesterdayStart.getTime())
                .le(Picture::getCreateTime, yesterdayEnd.getTime())
                .select(Picture::getUserId)
                .groupBy(Picture::getUserId)
                .list()
                .size();
        
        // 6. 待审核数量
        long pendingReview = this.lambdaQuery()
                .eq(Picture::getReviewStatus, PictureReviewStatusEnum.REVIEWING.getValue())
                .count();
        
        // 7. 昨日待审核数量（用于计算变化）
        // 这里简化处理，使用当前待审核与昨日上传数的对比
        long yesterdayPending = this.lambdaQuery()
                .ge(Picture::getCreateTime, yesterdayStart.getTime())
                .le(Picture::getCreateTime, yesterdayEnd.getTime())
                .eq(Picture::getReviewStatus, PictureReviewStatusEnum.REVIEWING.getValue())
                .count();
        
        // 计算变化百分比
        double todayUploadsChange = calculateChangePercent(todayUploads, yesterdayUploads);
        double totalUploadsChange = calculateChangePercent(totalUploads, totalUploads - todayUploads);
        double activeUsersChange = calculateChangePercent(activeUsers, yesterdayActiveUsers);
        double pendingReviewChange = calculateChangePercent(pendingReview, yesterdayPending);
        
        return StatisticsVO.builder()
                .todayUploads((int) todayUploads)
                .todayUploadsChange(todayUploadsChange)
                .totalUploads((int) totalUploads)
                .totalUploadsChange(totalUploadsChange)
                .activeUsers((int) activeUsers)
                .activeUsersChange(activeUsersChange)
                .pendingReview((int) pendingReview)
                .pendingReviewChange(pendingReviewChange)
                .build();
    }
    
    private double calculateChangePercent(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return Math.round((double) (current - previous) / previous * 1000.0) / 10.0;
    }

    @Override
    public UploadTrendVO getUploadTrend(UploadTrendRequest uploadTrendRequest) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displaySdf = new SimpleDateFormat("yyyy-M-d");
        
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        
        // 设置结束日期为今天的最后一刻
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        
        // 根据参数确定日期范围
        String dateRange = uploadTrendRequest.getDateRange();
        String startDateStr = uploadTrendRequest.getStartDate();
        String endDateStr = uploadTrendRequest.getEndDate();
        
        if (StrUtil.isNotEmpty(startDateStr) && StrUtil.isNotEmpty(endDateStr)) {
            // 自定义日期范围
            try {
                startCal.setTime(sdf.parse(startDateStr));
                endCal.setTime(sdf.parse(endDateStr));
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
            } catch (Exception e) {
                log.error("日期解析失败", e);
            }
        } else {
            // 预设日期范围
            if ("30days".equals(dateRange)) {
                startCal.add(Calendar.DAY_OF_MONTH, -29);
            } else if ("15days".equals(dateRange)) {
                startCal.add(Calendar.DAY_OF_MONTH, -14);
            } else if ("7days".equals(dateRange)) {
                startCal.add(Calendar.DAY_OF_MONTH, -6);
            } else {
                // 默认本月
                startCal.set(Calendar.DAY_OF_MONTH, 1);
            }
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
        }
        
        // 生成日期列表
        List<String> dates = new ArrayList<>();
        List<Integer> uploadCounts = new ArrayList<>();
        
        Calendar tempCal = (Calendar) startCal.clone();
        while (!tempCal.after(endCal)) {
            dates.add(displaySdf.format(tempCal.getTime()));
            
            // 获取当天的开始和结束时间
            Calendar dayStart = (Calendar) tempCal.clone();
            dayStart.set(Calendar.HOUR_OF_DAY, 0);
            dayStart.set(Calendar.MINUTE, 0);
            dayStart.set(Calendar.SECOND, 0);
            dayStart.set(Calendar.MILLISECOND, 0);
            
            Calendar dayEnd = (Calendar) tempCal.clone();
            dayEnd.set(Calendar.HOUR_OF_DAY, 23);
            dayEnd.set(Calendar.MINUTE, 59);
            dayEnd.set(Calendar.SECOND, 59);
            dayEnd.set(Calendar.MILLISECOND, 999);
            
            // 查询当天的上传数量
            long count = this.lambdaQuery()
                    .ge(Picture::getCreateTime, dayStart.getTime())
                    .le(Picture::getCreateTime, dayEnd.getTime())
                    .count();
            
            uploadCounts.add((int) count);
            
            // 移到下一天
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return UploadTrendVO.builder()
                .dates(dates)
                .uploadCounts(uploadCounts)
                .build();
    }

    @Override
    public ApprovedTrendVO getApprovedTrend(UploadTrendRequest uploadTrendRequest) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displaySdf = new SimpleDateFormat("yyyy-M-d");
        
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        
        // 设置结束日期为今天的最后一刻
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        
        // 根据参数确定日期范围
        String dateRange = uploadTrendRequest.getDateRange();
        String startDateStr = uploadTrendRequest.getStartDate();
        String endDateStr = uploadTrendRequest.getEndDate();
        
        if (StrUtil.isNotEmpty(startDateStr) && StrUtil.isNotEmpty(endDateStr)) {
            // 自定义日期范围
            try {
                startCal.setTime(sdf.parse(startDateStr));
                endCal.setTime(sdf.parse(endDateStr));
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
            } catch (Exception e) {
                log.error("日期解析失败", e);
            }
        } else {
            // 预设日期范围
            if ("30days".equals(dateRange)) {
                startCal.add(Calendar.DAY_OF_MONTH, -29);
            } else if ("15days".equals(dateRange)) {
                startCal.add(Calendar.DAY_OF_MONTH, -14);
            } else if ("7days".equals(dateRange)) {
                startCal.add(Calendar.DAY_OF_MONTH, -6);
            } else {
                // 默认本月
                startCal.set(Calendar.DAY_OF_MONTH, 1);
            }
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
        }
        
        // 生成日期列表
        List<String> dates = new ArrayList<>();
        List<Integer> approvedCounts = new ArrayList<>();
        
        Calendar tempCal = (Calendar) startCal.clone();
        while (!tempCal.after(endCal)) {
            dates.add(displaySdf.format(tempCal.getTime()));
            
            // 获取当天的开始和结束时间
            Calendar dayStart = (Calendar) tempCal.clone();
            dayStart.set(Calendar.HOUR_OF_DAY, 0);
            dayStart.set(Calendar.MINUTE, 0);
            dayStart.set(Calendar.SECOND, 0);
            dayStart.set(Calendar.MILLISECOND, 0);
            
            Calendar dayEnd = (Calendar) tempCal.clone();
            dayEnd.set(Calendar.HOUR_OF_DAY, 23);
            dayEnd.set(Calendar.MINUTE, 59);
            dayEnd.set(Calendar.SECOND, 59);
            dayEnd.set(Calendar.MILLISECOND, 999);
            
            // 查询当天审核通过的数量（道路有异常 + 道路无异常）
            long count = this.lambdaQuery()
                    .ge(Picture::getCreateTime, dayStart.getTime())
                    .le(Picture::getCreateTime, dayEnd.getTime())
                    .in(Picture::getReviewStatus, 
                            PictureReviewStatusEnum.PASS.getValue(), 
                            PictureReviewStatusEnum.NO_ISSUE.getValue())
                    .count();
            
            approvedCounts.add((int) count);
            
            // 移到下一天
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return ApprovedTrendVO.builder()
                .dates(dates)
                .approvedCounts(approvedCounts)
                .build();
    }

    @Override
    public DefectStatisticsVO getDefectStatistics(DefectStatisticsRequest defectStatisticsRequest) {
        // 构建地址查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        
        // 根据省市区构建地址查询
        StringBuilder addressBuilder = new StringBuilder();
        if (StrUtil.isNotEmpty(defectStatisticsRequest.getProvince())) {
            addressBuilder.append(defectStatisticsRequest.getProvince());
        }
        if (StrUtil.isNotEmpty(defectStatisticsRequest.getCity())) {
            addressBuilder.append(defectStatisticsRequest.getCity());
        }
        if (StrUtil.isNotEmpty(defectStatisticsRequest.getDistrict())) {
            addressBuilder.append(defectStatisticsRequest.getDistrict());
        }
        
        // 如果有地址条件，添加模糊查询
        if (addressBuilder.length() > 0) {
            queryWrapper.likeRight("address", addressBuilder.toString());
        }
        
        // 只查询已审核通过的图片
        queryWrapper.in("reviewStatus", 
                PictureReviewStatusEnum.PASS.getValue(), 
                PictureReviewStatusEnum.NO_ISSUE.getValue());
        
        // 查询符合条件的图片
        List<Picture> pictures = this.list(queryWrapper);
        
        // 统计缺陷类型
        Map<String, Integer> defectCountMap = new HashMap<>();
        int totalCount = 0;
        
        for (Picture picture : pictures) {
            if (StrUtil.isEmpty(picture.getProcessedResult())) {
                continue;
            }
            
            // 解析 processedResult，格式如：["修补坑洞(置信度:45.73%)", "井盖(置信度:38.75%)"]
            try {
                List<String> defects = JSONUtil.toList(picture.getProcessedResult(), String.class);
                for (String defect : defects) {
                    // 提取缺陷类型（去掉置信度部分）
                    String defectType = extractDefectType(defect);
                    if (StrUtil.isNotEmpty(defectType)) {
                        defectCountMap.merge(defectType, 1, Integer::sum);
                        totalCount++;
                    }
                }
            } catch (Exception e) {
                log.warn("解析 processedResult 失败：{}", picture.getProcessedResult(), e);
            }
        }
        
        // 构建返回结果
        List<DefectStatisticsVO.DefectItem> defectItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : defectCountMap.entrySet()) {
            String defectType = entry.getKey();
            Integer count = entry.getValue();
            Double percentage = totalCount > 0 ? 
                    Math.round((double) count / totalCount * 10000.0) / 100.0 : 0.0;
            
            defectItems.add(DefectStatisticsVO.DefectItem.builder()
                    .defectType(defectType)
                    .count(count)
                    .percentage(percentage)
                    .build());
        }
        
        // 按数量降序排序
        defectItems.sort((a, b) -> b.getCount().compareTo(a.getCount()));
        
        return DefectStatisticsVO.builder()
                .defects(defectItems)
                .totalCount(totalCount)
                .build();
    }
    
    /**
     * 从缺陷描述中提取缺陷类型（去掉置信度部分）
     * 例如："修补坑洞(置信度:45.73%)" -> "修补坑洞"
     */
    private String extractDefectType(String defect) {
        if (StrUtil.isEmpty(defect)) {
            return null;
        }
        // 去掉括号及括号内的内容
        int index = defect.indexOf("(");
        if (index > 0) {
            return defect.substring(0, index).trim();
        }
        return defect.trim();
    }

    @Override
    public Page<PictureVO> getMyPictureVOPage(PictureQueryRequest pictureQueryRequest, User loginUser, HttpServletRequest request) {
        if (pictureQueryRequest == null) {
            pictureQueryRequest = new PictureQueryRequest();
        }
        
        // 设置当前用户 ID
        pictureQueryRequest.setUserId(loginUser.getId());
        
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        
        // 查询数据库
        Page<Picture> picturePage = this.page(new Page<>(current, size),
                this.getQueryWrapper(pictureQueryRequest));
        
        // 获取封装类
        return this.getPictureVOPage(picturePage, request);
    }


}
