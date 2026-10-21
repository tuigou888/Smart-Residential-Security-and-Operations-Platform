# 待办模块（Roadmap 剩余 8 个模块）实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（已选定）逐任务实现此计划。步骤使用复选框（`- [ ]`）语法跟踪进度。

**目标：** 按 `docs/modules-roadmap-2026-10-20.md` 落地剩余 8 个业务模块，使其具备后端接口与前端入口，并通过冒烟测试。

**架构：** 沿用现有模块化单体结构：`modules/<域>/{controller,entity,mapper,service}` 四层；实体按表是否含 `del_flag` 选择 `DelEntity`/`BaseEntity`；Controller 统一 `R<T>` + `@PreAuthorize` + `@OperLog`；前端走 `config/modules.ts` 的 `moduleConfig` 配置驱动 CRUD，复杂页面单独建 `views/`。

**技术栈：** Spring Boot 3.3.4 / Java 21 / MyBatis-Plus 3.5.7 / Vue 3.5 + TS + Element Plus。

**规格：** `docs/modules-roadmap-2026-10-20.md`（本计划所有字段、状态码、路径均以该文档与真实 DDL 为准）

---

## 全局约束

- 项目**不是 git 仓库**，计划中的 commit 步骤替换为「编译 + 冒烟验证」。
- 注释、UI 文案、接口 `@Operation(summary=...)` 一律中文。
- 带 `community_id` 的表**必须**已在 `SrsDataPermissionHandler.COMMUNITY_TABLES`（已核对：`pat_*`、`biz_repair`、`sec_blacklist`、`ai_*`、`iot_device_event`、`stat_daily_summary` 均已登记；`msg_template`、`iot_device_type` 未登记且可空，**保持不登记**以免全局模板被小区过滤）。
- 权限串格式 `<域>:<资源>:<动作>`，且**必须**在 `sql/02_init_data.sql` 有对应 `sys_menu.perms` 行。
- 所有写接口加 `@OperLog(module="中文模块名", businessType=1|2|3)`。
- 所有分页接口返回 `PageResult.of(page)`。
- 业务异常统一 `throw BizException.badRequest/notFound/conflict(...)`。
- 单号生成使用 `NoGenerator`；本次顺手把 `Math.random()` 改为 `ThreadLocalRandom`（并发唯一性下限），不引入雪花算法（YAGNI，唯一约束兜底）。
- 表字段与 DDL 严格一致；`del_flag` 表用 `DelEntity`。
- 每个任务结束必须：`mvn -q -DskipTests compile` 通过；任务含前端改动时 `npm run typecheck` 通过。
- 冒烟脚本新增项追加到 `backend/smoke-test.sh` 对应分节。

## 审查重点（Review Focus）

1. **跨小区越权**：非超管用户调用新模块列表/详情时，必须只能看到 `community_id` 在本小区范围内的数据 —— 由数据权限拦截器覆盖，但新表名必须拼写正确（如 `pat_route_point` 未登记，需在查询层用 routeId 间接约束）。
2. **单号并发重复**：报修 `repair_no` 唯一索引冲突 —— 生成器需用 `ThreadLocalRandom` 且捕获 `DuplicateKeyException` 后重试一次。
3. **状态机非法跳转**：报修状态 0→1→2→3→4，接口层必须校验当前状态，禁止跳级。
4. **模板变量注入/异常**：`msg_template` 渲染时变量缺失应保留占位符或空串，不得抛 NPE；渲染结果不得执行任何表达式。
5. **定时任务重复执行**：日汇总与漏巡判定用 `saveOrUpdate` 按唯一键幂等，重复执行不得产生重复行。

---

## 文件结构总览

```
backend/src/main/java/com/srs/modules/
├── iot/
│   ├── controller/IotDeviceTypeController.java      [新增]
│   ├── controller/IotDeviceEventController.java     [新增]
│   ├── entity/IotDeviceEvent.java                   [新增]
│   ├── mapper/IotDeviceEventMapper.java             [新增]
│   └── service/IotDeviceEventService.java           [新增]
├── patrol/                                          [新增模块]
│   ├── controller/{PatPoint,PatRoute,PatPlan,PatRecord}Controller.java
│   ├── entity/{PatPoint,PatRoute,PatRoutePoint,PatPlan,PatRecord}.java
│   ├── mapper/{...}Mapper.java
│   └── service/{PatPoint,PatRoute,PatPlan,PatRecord}Service.java
├── repair/                                          [新增模块]
│   ├── controller/RepairController.java
│   ├── entity/BizRepair.java
│   ├── mapper/BizRepairMapper.java
│   └── service/RepairService.java
├── msg/
│   ├── controller/MsgTemplateController.java        [新增]
│   ├── entity/MsgTemplate.java                      [新增]
│   ├── mapper/MsgTemplateMapper.java                [新增]
│   └── service/MsgTemplateService.java              [新增]
├── sec/
│   ├── controller/BlacklistController.java          [新增]
│   └── service/BlacklistService.java                [新增]
├── ai/                                              [新增模块]
│   ├── controller/AiSceneController.java
│   ├── controller/AiEventController.java
│   ├── entity/{AiSceneConfig,AiEventRecord}.java
│   ├── mapper/{...}Mapper.java
│   └── service/{AiSceneService,AiEventService}.java
└── stat/
    ├── controller/DailySummaryController.java       [新增]
    ├── entity/StatDailySummary.java                 [新增]
    ├── mapper/StatDailySummaryMapper.java           [新增]
    └── service/DailySummaryService.java             [新增]

frontend/src/
├── api/index.ts                                     [修改：新增 7 组 API]
├── config/modules.ts                                [修改：新增/修复 8 个 moduleConfig]
├── router/index.ts                                  [修改：componentMap 增加专用页]
└── views/
    ├── patrol/route/index.vue                       [新增]
    ├── patrol/plan/index.vue                        [新增]
    ├── patrol/record/index.vue                      [新增]
    ├── repair/list/index.vue                        [新增]
    ├── msg/template/index.vue                       [新增]
    ├── access/blacklist/index.vue                   [新增]
    ├── iot/event/index.vue                          [新增]
    ├── ai/scene/index.vue                           [新增]
    ├── ai/event/index.vue                           [新增]
    └── stat/daily/index.vue                         [新增]

sql/02_init_data.sql                                 [修改：菜单与权限]
backend/smoke-test.sh                               [修改：新增接口校验]
CLAUDE.md                                           [修改：模块清单/表数说明]
```

