package com.zsck.bot.http.academic.timing;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.zsck.bot.helper.MsgSenderHelper;
import com.zsck.bot.enums.MsgType;
import com.zsck.bot.http.academic.pojo.Schedule;
import com.zsck.bot.http.academic.service.ScheduleService;
import com.zsck.bot.http.academic.util.ClassTableResolver;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 20:46
 */
@EnableScheduling
@EnableAsync
@Component
public class CallAttentionTo {
    @Autowired
    private BotManager botManager;
    private Bot bot;
    private final Logger logger = LoggerFactory.getLogger(CallAttentionTo.class);
    @Autowired
    private ScheduleService scheduleService;
    private ScheduledExecutorService scheduledExecutorService;
    private MsgSenderHelper senderHelper;
    @Value("${com.zsck.data.user-qq}")
    private String host;
    @PostConstruct
    private void init(){
        bot = botManager.getDefaultBot();
        scheduledExecutorService = Executors.newScheduledThreadPool(5);
        senderHelper = MsgSenderHelper.getInstance(bot.getSender(), MsgType.PRIVATE, host);
    }

    //0 40 7 * * 1-5
    @Scheduled(cron = "0 40 7 * * 1-5")
    @Async
    public void morning(){
        Date firstDate = scheduleService.getFirstDate();
        Date date = Date.valueOf(DateUtil.today());
        if (date.before(firstDate)) {
            senderHelper.PRIVATE.sendMsg("当前正处于假期，距离开学还有" +DateUtil.between(date , firstDate , DateUnit.DAY , true) + "天" );
        }else {
            long gap = DateUtil.between(firstDate, date, DateUnit.DAY, true);
            List<Schedule> scheduleList = scheduleService.getScheduleByDate(date);
            if (scheduleList.isEmpty()){
                return;
            }
            if (scheduleList.get(0).getStartTime() != 800){
                if (LocalDateTime.now().getHour() <= 8) {
                    scheduledExecutorService.schedule(()->{
                        logger.info("不是早八,延迟提醒");
                        morning();
                    } ,  2, TimeUnit.HOURS);
                    return;
                }
            }
            senderHelper.PRIVATE.sendMsg("今日为:" + date +", 第:" + (gap/7 + 1) + "周");
            ClassTableResolver.sendCourseDetail(host , bot.getSender() , scheduleList);
        }
    }
    @Scheduled(cron = "0 0 22 * * 1,2,3,4,7")
    @Async
    public void even(){
        Date firstDate = scheduleService.getFirstDate();
        Date date = Date.valueOf(DateUtil.formatDate(DateUtil.tomorrow()));
        if ( !date.before(firstDate)) {
            long gap = DateUtil.between(firstDate, date, DateUnit.DAY, true);
            List<Schedule> scheduleList = scheduleService.getScheduleByDate(date);
            if (scheduleList.isEmpty()){
                return;
            }
            senderHelper.PRIVATE.sendMsg("明日为:" + date +", 第:" + (gap/7 + 1) + "周");
            ClassTableResolver.sendCourseDetail(host , bot.getSender() , scheduleList);
        }
    }
}
