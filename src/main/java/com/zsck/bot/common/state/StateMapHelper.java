package com.zsck.bot.common.state;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zsck.bot.common.state.enums.GroupStateEnum;
import com.zsck.bot.common.state.pojo.GroupState;
import com.zsck.bot.common.state.service.GroupStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author QQ:825352674
 * @date 2022/9/3 - 13:31
 */
@Component
public class StateMapHelper {
    @Autowired
    private GroupStateService groupStateService;
    private final Map<String, GroupStateEnum> groupStateMap = new HashMap<>();

    @PostConstruct
    private void init(){
        List<GroupState> list = groupStateService.list();
        list.forEach( state -> groupStateMap.put(state.getGroupNumber()
                , GroupStateEnum.getInstance(state.getState())));
    }

    /**
     * 获取群组状态
     */
    public GroupStateEnum getState(String group){
        if (groupStateMap.get(group) == null) {
            GroupState groupState = new GroupState(group, GroupStateEnum.CLOSED.getValue());
            groupStateService.save(groupState);
            return GroupStateEnum.CLOSED;
        }
        return groupStateMap.get(group);
    }

    /**
     * 返回值为false则des 和 used 群组状态相同
     */
    public Boolean setState(String group, GroupStateEnum stateEnum){
        if ( getState(group) == stateEnum ){
            return false;
        }
        LambdaUpdateWrapper<GroupState> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(GroupState::getGroupNumber, group).set(GroupState::getState, stateEnum.getValue());
        if (groupStateService.update(wrapper)){
            groupStateMap.put(group, stateEnum);
            return true;
        }
        return false;
    }
}
