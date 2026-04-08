package com.zinqx.roaddefectsbackend.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zinqx.roaddefectsbackend.manager.CosManager;
import com.zinqx.roaddefectsbackend.mapper.PictureMapper;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 清理孤立文件定时任务
 * 用于清理 COS 中存在但数据库中没有记录的图片文件
 */
@Slf4j
@Component
public class CleanOrphanFilesJob {

    @Resource
    private CosManager cosManager;

    @Resource
    private PictureMapper pictureMapper;

    /**
     * 每 5 分钟执行一次（测试用）
     * 正式环境为：0 0 2 * * ?（每天凌晨 2 点）
     */
//    @Scheduled(cron = "0 */5 * * * ?")
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOrphanFiles() {
        log.info("开始执行清理孤立文件任务");
        
        try {
            // 1. 获取数据库中所有未删除记录的图片 URL
            Set<String> dbFileKeys = getAllDbFileKeys();
            log.info("数据库中共有 {} 个文件记录", dbFileKeys.size());
            
            // 2. 获取 COS 中所有的文件（包括两个前缀目录）
            List<String> cosFileKeys = new ArrayList<>();
            
            // 列出 RoadDefects/ 目录下的文件
            List<String> roadDefectsFiles = cosManager.listObjects("RoadDefects/");
            log.info("COS 中 RoadDefects/ 目录共有 {} 个文件", roadDefectsFiles.size());
            cosFileKeys.addAll(roadDefectsFiles);
            
            // 列出 road-defects/ 目录下的文件
            List<String> roadDefectsLowerFiles = cosManager.listObjects("road-defects/");
            log.info("COS 中 road-defects/ 目录共有 {} 个文件", roadDefectsLowerFiles.size());
            cosFileKeys.addAll(roadDefectsLowerFiles);
            
            log.info("COS 中共有 {} 个文件", cosFileKeys.size());
            
            // 3. 找出孤立文件（在 COS 中存在但在数据库中不存在）
            List<String> orphanFiles = cosFileKeys.stream()
                    .filter(key -> !dbFileKeys.contains(key))
                    .collect(Collectors.toList());
            
            if (CollUtil.isEmpty(orphanFiles)) {
                log.info("没有发现孤立文件");
                return;
            }
            
            log.info("发现 {} 个孤立文件，开始清理", orphanFiles.size());
            
            // 4. 删除孤立文件
            int deletedCount = 0;
            for (String orphanFile : orphanFiles) {
                try {
                    cosManager.deleteObject(orphanFile);
                    deletedCount++;
                    log.info("成功删除孤立文件：{}", orphanFile);
                } catch (Exception e) {
                    log.error("删除孤立文件失败：{}", orphanFile, e);
                }
            }
            
            log.info("清理孤立文件任务完成，共删除 {} 个文件", deletedCount);
            
        } catch (Exception e) {
            log.error("清理孤立文件任务执行失败", e);
        }
    }

    /**
     * 获取数据库中所有未删除记录的文件 key
     * 包括原始图片 url 和处理后的图片 processedUrl
     * 逻辑删除的记录（isDelete=1）不参与计算，其对应的 COS 文件不删除
     */
    private Set<String> getAllDbFileKeys() {
        Set<String> dbFileKeys = new HashSet<>();
        
        // 查询所有未删除图片的 url 和 processedUrl 字段
        // MyBatis-Plus 的 @TableLogic 会自动过滤 isDelete=1 的记录
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("url", "processedUrl");
        queryWrapper.eq("isDelete", 0);  // 明确指定只查询未删除的记录
        List<Picture> pictures = pictureMapper.selectList(queryWrapper);
        
        log.info("查询到 {} 条未删除的图片记录", pictures.size());
        
        // 提取 url 和 processedUrl 中的 key
        for (Picture picture : pictures) {
            // 提取 url 的 key
            String urlKey = extractKeyFromUrl(picture.getUrl());
            if (StrUtil.isNotEmpty(urlKey)) {
                dbFileKeys.add(urlKey);
            }
            
            // 提取 processedUrl 的 key
            String processedUrlKey = extractKeyFromUrl(picture.getProcessedUrl());
            if (StrUtil.isNotEmpty(processedUrlKey)) {
                dbFileKeys.add(processedUrlKey);
            }
        }
        
        return dbFileKeys;
    }

    /**
     * 从 URL 中提取 key
     * 处理 URL 中的双斜杠问题
     *
     */
    private String extractKeyFromUrl(String url) {
        if (StrUtil.isEmpty(url)) {
            return null;
        }
        
        try {
            // 查找 .com/ 后面的部分
            int index = url.indexOf(".com/");
            if (index > 0) {
                String key = url.substring(index + 5);
                // 去除开头的多余斜杠（处理 //RoadDefects 的情况）
                while (key.startsWith("/")) {
                    key = key.substring(1);
                }
                return key;
            }
        } catch (Exception e) {
            log.warn("提取 key 失败，url: {}", url, e);
        }
        
        return null;
    }
}