---

### 任务 1：设备类型 CRUD（P0，最小闭环）

**文件：**
- 创建：`backend/src/main/java/com/srs/modules/iot/controller/IotDeviceTypeController.java`
- 创建：`backend/src/main/java/com/srs/modules/iot/service/IotDeviceTypeService.java`
- 修改：`frontend/src/api/index.ts`（`iotApi` 增加 4 个方法）
- 修改：`frontend/src/config/modules.ts:190-198`（修正取数接口并补写操作）

**Interfaces（后续任务可依赖）：**
```java
@RestController @RequestMapping("/api/v1/iot/device-type")
class IotDeviceTypeController {
    R<PageResult<IotDeviceType>> list(PageQuery q, Long communityId, String typeName)
    R<List<IotDeviceType>> options()
    R<Void> save(IotDeviceType entity)          // POST，id 为空新增
    R<Void> update(IotDeviceType entity)        // PUT
    R<Void> remove(List<Long> ids)              // DELETE /{ids}
}
class IotDeviceTypeService extends ServiceImpl<IotDeviceTypeMapper, IotDeviceType> {
    IPage<IotDeviceType> page(PageQuery q, Long communityId, String typeName)
    void saveType(IotDeviceType entity)         // 编码重复抛 BizException.conflict
    void removeTypes(List<Long> ids)            // 有子类型时抛 conflict
}
```

- [ ] **步骤 1：编写后端 Service**

`page` 用 `LambdaQueryWrapper`：`eq(communityId!=null, ...)`、`like(StringUtils.hasText(typeName), IotDeviceType::getTypeName, typeName)`、`orderByAsc(orderNum)`。
`saveType`：`typeCode` 非空校验；`count(typeCode=x, id ne 当前id)>0` 抛 `BizException.conflict("类型编码已存在")`；id 为空 `insert` 否则 `updateById`。
`removeTypes`：删除前 `count(parentId in ids) > 0` 抛 `conflict("存在子类型，无法删除")`。

- [ ] **步骤 2：编写 Controller**

权限串：`iot:deviceType:list`（list/options）、`iot:deviceType:add`（save）、`iot:deviceType:edit`（update）、`iot:deviceType:remove`（remove）。保存与删除加 `@OperLog(module="设备类型", businessType=1/2/3)`。

- [ ] **步骤 3：编译验证**

运行：`cd backend && mvn -q -DskipTests compile`
预期：BUILD SUCCESS（无输出即成功）

- [ ] **步骤 4：前端 API**

`frontend/src/api/index.ts` 的 `iotApi` 追加：
```ts
deviceTypes: (q: any) => get('/iot/device-type/list', q),
deviceTypeOptions: () => get('/iot/device-type/options'),
saveDeviceType: (d: any) => (d.id ? put('/iot/device-type', d) : post('/iot/device-type', d)),
removeDeviceType: (ids: any) => del('/iot/device-type/' + ids),
```

- [ ] **步骤 5：修正 moduleConfig**

把 `/iot/device-type` 的 `fetch` 从 `iotApi.devices(...)`（错误取数）改为 `wrap((q) => iotApi.deviceTypes(q))(p)`；补 `searchFields`（typeName/status）、`formFields`（typeCode/typeName/parentId/icon/orderNum/status）、`createApi`、`updateApi`、`removeApi`、`addText: '新增类型'`。

- [ ] **步骤 6：前端类型检查**

运行：`cd frontend && npm run typecheck`
预期：退出码 0

- [ ] **步骤 7：菜单权限 SQL**

在 `sql/02_init_data.sql` 末尾追加（设备类型菜单 id=32 已存在，只补 F 行）：
```sql
INSERT INTO sys_menu (id,parent_id,menu_name,menu_type,perms,order_num,create_by) VALUES
(104,32,'设备类型新增','F','iot:deviceType:add',1,'admin'),
(105,32,'设备类型修改','F','iot:deviceType:edit',2,'admin'),
(106,32,'设备类型删除','F','iot:deviceType:remove',3,'admin');
```
执行：`mysql -uroot -p123456 srs_platform < sql/02_init_data.sql` 前需确认脚本幂等（见任务 9 统一处理）。

- [ ] **步骤 8：冒烟验证**

