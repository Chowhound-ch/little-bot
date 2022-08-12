package com.zsck.bot.http.Academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsck.bot.http.Academic.pojo.Schedule;

import java.sql.Date;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 15:50
 */
public interface ScheduleService extends IService<Schedule> {
    Date getFirstDate();

    Date getEndDate();

    List<Schedule> getScheduleByWeekIndex(Integer index);

    List<Schedule> getScheduleByDate(Date date);
//    Date getEndDate();
//
//    Date getFirstDate();
//
//    List<Schedule> getScheduleByDate(Date date);
//
//    Integer keepDate(List<Schedule> list);
//
//    List<Schedule> getAllSchedule();
//
//    List<Schedule> getScheduleByWeekIndex(Integer weekIndex);
//
//    Integer deleteAll();
}
