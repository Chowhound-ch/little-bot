package com.zsck.bot.common.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author QQ:825352674
 * @date 2022/8/13 - 13:34
 */
@Data
@TableName("permit_detail")
public class PermitDetail {
    @TableId
    private Integer id;
    private String qqNumber;
    private Integer permit;

    public PermitDetail(String qqNumber, Integer permit) {
        this.qqNumber = qqNumber;
        this.permit = permit;
    }
}
