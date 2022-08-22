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
    private String songName;
    @JsonProperty("img")
    private String imgUrl;
    @JsonProperty("author_name")
    private String author;
    @JsonProperty("timelength")
    private Integer time;
    private String tip;
    @TableId
    private String md5;

    public Music(String tip) {
        this.tip = tip;
    }
}
