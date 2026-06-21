# DailyTask_ZJCS 工作交接单

生成时间：2026-06-21

## 1. 当前项目状态

- 项目路径：`E:\Programming\DailyTask_ZJCS`
- 项目类型：Android 原生应用，Kotlin + Jetpack Compose + Material 3。
- 主要业务代码仍集中在 `app\src\main\java\com\otori\zjcstools\MainActivity.kt`。
- 本机 SDK 配置文件：`local.properties` 已补充，用于 Android Studio / Gradle 本机编译，不应提交到 GitHub。
- 本线程环境里 `git` 命令不可用，未执行 git diff/status/commit/push。
- 已多次验证：`.\gradlew.bat :app:compileDebugKotlin`，结果 `BUILD SUCCESSFUL`。

## 2. 本次主要改动

### 联网权限

- `app\src\main\AndroidManifest.xml`
  - 新增：
    - `android.permission.INTERNET`

### 联网公告与兑换码

- `MainActivity.kt`
  - 新增 OSS 远程地址：
    - 兑换码：`https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json`
    - 公告：`https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/XQF_Announcements.json`
  - 使用 `HttpURLConnection` + `org.json` 读取远程 JSON，未额外引入第三方网络库。
  - 新增缓存：
    - `cached_exchange_codes_json`
    - `cached_update_preview_json`
  - App 启动时会后台拉取公告和兑换码，成功后写入缓存。
  - 启动弹窗仍允许使用缓存兜底，避免无网时影响启动体验。

### 清理代码内置数据

- 已删除/清空 `MainActivity.kt` 内写死的旧公告列表。
- 已删除/清空 `MainActivity.kt` 内写死的兑换码占位值 `xxx`。
- 目前公告和兑换码数据来源应以远程 JSON / 本地 `remote_data` 文件为准。

## 3. 兑换码系统现状

### 一级菜单入口

- 首页一级菜单新增：`兑换码`
- 点击后进入独立兑换码页面。

### 兑换码页面展示

兑换码页面分三组：

1. `当前可用`
   - 默认展开。
   - 显示当前日期仍在有效期内的兑换码。
   - 限时兑换码排在长期兑换码上方。
   - 限时兑换码按开始日期倒序显示，最新在上。
   - 长期兑换码 JSON 内到期日写 `2099-12-31`，App 内显示为 `可用日期：长期`。

2. `已隐藏`
   - 默认折叠。
   - 存储“当前未过期，但用户在兑换码菜单里点击过不再提醒”的兑换码。
   - 使用本地 key：`menu_hidden_exchange_codes`。
   - 与启动弹窗的“不再提醒”互相独立。

3. `已过期`
   - 默认折叠。
   - 显示所有已过期兑换码，不再限制一个月以内。

### 兑换码页面错误状态

每次进入兑换码页面都会重新请求远程 `DHM_codes.json`。

- 如果手机无网、连接失败、超时等 IO 错误：显示 `网络异常`。
- 如果 OSS 返回 403/404、远程文件不可用、JSON 为空或不可解析：显示 `远程文件不可用`。
- 错误提示和按钮位于页面中间。
- 错误状态下提供按钮：`显示本地数据`
  - 点击后使用手机本地缓存显示正常兑换码页面。
  - 下次重新进入兑换码页仍会再次尝试联网。

### 启动兑换码弹窗

- 弹窗不再显示 `可用兑换码：` 前缀，只显示兑换码本身。
- 每条兑换码点击 `一键复制` 后，会在本次弹窗内临时隐藏。
- 最后一条复制后，弹窗内显示 `空`，不自动关闭。
- 用户仍可点击 `不再提醒`。
- 启动弹窗的“不再提醒”使用本地 key：`hidden_exchange_codes`。
- 该记录按兑换码逐条保存，以后新兑换码出现时不会被旧记录挡住。

## 4. 本地远程数据文件

新增/维护目录：

- `remote_data\DHM_codes.json`
- `remote_data\XQF_Announcements.json`

这些文件用于本地编辑，然后手动上传覆盖阿里云 OSS 同名文件。

### 当前兑换码 JSON

`remote_data\DHM_codes.json` 当前共 19 条兑换码：

