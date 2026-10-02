# AGENTS.md

本文档供在本仓库中工作的 AI Agent 与开发者使用。修改代码前先阅读相关模块；以仓库中的实际实现和配置为准，不要套用服务端模组或旧版 Minecraft/Fabric 的 API 经验。

## 项目概览

MakeMoney（模组 ID：`makemoney`）是面向 Minecraft **26.2** 的 Fabric **纯客户端模组**，主要为“拾玖世界”服务器提供自动丢弃、自动钓鱼、自动修补、菜单点击、消息触发命令、挂机辅助等功能。

- 语言：Java 25
- 构建：Gradle Wrapper 9.5.0 + Fabric Loom 1.17-SNAPSHOT
- Minecraft：26.2
- Fabric Loader：0.19.3
- Fabric API：0.156.0+26.2
- 配置 UI：YACL 3.9.6+26.2-fabric
- Mod Menu：20.0.1（可选集成）
- 许可证：AGPL-3.0
- 主入口：`cn.taotxi.Makemoney.Makemoney`
- 运行环境：`client`

版本的权威来源是 `gradle.properties`。升级版本时还要同步检查 `fabric.mod.json`、`makemoney.mixins.json` 和 `.github/workflows/build.yml`，尤其是 Java 版本与发布依赖元数据。

> 注意：历史包名 `cn.taotxi.Makemoney` 和部分模块子包以大写字母开头。虽然不符合常见 Java 包命名习惯，但它们已被入口配置和大量源码引用，不要在无关任务中顺手重命名。

## 目录结构

```text
.
├─ build.gradle                         # Loom、依赖、Java 版本、打包配置
├─ gradle.properties                    # Minecraft/Fabric/模组版本矩阵
├─ .github/workflows/build.yml          # tag 构建与多平台发布
├─ src/main/java/cn/taotxi/Makemoney/
│  ├─ Makemoney.java                    # 初始化入口和顶层客户端命令
│  ├─ config/                           # JSON 配置框架与全局配置
│  │  └─ type/                          # Boolean/Integer/String/Array 等配置类型
│  ├─ gui/                              # YACL、Mod Menu、弹窗和 GUI 工具
│  ├─ mixin/                            # vanilla 客户端行为/数据包注入
│  ├─ module/                           # 各业务模块
│  │  ├─ AutoDrop/                      # 自动丢弃
│  │  ├─ AutoFish/                      # 自动钓鱼
│  │  ├─ AutoAFK/                       # 自动攻击、TPS/位置检测等
│  │  ├─ MendingHelper/                 # 经验修补、自动修复
│  │  ├─ MenuClick/                     # 容器菜单自动点击
│  │  ├─ MessageCommand/                # 聊天正则触发命令
│  │  ├─ NineteenWorld/                 # 拾玖世界特定功能
│  │  ├─ Task/                          # 用户任务功能
│  │  └─ UpdateCheck/                   # Modrinth 更新检查
│  └─ util/                             # tick 任务、消息、日志、翻译及游戏工具
└─ src/main/resources/
   ├─ fabric.mod.json                   # Fabric 元数据、入口和依赖
   ├─ makemoney.mixins.json             # Mixin 注册
   └─ assets/makemoney/
      ├─ lang/zh_cn.json
      ├─ lang/en_us.json
      ├─ icon.png
      └─ images/
```

`run/`、`build/`、`.gradle/` 是本地运行或构建产物，不是业务源码。不要修改或提交其中的生成文件。

## 架构与执行流程

### 初始化

Fabric 通过 `Makemoney.onInitialize()` 启动模组。当前顺序是：

1. 加载 `MakemoneyConfig`。
2. 注册 `/makemoney`、`/mn` 和 AutoDrop 客户端命令。
3. 初始化各业务模块。
4. 初始化 `TaskUtil` 的 client tick 调度。
5. 初始化任务入口与更新检查。

虽然入口实现 `ModInitializer`，但 `fabric.mod.json` 已将模组限制为客户端，且源码直接依赖客户端 API。不要尝试把当前入口加载到 dedicated server。

### 配置系统

