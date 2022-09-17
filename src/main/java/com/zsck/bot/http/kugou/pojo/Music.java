package com.zsck.bot.http.kugou.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author QQ:825352674
 * @date 2022/8/12 - 21:11
 */
@TableName("music")
@NoArgsConstructor
@Data
public class Music {
    @TableId
    private Integer id;
    private String audioName;
    @JsonProperty("song_name")
    private String title;
    @JsonProperty("author_name")
    private String artist;

    private String md5;
    @JsonProperty("play_backup_url")
    private String url;
    @TableField(exist = false)
    @JsonProperty("img")
    private String imgUrl;

    public Music(String audioName, String songName, String artist, String md5, String url) {
        this.audioName = audioName;
        this.title = songName;
        this.artist = artist;
        this.md5 = md5;
        this.url = url;
    }
}
