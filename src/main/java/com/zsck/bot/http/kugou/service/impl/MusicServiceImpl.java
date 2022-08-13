package com.zsck.bot.http.kugou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsck.bot.http.kugou.pojo.Music;
import com.zsck.bot.http.kugou.service.MusicService;
import com.zsck.bot.mybatis.mapper.MusicMapper;
import org.springframework.stereotype.Service;

/**
 * @author QQ:825352674
 * @date 2022/8/12 - 21:12
 */
@Service
public class MusicServiceImpl extends ServiceImpl<MusicMapper, Music> implements MusicService {
}
