package com.zinqx.roaddefectsbackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVO implements Serializable {

    private static final long serialVersionUID = -7873933699385701993L;
    /**
     * id  
     */  
    private Long id;  
  
    /**  
     * 图片 url  
     */  
    private String url;  
  
    /**  
     * 图片名称  
     */  
    private String name;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 地址
     */
    private String address;

    /**
     * 处理后的图片 url
     */
    private String processedUrl;

    /**
     * 处理结果（列表形式）
     */
    private List<String> processedResult;
  

    /**  
     * 文件体积  
     */  
    private Long picSize;  

    /**  
     * 创建用户 id
     */  
    private Long userId;

    /**
     * 创建用户电话
     */
    private String phone;
  
    /**  
     * 创建时间  
     */  
    private Date createTime;

    /**  
     * 更新时间  
     */  
    private Date updateTime;  

    /**
     * 审核状态：0-待审核；1-道路有异常；2-道路无异常；3-拒绝通过
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;
  
    /**  
     * 创建用户信息  
     */  
    private UserVO user;  

  
    /**  
     * 封装类转对象  
     */  
    public static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {  
            return null;  
        }  
        Picture picture = new Picture();  
        BeanUtils.copyProperties(pictureVO, picture);
        // 将 List<String> 类型的 processedResult 转换为 String
        if (pictureVO.getProcessedResult() != null) {
            picture.setProcessedResult(JSONUtil.toJsonStr(pictureVO.getProcessedResult()));
        }
        return picture;  
    }  
  
    /**  
     * 对象转封装类  
     */  
    public static PictureVO objToVo(Picture picture) {  
        if (picture == null) {  
            return null;  
        }  
        PictureVO pictureVO = new PictureVO();  
        BeanUtils.copyProperties(picture, pictureVO);
        // 将 String 类型的 processedResult 转换为 List<String>
        if (picture.getProcessedResult() != null && !picture.getProcessedResult().isEmpty()) {
            pictureVO.setProcessedResult(JSONUtil.toList(picture.getProcessedResult(), String.class));
        }
        return pictureVO;  
    }  
}