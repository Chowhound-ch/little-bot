package com.zsck.bot.common.permit.enums;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.util.EnumUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/8/12 - 23:35
 */
public enum Permit {
    HOST(3),
    MANAGER(2),
    MEMBER(1);

    private final Integer value;
    private static Map<String , Permit> nameMap;
    private final static Map<Integer , Permit> valueMap;
    static {
        Permit.nameMap = EnumUtil.getEnumMap(Permit.class);
        valueMap = new HashMap<>();
        nameMap.forEach((key, value)->{
            Permit pe = EnumUtil.getBy((Func1<Permit, Integer>) permit -> permit.value, value.value);
            valueMap.put(value.value, pe);
        });
    }
    Permit(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
    public static Permit getEnumByValue(Integer value){
        return valueMap.get(value);
    }
    public static Permit getEnumByName(String name){
        return nameMap.get(name);
    }
}
