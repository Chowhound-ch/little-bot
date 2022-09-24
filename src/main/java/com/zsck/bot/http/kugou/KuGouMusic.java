package com.zsck.bot.http.kugou;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.zsck.bot.http.HttpBase;
import com.zsck.bot.http.kugou.pojo.Music;
import com.zsck.bot.http.kugou.pojo.MusicRes;
import com.zsck.bot.http.kugou.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author QQ:825352674
 * @date 2022/8/11 - 19:33
 */

@Slf4j
@Component
public class KuGouMusic extends HttpBase {
    public String path;
    @Autowired
    private MusicService musicService;


    @PostConstruct
    private void init(){
        path = ClassUtil.getClassPath()+"static/";
    }

    public Music getMusicUrlByAlbumIDAndHash(Music music){
        MusicRes musicRes = getMusicDetail(music.getAlbumID(), music.getFileHash());

        Music des = musicRes.getMusic();


        return des;
    }
    public Music getOneMusicForUrl(String keyword){
        return getMusicUrlByAlbumIDAndHash(getFileName(keyword));
    }

    /**
     * 返回一个带AlbumID和FileHash的music对象，不可直接使用
     */
    public Music getFileName(String keyWord){
        return getFileNames(keyWord, 1).get(0);
    }
    /**
     * 返回多个带AlbumID和FileHash的music对象，不可直接使用
     */
    public List<Music> getFileNames(String keyWord, Integer number){
        try {
            String searchRes= ReUtil.get("\\((.+)\\)",doGetStr(encoding(keyWord)), 1);
            JsonNode searchResJsonObj = objectMapper.readTree(searchRes);
            List<Music> list = new ArrayList<>();
            //默认取搜索结果前number歌，searchResJsonObj.optJSONObject("data").optJSONArray("lists") 为所有搜索结果
            for (int i = 0; i < number; i++) {
                JsonNode searchFirst = searchResJsonObj.get("data").get("lists").get(i);
                Music music = new Music();
                music.setAudioName(searchFirst.get("FileName").asText());
                music.setAlbumID(searchFirst.get("AlbumID").asText());
                music.setFileHash(searchFirst.get("FileHash").asText());
                list.add(music);
            }

            return list.stream()
                    .peek(value -> value.setAudioName(value.getAudioName().replaceAll("</?em>", "")))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private MusicRes getMusicDetail(String albumID, String hash){
        String urlForMusicDetail = "https://wwwapi.kugou.com/yy/index.php?r=play/getdata&callback=jQuery191033144701096575724_1660124702942&hash=" + hash+
                "&dfid=3eyKKr1tAQle0EQs9n1ItnQV&appid=1014&mid=d30a3efc49071a50132e4b338f93aa0a&platid=4&album_id=" + albumID +
                "&_=1660124702944";
        try{
            String detailRes = ReUtil.get("\\((.+)\\)", doGetStr(urlForMusicDetail), 1);
            MusicRes musicRes = objectMapper.readValue(detailRes, MusicRes.class);

            if (musicRes.getMusic() == null){
                log.warn("获取歌曲url失败，错误的返回信息: {}", detailRes);
            }
            return musicRes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String encoding(String keyWord){
        String signature = MD5Encoding.MD5(keyWord);
        keyWord = URLEncodeUtil.encode(keyWord);
        String url = "https://complexsearch.kugou.com/v2/search/song?callback=callback123&keyword=" + keyWord + "&page=1&pagesize=30&bitrate=0&isfuzzy=0&tag=em&inputtype=0&platform=WebFilter&userid=-1&clientver=2000&iscorrection=1&privilege_filter=0&srcappid=2919&clienttime=1600305065609&mid=1600305065609&uuid=1600305065609&dfid=-&signature=" + signature;
        return url;
    }

    @Override
    protected <T extends HttpRequestBase> T setHeader(T base) {
        return base;
    }
}
