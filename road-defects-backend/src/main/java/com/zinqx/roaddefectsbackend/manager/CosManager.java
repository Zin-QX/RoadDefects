package com.zinqx.roaddefectsbackend.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;
import com.zinqx.roaddefectsbackend.config.CosClientConfig;
import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {  
  
    @Resource
    private CosClientConfig cosClientConfig;  
  
    @Resource  
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        // 构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 使用流上传对象
     *
     * @param key  唯一键
     * @param inputStream 输入流
     */
    public PutObjectResult putObject(String key, InputStream inputStream) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                inputStream, null);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 使用流上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param inputStream 输入流
     */
    public PutObjectResult putPictureObject(String key, InputStream inputStream) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                inputStream, null);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        // 构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 列出桶中的所有对象
     *
     * @param prefix 对象前缀（可选，用于筛选特定目录）
     * @return 对象 key 列表
     */
    public List<String> listObjects(String prefix) {
        List<String> objectKeys = new ArrayList<>();
        
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(cosClientConfig.getBucket());
        if (prefix != null && !prefix.isEmpty()) {
            listObjectsRequest.setPrefix(prefix);
        }
        listObjectsRequest.setMaxKeys(1000);
        
        ObjectListing objectListing = null;
        do {
            objectListing = cosClient.listObjects(listObjectsRequest);
            
            List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
            for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
                objectKeys.add(cosObjectSummary.getKey());
            }
            
            String nextMarker = objectListing.getNextMarker();
            listObjectsRequest.setMarker(nextMarker);
        } while (objectListing.isTruncated());
        
        return objectKeys;
    }
}
