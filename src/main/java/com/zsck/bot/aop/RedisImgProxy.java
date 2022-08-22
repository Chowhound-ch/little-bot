package com.zsck.bot.aop;

import com.zsck.bot.aop.exception.AopException;
import com.zsck.bot.aop.exception.AopParamTypeException;
import com.zsck.bot.common.helper.ImageHelper;
import com.zsck.bot.enums.FileName;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author QQ:825352674
 * @date 2022/8/19 - 13:16
 */
@Slf4j
@Aspect
@ConditionalOnProperty(prefix = "com.zsck.cache", value = "enable-redis-cache-img", havingValue = "true")
@Component
public class RedisImgProxy implements CommandLineRunner {
    private String integerNameType;
    @Value("${com.zsck.cache.max-cached-image}")
    private Integer maxCached;
    @Autowired
    private RedisTemplate<String , Object> redisTemplate;


    public void init(){
        integerNameType = Integer.class.getTypeName();
        List<byte[]> img = ImageHelper.getRandomImg(maxCached);
        log.info("使用redis进行缓存,数量:{}", maxCached );
        for (byte[] file: img) {
            redisTemplate.opsForList().leftPush("image", file);
        }
        log.info("redis缓存图片完毕");
    }
    @Pointcut("execution(* com.zsck.bot.common.helper.ImageHelper.getImageContext(..))")
    public void pointcut(){
    }
    @Before("pointcut()")
    public void before(){
        log.info("开始从redis获取缓存数据");
    }
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
        Integer num = null, index = 0;//需要发送图片的数量
        for (Integer i = 0 ; i < args.length; i++){
            String typeName = args[i].getClass().getTypeName();
            if (typeName.equals(integerNameType)){
                num = ((Integer) args[i]);
                index = i;
            }else {
                throw new AopParamTypeException("错误的参数类型:" + typeName);
            }
        }

        if (num == null){
            throw new AopException("参数不能为空");
        }

        List<byte[]> list = new ArrayList<>();
        List<Object> objects = redisTemplate.opsForList().leftPop("image", num);
        for (Object o : objects){
            if (o != null){
                list.add(((byte[]) o));
                num--;
            }
        }
        log.info("发送图片: {}, 其中从本地读取: {}", args[index], num);
        args[index] = num;
        try {
            list.addAll((List<byte[]>) joinPoint.proceed(args));
            return list;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run(String... args) throws Exception {
        init();
    }
}
