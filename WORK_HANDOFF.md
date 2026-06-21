# DailyTask_ZJCS 工作交接单

生成时间：2026-06-22

## 1. 当前项目状态

- 当前项目路径：`E:\Programming\ZJCS-Tools_DailyTasks-and-Calculators\ZJCS-Tools_DailyTasks-and-Calculators`
- 项目类型：Android 原生应用，Kotlin + Jetpack Compose + Material 3。
- 主要业务代码仍集中在 `app\src\main\java\com\otori\zjcstools\MainActivity.kt`。
- 本机 SDK 配置文件：`local.properties` 用于 Android Studio / Gradle 本机编译，不应提交到 GitHub。
- 本线程环境里 `git` 命令不可用，未执行 git diff/status/commit/push。

## 2. 当前版本与构建配置

配置文件：`app\build.gradle.kts`

- `namespace`：`com.otori.zjcstools`
- `applicationId`：`com.otori.zjcstools`
- `compileSdk`：`36.1`
- `minSdk`：`24`
- `targetSdk`：`36`
- `versionCode`：`5`
- `versionName`：`2.3.0`
- APK 输出名前缀：`zjcsTools-v${versionName}`
- 当前 debug APK 相对路径：`app\build\outputs\apk\debug\zjcsTools-v2.3.0-debug.apk`

当前 `remote_data` 已通过 Gradle 打包进 APK assets：

```kotlin
sourceSets {
    getByName("main") {
        assets.srcDir("../remote_data")
    }
}
```

构建时会出现 `assets.srcDir` deprecated 警告，但不影响当前编译和打包。后续可以改成新版 Gradle 推荐的目录集合写法。

## 3. 联网权限与远程数据

`app\src\main\AndroidManifest.xml` 已包含：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

远程 JSON 地址：

- 兑换码：`https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json`
- 公告：`https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/XQF_Announcements.json`

读取方式：

- 使用 `HttpURLConnection` + `org.json`，未引入第三方网络库。
- App 启动时后台拉取公告和兑换码。
- 拉取成功后写入 `SharedPreferences("check_data")` 缓存。

缓存 key：

- `cached_exchange_codes_json`
- `cached_update_preview_json`

## 4. 数据加载链路

当前兑换码和公告的加载优先级：

1. 读取本机缓存 JSON。
2. 缓存为空或不可解析时，读取 APK 内置 assets 中的 `remote_data` JSON。
3. 远程请求成功后，更新缓存。
4. 缓存、assets、远程都不可用时才回退为空列表。

相关内置 assets 文件名：

- `DHM_codes.json`
- `XQF_Announcements.json`

注意：

- 修改电脑本地 `remote_data` 后，已安装用户不会自动获得新内容。
- 线上更新仍需要手动上传覆盖 OSS 上的同名 JSON 文件。
- APK 内置 `remote_data` 只负责首装、离线、远程失败时的兜底。
- 浏览器打开 JSON 自动下载是正常现象，只要不是 403/404 即可。

## 5. 兑换码系统现状

一级菜单已有 `兑换码` 入口。

兑换码页面分三组：

- `当前可用`
  - 默认展开。
  - 显示当前日期仍在有效期内的兑换码。
  - 限时兑换码排在长期兑换码上方。
  - 限时兑换码按开始日期倒序显示。
  - 长期兑换码到期日使用 `2099-12-31`，App 内显示为 `可用日期：长期`。

- `已隐藏`
  - 默认折叠。
  - 保存用户在兑换码菜单里点击过“不再提醒”的当前未过期兑换码。
  - 使用本地 key：`menu_hidden_exchange_codes`。
  - 与启动弹窗的“不再提醒”互相独立。

- `已过期`
  - 默认折叠。
  - 显示所有已过期兑换码，不限制一个月以内。

兑换码页面错误状态：

- 每次进入兑换码页面都会重新请求远程 `DHM_codes.json`。
- 网络 IO 错误显示：`网络异常`。
- OSS 返回 403/404、远程文件不可用、JSON 为空或不可解析显示：`远程文件不可用`。
- 错误状态下提供 `显示本地数据` 按钮，点击后显示缓存/assets 兜底数据。
- 下次重新进入兑换码页仍会再次尝试联网。

启动兑换码弹窗：

