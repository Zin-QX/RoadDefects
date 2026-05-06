package com.zinqx.roaddefectsbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> dates;

    private List<Integer> uploadCounts;

}
