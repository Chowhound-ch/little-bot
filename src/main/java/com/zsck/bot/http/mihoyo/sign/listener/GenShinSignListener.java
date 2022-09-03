package com.zsck.bot.http.mihoyo.sign.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zsck.bot.helper.MsgSenderHelper;
import com.zsck.bot.http.mihoyo.sign.GenShinSign;
import com.zsck.bot.http.mihoyo.sign.pojo.GenshinInfo;
import com.zsck.bot.http.mihoyo.sign.service.GenshinService;
import com.zsck.bot.util.ContextUtil;
import kotlinx.coroutines.TimeoutCancellationException;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.annotation.OnlySession;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.listener.ContinuousSessionScopeContext;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.SessionCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/9/2 - 19:12
 */
@DependsOn("contextUtil")
@Component
public class GenShinSignListener {
    @Autowired
    private GenShinSign genShinSign;
    @Autowired
    private GenshinService genshinService;
    @Autowired
    private MessageContentBuilderFactory factory;
    private final MiraiMessageContentBuilderFactory miraiFactory = ContextUtil.getForwardBuilderFactory();
    private final static String GENSHIN_SIGN = "GENSHIN_SIGN:COOKIE";

    @Filter(value = "ys签到", matchType = MatchType.EQUALS)
    @OnGroup
    public void signReady(GroupMsg groupMsg, MsgSender sender){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        String qqNumber = groupMsg.getAccountInfo().getAccountCode();


        LambdaQueryWrapper<GenshinInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenshinInfo::getQqNumber, qqNumber);
        List<GenshinInfo> list = genshinService.list(wrapper);
        if (list.isEmpty()){
            senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]你当前还没有绑定原神账号呢");
        } else {
            MiraiMessageContentBuilder forwardBuilder = miraiFactory.getMessageContentBuilder();
            forwardBuilder.forwardMessage( fun ->{
                list.forEach( info -> fun.add(groupMsg.getBotInfo(), genShinSign.doSign(info)));
            });
            senderHelper.groupMsg(forwardBuilder.build());
        }
    }

    @Filter(value = "绑定原神账号", matchType = MatchType.EQUALS)
    @OnGroup
    public void bindGenshin(GroupMsg groupMsg, MsgSender sender, ListenerContext context) {
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        String qqNumber = groupMsg.getAccountInfo().getAccountCode();
        MessageContentBuilder builder = factory.getMessageContentBuilder();
        builder.at(qqNumber);

        String key = GENSHIN_SIGN + ":" + qqNumber;
        ContinuousSessionScopeContext scopeContext = (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        SessionCallback<String> callback = SessionCallback.builder(String.class).onResume( cookie ->{
            GenshinInfo info = genShinSign.analyzeCookie(cookie);
            info.setQqNumber(qqNumber);

            //TODO: 待优化
            LambdaQueryWrapper<GenshinInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GenshinInfo::getUid, info.getUid());
            GenshinInfo genshinInfo = genshinService.getOne(wrapper);
            if (genshinInfo == null) {
                genshinService.save(info);
            }else {
                LambdaUpdateWrapper<GenshinInfo> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(GenshinInfo::getUid, info.getUid()).set(GenshinInfo::getCookie, cookie);
                genshinService.update(updateWrapper);
            }

            senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]绑定成功,uid:" + info.getUid());
        }).onError(exception -> {
            exception.printStackTrace();
            if (exception instanceof TimeoutCancellationException){
                senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]超时啦，若想继续绑定原神账号请再次发送 绑定原神账号 触发会话");
                return;
            }
            senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]cookie无效");
            //bindGenshin(groupMsg, sender, context);
        }).build();

        scopeContext.waiting(GENSHIN_SIGN, key,300000 , callback);

        senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]请私发米游社登录cookie到本账号 (注:若担心隐私泄露建议停止操作)");
    }

    @OnPrivate
    @OnlySession(group = GENSHIN_SIGN)
    public void getCookie(PrivateMsg privateMsg, MsgSender sender, ListenerContext context) {

        final ContinuousSessionScopeContext session = (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);

        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(privateMsg, sender);

        final String qqNumber = privateMsg.getAccountInfo().getAccountCode();

        // 拼接出来这个人对应的唯一key
        String key = GENSHIN_SIGN + ":" + qqNumber;

        final String cookie = privateMsg.getText();


        session.push(GENSHIN_SIGN, key, cookie);

        senderHelper.priMsg("成功收到cookie，正在检查cookie有效性");
    }

}