`backend/smoke-test.sh` 的「设备与视频」节追加：
```bash
check "设备类型列表" "/api/v1/iot/device-type/list?pageNum=1&pageSize=10"
check "设备类型新增" "/api/v1/iot/device-type" POST '{"typeCode":"TEST_TMP","typeName":"临时类型","status":1}'
```
运行：`bash backend/smoke-test.sh`
预期：新增行 PASS

---

### 任务 2：巡更点位 + 路线（含路线点位编排）

**文件：**
- 创建：`backend/src/main/java/com/srs/modules/patrol/entity/{PatPoint,PatRoute,PatRoutePoint}.java`
- 创建：`backend/src/main/java/com/srs/modules/patrol/mapper/{PatPoint,PatRoute,PatRoutePoint}Mapper.java`
- 创建：`backend/src/main/java/com/srs/modules/patrol/service/{PatPointService,PatRouteService}.java`
- 创建：`backend/src/main/java/com/srs/modules/patrol/controller/{PatPointController,PatRouteController}.java`
- 创建：`frontend/src/views/patrol/route/index.vue`
- 修改：`frontend/src/api/index.ts`、`frontend/src/config/modules.ts`、`frontend/src/router/index.ts`

**Interfaces：**
```java
// PatPointService extends ServiceImpl<PatPointMapper, PatPoint>
IPage<PatPoint> page(PageQuery q, Long communityId, String pointName)
void savePoint(PatPoint p)      // pointCode 唯一
void removePoints(List<Long> ids)

// PatRouteService extends ServiceImpl<PatRouteMapper, PatRoute>
IPage<PatRoute> page(PageQuery q, Long communityId, String routeName)
PatRoute detail(Long id)                    // 含 points: List<PatPoint>（按 orderNum）
void saveRoute(PatRoute r, List<Long> pointIds)   // 事务：写 route + 重建 route_point
void removeRoutes(List<Long> ids)                 // 级联删 route_point；有计划引用则 conflict
```

**端点：**
- `GET/POST/PUT/DELETE /api/v1/patrol/point`（`/list`、`/options`）
- `GET/POST/PUT/DELETE /api/v1/patrol/route`（`/list`、`/{id}`）
- 权限：`patrol:point:list/add/edit/remove`、`patrol:route:list/add/edit/remove`

- [ ] **步骤 1：实体类**

三表均含 `del_flag` → 继承 `DelEntity`；`@TableName("pat_point")` 等；`@TableId(type = IdType.ASSIGN_ID)`；字段按 DDL 逐一映射（`PatRoutePoint` 另含 `communityId`，DDL 可空）。

> **预检裁定：** `IotDeviceType` 当前继承 `BaseEntity`，但表含 `del_flag`；任务 1 中改为继承 `DelEntity`，否则删除为物理删除且与全局约定不符。

- [ ] **步骤 2：Mapper**

```java
@Mapper public interface PatPointMapper extends BaseMapper<PatPoint> {}
@Mapper public interface PatRouteMapper extends BaseMapper<PatRoute> {}
@Mapper public interface PatRoutePointMapper extends BaseMapper<PatRoutePoint> {}
```

- [ ] **步骤 3：PatPointService**

`savePoint`：`pointCode` 唯一校验（同 id 排除）；新增/更新。`removePoints`：直接 `removeByIds`（逻辑删除）。

- [ ] **步骤 4：PatRouteService（事务）**

`saveRoute`：
```java
@Transactional(rollbackFor = Exception.class)
public void saveRoute(PatRoute r, List<Long> pointIds) {
    // 1. routeCode 唯一校验
    // 2. insert 或 updateById
    // 3. delete from pat_route_point where route_id = r.id
    // 4. 按 pointIds 顺序插入，orderNum = index+1
    // 5. 回写 pointCount = pointIds.size()
}
```
`detail`：route + `pointIds` 关联查询出的点位列表（`orderByAsc(orderNum)`）。
`removeRoutes`：先查 `pat_plan where route_id in ids and del_flag=0`，非空抛 `conflict("路线已被巡更计划引用")`；再删关联与路线。

- [ ] **步骤 5：Controller**

按现有风格（`@RequiredArgsConstructor`、`R.data`、`@OperLog`）。路线保存请求体用内部静态类：
```java
public record RouteSaveRequest(PatRoute route, List<Long> pointIds) {}
```
`@PostMapping("/route")` 与 `@PutMapping("/route")` 均接收该结构。

- [ ] **步骤 6：编译验证**

`cd backend && mvn -q -DskipTests compile` → BUILD SUCCESS

- [ ] **步骤 7：前端 API**

`api/index.ts` 新增：
```ts
export const patrolApi = {
  points: (q: any) => get('/patrol/point/list', q),
  pointOptions: (c: any) => get('/patrol/point/options', { communityId: c }),
  savePoint: (d: any) => (d.id ? put('/patrol/point', d) : post('/patrol/point', d)),
  removePoint: (ids: any) => del('/patrol/point/' + ids),
  routes: (q: any) => get('/patrol/route/list', q),
  routeDetail: (id: any) => get('/patrol/route/' + id),
  saveRoute: (d: any) => (d.route?.id ? put('/patrol/route', d) : post('/patrol/route', d)),
  removeRoute: (ids: any) => del('/patrol/route/' + ids)
}
```

- [ ] **步骤 8：moduleConfig**

新增 `/patrol/point`（纯 CRUD，配置 `createApi/updateApi/removeApi`）；`/patrol/route` 走专用页。