- 限时兑换码 13 条：
  - `电商直播自选奇迹古遗物`：2026-06-17 至 2026-06-24
  - `杖剑传说618买买买`：2026-06-16 至 2026-06-23
  - `燃放礼花得晨星`：2026-06-10 至 2026-06-17
  - `杖剑传说暴打策划`：2026-06-03 至 2026-06-10
  - `杖剑传说周年庆典`：2026-05-27 至 2026-06-03
  - `森森周年庆旅行活动`：2026-05-20 至 2026-05-27
  - `伊格尼斯冰火世界`：2026-05-13 至 2026-05-20
  - `寒霜与烈焰的赞歌`：2026-05-06 至 2026-05-13
  - `和BDuck一起旅行坎斯汀`：2026-04-29 至 2026-05-06
  - `森森福袋送幻装`：2026-04-22 至 2026-04-29
  - `森森特别赠礼即将到达`：2026-04-14 至 2026-04-21
  - `熔火炎龙烧熔烈焰`：2026-04-08 至 2026-04-15
  - `大葫芦春日福利`：2026-04-01 至 2026-04-08

- 长期兑换码 6 条：
  - `杖剑666`
  - `杖剑888`
  - `vip666`
  - `vip888`
  - `CH666`
  - `CH888`

长期兑换码的 JSON 到期日统一为 `2099-12-31`。

### 当前公告 JSON

`remote_data\XQF_Announcements.json` 当前为示例数据，共 5 条：

- `测试公告`
- `6月20日维护公告示例`
- `限时活动预告示例`
- `玩法调整说明示例`
- `已知问题说明示例`

后续应替换为真实公告内容。

## 5. 阿里云 OSS 状态

已创建 Bucket：

- Bucket：`zjcs-tools-otori-database`
- 地域：华东2（上海）
- Endpoint：`oss-cn-shanghai.aliyuncs.com`
- 当前用于 App 读取的公网地址：
  - `https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json`
  - `https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/XQF_Announcements.json`

注意：

- 修改本地 `remote_data` 文件后，线上用户端 App 不会自动读取电脑本地文件。
- 当前 Gradle 已将 `remote_data` 打包进 APK assets，App 会在缓存为空或远程不可用时读取 APK 内置 JSON 作为离线兜底。
- 已安装用户要获取最新线上内容，仍需要手动上传覆盖 OSS 上的同名 JSON 文件。
- 浏览器打开 JSON 自动下载是正常的，只要不是 403/404 即可。

## 6. GitHub / 新工作设备交接

用户当前在另一台设备继续工作，建议使用 GitHub Desktop：

1. 在 GitHub Desktop 登录账号。
2. Clone 仓库：`SK-otori/ZJCS-Tools_DailyTasks-and-Calculators`
3. 建议克隆到新的空目录，不要直接覆盖当前 `DailyTask_ZJCS`。
4. 将本项目关键文件复制到新克隆仓库：
   - `app\src\main\java\com\otori\zjcstools\MainActivity.kt`
   - `app\src\main\AndroidManifest.xml`
   - `remote_data\DHM_codes.json`
   - `remote_data\XQF_Announcements.json`
   - 如新仓库没有 `remote_data` 目录，需要一起创建。
5. 不要复制或提交 `local.properties`。
6. 在 GitHub Desktop 中填写提交说明并 `Commit to main`，然后 `Push origin`。

## 7. 已知注意事项

- 当前所有业务和 UI 仍集中在一个很大的 `MainActivity.kt` 中，后续扩展建议拆分：
  - `data`
  - `network`
  - `screens`
  - `components`
  - `calculators`
- 当前远程 JSON 解析做了字段兼容：
  - 兑换码优先读 `code`。
  - 公告优先读 `title`、`date`、`summary`、`body/content`。
- 兑换码 JSON 日期必须保持 `yyyy-MM-dd`，例如 `2026-06-21`。
- 如果本地缓存中有旧兑换码隐藏状态，测试时可能需要清理 App 数据才能看到弹窗重新出现。

## 8. 最近一次验证

已执行：

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

结果：

```text
BUILD SUCCESSFUL
```
