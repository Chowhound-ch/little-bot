package com.zsck.bot.util;

import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author QQ:825352674
 * @date 2022/7/27 - 9:06
 */
@Component
public class ContextUtil {
    /**
     * 无法Autowired
     */
    private static ApplicationContext applicationContext;

    private static MiraiMessageContentBuilderFactory factory;

    public static MiraiMessageContentBuilderFactory getMiraiFactory(){
       return factory;
    }
    @PostConstruct
    public void setMiraiMessageContentBuilderFactory() {
        ContextUtil.factory = ((MiraiMessageContentBuilderFactory) applicationContext.getBean("simbotMessageContentBuilderFactory"));
    }
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        ContextUtil.applicationContext = applicationContext;
    }
}
