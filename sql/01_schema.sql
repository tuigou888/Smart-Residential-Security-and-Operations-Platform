-- ============================================================
-- 智慧安防小区运营平台 - 数据库DDL脚本
-- 数据库: MySQL 8.0+   字符集: utf8mb4 / utf8mb4_general_ci
-- 说明: 覆盖系统/基础/设备/视频/安防/业务/巡更/消息/AI/统计/文件 11 个域
--       在需求《架构.md》45 张表基础上补充 2 张表:
--         sec_alarm_rule      告警规则表(接口清单 alarm:rule 有接口无表)
--         sec_access_permission 门禁授权表(接口清单 access:permission 有接口无表)
--       并统一修正原文中的字段歧义(见文件末尾"歧义修正说明")
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 系统管理域 ====================

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL COMMENT '主键ID',
  dept_id BIGINT DEFAULT NULL COMMENT '部门ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  user_type TINYINT DEFAULT 1 COMMENT '1平台用户 2物业 3安保 4业主',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志 0正常 1删除',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_dept_id (dept_id),
  KEY idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL COMMENT '主键ID',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  role_key VARCHAR(50) NOT NULL COMMENT '角色标识',
  role_sort INT DEFAULT 0 COMMENT '排序',
  data_scope TINYINT DEFAULT 1 COMMENT '数据范围 1全部 2自定义 3本部门 4本小区',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT NOT NULL COMMENT '主键ID',
  parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
  menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
  menu_type CHAR(1) NOT NULL COMMENT 'M目录 C菜单 F按钮',
  path VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
  component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
  perms VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
  order_num INT DEFAULT 0 COMMENT '排序',
  visible TINYINT DEFAULT 1 COMMENT '1显示 0隐藏',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
  id BIGINT NOT NULL COMMENT '主键ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_menu (role_id, menu_id),
  KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS sys_dept (
  id BIGINT NOT NULL COMMENT '主键ID',
  parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
  dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
  leader VARCHAR(50) DEFAULT NULL COMMENT '负责人',
  phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  order_num INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门组织表';

CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT NOT NULL COMMENT '主键ID',
  dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
  dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_data (
  id BIGINT NOT NULL COMMENT '主键ID',
  dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
  dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
  dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
  dict_sort INT DEFAULT 0 COMMENT '排序',
  css_class VARCHAR(100) DEFAULT NULL COMMENT '样式',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT NOT NULL COMMENT '主键ID',
  config_name VARCHAR(100) NOT NULL COMMENT '参数名称',
  config_key VARCHAR(100) NOT NULL COMMENT '参数键名',
  config_value VARCHAR(500) DEFAULT NULL COMMENT '参数值',
  config_type TINYINT DEFAULT 1 COMMENT '1系统内置 2用户自定义',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

CREATE TABLE IF NOT EXISTS sys_oper_log (
  id BIGINT NOT NULL COMMENT '主键ID',
  module VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
  business_type TINYINT DEFAULT 0 COMMENT '0其他 1新增 2修改 3删除 4导出 5登录',
  method VARCHAR(200) DEFAULT NULL COMMENT '方法名',
  request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
  oper_name VARCHAR(50) DEFAULT NULL COMMENT '操作人',
  oper_url VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  oper_ip VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
  oper_param TEXT COMMENT '请求参数',
  json_result TEXT COMMENT '返回结果',
  status TINYINT DEFAULT 1 COMMENT '1成功 0失败',
  error_msg TEXT COMMENT '错误信息',
  oper_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (id),
  KEY idx_oper_time (oper_time),
  KEY idx_oper_name (oper_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS sys_login_log (
  id BIGINT NOT NULL COMMENT '主键ID',
  username VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  login_ip VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
  login_location VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
  browser VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
  os VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
  status TINYINT DEFAULT 1 COMMENT '1成功 0失败',
  msg VARCHAR(255) DEFAULT NULL COMMENT '提示信息',
  login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (id),
  KEY idx_login_time (login_time),
  KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ==================== 基础数据域 ====================

CREATE TABLE IF NOT EXISTS base_community (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_code VARCHAR(50) NOT NULL COMMENT '小区编码',
  community_name VARCHAR(100) NOT NULL COMMENT '小区名称',
  address VARCHAR(255) DEFAULT NULL COMMENT '地址',
  longitude DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  latitude DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  contact_person VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  cover_area DECIMAL(12,2) DEFAULT NULL COMMENT '占地面积(㎡)',
  building_count INT DEFAULT 0 COMMENT '楼栋数',
  room_count INT DEFAULT 0 COMMENT '房屋数',
  cover_img VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_community_code (community_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小区表';

CREATE TABLE IF NOT EXISTS base_building (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  building_name VARCHAR(50) NOT NULL COMMENT '楼栋名称',
  building_no VARCHAR(20) DEFAULT NULL COMMENT '楼栋号',
  floor_count INT DEFAULT 0 COMMENT '楼层数',
  unit_count INT DEFAULT 0 COMMENT '单元数',
  room_count INT DEFAULT 0 COMMENT '房屋数',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_community_id (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼栋表';

CREATE TABLE IF NOT EXISTS base_unit (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  building_id BIGINT NOT NULL COMMENT '楼栋ID',
  unit_name VARCHAR(50) NOT NULL COMMENT '单元名称',
  floor_count INT DEFAULT 0 COMMENT '楼层数',
  room_count INT DEFAULT 0 COMMENT '房屋数',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_building_id (building_id),
  KEY idx_community_id (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单元表';

CREATE TABLE IF NOT EXISTS base_room (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  building_id BIGINT NOT NULL COMMENT '楼栋ID',
  unit_id BIGINT NOT NULL COMMENT '单元ID',
  room_no VARCHAR(20) NOT NULL COMMENT '房号',
  floor INT DEFAULT NULL COMMENT '楼层',
  area DECIMAL(8,2) DEFAULT NULL COMMENT '建筑面积(㎡)',
  room_type TINYINT DEFAULT 1 COMMENT '1住宅 2商铺 3车位 4储藏室 5其他',
  owner_status TINYINT DEFAULT 0 COMMENT '0未入住 1已入住',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_unit_id (unit_id),
  KEY idx_community_id (community_id),
  KEY idx_building_id (building_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房屋表';

CREATE TABLE IF NOT EXISTS base_owner (
  id BIGINT NOT NULL COMMENT '主键ID',
  owner_name VARCHAR(50) NOT NULL COMMENT '姓名',
  phone VARCHAR(200) NOT NULL COMMENT '手机号(AES加密存储)',
  phone_mask VARCHAR(20) DEFAULT NULL COMMENT '手机号脱敏展示',
  id_card VARCHAR(200) DEFAULT NULL COMMENT '身份证(AES加密存储)',
  id_card_mask VARCHAR(30) DEFAULT NULL COMMENT '身份证脱敏展示',
  gender TINYINT DEFAULT 0 COMMENT '0未知 1男 2女',
  face_token VARCHAR(64) DEFAULT NULL COMMENT '人脸库标识',
  face_img VARCHAR(255) DEFAULT NULL COMMENT '人脸照片URL',
  user_type TINYINT DEFAULT 1 COMMENT '1业主 2家属 3租客',
  auth_status TINYINT DEFAULT 0 COMMENT '实名认证 0未认证 1已认证',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_phone (phone),
  KEY idx_owner_name (owner_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业主住户表';

CREATE TABLE IF NOT EXISTS base_room_resident (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  room_id BIGINT NOT NULL COMMENT '房屋ID',
  owner_id BIGINT NOT NULL COMMENT '业主ID',
  relation TINYINT DEFAULT 1 COMMENT '1户主 2家属 3租客',
  move_in_date DATE DEFAULT NULL COMMENT '入住日期',
  move_out_date DATE DEFAULT NULL COMMENT '迁出日期',
  status TINYINT DEFAULT 1 COMMENT '1在住 0迁出',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_room_id (room_id),
  KEY idx_owner_id (owner_id),
  KEY idx_community_id (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人房关系表';

CREATE TABLE IF NOT EXISTS base_vehicle (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  owner_id BIGINT DEFAULT NULL COMMENT '车主ID',
  plate_no VARCHAR(20) NOT NULL COMMENT '车牌号',
  plate_color VARCHAR(10) DEFAULT '蓝' COMMENT '车牌颜色',
  vehicle_type TINYINT DEFAULT 1 COMMENT '1固定车 2月租车 3临时车',
  vehicle_brand VARCHAR(50) DEFAULT NULL COMMENT '车辆品牌',
  vehicle_color VARCHAR(20) DEFAULT NULL COMMENT '车辆颜色',
  start_date DATE DEFAULT NULL COMMENT '有效期开始',
  end_date DATE DEFAULT NULL COMMENT '有效期结束',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_plate_no (plate_no),
  KEY idx_community_id (community_id),
  KEY idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- ==================== 设备管理域 ====================

CREATE TABLE IF NOT EXISTS iot_device_type (
  id BIGINT NOT NULL COMMENT '主键ID',
  type_code VARCHAR(32) NOT NULL COMMENT '类型编码',
  type_name VARCHAR(50) NOT NULL COMMENT '类型名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父类型ID',
  icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
  order_num INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_type_code (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备类型表';

CREATE TABLE IF NOT EXISTS iot_device (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  device_code VARCHAR(64) NOT NULL COMMENT '设备编码',
  device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
  device_type VARCHAR(32) NOT NULL COMMENT '设备类型 camera/guard/vehicle_gate/smoke/edge等',
  type_id BIGINT DEFAULT NULL COMMENT '类型ID',
  vendor VARCHAR(50) DEFAULT NULL COMMENT '厂商',
  model VARCHAR(50) DEFAULT NULL COMMENT '型号',
  firmware_version VARCHAR(50) DEFAULT NULL COMMENT '固件版本',
  install_location VARCHAR(255) DEFAULT NULL COMMENT '安装位置',
  longitude DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  latitude DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  access_protocol VARCHAR(32) DEFAULT NULL COMMENT '接入协议 gb28181/onvif/sdk/mqtt/modbus',
  access_config TEXT COMMENT '接入配置(JSON)',
  online_status TINYINT DEFAULT 0 COMMENT '0离线 1在线',
  run_status TINYINT DEFAULT 1 COMMENT '1正常 2故障 3维保中',
  last_online_time DATETIME DEFAULT NULL COMMENT '最后在线时间',
  install_date DATE DEFAULT NULL COMMENT '安装日期',
  warranty_date DATE DEFAULT NULL COMMENT '保修截止',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_device_code (device_code),
  KEY idx_community_type (community_id, device_type),
  KEY idx_online_status (online_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

CREATE TABLE IF NOT EXISTS iot_device_status_log (
  id BIGINT NOT NULL COMMENT '主键ID',
  device_id BIGINT NOT NULL COMMENT '设备ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  event_type TINYINT NOT NULL COMMENT '1上线 2离线 3故障 4恢复',
  event_time DATETIME NOT NULL COMMENT '事件时间',
  extra VARCHAR(500) DEFAULT NULL COMMENT '附加信息',
  PRIMARY KEY (id),
  KEY idx_device_time (device_id, event_time),
  KEY idx_community_time (community_id, event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备状态日志表';

CREATE TABLE IF NOT EXISTS iot_device_event (
  id BIGINT NOT NULL COMMENT '主键ID',
  device_id BIGINT NOT NULL COMMENT '设备ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  event_code VARCHAR(50) NOT NULL COMMENT '事件编码',
  event_name VARCHAR(100) DEFAULT NULL COMMENT '事件名称',
  event_data TEXT COMMENT '事件数据(JSON)',
  event_time DATETIME NOT NULL COMMENT '事件时间',
  handle_status TINYINT DEFAULT 0 COMMENT '0未处理 1已处理',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_device_time (device_id, event_time),
  KEY idx_event_code (event_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备事件表';

-- ==================== 视频监控域 ====================

CREATE TABLE IF NOT EXISTS vs_channel (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  device_id BIGINT NOT NULL COMMENT '设备ID',
  channel_no INT DEFAULT 1 COMMENT '通道号',
  channel_name VARCHAR(100) NOT NULL COMMENT '通道名称',
  gb_code VARCHAR(64) DEFAULT NULL COMMENT '国标编码',
  stream_url VARCHAR(255) DEFAULT NULL COMMENT '拉流地址',
  has_ptz TINYINT DEFAULT 0 COMMENT '是否支持云台',
  has_audio TINYINT DEFAULT 0 COMMENT '是否支持音频',
  record_plan_id BIGINT DEFAULT NULL COMMENT '录像计划ID',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_device_id (device_id),
  KEY idx_gb_code (gb_code),
  KEY idx_community_id (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频通道表';

CREATE TABLE IF NOT EXISTS vs_record_plan (
  id BIGINT NOT NULL COMMENT '主键ID',
  plan_name VARCHAR(100) NOT NULL COMMENT '计划名称',
  record_type TINYINT DEFAULT 1 COMMENT '1全天 2定时 3移动侦测',
  start_time VARCHAR(10) DEFAULT '00:00' COMMENT '开始时间',
  end_time VARCHAR(10) DEFAULT '23:59' COMMENT '结束时间',
  storage_days INT DEFAULT 30 COMMENT '存储天数',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='录像计划表';

-- ==================== 安防管理域 ====================

CREATE TABLE IF NOT EXISTS sec_alarm_rule (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT DEFAULT NULL COMMENT '小区ID(NULL=全局)',
  rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
  alarm_type VARCHAR(32) NOT NULL COMMENT '适用告警类型',
  alarm_level TINYINT DEFAULT 2 COMMENT '匹配后告警级别 1紧急 2重要 3一般',
  auto_dispatch TINYINT DEFAULT 0 COMMENT '是否自动派单',
  auto_dispatch_role VARCHAR(50) DEFAULT NULL COMMENT '自动派单目标角色标识',
  notify_channels VARCHAR(100) DEFAULT NULL COMMENT '通知渠道 1站内信 2短信 3推送 4语音',
  sla_minutes INT DEFAULT 60 COMMENT '处置时限(分钟)',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_community_type (community_id, alarm_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';

CREATE TABLE IF NOT EXISTS sec_alarm_event (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  alarm_no VARCHAR(32) NOT NULL COMMENT '告警编号',
  alarm_type VARCHAR(32) NOT NULL COMMENT '告警类型 intrusion/occupy/ebike/smoke/door_force等',
  alarm_level TINYINT NOT NULL DEFAULT 2 COMMENT '1紧急 2重要 3一般',
  source_type TINYINT NOT NULL COMMENT '1设备 2AI 3人工',
  device_id BIGINT DEFAULT NULL COMMENT '设备ID',
  channel_id BIGINT DEFAULT NULL COMMENT '通道ID',
  title VARCHAR(200) NOT NULL COMMENT '告警标题',
  content VARCHAR(1000) DEFAULT NULL COMMENT '告警内容',
  snapshot_url VARCHAR(255) DEFAULT NULL COMMENT '抓拍图',
  clip_url VARCHAR(255) DEFAULT NULL COMMENT '告警片段',
  location VARCHAR(255) DEFAULT NULL COMMENT '告警位置',
  status TINYINT DEFAULT 0 COMMENT '0未处理 1处理中 2已处理 3误报',
  occur_time DATETIME NOT NULL COMMENT '发生时间',
  handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
  handler_id BIGINT DEFAULT NULL COMMENT '处理人ID',
  handler_name VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_alarm_no (alarm_no),
  KEY idx_community_status (community_id, status),
  KEY idx_occur_time (occur_time),
  KEY idx_alarm_type (alarm_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警事件表';

CREATE TABLE IF NOT EXISTS sec_alarm_handle (
  id BIGINT NOT NULL COMMENT '主键ID',
  alarm_id BIGINT NOT NULL COMMENT '告警ID',
  handle_type TINYINT NOT NULL COMMENT '1派单 2电话 3现场 4复核',
  handler_id BIGINT DEFAULT NULL COMMENT '处理人ID',
  handler_name VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
  handle_result VARCHAR(1000) DEFAULT NULL COMMENT '处理结果',
  handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
  work_order_id BIGINT DEFAULT NULL COMMENT '关联工单ID',
  attachments VARCHAR(1000) DEFAULT NULL COMMENT '附件',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_alarm_id (alarm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警处置记录表';

CREATE TABLE IF NOT EXISTS sec_access_record (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  device_id BIGINT NOT NULL COMMENT '设备ID',
  owner_id BIGINT DEFAULT NULL COMMENT '业主ID',
  person_name VARCHAR(50) DEFAULT NULL COMMENT '人员姓名',
  open_method TINYINT DEFAULT NULL COMMENT '1人脸 2二维码 3刷卡 4蓝牙 5远程 6密码',
  pass_result TINYINT DEFAULT 1 COMMENT '1通过 0拒绝',
  deny_reason VARCHAR(100) DEFAULT NULL COMMENT '拒绝原因',
  snapshot_url VARCHAR(255) DEFAULT NULL COMMENT '抓拍图',
  pass_time DATETIME NOT NULL COMMENT '通行时间',
  PRIMARY KEY (id),
  KEY idx_device_time (device_id, pass_time),
  KEY idx_owner_id (owner_id),
  KEY idx_community_time (community_id, pass_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁通行记录表';

CREATE TABLE IF NOT EXISTS sec_access_permission (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  device_id BIGINT NOT NULL COMMENT '门禁设备ID',
  owner_id BIGINT NOT NULL COMMENT '业主ID',
  owner_name VARCHAR(50) DEFAULT NULL COMMENT '业主姓名',
  time_start VARCHAR(10) DEFAULT '00:00' COMMENT '允许通行开始时段',
  time_end VARCHAR(10) DEFAULT '23:59' COMMENT '允许通行结束时段',
  effective_start DATE DEFAULT NULL COMMENT '生效开始',
  effective_end DATE DEFAULT NULL COMMENT '生效结束',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  UNIQUE KEY uk_device_owner (device_id, owner_id),
  KEY idx_community_id (community_id),
  KEY idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁授权表';

CREATE TABLE IF NOT EXISTS sec_visitor_invite (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  invite_no VARCHAR(32) NOT NULL COMMENT '邀请编号',
  inviter_id BIGINT NOT NULL COMMENT '邀请业主ID',
  inviter_name VARCHAR(50) DEFAULT NULL COMMENT '邀请人姓名',
  visitor_name VARCHAR(50) NOT NULL COMMENT '访客姓名',
  visitor_phone VARCHAR(20) DEFAULT NULL COMMENT '访客电话',
  visitor_face_url VARCHAR(255) DEFAULT NULL COMMENT '访客人脸',
  qr_code VARCHAR(255) DEFAULT NULL COMMENT '访客二维码',
  visit_start DATETIME NOT NULL COMMENT '到访开始时间',
  visit_end DATETIME NOT NULL COMMENT '到访结束时间',
  pass_scope VARCHAR(255) DEFAULT NULL COMMENT '可通行范围',
  visit_purpose VARCHAR(200) DEFAULT NULL COMMENT '来访目的',
  status TINYINT DEFAULT 0 COMMENT '0待到访 1已到访 2已过期 3已撤销',
  arrive_time DATETIME DEFAULT NULL COMMENT '实际到访时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_invite_no (invite_no),
  KEY idx_inviter (inviter_id),
  KEY idx_community_status (community_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客邀请表';

CREATE TABLE IF NOT EXISTS sec_vehicle_pass (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  gate_device_id BIGINT NOT NULL COMMENT '道闸设备ID',
  plate_no VARCHAR(20) NOT NULL COMMENT '车牌号',
  direction TINYINT DEFAULT 1 COMMENT '1进 2出',
  vehicle_type TINYINT DEFAULT 3 COMMENT '1固定 2月租 3临时 4访客',
  snapshot_url VARCHAR(255) DEFAULT NULL COMMENT '抓拍图',
  pass_time DATETIME NOT NULL COMMENT '通行时间',
  PRIMARY KEY (id),
  KEY idx_plate_time (plate_no, pass_time),
  KEY idx_community_time (community_id, pass_time),
  KEY idx_gate_time (gate_device_id, pass_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆通行记录表';

CREATE TABLE IF NOT EXISTS sec_blacklist (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  blacklist_type TINYINT NOT NULL COMMENT '1人员 2车辆',
  target_name VARCHAR(50) DEFAULT NULL COMMENT '姓名/车牌',
  target_id VARCHAR(50) DEFAULT NULL COMMENT '身份证/车牌号',
  face_img VARCHAR(255) DEFAULT NULL COMMENT '人脸图片',
  reason VARCHAR(500) DEFAULT NULL COMMENT '拉黑原因',
  effective_start DATE DEFAULT NULL COMMENT '生效开始',
  effective_end DATE DEFAULT NULL COMMENT '生效结束',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_community_type (community_id, blacklist_type),
  KEY idx_target_id (target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单表';

-- ==================== 业务管理域 ====================

CREATE TABLE IF NOT EXISTS biz_work_order (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  order_no VARCHAR(32) NOT NULL COMMENT '工单编号',
  order_type VARCHAR(32) NOT NULL COMMENT 'alarm/repair/complaint/patrol/fire',
  title VARCHAR(200) NOT NULL COMMENT '工单标题',
  content VARCHAR(2000) DEFAULT NULL COMMENT '工单内容',
  source_id BIGINT DEFAULT NULL COMMENT '来源ID',
  source_type VARCHAR(32) DEFAULT NULL COMMENT '来源类型',
  priority TINYINT DEFAULT 2 COMMENT '1紧急 2高 3中 4低',
  status TINYINT DEFAULT 0 COMMENT '0待派发 1待接单 2处理中 3待验收 4已完成 5已关闭',
  assignee_id BIGINT DEFAULT NULL COMMENT '处理人ID',
  assignee_name VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
  sla_deadline DATETIME DEFAULT NULL COMMENT 'SLA时限',
  finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
  evaluate_score TINYINT DEFAULT NULL COMMENT '评价分数1-5',
  evaluate_content VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_status (community_id, status),
  KEY idx_assignee (assignee_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

CREATE TABLE IF NOT EXISTS biz_work_order_flow (
  id BIGINT NOT NULL COMMENT '主键ID',
  order_id BIGINT NOT NULL COMMENT '工单ID',
  action VARCHAR(32) NOT NULL COMMENT 'create/assign/accept/handle/verify/evaluate/upgrade',
  operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
  operator_name VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  action_detail VARCHAR(1000) DEFAULT NULL COMMENT '操作详情',
  attachments VARCHAR(1000) DEFAULT NULL COMMENT '附件URL',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转表';

CREATE TABLE IF NOT EXISTS biz_notice (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  content TEXT COMMENT '内容',
  notice_type TINYINT DEFAULT 1 COMMENT '1公告 2通知 3活动',
  publish_status TINYINT DEFAULT 0 COMMENT '0草稿 1已发布',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  top_flag TINYINT DEFAULT 0 COMMENT '是否置顶',
  view_count INT DEFAULT 0 COMMENT '浏览次数',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_community_status (community_id, publish_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

CREATE TABLE IF NOT EXISTS biz_repair (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  repair_no VARCHAR(32) NOT NULL COMMENT '报修编号',
  owner_id BIGINT NOT NULL COMMENT '报修业主ID',
  room_id BIGINT DEFAULT NULL COMMENT '房屋ID',
  repair_type TINYINT DEFAULT 1 COMMENT '1水电 2门窗 3电梯 4其他',
  title VARCHAR(200) NOT NULL COMMENT '报修标题',
  content VARCHAR(2000) DEFAULT NULL COMMENT '报修内容',
  images VARCHAR(1000) DEFAULT NULL COMMENT '图片URL',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  expect_time DATETIME DEFAULT NULL COMMENT '期望上门时间',
  status TINYINT DEFAULT 0 COMMENT '0待派单 1已派单 2处理中 3已完成 4已评价',
  work_order_id BIGINT DEFAULT NULL COMMENT '关联工单ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_repair_no (repair_no),
  KEY idx_owner_id (owner_id),
  KEY idx_community_status (community_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修工单表';

-- ==================== 巡更管理域(P1 建表留接口) ====================

CREATE TABLE IF NOT EXISTS pat_point (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  point_name VARCHAR(100) NOT NULL COMMENT '点位名称',
  point_code VARCHAR(50) NOT NULL COMMENT '点位编码',
  location VARCHAR(255) DEFAULT NULL COMMENT '位置描述',
  longitude DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  latitude DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  nfc_code VARCHAR(100) DEFAULT NULL COMMENT 'NFC编码',
  qr_code VARCHAR(255) DEFAULT NULL COMMENT '二维码',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  UNIQUE KEY uk_point_code (point_code),
  KEY idx_community_id (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡更点位表';

CREATE TABLE IF NOT EXISTS pat_route (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  route_name VARCHAR(100) NOT NULL COMMENT '路线名称',
  route_code VARCHAR(50) NOT NULL COMMENT '路线编码',
  description VARCHAR(500) DEFAULT NULL COMMENT '路线描述',
  point_count INT DEFAULT 0 COMMENT '点位数量',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  UNIQUE KEY uk_route_code (route_code),
  KEY idx_community_id (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡更路线表';

CREATE TABLE IF NOT EXISTS pat_route_point (
  id BIGINT NOT NULL COMMENT '主键ID',
  route_id BIGINT NOT NULL COMMENT '路线ID',
  point_id BIGINT NOT NULL COMMENT '点位ID',
  order_num INT DEFAULT 0 COMMENT '顺序',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_route_point (route_id, point_id),
  KEY idx_route_id (route_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线点位关联表';

CREATE TABLE IF NOT EXISTS pat_plan (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  plan_name VARCHAR(100) NOT NULL COMMENT '计划名称',
  route_id BIGINT NOT NULL COMMENT '路线ID',
  patrol_type TINYINT DEFAULT 1 COMMENT '1日常 2夜间 3节假日',
  start_time VARCHAR(10) DEFAULT '20:00' COMMENT '开始时间',
  end_time VARCHAR(10) DEFAULT '06:00' COMMENT '结束时间',
  interval_minutes INT DEFAULT 60 COMMENT '巡更间隔(分钟)',
  assign_user_ids VARCHAR(500) DEFAULT NULL COMMENT '指定人员ID',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_community_id (community_id),
  KEY idx_route_id (route_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡更计划表';

CREATE TABLE IF NOT EXISTS pat_record (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  plan_id BIGINT NOT NULL COMMENT '计划ID',
  route_id BIGINT NOT NULL COMMENT '路线ID',
  point_id BIGINT NOT NULL COMMENT '点位ID',
  user_id BIGINT NOT NULL COMMENT '巡更人员ID',
  user_name VARCHAR(50) DEFAULT NULL COMMENT '巡更人员姓名',
  check_method TINYINT DEFAULT 1 COMMENT '1NFC 2二维码 3GPS',
  check_time DATETIME NOT NULL COMMENT '打卡时间',
  longitude DECIMAL(10,6) DEFAULT NULL COMMENT '打卡经度',
  latitude DECIMAL(10,6) DEFAULT NULL COMMENT '打卡纬度',
  status TINYINT DEFAULT 1 COMMENT '1正常 2异常 3漏巡',
  exception_desc VARCHAR(500) DEFAULT NULL COMMENT '异常描述',
  images VARCHAR(1000) DEFAULT NULL COMMENT '现场图片',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_plan_time (plan_id, check_time),
  KEY idx_user_time (user_id, check_time),
  KEY idx_community_time (community_id, check_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡更记录表';

-- ==================== 消息通知域 ====================

CREATE TABLE IF NOT EXISTS msg_template (
  id BIGINT NOT NULL COMMENT '主键ID',
  template_code VARCHAR(50) NOT NULL COMMENT '模板编码',
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  channel_type TINYINT NOT NULL COMMENT '1站内信 2短信 3推送 4语音',
  content_template TEXT NOT NULL COMMENT '内容模板',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

CREATE TABLE IF NOT EXISTS msg_send_log (
  id BIGINT NOT NULL COMMENT '主键ID',
  template_id BIGINT DEFAULT NULL COMMENT '模板ID',
  channel_type TINYINT NOT NULL COMMENT '1站内信 2短信 3推送 4语音',
  receiver_id BIGINT DEFAULT NULL COMMENT '接收人ID',
  receiver_name VARCHAR(50) DEFAULT NULL COMMENT '接收人姓名',
  receiver_contact VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  title VARCHAR(200) DEFAULT NULL COMMENT '标题',
  content TEXT COMMENT '内容',
  send_status TINYINT DEFAULT 0 COMMENT '0待发送 1已发送 2失败',
  read_flag TINYINT DEFAULT 0 COMMENT '站内信是否已读',
  send_time DATETIME DEFAULT NULL COMMENT '发送时间',
  error_msg VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_receiver (receiver_id),
  KEY idx_send_time (send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息发送日志表';

-- ==================== AI分析域(P1 建表留接口) ====================

CREATE TABLE IF NOT EXISTS ai_scene_config (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  scene_code VARCHAR(50) NOT NULL COMMENT '场景编码',
  scene_name VARCHAR(100) NOT NULL COMMENT '场景名称',
  channel_ids VARCHAR(500) DEFAULT NULL COMMENT '关联通道ID',
  algorithm_type VARCHAR(50) DEFAULT NULL COMMENT '算法类型',
  threshold DECIMAL(5,2) DEFAULT 0.80 COMMENT '置信度阈值',
  alarm_level TINYINT DEFAULT 2 COMMENT '告警级别',
  enable_time_start VARCHAR(10) DEFAULT '00:00' COMMENT '启用开始时间',
  enable_time_end VARCHAR(10) DEFAULT '23:59' COMMENT '启用结束时间',
  status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (id),
  KEY idx_community_scene (community_id, scene_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI场景配置表';

CREATE TABLE IF NOT EXISTS ai_event_record (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  scene_id BIGINT DEFAULT NULL COMMENT '场景ID',
  channel_id BIGINT DEFAULT NULL COMMENT '通道ID',
  event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
  confidence DECIMAL(5,2) DEFAULT NULL COMMENT '置信度',
  snapshot_url VARCHAR(255) DEFAULT NULL COMMENT '抓拍图',
  clip_url VARCHAR(255) DEFAULT NULL COMMENT '视频片段',
  bbox_data TEXT COMMENT '检测框数据(JSON)',
  event_time DATETIME NOT NULL COMMENT '事件时间',
  handle_status TINYINT DEFAULT 0 COMMENT '0未处理 1已处理 2误报',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_community_time (community_id, event_time),
  KEY idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI事件记录表';

-- ==================== 统计分析域 ====================

CREATE TABLE IF NOT EXISTS stat_daily_summary (
  id BIGINT NOT NULL COMMENT '主键ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  stat_date DATE NOT NULL COMMENT '统计日期',
  alarm_total INT DEFAULT 0 COMMENT '告警总数',
  alarm_handled INT DEFAULT 0 COMMENT '已处理告警',
  access_total INT DEFAULT 0 COMMENT '门禁通行总数',
  vehicle_in_total INT DEFAULT 0 COMMENT '车辆进入总数',
  vehicle_out_total INT DEFAULT 0 COMMENT '车辆离开总数',
  visitor_total INT DEFAULT 0 COMMENT '访客总数',
  work_order_total INT DEFAULT 0 COMMENT '工单总数',
  work_order_finished INT DEFAULT 0 COMMENT '已完成工单',
  patrol_total INT DEFAULT 0 COMMENT '巡更打卡总数',
  patrol_missing INT DEFAULT 0 COMMENT '漏巡数',
  device_online_rate DECIMAL(5,2) DEFAULT NULL COMMENT '设备在线率',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_community_date (community_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日统计汇总表';

-- ==================== 文件管理域 ====================

CREATE TABLE IF NOT EXISTS file_upload (
  id BIGINT NOT NULL COMMENT '主键ID',
  file_name VARCHAR(255) NOT NULL COMMENT '文件名',
  file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
  file_url VARCHAR(500) NOT NULL COMMENT '访问URL',
  file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
  file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
  biz_type VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
  biz_id BIGINT DEFAULT NULL COMMENT '业务ID',
  upload_user_id BIGINT DEFAULT NULL COMMENT '上传人ID',
  upload_user_name VARCHAR(50) DEFAULT NULL COMMENT '上传人姓名',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传记录表';

-- ==================== 用户-小区数据权限(多小区隔离) ====================
-- 说明: 45 张表中 sys_user 仅带 dept_id, 无法实现"小区级数据隔离"(招标★实质性条款),
--       故补充一张最小映射表, 支持一个用户管理多个小区。
CREATE TABLE IF NOT EXISTS sys_user_community (
  id BIGINT NOT NULL COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  community_id BIGINT NOT NULL COMMENT '小区ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_community (user_id, community_id),
  KEY idx_community_id (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户小区数据权限表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 歧义修正说明(与《架构.md》《总体建设方案.md》原文差异)
-- 1. vs_channel: 采用 record_plan_id BIGINT 关联 vs_record_plan(原文核心DDL为 record_plan VARCHAR)
-- 2. 字典: 采用 sys_dict_type + sys_dict_data 两表(原文清单曾写作 sys_dict 单表)
-- 3. iot_device_status_log: 增加 community_id(与完整DDL一致, 便于多小区检索)
-- 4. base_room.room_type: 采用 5 类(1住宅 2商铺 3车位 4储藏室 5其他)
-- 5. sec_alarm_event.status: 统一为 0未处理 1处理中 2已处理 3误报
-- 6. base_owner: 增加 phone_mask / id_card_mask 脱敏列(满足"加密存储+脱敏展示"要求)
-- 7. msg_send_log: 增加 read_flag 支撑"我的消息/标记已读"接口
-- 8. 新增 sec_alarm_rule、sec_access_permission 补足接口清单中无对应表的接口
-- 9. 新增 sys_user_community 支撑"小区级数据隔离"(sys_user 仅带 dept_id, 无法表达多小区)
-- ============================================================
