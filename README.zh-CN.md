# DailyLife
<p align="center">
    <a href="https://github.com/Evening-01/DailyLife">
        <img src="https://socialify.git.ci/Evening-01/DailyLife/image?font=Source+Code+Pro&forks=1&issues=1&language=1&name=1&owner=1&pattern=Circuit+Board&pulls=1&stargazers=1&theme=Light" alt="socialify"/>
    </a>
    <a target="_blank" href="https://socialify.git.ci/Evening-01/DailyLife/README.zh-CN.md">简体中文</a>&nbsp;&nbsp;|&nbsp;&nbsp;  English&nbsp;&nbsp;
</p>
DailyLife 是一款基于 Jetpack Compose 的个人财务应用，帮助你记录收支、洞察消费趋势，并保持良好的日常习惯。应用内置 Material 3 设计体系、离线优先的数据存储、丰富的统计分析，以及桌面微件、指纹保护、房贷和汇率计算等效率工具。

本项目也是作者的金融相关毕业设计，旨在将大学三年所学的 Android 知识付诸实践，构建一款功能完整、体验现代的个人应用，同样也是一份求职作品。

如果您觉得不错，请在 GitHub 点击右上角 ⭐ Star 以支持我在空余时间继续开发。

## 目录
- [功能亮点](#功能亮点)
- [效果图](#效果图)
- [架构概览](#架构概览)
- [技术栈](#技术栈)
- [模块导览](#模块导览)
- [快速开始](#快速开始)
- [开发任务](#开发任务)
- [质量规范](#质量规范)
- [国际化](#国际化)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## 功能亮点
- 交易流水账：提供快捷的新增/编辑流程、预置分类、心情记录以及软删除。
- 首页信息流：按月汇总、按日分组，快速跳转常用功能。
- 交互式统计：支持收支图表、分类排行、心情相关性时间线等分析能力。
- 探索中心：包含消费类型画像、AI 功能预览、房贷等额/等本计算器、汇率换算（支持倒算与快速互换）。
- “我的”空间：指纹锁设置、主题与字体调节、签到天数、完整的数据备份/恢复流程。
- Glance 桌面微件：展示当日收支、最近记录，并提供一键记账入口。
- Room 持久化：离线可用、基于 StateFlow 实时刷新，并在后台清理归档数据。
- 动态主题、自定义字体、大字号 UI，以及开启动态取色后的自适应图标。

## 效果图
您可以[**点击此处**](https://github.com/Evening-01/DailyLife/tree/master/images)跳转到 `images` 目录查看所有截图。

## 架构概览
DailyLife 采用模块化的 MVVM + 单向数据流架构：
- **Compose 优先的界面层**：界面通过 `@Composable` 实现，状态统一交由 ViewModel（`StateFlow`）管理。
- **按领域拆分 Feature**：交易、图表、探索、房贷、汇率、个人中心等均位于 `feature/<area>` 目录，拥有独立的 UI 与状态控制。
- **共享基础能力**：`core` 包含设计系统、依赖注入模块、Room 实体/DAO、FastKV 偏好设置、统计缓存与通用工具类。
- **导航编排**：`app/.../navigation` 负责顶层路由图，由各 Feature 嵌套组成主界面 `HomeScreen`。
- **长生命周期统计缓存**：`TransactionAnalyticsRepository` 统一聚合交易数据，为图表、探索、个人中心及微件提供快照。
- **安全的后台任务**：仓库操作运行在注入的协程作用域内（`@ApplicationScope`），保持 UI 响应且避免泄漏。

```text
app/src/main/java/com/evening/dailylife/
├── app/                # 应用入口、导航、桌面微件
├── core/               # 数据层、DI、设计系统、工具类
└── feature/            # 各业务模块（chart、currency、details、discover、home、me、mortgage、transaction 等）
```

## 技术栈
- Kotlin 2.1、Coroutines、Flow
- Jetpack Compose（Material 3、Navigation、Glance App Widgets）
- Hilt 依赖注入
- Room 本地数据库
- FastKV 偏好存储
- AndroidX Biometric 指纹认证
- Material Kolor 与自定义主题工具
- 最低支持 API 23，目标/编译 API 35，JVM 目标 17

## 模块导览
| 模块 | 路径 | 核心内容                                                  |
| --- | --- |-------------------------------------------------------|
| 应用外壳 | `app/src/main/java/com/evening/dailylife/app` | `MainActivity`、Hilt `DailyLifeApplication`、导航图、桌面微件宿主 |
| 基础能力 | `app/src/main/java/com/evening/dailylife/core` | Room 数据库、仓库、统计缓存、设计系统、指纹管理、DI 模块                      |
| 交易流程 | `feature/transaction` | 交易新增/编辑界面、校验、心情选择、仓库交互                                |
| 数据分析 | `feature/chart` | 支出排行、收支图表、心情趋势                                        |
| 效率工具 | `feature/discover` | 消费画像、AI 预览、房贷与汇率工具                                    |
| 个性化 | `feature/me` | 指纹锁、主题字体、使用统计、备份/恢复                                   |

## 快速开始
1. **环境要求**
   - Android Studio Ladybug（2024.2）或更新版本
   - JDK 17
   - 安装 Android SDK Platform 35 及对应构建工具
2. **克隆仓库**
   ```bash
   git clone https://github.com/Evening-01/DailyLife
   cd DailyLife
   ```
3. **本地配置**
   - 确认 `local.properties` 中的 `sdk.dir=...` 指向本地 Android SDK。
   - 可选：在 `local.properties` 或环境变量中设置签名信息（`SIGNING_STORE_FILE`、`SIGNING_STORE_PASSWORD`、`SIGNING_KEY_ALIAS`、`SIGNING_KEY_PASSWORD`），用于生成发布版本。
4. **同步与构建**
   - 在 Android Studio 中打开工程并同步 Gradle，或运行 `./gradlew assembleDebug`。
5. **运行**
   - 将 `app` 模块部署到 Android 6.0（API 23）及以上的模拟器或真实设备。

## 开发任务
- 构建调试包：`./gradlew assembleDebug`
- JVM 单元测试：`./gradlew test`
- 仪器测试与 Compose UI 测试：`./gradlew connectedDebugAndroidTest`
- 静态分析：`./gradlew lint`
- 清理构建产物：`./gradlew clean`
- CI / 分发辅助任务：
  - `./gradlew printAppName`
  - `./gradlew printVersionName`
  - `./gradlew printCommitCount`
  - `./gradlew renameReleaseBundle`

## 质量规范
- Kotlin 风格：四空格缩进、禁止通配符导入、多行参数结尾保留逗号。
- Compose 组件需提升状态，优先在 ViewModel 中管理并通过 Hilt 注入。
- 单元测试与被测包同路径放在 `app/src/test`；Compose/UI 自动化测试放在 `app/src/androidTest`。
- 依赖版本通过 `gradle/sweet-dependency/sweet-dependency-config.yaml` 管理，勿在 `build.gradle.kts` 中硬编码。
- 在提交 PR 前运行 `./gradlew test lint`，涉及 UI 或导航改动时请额外执行 `connectedDebugAndroidTest`。

## 国际化
- 默认提供英文与简体中文（`values-zh-rCN`）资源。
- 如需新增语言，可使用 `LanguagePreferencesRepository` 暴露的语言切换能力。
- 所有面向用户的文案请放在 `app/src/main/res/values/strings.xml`，新增字符串时保持翻译文件同步。

## 贡献指南
1. Fork 仓库并从 `main` 创建主题分支。
2. 按 `{动作} {编号}.` 的规范编写原子提交信息，例如 `Add 1. 实现房贷计算缓存。`
3. 在提交 PR 前运行 `./gradlew test lint`（若涉及 UI 变更，请追加 `connectedDebugAndroidTest`）。
4. 在 PR 描述中提供概要、UI 截图/GIF（如适用）以及执行过的验证命令。

欢迎通过 GitHub Issues 提交缺陷或功能需求，提供日志与复现步骤会更有助于定位问题。

## 许可证
DailyLife 采用 [Cooperative Non-Commercial License v1.0（CNC-1.0）](LICENSE) 授权。
- 允许在非商业场景中免费使用、修改与分发，并需保留来源声明。
- 衍生作品必须采用同等或相近条款授权，并公开完整源码。
- 以盈利为目的的企业、风投支持公司或上市公司如需使用，须提前获得书面许可。
如有商业授权需求，请联系 DailyLife 维护者。
