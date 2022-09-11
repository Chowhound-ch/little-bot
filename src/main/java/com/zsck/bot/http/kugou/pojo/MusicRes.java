package com.zsck.bot.http.kugou.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author QQ:825352674
 * @date 2022/9/11 - 12:34
 */
@Data
public class MusicRes {
    private String showTips;
    @JsonProperty("songname")
    private String songName;
    private String authorName;
    @JsonProperty("data")
    private MusicDetail musicDetail;
}
