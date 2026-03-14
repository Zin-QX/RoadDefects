package com.zinqx.roaddefectsbackend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zinqx.roaddefectsbackend.model.dto.picture.PictureUploadRequest;
import com.zinqx.roaddefectsbackend.model.entity.Picture;
import com.zinqx.roaddefectsbackend.model.entity.User;
import com.zinqx.roaddefectsbackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author 23378
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-03-11 22:48:13
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


}
