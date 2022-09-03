package com.zsck.bot.common.permit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsck.bot.common.permit.mapper.PermitDetailMapper;
import com.zsck.bot.common.permit.service.PermitDetailService;
import com.zsck.bot.common.permit.pojo.PermitDetail;
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
