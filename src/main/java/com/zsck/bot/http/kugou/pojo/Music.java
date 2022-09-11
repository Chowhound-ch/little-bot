package com.zsck.bot.http.kugou.pojo;

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
    private Integer id;
    private String audioName;
    //@JsonProperty("songname")
    private String songName;
    @JsonProperty("img")
    private String imgUrl;
    @JsonProperty("author_name")
    private String author;
    @JsonProperty("timelength")
    private Integer time;
    @JsonProperty("show_tips")
    private String tip;
    @TableId
    private String md5;
    private String playBackupUrl;

    public Music(String audioName, String songName, String author, String md5, String playBackupUrl) {
        this.audioName = audioName;
        this.songName = songName;
        this.author = author;
        this.md5 = md5;
        this.playBackupUrl = playBackupUrl;
    }
}
