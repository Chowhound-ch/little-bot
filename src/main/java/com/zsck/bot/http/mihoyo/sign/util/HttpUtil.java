package com.zsck.bot.http.mihoyo.sign.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/8/31 - 22:12
 */
@Component
public class HttpUtil {

    private CloseableHttpClient httpClient;

    public HttpUtil() {
        this.httpClient = HttpClients.createDefault();
    }

    //TODO
    public static JSONObject doGetJson(String url, Header[] headers){
        return doGetJson(url, headers);
    }
    public static JSONObject doGetJson(String url, Header[] headers, Map<String, Object> data){
        return null;
    }
    public static JSONObject doPostJson(String url, Header[] headers, Map<String, Object> data){
        return null;
    }
}
