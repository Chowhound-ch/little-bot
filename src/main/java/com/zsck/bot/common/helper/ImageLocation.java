package com.zsck.bot.common.helper;

import com.zsck.bot.enums.FileName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

/**
 * @author QQ:825352674
 * @date 2022/8/19 - 19:06
 */
@AllArgsConstructor
@Data
public class ImageLocation {
    FileName fileName;
    File file;
}
