package com.zsck.bot.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsck.bot.common.pojo.PermitDetail;
import com.zsck.bot.common.service.PermitDetailService;
import com.zsck.bot.enums.Permit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;

/**
 * 启动时检查
 * @author QQ:825352674
 * @date 2022/8/13 - 19:01
 */
@Slf4j
@Configuration
public class CheckHostPermit {
    @Value("${com.zsck.data.host}")
    private String hostNumber;
    @Autowired
    private PermitDetailService permitDetailService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    private void init(){
        LambdaQueryWrapper<PermitDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermitDetail::getQqNumber, hostNumber);
        PermitDetail detail = permitDetailService.getOne(wrapper);
        if (detail == null){
            detail = new PermitDetail(hostNumber, Permit.MEMBER.getValue());
        }
        if (Permit.getEnumByValue(detail.getPermit()) != Permit.HOST){
            detail.setPermit(Permit.HOST.getValue());
            permitDetailService.saveOrUpdate(detail);
            log.warn("检测到bot所有者[{}]权限非 HOST,已自动修正", hostNumber);
        }
    }
}
