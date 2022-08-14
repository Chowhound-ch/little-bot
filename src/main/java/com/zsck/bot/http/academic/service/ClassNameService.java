package com.zsck.bot.http.academic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsck.bot.http.academic.pojo.ClassMap;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 17:54
 */
public interface ClassNameService extends IService<ClassMap> {
       String getClassName(Integer lessonId);
}
