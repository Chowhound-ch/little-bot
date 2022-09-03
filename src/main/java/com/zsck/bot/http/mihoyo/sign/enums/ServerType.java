package com.zsck.bot.http.mihoyo.sign.enums;

import com.zsck.bot.http.mihoyo.sign.SignConstant;

/**
 * @author QQ:825352674
 * @date 2022/9/2 - 16:38
 */
public enum ServerType {

    /**
     * 官服
     */
    OFFICIAL(SignConstant.REGION),

    /**
     * B服
     */
    FOREIGN(SignConstant.REGION2);

    String value;

    ServerType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
