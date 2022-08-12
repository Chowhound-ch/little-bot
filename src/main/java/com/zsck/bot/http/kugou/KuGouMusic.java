package com.zsck.bot.http.kugou;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.ast.statement.SQLIfStatement;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/8/11 - 19:33
 */

@Component
public class KuGouMusic {

    public static String path;
    private static ObjectMapper mapper;
    private static Integer perFileSize;//建议1MB(默认)时长约 1:05

    @Resource
    public void setMapper(ObjectMapper mapper) {
        KuGouMusic.mapper = mapper;
    }
    @PostConstruct
    private void init(){
        path = ClassUtil.getClassPath()+"/temp/music/";
        perFileSize = 1024 * 1024;
    }

    public static List<String> getFileName(String keyWord){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse mp3File = null;
        try(CloseableHttpResponse searchResponse = httpClient.execute(new HttpGet(encoding( keyWord )))) {
            String searchRes= ReUtil.get("\\((.+)\\)", EntityUtils.toString(searchResponse.getEntity()), 1);
            JSONObject searchResJsonObj = new JSONObject(searchRes);
            //默认取搜索结果第一个，searchResJsonObj.optJSONObject("data").optJSONArray("lists") 为所有搜索结果
            JSONObject searchFirst = searchResJsonObj
                    .optJSONObject("data")
                    .optJSONArray("lists")
                    .optJSONObject(0);
            //准备发送下一个请求
            MusicDetail musicDetail = getMusicDetail(searchFirst, httpClient);
            List<String> list = new ArrayList<>();
            if (musicDetail.getShowTips() != null){
                return readyForSender(list, musicDetail);
            }
            mp3File = httpClient.execute(new HttpGet(musicDetail.getPlayBackupUrl()));
            byte[] bytes = EntityUtils.toByteArray(mp3File.getEntity());
            List<String> fileList = keep(bytes, musicDetail.getAudioName());
            return readyForSender(fileList, musicDetail);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            IoUtil.close(mp3File);
        }
        return null;
    }
    private static List<String> readyForSender(List<String> fileList, MusicDetail musicDetail){
        List<String> list = new ArrayList<>();
        list.add("歌曲:" + (StrUtil.isBlankOrUndefined(musicDetail.getSongName())? "无法获取" :musicDetail.getSongName()));
        list.add("歌手:" + musicDetail.getAuthorName());
        if (!StrUtil.isBlankOrUndefined(musicDetail.getShowTips())){
            list.add(musicDetail.getShowTips());
            return list;
        }else {
            list.add("url:" + musicDetail.getImg());
            if (musicDetail.getIsFreePart() == 1){
                list.add("tip: 歌曲为付费歌曲，仅可试听1分钟.欲听完整版请前往酷狗音乐app开通VIP");
            }
        }
        list.addAll(fileList);
        return list;
    }
    private static List<String> keep(byte[] bytes, String audioName){
        isExist(audioName);
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        List<String> fileList = new ArrayList<>();
        for (int i = 0; true ; i++) {
            if (i * perFileSize >bytes.length){
                break;
            }
            String fileName = audioName + "/" + audioName + "-"+i+ ".mp3";
            File file = new File( path+ fileName );
            try {
                byte[] bytesPerFile;
                if ((i+1) * perFileSize <= bytes.length){
                    bytesPerFile = new byte[perFileSize];
                    wrap.get(bytesPerFile, 0, perFileSize);
                }else {
                    bytesPerFile = new byte[bytes.length - i*perFileSize];
                    wrap.get(bytesPerFile, 0, bytes.length - i*perFileSize);
                }
                Files.write(file.toPath(), bytesPerFile);
                fileList.add( fileName );
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileList;
    }
    private static List<String> isExist(String musicName){
        File file = new File(   path+ musicName + "/");
        List<String> list = null;
        if (file.exists()){
            File[] files = file.listFiles();
            list = new ArrayList<>();
            for (File fileTemp : files){
                list.add( fileTemp.getName());
            }
        }else {
            file.mkdirs();
        }
        return list;
    }
    private static MusicDetail getMusicDetail(JSONObject searchResOne, CloseableHttpClient httpClient){
        String albumID = searchResOne.optString("AlbumID");
        String hash = searchResOne.optString("FileHash");
        String urlForMusicDetail = "https://wwwapi.kugou.com/yy/index.php?r=play/getdata&callback=jQuery191033144701096575724_1660124702942&hash=" + hash+
                "&dfid=3eyKKr1tAQle0EQs9n1ItnQV&appid=1014&mid=d30a3efc49071a50132e4b338f93aa0a&platid=4&album_id=" + albumID +
                "&_=1660124702944";
        try( CloseableHttpResponse musicDetailResponse = httpClient.execute(new HttpGet(urlForMusicDetail)) ) {
            String musicRes = ReUtil.get("\\((.+)\\)", EntityUtils.toString(musicDetailResponse.getEntity()), 1);
            JSONObject res = new JSONObject(musicRes);
            if (!StrUtil.isBlankOrUndefined(res.optString("show_tips"))){
                MusicDetail musicDetail = new MusicDetail("歌曲只能在酷狗客户端播放或无法试听");
                musicDetail.setSongName(res.optString("songname"));
                musicDetail.setAuthorName(res.optString("author_name"));
                return musicDetail;
            }else {
                MusicDetail musicDetail = mapper.readValue(res.optJSONObject("data").toString(), MusicDetail.class);
                if (StrUtil.isBlankOrUndefined(musicDetail.getPlayBackupUrl())){
                    musicDetail.setShowTips("歌曲只能在酷狗客户端播放或无法试听");
                }
                return musicDetail;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String encoding(String keyWord){
        String signature = MD5Encoding.MD5(keyWord);
        keyWord = URLEncodeUtil.encode(keyWord);
        String url = "https://complexsearch.kugou.com/v2/search/song?callback=callback123&keyword=" + keyWord + "&page=1&pagesize=30&bitrate=0&isfuzzy=0&tag=em&inputtype=0&platform=WebFilter&userid=-1&clientver=2000&iscorrection=1&privilege_filter=0&srcappid=2919&clienttime=1600305065609&mid=1600305065609&uuid=1600305065609&dfid=-&signature=" + signature;
        return url;
    }

}
