package com.zsck.bot.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author QQ:825352674
 * @date 2022/9/7 - 11:15
 */
public abstract class HttpBase {
    @Resource
    protected ObjectMapper objectMapper;
    private CloseableHttpClient httpClient = HttpClients.createDefault();


    private String doGetStr(String url, CloseableHttpClient httpClient) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet = setHeader(httpGet);
        try (CloseableHttpResponse execute = httpClient.execute(httpGet)){
            return EntityUtils.toString(execute.getEntity());
        }
    }
    private byte[] doGetBytes(String url, CloseableHttpClient httpClient) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet = setHeader(httpGet);
        try (CloseableHttpResponse execute = httpClient.execute(httpGet)){
            return EntityUtils.toByteArray(execute.getEntity());
        }
    }
    protected byte[] doGetBytes(String url, boolean isDefault) throws IOException {
        if ( isDefault){
            return doGetBytes(url, httpClient);
        }else {
            try(CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
                return doGetBytes(url, closeableHttpClient);
            }
        }
    }
    protected byte[] doGetBytes(String url) throws IOException {
        return doGetBytes(url, true);
    }


    /**
     *
     * @param url
     * @param isDefault 是否使用默认的httpclient
     * @return
     * @throws IOException
     */
    protected String doGetStr(String url, boolean isDefault) throws IOException {
        if ( isDefault){
            return doGetStr(url, httpClient);
        }else {
            try(CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
                return doGetStr(url, closeableHttpClient);
            }
        }
    }
    protected String doGetStr(String url) throws IOException {
        return doGetStr(url, true);
    }

    protected JsonNode doGetJson(String url) throws IOException {
        return objectMapper.readTree( doGetStr(url, true));
    }
    protected JsonNode doGetJson(String url, boolean isDefault) throws IOException {
        return objectMapper.readTree(doGetStr(url, isDefault));
    }



    protected JsonNode doPostJson(String url, HttpEntity entity) throws IOException {
        return objectMapper.readTree(doPostStr(url, entity, true));
    }

    protected JsonNode doPostJson(String url, HttpEntity entity, boolean isDefault) throws IOException {
        return objectMapper.readTree(doPostStr(url, entity, isDefault));
    }



    protected String doPostStr(String url, HttpEntity entity) throws IOException {
        return doPostStr(url, entity, true);
    }
    protected String doPostStr(String url, HttpEntity entity, boolean isDefault) throws IOException {
        if (isDefault) {
            return doPostStr(url, entity, httpClient);
        }else {
            try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()){
                return doPostStr(url, entity, closeableHttpClient);
            }
        }
    }
    private String doPostStr(String url, HttpEntity entity, CloseableHttpClient httpClient) throws IOException {
        HttpPost httpPost = new HttpPost(url);

        httpPost = setHeader(httpPost);

        httpPost.setEntity(entity);

        try (CloseableHttpResponse execute = httpClient.execute(httpPost);) {
            return EntityUtils.toString(execute.getEntity());
        }
    }


    protected abstract <T extends HttpRequestBase> T setHeader(T base);
}