- [ ] **步骤 9：路线编排页**

`views/patrol/route/index.vue`：左侧点位多选列表（`el-checkbox-group`），右侧已选顺序列表（上移/下移按钮，避免引入拖拽依赖），保存时提交 `{route, pointIds}`。列表页展示 routeName/routeCode/pointCount/status + 编辑/删除。

- [ ] **步骤 10：路由映射**

`router/index.ts` 的 `componentMap` 增加 `'patrol/route/index': () => import('@/views/patrol/route/index.vue')`。

- [ ] **步骤 11：类型检查**

`cd frontend && npm run typecheck` → 0

---

### 任务 3：巡更计划 + 记录 + 漏巡判定

**文件：**
- 创建：`backend/.../patrol/entity/{PatPlan,PatRecord}.java`
- 创建：`backend/.../patrol/mapper/{PatPlan,PatRecord}Mapper.java`
- 创建：`backend/.../patrol/service/{PatPlanService,PatRecordService}.java`
- 创建：`backend/.../patrol/controller/{PatPlanController,PatRecordController}.java`
- 创建：`frontend/src/views/patrol/plan/index.vue`、`frontend/src/views/patrol/record/index.vue`
- 修改：`frontend/src/api/index.ts`、`modules.ts`、`router/index.ts`

**Interfaces：**
```java
// PatPlanService extends ServiceImpl<PatPlanMapper, PatPlan>
IPage<PatPlan> page(PageQuery q, Long communityId, String planName)
void savePlan(PatPlan p)        // routeId 必须存在
void removePlans(List<Long> ids)

// PatRecordService extends ServiceImpl<PatRecordMapper, PatRecord>
IPage<PatRecord> page(PageQuery q, Long communityId, Long planId, Long userId, Integer status)
PatRecord checkIn(PatRecord r)  // 补 communityId/planId/routeId、userName、checkTime；status 默认 1
Map<String,Object> stat(Long communityId, LocalDate date) // 应巡/实巡/漏巡/异常
int markMissing(LocalDate date) // 幂等：同一 plan+point+date 只标记一次
```

**端点：** `/api/v1/patrol/plan`、`/api/v1/patrol/record`（`/check-in`、`/stat`）。

- [ ] **步骤 1：实体 + Mapper**

均继承 `DelEntity`，字段严格按 DDL（`pat_plan.start_time`/`end_time` 为 `String`；`pat_record.check_time` 为 `LocalDateTime`；经纬度 `BigDecimal`）。

- [ ] **步骤 2：PatPlanService**

`savePlan`：校验 route 存在（`pat_route` 未删除）；`startTime/endTime` 格式校验 `HH:mm`（`DateTimeFormatter.ofPattern("HH:mm")` 解析失败抛 `badRequest`）；`intervalMinutes` 必须 > 0。删除计划不级联记录（保留历史）。

- [ ] **步骤 3：PatRecordService.checkIn**

```java
@Transactional
public PatRecord checkIn(PatRecord r) {
    // plan = planMapper.selectById(r.planId); 不存在 -> notFound
    // r.communityId = plan.communityId; r.routeId = plan.routeId
    // r.userId 为空 -> SecurityUtils.getUserId(); r.userName 为空 -> getRealName()
    // r.checkTime 为空 -> now(); r.status 为空 -> 1
    // insert，返回
}
```

- [ ] **步骤 4：漏巡判定（幂等）**

```java
/** 对指定日期：按计划应打卡点位与已打卡记录比对，缺失写入 status=3 的漏巡记录 */
@Transactional
public int markMissing(LocalDate date) {
    // 1. 取启用计划 + 其路线点位
    // 2. 对每个 (planId, pointId)：count(plan_id, point_id, check_time in [date 00:00, date+1 00:00), status=3) > 0 则跳过
    //    且 count(正常打卡 status=1/2) > 0 则跳过
    // 3. 否则 insert PatRecord(status=3, checkMethod=1, checkTime=date.atTime(23,59,59),
    //    userId=计划首个 assignUserIds, userName 留空, exceptionDesc="系统判定漏巡")
    // 返回新增条数
}
```

- [ ] **步骤 5：定时任务**

新建 `backend/.../patrol/service/PatrolScheduleJob.java`：
```java
@Component @RequiredArgsConstructor
public class PatrolScheduleJob {
    private final PatRecordService recordService;
    @Scheduled(cron = "0 5 0 * * ?")   // 每日 00:05，判定前一天
    public void run() { recordService.markMissing(LocalDate.now().minusDays(1)); }
}
```
需确认启动类已开启 `@EnableScheduling`；未开启则在 `SrsApplication` 追加。

- [ ] **步骤 6：Controller + 权限**

`patrol:plan:list/add/edit/remove`、`patrol:record:list/add`（`/check-in` 用 add）、`patrol:record:stat`。

- [ ] **步骤 7：编译验证**

`mvn -q -DskipTests compile`

- [ ] **步骤 8：前端**

- `/patrol/plan`：配置驱动 CRUD；`routeId` 用 `patrolApi` 的路线下拉（`formFields` 中 `type: 'select'` + `options` 由页面加载后注入）。
- `/patrol/record`：专用页，顶部日期选择 + 计划/人员/状态筛选，表格展示打卡时间、方式、状态、异常描述；`stat` 区展示应巡/实巡/漏巡。
- `componentMap` 增加 `patrol/record/index`。