- 弹窗只显示兑换码本身，不再显示 `可用兑换码：` 前缀。
- 每条兑换码点击 `一键复制` 后，会在本次弹窗内临时隐藏。
- 最后一条复制后，弹窗内显示 `空`，不会自动关闭。
- 启动弹窗“不再提醒”使用本地 key：`hidden_exchange_codes`。
- 该记录按兑换码逐条保存，新兑换码不会被旧隐藏记录挡住。

## 6. 当前 remote_data 内容

目录：

- `remote_data\DHM_codes.json`
- `remote_data\XQF_Announcements.json`

当前兑换码 JSON 共 19 条：

- 限时兑换码 13 条。
- 长期兑换码 6 条。
- 长期兑换码到期日统一为 `2099-12-31`。

当前公告 JSON 共 5 条，仍是示例数据：

- `测试公告`
- `6月20日维护公告示例`
- `限时活动预告示例`
- `玩法调整说明示例`
- `已知问题说明示例`

后续应将公告 JSON 替换为真实公告内容。

## 7. 阿里云 OSS 状态

已创建 Bucket：

- Bucket：`zjcs-tools-otori-database`
- 地域：华东2（上海）
- Endpoint：`oss-cn-shanghai.aliyuncs.com`

当前 App 读取公网地址：

- `https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json`
- `https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/XQF_Announcements.json`

更新流程：

1. 在本地编辑 `remote_data` 对应 JSON。
2. 确保 JSON 格式合法，日期保持 `yyyy-MM-dd`。
3. 手动上传覆盖 OSS 同名文件。
4. App 下次启动或进入相关页面后会读取远程最新内容并写入缓存。

## 8. 签名 APK 注意事项

正式发布和覆盖升级必须继续使用原来的签名 key。

当前已知旧 key 路径示例：

- `E:\AndroidKeyStore\zjcs_richang.jks`

需要保留并迁移的信息：

- `.jks` 或 `.keystore` 文件
- `Key store password`
- `Key alias`
- `Key password`

不要重新生成新的 key。新 key 会导致已安装旧版本的用户无法直接覆盖升级，只能卸载后重装。

建议：

- key 文件放在项目目录外。
- 不要提交到 GitHub。
- 如果忘记 alias，可用 `keytool -list -v -keystore E:\AndroidKeyStore\zjcs_richang.jks` 查看。

## 9. GitHub / 新设备交接

建议使用 GitHub Desktop：

1. 登录 GitHub 账号。
2. Clone 仓库：`SK-otori/ZJCS-Tools_DailyTasks-and-Calculators`。
3. 克隆到新的空目录，不要直接覆盖旧项目。
4. 重点确认以下文件已同步：
   - `app\src\main\java\com\otori\zjcstools\MainActivity.kt`
   - `app\src\main\AndroidManifest.xml`
   - `app\build.gradle.kts`
   - `remote_data\DHM_codes.json`
   - `remote_data\XQF_Announcements.json`
   - `WORK_HANDOFF.md`
5. 不要提交 `local.properties`。
6. 不要提交签名 key 文件。
7. 在 GitHub Desktop 中填写提交说明，`Commit to main`，然后 `Push origin`。

## 10. 已知注意事项

- 当前所有业务和 UI 仍集中在一个很大的 `MainActivity.kt` 中。
- 后续扩展建议逐步拆分：
  - `data`
  - `network`
  - `screens`
  - `components`
  - `calculators`
- 当前远程 JSON 解析做了字段兼容：
  - 兑换码优先读 `code`。
  - 公告优先读 `title`、`date`、`summary`、`body/content`。
- 兑换码 JSON 日期必须保持 `yyyy-MM-dd`，例如 `2026-06-22`。
- 如果本地缓存中有旧兑换码隐藏状态，测试启动弹窗时可能需要清理 App 数据。
- `git` 命令在本 Codex 环境不可用，如需提交建议用 GitHub Desktop 或在本机终端配置 Git。

## 11. 最近一次验证

已执行：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

结果：

```text
BUILD SUCCESSFUL
```

已确认 debug APK 中包含：

```text
assets/DHM_codes.json
assets/XQF_Announcements.json
```

当前 debug APK：

```text
app\build\outputs\apk\debug\zjcsTools-v2.3.0-debug.apk
```
