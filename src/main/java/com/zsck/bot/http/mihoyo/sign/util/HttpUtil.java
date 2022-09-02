package com.zsck.bot.http.mihoyo.sign.util;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/8/31 - 22:12
 */
@Component
public class HttpUtil {

    private static CloseableHttpClient httpClient;

    public HttpUtil() {
        this.httpClient = HttpClients.createDefault();
    }

    //TODO
    public static JSONObject doGetJson(String url, Header[] headers){
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeaders(headers);
        try (CloseableHttpResponse execute = httpClient.execute(httpGet);){
            String res = EntityUtils.toString(execute.getEntity());
            return JSONObject.parseObject(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doGetJson(url, headers);
    }
    public static JSONObject doGetJson(String url, Header[] headers, Map<String, Object> data){
        return null;
    }
    public static JSONObject doPostJson(String url, Header[] headers, Map<String, Object> data){
        HttpPost post = new HttpPost(url);
        post.setHeaders(headers);
        CloseableHttpResponse execute = null;
        try {
            StringEntity entity = new StringEntity(JSON.toJSONString(data), StandardCharsets.UTF_8);
            post.setEntity(entity);
            execute = httpClient.execute(post);
            return JSONObject.parseObject(EntityUtils.toString(execute.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtil.close(execute);
        }
        return null;
    }
}
