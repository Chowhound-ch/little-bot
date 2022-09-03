package com.zsck.bot.http.mihoyo.sign.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsck.bot.helper.MsgSenderHelper;
import com.zsck.bot.http.mihoyo.sign.GenShinSign;
import com.zsck.bot.http.mihoyo.sign.pojo.GenshinInfo;
import com.zsck.bot.http.mihoyo.sign.service.GenshinService;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.ScopeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/9/2 - 19:12
 */
@Component
public class GenShinGroupListener {
    @Autowired
    private GenShinSign genShinSign;
    @Autowired
    private GenshinService genshinService;
    @Autowired
    private MessageContentBuilderFactory factory;

    @Filter(value = "ys签到", matchType = MatchType.EQUALS)
    @OnGroup
    public void signReady(GroupMsg groupMsg, MsgSender sender){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        String qqNumber = groupMsg.getAccountInfo().getAccountCode();
        MessageContentBuilder builder = factory.getMessageContentBuilder();
        builder.at(qqNumber);

        LambdaQueryWrapper<GenshinInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenshinInfo::getQqNumber, qqNumber);
        List<GenshinInfo> list = genshinService.list(wrapper);
        if (list.isEmpty()){
            senderHelper.groupMsg(builder.text("你当前还没有绑定原神账号呢").build());
        }

    }

    @Filter(value = "绑定原神账号", matchType = MatchType.EQUALS)
    @OnGroup
    public void bindGenshin(GroupMsg groupMsg, MsgSender sender, ListenerContext context) {
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        String qqNumber = groupMsg.getAccountInfo().getAccountCode();
        ScopeContext scopeContext = context.getContext(ListenerContext.Scope.GLOBAL);
  //      scopeContext.set();

        senderHelper.groupMsg("请私发米游社登录cookie到本账号(注:若担心隐私泄露建议停止操作)");
    }
}
