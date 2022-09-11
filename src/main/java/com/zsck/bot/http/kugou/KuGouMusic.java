package com.zsck.bot.http.kugou;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.zsck.bot.http.HttpBase;
import com.zsck.bot.http.kugou.pojo.Music;
import com.zsck.bot.http.kugou.pojo.MusicDetail;
import com.zsck.bot.http.kugou.pojo.MusicRes;
import com.zsck.bot.http.kugou.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

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

    public Music getFileName(String keyWord){
        try {
            String searchRes= ReUtil.get("\\((.+)\\)",doGetStr(encoding(keyWord)), 1);
            JsonNode searchResJsonObj = objectMapper.readTree(searchRes);
            //默认取搜索结果第一个，searchResJsonObj.optJSONObject("data").optJSONArray("lists") 为所有搜索结果
            JsonNode searchFirst = searchResJsonObj.get("data").get("lists").get(0);

            //准备发送下一个请求获取mp3文件
            MusicRes musicRes = getMusicDetail(searchFirst);

            MusicDetail musicDetail = musicRes.getMusicDetail();

            if (musicRes.getShowTips() != null){
                musicDetail.setTip(musicRes.getShowTips());
                return musicDetail;
            }
            return musicDetail;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*private List<String> readyForSender(String fileName, MusicDetail musicDetail){
        List<String> list = getReady(musicDetail, musicDetail.getIsFreePart());
        list.add(fileName);
        return list;
    }
    public List<String> getReady(Music music, @Nullable Integer isPartFree*//*默认0*//*){
        List<String> list = new ArrayList<>();
        list.add("歌曲:" + (StrUtil.isBlankOrUndefined(music.getSongName())? "无法获取" :music.getSongName()));
        list.add("歌手:" + music.getAuthor());
        if (!StrUtil.isBlankOrUndefined(music.getTip())){
            list.add(music.getTip());
            return list;
        }else {
            if (!StrUtil.isBlankOrUndefined(music.getImgUrl())) {
                list.add("url:" + music.getImgUrl());
            }
            if (isPartFree != null && isPartFree == 1){
                list.add("tip: 歌曲为付费歌曲，仅可试听1分钟.欲听完整版请前往酷狗音乐app开通VIP");
            }
        }
        return list;
    }*/
    /*public String keepMusicToDB(Music detail, byte[] bytes){
        String fileName = path + detail.getAudioName() + ".mp3";

        //文件存在则先删除，以更新MP3文件
        FileUtil.del(fileName);
        log.info("新增歌曲: {}", detail.getAudioName());

        musicService.keepMusic(detail);
        File file = new File(fileName);
        try {
            if (file.createNewFile()) {

                Files.write(file.toPath(), bytes);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileName;
    }*/
    private MusicRes getMusicDetail(JsonNode searchResOne){
        String albumID = searchResOne.get("AlbumID").asText();
        String hash = searchResOne.get("FileHash").asText();
        String urlForMusicDetail = "https://wwwapi.kugou.com/yy/index.php?r=play/getdata&callback=jQuery191033144701096575724_1660124702942&hash=" + hash+
                "&dfid=3eyKKr1tAQle0EQs9n1ItnQV&appid=1014&mid=d30a3efc49071a50132e4b338f93aa0a&platid=4&album_id=" + albumID +
                "&_=1660124702944";
        try{
            String detailRes = ReUtil.get("\\((.+)\\)", doGetStr(urlForMusicDetail), 1);
            MusicRes musicRes = objectMapper.readValue(detailRes, MusicRes.class);

            if (StrUtil.isBlankOrUndefined(musicRes.getMusicDetail().getPlayUrl())){
                musicRes.setShowTips("歌曲只能在酷狗客户端播放或无法试听");
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
