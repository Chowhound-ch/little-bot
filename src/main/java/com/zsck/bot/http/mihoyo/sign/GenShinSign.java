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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GenShinSign {

    @Autowired
    private GenshinService genshinService;
    /**
     * 获取cookie对应的Uid,获取对应信息
     */
    public GenshinInfo checkUid(String cookie) {
        GenshinInfo genshinInfo = new GenshinInfo();
        JSONObject result = HttpUtil.doGetJson(String.format("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=%s", "hk4e_cn"), HeadersUtil.getBasicHeaders(cookie));
        String uid = (String) result.getJSONObject("data").getJSONArray("list").getJSONObject(0).get("game_uid");
        String nickname = (String) result.getJSONObject("data").getJSONArray("list").getJSONObject(0).get("nickname");

        log.info("cookie对应的uid：{}" , uid);
        log.info("cookie对应的昵称：{}" , nickname);
        genshinInfo.setCookie(cookie);
        genshinInfo.setUid(uid);
        genshinInfo.setNickName(nickname);

        return genshinInfo;
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
        try {
            GenShinUtil.checkCookie(info.getCookie());
            Map<String, Object> data = GenShinUtil.getSignDataMap(info.getUid());
            JSONObject signResult = HttpUtil.doPostJson(SignConstant.SIGN_URL, HeadersUtil.getHeaders(info.getCookie()), data);

            log.info("签到uid:{} 结果:{}", info.getUid(), signResult);
            return GenShinUtil.checkRet(signResult.getInteger("ret"));
        }catch (GenShinCookieException e){
            return "cookie无效";
        }
    }

    /**
     * 奖励列表
     *
     */
    public void signList(GenshinInfo info) {

        Map<String, Object> data = new HashMap<>(3);
        data.put("act_id", SignConstant.ACT_ID);
        data.put("region", SignConstant.REGION);
        data.put("uid", info.getUid());

        JSONObject signInfoResult = HttpUtil.doGetJson(SignConstant.INFO_URL, HeadersUtil.getHeaders(""), data);//TODO cookie

        JSONObject listInfoResult = HttpUtil.doGetJson(SignConstant.LIST_URL, HeadersUtil.getHeaders(info.getCookie()), data);

        int totalSignDay = signInfoResult.getJSONObject("data").getInteger("total_sign_day") - 1;

        JSONObject data2 = listInfoResult.getJSONObject("data");
        String awards = data2.getString("awards");
        JSONArray jsonArray = new JSONArray();
       // jsonArray = new JSONArray(awards);//TODO

        String itemName = jsonArray.getJSONObject(totalSignDay).getString("name");
        int itemCnt = jsonArray.getJSONObject(totalSignDay).getInteger("cnt");


        System.out.println(jsonArray.getJSONObject(totalSignDay).getString("name"));
        System.out.println(jsonArray.getJSONObject(totalSignDay).getInteger("cnt"));

        String img = jsonArray.getJSONObject(totalSignDay).getString("icon");

//        setItemMsg("今天获取的奖励是:" + itemName + "X" + itemCnt);
//        setItemImg(img);
        //return isSign;
    }

}
