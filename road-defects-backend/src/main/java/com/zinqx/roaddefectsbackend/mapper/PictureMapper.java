package com.zinqx.roaddefectsbackend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 23378
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2026-03-11 22:48:13
* @Entity generator.domain.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {

    /**
     * 获取所有图片记录的 url / processedUrl（包含 isDelete=1 的逻辑删除记录）。
     *
     * 说明：该查询使用自定义 SQL，不走 MyBatis-Plus 内置逻辑删除条件拼接，
     * 因而能覆盖“数据库有记录但逻辑删除”的场景，避免误删 COS 文件。
     */
    @Select("select url, processedUrl from picture")
    List<Picture> selectAllUrlFields();
}




