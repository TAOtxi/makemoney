<div align="center"><center>

<img alt="Icon" src="./img/icon.png" width="200">

# Makemoney
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/TAOtxi/makemoney)

</center></div>

| <sub>EN</sub> [English](./en/README_EN.md) | <sub>ZH</sub> [中文](README.md) |
| --- | --- |



<br>
专用于拾玖世界服务器的模组<br>
但也有许多通用的功能<br>
ohhhhhhh <br>

拾玖世界Q群：`518271249`

## 前置模组
[Yacl >=3.8.2+1.21.11-fabric](https://modrinth.com/mod/yacl)

## 更新日志
[history.md](./history.md)

### 配置界面
通过 `/makemoney config` 指令或 `Mod Menu` 菜单打开配置界面。

## Feature

### 🗑️ 自动丢弃 (AutoDrop)
根据设置的匹配条件，将背包内的不需要的物品丢弃。<br>
条件可设置名字、id、耐久、标签、附魔词条匹配。<br>
一般用于搞冲突附魔装备，或者钓鱼佬的懒人分类。<br>
基础命令：
```bash
/autodrop                      # 别名 /ad
/ad help                       # 查看帮助
/ad on | off                   # 开启/关闭丢弃功能
/ad config                     # 打开配置界面
/ad reload                     # 重新加载配置
/ad interval <tick>            # 设置定时触发丢弃的间隔（单位：tick）
/ad test                       # 手动触发一次丢弃功能
/ad ignore
       ├── current             # 忽略当前背包内不为空的槽位
       ├── clear               # 清空忽略列表
       └── set <1,2,3,...>     # 设置忽略的槽位，使用逗号分隔
/ad timeTrigger on | off       # 开启/关闭 时间触发丢弃功能
/ad itemTrigger on | off       # 开启/关闭 拾取指定掉落物触发丢弃功能
/ad containerTrigger on | off  # 开启/关闭 打开容器时触发丢弃功能
```

### 🎣 自动钓鱼 (AutoFish)
通过监听网络数据包判断是否上钩。<br>
基础命令：
```bash
/autofish                    # 别名 /fish
/fish help                   # 查看帮助
/fish on | off               # 开启/关闭自动钓鱼
/fish config                 # 打开配置界面
/fish randomDelay on | off   # 开启/关闭随机延迟功能
/fish throwDelay <tick>      # 设置抛掷延迟（单位：tick）
```

### ♋ 装备分解辅助 (MendingHelper)
吸取到经验球时，自动替换带有经验修补且耐久未满的装备到副手<br>
打开铁砧时，自动为装备附魔经验修补<br>
站在金块上时，自动触发装备分解 (mcmmo)<br>
背包内存在合金装备时，自动从容器中拿取经验修补进行附魔（可搭配自动钓鱼）<br>
基础命令：
```bash
/mendingHelper                    # 别名 /mh
/mh config                        # 打开配置界面
/mh autoreplace on | off          # 开启/关闭 自动替换经验修补装备到副手
/mh autoenchant on | off          # 开启/关闭 自动为装备附魔经验修补
/mh autodecompose on | off        # 开启/关闭 自动触发装备分解
/mh autorepair on | off           # 开启/关闭 自动修复合金装备
/mh autorepair setMendingBookPos  # 设置存储经验修补附魔书的容器坐标为注视的方块
```

### 🌐 自动挂机 (AutoAFK)
根据服务器tps动态调整攻击周期。<br>
当服务器tps低于危险阈值时，则触发补救命令。<br>
触发补救命令后，若tps回升到良好阈值，则触发归位命令。<br>
基础命令：
```bash
/autoafk                        # 别名 /afkk
/afkk config                    # 打开配置界面
/afkk help                      # 查看帮助
/afkk attack on | off           # 开启/关闭 自适应攻击
/afkk attack interval <tick>    # 设置攻击周期
/afkk attack info               # 查看攻击周期相关数据
/afkk tpsCheck on | off         # 开启/关闭tps检查功能
```

### 🐕 狗皮膏药 (AutoRide) (需要服务器支持右键玩家即可坐到此玩家头上)
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
设置匹配规则: `^\[!\]\[拾玖世界同好会.*?\(腐竹Q号\)>&v (\w+) (\d+)块?钱?$`<br>
其命令可以设置为 `/pay ${1} ${2}`，经过替换，将执行 `/pay TAOtxi 1000`。<br>
更详细的介绍请参考游戏中，模组配置界面的介绍。<br>

### 🚫 消息屏蔽 (ignoreMessage)
可自定义屏蔽服务器消息 <br>
配置界面已预设好一些屏蔽规则，比如猜单词、扫地机、小道消息等。

### 🎯 菜单点击 (menuClick)
可自定义点击菜单的位置、方式以及点击间隔。<br>
基础命令：
```bash
/menuClick                  # 别名 /click
/click help                 # 查看帮助
/click config               # 打开配置界面
/click run <task_name>      # 执行指定任务
```

### 💡 高亮实体 (highlight)
可透视墙壁高亮实体边框 <br>
基础命令：
```bash
/highlight                 # 别名 /hl
/hl help                   # 查看帮助
/hl on | off               # 开启/关闭 高亮实体功能
/hl radius <radius>        # 设置高亮实体范围
/hl config                 # 打开配置界面
```

### 快捷骑乘 (RightClickRide)
空手右键生物即可骑乘 <br>
原理为使用服务器的 `/ride` 命令。

### 特定任务
使用方法：
```bash
/task <taskName> on | off  # 开启/关闭
```

#### 一键丢出背包内潜影盒的所有物品（需服务器支持潜行+右键打开潜影盒）
任务名称：`dropAllItemFromShulkerBox`<br>
任务流程：将背包内不为空的潜影盒替换到主手，并向服务器发送潜行与使用物品的数据包。之后等待潜影盒容器界面打开后，自动丢出潜影盒的所有物品，并将空盒子丢到正南方向。重复上述流程，直到玩家背包内不存在不为空的潜影盒之后，自动结束任务（有小概率会将不为空的潜影盒也丢出去~）。
