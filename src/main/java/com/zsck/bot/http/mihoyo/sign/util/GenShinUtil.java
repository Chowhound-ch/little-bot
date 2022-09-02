package com.zsck.bot.http.mihoyo.sign.util;

import com.alibaba.fastjson.JSONObject;
import com.zsck.bot.http.mihoyo.sign.SignConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/8/31 - 22:10
 */
@Slf4j
public class GenShinUtil {

    public static String checkCookie(String cookie) {
        JSONObject result = HttpUtil.doGetJson(String.format("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=%s", "hk4e_cn")
                , HeadersUtil.getBasicHeaders(cookie));

        int retcode = result.getInteger("retcode");
        if (retcode == SignConstant.RETCODE3) {
            log.info("cookie正常,ret_code: {}, res: {}", retcode, result);
            return "cookie正常";
        } else {
            log.warn("cookie错误,ret_code: {}, res: {}", retcode, result);
            return "";
        }
    }
    public static Map<String, Object> getSignDataMap(String uid){
        Map<String, Object> data = new HashMap<>(3);
        data.put("act_id", SignConstant.ACT_ID);
        data.put("region", SignConstant.REGION);
        data.put("uid", uid);
        return data;
    }
    public static String checkRet(Integer ret){
        if (ret == SignConstant.RETCODE1) {
            return "cookie错误或者你还没有登陆米游社哦，这个cookie是无效的";
        }
        if (ret == SignConstant.RETCODE2) {
            return "你今天已经签到过了哦，明天在来吧！";
        }
        if (ret == SignConstant.RETCODE3) {
            return "签到成功";
        }else {
            return  "未知错误，错误码:"+ret;
        }
    }
}
