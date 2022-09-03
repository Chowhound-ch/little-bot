package com.zsck.bot.common.state.listener;

import com.zsck.bot.common.permit.annotation.BotPermits;
import com.zsck.bot.common.permit.enums.Permit;
import com.zsck.bot.common.state.StateMapHelper;
import com.zsck.bot.common.state.annotation.ExcludeGroupState;
import com.zsck.bot.common.state.enums.GroupStateEnum;
import com.zsck.bot.common.state.intercept.GroupStateIntercept;
import com.zsck.bot.helper.MsgSenderHelper;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author QQ:825352674
 * @date 2022/9/3 - 14:05
 */
@ConditionalOnBean(GroupStateIntercept.class)
@Component
public class StateChangeListener {
    @Autowired
    private StateMapHelper stateMapHelper;

    @BotPermits(Permit.MANAGER)
    @ExcludeGroupState
    @Filter(value = "/{{desState,(开机|关机)}}", matchType = MatchType.REGEX_MATCHES)
    @OnGroup
    public void changeState(GroupMsg groupMsg, MsgSender sender,
                            @FilterValue("desState") String desState){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        GroupStateEnum desStateEnum;
        if ("开机".equals(desState)) {
            desStateEnum = GroupStateEnum.ALL;
        } else {
            desStateEnum = GroupStateEnum.CLOSED;
        }

        if (stateMapHelper.setState(senderHelper.getNumber(), desStateEnum)) {
            senderHelper.groupMsg("修改成功,该群状态变更为" + desState);
        }else {
            senderHelper.groupMsg("修改失败,该群状态已是" + desState);
        }
    }
}
