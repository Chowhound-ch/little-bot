package com.zsck.bot.helper;

import com.zsck.bot.enums.MsgType;
import lombok.AllArgsConstructor;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.api.sender.MsgSender;

/**
 * @author QQ:825352674
 * @date 2022/8/13 - 12:57
 */
@AllArgsConstructor
public class MsgSenderHelper{
    private MsgType msgType;
    private String number;
    private MsgSender sender;


    private MsgSenderHelper(MsgType msgType, MsgSender sender) {
        this.msgType = msgType;
        this.sender = sender;
    }



    public String getNumber() {
        return number;
    }

    private void setNumber(String number) {
        this.number = number;
    }

    public static MsgSenderHelper getInstance(MsgGet msgGet, MsgSender sender){
        MsgType msgType = getSenderType(msgGet);
        MsgSenderHelper msgSenderHelper = new MsgSenderHelper(msgType, sender);
        if (msgType == MsgType.GROUP){
            msgSenderHelper.setNumber(((GroupMsg) msgGet).getGroupInfo().getGroupCode());
        }else{
            msgSenderHelper.setNumber(msgGet.getAccountInfo().getAccountCode());
        }
        return msgSenderHelper;
    }
    public static MsgSenderHelper getInstance(String qqNumber, MsgSender sender, MsgType msgType){
        return new MsgSenderHelper(msgType, qqNumber, sender);
    }

    private static MsgType getSenderType(MsgGet msgGet){
        if (msgGet.getOriginalData().startsWith("Group")){
            return MsgType.GROUP;
        }
        return MsgType.PRIVATE;
    }
    /**
     * 不确定需要发送什么类型消息时使用
     * @param msg
     */
    public void sendMsg(String msg){
        if (msgType == MsgType.GROUP){
            sender.SENDER.sendGroupMsg(number, msg);
        }else if (msgType == MsgType.PRIVATE){
            sender.SENDER.sendPrivateMsg(number, msg);
        }
    }
    /**
     * 不确定需要发送什么类型消息时使用
     * @param msg
     */
    public void sendMsg(MessageContent msg){
        if (msgType == MsgType.GROUP){
            sender.SENDER.sendGroupMsg(number, msg);
        }else if (msgType == MsgType.PRIVATE){
            sender.SENDER.sendPrivateMsg(number, msg);
        }
    }
    public void priMsg(String msg){
        sender.SENDER.sendPrivateMsg(number, msg);
    }

    public void priMsg(MessageContent msg){
        sender.SENDER.sendPrivateMsg(number, msg);
    }
    public void groupMsg(String msg){
        sender.SENDER.sendGroupMsg(number, msg);
    }

    public void groupMsg(MessageContent msg){
        sender.SENDER.sendGroupMsg(number, msg);
    }
}