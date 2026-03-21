package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.Wxuser;
import generator.service.WxuserService;
import generator.mapper.WxuserMapper;
import org.springframework.stereotype.Service;

/**
* @author 23378
* @description 针对表【wxuser(用户信息)】的数据库操作Service实现
* @createDate 2026-03-20 15:46:36
*/
@Service
public class WxuserServiceImpl extends ServiceImpl<WxuserMapper, Wxuser>
    implements WxuserService{

}




