-- ============================================================
-- 智慧安防小区运营平台 - 初始化数据
-- 默认账号: admin / admin123(超级管理员), wuye / admin123(物业), anbao / admin123(安保)
-- 说明: 密码为 BCrypt("admin123"); 业主手机号为 AES-ECB-PKCS5Padding 密文
--       (密钥 SrsPlatform@2026, 与 application.yml 中 app.crypto.aes-key 一致)
-- ============================================================

USE `srs_platform`;
SET NAMES utf8mb4;

-- ==================== 部门 ====================
DELETE FROM sys_dept;
INSERT INTO sys_dept(id, parent_id, dept_name, leader, phone, order_num, status) VALUES
(1, 0, '集团总部', '张伟', '020-88880001', 1, 1),
(2, 1, '智慧安防事业部', '李强', '020-88880002', 1, 1),
(3, 2, '锦绣家园物业服务中心', '王芳', '020-88880003', 1, 1),
(4, 2, '未来城物业服务中心', '赵敏', '020-88880004', 2, 1);

-- ==================== 角色 ====================
DELETE FROM sys_role;
INSERT INTO sys_role(id, role_name, role_key, role_sort, data_scope, status, remark) VALUES
(1, '超级管理员', 'admin', 1, 1, 1, '拥有系统全部权限, 数据范围: 全部'),
(2, '物业管理员', 'property', 2, 4, 1, '数据范围: 本小区'),
(3, '安保人员', 'security', 3, 4, 1, '数据范围: 本小区');

