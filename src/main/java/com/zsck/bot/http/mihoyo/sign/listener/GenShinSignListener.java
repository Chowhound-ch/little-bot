package com.zsck.bot.http.mihoyo.sign.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
            senderHelper.GROUP.sendMsg("[CAT:at,code=" + qqNumber + "]你当前还没有绑定原神账号呢");
        } else {
            MiraiMessageContentBuilder forwardBuilder = miraiFactory.getMessageContentBuilder();
            forwardBuilder.forwardMessage( fun ->{
                list.forEach( info -> fun.add(groupMsg.getBotInfo(), genShinSign.doSign(info)));
            });
            senderHelper.GROUP.sendMsg(forwardBuilder.build());
        }
    }

    @Filter(value = "解绑原神账号", matchType = MatchType.EQUALS)
    @OnGroup
    public void unbindGenshin(GroupMsg groupMsg, MsgSender sender, ListenerContext context) {
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);

        MessageContentBuilder builder = factory.getMessageContentBuilder();
        builder.at(senderHelper.getQqNumber());
        String key = KEY_START + ":" + senderHelper.getQqNumber();

        senderHelper.GROUP.sendMsg("请在私聊中完成后续操作");


        LambdaQueryWrapper<GenshinInfo> listWrapper = new LambdaQueryWrapper<>();
        listWrapper.eq(GenshinInfo::getQqNumber, senderHelper.getQqNumber());
        List<GenshinInfo> list = genshinService.list(listWrapper);
        if (!list.isEmpty()) {
            StringBuffer infoDetail = new StringBuffer("当前已绑定的账号如下:\n");
            list.forEach(info -> infoDetail.append(info.getUid()).append(" : ").append(info.getNickName()).append("\n"));
            infoDetail.append("请选择要删除的账号的uid");
            senderHelper.PRIVATE.sendMsg(infoDetail.toString());

            ContinuousSessionScopeContext scopeContext = (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);

            context.getContext(ListenerContext.Scope.GLOBAL);
            SessionCallback<String> callback = SessionCallback.builder(String.class).onResume(uid -> {
                AtomicReference<GenshinInfo> des = new AtomicReference<>();
                list.forEach(info -> {
                    if (info.getUid().equals(uid)) {
                        des.set(info);
                    }
                });

                LambdaQueryWrapper<GenshinInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(GenshinInfo::getUid, uid);

                if (des.get() != null) {
                    genshinService.remove(wrapper);
                    StringBuffer buffer = new StringBuffer("成功解绑原神账号:");
                    buffer.append(uid).append(" : ").append(des.get().getNickName());
                    senderHelper.PRIVATE.sendMsg(buffer.toString());
                } else {
                    senderHelper.PRIVATE.sendMsg("无效的uid");
                }
            }).onError(exception -> {
                if (exception instanceof TimeoutCancellationException){
                    senderHelper.PRIVATE.sendMsg("超时啦，如若继续请再次尝试");
                }else {
                    senderHelper.PRIVATE.sendMsg("未知错误，已终止操作");
                }
            }).build();
            scopeContext.waiting(GENSHIN_SIGN_CHOOSE_UID, key, 120000, callback);
        }else {
            senderHelper.GROUP.sendMsg("当前账号为绑定原神账号");
        }
    }


    @Filter(value = "绑定原神账号", matchType = MatchType.EQUALS)
    @OnGroup
    public void bindGenshin(GroupMsg groupMsg, MsgSender sender, ListenerContext context) {
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);

        MessageContentBuilder builder = factory.getMessageContentBuilder();
        builder.at(senderHelper.getQqNumber());

        String key = KEY_START + ":" + senderHelper.getQqNumber();

        ContinuousSessionScopeContext scopeContext = (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        SessionCallback<String> callback = SessionCallback.builder(String.class).onResume( cookie ->{
            List<GenshinInfo> infoList = genShinSign.analyzeCookie(cookie);
            AtomicReference<GenshinInfo> info = new AtomicReference<>(null);
            if (infoList.size() == 1){
                //只有一个账号，直接保存
                saveGenshinInfo(infoList.get(0));

                senderHelper.GROUP.sendMsg("[CAT:at,code=" + senderHelper.getQqNumber() + "]绑定成功,uid:" + infoList.get(0).getUid());
            }else {
                //发送待确认信息
                senderHelper.PRIVATE.sendMsg("请发送需要绑定的原神账号的uid");
                StringBuffer buffer = new StringBuffer("可选项\n");
                infoList.forEach( res -> buffer.append(res.getUid()).append(":").append(res.getNickName()).append("\n"));
                senderHelper.PRIVATE.sendMsg(buffer.toString());

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

                    info.get().setQqNumber(senderHelper.getQqNumber());

                    saveGenshinInfo(info.get());

                    senderHelper.GROUP.sendMsg("[CAT:at,code=" + senderHelper.getQqNumber() + "]绑定成功,uid:" + info.get().getUid());

                }).onError( exception -> {
                    if (exception instanceof TimeoutCancellationException){
                        senderHelper.GROUP.sendMsg("[CAT:at,code=" + senderHelper.getQqNumber() + "]超时啦，若想继续绑定原神账号请再次发送 绑定原神账号 触发会话");
                        return;
                    }else if (exception instanceof GenShinNoSuchUIDException){
                        senderHelper.GROUP.sendMsg("[CAT:at,code=" + senderHelper.getQqNumber() + "]" + exception.getMessage());
                    }
                }).build();
                scopeContext.waiting(GENSHIN_SIGN_CHOOSE_UID, key, 120000, uidCallback);
            }

        }).onError(exception -> {
            if (exception instanceof TimeoutCancellationException){
                senderHelper.GROUP.sendMsg("[CAT:at,code=" + senderHelper.getQqNumber() + "]超时啦，若想继续绑定原神账号请再次发送 绑定原神账号 触发会话");
                return;
            }
            senderHelper.GROUP.sendMsg("[CAT:at,code=" + senderHelper.getQqNumber() + "]cookie无效");
        }).build();

        scopeContext.waiting(GENSHIN_SIGN, key,300000 , callback);

        senderHelper.GROUP.sendMsg("[CAT:at,code=" + senderHelper.getQqNumber() + "]请私发米游社登录cookie到本账号\n (注:若担心隐私泄露建议停止操作)");
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

        // 拼接出来这个人对应的唯一key
        String key = KEY_START + ":" + senderHelper.getQqNumber();

        session.push(GENSHIN_SIGN, key, privateMsg.getText());

        senderHelper.PRIVATE.sendMsg("成功收到cookie，正在检查cookie有效性");
    }
    @Filter(value = "^[1-5]\\d{8}", matchType = MatchType.REGEX_MATCHES)
    @OnPrivate
    @OnlySession(group = GENSHIN_SIGN_CHOOSE_UID)
    public void getChooseUID(PrivateMsg privateMsg, MsgSender sender, ListenerContext context) {

        final ContinuousSessionScopeContext session = (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);

        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(privateMsg, sender);

        // 拼接出来这个人对应的唯一key
        String key = KEY_START + ":" + senderHelper.getQqNumber();

        session.push(GENSHIN_SIGN_CHOOSE_UID, key, privateMsg.getText());

        senderHelper.PRIVATE.sendMsg("选择成功，正在绑定原神账号");
    }

}
