package com.zsck.bot.http.academic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.zsck.bot.http.HttpBase;
import com.zsck.bot.http.academic.pojo.ClassMap;
import com.zsck.bot.http.academic.pojo.Schedule;
import com.zsck.bot.http.academic.service.ClassNameService;
import com.zsck.bot.http.academic.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 9:59
 */
@Slf4j
@Component
public class Academic extends HttpBase {
    public String USERAGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.0.3161 SLBChan/103";
    public String REFERER = "http://jxglstu.hfut.edu.cn/eams5-student/login?refer=http://jxglstu.hfut.edu.cn/eams5-student/for-std/course-table/info/152113";


    public static final String GET_SALT = "http://jxglstu.hfut.edu.cn/eams5-student/login-salt";
    public static final String LOGIN_URL = "http://jxglstu.hfut.edu.cn/eams5-student/login";
    public static final String LESSON_URL = "http://jxglstu.hfut.edu.cn/eams5-student/ws/schedule-table/datum";


    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ClassNameService classNameService;
    @Value("${com.zsck.data.user-name}")
    private String userName;
    @Value("${com.zsck.data.pwd}")
    private String pwd;

    public Boolean init(){

        try {
            //访问中间网址，获取会话cookie和加密密钥
            String encode = DigestUtils.sha1Hex(doGetStr( GET_SALT ) + "-" + pwd);//密码加密

            //登录验证
            StringEntity entity = new StringEntity("{\"username\":\"" + userName + "\",\"password\":\""+encode+"\",\"captcha\":\"\"}" , "UTF-8");
            entity.setContentType("application/json");
            JsonNode loginRes = doPostJson(LOGIN_URL, entity);//登录结果

            //访问我的课程表，获取课程表中lessons的id
            JsonNode lessonRes = doGetJson(new URI("http://jxglstu.hfut.edu.cn/eams5-student/for-std/course-table/get-data?bizTypeId=23&semesterId=194&dataId=152113").toString());

            JsonNode lessonIds = lessonRes.get("lessonIds");

            //再次请求，根据lessonIds请求得到具体lesson信息
            StringEntity entityForRes = new StringEntity("{\"lessonIds\":" + lessonIds.toString() +",\"studentId\":152113,\"weekIndex\":\"\"}" , "UTF-8");
            entityForRes.setContentType("application/json");
            JsonNode lessonsRes = doPostJson(LESSON_URL, entityForRes);

            //对返回的lesson信息进行解析
            try {

                List<Schedule> scheduleList = new ArrayList<>();
                lessonsRes.get("result").get("scheduleList").forEach( res ->{
                    try {
                        Schedule schedule = objectMapper.readValue(res.toString(), Schedule.class);
                        schedule.setRoom( res.get("room").get("nameZh").asText() );
                        scheduleList.add(schedule);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                } );
                List<ClassMap> classMapList = new ArrayList<>();
                lessonsRes.get("result").get("lessonList").forEach( res ->{
                    classMapList.add( new ClassMap(res.get("id").asInt(), res.get("courseName").asText()) );
                } );

                log.info("表schedule新增数据:" +  scheduleService.saveBatch(scheduleList) + "条");
                log.info("表class_map新增数据:" + classNameService.saveBatch(classMapList) + "条");
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                log.error("数据解析错误");
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("访问网址错误");
        }
        return false;
    }


    @Override
    public <T extends HttpRequestBase> T setHeader(T base) {
        base.setHeader(HttpHeaders.USER_AGENT , USERAGENT);
        base.setHeader(HttpHeaders.REFERER , REFERER);
        return base;
    }
}