- `ConfigMaker` 负责 `config/makemoney/<module>.json` 的 UTF-8 JSON 读写、损坏文件备份、临时文件和原子替换。
- `ConfigManager` 管理 option、默认值、加载、保存、重置以及顶层字段补齐/裁剪。
- 各模块通常持有自己的 `*Config` 单例；修改配置值后，只有调用 `saveConfig()` 才能确保持久化。
- change listener 常用于重建正则、任务或缓存。新增配置项时要判断加载后和 GUI 保存后是否需要主动触发 listener。
- 重命名配置 key 会让旧字段被视为废弃字段并删除。涉及 schema 变化时必须设计迁移，不能只改 key 字符串。
- 不要绕过现有配置层直接覆盖用户 JSON。

### 命令与 GUI

命令使用 Fabric API v2 的客户端命令和 Brigadier，不向服务器注册命令树。模块通常包含正式命令、短别名和 `HelpMenu`。

总配置页由 YACL 的 `ConfigScreen` 构建，通过 `/makemoney config`、`/mn config` 或 Mod Menu 打开。`GuiUtil` 会延迟开屏并维护 tab 映射。增删或调整配置分类时，需要同步检查：

- `ConfigScreen` 中分类顺序；
- `GuiUtil.configTabIndexMap`；
- `Makemoney.java` 中 `config <0-5>` 参数范围；
- 帮助文本和双语翻译键。

### Tick 任务

`TaskUtil` 在 `END_CLIENT_TICK` 上统一调度 `TimeTask`。任务周期可以是固定值，也可以通过 `IntSupplier` 动态读取配置。

- 任务 ID 全局唯一；重复创建会抛出 `IllegalArgumentException`。
- 重建任务前按现有模式使用 `has`/`remove`。
- 一次性延迟操作也应复用 `TaskUtil`，不要新增独立 tick listener。
- 回调中访问 `client.player`、`client.level`、`gameMode` 或容器前必须做生命周期和 null 检查。
- 世界切换后应重置实体 ID、旋转、计数器和服务器状态等临时数据。

### Mixin 与数据包分发

`makemoney.mixins.json` 注册三个客户端 Mixin：

- `ClientPacketListenerMixin`：把聊天、实体、容器、时间、死亡/复活等 vanilla 入站包分发到业务模块。
- `MultiPlayerGameModeMixin`：在实体交互或使用物品时触发模块逻辑。
- `PlayerTabOverlayMixin`：按条件替换玩家列表排序。

Mixin 应保持轻量，只做线程/状态检查和事件分发，业务逻辑放在对应模块。当前 `defaultRequire` 为 `1`，目标方法或注入点失配通常会导致客户端启动失败。修改方法签名、`@At`、`cancellable` 或升级 Minecraft 后，必须进行真实客户端启动验证。新增 Mixin 时同时登记到 `makemoney.mixins.json`。

项目没有 Fabric 自定义网络 channel。网络相关功能主要监听 vanilla clientbound packet、发送 vanilla serverbound packet，以及发送聊天/命令。

### 更新检查

`UpdateCheck` 使用 Java `HttpClient` 请求 Modrinth API，是项目中明确的外部 HTTP 请求。网络工作在 daemon 线程中执行，Minecraft UI 操作必须通过客户端线程或 `TaskUtil` 调度，不能直接从网络线程操作界面。

## 编码约定

- Java 使用 4 空格缩进；类名 PascalCase，方法和字段 camelCase，常量 `UPPER_SNAKE_CASE`。
- 保持现有“模块入口/聚合类 + `*Config` + 可选 `*Gui` + 执行器”的静态单例式组织。
- 新业务放入所属 `module`，通用能力放入 `util`；不要继续堆积到主入口或 Mixin。
- 注释以中文为主，重点解释原因、协议约束和兼容行为，不复述代码。
- 使用 SLF4J 占位符日志，不使用 `System.out`。全局重要日志使用 `Makemoney.LOGGER`，模块调试优先使用 `MLogger`。
- 面向玩家的消息使用 `Message` 和 `T.tl(...)`；新增用户可见文本优先使用翻译键。
- 新增或修改玩家可见文案时同时维护 `zh_cn.json` 与 `en_us.json`。
- 不做与当前任务无关的大范围格式化、重命名或“顺手重构”。保留仍有效的历史兼容逻辑和 TODO。
- packet 回调访问世界状态前沿用 `minecraft.isSameThread()` 和 null/lifecycle 防护。
- 容器槽位、实体 metadata 索引、包方法名及 Mixin 签名高度依赖 Minecraft 版本，不要凭旧版映射猜测 API。

