package com.zsck.bot.common.helper;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.sun.xml.internal.bind.v2.TODO;
import com.zsck.bot.enums.FileName;
import com.zsck.bot.util.ContextUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

/**
 * @author QQ:825352674
 * @date 2022/7/29 - 12:45
 */
@DependsOn("contextUtil")
@Slf4j
@Component
public class ImageHelper implements CommandLineRunner {
    private Bot bot;
    private final static Random random = new Random(Instant.now().getNano());
    private MessageContentBuilderFactory messageContentBuilderFactory;
    private MiraiMessageContentBuilderFactory forwardMessage;
    private static String filePath;
    private final static FileName[] fileNames = FileName.values();//所有图片目录枚举
    private static Map<FileName, File> fileMap;
    private ImageHelper imageHelper;

    @Value("${com.zsck.data.file-path}")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public ImageHelper(@NotNull BotManager botManager , MessageContentBuilderFactory factory) {
        bot = botManager.getDefaultBot();
        messageContentBuilderFactory = factory;
        forwardMessage = ContextUtil.getForwardBuilderFactory();
    }
    @PostConstruct
    private void init(){
        fileMap = new HashMap<>(fileNames.length);
        for (FileName name : fileNames){
            fileMap.put(name, new File(filePath + name));
        }
    }
    public void checkAndGetImagesMsg(GroupMsg groupMsg){
        String msg = groupMsg.getText();
        MsgSenderHelper senderHelper = MsgSenderHelper.getInstance(groupMsg, bot.getSender());
        Integer num = getNum(msg);
        if (num > 50) {
            senderHelper.senderMsg("请控制单次请求数量在1-50范围内");
            return ;
        }else if(num >= 10) {
            senderHelper.senderMsg("单次请求的图片较多，请耐心等待");
        }
        senderHelper.senderMsg(getImages(num ).build());
    }

    public MiraiMessageContentBuilder getImages(Integer num){
        List<byte[]> list = imageHelper.getImageContext(num);
        return getImages(list);
    }
    private MiraiMessageContentBuilder getImages(List<byte[]> list){
        MiraiMessageContentBuilder builder = forwardMessage.getMessageContentBuilder();
        builder.forwardMessage( (fun)->{
            list.forEach( image ->{
                MessageContentBuilder imageBuilder = messageContentBuilderFactory.getMessageContentBuilder();
                fun.add(bot.getBotInfo() , imageBuilder.image(IoUtil.toStream(image)).build());
            });
            fun.add(bot.getBotInfo() , "图片已过期的话点击查看原图即可");
        });
        return builder;
    }

    /**
     * 切点,代理之后使用redis作为缓存
     * @param num
     * @return
     */
    public List<byte[]> getImageContext(Integer num){
        if (this != imageHelper && num > 0) {//使用了aop代理，以redis作为缓存
            log.info("缓存区图片不足,临时从本地读取图片{}张", num);
        }
        return getRandomImg(num);
    }

    /**
     * 得到随机的，数量为num的图片的二进制流
     * @param num
     * @return
     */
    public static List<byte[]> getRandomImg(Integer num){
        Set<ImageLocation> locationSet = getSetRandom(num);
        List<byte[]> list = new ArrayList<>(num);

        for (ImageLocation location : locationSet){
            try {
                list.add( Files.readAllBytes( location.getFile().toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 随机获取数量位num的不重复的图片的坐标集合(子目录,文件)
     * @param num
     * @return
     */
    private static Set<ImageLocation> getSetRandom(Integer num){
        Set<ImageLocation> indexSet = new HashSet<>(num);
        Map<FileName, File[]> fileNameMap = getFileMapList();
        while (indexSet.size() < num){
            int i = random.nextInt(fileNames.length);
            FileName fileName = fileNames[i];
            File[] files = fileNameMap.get(fileName);//对应目录下的所有图片文件
            indexSet.add( new ImageLocation(fileName, files[random.nextInt(files.length)] ));
        }
        return indexSet;
    }

    /**
     * 将所有img存储位置的目录打开并保存对应的图片目录
     * @return
     */
    private static Map<FileName, File[]> getFileMapList(){
        Map<FileName, File[]> map = new HashMap<>(fileMap.size());
        fileMap.forEach((key, value)->{
            map.put(key, value.listFiles());
        });
        return map;
    }
    private Integer getNum(String text){
        String num = ReUtil.get("(\\d+)", text, 0);
        int n;
        if (StrUtil.isBlankOrUndefined(num)){
            n = 1;
        }else {
            n = Integer.parseInt(num);
        }
        return n;
    }

    /**
     * 启动完成后从ioc获取代理后的自身对象
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        imageHelper = ContextUtil.getBeanByType(ImageHelper.class);
    }
}
