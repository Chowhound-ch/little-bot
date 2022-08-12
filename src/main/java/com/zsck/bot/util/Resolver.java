package com.zsck.bot.util;

import cn.hutool.core.date.DateUtil;
import com.zsck.bot.http.Academic.pojo.Schedule;
import com.zsck.bot.http.Academic.service.ClassNameService;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.sender.MsgSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.temporal.ChronoField;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 19:38
 */
@Component
public class Resolver {
    private static MessageContentBuilderFactory messageContentBuilderFactory;
    private static ClassNameService classNameService;

    public static void sendCourseDetail( String qqNumber, MsgSender sender , List<Schedule> list){
        Date today = null;
        MessageContentBuilder builder = messageContentBuilderFactory.getMessageContentBuilder();
        for (Schedule detail : list) {
            Date th  = detail.getDate();
            if (today == null || th.compareTo(today) != 0) {
                if (today != null){
                    sender.SENDER.sendPrivateMsg(qqNumber, builder.build());
                    builder = messageContentBuilderFactory.getMessageContentBuilder();
                }

                int i = detail.getDate().toLocalDate().get(ChronoField.DAY_OF_WEEK);
                builder.text(DateUtil.format( th, "MM-dd") + " 周"  + WeekUtil.WEEKDAY[i - 1] + ":");
                today = th;
            }
            if (th.compareTo(today) >= 0) {
                String room = detail.getRoom();
                String course = classNameService.getClassName(detail.getLessonId());
                String start = String.format( "%02d",detail.getStartTime()/100) + ":" + String.format( "%02d",detail.getStartTime()%100);
                String end = String.format( "%02d",detail.getEndTime()/100 )+ ":" + String.format("%02d",detail.getEndTime()%100);
                builder.text("\n" + start + " - " + end + room + " " + course + " " + detail.getPersonName());
            }
        }
        sender.SENDER.sendPrivateMsg(qqNumber, builder.build());
    }

    @Autowired
    public void setMessageContentBuilderFactory(MessageContentBuilderFactory messageContentBuilderFactory) {
        Resolver.messageContentBuilderFactory = messageContentBuilderFactory;
    }

    @Autowired
    public void setClassNameService(ClassNameService classNameService) {
        Resolver.classNameService = classNameService;
    }
}
