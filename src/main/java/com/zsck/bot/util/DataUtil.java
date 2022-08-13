package com.zsck.bot.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 17:44
 */
@ConfigurationProperties(prefix = "com.zsck.data")
@Configuration
public class DataUtil {
    public static String userName;
    public static String pwd;
    public static String filePath;

    public void setFilePath(String filePath) {
        DataUtil.filePath = filePath;
    }

    public void setUserName(String userName) {
        DataUtil.userName = userName;
    }

    public void setPwd(String pwd) {
        DataUtil.pwd = pwd;
    }

}
