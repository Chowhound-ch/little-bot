package com.zsck.bot.group;

import catcode.CatCodeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.math.BitStatusUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ByteUtil;
import cn.hutool.db.meta.MetaUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsck.bot.annotation.BotPermits;
import com.zsck.bot.enums.Permit;
import com.zsck.bot.http.kugou.KuGouMusic;
import com.zsck.bot.http.kugou.pojo.Music;
import com.zsck.bot.http.kugou.service.MusicService;
import com.zsck.bot.util.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.results.FileResult;
import love.forte.simbot.api.sender.AdditionalApi;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.additional.MiraiAdditionalApis;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.listener.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;


import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
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
public class GroupListener {
    private String path;
    private MiraiMessageContentBuilderFactory factory;
    private CatCodeUtil codeUtil;
    @Autowired
    private MusicService musicService;

    @PostConstruct
    private void init(){
        path = KuGouMusic.path;
        factory = ContextUtil.getMiraiFactory();
        codeUtil = CatCodeUtil.getInstance();
    }

    @Filter(value = "^/点歌\\s*{{music}}" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getRandom(GroupMsg groupMsg, MsgSender sender, @FilterValue("music") String music){
        List<String> fileNames = getMusicByKeyword(music);
        if (fileNames == null) {//本地找不到，开始从酷狗搜索
            fileNames = KuGouMusic.getFileName(music);
        }
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
        String group = groupMsg.getGroupInfo().getGroupCode();
        sender.SENDER.sendGroupMsg(group, builder.build());
        if (! catCodes.isEmpty()) {
            for (String voice : catCodes) {
                sender.SENDER.sendGroupMsg(group, voice);
            }
        }
    }
    private List<String> getMusicByKeyword(String key){
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Music::getAudioName , key);
        Music music = musicService.getOne(wrapper);
        if (music != null){
            List<String> list = KuGouMusic.getReady(music, 0 );
            try {
                Stream<Path> pathStream = Files.list(new File(path + music.getAudioName() + "/").toPath());
                pathStream.forEach(p ->{
                    list.add(music.getAudioName() + "/" + p.getFileName());
                });//获取子文件夹内容
                return list;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    //@OnlySession(group = "添加歌曲")
    @OnGroup
    public void setMP3File(GroupMsg groupMsg, MsgSender sender,ListenerContext context){
        ScopeContext scopeContext = context.getContext(ListenerContext.Scope.GLOBAL);
        String group = groupMsg.getGroupInfo().getGroupCode();
        String key = group + ":" +groupMsg.getAccountInfo().getAccountCode() + ":添加歌曲";
        String audioName = ((String) scopeContext.get(key));
        if (audioName != null) {
            String id = codeUtil.getParam(groupMsg.getMsg(), "id");
            if (id != null) {
                AdditionalApi<FileResult> fileRes = MiraiAdditionalApis.GETTER.getGroupFileById(group, id,  true);
                FileResult file = sender.GETTER.additionalExecute(fileRes);
                File temp = new File(path + "temp.mp3");
                HttpUtil.downloadFile(file.getValue().getUrl(), temp);
                try {
                    int time = AudioFileIO.read(temp).getAudioHeader().getTrackLength();
                    Music music = new Music();
                    String[] audioAndName = audioName.split(" - ");
                    music.setAudioName(audioName);
                    music.setAuthor(audioAndName[0]);
                    music.setSongName(audioAndName[1]);
                    music.setTime(time);
                    byte[] bytes = Files.readAllBytes(temp.toPath());
                    music.setMd5(DigestUtils.md5Hex(bytes));
                    KuGouMusic.keepMusicToDB(music);
                    KuGouMusic.keep(bytes, audioName , time);
                    log.info("自定义歌曲: {}.mp3", audioName);
                    sender.SENDER.sendGroupMsg(group, "添加自定义歌曲:" + audioName);
                    scopeContext.remove(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //@BotPermits(Permit.MANAGER)
    @Filter(value = "^/添加\\s*{{music,\\S+}}\\s+{{author,\\S+}}" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void setMP3Detail(GroupMsg groupMsg, MsgSender sender,
                         @FilterValue("music")String music,
                         @FilterValue("author")String author,
                         ListenerContext context){
        ScopeContext scopeContext = context.getContext(ListenerContext.Scope.GLOBAL);
        String group = groupMsg.getGroupInfo().getGroupCode();
        String key = group + ":" +groupMsg.getAccountInfo().getAccountCode() + ":添加歌曲";
        sender.SENDER.sendGroupMsg(group , "请发送MP3文件");
        scopeContext.set(key, author + " - " + music);
        Runnable runnable = ()->{
            try {
                Thread.sleep(60000L);
                if (scopeContext.remove(key) != null){
                    sender.SENDER.sendGroupMsg(group , "会话超时，添加歌曲请再次发起会话");
                    log.info("会话失效: {}",key);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        runnable.run();
    }

}
