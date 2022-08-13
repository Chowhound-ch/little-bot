package com.zsck.bot.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsck.bot.common.pojo.PermitDetail;
import com.zsck.bot.common.service.PermitDetailService;
import com.zsck.bot.mybatis.mapper.PermitDetailMapper;
import org.springframework.stereotype.Service;

/**
 * @author QQ:825352674
 * @date 2022/8/13 - 13:40
 */
@Service
public class PermitDetailServiceImpl
        extends ServiceImpl<PermitDetailMapper, PermitDetail>
        implements PermitDetailService {
}
