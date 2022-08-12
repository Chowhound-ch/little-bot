package com.zsck.bot.http.kugou;

import cn.hutool.core.net.URLEncodeUtil;
import com.sun.jndi.toolkit.url.Uri;
import com.sun.org.apache.xerces.internal.util.URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/8/11 - 19:40
 */
public class MD5Encoding {
    public static String MD5(String a){
//        String time = "1600305065609";
//        String url = "NVPh5oo715z5DIWAeQlhMDsWXXQV4hwtbitrate=0callback=callback123clienttime="+time+"clientver=2000dfid=-inputtype=0iscorrection=1isfuzzy=0keyword="+a+"mid="+time+"page=1pagesize=30platform=WebFilterprivilege_filter=0srcappid=2919tag=emuserid=-1uuid="+time+"NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt";
        String urlEn = "NVPh5oo715z5DIWAeQlhMDsWXXQV4hwtbitrate=0callback=callback123clienttime=1600305065609clientver=2000dfid=-inputtype=0iscorrection=1isfuzzy=0keyword="+a+"mid=1600305065609page=1pagesize=30platform=WebFilterprivilege_filter=0srcappid=2919tag=emuserid=-1uuid=1600305065609NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt";
        return o(urlEn);
    }

    /**
     * js和java中>>>效果并不一样，故以此替代js中的 >>>
     * js : from >>> num  ===  replace( from, num)
     * @param from
     * @param num
     * @return
     */
    private static Long replace(Long from, Long num){
        if (from < 0){
            from += 4294967296L;
        }
        return from >> num;
    }

    /**
     * ~ 按位取反
     * @param from
     * @return
     */
    private static Long reverse(Long from){
        return from ^ 4294967295L;
    }

    /**
     * & 与
     * @param
     * @return
     */
    private static Integer and(Long left, Long right){
        return (int) (left & right);
    }
    private static String b(Long a){
        a = replace(a, 0L);
        String b = Long.toString(a, 16);
        return "00000000".substring(0, 8 - b.length()) + b;
    }
    private static Long[] d(Long a){
        Long[] b = new Long[8];
        for (int c = 0; c < 8; c++){
            b[c] = 255 & a;
            a = replace(a, 8L);
        }
        return b;
    }
    private static Long e(Long a, Long b){
        return ((a << b) & 4294967295L) | replace(a, (32 - b));
    }
    private static Long f(Long a, Long b, Long c){
        return and(a, b) | (reverse(a) & c);
    }
    private static Long g(Long a, Long b, Long c){
        return (int)(c & a) | (reverse(c) & b);
    }
    private static Long h(Long a, Long b, Long c){
        return a ^ b ^ c;
    }
    private static Long i(Long a, Long b, Long c){
        return b ^ (a | reverse(c));
    }
    private static Long j(List<Long> a, Long b){
        Integer dex = b.intValue();
        return (a.get(dex + 3) << 24) | (a.get(dex + 2) << 16) | (a.get(dex+ 1) << 8) | a.get(dex);
    }
    private static List<Long> k(String a){
        List<Long> b = new ArrayList<>();
        for (Integer c = 0; c < a.length() ; c++) {
            if (a.codePointAt(c) <= 127) {
                b.add(Integer.valueOf(a.codePointAt(c)).longValue());
            }
            else {
                String[] d = URLEncodeUtil.encode(String.valueOf(a.charAt(c))).substring(1).split("%");

                for (Integer e = 0; e < d.length; e++) {
                    b.add(Long.parseLong(d[e] , 16));
                }
            }
        }
        return b;
    }
    private static String l(Long...arguments){
        String a = "";
        for (Long c = 0L, d = 0L, e = 3L; e >= 0L; e--) {
            d = arguments[e.intValue()];
            c = 255 & d;
            d= replace(d, 8L);
            c <<= 8;
            c |= 255 & d;
            d= replace(d, 8L);
            c <<= 8;
            c |= 255 & d;
            d= replace(d, 8L);
            c <<= 8;
            c |= d;
            a += b(c);
        }
        return a;
    }
    private static Long n(Long a, Long b){
        return Integer.valueOf(and(4294967295L, (a + b))).longValue() ;
    }

    /**
     * 0 - 3  v, u, t, s;
     * @param temp
     * @param a
     * @param b
     * @param c
     * @param d
     */
    private static void a(List<Long> temp ,Long a, Long b, Long c, Long d){
        Long f = temp.get(0);
        temp.set(0, temp.get(1));
        temp.set(1, temp.get(2));
        temp.set(2, n(temp.get(2), e(n(temp.get(3), n(a, n(b, c))), d)));
        temp.set(3, f);
    }

