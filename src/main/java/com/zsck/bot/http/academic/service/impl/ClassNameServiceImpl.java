package com.zsck.bot.http.academic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsck.bot.mybatis.mapper.ClassMapMapper;
import com.zsck.bot.http.academic.pojo.ClassMap;
import com.zsck.bot.http.academic.service.ClassNameService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/7/23 - 15:52
 */
@CacheConfig(cacheNames = "detail")
@Service
public class ClassNameServiceImpl extends ServiceImpl<ClassMapMapper , ClassMap>
        implements ClassNameService {
    private Map<Integer , String> classNameMap;

    @PostConstruct
    private void init(){
        classNameMap = new HashMap<>();
        List<Map<String , Object>> list = baseMapper.selectMaps(null);
        list.forEach(el -> classNameMap.put(((Integer) el.get("id")), ((String) el.get("class_name"))));
    }


    @Override
    public String getClassName(Integer lessonId) {
        return classNameMap.get(lessonId);
    }
//    @Autowired
//    private ClassMapMapper classMapMapper;


//
//    @Override
//    public Integer keepDate(Map<Integer, String> map) {
//        return classMapMapper.keepAllMap(map);
//    }
//
//    @Cacheable(key = "#lessonId")
//    public String getClassName(Integer lessonId){
//        return classNameMap.get(lessonId);
//    }
//    @CacheEvict(allEntries = true)
//    public Integer deleteAll(){
//        classNameMap = null;
//        init();
//        return classMapMapper.deleteAll();
//    }

}
