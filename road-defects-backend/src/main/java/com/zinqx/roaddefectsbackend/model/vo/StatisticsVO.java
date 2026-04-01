package com.zinqx.roaddefectsbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer todayUploads;

    private Double todayUploadsChange;

    private Integer totalUploads;

    private Double totalUploadsChange;

    private Integer activeUsers;

    private Double activeUsersChange;

    private Integer pendingReview;

    private Double pendingReviewChange;

}
