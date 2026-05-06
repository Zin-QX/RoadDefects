package com.zinqx.roaddefectsbackend.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureQueryRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureReviewRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureUploadRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.UploadTrendRequest;
import com.zinqx.roaddefectsbackend.model.dto.picture.DefectStatisticsRequest;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import com.zinqx.roaddefectsbackend.model.entity.User;
import com.zinqx.roaddefectsbackend.model.vo.PictureVO;
import com.zinqx.roaddefectsbackend.model.vo.ApprovedTrendVO;
import com.zinqx.roaddefectsbackend.model.vo.DefectStatisticsVO;
import com.zinqx.roaddefectsbackend.model.vo.StatisticsVO;
import com.zinqx.roaddefectsbackend.model.vo.UploadTrendVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author 23378
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-03-11 22:48:13
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);
    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 获取查询条件
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片封装类
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装类
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 获取统计数据
     * @return
     */
    StatisticsVO getStatistics();

    /**
     * 获取上传趋势
     * @param uploadTrendRequest
     * @return
     */
    UploadTrendVO getUploadTrend(UploadTrendRequest uploadTrendRequest);

    /**
     * 获取审核通过趋势
     * @param uploadTrendRequest
     * @return
     */
    ApprovedTrendVO getApprovedTrend(UploadTrendRequest uploadTrendRequest);

    /**
     * 获取缺陷统计数据
     * @param defectStatisticsRequest
     * @return
     */
    DefectStatisticsVO getDefectStatistics(DefectStatisticsRequest defectStatisticsRequest);

    /**
     * 分页获取当前用户上传的图片
     * @param pictureQueryRequest
     * @param loginUser
     * @param request
     * @return
     */
    Page<PictureVO> getMyPictureVOPage(PictureQueryRequest pictureQueryRequest, User loginUser, HttpServletRequest request);

}
