package com.zsck.bot.group;


import com.zsck.bot.enums.FileName;
import com.zsck.bot.util.ContextUtil;
import com.zsck.bot.util.ImageUtil;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.ListenGroup;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Random;

/**
 * @author QQ:825352674
 * @date 2022/7/28 - 22:45
 */
@DependsOn("contextUtil")
@ListenGroup("image")
@Controller
public class GroupPhoto {
    private MiraiMessageContentBuilderFactory factory;
    private Random random;


    @PostConstruct
    private void Init(){
        factory = ContextUtil.getMiraiFactory();
        random = new Random(Instant.now().getNano());
    }

    @Filter(value = "^来点好康的(\\d*)$" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getRandom(GroupMsg groupMsg){
        ImageUtil.checkAndGetImagesMsg(factory.getMessageContentBuilder() ,groupMsg ,random.nextBoolean() ? FileName.RANDOM : FileName.RAN);
    }
    @Filter(value = "^来点兽耳(\\d*)$" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getCat(GroupMsg groupMsg){
        ImageUtil.checkAndGetImagesMsg(factory.getMessageContentBuilder() ,groupMsg, FileName.ANI);
    }
    @Filter(value = "^来点白毛(\\d*)$" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getYin(GroupMsg groupMsg){
        ImageUtil.checkAndGetImagesMsg(factory.getMessageContentBuilder() ,groupMsg , FileName.YIN);
    }
    @Filter(value = "^来点横屏的(\\d*)$" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getPC(GroupMsg groupMsg){
        ImageUtil.checkAndGetImagesMsg(factory.getMessageContentBuilder() ,groupMsg , FileName.PC);
    }
    @Filter(value = "^来点壁纸(\\d*)$" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getWal(GroupMsg groupMsg){
        ImageUtil.checkAndGetImagesMsg(factory.getMessageContentBuilder() ,groupMsg , random.nextBoolean() ? FileName.WAL : FileName.PAPER);
    }
    @Filter(value = "^随便来点(\\d*)$" , matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void getOthers(GroupMsg groupMsg){
        ImageUtil.checkAndGetImagesMsg(factory.getMessageContentBuilder() ,groupMsg , random.nextBoolean() ? FileName.SE : FileName.SKY);
    }

}