-- ==================== 用户 ====================
DELETE FROM sys_user;
INSERT INTO sys_user(id, dept_id, username, password, real_name, phone, email, user_type, status) VALUES
(1, 1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', '13800000001', 'admin@srs.com', 1, 1),
(2, 3, 'wuye',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王芳',       '13800000002', 'wuye@srs.com', 2, 1),
(3, 3, 'anbao',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '刘洋',       '13800000003', 'anbao@srs.com', 3, 1);

DELETE FROM sys_user_role;
INSERT INTO sys_user_role(id, user_id, role_id) VALUES (1, 1, 1), (2, 2, 2), (3, 3, 3);

-- ==================== 用户-小区数据权限 ====================
DELETE FROM sys_user_community;
INSERT INTO sys_user_community(id, user_id, community_id) VALUES
(1, 1, 1), (2, 1, 2),   -- 超级管理员: 全部小区(同时 data_scope=1)
(3, 2, 1), (4, 2, 2),   -- 物业管理员
(5, 3, 1);              -- 安保人员

-- ==================== 菜单 ====================
DELETE FROM sys_menu;
INSERT INTO sys_menu(id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, visible, status) VALUES
(1,   0, '工作台',   'C', '/dashboard',        'dashboard/index',      'dashboard:view',   'Odometer', 1, 1, 1),
(100, 0, '运营大屏', 'C', '/screen',           'screen/index',         'screen:view',      'DataLine',  2, 1, 1),

(10,  0, '系统管理', 'M', '/system',           NULL,                   NULL,               'Setting',   90, 1, 1),
(11,  10, '用户管理', 'C', '/system/user',      'system/user/index',    'sys:user:list',    'User',      1, 1, 1),
(12,  10, '角色管理', 'C', '/system/role',      'system/role/index',    'sys:role:list',    'UserFilled',2, 1, 1),
(13,  10, '菜单管理', 'C', '/system/menu',      'system/menu/index',    'sys:menu:list',    'Menu',      3, 1, 1),
(14,  10, '部门管理', 'C', '/system/dept',      'system/dept/index',    'sys:dept:list',    'OfficeBuilding', 4, 1, 1),
(15,  10, '字典管理', 'C', '/system/dict',      'system/dict/index',    'sys:dict:list',    'Collection',5, 1, 1),
(16,  10, '参数配置', 'C', '/system/config',    'system/config/index',  'sys:config:list',  'Tools',     6, 1, 1),
(17,  10, '操作日志', 'C', '/system/log/oper',  'system/log/oper',      'sys:log:list',     'Document',  7, 1, 1),
(18,  10, '登录日志', 'C', '/system/log/login', 'system/log/login',     'sys:log:list',     'Tickets',   8, 1, 1),
(101, 11, '用户新增', 'F', NULL, NULL, 'sys:user:add',    NULL, 1, 0, 1),
(102, 11, '用户修改', 'F', NULL, NULL, 'sys:user:edit',   NULL, 2, 0, 1),
(103, 11, '用户删除', 'F', NULL, NULL, 'sys:user:remove', NULL, 3, 0, 1),

(20,  0, '小区基础', 'M', '/base',             NULL,                   NULL,               'Location',  80, 1, 1),
(21,  20, '小区管理', 'C', '/base/community',  'base/community/index', 'base:community:list', 'Shop',    1, 1, 1),
(22,  20, '楼栋管理', 'C', '/base/building',   'base/building/index',  'base:building:list',  'Grid',    2, 1, 1),
(23,  20, '单元管理', 'C', '/base/unit',       'base/unit/index',      'base:unit:list',      'Grid',    3, 1, 1),
(24,  20, '房屋管理', 'C', '/base/room',       'base/room/index',      'base:room:list',      'House',   4, 1, 1),
(25,  20, '业主管理', 'C', '/base/owner',      'base/owner/index',     'base:owner:list',     'User',    5, 1, 1),
(26,  20, '车辆管理', 'C', '/base/vehicle',    'base/vehicle/index',   'base:vehicle:list',   'Van',     6, 1, 1),

(30,  0, '设备管理', 'M', '/iot',              NULL,                   NULL,               'Cpu',       70, 1, 1),
(31,  30, '设备台账', 'C', '/iot/device',      'iot/device/index',     'iot:device:list',     'Monitor', 1, 1, 1),
(32,  30, '设备类型', 'C', '/iot/device-type', 'iot/device-type/index','iot:deviceType:list', 'Files',   2, 1, 1),
(33,  30, '设备状态', 'C', '/iot/device/status','iot/device/status',   'iot:device:list',     'DataAnalysis', 3, 1, 1),

(40,  0, '视频监控', 'M', '/video',            NULL,                   NULL,               'VideoCamera',60, 1, 1),
(41,  40, '实时预览', 'C', '/video/live',      'video/live/index',     'video:live:view',     'VideoPlay',1, 1, 1),
(42,  40, '通道管理', 'C', '/video/channel',   'video/channel/index',  'video:channel:list',  'Film',    2, 1, 1),
(43,  40, '录像计划', 'C', '/video/record-plan','video/record-plan/index','video:record:list','Timer',  3, 1, 1),

(50,  0, '门禁管理', 'M', '/access',           NULL,                   NULL,               'Key',       50, 1, 1),
(51,  50, '门禁设备', 'C', '/access/device',   'access/device/index',  'access:device:list',  'Lock',    1, 1, 1),
(52,  50, '通行记录', 'C', '/access/record',   'access/record/index',  'access:record:list',  'Tickets', 2, 1, 1),
(53,  50, '权限管理', 'C', '/access/permission','access/permission/index','access:permission:list','Postcard',3,1,1),
(54,  50, '远程开门', 'C', '/access/remote',   'access/remote/index',  'access:remote:open',  'Unlock',  4, 1, 1),

(60,  0, '访客管理', 'M', '/visitor',          NULL,                   NULL,               'Avatar',    45, 1, 1),
(61,  60, '访客邀请', 'C', '/visitor/invite',  'visitor/invite/index', 'visitor:invite:list', 'EditPen', 1, 1, 1),
(62,  60, '访客记录', 'C', '/visitor/record',  'visitor/record/index', 'visitor:record:list', 'List',    2, 1, 1),

(65,  0, '车辆通行', 'M', '/vehicle',          NULL,                   NULL,               'Van',       44, 1, 1),
(66,  65, '通行记录', 'C', '/vehicle/pass',    'vehicle/pass/index',   'vehicle:pass:list',   'List',    1, 1, 1),

(70,  0, '告警中心', 'M', '/alarm',            NULL,                   NULL,               'Bell',      40, 1, 1),
(71,  70, '告警列表', 'C', '/alarm/list',      'alarm/list/index',     'alarm:event:list',    'WarnTriangleFilled', 1, 1, 1),
(72,  70, '告警规则', 'C', '/alarm/rule',      'alarm/rule/index',     'alarm:rule:add',      'SetUp',   2, 1, 1),
(73,  70, '告警统计', 'C', '/alarm/stat',      'alarm/stat/index',     'alarm:stat:view',     'TrendCharts', 3, 1, 1),

(80,  0, '工单中心', 'M', '/workorder',        NULL,                   NULL,               'Tickets',   35, 1, 1),
(81,  80, '工单列表', 'C', '/workorder/list',  'workorder/list/index', 'workorder:list',      'List',    1, 1, 1),
(82,  80, '工单统计', 'C', '/workorder/stat',  'workorder/stat/index', 'workorder:stat:view', 'PieChart',2, 1, 1),

(90,  0, '消息通知', 'M', '/msg',              NULL,                   NULL,               'Message',   30, 1, 1),
(91,  90, '公告管理', 'C', '/msg/notice',      'msg/notice/index',     'msg:notice:list',     'Document',1, 1, 1),
(92,  90, '发送记录', 'C', '/msg/log',         'msg/log/index',        'msg:notice:list',     'List',    2, 1, 1),

(95,  0, '统计分析', 'M', '/stat',             NULL,                   NULL,               'Histogram', 25, 1, 1),
(96,  95, '综合报表', 'C', '/stat/overview',   'stat/overview/index',  'stat:overview:view',  'DataBoard',1, 1, 1),
(97,  95, '告警分析', 'C', '/stat/alarm',      'stat/alarm/index',     'stat:alarm:view',     'Warning', 2, 1, 1),
(98,  95, '通行分析', 'C', '/stat/access',     'stat/access/index',    'stat:access:view',    'Connection',3,1,1),
(99,  95, '设备分析', 'C', '/stat/device',     'stat/device/index',    'stat:device:view',    'Cpu',     4, 1, 1);

-- 角色菜单: 超级管理员拥有全部菜单
DELETE FROM sys_role_menu;
SET @rn = 1000;
INSERT INTO sys_role_menu(id, role_id, menu_id, create_time)
SELECT (@rn := @rn + 1), 1, id, NOW() FROM sys_menu;

-- 物业管理员: 工作台/大屏/小区基础/设备/视频/门禁/访客/车辆/告警/工单/消息/统计(不含系统管理)
SET @rn = 5000;
INSERT INTO sys_role_menu(id, role_id, menu_id, create_time)
SELECT (@rn := @rn + 1), 2, id, NOW() FROM sys_menu
WHERE parent_id IN (0, 20, 30, 40, 50, 60, 65, 70, 80, 90, 95) OR id IN (1, 100);

-- 安保人员: 工作台/告警/工单/门禁/访客/视频
SET @rn = 9000;
INSERT INTO sys_role_menu(id, role_id, menu_id, create_time)
SELECT (@rn := @rn + 1), 3, id, NOW() FROM sys_menu
WHERE id IN (1, 50, 51, 52, 54, 60, 61, 62, 70, 71, 80, 81, 40, 41);

-- ==================== 字典 ====================
DELETE FROM sys_dict_type;
INSERT INTO sys_dict_type(id, dict_name, dict_type, status, remark) VALUES
(1, '告警类型', 'alarm_type', 1, '告警事件类型'),
(2, '告警级别', 'alarm_level', 1, '1紧急 2重要 3一般'),
(3, '设备类型', 'device_type', 1, '物联网设备类型'),
(4, '开门方式', 'open_method', 1, '门禁开门方式'),
(5, '房屋类型', 'room_type', 1, '房屋用途类型'),
(6, '车辆类型', 'vehicle_type', 1, '车辆属性'),
(7, '工单状态', 'order_status', 1, '工单流转状态'),
(8, '告警状态', 'alarm_status', 1, '告警处置状态'),
(9, '用户类型', 'user_type', 1, '平台用户类型');

DELETE FROM sys_dict_data;
INSERT INTO sys_dict_data(id, dict_type, dict_label, dict_value, dict_sort, css_class, status) VALUES
(1,'alarm_type','周界入侵','intrusion',1,'danger',1),
(2,'alarm_type','消防通道占用','occupy',2,'warning',1),
(3,'alarm_type','电动车入梯','ebike',3,'warning',1),
(4,'alarm_type','烟雾报警','smoke',4,'danger',1),
(5,'alarm_type','门禁强开','door_force',5,'danger',1),
(6,'alarm_type','人员聚集','gather',6,'info',1),
(7,'alarm_type','高空抛物','throw',7,'danger',1),
(8,'alarm_type','设备离线','offline',8,'info',1),
(11,'alarm_level','紧急','1',1,'danger',1),
(12,'alarm_level','重要','2',2,'warning',1),
(13,'alarm_level','一般','3',3,'info',1),
(21,'device_type','摄像头','camera',1,'',1),
(22,'device_type','门禁','guard',2,'',1),
(23,'device_type','车辆道闸','vehicle_gate',3,'',1),
(24,'device_type','烟感','smoke',4,'',1),
(25,'device_type','边缘网关','edge',5,'',1),
(26,'device_type','人脸一体机','face',6,'',1),
(31,'open_method','人脸','1',1,'',1),
(32,'open_method','二维码','2',2,'',1),
(33,'open_method','刷卡','3',3,'',1),
(34,'open_method','蓝牙','4',4,'',1),
(35,'open_method','远程','5',5,'',1),
(36,'open_method','密码','6',6,'',1),
(41,'room_type','住宅','1',1,'',1),
(42,'room_type','商铺','2',2,'',1),
(43,'room_type','车位','3',3,'',1),
(44,'room_type','储藏室','4',4,'',1),
(45,'room_type','其他','5',5,'',1),
(51,'vehicle_type','固定车','1',1,'',1),
(52,'vehicle_type','月租车','2',2,'',1),
(53,'vehicle_type','临时车','3',3,'',1),
(54,'vehicle_type','访客车','4',4,'',1),
(61,'order_status','待派发','0',1,'info',1),
(62,'order_status','待接单','1',2,'warning',1),
(63,'order_status','处理中','2',3,'warning',1),
(64,'order_status','待验收','3',4,'info',1),
(65,'order_status','已完成','4',5,'success',1),
(66,'order_status','已关闭','5',6,'info',1),
(71,'alarm_status','未处理','0',1,'danger',1),
(72,'alarm_status','处理中','1',2,'warning',1),
(73,'alarm_status','已处理','2',3,'success',1),
(74,'alarm_status','误报','3',4,'info',1),
(81,'user_type','平台用户','1',1,'',1),
(82,'user_type','物业','2',2,'',1),
(83,'user_type','安保','3',3,'',1),
(84,'user_type','业主','4',4,'',1);

-- ==================== 参数配置 ====================
DELETE FROM sys_config;
INSERT INTO sys_config(id, config_name, config_key, config_value, config_type, status, remark) VALUES
(1, '用户初始密码', 'sys.user.initPassword', 'admin123', 1, 1, '重置密码时使用'),
(2, '是否开启验证码', 'sys.account.captchaEnabled', 'false', 1, 1, 'true/false'),
(3, '流媒体服务地址', 'srs.media.baseUrl', 'http://127.0.0.1:8080', 1, 1, 'ZLMediaKit HTTP 地址'),
(4, '是否启用流媒体', 'srs.media.enabled', 'false', 1, 1, '未接入流媒体时返回演示流地址'),
(5, '告警自动派单', 'srs.alarm.autoDispatch', 'false', 1, 1, 'true 时按告警规则自动派单'),
(6, '录像默认保存天数', 'srs.video.storageDays', '30', 1, 1, '单位: 天');

-- ==================== 小区基础 ====================
DELETE FROM base_community;
INSERT INTO base_community(id, community_code, community_name, address, longitude, latitude, contact_person, contact_phone, cover_area, building_count, room_count, status) VALUES
(1, 'C001', '锦绣家园', '广州市天河区天河北路100号', 113.324, 23.135, '王芳', '020-88880003', 86000.00, 2, 60, 1),
(2, 'C002', '未来城',   '广州市番禺区大学城外环西路66号', 113.392, 23.041, '赵敏', '020-88880004', 120000.00, 1, 40, 1);

DELETE FROM base_building;
INSERT INTO base_building(id, community_id, building_name, building_no, floor_count, unit_count, room_count, status) VALUES
(1, 1, '1栋', 'B1', 18, 2, 36, 1),
(2, 1, '2栋', 'B2', 18, 2, 36, 1),
(3, 2, 'A座', 'A1', 26, 2, 52, 1);

DELETE FROM base_unit;
INSERT INTO base_unit(id, community_id, building_id, unit_name, floor_count, room_count, status) VALUES
(1, 1, 1, '1单元', 18, 18, 1),
(2, 1, 1, '2单元', 18, 18, 1),
(3, 1, 2, '1单元', 18, 18, 1),
(4, 2, 3, '1单元', 26, 26, 1);

DELETE FROM base_room;
INSERT INTO base_room(id, community_id, building_id, unit_id, room_no, floor, area, room_type, owner_status, status) VALUES
(1, 1, 1, 1, '101', 1, 89.50, 1, 1, 1),
(2, 1, 1, 1, '102', 1, 89.50, 1, 1, 1),
(3, 1, 1, 1, '201', 2, 89.50, 1, 1, 1),
(4, 1, 1, 2, '301', 3, 110.00, 1, 1, 1),
(5, 1, 2, 3, '502', 5, 76.00, 1, 0, 1),
(6, 2, 3, 4, '1801', 18, 120.00, 1, 1, 1),
(7, 1, 1, 1, '商铺01', 1, 45.00, 2, 1, 1);

DELETE FROM base_owner;
INSERT INTO base_owner(id, owner_name, phone, phone_mask, id_card, id_card_mask, gender, user_type, auth_status, status) VALUES
(1, '陈建国', '0140f7711d7f4e7c40621677b7094e99', '138****8001', NULL, NULL, 1, 1, 1, 1),
(2, '林晓梅', '063bbbbf9a5dbf7695f7a7f2d5f987d2', '138****8002', NULL, NULL, 2, 1, 1, 1),
(3, '周文斌', 'b449f4ae677515909ffa804728b850dd', '138****8003', NULL, NULL, 1, 2, 1, 1),
(4, '吴桂芳', '516089706f4a1622d9f337ac05e44f75', '138****8004', NULL, NULL, 2, 3, 0, 1),
(5, '郭志强', '2d49bc66c17a77227b9b30fda41891d4', '138****8005', NULL, NULL, 1, 1, 1, 1),
(6, '何丽娟', 'eda9601e170dd0257978d5d02cbe8bb3', '138****8006', NULL, NULL, 2, 1, 0, 1);

DELETE FROM base_room_resident;
INSERT INTO base_room_resident(id, community_id, room_id, owner_id, relation, move_in_date, status) VALUES
(1, 1, 1, 1, 1, '2021-06-01', 1),
(2, 1, 2, 2, 1, '2021-08-15', 1),
(3, 1, 3, 3, 2, '2022-03-01', 1),
(4, 1, 4, 4, 3, '2023-01-10', 1),
(5, 2, 6, 5, 1, '2020-11-20', 1),
(6, 1, 1, 6, 2, '2021-06-01', 1);

DELETE FROM base_vehicle;
INSERT INTO base_vehicle(id, community_id, owner_id, plate_no, plate_color, vehicle_type, vehicle_brand, vehicle_color, start_date, end_date, status) VALUES
(1, 1, 1, '粤A12345', '蓝', 1, '丰田凯美瑞', '白', '2025-01-01', '2026-12-31', 1),
(2, 1, 2, '粤A67890', '蓝', 2, '本田雅阁',   '黑', '2025-06-01', '2026-05-31', 1),
(3, 1, 3, '粤B0H321', '绿', 1, '比亚迪汉',   '灰', '2025-03-01', '2027-02-28', 1),
(4, 2, 5, '粤A88888', '蓝', 1, '宝马5系',    '蓝', '2024-01-01', '2026-12-31', 1);

-- ==================== 设备 ====================
DELETE FROM iot_device_type;
INSERT INTO iot_device_type(id, type_code, type_name, parent_id, order_num, status) VALUES
(1, 'camera', '摄像头', 0, 1, 1),
(2, 'guard', '门禁设备', 0, 2, 1),
(3, 'vehicle_gate', '车辆道闸', 0, 3, 1),
(4, 'smoke', '烟感探测器', 0, 4, 1),
(5, 'edge', '边缘网关', 0, 5, 1),
(6, 'face', '人脸一体机', 0, 6, 1);

DELETE FROM iot_device;
INSERT INTO iot_device(id, community_id, device_code, device_name, device_type, type_id, vendor, model, install_location, longitude, latitude, access_protocol, online_status, run_status, last_online_time, install_date, status) VALUES
(1, 1, 'CAM-001', '1栋大堂摄像头', 'camera', 1, '海康威视', 'DS-2CD3T46', '锦绣家园1栋1单元大堂', 113.3241, 23.1351, 'gb28181', 1, 1, NOW(), '2023-05-10', 1),
(2, 1, 'CAM-002', '小区西门摄像头', 'camera', 1, '海康威视', 'DS-2CD3T46', '锦绣家园西门', 113.3235, 23.1348, 'gb28181', 1, 1, NOW(), '2023-05-10', 1),
(3, 1, 'CAM-003', '地下车库摄像头', 'camera', 1, '大华股份', 'DH-IPC-HFW', '锦绣家园负一层车库', 113.3245, 23.1355, 'onvif', 0, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR), '2023-05-10', 1),
(4, 1, 'GRD-001', '1栋单元门禁', 'guard', 2, '海康威视', 'DS-K1T341', '锦绣家园1栋1单元入口', 113.3241, 23.1351, 'mqtt', 1, 1, NOW(), '2023-06-01', 1),
(5, 1, 'GRD-002', '2栋单元门禁', 'guard', 2, '海康威视', 'DS-K1T341', '锦绣家园2栋1单元入口', 113.3243, 23.1353, 'mqtt', 1, 1, NOW(), '2023-06-01', 1),
(6, 1, 'GATE-001', '西门车辆道闸', 'vehicle_gate', 3, '科拓', 'KT-G6', '锦绣家园西门出入口', 113.3235, 23.1348, 'sdk', 1, 1, NOW(), '2023-06-01', 1),
(7, 1, 'SMK-001', '1栋3层烟感', 'smoke', 4, '海曼', 'HM-633PH', '锦绣家园1栋3层走廊', 113.3241, 23.1351, 'mqtt', 1, 1, NOW(), '2023-07-15', 1),
(8, 1, 'EDGE-001', '锦绣家园边缘网关', 'edge', 5, '自研', 'EG-2000', '锦绣家园中心机房', 113.3240, 23.1350, 'mqtt', 1, 1, NOW(), '2023-04-20', 1),
(9, 2, 'CAM-101', '未来城A座大堂', 'camera', 1, '大华股份', 'DH-IPC-HFW', '未来城A座大堂', 113.3921, 23.0412, 'gb28181', 1, 1, NOW(), '2024-01-10', 1),
(10, 2, 'GATE-101', '未来城地库道闸', 'vehicle_gate', 3, '科拓', 'KT-G6', '未来城地库入口', 113.3923, 23.0415, 'sdk', 1, 1, NOW(), '2024-01-10', 1);

DELETE FROM iot_device_status_log;
INSERT INTO iot_device_status_log(id, device_id, community_id, event_type, event_time, extra) VALUES
(1, 3, 1, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR), '心跳超时'),
(2, 8, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), '启动'),
(3, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR), '启动');

