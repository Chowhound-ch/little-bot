package com.zsck.bot.http.kugou;

import cn.hutool.http.Header;
import com.zsck.bot.http.HttpBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

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

    public void sendMusicFile(File file){
        HttpEntity httpEntity = MultipartEntityBuilder.create().addBinaryBody("file", file).build();
        try {
            doPostStr(remoteUrl, httpEntity);
        } catch (IOException e) {
            log.warn("添加mp3文件: {} 失败", file.getAbsolutePath());
            e.printStackTrace();
        }
    }


    @Override
    protected <T extends HttpRequestBase> T setHeader(T base) {
        base.setHeader(Header.USER_AGENT.getValue(), Header.USER_AGENT.getValue());
        return base;
    }
}
