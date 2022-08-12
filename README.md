# little-bot
基于[Simpler Robot](https://github.com/ForteScarlet/simpler-robot/tree/v2-dev)的群聊机器人项目, [v2]
# 功能
酷狗点歌并以qq语音形式发送到群聊（tip:电脑端无法播放，手机端正常）
发送随机图片到qq群聊(从本地)
私聊发送huft课表(自用，可忽略)

# 如何开始?
1. 先学会`SpringBoot`框架
2. 所需环境: `Java1.8+`  `MySQL()`
3. 打开`\src\main\resources\simbot-bots\`文件夹, 在里面创建一个`*.bot`文件, `*`可以是任意字符, [参考](https://www.yuque.com/simpler-robot/simpler-robot-doc/fk6o3e#iUKbX)
4. 枚举类com.zsck.bot.enums.FileName 中value值为子文件夹名，yml中配置主文件夹: 例如com.zsck.data.file-path: E:\img\ ,即可在com.zsck.bot.group.GroupPhoto中增加监听器，
   随机发送子文件夹中图片
5. 仅huft课表需要使用数据库,正常使用时忽略即可
