package com.zsck.bot.http.mihoyo.sign.timing;

import com.zsck.bot.helper.MsgSenderHelper;
import com.zsck.bot.enums.MsgType;
import com.zsck.bot.http.mihoyo.sign.GenShinSign;
import com.zsck.bot.http.mihoyo.sign.pojo.GenshinInfo;
import com.zsck.bot.http.mihoyo.sign.service.GenshinService;
import com.zsck.bot.util.ContextUtil;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/9/1 - 21:46
 */
@DependsOn("contextUtil")
@EnableScheduling
@EnableAsync
@Component
public class SignTiming {
    @Autowired
    private GenShinSign genShinSign;
    @Autowired
    private GenshinService genshinService;
    private MiraiMessageContentBuilderFactory factory;
    @Value("${com.zsck.data.user-qq}")
    private String host;
    private Bot bot;

    public SignTiming(BotManager botManager) {
        bot = botManager.getDefaultBot();
    }
    @PostConstruct
    public void init(){
        factory = ContextUtil.getForwardBuilderFactory();
    }

    @Scheduled(cron = "00 00 10 * * ?")
    //@Async
    public void sign(){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(host, bot.getSender(), MsgType.PRIVATE);
        List<GenshinInfo> list = genshinService.list();

        MiraiMessageContentBuilder builder = factory.getMessageContentBuilder();
        builder.forwardMessage(fun ->{
            for (GenshinInfo info : list){
                fun.add(bot.getBotInfo(), genShinSign.doSign(info));
            }
        });
        bot.getSender().SENDER.sendGroupMsg( 811545265, builder.build());
        bot.getSender().SENDER.sendPrivateMsg( host, builder.build());


        //senderHelper.priMsg(builder.build());
    }
}