- [ ] **步骤 9：类型检查**

`npm run typecheck` → 0

---

### 任务 4：报修投诉

**文件：**
- 创建：`backend/.../repair/entity/BizRepair.java`
- 创建：`backend/.../repair/mapper/BizRepairMapper.java`
- 创建：`backend/.../repair/service/RepairService.java`
- 创建：`backend/.../repair/controller/RepairController.java`
- 修改：`backend/.../common/util/NoGenerator.java`（`ThreadLocalRandom`）
- 创建：`frontend/src/views/repair/list/index.vue`
- 修改：`api/index.ts`、`modules.ts`、`router/index.ts`

**Interfaces：**
```java
// RepairService extends ServiceImpl<BizRepairMapper, BizRepair>
IPage<BizRepair> page(PageQuery q, Long communityId, Integer status, Integer repairType, String keyword)
Long createRepair(BizRepair r)      // 生成 repairNo；status=0；可选自动派单
BizRepair detail(Long id)
void dispatch(Long id, Long assigneeId, String assigneeName)  // 0 -> 1，创建工单
void start(Long id)                  // 1 -> 2
void finish(Long id, String remark)  // 2 -> 3
void evaluate(Long id, String content) // 3 -> 4
```

**端点：** `GET /api/v1/repair/list`、`GET /api/v1/repair/{id}`、`POST /api/v1/repair`、`PUT /api/v1/repair/dispatch|start|finish|evaluate`。权限 `repair:list/add/handle/query`。

- [ ] **步骤 1：NoGenerator 并发修正**

`next(prefix)` 内 `Math.random()` 替换为 `java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 10000)`；保持 4 位。`repairNo()` 复用 `next("RP")`。

- [ ] **步骤 2：实体 + Mapper**

`BizRepair extends DelEntity`；`effectiveTime` 等按 DDL：`expectTime` 为 `LocalDateTime`；`status` 为 `Integer`。

- [ ] **步骤 3：RepairService.createRepair**

```java
@Transactional
public Long createRepair(BizRepair r) {
    // 必填：communityId/ownerId/title；缺失 -> badRequest
    // r.repairNo = NoGenerator.repairNo(); r.status = 0
    // try insert；catch DuplicateKeyException -> 重新生成一次再 insert（审查重点 2）
    // 返回 id
}
```

- [ ] **步骤 4：状态机**

统一私有方法 `requireStatus(BizRepair r, int expected)`，不匹配抛 `badRequest("当前状态不允许该操作")`。
- `dispatch`：0→1，并调用 `WorkOrderService.create(...)` 创建 `orderType="repair"`、`sourceType="repair"`、`sourceId=repair.id` 的工单，回写 `workOrderId`。
- `start`：1→2；`finish`：2→3 并写 `remark`；`evaluate`：3→4。

- [ ] **步骤 5：Controller + 权限 + OperLog**

`@OperLog(module="报修管理", businessType=1/2)`。

- [ ] **步骤 6：编译验证**

`mvn -q -DskipTests compile`

- [ ] **步骤 7：前端页面**

`/repair/list`：筛选（状态/类型/关键字）+ 表格 + 新建弹窗（业主选择、房屋、类型、标题、内容、联系电话、期望时间）+ 行操作（派单/开始/完成/评价，按状态显隐）。`repairApi` 加入 `api/index.ts`。`componentMap` 增加映射。

- [ ] **步骤 8：类型检查**

`npm run typecheck` → 0

---

### 任务 5：消息模板

**文件：**
- 创建：`backend/.../msg/entity/MsgTemplate.java`、`mapper/MsgTemplateMapper.java`、`service/MsgTemplateService.java`、`controller/MsgTemplateController.java`
- 创建：`frontend/src/views/msg/template/index.vue`
- 修改：`api/index.ts`、`modules.ts`、`router/index.ts`

**Interfaces：**
```java
IPage<MsgTemplate> page(PageQuery q, String templateName, Integer channelType)
void saveTemplate(MsgTemplate t)     // templateCode 唯一
void removeTemplates(List<Long> ids)
String render(Long templateId, Map<String,Object> params)  // {key} 替换；缺失保留占位符
```
**端点：** `/api/v1/msg/template`（list、POST、PUT、DELETE、POST `/render`）。权限 `msg:template:list/add/edit/remove`。

- [ ] **步骤 1：实体 + Mapper**

`MsgTemplate extends DelEntity`；`contentTemplate` 为 `String`（TEXT）。

- [ ] **步骤 2：Service.render（审查重点 4）**

用正则 `\{(\w+)\}` 匹配，命中则替换为 `String.valueOf(params.get(key))`，**未命中保留原占位符**；`params` 为 null 时返回原文。禁止任何脚本求值。

- [ ] **步骤 3：Controller + 编译**

写接口加 `@OperLog(module="消息模板", ...)`；`render` 用 `businessType=0` 或不加。`mvn -q -DskipTests compile`。

- [ ] **步骤 4：前端**

`/msg/template` 配置驱动 CRUD；表单含 `contentTemplate` 多行文本；列表操作「预览」弹窗调用 render（传示例参数 `{ownerName:'张三'}`）。

- [ ] **步骤 5：类型检查**

`npm run typecheck` → 0

---

### 任务 6：黑名单

