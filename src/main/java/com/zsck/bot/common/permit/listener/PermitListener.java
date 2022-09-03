package com.zsck.bot.common.permit.listener;

import catcode.CatCodeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsck.bot.common.permit.annotation.BotPermits;
import com.zsck.bot.common.permit.enums.Permit;
import com.zsck.bot.common.permit.pojo.PermitDetail;
import com.zsck.bot.common.permit.service.PermitDetailService;
import com.zsck.bot.helper.MsgSenderHelper;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author QQ:825352674
 * @date 2022/9/3 - 14:51
 */
@Slf4j
@Component
public class PermitListener {
    private final CatCodeUtil codeUtil = CatCodeUtil.getInstance();
    @Autowired
    private PermitDetailService permitDetailService;


    @BotPermits(Permit.HOST)
    @Filter(value = "^/设置权限\\s*{{position,(1|2)}}", matchType = MatchType.REGEX_MATCHES, anyAt = true)
    @OnGroup
    public void setPosition(GroupMsg groupMsg, MsgSender sender,
                            @FilterValue("position")Integer position) {
        LambdaQueryWrapper<PermitDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermitDetail::getQqNumber, codeUtil.getParam(groupMsg.getMsg(), "code")/*获取被at人qq*/);
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, sender);
        PermitDetail detail = permitDetailService.getOne(wrapper);
        if (detail == null){
            detail = new PermitDetail(senderHelper.getNumber(), 1);
        }
        if (groupMsg.getAccountInfo().getAccountCode().equals(detail.getQqNumber())) {
            senderHelper.sendMsg("自己改自己权限是吧");
            return;//操作人和被操作人不能为同一人
        }
        if (!Objects.equals(detail.getPermit(), position)){
            detail.setPermit(position);
            senderHelper.sendMsg("已将[" + detail.getQqNumber() +"]权限设置为" + Permit.getEnumByValue(position).name());
            permitDetailService.saveOrUpdate(detail);
            log.info("权限变更: [{}]({}) -> {}", detail.getQqNumber(), detail.getPermit(), position);
        }else {
            senderHelper.sendMsg("["+ detail.getQqNumber() + "]权限已是" + Permit.getEnumByValue(position).name() + "不能重复设置");
        }
    }
}
