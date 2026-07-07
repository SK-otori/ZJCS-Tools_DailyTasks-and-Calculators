# ZJCS-Tools 工作交接报告

生成时间：2026-07-07

## 1. 当前项目

- 当前项目路径：`E:\Programming\ZJCS-Tools_DailyTasks-and-Calculators`
- 项目类型：Android 原生应用，Kotlin + Jetpack Compose + Material 3。
- 包名 / applicationId：`com.otori.zjcstools`
- 当前版本：`2.4.2`
- 当前 `versionCode`：`11`
- 默认正式包目录：`app\release`
- 当前正式包：`app\release\zjcsTools-v2.4.2-release.apk`

## 2. 本机配置提醒

`local.properties` 不提交到 Git，换电脑或在另一台设备编译时需要手动配置。

至少需要 Android SDK 路径：

```properties
sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

如需生成 release 包，还需要签名信息：

```properties
release.storeFile=E\:\\AndroidKeyStore\\zjcs_richang.jks
release.storePassword=你的密码
release.keyAlias=你的alias
release.keyPassword=你的密码
```

注意：

- `.properties` 中的 `\` 和 `:` 需要转义。
- 不要提交 `local.properties`、签名 `.jks`、AccessKey 或任何密钥。
- 不要重新生成新的签名 key，否则旧用户无法覆盖安装。

## 3. 当前构建配置

配置文件：`app\build.gradle.kts`

- `compileSdk`：`36.1`
- `minSdk`：`24`
- `targetSdk`：`36`
- `versionCode`：`11`
- `versionName`：`2.4.2`
- APK 输出名前缀：`zjcsTools-v${versionName}`
- release 签名从 `local.properties` 读取。
- `remote_data` 已通过 `assets.srcDir("../remote_data")` 打入 assets，作为离线兜底数据。

常用命令：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat assembleRelease --console=plain
```

约定：

- 普通功能修改只跑必要测试，不自动生成正式包。
- 用户明确说“打包 / 升版 / 生成正式包 / 发布包”时，才生成 release APK 并复制到 `app\release`。

## 4. 远程数据与 OSS

远程数据目录：

- `remote_data\app_config.json`
- `remote_data\app_update.json`
- `remote_data\DHM_codes.json`
- `remote_data\XQF_Announcements.json`
- `remote_data\ZSF_Announcements.json`
- `remote_data\dungeon_details.json`
- `remote_data\monster_details.json`

OSS Bucket：

- Bucket：`zjcs-tools-otori-database`
- 地域：华东2（上海）
- Endpoint：`oss-cn-shanghai.aliyuncs.com`

App 当前主要读取 `app_config.json`，再按其中的 `dataFiles` 获取各类数据：

```text
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/app_config.json
```

当前数据版本：

- `exchangeCodes`：`5`
- `updatePreviewNotices`：`4`
- `officialNotices`：`2`
- `dungeonDetails`：`10`
- `monsterDetails`：`1`

公开数据地址：

```text
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/XQF_Announcements.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/ZSF_Announcements.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/dungeon_details.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/monster_details.json
https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/app_update.json
```

GitHub Actions：

- 工作流：`.github\workflows\upload-remote-data-to-oss.yml`
- 推送 `remote_data/*.json` 到 `main` 时会自动上传到 OSS。
- GitHub Secrets 需要配置：
  - `ALIYUN_ACCESS_KEY_ID`
  - `ALIYUN_ACCESS_KEY_SECRET`

注意：修改远程数据后，需要递增 `app_config.json` 中对应 `dataFiles` 的 `version`，否则已缓存用户可能继续读取旧数据。

## 5. 当前版本更新配置

配置文件：

- `remote_data\app_config.json`
- `remote_data\app_update.json`

当前更新配置：

```json
{
  "enabled": true,
  "versionCode": 11,
  "versionName": "2.4.2",
  "apkUrl": "https://gitee.com/evilian/ZJCS-Tools_DailyTasks-and-Calculators/releases/download/v2.4.2/zjcsTools-v2.4.2-release.apk",
  "forceUpdate": false,
  "title": "发现新版本",
  "message": "检测到杖剑小助手有新版本可用。",
  "releaseNotes": [
    "新增升级时间计算器，支持按赛季估算目标等级达成时间",
    "支持当前经验单位切换，并自动限制等级与经验输入范围",
    "优化升级结果展示，可查看还需经验、预计时间、所需天数和加速次数"
  ]
}
```

规则：

- App 启动时优先读取 `app_config.json` 中的 `appUpdate`。
- 兼容读取单独的 `app_update.json`。
- 远程 `versionCode` 大于本机 `versionCode` 时弹出更新提示。
- `forceUpdate = true` 时隐藏“稍后再说”，当前默认是 `false`。

## 6. Gitee Release 发布

APK 当前通过 Gitee Release 分发，OSS 只分发 JSON。

当前 2.4.2 Release 信息：

```text
标签：v2.4.2
附件：zjcsTools-v2.4.2-release.apk
本地附件路径：app\release\zjcsTools-v2.4.2-release.apk
下载链接：https://gitee.com/evilian/ZJCS-Tools_DailyTasks-and-Calculators/releases/download/v2.4.2/zjcsTools-v2.4.2-release.apk
```

发布新版本通常步骤：

1. 修改 `app\build.gradle.kts` 中的 `versionCode` 和 `versionName`。
2. 执行 `.\gradlew.bat assembleRelease --console=plain`。
3. 将 APK 放到 `app\release`，文件名保持 `zjcsTools-v版本号-release.apk`。
4. 上传 APK 到 Gitee Release。
5. 修改 `remote_data\app_config.json` 和 `remote_data\app_update.json`。
6. 推送 `remote_data/*.json` 到 `main`，触发 Actions 同步 OSS。

