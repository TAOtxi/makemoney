# Makemoney
<div style="display: flex; justify-content: center; width: 100%;">
    <img src="./icon.png" alt="icon" width="200" height="200">
</div>

<br>
> 专用于拾玖世界服务器的模组<br>
> ohhhhhhh<br>
><br>
> 拾玖世界Q群：`518271249`

## 前置模组
[Yacl >=3.8.2+1.21.11-fabric](https://modrinth.com/mod/yacl)

## Feature

### 配置界面
通过 `/makemoney config` 指令或 `Mod Menu` 菜单打开配置界面。


### 🗑️ 自动丢弃 (AutoDrop)
根据设置的匹配条件，将背包内的不需要的物品丢弃。<br>
条件可设置名字、id、标签、附魔词条匹配。<br>
一般用于搞冲突附魔装备，或者钓鱼佬的懒人分类。<br>
基础命令：
```bash
/autodrop                   # 别名 /ad
/ad help                    # 查看帮助
/ad on | off                # 开启/关闭丢弃功能
/ad config                  # 打开配置界面
/ad reload                  # 重新加载配置
/ad interval <tick>         # 设置定时触发丢弃的间隔（单位：tick）
/ad test                    # 手动触发一次丢弃功能
/ad ignore
       ├── current          # 将当前背包内不为空的槽位加入忽略列表
       ├── clear            # 清空忽略列表
       └── set <1,2,3,...>  # 设置忽略的槽位，使用逗号分隔
```


### 🎣 自动钓鱼 (AutoFish)
通过监听网络数据包判断是否上钩。<br>
基础命令：
```bash
/autofish                    # 别名 /fish
/fish help                   # 查看帮助
/fish on | off               # 开启/关闭自动钓鱼
/fish config
        ├── open             # 打开配置界面
        └── reload           # 重新加载配置
/fish randomDelay on | off   # 开启/关闭随机延迟功能
/fish throwDelay <tick>      # 设置抛掷延迟（单位：tick）
```


### 🐕 狗皮膏药 (AutoRide)
像个狗皮膏药一样黏在其它玩家头上<br>
 也可以禁止别的玩家骑你头上<br>
基础命令：
```bash
/autoride                  # 别名 /ar
/ar help                   # 查看帮助
/ar on | off               # 开启/关闭自动骑乘
/ar config                 # 打开配置界面
/ar distance <distance>    # 设置骑乘判断范围
/ar smoothHead on | off    # 开启/关闭光滑头功能
/ar target <name>          # 锁定目标玩家
/ar interval <tick>        # 设置判断周期（单位：tick）
/ar reset                  # 重置所有设置
```


### 📣 监听服务器聊天消息
当服务器消息匹配到设定的正则表达式，将执行命令。<br>
支持使用正则表达式捕获组的内容作为命令参数。<br>
例如，匹配来自拾玖群发送到服务器的消息: `宁【量产plus型云计算中心版】：&v TAOtxi 1000块钱`<br>
设置匹配规则: `^\[!\]\[拾玖世界同好会.*?\\(腐竹Q号\\)>&v (\w+) (\d+)块?钱?$`<br>
其命令可以设置为 `/pay ${1} ${2}`，经过替换，将执行 `/pay TAOtxi 1000`。<br>
更详细的介绍请参考游戏中，模组配置界面的介绍。<br>
基础命令：
```bash
/messageCommand          # 别名 /mr
/mr help                 # 查看帮助
/mr on | off             # 开启/关闭消息命令功能
/mr config               # 打开配置界面
```

### 🚫 消息屏蔽 (ignoreMessage)
可自定义屏蔽服务器消息 <br>
配置界面已预设好一些屏蔽规则，比如猜单词、扫地机、小道消息等。


### rightClickRide
空手右键生物即可骑乘 <br>
已屏蔽马、装备鞍的猪等原版右键可骑乘生物。

### 自动替换经验修补工具（待完善）
将背包内的带有经验修补且耐久为满的物品替换到副手。<br>
用于钓鱼佬分解钻石或合金工具。

### 生物高亮（待完善）
高亮指定的生物边框。透视墙壁<br>