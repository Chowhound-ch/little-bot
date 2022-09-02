package com.zsck.bot.http.mihoyo.sign.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsck.bot.http.mihoyo.sign.mapper.GenshinInfoMapper;
import com.zsck.bot.http.mihoyo.sign.pojo.GenshinInfo;
import com.zsck.bot.http.mihoyo.sign.service.GenshinService;
import org.springframework.stereotype.Service;

/**
 * @author QQ:825352674
 * @date 2022/8/31 - 22:51
 */
@Service
public class GenshinServiceImpl extends ServiceImpl<GenshinInfoMapper, GenshinInfo> implements GenshinService {
}
