package com.zsck.bot.http.academic.pri;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.zsck.bot.annotation.BotPermits;
import com.zsck.bot.enums.Permit;
import com.zsck.bot.http.academic.Academic;
import com.zsck.bot.http.academic.pojo.Schedule;
import com.zsck.bot.http.academic.service.ClassNameService;
import com.zsck.bot.http.academic.service.ScheduleService;
import com.zsck.bot.util.Resolver;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/7/21 - 12:58
 */
@Component
public class PrivateListener {
    private final Logger logger = LoggerFactory.getLogger(PrivateListener.class);

    @Autowired
    private Academic academic;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ClassNameService classNameService;
    @Filter(value = "\\d{1,2}" , matchType = MatchType.REGEX_MATCHES)
    @OnPrivate
    public void viewWeeks(PrivateMsg privateMsg  ,MsgSender sender){
        String qqNumber = privateMsg.getAccountInfo().getAccountCode();
        List<Schedule> scheduleList = scheduleService.getScheduleByWeekIndex(Integer.valueOf(privateMsg.getText()));
        if (scheduleList.isEmpty()){
            sender.SENDER.sendPrivateMsg(qqNumber, "没有查到第" + privateMsg.getText() + "周的课表呢");
            return;
        }
        Resolver.sendCourseDetail(qqNumber , sender , scheduleList);
    }
    @Filter("本周")
    @OnPrivate
    public void thisWeek(PrivateMsg privateMsg  ,MsgSender sender){
        String qqNumber = privateMsg.getAccountInfo().getAccountCode();
        Date firstDate = scheduleService.getFirstDate();
        Date date = Date.valueOf(DateUtil.today());
        if (date.after(scheduleService.getEndDate())){
            sender.SENDER.sendPrivateMsg(qqNumber , "全部课程都已结束");
            return;
        }
        Long gap = DateUtil.between(firstDate, date, DateUnit.DAY, false) / 7 + 1;
        if (gap >= 0) {
            sender.SENDER.sendPrivateMsg(qqNumber, "本周为第:" + gap + "周");
        }else {
            sender.SENDER.sendPrivateMsg(qqNumber, "尚未开学,距离开学还有" + DateUtil.between(date , firstDate , DateUnit.DAY , true) + "天");
        }
        List<Schedule> scheduleList = scheduleService.getScheduleByWeekIndex(gap.intValue());
        if (scheduleList.isEmpty()){
            sender.SENDER.sendPrivateMsg(qqNumber, "日期:" + date + ",本周暂无课程");
        }
        Resolver.sendCourseDetail(qqNumber , sender , scheduleList);
    }
    @Filter(value = "今日" , matchType = MatchType.EQUALS)
    @OnPrivate
    public void today(PrivateMsg privateMsg  ,MsgSender sender){
        String qqNumber = privateMsg.getAccountInfo().getAccountCode();
        Date firstDate = scheduleService.getFirstDate();
        Date date = Date.valueOf(DateUtil.today());
        if (date.before(firstDate)) {
            sender.SENDER.sendPrivateMsg(qqNumber, "当前正处于假期，距离开学还有" +DateUtil.between(date , firstDate , DateUnit.DAY , true) + "天" );
        }else {
            if (date.after(scheduleService.getEndDate())){
                sender.SENDER.sendPrivateMsg(qqNumber , "全部课程都已结束");
                return;
            }
            long gap = DateUtil.between(firstDate, date, DateUnit.DAY, true);
            List<Schedule> scheduleList = scheduleService.getScheduleByDate(date);
            sender.SENDER.sendPrivateMsg(qqNumber , "本周为第:" + (gap/7 + 1) + "周");
            if (scheduleList.isEmpty()){
                sender.SENDER.sendPrivateMsg(qqNumber, "日期:" + date + ",无课程");
            }
            Resolver.sendCourseDetail(qqNumber , sender , scheduleList);
        }
    }
    @Filter(value = "明日" , matchType = MatchType.EQUALS)
    @OnPrivate
    public void tomorrow(PrivateMsg privateMsg  ,MsgSender sender){
        String qqNumber = privateMsg.getAccountInfo().getAccountCode();
        Date date = Date.valueOf(DateUtil.tomorrow().toDateStr());
        if (date.after(scheduleService.getEndDate())){
            sender.SENDER.sendPrivateMsg(qqNumber , "全部课程都已结束");
            return;
        }
        List<Schedule> scheduleList = scheduleService.getScheduleByDate(date);
        if (scheduleList.isEmpty()){
            sender.SENDER.sendPrivateMsg(qqNumber, "日期:" + date + ",无课程");
        }
        Resolver.sendCourseDetail(qqNumber , sender , scheduleList);
    }
    @BotPermits(Permit.HOST)
    @Transactional
    @Filter(value = "刷新" , matchType = MatchType.EQUALS)
    @OnPrivate
    public void refresh(PrivateMsg privateMsg  ,MsgSender sender){
        try {
            logger.info( "删除原表schedule数据:"+ scheduleService.remove(null) + "条");
            logger.info( "删除原表class_map数据:" + classNameService.remove(null) + "条");
            academic.init();
            logger.info("刷新成功:");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
