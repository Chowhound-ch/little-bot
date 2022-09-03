package com.zsck.bot.common.state.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/9/3 - 13:37
 */
public enum GroupStateEnum {
    /**
     * 关机
     */
    CLOSED(0),
    /**
     * 开启全部功能
     */
    ALL(1);

    private final Integer value;

    private static final Map<Integer, GroupStateEnum> map = new HashMap<>();

    static {
        GroupStateEnum[] values = GroupStateEnum.values();
        for (GroupStateEnum groupStateEnum : values){
            map.put(groupStateEnum.value, groupStateEnum);
        }
    }

    GroupStateEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static GroupStateEnum getInstance(Integer code){
        return map.get(code) == null ? CLOSED : map.get(code);
    }
}
