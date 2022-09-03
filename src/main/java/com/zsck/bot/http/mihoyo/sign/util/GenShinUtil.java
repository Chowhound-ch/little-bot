package com.zsck.bot.http.mihoyo.sign.util;

import com.alibaba.fastjson.JSONObject;
import com.zsck.bot.http.mihoyo.sign.SignConstant;
import com.zsck.bot.http.mihoyo.sign.exception.GenShinCookieException;
import com.zsck.bot.http.mihoyo.sign.pojo.GenshinInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/8/31 - 22:10
 */
@Slf4j
public class GenShinUtil {

    public static void checkCookie(String cookie) {
        JSONObject result = HttpUtil.doGetJson(String.format("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=%s", "hk4e_cn")
                , HeadersUtil.getBasicHeaders(cookie));

        int retcode = result.getInteger("retcode");
        if (retcode == SignConstant.RETCODE3) {
            log.info("cookie正常,ret_code: {}, res: {}", retcode, result);
        } else {
            log.warn("cookie错误,ret_code: {}, res: {}", retcode, result);
            throw new GenShinCookieException( "你还没有登陆米游社哦，这个cookie是无效的");
        }
    }
    public static Map<String, Object> getSignDataMap(GenshinInfo info){
        Map<String, Object> data = new HashMap<>(3);
        data.put("act_id", SignConstant.ACT_ID);
        data.put("region", info.getServerType().getValue());
        data.put("uid", info.getUid());
        return data;
    }

    public static String analyzeRet(Integer ret){
        if (ret.equals(SignConstant.RETCODE1)) {
            return "cookie错误或者你还没有登陆米游社哦，这个cookie是无效的";
        }
        if (ret.equals(SignConstant.RETCODE2)) {
            return "你今天已经签到过了哦，明天在来吧！";
        }
        if (ret.equals(SignConstant.RETCODE3)) {
            return "签到成功";
        }else {
            return  "未知错误，错误码:"+ret;
        }
    }
}
