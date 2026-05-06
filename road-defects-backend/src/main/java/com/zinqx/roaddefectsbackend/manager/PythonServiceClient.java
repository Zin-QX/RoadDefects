package com.zinqx.roaddefectsbackend.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zinqx.roaddefectsbackend.common.util.HttpClientUtil;
import com.zinqx.roaddefectsbackend.config.PythonServiceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Python 服务调用管理类
 */
@Slf4j
@Component
public class PythonServiceClient {

    @Resource
    private PythonServiceProperties pythonServiceProperties;

    /**
     * 调用 Python 图片处理接口
     * 
     * @param pictureId 图片 ID
     * @param pictureUrl 图片 URL
     * @param userId 用户 ID
     * @return 处理结果，包含 processedUrl 和 processedResult
     */
    public PythonProcessResult processPicture(Long pictureId, String pictureUrl, Long userId) {
        log.info("开始调用 Python 图片处理接口，pictureId: {}, pictureUrl: {}, userId: {}", pictureId, pictureUrl, userId);
        
        String processUrl = pythonServiceProperties.getProcessPictureUrl();
        
        // 构建请求参数（JSON 格式）
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", String.valueOf(pictureId));
        paramMap.put("url", pictureUrl);
        paramMap.put("userId", String.valueOf(userId));
        
        try {
            // 发送 POST 请求（JSON 格式）
            // Python 接口请求格式：
            // {
            //   "id": 2034196409431478274,
            //   "url": "https://zinqx-1391992760.cos.ap-guangzhou.myqcloud.com/RoadDefects/xxx.jpg",
            //   "userId": 1234567890
            // }
            String responseJson = HttpClientUtil.doPost4Json(processUrl, paramMap);
            log.info("Python 接口返回：{}", responseJson);
            
            // 解析返回结果
            // Python 接口返回格式示例：
            // {
            //   "id": 2034196409431478300,
            //   "processedUrl": "https://zinqx-1391992760.cos.ap-guangzhou.myqcloud.com/road-defects/xxx.jpg",
            //   "processedResult": "共检测到 2 个缺陷：Patch-Pothole(置信度:45.73%), Manhole(置信度:38.75%)"
            // }
            JSONObject response = JSON.parseObject(responseJson);
            
            // 直接解析返回的字段
            String processedUrl = response.getString("processedUrl");
            String processedResult = response.getString("processedResult");
            
            // 校验必要字段是否存在
            if (processedUrl == null || processedUrl.isEmpty()) {
                log.error("Python 接口返回的 processedUrl 为空，pictureId: {}", pictureId);
                return null;
            }
            
            log.info("图片处理成功，pictureId: {}, processedUrl: {}", pictureId, processedUrl);
            
            return new PythonProcessResult(processedUrl, processedResult);
            
        } catch (IOException e) {
            log.error("调用 Python 接口异常，pictureId: {}", pictureId, e);
            return null;
        } catch (Exception e) {
            log.error("解析 Python 接口返回异常，pictureId: {}", pictureId, e);
            return null;
        }
    }

    /**
     * Python 处理结果封装类
     */
    public static class PythonProcessResult {
        private String processedUrl;
        private String processedResult;

        public PythonProcessResult(String processedUrl, String processedResult) {
            this.processedUrl = processedUrl;
            this.processedResult = processedResult;
        }

        public String getProcessedUrl() {
            return processedUrl;
        }

        public void setProcessedUrl(String processedUrl) {
            this.processedUrl = processedUrl;
        }

        public String getProcessedResult() {
            return processedResult;
        }

        public void setProcessedResult(String processedResult) {
            this.processedResult = processedResult;
        }
    }
}
