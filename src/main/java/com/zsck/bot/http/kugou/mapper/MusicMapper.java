package com.zsck.bot.http.kugou.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsck.bot.http.kugou.pojo.Music;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author QQ:825352674
 * @date 2022/8/12 - 21:11
 */
@DS("music")
@Mapper
public interface MusicMapper extends BaseMapper<Music> {
    Music likeAudioName(@Param("audioName") String audioName);
}
