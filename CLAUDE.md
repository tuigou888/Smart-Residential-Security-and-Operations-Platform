# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

智慧安防小区运营平台(一期模块化单体),前后端分离:

- `backend/` — Spring Boot 3.3.4 + Java 21 + MyBatis-Plus 3.5.7,MySQL 8 + Redis,端口 8088
- `frontend/` — Vue 3.5 + TypeScript + Vite 5 + Element Plus + Pinia,开发端口 5175(strictPort;5173 被本机其他项目 Vite 长期占用),代理 `/api` → 8088
- `sql/` — 建库脚本:`01_schema.sql`(DDL,48 张表)→ `02_init_data.sql`(初始数据)。初始演示账号见 `02_init_data.sql`，不要在代码或提交中追加新的明文凭据
- `需求/` — 建设方案、技术选型等需求文档
- `功能实现情况报告.md` — 各模块实现度审计报告(2026-09-15),含已知缺陷清单、桩接口清单、未实现模块清单。改动前先读它,避免重复踩已记录的问题

项目内注释、UI 文案、文档均为中文;新增代码保持中文注释风格。

## 常用命令

```bash
# 后端(在 backend/ 下)
mvn spring-boot:run          # 启动,端口 8088;依赖本机 MySQL(127.0.0.1:3306/srs_platform)与 Redis(6379)
mvn test                     # 运行现有测试;当前仓库没有 src/test 单元测试
mvn clean package            # 打包为 srs-platform.jar

# 前端(在 frontend/ 下)
npm run dev                  # Vite 开发服务器 :5173
npm run typecheck            # vue-tsc --noEmit,提交前必须通过
npm run build                # 产物输出到 dist/

# 本地初始化数据库(后端启动前,按顺序执行;凭据以本机配置为准)
mysql -uroot -p < sql/01_schema.sql
mysql -uroot -p < sql/02_init_data.sql

# 冒烟测试(需后端已在 8088 运行;无单元测试,验证靠这两个脚本 + typecheck)
backend/smoke-test.sh        # 全量接口冒烟:登录后逐一校验 ~50 个接口 HTTP 200 且业务码 200
backend/fe-smoke.sh          # 前端依赖接口冒烟:校验前端实际调用的接口存在
```

## 后端架构

包根 `com.srs`,启动类 `SrsApplication`(`@MapperScan("com.srs.**.mapper")`,无 XML mapper,`resources` 下仅 `application.yml`)。

- `modules/{system,base,iot,video,sec,workorder,msg,stat,file}` — 9 个业务模块,各自 controller/service/entity/mapper 四层
- `common/core` — 统一响应 `R<T>`(code 复用 HTTP 语义,200 成功)、`PageQuery`/`PageResult` 分页、`BaseEntity`(create_by/create_time 等审计字段,由 `FillMetaObjectHandler` 自动填充)、`DelEntity`(`@TableLogic` 逻辑删除)
- `common/anno/OperLog` + `OperLogAspect` — 写操作加 `@OperLog(module=..., businessType=...)` 记审计日志
- `common/util/AesCrypto` — 业主手机/证件等敏感字段 AES 加密(密钥 `srs.crypto.aes-key`)
- `common/exception` — 业务异常抛 `BizException`,由 `GlobalExceptionHandler` 统一转 `R`

### 认证与权限(改接口必读)

链路:CORS → JWT 认证(OAuth2 Resource Server,RS256)→ `@PreAuthorize` 方法授权 → 业务。

1. 登录 `/api/v1/auth/login` 签发 JWT;密钥持久化在 `./data/jwt-jwk.json`,不存在时自动生成
2. `JwtAuthConverter` 把 token 转为 authorities
3. Controller 方法用 `@PreAuthorize("@ss.has('alarm:event:list')")` 鉴权;`SecurityService`(bean 名 `ss`)超管直接放行,其余按 authorities 匹配。**权限串必须与 `sys_menu.perms` 一致**(见 `sql/02_init_data.sql`),否则非超管角色永远 403
4. 免鉴权路径在 `srs.security.permit-paths` 配置

### 数据权限(新增表必读)

`datascope/SrsDataPermissionHandler` 是 MyBatis-Plus 的 `MultiDataPermissionHandler`,按登录用户 `dataScope` 自动给 SQL 追加 `community_id IN (...)`(dataScope=4)或 `dept_id IN (...)`(=3):

- 带 `community_id` 的新业务表**必须**加进 `COMMUNITY_TABLES`,否则小区级用户能看到全量数据
- "无需限制"时必须返回 `null`,不能原样返回 where(会生成 `where AND where`)

### 功能开关(application.yml `srs.*`)

- `srs.media.enabled` — 流媒体(ZLMediaKit)未接入时为 false,`/video/live`、`/playback` 返回演示地址;`ptz`、`snapshot`、`/iot/device/restart`、`/access/remote-open`、`/vehicle/gate/open` 目前都是仅写日志/写记录的桩
- `srs.alarm.auto-dispatch` — 告警自动派单,默认关
- `srs.cache.enabled` — Redis 不可用时自动降级为无缓存

## 前端架构

- `api/request.ts` — axios 封装,baseURL `/api/v1`,自动解包 `R<T>`(只返回 data),业务码 401/HTTP 401/403 清 token 跳登录。token 存 localStorage `accessToken`
- `api/index.ts` — 全部接口按域分组(sysApi/baseApi/iotApi/videoApi/secApi/msgApi/alarmApi/statApi...),新接口加在这里
- 动态路由:路由守卫拉 `/auth/info` 取菜单树,按 `menu.path` 注册路由。`router/index.ts` 的 `componentMap` 把 `sys_menu.component` 字符串映射到专用页面(共 15 个);无映射的走 `views/generic/index.vue` 通用 CRUD,再兜底 placeholder
- **新增页面 = 在 `componentMap` 加映射写专用页面,或在 `config/modules.ts` 加一条 `moduleConfig`**。moduleConfig 以路由 path 为 key,声明 columns/searchFields/formFields 和 fetch/createApi/updateApi/removeApi,`generic/CrudPage.vue` 据此渲染列表+弹窗表单;只配 fetch 就是只读页
- `config/modules.ts` 的 `withCommunity()` 会自动把顶栏切换的当前小区 id(`appStore.communityId`)注入查询参数

## 新增一个业务模块的完整清单

1. 表:加进 `sql/01_schema.sql`;带 `community_id` 的表同步登记 `SrsDataPermissionHandler.COMMUNITY_TABLES`
2. 后端:`modules/<域>/` 下 entity(继承 `BaseEntity`/`DelEntity`)+ mapper(放 `**.mapper` 即被扫描)+ service + controller;接口路径 `/api/v1/<域>/...`;写操作加 `@OperLog`;鉴权用 `@PreAuthorize("@ss.has('<域>:<资源>:<动作>')")`
3. 权限串:在 `sys_menu` 插入对应菜单与按钮权限(perms 与代码一致)
4. 前端:`api/index.ts` 加接口;`modules.ts` 加 moduleConfig(或专用页面 + componentMap 映射)
5. 验证:`npm run typecheck` 通过后,把新接口补进 `smoke-test.sh` 跑一遍

## 指令文件维护规则

当项目结构、构建/测试命令、架构边界、开发约定或本文件记录的其他事实发生变化时,必须在同一次改动中同步更新本文件(含表数量、模块清单、桩接口与功能开关状态)。
