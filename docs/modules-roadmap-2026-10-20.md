# 智慧安防运营平台 — 待办模块开发路线图

> 生成日期：2026-10-20  
> 适用范围：二期功能规划、人力评估、开发排期  
> 数据源：数据库 DDL + 后端代码静态扫描

---

## 1. 模块概览

| 优先级 | 模块 | 关联表 | 后端状态 | 前端状态 | 预估工时 | 依赖项 |
|--------|------|--------|----------|----------|----------|--------|
| **P0** | 设备类型 CRUD | `iot_device_type` | 🟡 Mapper 存在 | ❌ 只读 | 0.5d | 无 |
| **P1** | 巡更管理 | `pat_point/route/route_point/plan/record` | 🟡 Mapper 存在 | ❌ 无 | 3d | 小区数据权限 |
| **P1** | 报修投诉 | `biz_repair` | 🟡 Mapper 存在 | ❌ 无 | 2d | 工单系统 |
| **P1** | 消息模板 | `msg_template` | 🟡 3 条初始数据 | ❌ 无 | 1d | 消息中心 |
| **P2** | 黑名单 | `sec_blacklist` | 🟡 Entity+Mapper 存在 | ❌ 无 | 1d | 门禁/车辆识别 |
| **P2** | AI 场景配置 | `ai_scene_config/event_record` | ❌ 完全未实现 | ❌ 无 | 3d | 算法服务 |
| **P2** | 设备事件 | `iot_device_event` | ❌ 完全未实现 | ❌ 无 | 1d | IoT 设备上报 |
| **P2** | 统计日汇总 | `stat_daily_summary` | ❌ 完全未实现 | ❌ 无 | 1d | 定时任务 |

**总工时预估**：~12 人天（不含测试与联调）

---

## 2. P0 — 设备类型 CRUD（立即可做）

### 2.1 业务价值
- 设备台账分类管理的基础数据
- 当前仅能查看所有类型，无法新增/修改/删除
- 影响设备管理的扩展性

### 2.2 表结构
```sql
CREATE TABLE `iot_device_type` (
  `id` bigint NOT NULL,
  `type_code` varchar(32) NOT NULL COMMENT '类型编码',
  `type_name` varchar(50) NOT NULL COMMENT '类型名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父类型 ID',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `order_num` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1',
  `community_id` bigint DEFAULT NULL,
  -- 标准审计字段略
  UNIQUE KEY `uk_type_code` (`type_code`)
);
```

### 2.3 实现清单

#### 后端（1 小时）
**文件**：`backend/src/main/java/com/srs/modules/iot/controller/IotDeviceTypeController.java`

```java
@RestController
@RequestMapping("/api/v1/iot/device-type")
public class IotDeviceTypeController {
    
    @Autowired
    private IotDeviceTypeService deviceTypeService;

    @GetMapping("/list")
    public R<List<IotDeviceType>> list(IotDeviceTypeQuery query) {
        return R.ok(deviceTypeService.list(query));
    }

    @PostMapping
    public R<Void> save(@RequestBody IotDeviceType entity) {
        deviceTypeService.save(entity);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody IotDeviceType entity) {
        deviceTypeService.updateById(entity);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        deviceTypeService.removeByIds(ids);
        return R.ok();
    }
}
```

**Service/Mapper**：沿用 MyBatis-Plus 自动扫描机制，无需 XML。

#### 前端（2 小时）
**文件**：`frontend/src/config/modules.ts`

```typescript
'/iot/device-type': {
  module: 'iot',
  label: '设备类型',
  path: '/iot/device-type',
  icon: 'Monitor',
  searchFields: [
    { prop: 'typeCode', label: '类型编码', type: 'input' },
    { prop: 'typeName', label: '类型名称', type: 'input' },
    { prop: 'status', label: '状态', type: 'select', options: dict('yes_no') }
  ],
  columns: [
    { prop: 'typeCode', label: '类型编码', width: 120 },
    { prop: 'typeName', label: '类型名称', width: 150 },
    { prop: 'orderNum', label: '排序', width: 80 },
    { prop: 'status', label: '状态', width: 80, type: 'tag', map: yesNoTagMap },
    { prop: 'createTime', label: '创建时间', width: 180 },
    { prop: 'actions', label: '操作', width: 150, fixed: 'right' }
  ],
  formFields: [
    { prop: 'typeCode', label: '类型编码', required: true, type: 'input' },
    { prop: 'typeName', label: '类型名称', required: true, type: 'input' },
    { prop: 'parentId', label: '父类型', type: 'tree-select', props: { data: treeData } },
    { prop: 'icon', label: '图标', type: 'input' },
    { prop: 'orderNum', label: '排序', type: 'number' },
    { prop: 'status', label: '状态', type: 'radio', options: yesNoOptions }
  ],
  fetchApi: (params) => iotApi.deviceTypeList(params),
  createApi: (data) => iotApi.saveDeviceType(data),
  updateApi: (data) => iotApi.updateDeviceType(data),
  removeApi: (ids) => iotApi.removeDeviceType(ids)
}
```