**文件：**
- 创建：`backend/.../sec/service/BlacklistService.java`
- 创建：`backend/.../sec/controller/BlacklistController.java`
- 创建：`frontend/src/views/access/blacklist/index.vue`
- 修改：`api/index.ts`、`modules.ts`、`router/index.ts`
- 修改：`backend/.../sec/service/AccessService.java`（开门前置校验）

**Interfaces：**
```java
IPage<SecBlacklist> page(PageQuery q, Long communityId, Integer blacklistType, String keyword)
void saveBlacklist(SecBlacklist b)
void removeBlacklist(List<Long> ids)
boolean isBlocked(Long communityId, Integer type, String targetId)  // type 1人员 2车辆；含生效期与 status=1
```
**端点：** `/api/v1/access/blacklist`（list、POST、PUT、DELETE）。权限 `access:blacklist:list/add/edit/remove`。

- [ ] **步骤 1：Service**

`isBlocked`：`eq(communityId)`、`eq(blacklistType)`、`eq(status,1)`、`eq(targetId)`，生效期条件：
```java
.and(w -> w.isNull(SecBlacklist::getEffectiveStart).or().le(SecBlacklist::getEffectiveStart, LocalDate.now()))
.and(w -> w.isNull(SecBlacklist::getEffectiveEnd).or().ge(SecBlacklist::getEffectiveEnd, LocalDate.now()))
```
`saveBlacklist`：`targetId` 必填；同一小区同类型同 target 且启用中不得重复（conflict）。

- [ ] **步骤 2：接入远程开门（审查重点 1 的配套）**

现有 `AccessController.OpenBody` 只有 `deviceId/communityId/reason`。追加可选字段 `targetId`、`targetType`（1 人员 / 2 车辆，默认 1），并把它传入 `AccessService.remoteOpen` 的新重载：
```java
public void remoteOpen(Long deviceId, Long communityId, String reason,
                       Integer targetType, String targetId)
```
重载内：`targetId` 非空且 `isBlocked(record.communityId, targetType, targetId)` 为 true 时抛 `BizException.forbidden("该目标在黑名单，禁止开门")`；为兼容旧调用保留三参方法委托到五参（传 null）。

- [ ] **步骤 3：Controller + 编译**

`mvn -q -DskipTests compile`

- [ ] **步骤 4：前端**

`/access/blacklist` 配置驱动 CRUD；类型字典（1人员/2车辆）；生效期日期选择。

- [ ] **步骤 5：类型检查**

`npm run typecheck` → 0

---

### 任务 7：AI 场景配置 + AI 事件

**文件：**
- 创建：`backend/.../ai/entity/{AiSceneConfig,AiEventRecord}.java`
- 创建：`backend/.../ai/mapper/{AiSceneConfig,AiEventRecord}Mapper.java`
- 创建：`backend/.../ai/service/{AiSceneService,AiEventService}.java`
- 创建：`backend/.../ai/controller/{AiSceneController,AiEventController}.java`
- 创建：`frontend/src/views/ai/scene/index.vue`、`frontend/src/views/ai/event/index.vue`
- 修改：`api/index.ts`、`modules.ts`、`router/index.ts`

**Interfaces：**
```java
IPage<AiSceneConfig> page(PageQuery q, Long communityId, String sceneName)
void saveScene(AiSceneConfig s)     // sceneCode 小区内唯一
void removeScenes(List<Long> ids)

IPage<AiEventRecord> pageEvents(PageQuery q, Long communityId, String eventType, Integer handleStatus)
AiEventRecord receive(AiEventRecord e)   // 写库；置信度 >= 阈值时转告警
void handle(Long id, Integer handleStatus)
```
**端点：** `/api/v1/ai/scene`（CRUD）、`/api/v1/ai/event`（`/list`、`/callback`、`/handle`）。权限 `ai:scene:*`、`ai:event:list/handle`。

- [ ] **步骤 1：实体 + Mapper**

两张表均含 `del_flag` → `DelEntity`。`bboxData` 为 `String`（TEXT）。

- [ ] **步骤 2：AiSceneService**

`saveScene`：`sceneCode` 必填；同小区唯一（排除自身）；`threshold` 默认 0.80。

- [ ] **步骤 3：AiEventService.receive**

```java
@Transactional
public AiEventRecord receive(AiEventRecord e) {
    // eventTime 为空 -> now(); handleStatus 默认 0; insert
    // scene = sceneMapper.selectById(e.sceneId)
    // if (scene != null && e.confidence != null && e.confidence >= scene.threshold) {
    //     SecAlarmEvent alarm = new SecAlarmEvent();
    //     alarm.communityId/channelId/snapshotUrl/title/content from event & scene
    //     alarm.alarmType = "ai_" + e.eventType; alarm.sourceType = 2; alarm.alarmLevel = scene.alarmLevel
    //     alarmService.receive(alarm);
    // }
}
```

- [ ] **步骤 4：Controller + 编译**

`mvn -q -DskipTests compile`

- [ ] **步骤 5：前端**

- `/ai/scene`：配置驱动 CRUD；通道多选用 `videoApi.channelTree` 展平后的下拉；算法类型下拉（人脸/入侵/烟火/离岗/徘徊）。
- `/ai/event`：列表 + 状态筛选 + 处置按钮；顶部 KPI（今日事件/未处理）。`componentMap` 增加两条。

- [ ] **步骤 6：类型检查**

`npm run typecheck` → 0

---

### 任务 8：设备事件

