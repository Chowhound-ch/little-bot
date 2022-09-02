package com.zsck.bot.http.academic.pri;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsck.bot.annotation.BotPermits;
import com.zsck.bot.common.helper.MsgSenderHelper;
import com.zsck.bot.enums.Permit;
import com.zsck.bot.http.academic.Academic;
import com.zsck.bot.http.academic.AcademicHelper;
import com.zsck.bot.http.academic.pojo.ClassMap;
import com.zsck.bot.http.academic.pojo.Schedule;
import com.zsck.bot.http.academic.service.ClassNameService;
import com.zsck.bot.http.academic.service.ScheduleService;
import com.zsck.bot.util.Resolver;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/7/21 - 12:58
 */
@Slf4j
@Component
public class PrivateListener {

    @Autowired
    private Academic academic;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ClassNameService classNameService;


    @Filter(value = "\\d{1,2}" , matchType = MatchType.REGEX_MATCHES)
    @OnPrivate
    public void viewWeeks(PrivateMsg privateMsg  ,MsgSender sender){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(privateMsg, sender);
        List<Schedule> scheduleList = scheduleService.getScheduleByWeekIndex(Integer.valueOf(privateMsg.getText()));
        if (scheduleList.isEmpty()){
            senderHelper.senderPriMsg("没有查到第" + privateMsg.getText() + "周的课表呢");
            return;
        }
        Resolver.sendCourseDetail(privateMsg.getAccountInfo().getAccountCode() , sender , scheduleList);
    }
    @Filter(value = "(w|W)", matchType = MatchType.REGEX_MATCHES)
    @OnPrivate
    public void thisWeek(PrivateMsg privateMsg  , MsgSender sender){
        week(privateMsg, sender, '=');
    }
    @Filter(value = "(w|W){{param,(\\+|-|=)}}", matchType = MatchType.REGEX_MATCHES)
    @OnPrivate
    public void week(PrivateMsg privateMsg  , MsgSender sender,
                         @FilterValue("param") char param){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(privateMsg, sender);
        Date firstDate = scheduleService.getFirstDate();
        Date date = Date.valueOf(DateUtil.today());
        Standard balance = getBalanceByParam(param);

        Long gap = DateUtil.between(firstDate, date, DateUnit.DAY, false) / 7 + balance.value;
        if ( !scheduleService.hasClass(gap.intValue()) ){
            senderHelper.senderPriMsg(balance.getWeek() + "无课程");
            return;
        }
        if (gap >= 0) {
            senderHelper.senderPriMsg(balance.getWeek() + "为第" + gap + "周");
        }else {
            senderHelper.senderPriMsg(balance.getWeek() + "尚未开学,距离开学还有" + DateUtil.between(date , firstDate , DateUnit.DAY , true) + "天");
        }
        List<Schedule> scheduleList = scheduleService.getScheduleByWeekIndex(gap.intValue());
        if (scheduleList.isEmpty()){
            senderHelper.senderPriMsg(balance.getWeek() + "暂无课程(周次:"+ gap +")");
        }
        Resolver.sendCourseDetail(privateMsg.getAccountInfo().getAccountCode() , sender , scheduleList);
    }
    @Filter(value = "(d|D)", matchType = MatchType.REGEX_MATCHES)
    @OnPrivate
    public void thisDay(PrivateMsg privateMsg  , MsgSender sender){
        day(privateMsg, sender, '=');
    }
    @Filter(value = "(d|D){{param,(\\+|-|=)}}" , matchType = MatchType.REGEX_MATCHES)
    @OnPrivate
    public void day(PrivateMsg privateMsg  ,MsgSender sender,
                    @FilterValue("param")char param){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(privateMsg, sender);
        Standard balance = getBalanceByParam(param);
        Date firstDate = scheduleService.getFirstDate();
        Date date = balance.getDate();

        if (date.before(firstDate)) {
            senderHelper.senderPriMsg( "该日正处于假期，距离开学:" +DateUtil.between(date , firstDate , DateUnit.DAY , true) + "天" );
        }else {
            long gap = DateUtil.between(firstDate, date, DateUnit.DAY, true);
            List<Schedule> scheduleList = scheduleService.getScheduleByDate(date);
            senderHelper.senderPriMsg( balance.getDay() + "日期:" + date + ",属于第 " + (gap/7 + 1) + " 周");
            if (scheduleList.isEmpty()){
                senderHelper.senderPriMsg("当日无课程");
                return;
            }
            Resolver.sendCourseDetail(privateMsg.getAccountInfo().getAccountCode() , sender , scheduleList);
        }
    }
    @Filter(value = "(f|F)\\s*{{name}}" , matchType = MatchType.REGEX_MATCHES)
    @OnPrivate
    public void find(PrivateMsg privateMsg  ,MsgSender sender,
                     @FilterValue("name") String name){
        LambdaQueryWrapper<ClassMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(ClassMap::getClassName, name);
        List<ClassMap> list = classNameService.list(wrapper);
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(privateMsg, sender);


        if ( list.isEmpty()){
            senderHelper.senderPriMsg("未查询到符合条件的信息");
        }else {
            for (ClassMap classMap : list) {
                Map<String, Object> classDetail = scheduleService.getClassDetail(classMap.getId());
                MessageContent msg = AcademicHelper.getDetailMsg(classMap, classDetail);

                senderHelper.senderPriMsg(msg);
            }
        }
    }


    @BotPermits(Permit.HOST)
    @Transactional
    @Filter(value = "刷新" , matchType = MatchType.EQUALS)
    @OnPrivate
    public void refresh(PrivateMsg privateMsg  ,MsgSender sender){
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(privateMsg, sender);
        try {
            long countSch = scheduleService.count();
            long countClass = classNameService.count();
            if (scheduleService.remove(null)) {
                log.info("删除原表schedule数据:" + countSch + "条");
                senderHelper.senderPriMsg("删除原表schedule数据:" + countSch + "条");
            }
            if (classNameService.remove(null)) {
                log.info("删除原表class_map数据:" + countClass +"条");
                senderHelper.senderPriMsg("删除原表class_map数据:" + countClass +"条");
            }
            academic.init();
            log.info("刷新成功:");
            senderHelper.senderPriMsg("刷新成功");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    Standard getBalanceByParam(char param){
        switch (param){
            case '+':{
                return Standard.NEXT;
            }
            case '-':{
                return Standard.LAST;
            }
            default:
                return Standard.THIS;
        }
    }
    enum Standard{//今天、明天、昨天 或 本周、下周、上周
        THIS(1),
        NEXT(2),
        LAST(0);

        Integer value;
        Standard(Integer value) {
            this.value = value;
        }
        String getWeek(){
            switch (value){
                case 1: return "本周";
                case 2: return "下周";
                case 0: return "上周";
                default: return "";
            }
        }
        String getDay(){
            switch (value){
                case 1: return "今天";
                case 2: return "明天";
                case 0: return "昨天";
                default: return "";
            }
        }
        Date getDate(){
            switch (value){
                case 1: return Date.valueOf(DateUtil.today());
                case 2: return Date.valueOf(DateUtil.formatDate(DateUtil.tomorrow()));
                case 0: return Date.valueOf(DateUtil.formatDate(DateUtil.yesterday()));
                default: return null;
            }
        }
    }
}
