package com.zsck.bot.http.kugou.listener;

import catcode.CatCodeUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.zsck.bot.common.permit.annotation.BotPermits;
import com.zsck.bot.common.permit.enums.Permit;
import com.zsck.bot.helper.MsgSenderHelper;
import com.zsck.bot.http.kugou.HttpMusicSender;
import com.zsck.bot.http.kugou.KuGouMusic;
import com.zsck.bot.http.kugou.pojo.Music;
import com.zsck.bot.http.kugou.service.MusicService;
import kotlinx.coroutines.TimeoutCancellationException;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MessageGet;
import love.forte.simbot.api.message.results.FileInfo;
import love.forte.simbot.api.message.results.FileResult;
import love.forte.simbot.api.sender.AdditionalApi;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.additional.MiraiAdditionalApis;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.listener.ContinuousSessionScopeContext;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.SessionCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author QQ:825352674
 * @date 2022/7/28 - 22:30
 */
@Slf4j
@DependsOn({"kuGouMusic","contextUtil"})
@ListenGroup("music")
@Controller
public class KuGouListener {
    private final CatCodeUtil codeUtil = CatCodeUtil.getInstance();
    @Autowired
    private MusicService musicService;
    @Autowired
    private KuGouMusic kuGouMusic;
    @Autowired
    private HttpMusicSender musicSender;
    @Autowired
    private MessageContentBuilderFactory factory;


    private final static String KEY_START = "==KEY_START==";
    private final static String KUGOU_MUSIC = "KUGOU_MUSIC:UPLOAD_FILE";
    private final static String KUGOU_CHOOSE_MUSIC = "KUGOU_MUSIC:KUGOU_CHOOSE_MUSIC";


    @Filter(value = "^/点歌\\s*({{param,(-d|D){0,1}}})\\s*{{keyword}}" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getRandom(GroupMsg groupMsg, MsgSender sender,
                          ListenerContext context,
                          @FilterValue("param") String param,
                          @FilterValue("keyword") String keyword){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        AtomicReference<Music> desMusic = new AtomicReference<>();
        List<Music> localMusic = musicService.likeMusic(keyword);
        if (StrUtil.isBlankOrUndefined(param)){
            if (!localMusic.isEmpty()) {
                desMusic.set(localMusic.get(0));
            }

            if (desMusic.get() == null) {//本地找不到，开始从酷狗搜索
                log.info("关键词: {}本地找不到，开始从酷狗搜索", keyword);
                desMusic.set(kuGouMusic.getOneMusicForUrl(keyword));
            }

            if (desMusic.get().getImgUrl() == null) {
                desMusic.get().setImgUrl(groupMsg.getAccountInfo().getAccountAvatar());
            }

            senderHelper.GROUP.sendMsg(getKuGouMsg(desMusic.get()));
        }else {
            List<Music> netMusic = kuGouMusic.getFileNames(keyword, 5);
            MessageContentBuilder builder = factory.getMessageContentBuilder();
            builder.text("关键词:" + keyword + ",搜索结果:\n酷狗搜索:");
            int i = 0;
            for (Music music : netMusic){
                i++;
                builder.text("\n" + i + ". " + music.getAudioName());
                music.setId(i);
            }

            builder.text("\n本地搜索:");
            if (localMusic.isEmpty()){
                builder.text("\n本地暂无记录");
            }else {

                for (Music music : localMusic) {
                    i++;
                    if (i >= 8){
                        break;
                    }
                    builder.text("\n" + i + ". " + music.getAudioName());
                    music.setId(i);
                }
            }

            senderHelper.GROUP.sendMsg(builder.build());
            ContinuousSessionScopeContext scopeContext = (ContinuousSessionScopeContext)context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);

            SessionCallback<Integer> callback = SessionCallback.builder(Integer.class).onResume(id ->{

                netMusic.addAll(localMusic);
                for (Music music : netMusic){
                    if (music.getId().equals(id)){
                        desMusic.set(music);
                    }
                }

                if (desMusic.get() != null) {
                    if (desMusic.get().getUrl() == null){
                        desMusic.set( kuGouMusic.getMusicUrlByAlbumIDAndHash( desMusic.get() ) );
                    }

                    if (desMusic.get().getImgUrl() == null) {
                        desMusic.get().setImgUrl(groupMsg.getAccountInfo().getAccountAvatar());
                    }

                    senderHelper.GROUP.sendMsg(getKuGouMsg(desMusic.get()));
                }else {
                    senderHelper.GROUP.sendMsg("id错误");
                }

            } ).onError(exception -> {
                if ( !(exception instanceof TimeoutCancellationException)){
                    senderHelper.GROUP.sendMsg("未知错误");
                    exception.printStackTrace();
                }
            }).build();

            String key = KEY_START + senderHelper.getQqNumber() + KUGOU_CHOOSE_MUSIC;

            scopeContext.waiting(KUGOU_CHOOSE_MUSIC, key, 60000, callback);
        }
    }