**API**：`frontend/src/api/index.ts`

```typescript
// IotApi 追加
saveDeviceType: (data) => request.post('/iot/device-type', data),
updateDeviceType: (data) => request.put('/iot/device-type', data),
removeDeviceType: (ids) => request.delete(`/iot/device-type/${ids}`),
```

### 2.4 权限串
在 `sql/02_init_data.sql` 追加：

```sql
INSERT INTO sys_menu (menu_name, parent_id, url, perms, menu_type, component, sort) VALUES
('设备类型管理', 18, '/api/v1/iot/device-type', 'iot:deviceType:list', 'C', 'layout', 1, now(), 'admin'),
('设备类型新增', 19, '', 'iot:deviceType:add', 'F', '', 1, now(), 'admin'),
('设备类型修改', 19, '', 'iot:deviceType:edit', 'F', '', 2, now(), 'admin'),
('设备类型删除', 19, '', 'iot:deviceType:remove', 'F', '', 3, now(), 'admin');
```

---

## 3. P1 — 巡更管理

### 3.1 业务场景
- 安保人员按预定路线定时巡检
- NFC 卡/二维码打卡防止假巡
- 漏巡/异常自动告警

### 3.2 表结构（5 张表）

**巡更点位表 `pat_point`**
```sql
CREATE TABLE `pat_point` (
  `id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `point_name` varchar(100) NOT NULL COMMENT '点位名称',
  `point_code` varchar(50) NOT NULL COMMENT '点位编码',
  `location` varchar(255) DEFAULT NULL COMMENT '位置描述',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `nfc_code` varchar(100) DEFAULT NULL COMMENT 'NFC 编码',
  `qr_code` varchar(255) DEFAULT NULL COMMENT '二维码',
  `status` tinyint DEFAULT '1',
  UNIQUE KEY `uk_point_code` (`point_code`)
);
```

**巡更路线表 `pat_route`**
```sql
CREATE TABLE `pat_route` (
  `id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `route_name` varchar(100) NOT NULL,
  `route_code` varchar(50) NOT NULL,
  `point_count` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  UNIQUE KEY `uk_route_code` (`route_code`)
);
```

**路线点位关联 `pat_route_point`**
```sql
CREATE TABLE `pat_route_point` (
  `id` bigint NOT NULL,
  `route_id` bigint NOT NULL,
  `point_id` bigint NOT NULL,
  `order_num` int DEFAULT '0' COMMENT '巡更顺序',
  UNIQUE KEY `uk_route_point` (`route_id`,`point_id`)
);
```

**巡更计划表 `pat_plan`**
```sql
CREATE TABLE `pat_plan` (
  `id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `plan_name` varchar(100) NOT NULL,
  `route_id` bigint NOT NULL,
  `patrol_type` tinyint DEFAULT '1' COMMENT '1 日常 2 夜间 3 节假日',
  `start_time` varchar(10) DEFAULT '20:00',
  `end_time` varchar(10) DEFAULT '06:00',
  `interval_minutes` int DEFAULT '60',
  `assign_user_ids` varchar(500) COMMENT '指定人员 ID'
);
```

**巡更记录表 `pat_record`**
```sql
CREATE TABLE `pat_record` (
  `id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `plan_id` bigint NOT NULL,
  `route_id` bigint NOT NULL,
  `point_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `check_method` tinyint DEFAULT '1' COMMENT '1NFC 2 二维码 3GPS',
  `check_time` datetime NOT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1 正常 2 异常 3 漏巡',
  `exception_desc` varchar(500),
  `images` varchar(1000) COMMENT '现场图片'
);
```

### 3.3 后端实现（1.5 天）

**包结构**：`com.srs.modules.patrol`

```
patrol/
├── controller/
│   ├── PatPointController.java
│   ├── PatRouteController.java
│   ├── PatPlanController.java
│   └── PatRecordController.java
├── entity/
│   ├── PatPoint.java
│   ├── PatRoute.java
│   ├── PatRoutePoint.java
│   ├── PatPlan.java
│   └── PatRecord.java
├── mapper/
│   ├── PatPointMapper.java
│   ├── PatRouteMapper.java
│   ├── PatRoutePointMapper.java
│   ├── PatPlanMapper.java
│   └── PatRecordMapper.java
├── service/
│   ├── PatPointService.java
│   ├── PatRouteService.java
│   ├── PatPlanService.java
│   ├── PatRecordService.java
│   └── impl/
└── dto/
    ├── PatRouteVO.java (含点位列表)
    └── PatRecordQuery.java
