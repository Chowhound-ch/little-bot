package com.zsck.bot.http.academic;

import cn.hutool.core.io.IoUtil;
import com.zsck.bot.http.academic.pojo.ClassMap;
import com.zsck.bot.http.academic.pojo.Schedule;
import com.zsck.bot.http.academic.service.ClassNameService;
import com.zsck.bot.http.academic.service.ScheduleService;
import com.zsck.bot.util.DataUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 9:59
 */
@Component
public class Academic {
    public String USERAGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.0.3161 SLBChan/103";
    public String REFERER = "http://jxglstu.hfut.edu.cn/eams5-student/login?refer=http://jxglstu.hfut.edu.cn/eams5-student/for-std/course-table/info/152113";

    private Logger logger = LoggerFactory.getLogger(Academic.class);
    private List<Schedule> scheduleListRes;
    private List<ClassMap> className;

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ClassNameService classNameService;
    @Value("${com.zsck.data.user-name}")
    private String userName;
    @Value("${com.zsck.data.pwd}")
    private String pwd;

    public void init(){
        JSONArray lessons;
        scheduleListRes = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://jxglstu.hfut.edu.cn/eams5-student/login-salt");
        httpGet.setHeader( HttpHeaders.USER_AGENT, USERAGENT);
        httpGet.setHeader(HttpHeaders.REFERER , REFERER);

        CloseableHttpResponse prepareForKey = null;
        CloseableHttpResponse prepareLessonIds = null;
        CloseableHttpResponse res = null;
        try {
            //访问中间网址，获取会话cookie和加密密钥
            prepareForKey = httpClient.execute(httpGet);
            String salt = EntityUtils.toString(prepareForKey.getEntity());//密钥
            String encode = DigestUtils.sha1Hex(salt+"-" + pwd);//密码加密
            //登录验证
            HttpPost post = new HttpPost("http://jxglstu.hfut.edu.cn/eams5-student/login");
            post.setHeader(HttpHeaders.ACCEPT , "*/*");
            post.setHeader( HttpHeaders.USER_AGENT, USERAGENT);
            post.setHeader(HttpHeaders.REFERER , REFERER);
            JSONObject jsonObject = new JSONObject("{\"username\":\"" + userName + "\",\"password\":\""+encode+"\",\"captcha\":\"\"}");
            StringEntity entity = new StringEntity(jsonObject.toString() , "UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);
            httpClient.execute(post);
            //访问我的课程表，获取课程表中lessons的id
            httpGet.setURI(new URI("http://jxglstu.hfut.edu.cn/eams5-student/for-std/course-table/get-data?bizTypeId=23&semesterId=194&dataId=152113"));
            prepareLessonIds = httpClient.execute(httpGet);
            String t = EntityUtils.toString(prepareLessonIds.getEntity());
            JSONObject object = new JSONObject(t);
            lessons = object.optJSONArray("lessonIds");
            //再次请求，根据lessonIds请求得到具体lesson信息
            HttpPost newPost = new HttpPost("http://jxglstu.hfut.edu.cn/eams5-student/ws/schedule-table/datum");
            newPost.setHeader(HttpHeaders.USER_AGENT , USERAGENT);
            newPost.setHeader(HttpHeaders.REFERER , REFERER);
            JSONObject le = new JSONObject("{\"lessonIds\":" + lessons.toString() +",\"studentId\":152113,\"weekIndex\":\"\"}" );
            StringEntity entityForRes = new StringEntity(le.toString() , "UTF-8");
            entityForRes.setContentType("application/json");
            newPost.setEntity(entityForRes);
            res = httpClient.execute(newPost);

            //对返回的lesson信息进行解析
            try {
                JSONObject json = new JSONObject(EntityUtils.toString(res.getEntity()));
                JSONArray scheduleList = json.optJSONObject("result").optJSONArray("scheduleList");
                for (int i = 0; i < scheduleList.length(); i++) {
                    JSONObject course = scheduleList.optJSONObject(i);
                    Integer lessonId = course.optInt("lessonId");
                    Integer scheduleGroupId = course.optInt("scheduleGroupId");
                    Integer periods = course.optInt("periods");
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = format.parse(course.optString("date"));

                    String room = course.optJSONObject("room").optString("nameZh");
                    Integer weekday = course.optInt("weekday");
                    Integer startTime = course.optInt("startTime");
                    Integer endTime = course.optInt("endTime");
                    String  personName = course.optString("personName");
                    Integer weekIndex = course.optInt("weekIndex");
                    Schedule schedule = new Schedule(lessonId, scheduleGroupId, periods,new java.sql.Date(date.getTime()) , room, weekday, startTime, endTime, personName, weekIndex);
                    scheduleListRes.add(schedule);
                }
                className = new ArrayList<>();
                JSONArray lessonList  =  json.optJSONObject("result").optJSONArray("lessonList");
                for (int i = 0; i < lessonList.length(); i++) {
                    JSONObject lesson = lessonList.optJSONObject(i);
                    className.add(new ClassMap(lesson.optInt("id") , lesson.optString("courseName")));
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("数据解析错误");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("访问网址错误");
        } finally {
            IoUtil.close(prepareForKey);
            IoUtil.close(prepareLessonIds);
            IoUtil.close(res);
        }
        keepData();
    }
    public void keepData(){
        logger.info("表schedule新增数据:" +  scheduleService.saveBatch(scheduleListRes) + "条");
        logger.info("表class_map新增数据:" + classNameService.saveBatch(className) + "条");
    }
}
