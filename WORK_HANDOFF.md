# DailyTask_ZJCS 工作交接报告

生成时间：2026-06-21

## 1. 当前状态

- 项目路径：`E:\Programming\DailyTask_ZJCS`
- 项目类型：Android 原生应用，Kotlin + Jetpack Compose + Material 3。
- 当前分支：`main`
- Git 状态：新增本交接文档 `WORK_HANDOFF.md`，尚未提交。
- 远端状态：`main` 与 `origin/main` 指向同一个提交。
- 最新提交：`2b8cdfa 新增兑换码模块，新增icon美化`
- 验证结果：已执行 `.\gradlew.bat testDebugUnitTest`，结果 `BUILD SUCCESSFUL`。

## 2. 构建与运行

常用命令：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

当前构建配置位于 `app\build.gradle.kts`：

- `namespace` / `applicationId`：`com.otori.zjcstools`
- `compileSdk`：36.1
- `minSdk`：24
- `targetSdk`：36
- `versionCode`：4
- `versionName`：`2.2.0`
- APK 输出名前缀：`zjcsTools-v${versionName}`

## 3. 项目结构

主要文件：

- `app\src\main\java\com\otori\zjcstools\MainActivity.kt`
  - 当前核心业务、UI、数据模型、计算器逻辑都集中在这个文件。
- `app\src\main\AndroidManifest.xml`
  - 应用入口、图标、主题、键盘 resize 配置。
- `app\src\main\res\drawable`
  - 背景图、工具图标、示例状态图等资源。
- `app\src\main\res\mipmap-*`
  - 应用启动图标与圆形图标。
- `gradle\libs.versions.toml`
  - AGP、Kotlin、Compose BOM、AndroidX 依赖版本。

## 4. 当前功能概览

`MainActivity.kt` 中包含以下主要模块：

- 主页与工具入口
  - `App`
  - `MainHomeScreen`
  - `HomeScreen`
  - `ToolsHomeScreen`
  - `GameDataHomeScreen`
  - `SecondaryHomeScreen`
- 日常任务管理
  - 任务、角色、完成状态、角色启用状态。
  - 支持每日、每周、一次性任务。
  - 支持拖拽排序、删除确认、全部重置。
  - 数据保存在 `SharedPreferences("check_data")`。
- 自动重置逻辑
  - 每日重置时间：早上 8 点。
  - 每周任务按配置的周几重置。
  - 相关函数：`resetDateFor`、`weeklyResetDayPassed`、`resetIfNeeded`。
- 兑换码模块
  - 数据模型：`ExchangeCodeNotice`
  - 当前兑换码列表：`exchangeCodeNotices`
  - 当前有一个占位兑换码：`xxx`
  - 支持复制兑换码和隐藏已提示兑换码。
- 先遣服更新公告
  - 数据模型：`UpdatePreviewNotice`
  - 首页卡片标题：`先遣服更新前瞻`
  - 列表与详情页：`UpdatePreviewHomeScreen`、`UpdatePreviewDetailScreen`
  - 当前含 2026-06-13 的真实公告内容，以及多条 2026-06-01 到 2026-06-11 的测试公告。
- 晨星/收益类计算工具
  - `DungeonMorningStarScreen`
  - `DungeonMorningStarCalculator`
  - `calculateDungeonProfit`
- 星神/经验计算工具
  - `AstralKamiScreen`
  - `AstralKamiCalculator`
  - `calculateAstralKamiResult`
- 属性晨星/宝石战力计算工具
  - `AttributeMorningStarScreen`
  - `AttributeMorningStarCalculator`
  - `calculateCharacterAttributePower`
  - `calculateGemPowerRows`

## 5. 数据与兼容性

本地持久化主要使用 `SharedPreferences("check_data")`。

关键 key：

- `tasks`
- `persons`
- `checked`
- `disabled_roles`
- `last_reset_date`
- `default_daily_data_version`
- `hidden_exchange_codes`

分隔符：

- 列表分隔符：`###`
- 任务字段分隔符：`@@`
- 完成记录 key 分隔符：`|`

注意：新增任务名、角色名时应继续避免包含上述保留分隔符。已有函数 `containsReservedSeparator` 可用于校验。

## 6. 已知占位和待处理点

- `exchangeCodeNotices` 中当前兑换码仍是占位值 `xxx`，需要替换为真实兑换码。
- `updatePreviewNotices` 中除了 2026-06-13 维护公告外，多条 2026-06-01 到 2026-06-11 的公告是测试数据，需要后续替换或删除。
- 核心代码集中在一个超大的 `MainActivity.kt` 文件中，后续如果继续扩展，建议逐步拆分：
  - `data`：数据模型与持久化。
  - `screens`：页面级 Composable。
  - `components`：通用 UI 组件。
  - `calculators`：计算器业务逻辑。
- 当前单元测试仍是模板测试，尚未覆盖业务函数。建议优先补：
  - 任务序列化 / 反序列化。
  - 每日与每周重置规则。
  - 星神经验计算。
  - 副本收益计算。
  - 属性晨星与宝石战力计算。

## 7. 最近 Git 提交

```text
2b8cdfa 新增兑换码模块，新增icon美化
02f1b8a 报错修复，软件图标更新
3323404 新增先遣服公告列表
42c41b4 更新包名、版本号和APK名称
2bbc0a2 首次保存项目
```

当前本地 `main` 分支与远端 `origin/main` 指向同一个提交。切换到新工作端后，本交接文档是未提交新增文件，如需保留请先提交或随项目文件一起带走。

## 8. 建议下一步

1. 替换兑换码占位数据 `xxx`。
2. 清理或替换先遣服公告测试数据。
3. 运行 `.\gradlew.bat assembleDebug` 生成调试包并真机验证主要页面。
4. 为关键计算逻辑补充单元测试。
5. 如需保留本交接报告，提交 `WORK_HANDOFF.md`。