```

**核心逻辑**：
1. 点位/路线：标准 CRUD + 数据权限
2. 路线 - 点位关联：保存路线时批量插入 `pat_route_point`
3. 计划：关联路线，支持周期/时间段设置
4. 记录：手机端打卡写入，支持漏巡自动判定（超过计划时间窗口）

**漏巡判定伪代码**：
```java
@Scheduled(cron = "0 0 1 * * ?") // 每天凌晨 1 点执行
public void detectMissingPatrol() {
    LocalDateTime yesterdayEnd = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59);
    LocalDateTime planEnd = LocalDateTime.now().withHour(0).withMinute(0);
    
    List<PatPlan> plans = planService.listActivePlans();
    for (PatPlan plan : plans) {
        long recordCount = recordService.countByPlanAndTimeRange(plan.getId(), plan.getStart(), plan.getEnd());
        long expectedCount = calculateExpectedChecks(plan);
        
        if (recordCount < expectedCount) {
            // 标记漏巡 + 触发告警
            recordService.markMissing(plans);
            alarmService.createMissingPatrolAlarm(plan, expectedCount - recordCount);
        }
    }
}
```

### 3.4 前端实现（1 天）

**菜单路径**：`/patrol/point`, `/patrol/route`, `/patrol/plan`, `/patrol/record`

**页面组件**：
| 路径 | 组件 | 说明 |
|------|------|------|
| `/patrol/point` | `generic/CrudPage.vue` | 通用 CRUD |
| `/patrol/route` | `views/patrol/route/index.vue` | 路线编排（拖拽排序点位） |
| `/patrol/plan` | `views/patrol/plan/index.vue` | 计划 + 人员指派 |
| `/patrol/record` | `views/patrol/record/index.vue` | 打卡记录 + 异常统计 |

**特殊交互**：路线编排页需支持拖拽排序（vue-draggable-next）。

### 3.5 权限串
```sql
INSERT INTO sys_menu (menu_name, parent_id, url, perms, menu_type, component, sort) VALUES
('巡更管理', 4, '', 'patrol:view', 'M', 'layout', 25, now(), 'admin'),
('巡更点位', 45, '/patrol/point', 'patrol:point:list', 'C', 'patrol/point', 1, now(), 'admin'),
('巡更路线', 45, '/patrol/route', 'patrol:route:list', 'C', 'patrol/route', 2, now(), 'admin'),
('巡更计划', 45, '/patrol/plan', 'patrol:plan:list', 'C', 'patrol/plan', 3, now(), 'admin'),
('巡更记录', 45, '/patrol/record', 'patrol:record:list', 'C', 'patrol/record', 4, now(), 'admin');
```

---

## 4. P1 — 报修投诉

### 4.1 业务场景
- 业主通过 App/小程序提交报修
- 物业派单至维修人员
- 与工单系统自动关联

### 4.2 表结构
```sql
CREATE TABLE `biz_repair` (
  `id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `repair_no` varchar(32) NOT NULL COMMENT '报修编号',
  `owner_id` bigint NOT NULL,
  `room_id` bigint DEFAULT NULL,
  `repair_type` tinyint DEFAULT '1' COMMENT '1 水电 2 门窗 3 电梯 4 其他',
  `title` varchar(200) NOT NULL,
  `content` varchar(2000),
  `images` varchar(1000),
  `contact_phone` varchar(20),
  `expect_time` datetime,
  `status` tinyint DEFAULT '0' COMMENT '0 待派单 1 已派单 2 处理中 3 已完成 4 已评价',
  `work_order_id` bigint COMMENT '关联工单 ID',
  UNIQUE KEY `uk_repair_no` (`repair_no`)
);
```

### 4.3 核心逻辑

**单号生成**（修复当前 `NoGenerator` 缺陷）
```java
@Component
public class RepairNoGenerator {
    /**
     * 生成报修编号：RP-YYYYMMDD-NNNN
     * 使用雪花算法保证并发唯一
     */
    public String generate() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = sequenceIdGenerator.nextId() % 10000;
        return String.format("RP-%s-%04d", dateStr, seq);
    }
}
```

**自动派单到工单**
```java
@Service
public class RepairService {
    
    @Autowired
    private WorkOrderService workOrderService;
    
    @Transactional
    public Long createRepair(RepairCreateDTO dto) {
        BizRepair repair = new BizRepair();
        BeanUtils.copyProperties(dto, repair);
        repair.setRepairNo(noGenerator.generate());
        repair.setStatus(0); // 待派单
        
        save(repair);
        
        // 自动创建关联工单
        if (srs.alarm.autoDispatch) {
            WorkOrderCreate woDto = new WorkOrderCreate();
            woDto.setTitle("报修：" + dto.getTitle());
            woDto.setType("REPAIR");
            woDto.setSourceId(repair.getId());
            woDto.setSourceType("REPAIR");
            
            Long woId = workOrderService.create(woDto);
            repair.setWorkOrderId(woId);
            repair.setStatus(1); // 已派单
            updateById(repair);
        }
        
        return repair.getId();
    }
}
```

### 4.4 实现清单

**后端（0.5 天）**
- Controller/Service/Mapper 标准四件套
- 单号生成器
- 自动派单开关

**前端（0.5 天）**
- `views/repair/list.vue` — 列表 + 筛选
- `views/repair/detail.vue` — 详情 + 状态变更
- API 接入

**权限串**
```sql
INSERT INTO sys_menu (menu_name, parent_id, perms, menu_type, component, sort) VALUES
('报修管理', 4, 'repair:view', 'M', 'layout', 20, now(), 'admin'),
('报修列表', 50, '/repair/list', 'repair:list', 'C', 'repair/list', 1, now(), 'admin'),
('报修处理', 50, '', 'repair:handle', 'F', '', 2, now(), 'admin');
```

---

## 5. P1 — 消息模板

### 5.1 业务场景
- 预设公告/通知模板
- 支持变量替换（如 `{ownerName}`, `{roomNo}`）
- 多渠道复用（站内信/短信/推送）

### 5.2 表结构
```sql
CREATE TABLE `msg_template` (
  `id` bigint NOT NULL,
  `template_code` varchar(50) NOT NULL,
  `template_name` varchar(100) NOT NULL,
  `channel_type` tinyint NOT NULL COMMENT '1 站内信 2 短信 3 推送 4 语音',
  `content_template` text NOT NULL,
  `status` tinyint DEFAULT '1',
  `community_id` bigint DEFAULT NULL,
  UNIQUE KEY `uk_template_code` (`template_code`)
);
```

### 5.3 初始数据
```sql
-- 已存在的 3 条模板
INSERT INTO msg_template (template_code, template_name, channel_type, content_template, status) VALUES
('WELCOME', '欢迎入住', 1, '尊敬的 {ownerName}，欢迎您入住 {communityName}！', 1),
('ALARM_REMIND', '告警提醒', 2, '【告警提醒】{alarmType}发生在{location}，请及时处理。', 1),
('WORKORDER_NOTIFY', '工单通知', 3, '您有新的工单：{workOrderTitle}，请查收。', 1);
```

### 5.4 实现清单

**后端（0.5 天）**
```java
@Controller
@RequestMapping("/api/v1/msg/template")
public class MsgTemplateController {
    
    @GetMapping("/list")
    public R<List<MsgTemplate>> list(MsgTemplateQuery q) { ... }
    
    @PostMapping
    public R<Void> save(@RequestBody MsgTemplate t) { ... }
    
    @PutMapping
    public R<Void> update(...) { ... }
    
    @DeleteMapping("/{ids}")
    public R<Void> remove(...) { ... }
    
    /**
     * 模板渲染
     */
    @PostMapping("/render")
    public R<String> render(@RequestBody RenderRequest req) {
        MsgTemplate tpl = getById(req.getTemplateId());
        String rendered = replaceVariables(tpl.getContentTemplate(), req.getParams());
        return R.ok(rendered);
    }
}

private String replaceVariables(String template, Map<String, Object> params) {
    // 简单替换：{key} -> params.get(key)
    return StrSubstitutor.createInterpolator(template, params, "{", "}");
}
```

**前端（0.5 天）**
- `views/msg/template/index.vue` — 通用 CRUD
- 模板编辑器：支持预览渲染效果

**权限串**
```sql
INSERT INTO sys_menu (menu_name, parent_id, perms, menu_type, component) VALUES
('消息模板', 25, 'msg:template:view', 'M', 'layout'),
('模板列表', 55, '/msg/template', 'msg:template:list', 'C', 'msg/template');
```

---

## 6. P2 — 黑名单管理

### 6.1 业务场景
- 禁止特定人员/车辆进入小区
- 门禁/道闸识别时自动拦截
- 拦截记录自动告警

### 6.2 表结构
```sql
CREATE TABLE `sec_blacklist` (
  `id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `blacklist_type` tinyint NOT NULL COMMENT '1 人员 2 车辆',
  `target_name` varchar(50) COMMENT '姓名/车牌',
  `target_id` varchar(50) COMMENT '身份证/车牌号',
  `face_img` varchar(255) COMMENT '人脸图片',
  `reason` varchar(500) COMMENT '拉黑原因',
  `effective_start` date,
  `effective_end` date,
  `status` tinyint DEFAULT '1'
);
```

### 6.3 现有代码
```java
// 已存在但未使用
@Entity
@Table("sec_blacklist")
public class SecBlacklist extends BaseEntity {
    private Long communityId;
    private Integer blacklistType;
    private String targetName;
    private String targetId;
    private String faceImg;
    private String reason;
    private Date effectiveStart;
    private Date effectiveEnd;
    private Integer status;
    
