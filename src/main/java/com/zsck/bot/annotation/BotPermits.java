package com.zsck.bot.annotation;

import com.zsck.bot.enums.Permit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author QQ:825352674
 * @date 2022/7/15 - 0:03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotPermits {
    /**
     * 控制权限
     */
    Permit value();
}