## 7. App 内更新流程

已实现 App 内下载更新：

- 用户点击更新弹窗中的 `下载更新`。
- App 使用 Android `DownloadManager` 下载 APK。
- 下载开始后，版本说明和更新内容隐藏，只显示进度条和状态文本。
- 下载完成后按钮变为 `点击安装`。
- 下载完成后自动尝试打开系统安装界面。
- 如果系统要求“允许安装未知应用”，会打开授权页；授权后返回 App 会继续尝试安装。
- 如果自动打开失败，用户可以点击 `点击安装` 再次进入系统安装器。

相关文件：

- `app\src\main\java\com\otori\zjcstools\AppUpdateInstaller.kt`
- `app\src\main\java\com\otori\zjcstools\MainActivity.kt`
- `app\src\main\AndroidManifest.xml`

Manifest 已包含：

```xml
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

## 8. 公告与兑换码

公告：

- 先遣服公告：`remote_data\XQF_Announcements.json`
- 正式服公告：`remote_data\ZSF_Announcements.json`
- 当前先遣服公告数量：`7`
- 当前先遣服最新公告：`2026-07-03-preview-maintenance`
- 当前正式服公告数量：`3`
- 当前正式服最新公告：`2026-06-25-football-theme-green-field-carnival`
- App 默认联网获取，失败后显示本地缓存 / assets 兜底数据。

兑换码：

- 文件：`remote_data\DHM_codes.json`
- 当前总数：`21`
- 限时兑换码：`15`
- 长期兑换码：`6`
- 支持奖励显示，字段为 `rewards`，每项包含 `name` 和可选 `quantity`。
- 兼容奖励数组字段：`rewards`、`rewardItems`、`prizes`。
- 兼容奖励名称字段：`name`、`reward`、`item`、`title`。
- 兼容奖励数量字段：`quantity`、`count`、`amount`。

兑换码 UI：

- 弹窗与列表卡片都会显示 `奖励：物品 x数量`。
- 页面分为 `当前可用`、`已隐藏`、`已过期`。
- 可一键复制兑换码。
- 可对当前可用兑换码执行“不再提醒”，状态保存在 `check_data` SharedPreferences。

## 9. 游戏资料与远程数据

副本资料：

- 文件：`remote_data\dungeon_details.json`
- 当前数据版本：`10`
- 当前副本数量：`11`
- 页面：`副本资料`
- 相关代码：`CalculatorScreens.kt` 中的 `DungeonInfoScreen`

怪物资料：

- 文件：`remote_data\monster_details.json`
- 当前数据版本：`1`
- 当前怪物数量：`47`
- 页面：`每日委托怪物搜索`
- 相关代码：`CalculatorScreens.kt` 中的 `DailyMissionMonsterLookupScreen`

## 10. 代码结构现状

- `MainActivity.kt`：App 入口、导航、首页、共享背景、更新弹窗等。
- `RemoteNotices.kt`：公告、兑换码、更新配置的模型、解析、缓存、远程下载。
- `NoticeScreens.kt`：公告和兑换码页面 UI。
- `CalculatorScreens.kt`：计算器页面、游戏资料、小工具。
- `DailyTaskData.kt`：每日任务数据、持久化、重置逻辑。
- `DailyTaskScreens.kt`：每日任务 UI。
- `AppUpdateInstaller.kt`：应用内下载更新和安装逻辑。
- `ui\theme\*.kt`：Compose 主题配置。

## 11. 小工具与计算器

当前工具入口包括：

- `副本晨星计算器`
- `升级时间计算器`
- `每日委托怪物搜索`
- `副本资料`
- `星间之神`
- `强袭破甲增伤计算器`
- 属性晨星 / 宝石相关计算模块

升级时间计算器：

- 页面：`UpgradeTimeScreen`
- 经验表 assets：
  - `app\src\main\assets\level_exp_player_with_exp_diff.csv`
  - `app\src\main\assets\level_bless_with_exp_diff.csv`
- 支持按赛季估算目标等级达成时间。
- 支持当前经验单位切换。
- 自动限制等级与经验输入范围。
- 结果展示还需经验、预计时间、所需天数和加速次数。
- 当前版本更新说明已把该功能作为 v2.4.2 重点。

强袭破甲增伤计算器：

- 输入玩家攻击力、怪物防御力、强袭破甲固定值。
- 强袭破甲百分比使用品质选择控件。
- 品质选项：
  - `彩-不朽`：`46.1%`
  - `红-神话`：`39.6%`
  - `金-奇迹`：`33%`
  - `橙-传说`：`26.4%`
- 使用 `qxpj_1_bx.png`、`qxpj_2_sh.png`、`qxpj_3_qj.png`、`qxpj_4_cs.png`。

输入框体验：

- 计算器输入框保存用户上次输入。
- 用户点击输入框时默认全选当前文本。
- 每日任务新增行输入框也支持点击全选。

## 12. 已知注意事项

- `assets.srcDir("../remote_data")` 构建时可能有 deprecated 警告，不影响当前打包。
- 如果测试更新弹窗，需要确保远程 `app_update.json` 或 `app_config.json` 中的 `versionCode` 大于当前安装包。
- 如果用户已经安装同版本，不会弹更新。
- 如果手机阻止安装未知应用，需要按系统提示授权。
- `.kotlin/sessions/*.salive` 属于本地 Kotlin 会话文件，不应作为功能变更处理。

## 13. 最近验证

最近存在的 release APK：

```text
app\release\zjcsTools-v2.4.2-release.apk
```

本次文档更新未重新运行测试或打包。
