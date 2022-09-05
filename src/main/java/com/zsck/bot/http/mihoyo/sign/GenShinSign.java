package com.zsck.bot.http.mihoyo.sign;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsck.bot.http.mihoyo.sign.exception.GenShinCookieException;
import com.zsck.bot.http.mihoyo.sign.pojo.GenshinInfo;
import com.zsck.bot.http.mihoyo.sign.service.GenshinService;
import com.zsck.bot.http.mihoyo.sign.util.GenShinUtil;
import com.zsck.bot.http.mihoyo.sign.util.HeadersUtil;
import com.zsck.bot.http.mihoyo.sign.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class GenShinSign {

    @Autowired
    private GenshinService genshinService;
    /**
     * 获取cookie对应的Uid,获取对应信息
     */
    public List<GenshinInfo> analyzeCookie(String cookie) {
        JSONObject result = HttpUtil.doGetJson(String.format("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=%s", "hk4e_cn"), HeadersUtil.getBasicHeaders(cookie));
        JSONArray resArr = result.getJSONObject("data").getJSONArray("list");

        List<GenshinInfo> infoList = new ArrayList<>();
        resArr.forEach( res ->{
            JSONObject jsonObject = (JSONObject) res;
            GenshinInfo info = new GenshinInfo();

            String uid = jsonObject.getString("game_uid");
            String nickname = jsonObject.getString("nickname");
            info.setCookie(cookie);
            info.setUid(uid);
            info.setNickName(nickname);

            log.info("cookie对应的uid：{}" , uid);
            log.info("cookie对应的昵称：{}" , nickname);
            infoList.add(info);
        });

        return infoList;
    }


    /**
     * 签到
     */
    public String doSign(String uid) {
        LambdaQueryWrapper<GenshinInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenshinInfo::getUid, uid);
        GenshinInfo genshinInfo = genshinService.getOne(wrapper);
        return doSign(genshinInfo);
    }
    public String doSign(GenshinInfo info) {
        StringBuffer buffer = new StringBuffer();
        try {
            GenShinUtil.checkCookie(info.getCookie());//检查cookie

            Map<String, Object> data = GenShinUtil.getSignDataMap(info);
            JSONObject signResult = HttpUtil.doPostJson(SignConstant.SIGN_URL, HeadersUtil.getHeaders(info.getCookie()), data);

            log.info("签到uid:{} 结果:{}", info.getUid(), signResult);
            buffer.append("uid:").append(info.getUid()).append("\n昵称:").append(info.getNickName()).append("\n签到结果:")
                    .append(GenShinUtil.analyzeRet(signResult.getInteger("ret") == null? signResult.getInteger("retcode") : signResult.getInteger("ret")));
            return buffer.toString();
        }catch (GenShinCookieException e){
            return e.getMessage();
        }
    }

    /**
     * 奖励列表
     * TODO : 待处理
     */
    public void signList(String uid) {
        LambdaQueryWrapper<GenshinInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenshinInfo::getUid, uid);
        GenshinInfo genshinInfo = genshinService.getOne(wrapper);
        signList(genshinInfo);
    }
    public void signList(GenshinInfo info) {

        Map<String, Object> data = GenShinUtil.getSignDataMap(info);

        JSONObject signInfoResult = HttpUtil.doGetJson(SignConstant.INFO_URL, HeadersUtil.getHeaders(info.getCookie()), data);

        JSONObject listInfoResult = HttpUtil.doGetJson(SignConstant.LIST_URL, HeadersUtil.getHeaders(info.getCookie()), data);

        Optional<JSONObject> jsonData = Optional.ofNullable(listInfoResult);
        JSONObject data2 = listInfoResult.getJSONObject("data");
        int totalSignDay = data2.getInteger("total_sign_day") - 1;


        String awards = data2.getString("awards");
        JSONArray jsonArray = JSONArray.parseArray(awards);

        String itemName = jsonArray.getJSONObject(totalSignDay).getString("name");
        int itemCnt = jsonArray.getJSONObject(totalSignDay).getInteger("cnt");


        System.out.println(jsonArray.getJSONObject(totalSignDay).getString("name"));
        System.out.println(jsonArray.getJSONObject(totalSignDay).getInteger("cnt"));

        String img = jsonArray.getJSONObject(totalSignDay).getString("icon");

        log.info("今天获取的奖励是:" + itemName + "X" + itemCnt);
        log.info(img);
    }

}