    /**
     * @param a 待加密字符串
     * @return
     */
    private static String o(String a){
        List<Long> p = k(a);
        Long b = Integer.valueOf(p.size()).longValue();
        p.add(128L);
        Long c = p.size() % 64L;
        if (c > 56) {
            for (Integer k = 0; 64 - c > k; k++) {
                p.add(0L);
            }
            c = p.size() % 64L;
        }
        for (Integer k = 0; 56 - c > k; k++) {
            p.add(0L);
        }
        p.addAll(Arrays.asList(d(8 * b)));
        Long m = 1732584193L,
                o = 4023233417L,
                q = 2562383102L,
                r = 271733878L;
        Long v = 0L, u = 0L, t = 0L, s = 0L;
        List<Long> temp = Arrays.asList(v, u, t, s);
        for (Long k = 0L; k < p.size() / 64; k++) {
            temp.set(3, m);
            temp.set(2, o);
            temp.set(1, q);
            temp.set(0, r);
            Long w = 64 * k;
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 3614090360L, j(p, w), 7L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 3905402710L, j(p,  w + 4), 12L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 606105819L, j(p,   w + 8), 17L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 3250441966L, j(p, w + 12), 22L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 4118548399L, j(p , w + 16), 7L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 1200080426L, j(p, w + 20), 12L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 2821735955L, j(p, w + 24), 17L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 4249261313L, j(p, w + 28), 22L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 1770035416L, j( p, w + 32), 7L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 2336552879L, j(p, w + 36), 12L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 4294925233L, j(p, w + 40), 17L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 2304563134L, j(p, w + 44), 22L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 1804603682L, j (p, w + 48), 7L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 4254626195L, j(p, w + 52), 12L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 2792965006L, j(p, w + 56), 17L);
            a(temp, f(temp.get(2), temp.get(1), temp.get(0)), 1236535329L, j(p, w + 60), 22L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 4129170786L,   j(p, w + 4), 5L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 3225465664L,  j(p, w + 24), 9L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 643717713L,  j(p, w + 44), 14L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 3921069994L,         j(p, w), 20L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 3593408605L,  j(p, w + 20), 5L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 38016083L,    j(p, w + 40), 9L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 3634488961L, j(p, w + 60), 14L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 3889429448L, j(p, w + 16), 20L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 568446438L,   j(p, w + 36), 5L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 3275163606L,  j(p, w + 56), 9L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 4107603335L, j(p, w + 12), 14L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 1163531501L, j(p, w + 32), 20L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 2850285829L,  j(p, w + 52), 5L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 4243563512L,   j(p, w + 8), 9L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 1735328473L, j(p, w + 28), 14L);
            a(temp, g(temp.get(2), temp.get(1), temp.get(0)), 2368359562L, j(p, w + 48), 20L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 4294588738L,  j(p, w + 20), 4L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 2272392833L, j(p, w + 32), 11L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 1839030562L, j(p, w + 44), 16L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 4259657740L, j(p, w + 56), 23L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 2763975236L,   j(p, w + 4), 4L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 1272893353L, j(p, w + 16), 11L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 4139469664L, j(p, w + 28), 16L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 3200236656L, j(p, w + 40), 23L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 681279174L,   j(p, w + 52), 4L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 3936430074L,         j(p, w), 11L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 3572445317L, j(p, w + 12), 16L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 76029189L,   j(p, w + 24), 23L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 3654602809L,  j(p, w + 36), 4L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 3873151461L, j(p, w + 48), 11L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 530742520L,  j(p, w + 60), 16L);
            a(temp, h(temp.get(2), temp.get(1), temp.get(0)), 3299628645L,  j(p, w + 8), 23L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 4096336452L,          j(p, w), 6L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 1126891415L, j(p, w + 28), 10L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 2878612391L, j(p, w + 56), 15L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 4237533241L, j(p, w + 20), 21L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 1700485571L,  j(p, w + 48), 6L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 2399980690L, j(p, w + 12), 10L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 4293915773L, j(p, w + 40), 15L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 2240044497L,  j(p, w + 4), 21L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 1873313359L,  j(p, w + 32), 6L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 4264355552L, j(p, w + 60), 10L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 2734768916L, j(p, w + 24), 15L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 1309151649L, j(p, w + 52), 21L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 4149444226L,  j(p, w + 16), 6L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 3174756917L, j(p, w + 44), 10L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 718787259L,   j(p, w + 8), 15L);
            a(temp, i(temp.get(2), temp.get(1), temp.get(0)), 3951481745L, j(p, w + 36), 21L);
            m = n(m, temp.get(3));
            o = n(o, temp.get(2));
            q = n(q, temp.get(1));
            r = n(r, temp.get(0));
        }
        return l(r, q, o, m).toUpperCase();
    }
}
