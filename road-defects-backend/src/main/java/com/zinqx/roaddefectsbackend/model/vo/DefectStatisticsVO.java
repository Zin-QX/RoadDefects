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
public class DefectStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<DefectItem> defects;

    private Integer totalCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefectItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private String defectType;

        private Integer count;

        private Double percentage;

    }

}
