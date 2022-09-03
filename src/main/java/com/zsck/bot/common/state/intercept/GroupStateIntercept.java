package com.zsck.bot.common.state.intercept;

import com.zsck.bot.common.state.StateMapHelper;
import com.zsck.bot.common.state.annotation.ExcludeGroupState;
import com.zsck.bot.common.state.enums.GroupStateEnum;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ListenerFunction;
import love.forte.simbot.listener.ListenerInterceptContext;
import love.forte.simbot.listener.ListenerInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 控制群组开关机，若有群消息监听器需要在关机状态下也可触发请使用 @ExcludeGroupState 注解标记
 * @author QQ:825352674
 * @date 2022/9/3 - 13:01
 */
@ConditionalOnProperty(prefix = "com.zsck.config", value = "group-state-check", havingValue = "true")
@Component
public class GroupStateIntercept implements ListenerInterceptor {

    @Autowired
    private StateMapHelper stateMapHelper;

    @NotNull
    @Override
    public InterceptionType intercept(@NotNull ListenerInterceptContext context) {
        ListenerFunction function = context.getListenerFunction();
        MsgGet msgGet = context.getMsgGet();
        if (function.getAnnotation(ExcludeGroupState.class) == null && function.getAnnotation(OnGroup.class) !=null){
            GroupStateEnum state = stateMapHelper.getState(((GroupMsg) msgGet).getGroupInfo().getGroupCode());
            if (state == GroupStateEnum.CLOSED){
                return InterceptionType.INTERCEPT;
            }
        }
        return InterceptionType.PASS;
    }
}