-- ==================== 视频 ====================
DELETE FROM vs_record_plan;
INSERT INTO vs_record_plan(id, plan_name, record_type, start_time, end_time, storage_days, status) VALUES
(1, '全天录像', 1, '00:00', '23:59', 30, 1),
(2, '夜间录像', 2, '22:00', '06:00', 30, 1),
(3, '移动侦测录像', 3, '00:00', '23:59', 15, 1);

DELETE FROM vs_channel;
INSERT INTO vs_channel(id, community_id, device_id, channel_no, channel_name, gb_code, stream_url, has_ptz, has_audio, record_plan_id, status) VALUES
(1, 1, 1, 1, '1栋大堂', '34020000001320000001', NULL, 1, 1, 1, 1),
(2, 1, 2, 1, '小区西门', '34020000001320000002', NULL, 1, 0, 1, 1),
(3, 1, 3, 1, '地下车库', '34020000001320000003', NULL, 0, 0, 3, 1),
(4, 1, 9, 1, '2栋电梯厅', '34020000001320000004', NULL, 0, 1, 1, 1),
(5, 2, 9, 1, '未来城A座大堂', '34020000001320000101', NULL, 1, 1, 1, 1);

-- ==================== 安防: 门禁授权/通行记录 ====================
DELETE FROM sec_access_permission;
INSERT INTO sec_access_permission(id, community_id, device_id, owner_id, owner_name, time_start, time_end, status) VALUES
(1, 1, 4, 1, '陈建国', '00:00', '23:59', 1),
(2, 1, 4, 6, '何丽娟', '00:00', '23:59', 1),
(3, 1, 5, 2, '林晓梅', '00:00', '23:59', 1),
(4, 1, 5, 3, '周文斌', '06:00', '23:00', 1);

