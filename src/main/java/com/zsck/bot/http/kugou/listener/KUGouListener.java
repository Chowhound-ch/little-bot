package com.zsck.bot.http.kugou.listener;

import catcode.CatCodeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsck.bot.common.permit.annotation.BotPermits;
import com.zsck.bot.common.permit.enums.Permit;
import com.zsck.bot.common.permit.service.PermitDetailService;
import com.zsck.bot.helper.MsgSenderHelper;
import com.zsck.bot.http.kugou.KuGouMusic;
import com.zsck.bot.http.kugou.pojo.Music;
import com.zsck.bot.http.kugou.service.MusicService;
import com.zsck.bot.util.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.ListenGroup;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MessageGet;
import love.forte.simbot.api.message.results.FileResult;
import love.forte.simbot.api.sender.AdditionalApi;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.additional.MiraiAdditionalApis;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.ScopeContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author QQ:825352674
 * @date 2022/7/28 - 22:30
 */
@Slf4j
@DependsOn({"kuGouMusic","contextUtil"})
@ListenGroup("music")
@Controller
public class KUGouListener {
    private String path;
    private MiraiMessageContentBuilderFactory factory;
    private CatCodeUtil codeUtil;
    @Autowired
    private MusicService musicService;
    @Autowired
    private PermitDetailService permitDetailService;

    @PostConstruct
    private void init(){
        path = KuGouMusic.path;
        factory = ContextUtil.getForwardBuilderFactory();
        codeUtil = CatCodeUtil.getInstance();
    }


    @Filter(value = "^/点歌\\s*{{music}}" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getRandom(GroupMsg groupMsg, MsgSender sender, @FilterValue("music") String music){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        List<String> fileNames = getMusicByKeyword(music);
        if (fileNames == null) {//本地找不到，开始从酷狗搜索
            fileNames = KuGouMusic.getFileName(music);
        }
        //实际测试中linux环境下读取文件夹下文件的顺序并不确定，故发送语音之前先排序
        fileNames = sortMusicFiles(fileNames);
        MiraiMessageContentBuilder builder = factory.getMessageContentBuilder();
        List<String> catCodes = new ArrayList<>();

        List<String> filesForLambda = fileNames;//lambda中使用的变量必须未经更改,故此声明新变量
        builder.forwardMessage(forward->{
            for (String file : filesForLambda){
                if (file.endsWith(".mp3")) {
                    catCodes.add("[CAT:voice,file=" + path + file + "]");
                }else {
                    if (file.startsWith("url:http")){
                        forward.add(groupMsg.getBotInfo(), "[CAT:image,url=" + file.substring("url:".length()) + "]");
                    }else {
                        forward.add(groupMsg.getBotInfo(), file);
                    }
                }
            }
        });
        senderHelper.GROUP.sendMsg(builder.build());
        if (! catCodes.isEmpty()) {
            for (String voice : catCodes) {
                senderHelper.GROUP.sendMsg(voice);
            }
        }
    }
    @OnGroup
    public void setMP3File(MessageGet msgGet, MsgSender sender, ListenerContext context){
        ScopeContext scopeContext = context.getContext(ListenerContext.Scope.GLOBAL);
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(msgGet, sender);//获取(私聊|群聊)消息发送器
        String key = senderHelper.getGroup()+ ":" + senderHelper.getQqNumber() + ":添加歌曲";
        String audioName = ((String) scopeContext.get(key));
        if (audioName != null) {
            String id = codeUtil.getParam(msgGet.getMsg(), "id");
            if (id != null) {
                AdditionalApi<FileResult> fileRes = MiraiAdditionalApis.GETTER.getGroupFileById(senderHelper.getGroup(), id,  true);
                FileResult file = sender.GETTER.additionalExecute(fileRes);
                //创建临时文件，以获取mp3文件的具体时长来确认如何分割二进制流以发送qq语音
                File temp = new File(path + "temp.mp3");
                HttpUtil.downloadFile(file.getValue().getUrl(), temp);
                try {
                    int time = AudioFileIO.read(temp).getAudioHeader().getTrackLength();
                    byte[] bytes = Files.readAllBytes(temp.toPath());

                    Music music = setParam(audioName,DigestUtils.md5Hex(bytes), time );
                    KuGouMusic.keepMusicToDB(music, bytes);
                    log.info("自定义歌曲: {}.mp3", audioName);
                    senderHelper.GROUP.sendMsg("添加自定义歌曲:" + audioName);
                    scopeContext.remove(key);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    FileUtil.del(temp.toPath());//删除临时文件
                }
            }
        }
    }

    @BotPermits(Permit.MANAGER)//例如: /添加 love story - TaylorSwifter
    @Filter(value = "^/添加\\s*{{music,[^-]+}}\\s+-+\\s+{{author,[^-]+}}" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void setMP3Detail(GroupMsg groupMsg, MsgSender sender,
                             @FilterValue("music")String music,
                             @FilterValue("author")String author,
                             ListenerContext context){
        ScopeContext scopeContext = context.getContext(ListenerContext.Scope.GLOBAL);
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        String key =senderHelper.getGroup() + ":" + senderHelper.getQqNumber() + ":添加歌曲";
        senderHelper.GROUP.sendMsg("请发送MP3文件");
        scopeContext.set(key, author + " - " + music);
        Runnable runnable = ()->{//开设线程，设置会话持续时间, 默认60s
            try {
                Thread.sleep(60000L);
                if (scopeContext.remove(key) != null){
                    senderHelper.GROUP.sendMsg("会话超时，添加歌曲请再次发起会话");
                    log.info("会话失效: {}",key);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable.run();
    }

    public List<String> sortMusicFiles(List<String> fileNames){
        Collections.sort(fileNames, (s1, s2)->{
            if (s1.endsWith(".mp3") && s2.endsWith(".mp3")){
                Integer i1 = Integer.parseInt(s1.substring(s1.length() - 5).substring(0, 1));
                Integer i2 = Integer.parseInt(s2.substring(s2.length() - 5).substring(0, 1));
                if (i1 > i2){
                    return 1;
                }else if (i1 < i2){
                    return -1;
                }
            }
            return 0;
        });
        return fileNames;
    }
    private List<String> getMusicByKeyword(String key){
        Music music = getMusic(key);
        if (music != null){
            List<String> list = KuGouMusic.getReady(music, 0 );
            try {
                Stream<Path> pathStream = Files.list(new File(path + music.getAudioName() + "/").toPath());
                pathStream.forEach(p ->{
                    list.add(music.getAudioName() + "/" + p.getFileName());
                });//获取子文件夹内容
                return list;
            } catch (IOException e) {
                log.warn("数据库中有记录而不存在实际文件");
            }
        }
        return null;
    }
    private Music getMusic(String key){
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Music::getAudioName , key);
        return musicService.getOne(wrapper);
    }
    private Music setParam(String audioName, String md5, Integer time){
        Music music = new Music();
        String[] audioAndName = audioName.split(" - ");
        music.setAudioName(audioName);
        music.setAuthor(audioAndName[0]);
        music.setSongName(audioAndName[1]);
        music.setTime(time);
        music.setMd5(md5);
        return music;
    }
}
