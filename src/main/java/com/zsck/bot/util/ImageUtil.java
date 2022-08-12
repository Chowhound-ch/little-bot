package com.zsck.bot.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.zsck.bot.enums.FileName;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author QQ:825352674
 * @date 2022/7/29 - 12:45
 */
@Component
public class ImageUtil {
    private static Bot bot;
    private static Random random = new Random(Instant.now().getNano());
    private static MessageContentBuilderFactory messageContentBuilderFactory;

    public ImageUtil(@NotNull BotManager botManager , MessageContentBuilderFactory factory) {
        bot = botManager.getDefaultBot();
        this.messageContentBuilderFactory = factory;
    }

    public static void checkAndGetImagesMsg(MiraiMessageContentBuilder builder , GroupMsg groupMsg, FileName fileName){
        String msg = groupMsg.getText();
        String group = groupMsg.getGroupInfo().getGroupCode();
        Integer num = getNum(msg);
        if (num > 50) {
            builder.text("请控制单次请求数量在1-50范围内");
            return ;
        }else if(num >= 10) {
            bot.getSender().SENDER.sendGroupMsg(group , "单次请求的图片较多，请耐心等待");
        }
        bot.getSender().SENDER.sendGroupMsg(group , getImages(builder , num , fileName).build());
    }

    public static MiraiMessageContentBuilder getImages(MiraiMessageContentBuilder builder , Integer num , FileName fileName){
        File file = new File(DataUtil.filePath + fileName);
        File[] files = file.listFiles();
        Set<Integer> des = new HashSet<>();
        while (des.size() < num){
            des.add(random.nextInt(files.length));
        }
        builder.forwardMessage( (fun)->{
            des.forEach( index ->{
                MessageContentBuilder contentBuilder = messageContentBuilderFactory.getMessageContentBuilder();
                fun.add(bot.getBotInfo() , contentBuilder.image(files[index].getPath()).build());
            });
            fun.add(bot.getBotInfo() , "图片已过期的话点击查看原图即可");
        });
        return builder;
    }
    public static Integer getNum(String text){

        String num = ReUtil.get("(\\d*)", text, 0);
        Integer n;
        if (StrUtil.isBlankOrUndefined(num)){
            n = 1;
        }else {
            n = Integer.parseInt(num);
        }
        return n;
    }
}