DELETE FROM sec_access_record;
INSERT INTO sec_access_record(id, community_id, device_id, owner_id, person_name, open_method, pass_result, pass_time) VALUES
(1, 1, 4, 1, '陈建国', 1, 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(2, 1, 4, 6, '何丽娟', 1, 1, DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(3, 1, 5, 2, '林晓梅', 3, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(4, 1, 4, NULL, '陌生人', 1, 0, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(5, 1, 5, 3, '周文斌', 2, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(6, 1, 4, 1, '陈建国', 5, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 1, 4, 2, '林晓梅', 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

UPDATE sec_access_record SET deny_reason = '非本单元住户' WHERE pass_result = 0;

-- ==================== 安防: 访客 ====================
DELETE FROM sec_visitor_invite;
INSERT INTO sec_visitor_invite(id, community_id, invite_no, inviter_id, inviter_name, visitor_name, visitor_phone, qr_code, visit_start, visit_end, pass_scope, visit_purpose, status, arrive_time) VALUES
(1, 1, 'V20260910000001', 1, '陈建国', '孙明', '13900001111', 'VIS-V20260910000001', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), '1栋单元门禁', '亲友拜访', 1, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
(2, 1, 'V20260910000002', 2, '林晓梅', '快递员小李', '13900002222', 'VIS-V20260910000002', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 6 HOUR), '2栋单元门禁', '快递配送', 0, NULL),
(3, 1, 'V20260908000003', 1, '陈建国', '保洁王姐', '13900003333', 'VIS-V20260908000003', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), '1栋单元门禁', '家政服务', 2, NULL);

-- ==================== 安防: 车辆通行 ====================
DELETE FROM sec_vehicle_pass;
INSERT INTO sec_vehicle_pass(id, community_id, gate_device_id, plate_no, direction, vehicle_type, pass_time) VALUES
(1, 1, 6, '粤A12345', 1, 1, DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(2, 1, 6, '粤A67890', 2, 2, DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(3, 1, 6, '粤B0H321', 1, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(4, 1, 6, '粤C99999', 1, 3, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(5, 1, 6, '粤A12345', 2, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 2, 10, '粤A88888', 1, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- ==================== 告警 ====================
DELETE FROM sec_alarm_rule;
INSERT INTO sec_alarm_rule(id, community_id, rule_name, alarm_type, alarm_level, auto_dispatch, auto_dispatch_role, notify_channels, sla_minutes, status, remark) VALUES
(1, NULL, '周界入侵-紧急处置', 'intrusion', 1, 1, 'security', '1,3', 15, 1, '周界入侵自动派单给安保'),
(2, NULL, '烟雾报警-紧急处置', 'smoke', 1, 1, 'security', '1,2,3', 10, 1, '烟感告警短信+推送'),
(3, NULL, '通道占用-一般处置', 'occupy', 3, 0, NULL, '1', 120, 1, '消防通道占用仅站内信');

DELETE FROM sec_alarm_event;
INSERT INTO sec_alarm_event(id, community_id, alarm_no, alarm_type, alarm_level, source_type, device_id, channel_id, title, content, location, status, occur_time) VALUES
(1, 1, 'AL20260910000001', 'intrusion', 1, 2, 2, 2, '西门周界检测到人员翻越', 'AI识别置信度0.93, 检测到人员翻越围墙', '锦绣家园西门围墙', 0, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(2, 1, 'AL20260910000002', 'occupy', 3, 2, 1, 1, '消防通道被占用', '1栋大堂消防通道堆放杂物', '锦绣家园1栋大堂', 0, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(3, 1, 'AL20260910000003', 'smoke', 1, 1, 7, NULL, '烟感探测器报警', '1栋3层走廊烟感触发', '锦绣家园1栋3层', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(4, 1, 'AL20260910000004', 'door_force', 2, 1, 5, NULL, '门禁被强行开启', '2栋单元门禁非法开启', '锦绣家园2栋1单元', 2, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(5, 1, 'AL20260909000005', 'ebike', 2, 2, 1, 4, '电动车进入电梯', 'AI识别电动车进入2栋电梯', '锦绣家园2栋电梯厅', 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 1, 'AL20260908000006', 'gather', 3, 2, 1, 1, '人员聚集', '1栋大堂人员聚集超过10人', '锦绣家园1栋大堂', 3, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 2, 'AL20260910000007', 'offline', 3, 1, 3, 3, '设备离线告警', '地下车库摄像头心跳超时', '锦绣家园负一层车库', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- ==================== 工单 ====================
DELETE FROM biz_work_order;
INSERT INTO biz_work_order(id, community_id, order_no, order_type, title, content, source_id, source_type, priority, status, assignee_id, assignee_name, sla_deadline, create_time) VALUES
(1, 1, 'WO20260910000001', 'alarm', '【周界入侵】西门围墙翻越核查', '前往西门核查周界入侵告警', 1, 'alarm', 1, 1, 3, '刘洋', DATE_ADD(NOW(), INTERVAL 15 MINUTE), DATE_SUB(NOW(), INTERVAL 18 MINUTE)),
(2, 1, 'WO20260910000002', 'repair', '1栋3层走廊照明故障', '业主报修走廊灯不亮', NULL, NULL, 3, 2, 3, '刘洋', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 1, 'WO20260909000003', 'alarm', '【消防通道占用】杂物清理', '清理1栋大堂消防通道杂物', 2, 'alarm', 3, 4, 3, '刘洋', DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY));

UPDATE biz_work_order SET finish_time = DATE_SUB(NOW(), INTERVAL 20 HOUR), evaluate_score = 5, evaluate_content = '处理及时' WHERE id = 3;

DELETE FROM biz_work_order_flow;
INSERT INTO biz_work_order_flow(id, order_id, action, operator_id, operator_name, action_detail, create_time) VALUES
(1, 1, 'create', 1, '超级管理员', '由告警 AL20260910000001 自动生成', DATE_SUB(NOW(), INTERVAL 18 MINUTE)),
(2, 1, 'assign', 1, '超级管理员', '派单给 刘洋', DATE_SUB(NOW(), INTERVAL 17 MINUTE)),
(3, 2, 'create', 1, '超级管理员', '人工创建报修工单', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(4, 2, 'assign', 1, '超级管理员', '派单给 刘洋', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(5, 2, 'accept', 3, '刘洋', '已接单', DATE_SUB(NOW(), INTERVAL 150 MINUTE)),
(6, 3, 'create', 1, '超级管理员', '由告警 AL20260910000002 自动生成', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 3, 'verify', 1, '超级管理员', '验收通过', DATE_SUB(NOW(), INTERVAL 20 HOUR));

-- ==================== 消息 ====================
DELETE FROM biz_notice;
INSERT INTO biz_notice(id, community_id, title, content, notice_type, publish_status, publish_time, top_flag, view_count, create_by) VALUES
(1, 1, '关于小区门禁系统升级的通知', '为提升小区安全管理水平, 物业服务中心将于本周六凌晨 0:00-4:00 对门禁系统进行升级维护, 期间人脸与刷卡开门可能短暂受影响, 请提前做好准备。', 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 1, 128, 'admin'),
(2, 1, '秋季消防安全演练公告', '定于下周三上午 9:30 在小区中心广场举行消防安全演练, 欢迎各位业主积极参与。', 3, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 0, 56, 'wuye'),
(3, 1, '停车月卡续费提醒', '请月租车主于本月 25 日前完成月卡续费, 逾期将影响自动抬杆。', 2, 0, NULL, 0, 0, 'wuye');

DELETE FROM msg_template;
INSERT INTO msg_template(id, template_code, template_name, channel_type, content_template, status) VALUES
(1, 'ALARM_NOTIFY', '告警通知', 1, '【告警】{location} 发生 {alarmType}, 级别: {level}, 请及时处理。', 1),
(2, 'VISITOR_ARRIVE', '访客到访', 1, '您的访客 {visitorName} 已到达 {location}。', 1),
(3, 'WORKORDER_ASSIGN', '工单派发', 1, '您有新的工单: {title}, 请于 {deadline} 前处理。', 1);

DELETE FROM msg_send_log;
INSERT INTO msg_send_log(id, template_id, channel_type, receiver_id, receiver_name, receiver_contact, title, content, send_status, read_flag, send_time) VALUES
(1, 1, 1, 3, '刘洋', 'anbao', '告警通知', '【告警】锦绣家园西门围墙 发生 周界入侵, 级别: 紧急, 请及时处理。', 1, 0, DATE_SUB(NOW(), INTERVAL 19 MINUTE)),
(2, 3, 1, 3, '刘洋', 'anbao', '工单派发', '您有新的工单: 【周界入侵】西门围墙翻越核查, 请尽快处理。', 1, 1, DATE_SUB(NOW(), INTERVAL 17 MINUTE));

-- ==================== 日统计(近 7 天) ====================
DELETE FROM stat_daily_summary;
INSERT INTO stat_daily_summary(id, community_id, stat_date, alarm_total, alarm_handled, access_total, vehicle_in_total, vehicle_out_total, visitor_total, work_order_total, work_order_finished, device_online_rate)
SELECT
  (@i := @i + 1) AS id,
  1 AS community_id,
  d.stat_date,
  FLOOR(3 + RAND() * 8)  AS alarm_total,
  FLOOR(2 + RAND() * 6)  AS alarm_handled,
  FLOOR(200 + RAND() * 300) AS access_total,
  FLOOR(80 + RAND() * 120) AS vehicle_in_total,
  FLOOR(80 + RAND() * 120) AS vehicle_out_total,
  FLOOR(5 + RAND() * 20) AS visitor_total,
  FLOOR(2 + RAND() * 8)  AS work_order_total,
  FLOOR(1 + RAND() * 6)  AS work_order_finished,
  ROUND(85 + RAND() * 12, 2) AS device_online_rate
FROM (
  SELECT DATE_SUB(CURDATE(), INTERVAL seq DAY) AS stat_date
  FROM (SELECT 0 AS seq UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
        UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) s
) d, (SELECT @i := 0) t;

-- ==================== 巡更(P1 预留) ====================
DELETE FROM pat_point;
INSERT INTO pat_point(id, community_id, point_name, point_code, location, status) VALUES
(1, 1, '西门岗亭', 'P001', '锦绣家园西门', 1),
(2, 1, '1栋大堂', 'P002', '锦绣家园1栋大堂', 1),
(3, 1, '地下车库入口', 'P003', '锦绣家园负一层', 1);

DELETE FROM pat_route;
INSERT INTO pat_route(id, community_id, route_name, route_code, description, point_count, status) VALUES
(1, 1, '夜间常规路线', 'R001', '西门-1栋-车库', 3, 1);

DELETE FROM pat_route_point;
INSERT INTO pat_route_point(id, route_id, point_id, order_num) VALUES (1, 1, 1, 1), (2, 1, 2, 2), (3, 1, 3, 3);

DELETE FROM pat_plan;
INSERT INTO pat_plan(id, community_id, plan_name, route_id, patrol_type, start_time, end_time, interval_minutes, assign_user_ids, status) VALUES
(1, 1, '夜间巡更计划', 1, 2, '20:00', '06:00', 60, '3', 1);

-- ============================================================
-- 初始化完成
-- ============================================================
