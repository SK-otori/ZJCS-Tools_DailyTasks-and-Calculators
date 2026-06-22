# ZJCS-Tools 工作交接报告

生成时间：2026-06-22

## 1. 当前项目

- 当前项目路径：`E:\Programming\ZJCS-Tools_DailyTasks-and-Calculators`
- 项目类型：Android 原生应用，Kotlin + Jetpack Compose + Material 3。
- 包名 / applicationId：`com.otori.zjcstools`
- 当前版本：`2.3.2`
- 当前 `versionCode`：`7`
- 默认正式包目录：`app\release`
- 当前正式包：`app\release\zjcsTools-v2.3.2-release.apk`

## 2. 重要提醒：另一台设备也要配置 local.properties

`local.properties` 不会提交到 Git，也不应该提交到 Git。换电脑或在另一台设备编译正式包时，需要手动配置。

至少需要包含 Android SDK 路径：

```properties
sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

如果要生成正式 release 包，还必须配置签名信息：

```properties
release.storeFile=E\:\\AndroidKeyStore\\zjcs_richang.jks
release.storePassword=你的密码
release.keyAlias=你的alias
release.keyPassword=你的密码
```

注意：

- `\` 和 `:` 在 `.properties` 文件里要转义，例如 `E\:\\AndroidKeyStore\\zjcs_richang.jks`。
- 签名 `.jks` 文件不要放进仓库，不要上传 GitHub/Gitee。
- 不要重新生成新的签名 key，否则旧用户无法覆盖安装，只能卸载重装。

## 3. 当前构建配置

配置文件：`app\build.gradle.kts`

- `compileSdk`：`36.1`
- `minSdk`：`24`
- `targetSdk`：`36`
- `versionCode`：`7`
- `versionName`：`2.3.2`
- APK 输出名前缀：`zjcsTools-v${versionName}`
- release 签名从 `local.properties` 读取。

常用命令：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat assembleRelease --console=plain
```

之后约定：

- 普通功能修改只跑必要测试，不自动生成正式包。
- 用户明确说“打包 / 升版 / 生成正式包 / 发布包”时，才生成 release APK 并复制到 `app\release`。

## 4. 远程数据与 OSS

远程数据目录：

- `remote_data\DHM_codes.json`
- `remote_data\XQF_Announcements.json`
- `remote_data\app_update.json`

OSS Bucket：

- Bucket：`zjcs-tools-otori-database`
- 地域：华东2（上海）
- Endpoint：`oss-cn-shanghai.aliyuncs.com`

App 当前读取地址：

- 兑换码：`https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json`
- 公告：`https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/XQF_Announcements.json`
- 更新配置：`https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/app_update.json`

GitHub Actions：

- 工作流：`.github\workflows\upload-remote-data-to-oss.yml`
- 推送 `remote_data/*.json` 到 `main` 时会自动上传到 OSS。
- GitHub Secrets 需要配置：
  - `ALIYUN_ACCESS_KEY_ID`
  - `ALIYUN_ACCESS_KEY_SECRET`

注意：

- OSS 默认域名不能公开分发 APK，会出现 `ApkDownloadForbidden`。
- APK 目前使用 Gitee Release 分发，OSS 只放 JSON。

## 5. 当前版本更新配置

配置文件：`remote_data\app_update.json`

当前配置：

```json
{
  "enabled": true,
  "versionCode": 7,
  "versionName": "2.3.2",
  "apkUrl": "https://gitee.com/evilian/ZJCS-Tools_DailyTasks-and-Calculators/releases/download/v2.3.2/zjcsTools-v2.3.2-release.apk",
  "forceUpdate": false
}
```

规则：

- App 启动时读取 `app_update.json`。
- 远程 `versionCode` 大于本机 `versionCode` 时弹出更新提示。
- `versionCode` 相等时不会弹，避免用户反复收到同版本更新。
- `forceUpdate = true` 时隐藏“稍后再说”，当前默认是 `false`。

