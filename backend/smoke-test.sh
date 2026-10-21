#!/usr/bin/env bash
# 智慧安防小区运营平台 - 接口冒烟测试
# 用法: ./smoke-test.sh [baseUrl]
BASE=${1:-http://127.0.0.1:8088}
USER=${2:-admin}
PWD=${3:-admin123}

pass=0; fail=0
login() {
  curl -s -m 8 -X POST "$BASE/api/v1/auth/login" -H "Content-Type: application/json" \
    -d "{\"username\":\"$USER\",\"password\":\"$PWD\"}" \
    | python3 -c "import sys,json;d=json.load(sys.stdin);print(d.get('data',{}).get('accessToken',''))"
}
TOKEN=$(login)
if [ -z "$TOKEN" ]; then echo "登录失败"; exit 1; fi
echo "登录成功: $USER"

check() {
  local name=$1 url=$2 method=${3:-GET} body=${4:-}
  local code
  if [ "$method" = "GET" ]; then
    code=$(curl -s -m 10 -o /tmp/smoke.json -w "%{http_code}" "$BASE$url" -H "Authorization: Bearer $TOKEN")
  else
    code=$(curl -s -m 10 -o /tmp/smoke.json -w "%{http_code}" -X "$method" "$BASE$url" \
      -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$body")
  fi
  local ok
  ok=$(python3 -c "
import json,sys
try:
    d=json.load(open('/tmp/smoke.json'))
    print('OK' if d.get('code')==200 else 'BIZ:'+str(d.get('code'))+':'+str(d.get('msg'))[:60])
except Exception as e:
    print('NOJSON')
" 2>/dev/null)
  if [ "$code" = "200" ] && [ "$ok" = "OK" ]; then
    pass=$((pass+1)); printf "  \033[32mPASS\033[0m %s\n" "$name"
  else
    fail=$((fail+1)); printf "  \033[31mFAIL\033[0m %s (http=%s, %s)\n" "$name" "$code" "$ok"
  fi
}

echo "== 认证与系统 =="
check "获取当前用户信息" "/api/v1/auth/info"
check "令牌校验" "/api/v1/auth/check"
check "用户列表" "/api/v1/sys/user/list?pageNum=1&pageSize=5"
check "角色列表" "/api/v1/sys/role/list?pageNum=1&pageSize=10"
check "菜单列表" "/api/v1/sys/menu/list"
check "部门列表" "/api/v1/sys/dept/list"
check "字典类型" "/api/v1/sys/dict/type/list?pageNum=1&pageSize=10"
check "字典数据-告警类型" "/api/v1/sys/dict/data/type/alarm_type"
check "参数配置" "/api/v1/sys/config/list?pageNum=1&pageSize=10"
check "操作日志" "/api/v1/sys/log/oper/list?pageNum=1&pageSize=5"
check "登录日志" "/api/v1/sys/log/login/list?pageNum=1&pageSize=5"

echo "== 小区基础 =="
check "小区列表" "/api/v1/base/community/list?pageNum=1&pageSize=10"
check "小区下拉" "/api/v1/base/community/options"
check "楼栋列表" "/api/v1/base/building/list?pageNum=1&pageSize=10"
check "单元列表" "/api/v1/base/unit/list?pageNum=1&pageSize=10"
check "房屋列表" "/api/v1/base/room/list?pageNum=1&pageSize=10"
check "业主列表" "/api/v1/base/owner/list?pageNum=1&pageSize=10"
check "车辆档案" "/api/v1/base/vehicle/list?pageNum=1&pageSize=10"

echo "== 设备与视频 =="
check "设备列表" "/api/v1/iot/device/list?pageNum=1&pageSize=10"
check "设备状态统计" "/api/v1/iot/device/status?communityId=1"
check "设备类型分布" "/api/v1/iot/device/type-stat?communityId=1"
check "设备状态日志" "/api/v1/iot/device/log?deviceId=3"
check "设备类型列表" "/api/v1/iot/device/type/list"
check "视频通道列表" "/api/v1/video/channel/list?pageNum=1&pageSize=10"
check "视频通道树" "/api/v1/video/channel/tree?communityId=1"
check "实时流地址" "/api/v1/video/live/1"
check "回放地址" "/api/v1/video/playback/1?start=2026-09-10%2000:00:00&end=2026-09-10%2023:59:59"
check "录像计划" "/api/v1/video/record/list"

echo "== 门禁 / 访客 / 车辆 =="
check "门禁设备" "/api/v1/access/device/list?communityId=1"
check "门禁通行记录" "/api/v1/access/record/list?pageNum=1&pageSize=10"
check "门禁授权列表" "/api/v1/access/permission/list?communityId=1"
check "访客邀请列表" "/api/v1/visitor/invite/list?pageNum=1&pageSize=10"
check "访客到访记录" "/api/v1/visitor/record/list?pageNum=1&pageSize=10"
check "车辆通行记录" "/api/v1/vehicle/pass/list?pageNum=1&pageSize=10"
check "车辆每日统计" "/api/v1/vehicle/stat/daily?communityId=1&days=7"
check "道闸设备" "/api/v1/vehicle/gate/list?communityId=1"

echo "== 告警与工单 =="
check "告警列表" "/api/v1/alarm/event/list?pageNum=1&pageSize=10"
check "告警详情" "/api/v1/alarm/event/1"
check "告警统计" "/api/v1/alarm/stat/summary?communityId=1"
check "告警趋势" "/api/v1/alarm/stat/trend?communityId=1&days=7"
check "告警类型分布" "/api/v1/alarm/stat/type?communityId=1"
check "告警规则" "/api/v1/alarm/rule/list"
check "工单列表" "/api/v1/workorder/list?pageNum=1&pageSize=10"
check "工单详情" "/api/v1/workorder/1"
check "工单流转" "/api/v1/workorder/flow/1"
check "工单统计" "/api/v1/workorder/stat/summary?communityId=1"

echo "== 消息与统计 =="
check "公告列表" "/api/v1/msg/notice/list?pageNum=1&pageSize=10"
check "我的消息" "/api/v1/msg/my/list"
check "未读数" "/api/v1/msg/my/unread"
check "消息发送记录" "/api/v1/msg/log/list?pageNum=1&pageSize=5"
check "总览" "/api/v1/stat/overview?communityId=1"
check "告警趋势(统计)" "/api/v1/stat/alarm/trend?communityId=1&days=7"
check "通行趋势" "/api/v1/stat/access/trend?communityId=1&days=7"
check "车辆趋势" "/api/v1/stat/vehicle/trend?communityId=1&days=7"
check "工单统计(统计)" "/api/v1/stat/workorder/summary?communityId=1"
check "设备在线率" "/api/v1/stat/device/online-rate?communityId=1"
check "安全指数" "/api/v1/stat/safety-index?communityId=1"

echo "== 运营大屏 =="
check "大屏实时数据" "/api/v1/screen/realtime?communityId=1"
check "最新告警" "/api/v1/screen/alarm/latest?communityId=1&limit=5"
check "设备状态" "/api/v1/screen/device/status?communityId=1"
check "视频轮巡" "/api/v1/screen/video/rotation?communityId=1&size=4"

echo "== 关键流程: 告警接入 -> 自动派单 -> 处置 =="
NEW_ALARM=$(curl -s -m 10 -X POST "$BASE/api/v1/alarm/event" -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"communityId":1,"alarmType":"intrusion","alarmLevel":1,"sourceType":2,"title":"冒烟测试-周界入侵","content":"自动化冒烟测试产生的告警","location":"西门围墙","deviceId":2}')
echo "$NEW_ALARM" | head -c 200; echo
ALARM_ID=$(echo "$NEW_ALARM" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d.get('data',{}).get('id',''))" 2>/dev/null)
if [ -n "$ALARM_ID" ]; then
  check "处置告警" "/api/v1/alarm/event/handle" PUT "{\"alarmId\":$ALARM_ID,\"handleType\":3,\"handleResult\":\"冒烟测试处置\",\"finish\":true}"
  pass=$((pass+1))
else
  fail=$((fail+1)); echo "  FAIL 告警接入"
fi

echo
echo "================ 结果: 通过 $pass, 失败 $fail ================"
[ "$fail" -eq 0 ]
