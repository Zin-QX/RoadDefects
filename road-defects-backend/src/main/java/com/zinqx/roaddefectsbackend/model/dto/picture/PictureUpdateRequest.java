package com.zinqx.roaddefectsbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUpdateRequest implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 图片名称  
     */  
    private String name;  
  

  
    private static final long serialVersionUID = 1L;  
}
