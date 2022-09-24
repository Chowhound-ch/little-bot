package com.zsck.bot.http.kugou;

import cn.hutool.http.Header;
import com.fasterxml.jackson.databind.JsonNode;
import com.zsck.bot.http.HttpBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/9/17 - 20:59
 */
@Slf4j
@Component
public class HttpMusicSender extends HttpBase {

    private String remoteUrl;

    @Value("${com.zsck.data.domain}")
    public void setRemoteUrl(String domain){
        remoteUrl ="http://" +  domain + "/music/add/";
    }

    @Deprecated
    public JsonNode sendMusicFile(File file){
        log.info("准备发送文件{}", file.getName());
        HttpEntity httpEntity = MultipartEntityBuilder.create().addBinaryBody("file", file).build();
        try {
            log.info("正在发送文件: {}", file.getAbsolutePath());
            return doPostJson(remoteUrl, httpEntity);
        } catch (IOException e) {
            log.warn("添加音乐文件: {} 失败", file.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }
    public JsonNode sendMusicDetail(String url, String fileName){
        log.info("准备发送文件{}", fileName);
        List<BasicNameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("url", url));
        list.add(new BasicNameValuePair("fileName", fileName));


        try {
            HttpEntity httpEntity = new UrlEncodedFormEntity(list, "UTF-8");

            log.info("正在发送文件: {}", fileName);
            return doPostJson(remoteUrl, httpEntity);
        } catch (IOException e) {
            log.warn("添加音乐文件: {} 失败", fileName);
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected <T extends HttpRequestBase> T setHeader(T base) {
        base.setHeader(Header.USER_AGENT.getValue(), Header.USER_AGENT.getValue());
        return base;
    }
}
