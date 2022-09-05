package com.zsck.bot.http.mihoyo.sign.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zsck.bot.enums.MsgType;
import com.zsck.bot.helper.MsgSenderHelper;
import com.zsck.bot.http.mihoyo.sign.GenShinSign;
import com.zsck.bot.http.mihoyo.sign.exception.GenShinNoSuchUIDException;
import com.zsck.bot.http.mihoyo.sign.pojo.GenshinInfo;
import com.zsck.bot.http.mihoyo.sign.service.GenshinService;
import com.zsck.bot.util.ContextUtil;
import kotlinx.coroutines.TimeoutCancellationException;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author QQ:825352674
 * @date 2022/9/2 - 19:12
 */
@Slf4j
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
    private final static String KEY_START = "==KEY_START==";
    private final static String GENSHIN_SIGN = "GENSHIN_SIGN:COOKIE";
    private final static String GENSHIN_SIGN_CHOOSE_UID = "GENSHIN_SIGN:COOKIE_CHOOSE_UID";

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
        MsgSenderHelper senderHelperPri = MsgSenderHelper.getInstance(qqNumber, sender, MsgType.PRIVATE);

        MessageContentBuilder builder = factory.getMessageContentBuilder();
        builder.at(qqNumber);

        String key = KEY_START + ":" + qqNumber;

        ContinuousSessionScopeContext scopeContext = (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        SessionCallback<String> callback = SessionCallback.builder(String.class).onResume( cookie ->{
            List<GenshinInfo> infoList = genShinSign.analyzeCookie(cookie);
            AtomicReference<GenshinInfo> info = new AtomicReference<>(null);
            if (infoList.size() == 1){
                //只有一个账号，直接保存
                saveGenshinInfo(infoList.get(0));

                senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]绑定成功,uid:" + infoList.get(0).getUid());
            }else {
                //发送待确认信息
                senderHelperPri.priMsg("请发送需要绑定的原神账号的uid");
                StringBuffer buffer = new StringBuffer("可选项\n");
                infoList.forEach( res -> buffer.append(res.getUid()).append(":").append(res.getNickName()).append("\n"));
                senderHelperPri.priMsg(buffer.toString());

                //再次开启一个会话，获得uid
                SessionCallback<String> uidCallback = SessionCallback.builder(String.class).onResume(uid -> {
                    for (GenshinInfo genshinInfo : infoList) {
                        if (genshinInfo.getUid().equals(uid)) {
                            info.set(genshinInfo);
                        }
                    }
                    if (info.get() == null) {
                        throw new GenShinNoSuchUIDException("cookie中未查询到指定uid，请重新发送需要绑定的uid");
                    }

                    info.get().setQqNumber(qqNumber);

                    saveGenshinInfo(info.get());

                    senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]绑定成功,uid:" + info.get().getUid());

                }).onError( exception -> {
                    if (exception instanceof TimeoutCancellationException){
                        senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]超时啦，若想继续绑定原神账号请再次发送 绑定原神账号 触发会话");
                        return;
                    }else if (exception instanceof GenShinNoSuchUIDException){
                        senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]" + exception.getMessage());
                    }
                }).build();
                scopeContext.waiting(GENSHIN_SIGN_CHOOSE_UID, key, 120000, uidCallback);
            }

        }).onError(exception -> {
            if (exception instanceof TimeoutCancellationException){
                senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]超时啦，若想继续绑定原神账号请再次发送 绑定原神账号 触发会话");
                return;
            }
            senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]cookie无效");
        }).build();

        scopeContext.waiting(GENSHIN_SIGN, key,300000 , callback);

        senderHelper.groupMsg("[CAT:at,code=" + qqNumber + "]请私发米游社登录cookie到本账号\n (注:若担心隐私泄露建议停止操作)");
    }

    private void saveGenshinInfo(GenshinInfo info){
        LambdaQueryWrapper<GenshinInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenshinInfo::getUid, info.getUid());
        GenshinInfo genshinInfo = genshinService.getOne(wrapper);
        if (genshinInfo == null) {
            genshinService.save(info);
        }else {
            LambdaUpdateWrapper<GenshinInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(GenshinInfo::getUid, info.getUid()).set(GenshinInfo::getCookie, info.getCookie());
            genshinService.update(updateWrapper);
        }

    }

    @OnPrivate
    @OnlySession(group = GENSHIN_SIGN)
    public void getCookie(PrivateMsg privateMsg, MsgSender sender, ListenerContext context) {

        final ContinuousSessionScopeContext session = (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);

        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(privateMsg, sender);

        final String qqNumber = privateMsg.getAccountInfo().getAccountCode();

        // 拼接出来这个人对应的唯一key
        String key = KEY_START + ":" + qqNumber;

        final String cookie = privateMsg.getText();


        session.push(GENSHIN_SIGN, key, cookie);

        senderHelper.priMsg("成功收到cookie，正在检查cookie有效性");
    }
    @Filter(value = "^[1-5]\\d{8}", matchType = MatchType.REGEX_MATCHES)
    @OnPrivate
    @OnlySession(group = GENSHIN_SIGN_CHOOSE_UID)
    public void getChooseUID(PrivateMsg privateMsg, MsgSender sender, ListenerContext context) {

        final ContinuousSessionScopeContext session = (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);

        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(privateMsg, sender);


        final String qqNumber = privateMsg.getAccountInfo().getAccountCode();

        // 拼接出来这个人对应的唯一key
        String key = KEY_START + ":" + qqNumber;

        final String uid = privateMsg.getText();

        session.push(GENSHIN_SIGN_CHOOSE_UID, key, uid);

        senderHelper.priMsg("选择成功，正在绑定原神账号");
    }

}
