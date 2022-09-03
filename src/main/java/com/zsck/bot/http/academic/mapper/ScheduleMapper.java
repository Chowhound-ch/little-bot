package com.zsck.bot.http.academic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsck.bot.http.academic.pojo.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 15:12
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
    Map<String , Object> getClassDetail(@Param("lessonId") Integer lessonId);
}
