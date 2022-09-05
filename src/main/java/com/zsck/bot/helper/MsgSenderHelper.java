package com.zsck.bot.helper;

import com.zsck.bot.enums.MsgType;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.api.sender.MsgSender;

/**
 * @author QQ:825352674
 * @date 2022/8/13 - 12:57
 */
public class MsgSenderHelper {
    private final MsgType msgType;
    private String qqNumber;
    private String group;
    private final MsgSender sender;

    public Group GROUP;
    public Private PRIVATE;



    private MsgSenderHelper(MsgType msgType, MsgSender sender) {
        this.msgType = msgType;
        this.sender = sender;

        GROUP = new Group();
        PRIVATE = new Private();
    }


    public String getQqNumber() {
        return qqNumber;
    }

    public String getGroup() {
        return group;
    }

    private void setQqNumber(String qqNumber) {
        this.qqNumber = qqNumber;
    }
    private void setGroup(String group) {
        this.group = group;
    }

    public static MsgSenderHelper getInstance(MsgSender sender, MsgType msgType, String number){
        MsgSenderHelper msgSenderHelper = new MsgSenderHelper(msgType, sender);
        if (msgType == MsgType.GROUP){
            msgSenderHelper.setGroup(number);
        }else {
            msgSenderHelper.setQqNumber(number);
        }

        return msgSenderHelper;
    }
    public static MsgSenderHelper getInstance(MsgGet msgGet, MsgSender sender){
        MsgType msgType = getSenderType(msgGet);
        MsgSenderHelper msgSenderHelper = new MsgSenderHelper(msgType, sender);
        if (msgType == MsgType.GROUP){//群组消息则保存群号、发言人qq号
            GroupMsg groupMsg = (GroupMsg) msgGet;
            msgSenderHelper.setGroup(groupMsg.getGroupInfo().getGroupCode());
        }

        msgSenderHelper.setQqNumber(msgGet.getAccountInfo().getAccountCode());
        return msgSenderHelper;
    }
    private static MsgType getSenderType(MsgGet msgGet){
        if (msgGet.getOriginalData().startsWith("Group")){
            return MsgType.GROUP;
        }
        return MsgType.PRIVATE;
    }

    public class Group{
        public void sendMsg(String msg){
            sender.SENDER.sendGroupMsg(group, msg);
        }
        public void sendMsg(MessageContent msg){
            sender.SENDER.sendGroupMsg(group, msg);
        }
    }
    public class Private{
        public void sendMsg(String msg){
            sender.SENDER.sendPrivateMsg(qqNumber, msg);
        }
        public void sendMsg(MessageContent msg){
            sender.SENDER.sendPrivateMsg(qqNumber, msg);
        }
    }
}