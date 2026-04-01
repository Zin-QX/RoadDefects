package com.zinqx.roaddefectsbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id（用于修改）  
     */  
    private Long id;

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
  
    private static final long serialVersionUID = 1L;  
}
