package com.zinqx.roaddefectsbackend.model.dto.picture;

import com.zinqx.roaddefectsbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;


@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest  implements Serializable {


    private static final long serialVersionUID = 7860629912889958545L;

    /**
     * 图片 id
     */
    private Long id;
    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 地址
     */
    private String address;

    /**
     * 创建时间开始
     */
    private Date startTime;

    /**
     * 创建时间结束
     */
    private Date endTime;

    /**
     * 状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核人 id
     */
    private Long reviewerId;


}
