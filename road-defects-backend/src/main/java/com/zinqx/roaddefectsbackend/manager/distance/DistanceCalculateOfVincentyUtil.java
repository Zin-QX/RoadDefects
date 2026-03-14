package com.zinqx.roaddefectsbackend.manager.distance;

import java.text.DecimalFormat;

import static java.lang.Float.NaN;

import org.springframework.stereotype.Component;

/**
 *
 * Vincenty公式计算椭球面两点间绝对距离<br/>
 * 理论计算结果精度可达到1米内<br/>
 *
 * 参考文献：
 * https://www.movable-type.co.uk/scripts/latlong.html
 * https://www.movable-type.co.uk/scripts/latlong-vincenty.html
 * https://blog.csdn.net/qweasdzxc01233210/article/details/103405798
 *
 * @author Dong
 *
 */
@Component
public class DistanceCalculateOfVincentyUtil implements DistanceStrategy {

    /** 长半径a=6378137 */
    private static final double a = 6378137;
    /** 短半径b=6356752.314245 */
    private static final double b = 6356752.314245;
    /** 扁率f=1/298.257223563 */
    private static final double f = 1 / 298.257223563;

    /**
     * 根据提供的经纬度计算两点间距离
     *
     * @param lat_one 坐标1纬度
     * @param lon_one 坐标1经度
     * @param lat_two 坐标2纬度
     * @param lon_two 坐标2经度
     * @return 两点间距离 （米）
     */
    public static double getDistance(Double lat_one, Double lon_one, Double lat_two, Double lon_two)
    {
        double L = DistanceCalculateOfVincentyUtil.toRadians(lon_one - lon_two);
        double U1 = Math.atan((1 - f) * Math.tan(DistanceCalculateOfVincentyUtil.toRadians(lat_one)));
        double U2 = Math.atan((1 - f) * Math.tan(DistanceCalculateOfVincentyUtil.toRadians(lat_two)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1),
                sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);
        double lambda = L, lambdaP = Math.PI;
        double cosSqAlpha = 0L, sinSigma = 0L, cos2SigmaM = 0L, cosSigma = 0L, sigma = 0L;
        int circleCount = 40;
        //迭代循环
        while (Math.abs(lambda - lambdaP) > 1e-12 && --circleCount > 0) {
            double sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda) +
                    (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0) {
                return 0;  // co-incident points
            }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double alpha = Math.asin(cosU1 * cosU2 * sinLambda / sinSigma);
            cosSqAlpha = Math.cos(alpha) * Math.cos(alpha);
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * Math.sin(alpha) *
                    (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        }
        if (circleCount == 0) {
            return NaN;  // formula failed to converge
        }
        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
                B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));

        double result = b * A * (sigma - deltaSigma);
        DecimalFormat format = new DecimalFormat("0.000");
        double distance = Double.parseDouble(format.format(result));
        return distance;
    }

    /**
     * 根据提供的角度值，将其转化为弧度
     *
     * @param angle 角度值
     * @return 结果
     */
    public static double toRadians(Double angle)
    {
        double result = 0L;
        if (angle != null) {
            result = angle * Math.PI / 180;
        }
        return result;
    }

    @Override
    public double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        return getDistance(lat1, lng1, lat2, lng2);
    }


}