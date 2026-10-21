import { get, post, put, del } from './request'

export const authApi = {
  login: (data: any) => post('/auth/login', data),
  info: () => get('/auth/info'),
  check: () => get('/auth/check'),
  logout: () => post('/auth/logout')
}

export const sysApi = {
  users: (q: any) => get('/sys/user/list', q),
  saveUser: (d: any) => (d.id ? put('/sys/user', d) : post('/sys/user', d)),
  roles: (q: any) => get('/sys/role/list', q),
  menus: () => get('/sys/menu/list'),
  menuTree: () => get('/sys/menu/tree'),
  depts: (q?: any) => get('/sys/dept/list', q),
  dictTypes: (q: any) => get('/sys/dict/type/list', q),
  dictData: (type: string) => get('/sys/dict/data/type/' + type),
  configs: (q: any) => get('/sys/config/list', q),
  operLogs: (q: any) => get('/sys/log/oper/list', q),
  loginLogs: (q: any) => get('/sys/log/login/list', q)
}

export const baseApi = {
  communities: (q: any) => get('/base/community/list', q),
  communityOptions: () => get('/base/community/options'),
  buildings: (q: any) => get('/base/building/list', q),
  units: (q: any) => get('/base/unit/list', q),
  rooms: (q: any) => get('/base/room/list', q),
  owners: (q: any) => get('/base/owner/list', q),
  vehicles: (q: any) => get('/base/vehicle/list', q),
  saveCommunity: (d: any) => (d.id ? put('/base/community', d) : post('/base/community', d)),
  removeCommunity: (ids: any) => del('/base/community/' + ids)
}

export const iotApi = {
  devices: (q: any) => get('/iot/device/list', q),
  deviceStatus: (c: any) => get('/iot/device/status', { communityId: c }),
  deviceTypeStat: (c: any) => get('/iot/device/type-stat', { communityId: c }),
  deviceLogs: (d: any) => get('/iot/device/log', d),
  saveDevice: (d: any) => (d.id ? put('/iot/device', d) : post('/iot/device', d)),
  restart: (id: any) => post('/iot/device/restart', { id }),
  deviceEvents: (q: any) => get('/iot/device-event/list', q),
  reportDeviceEvent: (d: any) => post('/iot/device-event/report', d),
  handleDeviceEvent: (id: any) => put('/iot/device-event/handle/' + id),
  deviceTypes: (q: any) => get('/iot/device-type/list', q),
  deviceTypeOptions: () => get('/iot/device-type/options'),
  saveDeviceType: (d: any) => (d.id ? put('/iot/device-type', d) : post('/iot/device-type', d)),
  removeDeviceType: (ids: any) => del('/iot/device-type/' + ids)
}

export const videoApi = {
  channels: (q: any) => get('/video/channel/list', q),
  channelTree: (c: any) => get('/video/channel/tree', { communityId: c }),
  live: (id: any) => get('/video/live/' + id),
  playback: (id: any, start: any, end: any) => get('/video/playback/' + id, { start, end }),
  recordPlans: (q?: any) => get('/video/record/list', q),
  ptz: (d: any) => post('/video/ptz/control', d)
}

export const alarmApi = {
  list: (q: any) => get('/alarm/event/list', q),
  detail: (id: any) => get('/alarm/event/' + id),
  receive: (d: any) => post('/alarm/event', d),
  handle: (d: any) => put('/alarm/event/handle', d),
  markFalse: (d: any) => put('/alarm/event/false', d),
  summary: (c: any) => get('/alarm/stat/summary', { communityId: c }),
  trend: (c: any, days: any) => get('/alarm/stat/trend', { communityId: c, days }),
  typeStat: (c: any) => get('/alarm/stat/type', { communityId: c }),
  rules: (c: any) => get('/alarm/rule/list', { communityId: c })
}

export const workOrderApi = {
  list: (q: any) => get('/workorder/list', q),
  detail: (id: any) => get('/workorder/' + id),
  flows: (id: any) => get('/workorder/flow/' + id),
  create: (d: any) => post('/workorder', d),
  assign: (d: any) => put('/workorder/assign', d),
  accept: (d: any) => put('/workorder/accept', d),
  handle: (d: any) => put('/workorder/handle', d),
  finish: (d: any) => put('/workorder/finish', d),
  verify: (d: any) => put('/workorder/verify', d),
  evaluate: (d: any) => put('/workorder/evaluate', d),
  close: (d: any) => put('/workorder/close', d),
  summary: (c: any) => get('/workorder/stat/summary', { communityId: c })
}

