# 智慧安防小区运营平台（Smart Residential Security & Operations Platform）

一套面向住宅小区的智慧安防与物业运营一体化平台（一期模块化单体），覆盖 **小区基础数据、设备管理、视频监控、门禁/访客/车辆、告警处置、工单流转、巡更、报修、AI 事件、消息通知、统计分析与运营大屏** 全业务链路。

> 后端 12 个业务模块、约 180 个 REST 接口，51 个功能菜单；全量接口冒烟 **79/79 通过**，前端依赖冒烟 **68/68 通过**。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.3.4 · Java 21 · MyBatis-Plus 3.5.7 · Spring Security(OAuth2 Resource Server, JWT/RS256) |
| 存储 | MySQL 8（48 张表）· Redis 7（缓存，不可用时自动降级） |
| 前端 | Vue 3.5 · TypeScript · Vite 5 · Element Plus · Pinia · ECharts |
| 安全 | BCrypt 密码、AES 敏感字段加密、`@PreAuthorize` 方法级鉴权、小区/部门级数据权限（SQL 自动改写）、操作/登录审计日志 |

## 功能总览

### 已实现（前后端完整闭环）

- **系统管理** — 用户/角色/菜单/部门/字典/参数/操作日志/登录日志，完整 CRUD + 菜单树授权
- **小区基础** — 小区/楼栋/单元/房屋/业主/车辆档案，业主手机与证件 AES 加密存储，房屋 Excel 导入导出
- **设备管理** — 设备台账、设备类型 CRUD、状态/类型/在线率统计、状态日志、设备事件上报（离线/故障自动转告警）
- **视频监控** — 通道树、录像计划、实时流/回放地址（对接 ZLMediaKit 后即为真实流，见"功能开关"）
- **门禁管理** — 设备、通行记录、授权管理、远程开门（含**黑名单拦截**）
- **访客管理** — 邀请/签到/过期/撤销全流程
- **车辆通行** — 通行记录、每日统计、道闸设备、车牌入场识别（白名单放行）
- **告警中心** — 多源接入（设备/AI/人工）→ 规则匹配 → 分级 → 自动派单 → 处置/误报闭环，趋势与分布统计
- **工单中心** — 创建 → 派发 → 接单 → 处理 → 验收 → 评价 → 关闭全状态机 + 流转时间线
- **巡更管理** — 点位 → 路线编排（顺序可调）→ 计划（时段/间隔/人员）→ 打卡，**每日凌晨自动判定漏巡**
- **报修投诉** — 受理 → 派单（自动生成关联工单）→ 处理 → 完成 → 评价
- **黑名单** — 人员/车辆布控（含生效期），远程开门时实时拦截
- **AI 分析** — 场景配置（算法/通道/置信度阈值/生效时段），事件回调入库，**超阈值自动生成告警**
- **消息通知** — 公告发布、站内信、消息模板（变量渲染，如 `{ownerName}`）
- **统计分析** — 综合报表、告警/通行/车辆/设备多维分析、安全指数
- **运营大屏** — 实时指标 + 最新告警 + 设备状态 + 视频轮巡，30s 自动刷新
- **统计日汇总** — 每日 00:10 定时聚合各业务表（告警/通行/车辆/访客/工单/巡更/在线率），支持手动触发
- **文件服务** — 上传/下载/列表

### 定时任务

| 任务 | 时间 | 说明 |
|------|------|------|
| `PatrolScheduleJob` | 每日 00:05 | 对前一日启用计划按路线点位判定漏巡，写入 `status=3` 记录 |
| `DailySummaryJob` | 每日 00:10 | 聚合前一日全部小区的业务指标到 `stat_daily_summary` |

两者均幂等，重复执行不产生重复数据。

## 快速开始

### 环境要求

- JDK 21 · Maven 3.8+ · Node.js 18+ · MySQL 8 · Redis（可选，不可用时自动降级）

### 1. 初始化数据库

```bash
mysql -uroot -p < sql/01_schema.sql        # 建库建表（48 张表）
mysql -uroot -p < sql/02_init_data.sql     # 菜单/权限/字典/演示数据
```

### 2. 启动后端（端口 8088）

```bash
cd backend
mvn spring-boot:run
```

数据库连接、AES 密钥、功能开关等配置见 `backend/src/main/resources/application.yml`，请按本机环境调整。

### 3. 启动前端（端口 5175）

```bash
cd frontend
npm install
npm run dev        # 开发端口 5175，/api 代理到 8088
```

### 4. 登录

浏览器打开 `http://localhost:5175`，内置演示账号（密码均为 `admin123`）：

| 账号 | 角色 | 说明 |
|------|------|------|
| `admin` | 超级管理员 | 全部菜单与数据 |
| `wuye` | 物业人员 | 小区/设备/告警/工单/巡更/报修等业务菜单 |
| `anbao` | 安保人员 | 告警处置、工单、门禁、巡更记录等 |

