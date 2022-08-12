package com.zsck.bot.http.Academic.timing;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.zsck.bot.http.Academic.pojo.Schedule;
import com.zsck.bot.http.Academic.service.ScheduleService;
import com.zsck.bot.util.DataUtil;
import com.zsck.bot.util.Resolver;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final Logger logger = LoggerFactory.getLogger(CallAttentionTo.class);
    @Autowired
    private ScheduleService scheduleService;
    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    private void init(){
        scheduledExecutorService = Executors.newScheduledThreadPool(5);
    }

    //0 40 7 * * 1-5
    @Scheduled(cron = "0 40 7 * * 1-5")
    @Async
    public void morning(){
        Bot bot = botManager.getDefaultBot();
        Date firstDate = scheduleService.getFirstDate();
        Date date = Date.valueOf(DateUtil.today());
        if (date.before(firstDate)) {
            bot.getSender().SENDER.sendPrivateMsg(DataUtil.host, "当前正处于假期，距离开学还有" +DateUtil.between(date , firstDate , DateUnit.DAY , true) + "天" );
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
            bot.getSender().SENDER.sendPrivateMsg(DataUtil.host , "今日为:" + date +",本周为第:" + (gap/7 + 1) + "周");
            Resolver.sendCourseDetail(DataUtil.host , bot.getSender() , scheduleList);
        }
    }
    @Scheduled(cron = "0 0 22 * * 1,2,3,4,7")
    @Async
    public void even(){
        morning();
    }
}
