package com.zinqx.roaddefectsbackend.manager.distance;

/**
 * @author 23378
 * @description 距离计算策略接口
 * @date 2026/3/11 22:48
 */
public interface DistanceStrategy {
    double calculateDistance(double lng1, double lat1, double lng2, double lat2);
}