## 验证

```bash
# 后端需已在 8088 运行
backend/smoke-test.sh    # 全量接口冒烟：登录后逐一校验 ~79 个接口 HTTP 200 且业务码 200
backend/fe-smoke.sh      # 前端依赖接口冒烟：68 项
cd frontend && npm run typecheck   # vue-tsc 类型检查，提交前必须通过
```

## 功能开关（application.yml `srs.*`）

| 开关 | 默认 | 说明 |
|------|------|------|
| `srs.media.enabled` | `false` | 流媒体（ZLMediaKit）未接入时，`/video/live`、`/playback` 返回演示地址；`ptz`、`snapshot`、`/iot/device/restart`、`/access/remote-open`、`/vehicle/gate/open` 为记录式桩（流程与审计已通，设备指令下发待对接） |
| `srs.alarm.auto-dispatch` | `false` | 告警自动派单到工单，规则级 `auto_dispatch` 优先 |
| `srs.cache.enabled` | `true` | Redis 不可用时自动降级为无缓存 |

## 架构速览

```
backend/src/main/java/com/srs
├── modules/
│   ├── system/   认证/用户/角色/菜单/部门/字典/参数/日志
│   ├── base/     小区/楼栋/单元/房屋/业主/车辆
│   ├── iot/      设备台账/设备类型/设备事件/状态日志
│   ├── video/    通道/录像计划/直播回放
│   ├── sec/      门禁/通行/授权/访客/车辆/黑名单/告警
│   ├── patrol/   巡更点位/路线/计划/记录/漏巡判定
│   ├── workorder/ 工单状态机与流转
│   ├── repair/   报修投诉
│   ├── msg/      公告/站内信/消息模板
│   ├── ai/       AI 场景配置/事件回调
│   ├── stat/     统计报表/大屏/日汇总
│   └── file/     文件上传下载
├── common/       R<T> 统一响应、分页、审计填充、@OperLog、AES、BizException
├── config/       安全/缓存/MyBatis-Plus/OpenAPI
├── datascope/    小区/部门级数据权限(SQL 自动改写)
└── security/     JWT 认证与鉴权工具
```

- 各模块统一 `controller / service / entity / mapper` 四层，实体按表是否存在 `del_flag` 继承 `DelEntity`（逻辑删除）或 `BaseEntity`
- 带 `community_id` 的表统一登记在 `SrsDataPermissionHandler.COMMUNITY_TABLES`，由拦截器按登录用户数据范围自动追加 SQL 过滤
- 写操作统一 `@OperLog` 记审计日志；权限串与 `sys_menu.perms` 严格一致

### 前端

- `api/request.ts` axios 封装（自动解包 `R<T>`、401/403 清 token 跳登录）
- `api/index.ts` 全部接口按域分组
- 动态路由：登录后拉取菜单树注册路由；专用页面经 `router/index.ts` 的 `componentMap` 映射，标准 CRUD 页走 `config/modules.ts` 的 `moduleConfig` 配置驱动（`generic/CrudPage.vue` 渲染），顶栏切换小区自动注入 `communityId` 查询条件

## 新增一个业务模块（清单）

1. 表加入 `sql/01_schema.sql`；带 `community_id` 的表登记 `COMMUNITY_TABLES`
2. 后端四件套 + `@OperLog` + `@PreAuthorize("@ss.has('<域>:<资源>:<动作>')")`
3. `sys_menu` 插入菜单与按钮权限（perms 与代码一致）
4. 前端 `api/index.ts` 加接口；`modules.ts` 加 moduleConfig 或专用页面 + `componentMap` 映射
5. `npm run typecheck` 通过后，把新接口补进 `smoke-test.sh`

## 文档

- `功能实现情况报告.md` — 模块实现度审计（2026-09-15 原始版 + 2026-10 更新说明）
- `docs/demo-readiness-2026-10-20.md` — 演示系统就绪性评估（演示剧本、降级说明、话术）
- `docs/modules-roadmap-2026-10-20.md` — 二期模块路线图（已落地）
- `docs/superpowers/plans/2026-10-21-modules-roadmap.md` — 模块落地实现计划
- `需求/` — 总体建设方案、技术选型、系统架构与模块清单

## 已知边界（诚实清单）

- 视频实时流/云台/截图、远程开门、道闸抬杆、设备重启等**设备指令层**为记录式桩——业务流程、权限、审计已闭环，接入真实设备/流媒体后置 `srs.media.enabled=true` 并替换对应 Service 实现即可
- 登出为无状态 JWT，不做 Token 失效；生产部署建议外置数据库/Redis 密钥、收紧 CORS 白名单
- 仓库无单元测试，验证依赖两个冒烟脚本 + `typecheck`

## License

仅供学习与内部演示使用。
