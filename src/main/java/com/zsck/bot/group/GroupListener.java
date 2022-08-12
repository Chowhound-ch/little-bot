package com.zsck.bot.group;

import com.zsck.bot.http.kugou.KuGouMusic;
import com.zsck.bot.util.ContextUtil;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.ListenGroup;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;


import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/7/28 - 22:30
 */
@DependsOn({"kuGouMusic","contextUtil"})
@ListenGroup("music")
@Controller
public class GroupListener {
    private String path;
    private MiraiMessageContentBuilderFactory factory;

    @PostConstruct
    private void init(){
        path = KuGouMusic.path;
        factory = ContextUtil.getMiraiFactory();
    }

    @Filter(value = "^点歌\\s*{{music}}" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getRandom(GroupMsg groupMsg, MsgSender sender, @FilterValue("music") String music){
        List<String> fileNames = KuGouMusic.getFileName(music);
        MiraiMessageContentBuilder builder = factory.getMessageContentBuilder();
        List<String> catCodes = new ArrayList<>();
        builder.forwardMessage(forward->{
            for (String file : fileNames){
                if (file.endsWith(".mp3")) {
                    catCodes.add("[CAT:voice,file=" + path + file + "]");
                }else {
                    if (file.startsWith("url:")){
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

}
