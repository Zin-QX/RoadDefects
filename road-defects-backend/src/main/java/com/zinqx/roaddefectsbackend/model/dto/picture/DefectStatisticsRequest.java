package com.zinqx.roaddefectsbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class DefectStatisticsRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String province;

    private String city;

    private String district;

}
