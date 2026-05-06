package com.zinqx.roaddefectsbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadTrendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dateRange;

    private String startDate;

    private String endDate;

}
