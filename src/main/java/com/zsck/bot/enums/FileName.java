package com.zsck.bot.enums;

/**
 * @author QQ:825352674
 * @date 2022/7/29 - 12:06
 */
public enum FileName {
    RANDOM("random/"),
    ANI("ani/"),
    WAL("wal/"),
    RAN("ran/"),
    SKY("sky/"),
    SE("se/"),
    PC("pc/"),
    YIN("yin/"),
    PAPER("paper/");
    private final String value;

    FileName(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