**文件：**
- 创建：`backend/.../iot/entity/IotDeviceEvent.java`、`mapper/IotDeviceEventMapper.java`、`service/IotDeviceEventService.java`、`controller/IotDeviceEventController.java`
- 创建：`frontend/src/views/iot/event/index.vue`
- 修改：`api/index.ts`、`modules.ts`、`router/index.ts`

**Interfaces：**
```java
IPage<IotDeviceEvent> page(PageQuery q, Long communityId, Long deviceId, String eventCode, Integer handleStatus)
IotDeviceEvent report(IotDeviceEvent e)   // 补 eventTime/handleStatus；OFFLINE/FAULT 转告警
void handle(Long id)
```
**端点：** `/api/v1/iot/device-event`（`/list`、`/report`、`/handle/{id}`）。权限 `iot:deviceEvent:list/control`。

- [ ] **步骤 1：实体 + Mapper + Service**

`IotDeviceEvent extends DelEntity`。`report`：`communityId` 为空时从 `iot_device` 反查；`eventCode` 必填；插入后若 `eventCode in ("OFFLINE","FAULT")` 调 `alarmService.receive` 生成告警（`alarmType="device_offline"/"device_fault"`，`sourceType=1`）。

- [ ] **步骤 2：Controller + 编译**

`mvn -q -DskipTests compile`

- [ ] **步骤 3：前端**

`/iot/device-event`：列表（设备/事件编码/时间/处理状态）+ 设备下拉 + 处置按钮。`componentMap` 映射。

- [ ] **步骤 4：类型检查**

`npm run typecheck` → 0

---

### 任务 9：统计日汇总 + 定时任务

**文件：**
- 创建：`backend/.../stat/entity/StatDailySummary.java`、`mapper/StatDailySummaryMapper.java`、`service/DailySummaryService.java`、`controller/DailySummaryController.java`
- 创建：`frontend/src/views/stat/daily/index.vue`
- 修改：`api/index.ts`、`modules.ts`、`router/index.ts`

**Interfaces：**
```java
PageResult<StatDailySummary> page(PageQuery q, Long communityId, LocalDate begin, LocalDate end)
StatDailySummary generate(Long communityId, LocalDate date)  // 幂等 upsert
int generateAll(LocalDate date)
```
**端点：** `/api/v1/stat/daily/list`、`POST /api/v1/stat/daily/generate`（权限 `stat:daily:view/generate`）。

- [ ] **步骤 1：实体 + Mapper**

`StatDailySummary extends DelEntity`；`statDate` 为 `LocalDate`；`deviceOnlineRate` 为 `BigDecimal`。

- [ ] **步骤 2：DailySummaryService.generate（审查重点 5）**

按 `(communityId, statDate)` 查已有记录；存在则复用 id 更新，不存在则 insert（**不使用** `saveOrUpdate` 以免唯一键判断依赖 null id 语义）。聚合口径：
- `alarmTotal` = `sec_alarm_event` 当日 `occur_time` 数；`alarmHandled` = 其中 `status=2`
- `accessTotal` = `sec_access_record` 当日 `pass_time` 数
- `vehicleInTotal`/`vehicleOutTotal` = `sec_vehicle_pass` 当日按 `direction` 1/2
- `visitorTotal` = `sec_visitor_invite` 当日 `visit_start`
- `workOrderTotal`/`workOrderFinished` = `biz_work_order` 当日创建 / `status=4`
- `patrolTotal`/`patrolMissing` = `pat_record` 当日 `check_time` / `status=3`
- `deviceOnlineRate` = `iot_device` 中 `online_status=1` 占比 × 100，保留 2 位

各表用对应 Mapper 的 `selectCount`（通过注入 Mapper 实现，避免跨 Service 循环依赖）。

- [ ] **步骤 3：定时任务**

`DailySummaryJob`：`@Scheduled(cron = "0 10 0 * * ?")` 生成前一日全部小区。与任务 3 共用 `@EnableScheduling`。

- [ ] **步骤 4：Controller + 编译**

`mvn -q -DskipTests compile`

- [ ] **步骤 5：前端**

`/stat/daily`：日期范围筛选 + 表格（告警/通行/车辆/访客/工单/巡更/在线率）；「生成昨日汇总」按钮。`componentMap` 映射。

- [ ] **步骤 6：类型检查**

`npm run typecheck` → 0

---

### 任务 10：菜单权限 SQL、冒烟测试与文档同步

**文件：**
- 修改：`sql/02_init_data.sql`
- 修改：`backend/smoke-test.sh`
- 修改：`CLAUDE.md`

- [ ] **步骤 1：新增菜单与权限**

在 `sql/02_init_data.sql` 追加（ID 均未被占用）：

