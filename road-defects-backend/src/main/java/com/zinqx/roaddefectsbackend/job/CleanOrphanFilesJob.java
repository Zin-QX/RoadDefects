package com.zinqx.roaddefectsbackend.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
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
    @Scheduled(cron = "0 */5 * * * ?")
//    @Scheduled(cron = "0 0 2 * * ?")
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
     * 获取数据库中所有有记录的文件 key（无论 isDelete=0/1）
     * 包括原始图片 url 和处理后的图片 processedUrl
     *
     * 需求：COS 中只删除“数据库完全没有记录”的文件；
     * 因此即使图片记录被逻辑删除（isDelete=1），也必须视为“数据库有记录”，不能删除其 COS 文件。
     */
    private Set<String> getAllDbFileKeys() {
        Set<String> dbFileKeys = new HashSet<>();

        // 查询所有图片记录的 url / processedUrl（包含 isDelete=1 的记录）
        // 说明：使用自定义 SQL，避免 MyBatis-Plus 内置逻辑删除条件导致漏查已逻辑删除记录
        // 使用 MyBatis-Plus 查询会忽略 isDelete=1 的逻辑删除条件
        List<Picture> pictures = pictureMapper.selectAllUrlFields();

        log.info("查询到 {} 条图片记录（含逻辑删除记录）", pictures.size());

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
