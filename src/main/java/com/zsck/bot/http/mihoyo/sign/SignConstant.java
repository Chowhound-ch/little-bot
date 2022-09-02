package com.zsck.bot.http.mihoyo.sign;

/**
 * @author zeng
 * @date 2022/8/18 13:41
 * @user 86188
 */
public class SignConstant {
    /**
     * 切勿乱修改
     * 行动id
     */
    public static final String ACT_ID = "e202009291139501";

    /**
     * 切勿乱修改
     * 米游社版本
     */
    public static final String APP_VERSION = "2.35.2";

    /**
     * 服务id
     * 切勿乱修改
     * 官服服务id
     */
    public static final String REGION = "cn_gf01";

    /**
     * B服服务id
     */
    public static final String REGION2 = "cn_qd01";

    /**
     * Referer网址
     */
    public static final String REFERER_URL = String.format("https://webstatic.mihoyo.com/bbs/event/signin-ys/index.html?bbs_auth_required=%s&act_id=%s&utm_source=%s&utm_medium=%s&utm_campaign=%s", true, ACT_ID, "bbs", "mys", "icon");

    /**
     * 米游社签到链接
     */
    public static final String SIGN_URL = "https://api-takumi.mihoyo.com/event/bbs_sign_reward/sign";

    /**
     * 米游社主页链接
     */
    public static final String HOME_URL = "https://bbs-api.mihoyo.com/user/wapi/getUserFullInfo?gids=2";

    public static final String INFO_URL = "https://api-takumi.mihoyo.com/event/bbs_sign_reward/info";

    public static final String LIST_URL = "https://api-takumi.mihoyo.com/event/bbs_sign_reward/home";


    /**
     * 错误或未登录
     */
    public static final Integer RETCODE1 = -100;
    /**
     * 已经签到过了
     */
    public static final Integer RETCODE2 = -5003;
    /**
     * 签到成功
     */
    public static final Integer RETCODE3 = 0;

    /**
     * 无量姬原神帮助指令
     */
    public static final String GENSHIN_HELP =
            "[无量姬的原神帮助]\n" +
                    "1.【抽卡分析教程】:https://mp.weixin.qq.com/s/WcH6DgBRoAwbnmOlGTJBNg\n" +
                    "   ”原神抽卡分析 你的抽卡链接“" +
                    "2.【获取签到cookie教程】:https://docs.qq.com/doc/DR0R6SndxcElNck9U\n" +
                    "   米游社原神主页:https://bbs.mihoyo.com/ys/\n" +
                    "   获取cookie的指令:javascript:(function(){prompt(document.domain,document.cookie)})();\n" +
                    "   复制粘贴的话浏览器会自动删掉前面的“javascript:”,记得手动补上哦~\n\n" +
                    "3.【米游社自动签到】:\n" +
                    "   ①绑定cookie:\n    “绑定 你的cookie”\n" +
                    "   ②关闭/打开推送:\n    “@无量姬 打开/关闭推送”\n" +
                    "       打开/关闭每天原神米游社自动签到时的消息推送\n" +
                    "   ③原神签到:\n    “原神签到 你的UID”\n" +
                    "   ④删除存储的cookie:\n    “删除cookie 你的UID”\n" +
                    "       只有与UID绑定的QQ号才能在二次确认后删除UID";
}
