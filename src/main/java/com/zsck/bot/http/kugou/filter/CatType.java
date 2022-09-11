package com.zsck.bot.http.kugou.filter;

/**
 * @author QQ:825352674
 * @date 2022/9/11 - 17:52
 */
public enum CatType {
    AT("at"),
    FILE("file");

    final String type;

    CatType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