    @ManyToOne
    @JoinColumn(name = "community_id", insertable = false, updatable = false)
    private BaseCommunity community;
}

@Mapper
public interface SecBlacklistMapper extends BaseMapper<SecBlacklist> {}
```

### 6.4 实现清单

**后端（0.5 天）**
- Controller/Service 补齐
- 拦截规则校验接口（供门禁/道闸调用）

```java
@Service
public class BlacklistCheckService {
    
    /**
     * 检查是否在黑名单
     * @param type 1 人脸 2 车牌
     * @param target 人脸特征码/车牌号
     * @return true=黑名单，false=允许通行
     */
    public boolean isInBlacklist(Integer type, String target) {
        LambdaQueryWrapper<SecBlacklist> qw = new LambdaQueryWrapper<>();
        qw.eq(SecBlacklist::getBlacklistType, type == 1 ? 1 : 2);
        if (type == 1) {
            qw.eq(SecBlacklist::getFaceImg, target);
        } else {
            qw.eq(SecBlacklist::getTargetId, target);
        }
        qw.eq(SecBlacklist::getStatus, 1);
        qw.and(w -> w.between(SecBlacklist::getEffectiveStart, LocalDate.now())
                      .or()
                      .isNull(SecBlacklist::getEffectiveEnd));
        
        return count(qw) > 0;
    }
}
```

**前端（0.5 天）**
- `views/blacklist/index.vue` — 列表 + 新增（人脸上传图片）
- API 接入

**门禁联动伪代码**
```java
// 门禁远程开门前置检查
@PostMapping("/access/remote-open")
public R<Void> remoteOpen(@RequestBody OpenRequest req) {
    // 1. 检查是否在黑名单
    if (blacklistCheckService.isInBlacklist(1, req.getFaceCode())) {
        return R.fail("该人员在黑名单，禁止开门");
    }
    
    // 2. 正常流程...
    accessService.openDoor(req);
    return R.ok();
}
```

---

## 7. P2 — AI 场景配置

### 7.1 业务场景
- 配置 AI 算法（人脸比对/入侵检测/烟火识别）
- 绑定监控通道
- 置信度阈值设置
- 告警自动触发

### 7.2 表结构（2 张表）

**AI 场景配置 `ai_scene_config`**
```sql
CREATE TABLE `ai_scene_config` (
  `id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `scene_code` varchar(50) NOT NULL,
  `scene_name` varchar(100) NOT NULL,
  `channel_ids` varchar(500) COMMENT '关联通道 ID',
  `algorithm_type` varchar(50) COMMENT '算法类型',
  `threshold` decimal(5,2) DEFAULT '0.80' COMMENT '置信度',
  `alarm_level` tinyint DEFAULT '2' COMMENT '告警级别',
  `enable_time_start` varchar(10),
  `enable_time_end` varchar(10),
  `status` tinyint DEFAULT '1'
);
```

**AI 事件记录 `ai_event_record`**
```sql
CREATE TABLE `ai_event_record` (
  `id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `scene_id` bigint,
  `channel_id` bigint,
  `event_type` varchar(50) NOT NULL,
  `confidence` decimal(5,2),
  `snapshot_url` varchar(255),
  `clip_url` varchar(255),
  `bbox_data` text COMMENT '检测框 JSON',
  `event_time` datetime NOT NULL,
  `handle_status` tinyint DEFAULT '0' COMMENT '0 未处理 1 已处理 2 误报'
);
```

### 7.3 实现清单

**后端（1.5 天）**
```java
// 场景配置 CRUD
@RestController
@RequestMapping("/api/v1/ai/scene")
public class AiSceneController {
    
