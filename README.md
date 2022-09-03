# little-bot
基于[Simpler Robot](https://github.com/ForteScarlet/simpler-robot/tree/v2-dev)的群聊机器人项目, [v2]
# 功能
1. 酷狗点歌并以qq语音形式发送到群聊（tip:电脑端无法播放，手机端正常）指令:/点歌 搜索内容(当成酷狗网页搜索栏就好)
2. 支持根据mp3文件自定义点歌(群聊中可用)    指令格式: /添加 歌曲名 - 歌手 例如:/添加 love story - TaylorSwifter 后发送mp3文件即可
3. 发送随机图片到qq群聊(从本地, 已删除)
4. 私聊发送huft课表(自用，可忽略)
5. 原神自动签到

# 如何开始?
1. 所需环境: `Java1.8+`  `MySQL()`
2. 打开`\src\main\resources\simbot-bots\`文件夹, 在里面创建一个`*.bot`文件, `*`可以是任意字符, [参考](https://www.yuque.com/simpler-robot/simpler-robot-doc/fk6o3e#iUKbX)
3. 枚举类com.zsck.bot.enums.FileName 中value值为子文件夹名，yml中配置主文件夹: 例如com.zsck.data.file-path: E:\img\ ,即可在com.zsck.bot.group.GroupPhoto中增加监听器，
   随机发送子文件夹中图片(似乎用处也不大...)
4. 仅huft课表需要使用数据库,正常使用时忽略即可
