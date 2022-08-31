package com.zsck.bot.http.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsck.bot.http.academic.pojo.Schedule;

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

}
