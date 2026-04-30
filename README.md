# jandan-reader

`jandan-reader` 是一个 IntelliJ Platform 插件项目。插件会在 IDE 底部注册 `LogAnalyzer` 工具窗口，从煎蛋网接口读取树洞和无聊图内容，并将内容渲染成类似 Java 日志、Spring 配置代码的阅读界面。

项目当前插件名为 `Code Review Helper`，主要展示入口是 `LogAnalyzer` 工具窗口。

## 核心功能

- **IDE 内阅读内容**：在 IntelliJ IDEA 底部工具窗口中展示煎蛋内容，不需要离开 IDE。
- **树洞内容展示**：`ServiceLog` 标签页读取固定树洞帖子评论，首次加载会自动探测总页数并跳到最新页。
- **无聊图内容展示**：`ResourceCfg` 标签页读取无聊图评论，并在内容中预览图片资源。
- **分页与刷新**：支持上一页、下一页、跳转页码和刷新当前页。
- **吐槽回复展示**：对存在回复的评论批量拉取吐槽内容，并作为代码注释块展示。
- **代码风格伪装**：根据 IDE 明暗主题，将内容渲染成 Java/Spring 风格的 HTML 代码视图。

## 技术栈

- Kotlin JVM `1.9.25`
- Java `17`
- Gradle `8.5`
- IntelliJ Platform Gradle Plugin `1.17.4`
- IntelliJ IDEA Community `2023.3`
- Gson `2.10.1`
- Swing `JPanel` / `JEditorPane`
- `HttpURLConnection`

## 环境要求

- JDK 17
- 可访问 `jandan.net` 和 `i.jandan.net` 的网络环境
- Windows 环境可直接使用仓库内的 `gradlew.bat`

## 本地运行

进入项目目录：

```powershell
cd jandan-reader
```

启动 Gradle 沙箱 IDE：

```powershell
.\gradlew.bat runIde
```

启动后会打开一个用于调试插件的 IntelliJ IDEA 沙箱实例。在该实例中打开底部工具窗口 `LogAnalyzer`，即可看到两个标签页：

- `ServiceLog`：树洞评论内容。
- `ResourceCfg`：无聊图评论和图片预览。

## 打包与安装

构建插件 ZIP：

```powershell
.\gradlew.bat buildPlugin
```

构建完成后，插件包通常位于：

```text
build/distributions/
```

在 IntelliJ IDEA 中本地安装：

1. 打开 `Settings` / `Preferences`。
2. 进入 `Plugins`。
3. 点击齿轮菜单，选择 `Install Plugin from Disk...`。
4. 选择 `build/distributions/` 下生成的插件 ZIP 文件。
5. 重启 IDE。

## 常用开发命令

```powershell
# 运行插件沙箱
.\gradlew.bat runIde

# 构建插件
.\gradlew.bat buildPlugin

# 校验插件，适合发布前检查
.\gradlew.bat verifyPlugin
```

当前仓库未包含 `src/test` 测试目录，如需补充自动化测试，可在后续添加对应测试源码与 Gradle 配置。

## 项目结构

```text
.
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew.bat
└── src
    └── main
        ├── kotlin
        │   └── com/jandan/reader
        │       ├── CodeDisguiser.kt
        │       ├── JandanApi.kt
        │       ├── JandanPanel.kt
        │       └── JandanToolWindowFactory.kt
        └── resources
            └── META-INF/plugin.xml
```

主要文件说明：

- `JandanToolWindowFactory.kt`：插件工具窗口入口，创建 `ServiceLog` 和 `ResourceCfg` 两个标签页。
- `JandanPanel.kt`：Swing UI、分页按钮、跳转、刷新和后台加载逻辑。
- `JandanApi.kt`：煎蛋接口请求、JSON 解析和吐槽回复拉取。
- `CodeDisguiser.kt`：将评论内容转换为代码风格 HTML。
- `plugin.xml`：插件 ID、名称、依赖和 Tool Window 扩展注册。

## 发布配置

`build.gradle.kts` 已预留 JetBrains 插件签名与发布配置，发布时通过环境变量注入：

- `CERTIFICATE_CHAIN`
- `PRIVATE_KEY`
- `PRIVATE_KEY_PASSWORD`
- `PUBLISH_TOKEN`

