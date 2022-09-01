package com.zsck.bot.http.mihoyo.sign;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Random;

/**
 * @author zeng
 * @date 2022/8/18 14:08
 * @user 86188
 */
public class HeaderParams {

    /**
     * 生成随机的字符串作为DS的一部分
     *
     * @return 返回生成的字符串
     */
    public static String getRandomStr() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            String constants = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int number = random.nextInt(constants.length());
            char charAt = constants.charAt(number);
            sb.append(charAt);
        }
        return sb.toString();
    }

    /**
     * @param n 参数1
     * @param i 参数2
     * @param r 参数3
     * @return 返回创建的DS
     */
    private static String createDS(String n, String i, String r) {
        String c = DigestUtils.md5Hex("salt=" + n + "&t=" + i + "&r=" + r);
        return String.format("%s,%s,%s", i, r, c);
    }

    /**
     * 最终生成的DS
     *
     * @return 返回能加入Headers中的DS
     */
    public static String getDS() {
        String i = (System.currentTimeMillis() / 1000) + "";
        String r = getRandomStr();
        return createDS("z8DRIUjNDT7IT5IZXvrUAxyupA1peND9", i, r);

    }
}
