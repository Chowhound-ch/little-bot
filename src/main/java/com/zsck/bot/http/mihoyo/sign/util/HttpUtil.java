package com.zsck.bot.http.mihoyo.sign.util;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(35000)
            .setConnectionRequestTimeout(35000)
            .setSocketTimeout(60000)
            .setRedirectsEnabled(true)
            .build();

    public static JSONObject doGetJson(String url, Header[] headers){
        return doGetJson(url, headers, null);
    }
    public static JSONObject doGetJson(String url, Header[] headers, Map<String, Object> data) {
        try {
            //构建URI
            URIBuilder uriBuilder = new URIBuilder(url);
            if (data != null && !data.isEmpty()){
                List<NameValuePair> list = new ArrayList<>();
                data.forEach((key, value)-> list.add(new BasicNameValuePair(key, value.toString())));
                uriBuilder.addParameters(list);
            }

            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.setHeaders(headers);
            httpGet.setConfig(REQUEST_CONFIG);
            try (CloseableHttpResponse execute = httpClient.execute(httpGet)){
                String res = EntityUtils.toString(execute.getEntity());
                return JSONObject.parseObject(res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject doPostJson(String url, Header[] headers, Map<String, Object> data){
        HttpPost post = new HttpPost(url);
        post.setHeaders(headers);
        CloseableHttpResponse execute = null;

        try {
            StringEntity entity = new StringEntity(JSON.toJSONString(data), StandardCharsets.UTF_8);
            post.setEntity(entity);
            post.setConfig(REQUEST_CONFIG);
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
