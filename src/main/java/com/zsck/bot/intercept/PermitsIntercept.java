package com.zsck.bot.intercept;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsck.bot.annotation.BotPermits;
import com.zsck.bot.common.pojo.PermitDetail;
import com.zsck.bot.common.service.PermitDetailService;

import com.zsck.bot.enums.MsgType;
import com.zsck.bot.util.MsgSenderHelper;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ListenerInterceptContext;
import love.forte.simbot.listener.ListenerInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author QQ:825352674
 * @date 2022/7/15 - 0:05
 */
@DependsOn("dataUtil")
@Component
public class PermitsIntercept implements ListenerInterceptor {
    @Autowired
    private BotManager botManager;
    @Autowired
    private PermitDetailService permitDetailService;
    private Bot bot;
    @PostConstruct
    private void init(){
        bot = botManager.getDefaultBot();
    }
    @NotNull
    @Override
    public InterceptionType intercept(@NotNull ListenerInterceptContext context) {
        BotPermits permits = context.getListenerFunction().getAnnotation(BotPermits.class);
        if (permits == null){
            return InterceptionType.PASS;
        }
        MsgGet msgGet = context.getMsgGet();
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(msgGet, bot.getSender());
        //查询权限
        PermitDetail detail = getPermitDetail(msgGet.getAccountInfo().getAccountCode());
        //权限不足
        if (detail == null || detail.getPermit() < permits.value().getValue()){
            senderHelper.senderMsg("权限不够，尝试联系管理员吧");
            return InterceptionType.INTERCEPT;
        }
        return InterceptionType.PASS;
    }



    private PermitDetail getPermitDetail(String qqNumber){
        LambdaQueryWrapper<PermitDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermitDetail::getQqNumber, qqNumber);
        return permitDetailService.getOne(wrapper);
    }
}
