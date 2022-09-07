package com.zsck.bot.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/9/7 - 11:15
 */
public abstract class HttpBase {
    @Resource
    protected ObjectMapper objectMapper;
    private CloseableHttpClient httpClient = HttpClients.createDefault();
    private Map<String, String> map = null;


    protected String doGetStr(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet = setHeader(httpGet);
        try (CloseableHttpResponse execute = httpClient.execute(httpGet)){

            return EntityUtils.toString(execute.getEntity());
        }
    }

    protected JsonNode doGetJson(String url) throws IOException {
        return objectMapper.readTree( doGetStr(url));
    }

    protected JsonNode doPostJson(String url, AbstractHttpEntity entity) throws IOException {
        return objectMapper.readTree(doPostStr(url, entity));
    }

    protected String doPostStr(String url, AbstractHttpEntity entity) throws IOException {
        HttpPost httpPost = new HttpPost(url);

        httpPost = setHeader(httpPost);

        httpPost.setEntity(entity);

        try (CloseableHttpResponse execute = httpClient.execute(httpPost);) {
            return EntityUtils.toString(execute.getEntity());
        }
    }


    protected abstract <T extends HttpRequestBase> T setHeader(T base);
}
