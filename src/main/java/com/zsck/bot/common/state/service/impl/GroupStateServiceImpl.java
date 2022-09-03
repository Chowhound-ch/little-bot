package com.zsck.bot.common.state.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsck.bot.common.state.pojo.GroupState;
import com.zsck.bot.common.state.service.GroupStateService;
import com.zsck.bot.common.state.mapper.GroupStateMapper;
import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【group_state】的数据库操作Service实现
* @createDate 2022-09-03 13:00:13
*/
@Service
public class GroupStateServiceImpl extends ServiceImpl<GroupStateMapper, GroupState>
    implements GroupStateService{

}




