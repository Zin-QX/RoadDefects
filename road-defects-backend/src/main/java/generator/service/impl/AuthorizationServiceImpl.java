package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinqx.roaddefectsbackend.model.entity.Authorization;
import generator.service.AuthorizationService;
import com.zinqx.roaddefectsbackend.mapper.AuthorizationMapper;
import org.springframework.stereotype.Service;

/**
* @author 23378
* @description 针对表【authorization(用户授权)】的数据库操作Service实现
* @createDate 2026-03-21 09:37:16
*/
@Service
public class AuthorizationServiceImpl extends ServiceImpl<AuthorizationMapper, Authorization>
    implements AuthorizationService{

}




