package com.zsck.bot.http.academic.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 13:38
 */
@JsonIgnoreProperties("room")
@AllArgsConstructor
@Data
public class Schedule {
    private Integer lessonId;
    private Integer scheduleGroupId;
    private Integer periods;
    private Date date;
    private String room;
    @JsonProperty("weekday")
    private Integer weekDay;
    private Integer startTime;
    private Integer endTime;
    private String personName;
    private Integer weekIndex;
}
