package com.zsck.bot.http.kugou.filter;

import catcode.CatCodeUtil;
import love.forte.simbot.api.message.events.MessageGet;
import love.forte.simbot.filter.FilterData;
import love.forte.simbot.filter.ListenerFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * @author QQ:825352674
 * @date 2022/9/11 - 17:52
 */
@Component
public class CatFilter implements ListenerFilter {
    private CatCodeUtil codeUtil = CatCodeUtil.INSTANCE;

    @Override
    public boolean test(@NotNull FilterData data) {
        String text = ((MessageGet)data.getMsgGet()).getMsg();
        String start = text.split(",")[0];
        if (!start.startsWith("[CAT:")){
            return false;
        }
        if (start.substring("[CAT:".length()).equals("file")) {
            return true;
        }
        return false;
    }
}
