package com.zsck.bot.http.academic;

import com.zsck.bot.http.academic.pojo.ClassMap;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/9/1 - 16:48
 */
@Component
public class AcademicHelper {

    private static MessageContentBuilderFactory factory;

    public AcademicHelper(MessageContentBuilderFactory factory) {
        AcademicHelper.factory = factory;
    }

    public static MessageContent getDetailMsg(ClassMap classMap, Map<String, Object> classDetail){
        MessageContentBuilder builder = factory.getMessageContentBuilder();
        StringBuffer buffer = new StringBuffer();
        buffer.append("课程:").append(classMap.getClassName());

        if (classDetail != null && !classDetail.isEmpty()){
            buffer.append("\n开课周次:").append(classDetail.get("start"))
                    .append(" - ").append( classDetail.get("end") );
            buffer.append("\n授课教师:").append(classDetail.get("person_name"));
            return builder.text(buffer).build();
        }
        return builder.text(buffer.append("暂无信息")).build();
    }
}
