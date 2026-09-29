# Band No（来电筛选）

轻量安卓来电筛选：响铃前按「号段 + 时段 + 同号二次来电」决定放行或拦截。宁可多响，也不要误拦。

- 最低系统：Android 10（API 29）
- 当前版本：0.0.4
- 包名：`dev.bandno.app`（debug 为 `dev.bandno.app.debug`）

判定只看号码、是否联系人、来电时间和近期拨打次数。不录音、不读通话内容、不上传通讯录。策略在本机完成。

## 默认规则（均可在设置里改）

| 名称 | 默认 | 参数 |
|---|---|---|
| 联系人始终放行 | 开启 | 通讯录号码必响 |
| 放行时段 | 每日 18:00–22:00，备注「下班」 | 可加多段；备注可改、可空；总开关在详情页 |
| 二次来电放行 | 同一号码在 **3 分钟内**（不含整 3 分钟）再次来电则响铃 | 间隔；是否要求第一次曾被拦截 |
| 号段拦截 | 未设置 | 3–7 位数字前缀；空列表不按号段拦截 |

联系人默认始终放行。非放行时默认 **静音通知**（不响铃、不震动，系统仍记未接来电），可改为拒接（需二次确认）。

跨午夜时段按半开区间 `[开始, 结束)` 计算，例如手动设置 22:00–08:00 含 22:00、07:59，不含 08:00。任一段命中即放行。

## 判定顺序

对每次来电：

1. 隐藏 / 空号，且策略为默认放行 → 放行
2. 联系人，且「联系人始终放行」开启 → 放行
3. 规范化号码命中任一号段前缀 → 拦截（跟随全局拦截动作）
4. 落在任一启用的放行时段 → 放行
5. 满足二次来电 → 放行
6. 否则执行拦截动作（静音或拒接）

号段在放行时段和二次来电之前。判定失败时放行（避免漏接）。

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

APK：`app/build/outputs/apk/release/app-release.apk`。给别人安装用这个，包名 `dev.bandno.app`。以后升级必须用同一把密钥签，否则无法覆盖安装。密钥库（`keystore/bandno-release.p12`）和 `keystore.properties` 不要提交到 git。

推送到 `main` 或开 PR 时，GitHub Actions 会跑单测并上传 arm64-v8a 正式签名 APK。仓库 Secrets 需配置 `RELEASE_KEYSTORE_BASE64`、`BANDNO_STORE_PASSWORD`、`BANDNO_KEY_PASSWORD`（可选 `BANDNO_KEY_ALIAS`，默认 `bandno`）。打 tag（如 `v0.0.3`）成功后，APK 会出现在 [Releases](https://github.com/youfun/band-no/releases)。不上 Play，用户自行安装。

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
