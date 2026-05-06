package com.zinqx.roaddefectsbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Python 服务配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "python.service")
public class PythonServiceProperties {

    /**
     * Python 服务基础 URL
     */
    private String baseUrl;

    /**
     * 图片处理接口路径
     */
    private String processPictureEndpoint;

    /**
     * 获取完整的图片处理接口 URL
     * @return
     */
    public String getProcessPictureUrl() {
        return baseUrl + processPictureEndpoint;
    }
}