| id | parent | 名称 | 类型 | path | component | perms | order |
|----|--------|------|------|------|-----------|-------|-------|
| 110 | 0 | 巡更管理 | M | /patrol | NULL | NULL | 22 |
| 111 | 110 | 巡更点位 | C | /patrol/point | patrol/point/index | patrol:point:list | 1 |
| 112 | 110 | 巡更路线 | C | /patrol/route | patrol/route/index | patrol:route:list | 2 |
| 113 | 110 | 巡更计划 | C | /patrol/plan | patrol/plan/index | patrol:plan:list | 3 |
| 114 | 110 | 巡更记录 | C | /patrol/record | patrol/record/index | patrol:record:list | 4 |
| 115-126 | 111-114 | 各资源 add/edit/remove | F | NULL | NULL | `patrol:<res>:add/edit/remove` | 1-3 |
| 130 | 0 | 报修管理 | M | /repair | NULL | NULL | 23 |
| 131 | 130 | 报修列表 | C | /repair/list | repair/list/index | repair:list | 1 |
| 132-134 | 131 | 新增/处理/查询 | F | NULL | NULL | `repair:add/handle/query` | 1-3 |
| 140 | 0 | 智能分析 | M | /ai | NULL | NULL | 26 |
| 141 | 140 | 场景配置 | C | /ai/scene | ai/scene/index | ai:scene:list | 1 |
| 142 | 140 | 事件记录 | C | /ai/event | ai/event/index | ai:event:list | 2 |
| 143-148 | 141/142 | add/edit/remove、handle | F | NULL | NULL | `ai:scene:*`、`ai:event:handle` | — |
| 55 | 50 | 黑名单 | C | /access/blacklist | access/blacklist/index | access:blacklist:list | 5 |
| 56-58 | 55 | add/edit/remove | F | NULL | NULL | `access:blacklist:*` | 1-3 |
| 93 | 90 | 消息模板 | C | /msg/template | msg/template/index | msg:template:list | 3 |
| 94 | 93 | add/edit/remove | F | NULL | NULL | `msg:template:*` | 1-3 |
| 34 | 30 | 设备事件 | C | /iot/device-event | iot/event/index | iot:deviceEvent:list | 4 |
| 35 | 34 | 处置 | F | NULL | NULL | `iot:deviceEvent:control` | 1 |
| 107 | 95 | 日报汇总 | C | /stat/daily | stat/daily/index | stat:daily:view | 5 |
| 108 | 107 | 生成汇总 | F | NULL | NULL | stat:daily:generate | 1 |

`02_init_data.sql` 需幂等：使用 `INSERT ... ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name)` 或先 `DELETE FROM sys_menu WHERE id BETWEEN 110 AND 148`。

- [ ] **步骤 2：授权给非超管角色**

`sys_role_menu` 为 `wuye`、`anbao` 角色插入新菜单（至少 list 权限），保证非超管演示账号可见。id 用脚本内 `SELECT` 子查询或固定 id（角色 id 已核对：1 admin、2 物业、3 安保）。

- [ ] **步骤 3：冒烟脚本**

在 `backend/smoke-test.sh` 追加分节：
```bash
echo "== 新增业务模块 =="
check "巡更点位" "/api/v1/patrol/point/list?pageNum=1&pageSize=10"
check "巡更路线" "/api/v1/patrol/route/list?pageNum=1&pageSize=10"
check "巡更计划" "/api/v1/patrol/plan/list?pageNum=1&pageSize=10"
check "巡更记录" "/api/v1/patrol/record/list?pageNum=1&pageSize=10"
check "报修列表" "/api/v1/repair/list?pageNum=1&pageSize=10"
check "消息模板" "/api/v1/msg/template/list?pageNum=1&pageSize=10"
check "黑名单" "/api/v1/access/blacklist/list?pageNum=1&pageSize=10"
check "AI场景" "/api/v1/ai/scene/list?pageNum=1&pageSize=10"
check "AI事件" "/api/v1/ai/event/list?pageNum=1&pageSize=10"
check "设备事件" "/api/v1/iot/device-event/list?pageNum=1&pageSize=10"
check "日报汇总" "/api/v1/stat/daily/list?pageNum=1&pageSize=10"
```

- [ ] **步骤 4：重启后端并全量冒烟**

```bash
# 停止旧进程后重启
cd backend && nohup mvn spring-boot:run > /tmp/srs-backend.log 2>&1 &
# 等待端口就绪
until curl -s -m 2 http://127.0.0.1:8088/api/v1/auth/check >/dev/null; do sleep 2; done
bash backend/smoke-test.sh
```
预期：`失败 0`，总数 ≥ 74。

- [ ] **步骤 5：前端构建验证**

```bash
cd frontend && npm run typecheck && npm run build
```
预期：均退出码 0（build 允许既有 chunk 警告）。

- [ ] **步骤 6：更新 CLAUDE.md**

同步：模块清单增加 patrol/repair/ai/message-template/blacklist/device-event/daily-summary；`功能实现情况报告.md` 中对应「未实现」条目改为已实现；表数量不变（未新增表）。

---

## 自检记录

- **规格覆盖**：roadmap 8 个模块全部有对应任务（1、2-3、4、5、6、7、8、9），菜单/冒烟/文档在任务 10。
- **步骤扫描**：每个任务均给出确切文件、签名、算法要点与验证命令；无「待定」「适当处理」类空洞步骤。
- **类型一致性**：统一 `PageQuery/PageResult/R/BizException`；`PatRouteService.saveRoute(PatRoute, List<Long>)` 与前端 `{route, pointIds}` 一致；`RepairService` 状态机 0-4 与 DDL 注释一致。
- **审查重点覆盖**：越权（任务 2 的 route_point 间接约束说明 + 已登记表）、单号并发（任务 4 步骤 1/3）、状态机（任务 4 步骤 4）、模板渲染（任务 5 步骤 2）、定时幂等（任务 3 步骤 4 / 任务 9 步骤 2）。
- **比例**：不含实现代码全文，仅签名与关键算法，符合计划粒度。

---

*计划版本：v1.0 · 2026-10-21*
