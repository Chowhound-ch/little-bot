package com.zsck.bot.http.academic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsck.bot.mybatis.mapper.ScheduleMapper;
import com.zsck.bot.http.academic.pojo.Schedule;
import com.zsck.bot.http.academic.service.ScheduleService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 15:51
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper , Schedule>
        implements ScheduleService {
    @Override
    public Date getFirstDate() {
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("MIN(date) as date");
        Map<String, Object> map = getMap(queryWrapper);
        return ((Date) map.get("date"));
    }

    @Override
    public Date getEndDate() {
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("MAX(date) as date");
        Map<String, Object> map = getMap(queryWrapper);
        return ((Date) map.get("date"));
    }

    @Override
    public List<Schedule> getScheduleByWeekIndex(Integer index) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getWeekIndex , index);
        return list(wrapper);
    }

    @Override
    public List<Schedule> getScheduleByDate(Date date) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDate , date);
        return list(wrapper);
    }

}
