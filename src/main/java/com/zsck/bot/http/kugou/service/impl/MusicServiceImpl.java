package com.zsck.bot.http.kugou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsck.bot.http.kugou.pojo.Music;
import com.zsck.bot.http.kugou.service.MusicService;
import com.zsck.bot.http.kugou.mapper.MusicMapper;
import org.springframework.stereotype.Service;

/**
 * @author QQ:825352674
 * @date 2022/8/12 - 21:12
 */
@Service
public class MusicServiceImpl extends ServiceImpl<MusicMapper, Music> implements MusicService {
    @Override
    public void keepMusic(Music music) {
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Music::getAudioName, music.getAudioName()).or( con-> con.eq(Music::getMd5, music.getMd5()));
        remove(wrapper);//删除原有行
        save(music);
    }

    @Override
    public Music likeMusic(String keyword) {

        return baseMapper.likeAudioName(keyword);
    }
}
