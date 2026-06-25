# 杖剑小助手

杖剑小助手是一个面向《杖剑传说》的 Android 原生工具应用，使用 Kotlin、Jetpack Compose 和 Material 3 开发。项目目前包含每日任务记录、常用计算器、游戏公告、兑换码、版本更新检测和远程数据缓存等功能。

当前应用包名为 `com.otori.zjcstools`，版本为 `2.4.1`，`versionCode` 为 `10`。

## 功能概览

- 每日任务清单：记录日常任务状态，并支持本地持久化。
- 小工具与计算器：包含副本、怪物、技能相关的辅助计算页面。
- 游戏信息：展示正式服公告、先遣服公告、兑换码列表。
- 兑换码奖励显示：兑换码支持展示奖励内容，例如 `命运果实 x1、结缘绳 x1`。
- 远程数据：公告、兑换码、副本、怪物、更新配置等 JSON 数据从 OSS 拉取，并带本地缓存与随包兜底数据。
- App 内更新：启动时读取远程更新配置，发现更高版本后提示下载并安装 APK。

## 项目结构

```text
app/
  src/main/java/com/otori/zjcstools/
    MainActivity.kt          # App 入口、导航、首页、更新弹窗等
    DailyTaskData.kt         # 每日任务数据与持久化
    DailyTaskScreens.kt      # 每日任务 UI
    CalculatorScreens.kt     # 计算器与小工具 UI
    RemoteNotices.kt         # 公告、兑换码、远程配置、缓存与解析
    NoticeScreens.kt         # 公告与兑换码页面 UI
    AppUpdateInstaller.kt    # App 内下载更新与安装逻辑
remote_data/
  app_config.json            # 远程配置入口，包含更新配置和数据文件版本
  DHM_codes.json             # 兑换码数据
  XQF_Announcements.json     # 先遣服公告
  ZSF_Announcements.json     # 正式服公告
  dungeon_details.json       # 副本详情
  monster_details.json       # 怪物详情
.github/workflows/
  upload-remote-data-to-oss.yml
```

`app/build.gradle.kts` 中已配置：

```kotlin
sourceSets {
    getByName("main") {
        assets.srcDir("../remote_data")
    }
}
```

因此 `remote_data` 里的 JSON 会作为随包 assets 参与构建，用作离线兜底数据。

## 兑换码数据

兑换码文件为 `remote_data/DHM_codes.json`。应用会优先读取远程 OSS 数据，失败时回退到本地缓存，再回退到随包 assets。

单条兑换码格式示例：

```json
{
  "id": "limited-2026-06-24-football-bump",
  "code": "足球碰碰爽",
  "title": "限时兑换码",
  "description": "2026年6月24日至2026年7月1日可用。",
  "startDate": "2026-06-24",
  "endDate": "2026-07-01",
  "rewards": [
    {
      "name": "命运果实",
      "quantity": 1
    },
    {
      "name": "结缘绳",
      "quantity": 1
    }
  ]
}
```

字段说明：

- `id`：兑换码记录 ID，建议保持稳定且唯一。
- `code`：实际可复制的兑换码。
- `title`：类型标题，例如 `限时兑换码` 或 `长期兑换码`。
- `description`：给维护者和兜底展示使用的说明文本。
- `startDate`：开始日期，格式为 `yyyy-MM-dd`。
- `endDate`：结束日期，格式为 `yyyy-MM-dd`。长期兑换码可使用 `2099-12-31`。
- `rewards`：奖励列表。每项包含 `name` 和可选的 `quantity`。

奖励解析兼容以下字段：

- 奖励数组名：`rewards`、`rewardItems`、`prizes`。
- 奖励名称：`name`、`reward`、`item`、`title`。
- 奖励数量：`quantity`、`count`、`amount`。
- 奖励项也可以直接写成字符串，此时只显示名称。

兑换码页面会按状态分为：

- 当前可用
- 已隐藏
- 已过期

当前可用兑换码会按限时码优先、开始日期倒序、结束日期倒序、兑换码文本排序。用户复制兑换码后会调用系统剪贴板，并显示 `已复制兑换码`。

## 更新远程数据

远程配置入口为 `remote_data/app_config.json`。其中 `dataFiles.exchangeCodes.version` 控制兑换码缓存版本：

```json
{
  "dataFiles": {
    "exchangeCodes": {
      "version": 3,
      "url": "https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json"
    }
  }
}
```

更新兑换码或公告时：

1. 修改 `remote_data/*.json`。
2. 如果改了兑换码内容，同时递增 `app_config.json` 中 `dataFiles.exchangeCodes.version`。
3. 提交并推送到 `main`。
4. GitHub Actions 会自动上传 JSON 到阿里云 OSS。

当前 OSS Bucket：

```text
zjcs-tools-otori-database
```

当前公开数据地址：

```text
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/app_config.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/XQF_Announcements.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/ZSF_Announcements.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/dungeon_details.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/monster_details.json
```

GitHub Secrets 需要配置：

```text
ALIYUN_ACCESS_KEY_ID
ALIYUN_ACCESS_KEY_SECRET
```

## 本地开发

需要准备：

- Android Studio
- JDK 11 或兼容 Gradle/Android 插件的运行环境
- Android SDK

`local.properties` 不提交到仓库，需要本机自行配置：

```properties
sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

如果需要构建 release 包，还需要加入签名信息：

```properties
release.storeFile=E\:\\AndroidKeyStore\\zjcs_richang.jks
release.storePassword=你的密码
release.keyAlias=你的alias
release.keyPassword=你的密码
```

注意不要提交 `local.properties`、签名文件、AccessKey 或任何密钥。

## 常用命令

运行单元测试：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
```

构建正式包：

```powershell
.\gradlew.bat assembleRelease --console=plain
```

正式包输出前缀由 `base.archivesName` 控制：

```text
zjcsTools-v版本号
```

例如当前版本的 release APK 名称为：

```text
zjcsTools-v2.4.1-release.apk
```

## 发布更新

App 更新配置在 `remote_data/app_config.json` 的 `appUpdate` 节点：

```json
{
  "enabled": true,
  "versionCode": 10,
  "versionName": "2.4.1",
  "apkUrl": "https://gitee.com/evilian/ZJCS-Tools_DailyTasks-and-Calculators/releases/download/v2.4.1/zjcsTools-v2.4.1-release.apk",
  "forceUpdate": false,
  "title": "发现新版本",
  "message": "检测到杖剑小助手有新版本可用。",
  "releaseNotes": [
    "大幅优化代码",
    "数据放至云端读取",
    "怪物数据更新至将军陵"
  ]
}
```

发布新版本时通常需要：

1. 修改 `app/build.gradle.kts` 中的 `versionCode` 和 `versionName`。
2. 构建 release APK。
3. 上传 APK 到 Gitee Release。
4. 修改 `remote_data/app_config.json` 中的 `appUpdate`。
5. 推送 `remote_data/app_config.json` 到 `main`，由 Actions 同步到 OSS。

应用只有在远程 `versionCode` 大于本机 `versionCode` 时才会弹出更新提示。

## 维护提示

- JSON 文件请保持 UTF-8 编码。
- 修改远程数据后记得同步递增对应 `dataFiles` 版本号，否则已缓存用户可能继续读取旧数据。
- OSS 用来分发 JSON，APK 目前通过 Gitee Release 分发。
- 兑换码奖励显示逻辑位于 `RemoteNotices.kt` 和 `NoticeScreens.kt`。
- 兑换码弹窗提醒与页面隐藏状态保存在 `check_data` SharedPreferences 中。