## 常见改动检查表

### 新增或修改模块功能

1. 在所属模块中实现业务逻辑，不在 Mixin 中堆逻辑。
2. 若需配置：注册 option、加载/保存、change listener 和 YACL binding。
3. 若需周期/延迟执行：使用唯一 ID 接入 `TaskUtil`。
4. 若需命令：注册客户端命令、别名和 `HelpMenu`。
5. 若需事件：优先复用现有回调；只有缺少合适事件时才增加 Mixin。
6. 补齐中英文翻译键，并检查 README/CHANGELOG 是否需要同步。

### 修改配置 schema

- 用旧版真实 JSON 验证新增、删除和重命名字段。
- 确认顶层字段裁剪不会误删需要保留的数据。
- 嵌套对象需要自行实现迁移；通用配置管理器不会自动迁移嵌套结构。
- 验证损坏 JSON 的 `.broken` 备份和默认值恢复行为。

### 升级 Minecraft、Fabric 或 Java

同步检查：

- `gradle.properties`；
- `build.gradle` 中 Java release/source/target；
- `fabric.mod.json` 中运行时约束；
- `makemoney.mixins.json` 中 compatibility level；
- `.github/workflows/build.yml` 中 JDK 与发布依赖版本；
- 所有 Mixin 目标、packet accessor、实体 metadata 与 YACL 内部 API。

## 构建与验证

Windows PowerShell 下使用仓库自带 Wrapper：

```powershell
# 快速编译 Java
.\gradlew.bat classes

# verification 生命周期（当前没有独立测试或静态检查插件）
.\gradlew.bat check

# CI 同等完整构建，包含资源处理、sources jar 和 Loom remap
.\gradlew.bat build

# 启动开发客户端进行行为验证
.\gradlew.bat runClient
```

当前仓库没有 `src/test`、JUnit/GameTest、Checkstyle 或 Spotless。不要声称“测试通过”来代替实际说明；应明确报告执行了哪些 Gradle 任务。除非任务明确要求，不要为了普通功能改动自行引入测试框架或新依赖。

最低验证标准：

1. 普通 Java 改动至少运行 `.\gradlew.bat classes`；资源或打包相关改动运行 `.\gradlew.bat build`。
2. Mixin、GUI、数据包、交互或 Minecraft 版本相关改动需要运行 `.\gradlew.bat runClient`，确认无 Mixin apply error 和启动崩溃。
3. 配置相关改动应打开 `/mn config` 或 Mod Menu，执行保存、关闭、重开，并检查 `run/config/makemoney/` 下的实际结果。
4. 命令改动应测试正式命令、别名、帮助页、参数边界和中英文文本。
5. 事件功能按对应场景实测，例如换世界、拾取物品、钓鱼、系统聊天、打开容器、死亡/复活或 TPS 更新。
6. 检查 `run/logs/latest.log` 和必要时的 `debug.log`，确认没有任务异常、配置恢复、packet 或 Mixin 错误。

`runClient` 是交互式长运行任务；自动化 Agent 无法可靠完成交互验证时，应完成可执行的编译/构建检查，并明确告诉用户仍需手动验证的场景。

## Git 与发布注意事项

- 不修改或提交 `build/`、`run/`、`.gradle/` 等生成内容。
- 不在未被要求时创建提交、tag 或发布。
- 发布由 tag（`v*` 或 `*beta*`）触发 `.github/workflows/build.yml`；改动发布配置时特别注意 Modrinth、CurseForge 和 GitHub 三处产物及依赖元数据。
- 产物位于 `build/libs/`；常规发布应使用 remapped 主 jar，而不是 `sources`、`dev` 或 `javadoc` jar。

## Agent 工作原则

1. 修改前阅读目标模块、调用方、配置和资源文件，不依据类名猜行为。
2. 优先做最小且完整的改动，并覆盖与其强关联的配置、GUI、命令、翻译和初始化代码。
3. 不覆盖用户已有改动；发现无关工作区变化时保留并绕开。
4. 不新增依赖，除非现有 Fabric/Minecraft/JDK 能力确实无法满足需求；新增依赖时固定明确版本并说明原因。
5. 完成后报告修改文件、实际运行的验证命令、验证结果，以及未能自动验证的游戏内场景。