    @PostMapping("/event/callback")
    public R<Void> eventCallback(@RequestBody AiEventDTO dto) {
        // AI 引擎回调：接收事件并入库
        AiEventRecord event = new AiEventRecord();
        BeanUtils.copyProperties(dto, event);
        eventRecordService.save(event);
        
        // 置信度超过阈值则触发告警
        AiSceneConfig scene = sceneService.getById(dto.getSceneId());
        if (dto.getConfidence().compareTo(scene.getThreshold()) >= 0) {
            alarmService.createFromAiEvent(event, scene);
        }
        
        return R.ok();
    }
}
```

**依赖外部服务**：
- AI 算法引擎（第三方或自研）
- 视频流分析服务
- 人脸库比对服务

**前端（1.5 天）**
- `views/ai/scene/index.vue` — 场景配置（通道选择树 + 算法下拉）
- `views/ai/event/index.vue` — 事件记录 + 处置
- ECharts 展示事件趋势

**权限串**
```sql
INSERT INTO sys_menu (menu_name, parent_id, perms, menu_type, component) VALUES
('AI 分析', 4, 'ai:view', 'M', 'layout'),
('场景配置', 60, '/ai/scene', 'ai:scene:list', 'C', 'ai/scene'),
('事件记录', 60, '/ai/event', 'ai:event:list', 'C', 'ai/event');
```

---

## 8. P2 — 设备事件

### 8.1 业务场景
- 设备状态变更自动上报（离线/重启/故障）
- 事件记录与告警区分
- 支持事后追溯

### 8.2 表结构
```sql
CREATE TABLE `iot_device_event` (
  `id` bigint NOT NULL,
  `device_id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `event_code` varchar(50) NOT NULL,
  `event_name` varchar(100),
  `event_data` text COMMENT 'JSON 数据',
  `event_time` datetime NOT NULL,
  `handle_status` tinyint DEFAULT '0'
);
```

### 8.3 实现清单

**后端（0.5 天）**
```java
// 设备上报入口
@PostMapping("/iot/device/event/report")
public R<Void> reportEvent(@RequestBody DeviceEventDTO dto) {
    IotDeviceEvent event = new IotDeviceEvent();
    event.setDeviceId(dto.getDeviceId());
    event.setEventCode(dto.getCode());
    event.setEventName(dto.getName());
    event.setEventData(Json.toJson(dto.getData()));
    event.setEventTime(LocalDateTime.now());
    event.setHandleStatus(0);
    
    deviceEventService.save(event);
    
    // 重要事件自动转告警
    if ("OFFLINE".equals(dto.getCode()) || "FAULT".equals(dto.getCode())) {
        alarmService.createFromDeviceEvent(event);
    }
    
    return R.ok();
}
```

**前端（0.5 天）**
- `views/iot/event/index.vue` — 事件列表 + 筛选
- 对接 `iotApi.deviceEventList`

---

## 9. P2 — 统计日汇总

### 9.1 业务价值
- 每日凌晨自动生成前一日的统计数据
- 支持历史趋势分析
- 减轻实时统计压力

### 9.2 表结构
```sql
CREATE TABLE `stat_daily_summary` (
  `id` bigint NOT NULL,
  `community_id` bigint NOT NULL,
  `stat_date` date NOT NULL,
  `alarm_total` int DEFAULT '0',
  `alarm_handled` int DEFAULT '0',
  `access_total` int DEFAULT '0',
  `vehicle_in_total` int DEFAULT '0',
  `vehicle_out_total` int DEFAULT '0',
  `visitor_total` int DEFAULT '0',
  `work_order_total` int DEFAULT '0',
  `work_order_finished` int DEFAULT '0',
  `patrol_total` int DEFAULT '0', -- 待巡更模块完成后填充
  `patrol_missing` int DEFAULT '0', -- 待巡更模块完成后填充
  `device_online_rate` decimal(5,2),
  UNIQUE KEY `uk_community_date` (`community_id`,`stat_date`)
);
```

### 9.3 实现清单

**后端（0.5 天）**
```java
@Component
public class DailySummaryJob {
    
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨 2 点
    public void generateDailySummary() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDate statDate = yesterday.toLocalDate();
        
        // 获取所有小区
        List<BaseCommunity> communities = communityService.list();
        
        for (BaseCommunity comm : communities) {
            StatDailySummary summary = new StatDailySummary();
            summary.setCommunityId(comm.getId());
            summary.setStatDate(statDate);
            
            // 聚合查询各表
            summary.setAlarmTotal(alarmService.countByDate(comm.getId(), statDate));
            summary.setAlarmHandled(alarmService.countHandledByDate(comm.getId(), statDate));
            summary.setAccessTotal(accessService.countByDate(comm.getId(), statDate));
            // ... 其他指标
            
            // 计算设备在线率
            long deviceTotal = deviceService.countByCommunity(comm.getId());
            long deviceOnline = deviceService.countOnlineByCommunity(comm.getId());
            summary.setDeviceOnlineRate(
                deviceTotal > 0 ? new BigDecimal(deviceOnline * 100.0 / deviceTotal) : BigDecimal.ZERO
            );
            
            summaryService.saveOrUpdate(summary);
        }
    }
}
```

**前端（0.5 天）**
- `views/stat/daily/index.vue` — 日汇总列表 + 图表
- API: `/stat/daily/list`, `/stat/daily/{date}`

---

## 10. 实施建议

### 10.1 开发顺序推荐
```
Week 1: 设备类型 (P0) → 消息模板 (P1) → 报修投诉 (P1)
Week 2: 巡更管理 (P1) → 黑名单 (P2) → 设备事件 (P2)
Week 3: 统计日汇总 (P2) → AI 场景配置 (P2) → 联调测试
```

### 10.2 人力分配
| 模块 | 后端 | 前端 | 测试 | 合计 |
|------|------|------|------|------|
| 设备类型 | 0.5d | 0.5d | 0.5d | 1.5d |
| 巡更 | 1.5d | 1d | 1d | 3.5d |
| 报修 | 0.5d | 0.5d | 0.5d | 1.5d |
| 消息模板 | 0.5d | 0.5d | 0.5d | 1.5d |
| 黑名单 | 0.5d | 0.5d | 0.5d | 1.5d |
| AI 场景 | 1.5d | 1.5d | 1d | 4d |
| 设备事件 | 0.5d | 0.5d | 0.5d | 1.5d |
| 日汇总 | 0.5d | 0.5d | 0.5d | 1.5d |
| **总计** | **6d** | **6d** | **5d** | **17d** |

### 10.3 风险提示
| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| AI 算法引擎未就绪 | AI 模块无法联调 | 先用 Mock 数据打通流程 |
| 移动端打卡缺失 | 巡更记录无法录入 | 临时用 PC 端录入 + GPS 模拟 |
| 设备事件上报协议不统一 | 事件数据格式混乱 | 定义标准 DTO，设备侧适配 |
| 数据权限漏配 | 跨小区数据泄露 | 复制现有模块的 `DataScope` 实现 |

---

## 11. 验收标准

| 模块 | 验收要点 |
|------|----------|
| 设备类型 | 增删改查完整，类型编码唯一校验 |
| 巡更 | 路线可拖拽排序，漏巡自动告警 |
| 报修 | 单号自动生成，自动派单到工单 |
| 消息模板 | 模板渲染正确，变量替换无误 |
| 黑名单 | 门禁/车辆识别时自动拦截 |
| AI 场景 | 事件回调入库，超阈值触发告警 |
| 设备事件 | 设备上报接口可用，重要事件转告警 |
| 日汇总 | 定时任务每日执行，数据准确 |

---

*文档版本：v1.0*  
*最后更新：2026-10-20*