## 6. Gitee Release 发布

当前 APK 下载不走 OSS，而走 Gitee Release。

当前 2.3.2 应创建：

```text
标签：v2.3.2
附件：zjcsTools-v2.3.2-release.apk
```

附件路径：

```text
app\release\zjcsTools-v2.3.2-release.apk
```

下载链接格式：

```text
https://gitee.com/evilian/ZJCS-Tools_DailyTasks-and-Calculators/releases/download/v2.3.2/zjcsTools-v2.3.2-release.apk
```

建议：

- 如果不想公开源码，可以单独建一个公开 Gitee 仓库只放 Release 附件。
- 源码仓库可以私有，但 APK 下载仓库必须公开，否则用户手机无法下载。
- 后续可以用 Gitee Token + GitHub Actions 自动创建 Gitee Release，目前尚未配置。

## 7. App 内更新流程

已实现“App 内下载更新”：

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

Manifest 已增加：

```xml
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

## 8. 公告与兑换码

公告：

- 文件：`remote_data\XQF_Announcements.json`
- 当前包含真实公告：
  - `2026-06-18-maintenance`
  - `2026-06-12-maintenance`
  - `2026-05-15-maintenance`
- 测试公告已删除。
- App 默认联网获取，失败后可显示本地缓存 / assets 兜底数据。

兑换码：

- 文件：`remote_data\DHM_codes.json`
- 测试兑换码已删除。
- App 默认联网获取，失败后可显示本地缓存 / assets 兜底数据。

JSON 上传：

- 修改 `remote_data` 后提交并推送到 `main`。
- GitHub Actions 会自动同步到 OSS。

## 9. 代码结构现状

原本过长的 `MainActivity.kt` 已拆分出多个文件：

- `MainActivity.kt`：App 入口、导航、首页、共享背景、更新弹窗等。
- `RemoteNotices.kt`：公告、兑换码、更新配置的模型、解析、缓存、远程下载。
- `NoticeScreens.kt`：公告和兑换码页面 UI。
- `CalculatorScreens.kt`：计算器页面和小工具。
- `DailyTaskData.kt`：每日任务数据、持久化、重置逻辑。
- `DailyTaskScreens.kt`：每日任务 UI。
- `AppUpdateInstaller.kt`：应用内下载更新和安装逻辑。

## 10. 小工具与输入体验

已新增小工具：

- `强袭破甲增伤计算器`

功能：

- 输入玩家攻击力、怪物防御力、强袭破甲固定值。
- 强袭破甲百分比使用品质选择控件，不手输。
- 品质选项：
  - `彩-不朽`：`46.1%`
  - `红-神话`：`39.6%`
  - `金-奇迹`：`33%`
  - `橙-传说`：`26.4%`
- 使用 `qxpj_1_bx.png`、`qxpj_2_sh.png`、`qxpj_3_qj.png`、`qxpj_4_cs.png`。

输入框体验：

- 计算器输入框会保存用户上次输入。
- 用户点击输入框时默认全选当前文本。
- 每日任务新增行输入框也支持点击全选。

## 11. 已知注意事项

- `apk_chinese_strings.txt` 已确认无引用，可作为历史遗留文件处理。
- `.gitignore` 已忽略阿里云 RAM AccessKey 文件夹，AccessKey 不要放入仓库。
- `assets.srcDir("../remote_data")` 构建时有 deprecated 警告，不影响当前打包，后续可择机改成新版写法。
- 如果测试更新弹窗，需要确保远程 `app_update.json` 的 `versionCode` 大于当前安装包。
- 如果用户已经安装同版本，不会弹更新。
- 如果手机阻止安装未知应用，需要按系统提示授权。

## 12. 最近验证

最近已执行并通过：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat assembleRelease --console=plain
```

当前 release APK：

```text
app\release\zjcsTools-v2.3.2-release.apk
```

当前 Git 工作区在生成本报告前为干净状态。