export const secApi = {
  accessDevices: (c: any) => get('/access/device/list', { communityId: c }),
  accessRecords: (q: any) => get('/access/record/list', q),
  permissions: (q: any) => get('/access/permission/list', q),
  remoteOpen: (d: any) => post('/access/remote-open', d),
  visitorList: (q: any) => get('/visitor/invite/list', q),
  visitorRecords: (q: any) => get('/visitor/record/list', q),
  visitorCreate: (d: any) => post('/visitor/invite', d),
  visitorCancel: (id: any) => del('/visitor/invite/' + id),
  vehiclePass: (q: any) => get('/vehicle/pass/list', q),
  vehicleDaily: (c: any, days: any) => get('/vehicle/stat/daily', { communityId: c, days }),
  gates: (c: any) => get('/vehicle/gate/list', { communityId: c }),
  blacklists: (q: any) => get('/access/blacklist/list', q),
  saveBlacklist: (d: any) => (d.id ? put('/access/blacklist', d) : post('/access/blacklist', d)),
  removeBlacklist: (ids: any) => del('/access/blacklist/' + ids)
}

export const repairApi = {
  list: (q: any) => get('/repair/list', q),
  detail: (id: any) => get('/repair/' + id),
  create: (d: any) => post('/repair', d),
  dispatch: (d: any) => post('/repair/dispatch', d),
  start: (id: any) => put('/repair/start', { id }),
  finish: (id: any, remark?: string) => put('/repair/finish', { id, remark }),
  evaluate: (id: any, content?: string) => put('/repair/evaluate', { id, content })
}

export const aiApi = {
  scenes: (q: any) => get('/ai/scene/list', q),
  saveScene: (d: any) => (d.id ? put('/ai/scene', d) : post('/ai/scene', d)),
  removeScene: (ids: any) => del('/ai/scene/' + ids),
  events: (q: any) => get('/ai/event/list', q),
  callback: (d: any) => post('/ai/event/callback', d),
  handleEvent: (id: any, handleStatus: any) => put('/ai/event/handle', { id, handleStatus })
}

export const patrolApi = {
  points: (q: any) => get('/patrol/point/list', q),
  pointOptions: (c: any) => get('/patrol/point/options', { communityId: c }),
  savePoint: (d: any) => (d.id ? put('/patrol/point', d) : post('/patrol/point', d)),
  removePoint: (ids: any) => del('/patrol/point/' + ids),
  routes: (q: any) => get('/patrol/route/list', q),
  routeOptions: (c: any) => get('/patrol/route/options', { communityId: c }),
  routeDetail: (id: any) => get('/patrol/route/' + id),
  saveRoute: (d: any) => (d.route?.id ? put('/patrol/route', d) : post('/patrol/route', d)),
  removeRoute: (ids: any) => del('/patrol/route/' + ids),
  plans: (q: any) => get('/patrol/plan/list', q),
  savePlan: (d: any) => (d.id ? put('/patrol/plan', d) : post('/patrol/plan', d)),
  removePlan: (ids: any) => del('/patrol/plan/' + ids),
  records: (q: any) => get('/patrol/record/list', q),
  checkIn: (d: any) => post('/patrol/record/check-in', d),
  recordStat: (c: any, date: any) => get('/patrol/record/stat', { communityId: c, date }),
  markMissing: (date: any) => post('/patrol/record/mark-missing?date=' + date)
}

export const msgApi = {
  notices: (q: any) => get('/msg/notice/list', q),
  noticeDetail: (id: any) => get('/msg/notice/' + id),
  saveNotice: (d: any) => (d.id ? put('/msg/notice', d) : post('/msg/notice', d)),
  publish: (id: any) => put('/msg/notice/publish/' + id),
  removeNotice: (ids: any) => del('/msg/notice/' + ids),
  myMessages: () => get('/msg/my/list'),
  unread: () => get('/msg/my/unread'),
  read: (id: any) => put('/msg/my/read/' + id),
  templates: (q: any) => get('/msg/template/list', q),
  saveTemplate: (d: any) => (d.id ? put('/msg/template', d) : post('/msg/template', d)),
  removeTemplate: (ids: any) => del('/msg/template/' + ids),
  renderTemplate: (d: any) => post('/msg/template/render', d)
}

export const statApi = {
  overview: (c: any) => get('/stat/overview', { communityId: c }),
  alarmTrend: (c: any, days: any) => get('/stat/alarm/trend', { communityId: c, days }),
  accessTrend: (c: any, days: any) => get('/stat/access/trend', { communityId: c, days }),
  vehicleTrend: (c: any, days: any) => get('/stat/vehicle/trend', { communityId: c, days }),
  workOrderSummary: (c: any) => get('/stat/workorder/summary', { communityId: c }),
  deviceOnlineRate: (c: any) => get('/stat/device/online-rate', { communityId: c }),
  safetyIndex: (c: any) => get('/stat/safety-index', { communityId: c }),
  dailyList: (q: any) => get('/stat/daily/list', q),
  generateDaily: (d: any) => post('/stat/daily/generate', d)
}

export const screenApi = {
  realtime: (c: any) => get('/screen/realtime', { communityId: c }),
  latestAlarm: (c: any, limit: any) => get('/screen/alarm/latest', { communityId: c, limit }),
  deviceStatus: (c: any) => get('/screen/device/status', { communityId: c }),
  rotation: (c: any, size: any) => get('/screen/video/rotation', { communityId: c, size })
}
