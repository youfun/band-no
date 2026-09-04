# Band No（来电筛选）

轻量安卓来电筛选：响铃前按「时段 + 同号二次来电」决定放行或拦截。宁可多响，也不要在你设定的重要时段误拦。

- 最低系统：Android 10（API 29）
- 当前版本：0.0.1
- 包名：`dev.bandno.app`（debug 为 `dev.bandno.app.debug`）

判定只看号码、是否联系人、来电时间和近期拨打次数。不录音、不读通话内容、不上传通讯录。策略在本机完成。

## 默认规则（均可在设置里改）

| 编号 | 名称 | 默认 | 参数 |
|---|---|---|---|
| R1 | 夜间 / 清晨放行窗 | 每日 18:00–次日 09:00 放行 | 起止时间；是否仅陌生号 |
| R2 | 重要来电窗 | 每日 19:00–20:00 陌生号也不拒接 | 起止时间 |
| R3 | 二次来电放行 | 同一号码在 **3 分钟内**（不含整 3 分钟）再次来电则响铃 | 间隔；是否要求第一次曾被拦截 |

联系人默认始终放行。非放行时默认 **静音通知**（不响铃、不震动，系统仍记未接来电），可改为拒接（需二次确认）。

跨午夜时段按半开区间 `[开始, 结束)` 计算，例如 18:00–09:00 含 18:00、08:59，不含 09:00。R1 与 R2 重叠时以放行为准。

## 判定顺序

对每次来电：

1. 隐藏 / 空号，且策略为默认放行 → 放行
2. 联系人，且「联系人始终放行」开启 → 放行
3. 落在 R2 且 R2 开启 → 放行
4. 落在 R1 且 R1 开启 → 放行
5. 满足 R3 且 R3 开启 → 放行
6. 否则执行拦截动作（静音或拒接）

判定失败时放行（避免漏接）。

## 使用

1. 安装后按引导说明，将本应用设为 **默认来电筛选**。
2. 建议授予通讯录权限，否则联系人也会走时段规则。
3. 小米 / 华为 / OPPO / vivo 请关掉系统「未知号码拦截」，并允许自启动；说明见应用内「机型说明」。

未设为默认筛选时，首页会显示「筛选未生效」，系统仍按默认方式响铃。

## 构建

需要 JDK 17、Android SDK（compileSdk 36）。

```bash
./gradlew :decision:test :app:assembleDebug
```

APK：`app/build/outputs/apk/debug/app-debug.apk`

仅打 arm64（手机常用）：

```bash
./gradlew :app:assembleRelease -PabiFilters=arm64-v8a
```

推送到 `main` 或开 PR 时，GitHub Actions 会跑单测并上传 arm64-v8a APK（Actions → 对应 workflow → Artifacts）。

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

模拟器可注入来电：

```bash
adb emu gsm call 13900139000
```

下午默认规则下，陌生号第一次应被静音；3 分钟内再打应放行（R3）。

## 工程结构

```
decision/   纯 Kotlin 决策引擎（无 Android 依赖，可单测）
app/        CallScreeningService、Room、DataStore、Compose 界面
```

核心入口：`dev.bandno.decision.CallScreener.decide()`。

号码会去掉空格、横线及常见 `+86` / `0086` 前缀后再比较。日志可脱敏显示。来电尝试与筛选日志存在本机 Room 表 `call_attempts`，默认保留 14 天，可在设置中清除。

## 权限

| 权限 / 角色 | 用途 |
|---|---|
| `ROLE_CALL_SCREENING` | 响铃前拦截 |
| `READ_CONTACTS`（可选） | 判断是否通讯录号码 |

不申请 `READ_CALL_LOG`，近期来电由本机自行记录。

## 许可

[MIT](LICENSE)
