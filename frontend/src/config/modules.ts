import { sysApi, baseApi, iotApi, videoApi, secApi, msgApi, alarmApi } from '@/api'
import { useAppStore } from '@/store/app'

const community = () => useAppStore().communityId

function withCommunity(params: any) {
  const cid = community()
  if (cid) params.communityId = cid
  return params
}

function wrap(fetcher: (p: any) => Promise<any>) {
  return async (params: any) => {
    const res = await fetcher(withCommunity({ ...params }))
    return { list: res.records || res.list || [], total: res.total || 0 }
  }
}

function m(obj: Record<number, string>) {
  return obj as any
}

export interface ModuleConfig {
  title: string
  columns: any[]
  fetch: (p: any) => Promise<{ list: any[]; total: number }>
  formFields?: any[]
  searchFields?: any[]
  createApi?: (d: any) => Promise<any>
  updateApi?: (d: any) => Promise<any>
  removeApi?: (id: any) => Promise<any>
  addText?: string
}

export const moduleConfig: Record<string, ModuleConfig> = {
  '/system/user': {
    title: '用户',
    fetch: (p) => wrap((q) => sysApi.users(q))(p),
    columns: [
      { prop: 'username', label: '用户名', width: 120 },
      { prop: 'realName', label: '姓名', width: 110 },
      { prop: 'phone', label: '手机', width: 130 },
      { prop: 'email', label: '邮箱', width: 160 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '正常' : '停用') },
      { prop: 'loginTime', label: '最后登录', minWidth: 150 }
    ],
    searchFields: [
      { prop: 'username', label: '用户名' },
      { prop: 'phone', label: '手机' }
    ]
  },
  '/system/role': {
    title: '角色',
    fetch: (p) => wrap((q) => sysApi.roles(q))(p),
    columns: [
      { prop: 'roleName', label: '角色名称', width: 160 },
      { prop: 'roleKey', label: '权限标识', width: 160 },
      { prop: 'dataScope', label: '数据权限', width: 120, formatter: (_: any, r: any) =>
        (m({ 1: '全部', 2: '自定', 3: '本部门', 4: '本小区', 5: '仅本人' })[r.dataScope] || r.dataScope) },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '正常' : '停用') },
      { prop: 'remark', label: '备注', minWidth: 160 }
    ]
  },
  '/system/dept': {
    title: '部门',
    fetch: (p) => wrap((q) => sysApi.depts(q))(p),
    columns: [
      { prop: 'deptName', label: '部门名称', width: 180 },
      { prop: 'orderNum', label: '排序', width: 90 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '正常' : '停用') }
    ]
  },
  '/system/dict': {
    title: '字典',
    fetch: (p) => wrap((q) => sysApi.dictTypes(q))(p),
    columns: [
      { prop: 'dictName', label: '字典名称', width: 160 },
      { prop: 'dictType', label: '字典类型', width: 180 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '正常' : '停用') },
      { prop: 'remark', label: '备注', minWidth: 180 }
    ]
  },
  '/system/config': {
    title: '参数',
    fetch: (p) => wrap((q) => sysApi.configs(q))(p),
    columns: [
      { prop: 'configName', label: '参数名称', width: 180 },
      { prop: 'configKey', label: '参数键', width: 180 },
      { prop: 'configValue', label: '参数值', minWidth: 180 },
      { prop: 'remark', label: '备注', minWidth: 140 }
    ]
  },
  '/system/log/oper': {
    title: '操作日志',
    fetch: (p) => wrap((q) => sysApi.operLogs(q))(p),
    columns: [
      { prop: 'title', label: '操作模块', width: 140 },
      { prop: 'businessType', label: '类型', width: 80 },
      { prop: 'operName', label: '操作人', width: 110 },
      { prop: 'operIp', label: 'IP', width: 130 },
      { prop: 'operTime', label: '时间', minWidth: 160 }
    ]
  },
  '/system/log/login': {
    title: '登录日志',
    fetch: (p) => wrap((q) => sysApi.loginLogs(q))(p),
    columns: [
      { prop: 'username', label: '用户', width: 130 },
      { prop: 'ipaddr', label: 'IP', width: 130 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === '0' ? '成功' : '失败') },
      { prop: 'loginTime', label: '时间', minWidth: 160 }
    ]
  },
  '/base/community': {
    title: '小区',
    fetch: (p) => wrap((q) => baseApi.communities(q))(p),
    columns: [
      { prop: 'communityName', label: '小区名称', width: 160 },
      { prop: 'communityCode', label: '编号', width: 130 },
      { prop: 'address', label: '地址', minWidth: 200 },
      { prop: 'manager', label: '负责人', width: 100 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '启用' : '停用') }
    ],
    searchFields: [{ prop: 'communityName', label: '名称' }],
    formFields: [
      { prop: 'communityName', label: '小区名称', required: true },
      { prop: 'communityCode', label: '编号', required: true },
      { prop: 'address', label: '地址', type: 'input' },
      { prop: 'manager', label: '负责人', type: 'input' },
      { prop: 'phone', label: '联系电话', type: 'input' },
      { prop: 'status', label: '状态', type: 'select', options: [{ value: 1, label: '启用' }, { value: 0, label: '停用' }] }
    ],
    createApi: baseApi.saveCommunity,
    updateApi: baseApi.saveCommunity,
    removeApi: baseApi.removeCommunity,
    addText: '新增小区'
  },
  '/base/building': {
    title: '楼栋',
    fetch: (p) => wrap((q) => baseApi.buildings(q))(p),
    columns: [
      { prop: 'buildingName', label: '楼栋名称', width: 140 },
      { prop: 'buildingCode', label: '楼栋号', width: 120 },
      { prop: 'floorCount', label: '层数', width: 90 },
      { prop: 'unitCount', label: '单元数', width: 90 }
    ]
  },
  '/base/unit': {
    title: '单元',
    fetch: (p) => wrap((q) => baseApi.units(q))(p),
    columns: [
      { prop: 'unitName', label: '单元名称', width: 140 },
      { prop: 'unitCode', label: '单元号', width: 120 },
      { prop: 'floorCount', label: '层数', width: 90 }
    ]
  },
  '/base/room': {
    title: '房屋',
    fetch: (p) => wrap((q) => baseApi.rooms(q))(p),
    columns: [
      { prop: 'roomNo', label: '房号', width: 120 },
      { prop: 'buildingName', label: '楼栋', width: 120 },
      { prop: 'unitName', label: '单元', width: 100 },
      { prop: 'roomTypeName', label: '类型', width: 100 },
      { prop: 'area', label: '面积(㎡)', width: 110 }
    ]
  },
  '/base/owner': {
    title: '业主',
    fetch: (p) => wrap((q) => baseApi.owners(q))(p),
    columns: [
      { prop: 'ownerName', label: '姓名', width: 110 },
      { prop: 'phoneMask', label: '手机', width: 140 },
      { prop: 'idCardMask', label: '证件号', width: 160 },
      { prop: 'roomNo', label: '房屋', width: 120 },
      { prop: 'ownerTypeName', label: '类型', width: 100 }
    ],
    searchFields: [{ prop: 'ownerName', label: '姓名' }]
  },
  '/base/vehicle': {
    title: '车辆',
    fetch: (p) => wrap((q) => baseApi.vehicles(q))(p),
    columns: [
      { prop: 'plateNo', label: '车牌', width: 130 },
      { prop: 'ownerName', label: '车主', width: 110 },
      { prop: 'vehicleTypeName', label: '类型', width: 100 },
      { prop: 'color', label: '颜色', width: 90 }
    ]
  },
  '/iot/device-type': {
    title: '设备类型',
    fetch: (p) => wrap((q) => iotApi.deviceTypes(q))(p),
    columns: [
      { prop: 'typeName', label: '类型名称', width: 160 },
      { prop: 'typeCode', label: '编码', width: 140 },
      { prop: 'orderNum', label: '排序', width: 80 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '启用' : '停用') },
      { prop: 'remark', label: '备注', minWidth: 160 }
    ],
    searchFields: [
      { prop: 'typeName', label: '类型名称' },
      { prop: 'status', label: '状态', type: 'select', options: [
        { label: '启用', value: 1 }, { label: '停用', value: 0 }
      ] }
    ],
    formFields: [
      { prop: 'typeCode', label: '类型编码', required: true, type: 'input' },
      { prop: 'typeName', label: '类型名称', required: true, type: 'input' },
      { prop: 'parentId', label: '父类型ID', type: 'number' },
      { prop: 'icon', label: '图标', type: 'input' },
      { prop: 'orderNum', label: '排序', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: [
        { label: '启用', value: 1 }, { label: '停用', value: 0 }
      ] },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ],
    createApi: iotApi.saveDeviceType,
    updateApi: iotApi.saveDeviceType,
    removeApi: iotApi.removeDeviceType,
    addText: '新增类型'
  },
  '/video/channel': {
    title: '视频通道',
    fetch: (p) => wrap((q) => videoApi.channels(q))(p),
    columns: [
      { prop: 'channelName', label: '通道名称', width: 160 },
      { prop: 'gbCode', label: '国标编码', width: 180 },
      { prop: 'streamUrl', label: '流地址', minWidth: 200 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '启用' : '停用') }
    ]
  },
  '/video/record-plan': {
    title: '录像计划',
    fetch: (p) => wrap((q) => videoApi.recordPlans(q))(p),
    columns: [
      { prop: 'planName', label: '计划名称', width: 160 },
      { prop: 'recordType', label: '类型', width: 110, formatter: (_: any, r: any) =>
        (m({ 1: '全天', 2: '定时', 3: '移动侦测' })[r.recordType] || r.recordType) },
      { prop: 'storageDays', label: '存储天数', width: 100 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '启用' : '停用') }
    ]
  },
  '/access/device': {
    title: '门禁设备',
    fetch: (p) => wrap((q) => secApi.accessDevices(q))(p),
    columns: [
      { prop: 'deviceName', label: '设备名称', width: 160 },
      { prop: 'deviceCode', label: '设备编码', width: 160 },
      { prop: 'installLocation', label: '安装位置', minWidth: 180 }
    ]
  },
  '/access/record': {
    title: '通行记录',
    fetch: (p) => wrap((q) => secApi.accessRecords(q))(p),
    columns: [
      { prop: 'personName', label: '人员', width: 120 },
      { prop: 'deviceName', label: '设备', width: 160 },
      { prop: 'openMethod', label: '开门方式', width: 110, formatter: (_: any, r: any) =>
        (m({ 1: '人脸', 2: '二维码', 3: '刷卡', 4: '蓝牙', 5: '远程', 6: '密码' })[r.openMethod] || r.openMethod) },
      { prop: 'passResult', label: '结果', width: 80, formatter: (_: any, r: any) => (r.passResult === 1 ? '通过' : '拒绝') },
      { prop: 'passTime', label: '时间', minWidth: 160 }
    ]
  },
  '/access/permission': {
    title: '门禁授权',
    fetch: (p) => wrap((q) => secApi.permissions(q))(p),
    columns: [
      { prop: 'ownerName', label: '业主', width: 120 },
      { prop: 'deviceName', label: '门禁设备', width: 160 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '启用' : '停用') }
    ]
  },
  '/alarm/rule': {
    title: '告警规则',
    fetch: async (p) => ({ list: await alarmApi.rules(p.communityId), total: 0 }),
    columns: [
      { prop: 'ruleName', label: '规则名称', width: 140 },
      { prop: 'alarmType', label: '告警类型', width: 120 },
      { prop: 'alarmLevel', label: '级别', width: 80, formatter: (_: any, r: any) => (m({ 1: '紧急', 2: '重要', 3: '一般' })[r.alarmLevel] || r.alarmLevel) },
      { prop: 'autoDispatch', label: '自动派单', width: 90, formatter: (_: any, r: any) => (r.autoDispatch === 1 ? '是' : '否') },
      { prop: 'notifyChannels', label: '通知渠道', width: 120 },
      { prop: 'slaMinutes', label: 'SLA(分)', width: 90 },
      { prop: 'status', label: '状态', width: 80, formatter: (_: any, r: any) => (r.status === 1 ? '启用' : '停用') }
    ]
  },
  '/visitor/invite': {
    title: '访客邀请',
    fetch: (p) => wrap((q) => secApi.visitorList(q))(p),
    columns: [
      { prop: 'inviteNo', label: '邀请编号', width: 150 },
      { prop: 'visitorName', label: '访客', width: 110 },
      { prop: 'visitorPhone', label: '电话', width: 140 },
      { prop: 'inviterName', label: '邀请人', width: 110 },
      { prop: 'status', label: '状态', width: 90, formatter: (_: any, r: any) =>
        (m({ 0: '待到访', 1: '已到访', 2: '已过期', 3: '已撤销' })[r.status] || r.status) },
      { prop: 'visitStart', label: '到访时间', minWidth: 160 }
    ],
    formFields: [
      { prop: 'visitorName', label: '访客姓名', required: true },
      { prop: 'visitorPhone', label: '访客电话', required: true },
      { prop: 'inviterName', label: '邀请人', type: 'input' },
      { prop: 'visitPurpose', label: '来访目的', type: 'input' },
      { prop: 'visitStart', label: '到访开始', type: 'date' },
      { prop: 'visitEnd', label: '到访结束', type: 'date' }
    ],
    createApi: secApi.visitorCreate,
    addText: '新增邀请'
  },
  '/visitor/record': {
    title: '访客记录',
    fetch: (p) => wrap((q) => secApi.visitorRecords(q))(p),
    columns: [
      { prop: 'inviteNo', label: '邀请编号', width: 150 },
      { prop: 'visitorName', label: '访客', width: 110 },
      { prop: 'status', label: '状态', width: 90, formatter: (_: any, r: any) =>
        (m({ 0: '待到访', 1: '已到访', 2: '已过期', 3: '已撤销' })[r.status] || r.status) },
      { prop: 'arriveTime', label: '实际到访', minWidth: 160 }
    ]
  },
  '/vehicle/pass': {
    title: '车辆通行',
    fetch: (p) => wrap((q) => secApi.vehiclePass(q))(p),
    columns: [
      { prop: 'plateNo', label: '车牌', width: 130 },
      { prop: 'direction', label: '方向', width: 80, formatter: (_: any, r: any) => (r.direction === 1 ? '进' : '出') },
      { prop: 'vehicleTypeName', label: '类型', width: 100 },
      { prop: 'gateName', label: '道闸', width: 160 },
      { prop: 'passTime', label: '时间', minWidth: 160 }
    ]
  },
  '/msg/notice': {
    title: '公告',
    fetch: (p) => wrap((q) => msgApi.notices(q))(p),
    columns: [
      { prop: 'title', label: '标题', minWidth: 200 },
      { prop: 'noticeTypeName', label: '类型', width: 100 },
      { prop: 'publishStatus', label: '状态', width: 90, formatter: (_: any, r: any) => (r.publishStatus === 1 ? '已发布' : '草稿') },
      { prop: 'publishTime', label: '发布时间', width: 160 }
    ],
    formFields: [
      { prop: 'title', label: '标题', required: true },
      { prop: 'noticeType', label: '类型', type: 'select', options: [{ value: 1, label: '通知' }, { value: 2, label: '公告' }, { value: 3, label: '提醒' }] },
      { prop: 'content', label: '内容', type: 'textarea' }
    ],
    createApi: msgApi.saveNotice,
    updateApi: msgApi.saveNotice,
    removeApi: msgApi.removeNotice,
    addText: '发布公告'
  },
  '/msg/log': {
    title: '发送记录',
    fetch: (p) => wrap((q) => msgApi.myMessages().then((list: any) => ({ records: list, total: list.length })))(p),
    columns: [
      { prop: 'title', label: '标题', minWidth: 180 },
      { prop: 'channelTypeName', label: '渠道', width: 100 },
      { prop: 'readFlag', label: '已读', width: 80, formatter: (_: any, r: any) => (r.readFlag === 1 ? '已读' : '未读') },
      { prop: 'createTime', label: '时间', minWidth: 160 }
    ]
  }
}
