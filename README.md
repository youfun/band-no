# Band No（来电筛选）

Android 来电筛选应用，基于号段、时段和同号二次来电规则决定放行或拦截。纯本地规则判定，不依赖网络，不上传通讯录与来电信息。

- 最低系统：Android 10（API 29）
- 当前版本：0.0.5
- 包名：`dev.bandno.app`（debug 为 `dev.bandno.app.debug`）

## 默认规则

| 名称 | 默认 | 参数 |
|---|---|---|
| 联系人始终放行 | 开启 | 通讯录号码直接放行 |
| 放行时段 | 每日 18:00–22:00，备注「下班」 | 支持多时段与自定义备注 |
| 二次来电放行 | 同一号码在 **3 分钟内**（不含整 3 分钟）再次来电则响铃 | 可配置间隔时间及首次来电拦截条件 |
| 号段拦截 | 未设置 | 支持 3–7 位数字前缀 |

联系人默认始终放行。非放行来电默认执行 **静音通知**（不响铃、不震动，保留系统未接记录），亦可配置为直接拒接。

跨午夜时段按半开区间 `[开始, 结束)` 计算，例如手动设置 22:00–08:00 含 22:00、07:59，不含 08:00。任一段命中即放行。

## 判定顺序

对每次来电：

1. 隐藏 / 空号，且策略为默认放行 → 放行
2. 联系人，且「联系人始终放行」开启 → 放行
3. 规范化号码命中任一号段前缀 → 拦截（跟随全局拦截动作）
4. 落在任一启用的放行时段 → 放行
5. 满足二次来电 → 放行
6. 否则执行拦截动作（静音或拒接）

号段拦截优先级高于放行时段与二次来电。若判定异常则默认放行。

## 使用

1. 安装后按引导说明，将本应用设为 **默认来电筛选**。
2. 建议授予通讯录权限，否则联系人也会走号段和时段规则。
3. 小米 / 华为 / OPPO / vivo 请关掉系统「未知号码拦截」，并允许自启动；说明见应用内「机型说明」。

未设为默认筛选时，首页会显示「筛选未生效」，系统仍按默认方式响铃。

## 构建

需要 JDK 17、Android SDK（compileSdk 36）。

```bash
./gradlew :decision:test :app:assembleDebug
```

APK：`app/build/outputs/apk/debug/app-debug.apk`

仅打 arm64（手机常用），release 用本机正式签名（不是 debug 证书）：

```bash
# 首次：复制 keystore.properties.example 为 keystore.properties，填入密钥库路径和密码
./gradlew :app:assembleRelease -PabiFilters=arm64-v8a
```

APK 输出路径：`app/build/outputs/apk/release/app-release.apk`。

注：密钥库（`keystore/bandno-release.p12`）及 `keystore.properties` 请勿提交至版本控制。

推送到 `main` 或开 PR 时，GitHub Actions 会执行单元测试并构建 arm64-v8a 签名 APK。仓库 Secrets 需配置 `RELEASE_KEYSTORE_BASE64`、`BANDNO_STORE_PASSWORD`、`BANDNO_KEY_PASSWORD`（可选 `BANDNO_KEY_ALIAS`，默认 `bandno`）。发布 Release tag（如 `v0.0.5`）时将自动触发打包并附加到 [Releases](https://github.com/youfun/band-no/releases)。

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

模拟器可注入来电：

```bash
adb emu gsm call 13900139000
```

下午默认规则下，陌生号第一次应被静音；3 分钟内再打应放行（二次来电）。18:00–22:00 内陌生号默认放行。

## 工程结构

```
decision/   纯 Kotlin 决策引擎（无 Android 依赖，可单测）
app/        CallScreeningService、Room、DataStore、Compose 界面
```

核心入口：`dev.bandno.decision.CallScreener.decide()`。

号码会去掉空格、横线及常见 `+86` / `0086` 前缀后再比较。日志可脱敏显示，并在能查到时附上归属地，仅供展示，不参与拦截。中国归属地数据来自 [libphonenumber](https://github.com/google/libphonenumber) `resources/geocoding/zh/86.txt`（Apache-2.0）。来电尝试与筛选日志存在本机 Room 表 `call_attempts`，默认保留 14 天，可在设置中清除。

## 权限

| 权限 / 角色 | 用途 |
|---|---|
| `ROLE_CALL_SCREENING` | 响铃前拦截 |
| `READ_CONTACTS`（可选） | 判断是否通讯录号码 |

不申请 `READ_CALL_LOG`，近期来电由本机自行记录。

## 许可

[FSL-1.1-ALv2](LICENSE)。Copyright 2026 youfun。
