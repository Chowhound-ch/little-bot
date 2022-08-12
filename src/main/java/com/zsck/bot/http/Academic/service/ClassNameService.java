package com.zsck.bot.http.Academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsck.bot.http.Academic.pojo.ClassMap;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 17:54
 */
public interface ClassNameService extends IService<ClassMap> {
//    Integer keepDate(Map<Integer , String> map);
       String getClassName(Integer lessonId);
//    Integer deleteAll();
}
