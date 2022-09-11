package com.zsck.bot.http.kugou.listener;

import catcode.CatCodeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.zsck.bot.common.permit.annotation.BotPermits;
import com.zsck.bot.common.permit.enums.Permit;
import com.zsck.bot.common.permit.service.PermitDetailService;
import com.zsck.bot.helper.MsgSenderHelper;
import com.zsck.bot.http.kugou.KuGouMusic;
import com.zsck.bot.http.kugou.pojo.Music;
import com.zsck.bot.http.kugou.service.MusicService;
import com.zsck.bot.util.ContextUtil;
import kotlinx.coroutines.TimeoutCancellationException;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MessageGet;
import love.forte.simbot.api.message.results.FileResult;
import love.forte.simbot.api.sender.AdditionalApi;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.additional.MiraiAdditionalApis;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.listener.ContinuousSessionScopeContext;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.SessionCallback;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Objects;

/**
 * @author QQ:825352674
 * @date 2022/7/28 - 22:30
 */
@Slf4j
@DependsOn({"kuGouMusic","contextUtil"})
@ListenGroup("music")
@Controller
public class KuGouListener {
    private String path;
    private MiraiMessageContentBuilderFactory factory;
    private CatCodeUtil codeUtil;
    @Autowired
    private MusicService musicService;
    @Autowired
    private PermitDetailService permitDetailService;
    @Autowired
    private KuGouMusic kuGouMusic;

    @Value("${com.zsck.data.domain}")
    private String domain;

    private final static String KEY_START = "==KEY_START==";
    private final static String KUGOU_MUSIC = "GENSHIN_SIGN:COOKIE";


    @PostConstruct
    private void init(){
        path = kuGouMusic.path;
        factory = ContextUtil.getForwardBuilderFactory();
        codeUtil = CatCodeUtil.getInstance();
    }

   /* @ExcludeGroupState
    @OnGroup
    public void testMusic(GroupMsg groupMsg, MsgSender sender){
       // System.out.println(groupMsg.getMsg());
        System.out.println(groupMsg.getMsg());
        Music byId = musicService.getById(20);
        sender.SENDER.sendGroupMsg(groupMsg.getGroupInfo().getGroupCode(), getKuGouMsg(byId));
    }*/


    @Filter(value = "^/点歌\\s*{{keyword}}" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getRandom(GroupMsg groupMsg, MsgSender sender, @FilterValue("keyword") String keyword){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        Music music = musicService.likeMusic(keyword);

        if (music == null) {//本地找不到，开始从酷狗搜索
            log.info("关键词: {}本地找不到，开始从酷狗搜索", keyword);
            music = kuGouMusic.getFileName(keyword);
        }
        if (music.getImgUrl() == null){
            music.setImgUrl(groupMsg.getAccountInfo().getAccountAvatar());
        }

        senderHelper.GROUP.sendMsg( getKuGouMsg(music) );
    }

    private String getKuGouMsg(Music music) {
        return "[CAT:other,code=&#91;mirai:origin:MUSIC_SHARE&#93;][CAT:music,kind=kugou," +
                "musicUrl=" + music.getPlayBackupUrl() +//mp3文件url
                ",title=" + music.getSongName() +
                ",jumpUrl=https://www.kugou.com/song/#hash&#61;&amp;album_id&#61;48534841," +
                "pictureUrl=" + music.getImgUrl() + ",summary=" + music.getAudioName() + ",brief=&#91;分享&#93;" +
                music.getAudioName() + "]";

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
            //创建临时文件，以获取mp3文件的具体时长来确认如何分割二进制流以发送qq语音
            File file = new File(kuGouMusic.path + fileRes.getValue().getName());
            HttpUtil.downloadFile(Objects.requireNonNull(fileRes.getValue().getUrl()), file);

            log.info("自定义歌曲: {}.mp3", file.getName());
            senderHelper.GROUP.sendMsg("添加自定义歌曲:" + file.getName());

            scopeContext.push(KUGOU_MUSIC, key, file);

        }
    }

    @BotPermits(Permit.MANAGER)//例如: /添加 love story - TaylorSwifter
    @Filter(value = "^/添加\\s*{{music,[^-]+}}\\s*-+\\s*{{author,[^-]+}}\\s*" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void setMP3Detail(GroupMsg groupMsg, MsgSender sender,
                             @FilterValue("music")String music,
                             @FilterValue("author")String author,
                             ListenerContext context){
        ContinuousSessionScopeContext scopeContext = (ContinuousSessionScopeContext)context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);

        String key = KEY_START + senderHelper.getQqNumber() + KUGOU_MUSIC;
        String audioName = author + " - " + music;

        senderHelper.GROUP.sendMsg("请发送MP3文件");
        SessionCallback<File> callback = SessionCallback.builder(File.class).onResume( file ->{
            File desFile = new File(kuGouMusic.path + audioName + ".mp3");
            if (file.renameTo(desFile)) {
                Music musicRes = new Music(audioName, music, author, DigestUtils.md5Hex(FileUtil.readBytes(desFile)), domain + "/static/" + desFile.getName());
                musicService.keepMusic(musicRes);
                senderHelper.GROUP.sendMsg("成功添加歌曲" + music);
            }else {
                senderHelper.GROUP.sendMsg("添加歌曲" + music + "失败");
                file.deleteOnExit();
            }
        } ).onError(exception -> {
            if (exception instanceof TimeoutCancellationException){
                senderHelper.GROUP.sendMsg("操作超时");
            }else {
                senderHelper.GROUP.sendMsg("未知错误");
                exception.printStackTrace();
            }
        }).build();

        scopeContext.waiting(KUGOU_MUSIC, key, 120000, callback);
    }

}