    private String getKuGouMsg(Music music) {
        return "[CAT:other,code=&#91;mirai:origin:MUSIC_SHARE&#93;][CAT:music,kind=kugou," +
                "musicUrl=" + music.getUrl() +//mp3文件url
                ",title=" + music.getTitle() +
                ",jumpUrl=https://www.kugou.com/song/#hash&#61;&amp;album_id&#61;48534841," +
                "pictureUrl=" + music.getImgUrl() + ",summary=" + music.getAudioName() + ",brief=&#91;分享&#93;" +
                music.getAudioName() + "]";

    }

    @Filter(value = "[1-8]", matchType = MatchType.REGEX_MATCHES)
    @OnlySession(group = KUGOU_CHOOSE_MUSIC)
    @OnGroup
    public void getMusicId(GroupMsg msgGet, ListenerContext context){
        ContinuousSessionScopeContext scopeContext = (ContinuousSessionScopeContext)context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        String key = KEY_START + msgGet.getAccountInfo().getAccountCode() + KUGOU_CHOOSE_MUSIC;

        scopeContext.push(KUGOU_CHOOSE_MUSIC, key, Integer.parseInt(msgGet.getText()));

    }
    @OnlySession(group = KUGOU_MUSIC)
    @Filters(customFilter = "catFilter")
    @OnGroup
    public void setMP3File(MessageGet msgGet, MsgSender sender, ListenerContext context){
        ContinuousSessionScopeContext scopeContext = (ContinuousSessionScopeContext)context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(msgGet, sender);//获取(私聊|群聊)消息发送器
        String key = KEY_START + senderHelper.getQqNumber() + KUGOU_MUSIC;

        String id = codeUtil.getParam(msgGet.getMsg(), "id");
        if (id != null) {
            AdditionalApi<FileResult> fileApi = MiraiAdditionalApis.GETTER.getGroupFileById(senderHelper.getGroup(), id,  true);

            FileResult fileRes = sender.GETTER.additionalExecute(fileApi);
            //创建临时文件

            log.info("自定义歌曲: {}", fileRes.getValue().getName());
            senderHelper.GROUP.sendMsg("添加自定义歌曲:" + fileRes.getValue().getName());

            scopeContext.push(KUGOU_MUSIC, key, fileRes.getValue());

        }
    }

    @BotPermits(Permit.MANAGER)//例如: /添加 love story - TaylorSwifter
    @Filter(value = "/添加歌曲" , matchType = MatchType.EQUALS)
    @OnGroup
    public void setMP3Detail(GroupMsg groupMsg, MsgSender sender, ListenerContext context){
        ContinuousSessionScopeContext scopeContext = (ContinuousSessionScopeContext)context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);

        String key = KEY_START + senderHelper.getQqNumber() + KUGOU_MUSIC;

        senderHelper.GROUP.sendMsg("请发送MP3或flac文件或对应的zip压缩包");
        SessionCallback<FileInfo> callback = SessionCallback.builder(FileInfo.class).onResume(file ->{
            MessageContentBuilder builder = factory.getMessageContentBuilder();

            //将添加的mp3文件或mp3文件的zip压缩包发送至文件管理项目
            JsonNode jsonNode = musicSender.sendMusicDetail(file.getUrl(), file.getName());
            jsonNode.forEach( node-> builder.text(node.asText() + "\n"));

            senderHelper.GROUP.sendMsg(builder.build());

        } ).onError(exception -> {
            if ( !(exception instanceof TimeoutCancellationException)){
                senderHelper.GROUP.sendMsg("未知错误");
                exception.printStackTrace();
            }
        }).build();

        scopeContext.waiting(KUGOU_MUSIC, key, 300000, callback);
    }

}
