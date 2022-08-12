package com.zsck.bot.intercept;


import com.zsck.bot.annotation.BotPermits;
import com.zsck.bot.util.DataUtil;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.containers.AccountInfo;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ListenerInterceptContext;
import love.forte.simbot.listener.ListenerInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * @author QQ:825352674
 * @date 2022/7/15 - 0:05
 */
@DependsOn("dataUtil")
@Component
public class PermitsIntercept implements ListenerInterceptor {
    @Autowired
    private BotManager botManager;
    @NotNull
    @Override
    public InterceptionType intercept(@NotNull ListenerInterceptContext context) {
        BotPermits permits = context.getListenerFunction().getAnnotation(BotPermits.class);
        if (permits == null){
            return InterceptionType.PASS;
        }
        //群聊，放行
        if (context.getListenerFunction().getAnnotation(OnPrivate.class) != null){
            return InterceptionType.PASS;
        }
        Bot bot = botManager.getDefaultBot();
        AccountInfo accountInfo = context.getMsgGet().getAccountInfo();
        //权限足够,放行
        if (accountInfo.getAccountCode().equals(DataUtil.host)){
            return InterceptionType.PASS;
        }
        bot.getSender().SENDER.sendPrivateMsg(accountInfo.getAccountCode() , "权限不够，尝试联系管理员吧");
        return InterceptionType.INTERCEPT;
    }
}
