#!/usr/bin/env bash
# 前端依赖接口冒烟：验证所有前端调用的后端接口存在且返回预期结构
BASE="http://127.0.0.1:8088/api/v1"
LOGIN=$(curl -s -m 8 -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}')
TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['accessToken'])" 2>/dev/null)
if [ -z "$TOKEN" ]; then echo "LOGIN FAILED: $LOGIN"; exit 1; fi
H="Authorization: Bearer $TOKEN"
PASS=0; FAIL=0
chk() {
  local name="$1"; local url="$2"
  local code=$(curl -s -m 8 -o /tmp/fe-body.json -w "%{http_code}" "$BASE$url" -H "$H")
  local biz=$(python3 -c "import json;print(json.load(open('/tmp/fe-body.json')).get('code'))" 2>/dev/null)
  if [ "$code" = "200" ] && [ "$biz" = "200" ]; then
    printf "  OK   %-40s\n" "$name"; PASS=$((PASS+1))
  else
    printf "  FAIL %-40s http=%s biz=%s body=%s\n" "$name" "$code" "$biz" "$(head -c 120 /tmp/fe-body.json)"; FAIL=$((FAIL+1))
  fi
}
echo "===== 认证/系统 ====="
chk "auth/info"            "/auth/info"
chk "auth/check"           "/auth/check"
chk "sys/user/list"        "/sys/user/list?pageNum=1&pageSize=10"
chk "sys/role/list"        "/sys/role/list?pageNum=1&pageSize=10"
chk "sys/menu/list"        "/sys/menu/list"
chk "sys/menu/tree"        "/sys/menu/tree"
chk "sys/dept/list"        "/sys/dept/list"
chk "sys/dict/type/list"   "/sys/dict/type/list?pageNum=1&pageSize=10"
chk "sys/config/list"      "/sys/config/list?pageNum=1&pageSize=10"
chk "sys/log/oper/list"    "/sys/log/oper/list?pageNum=1&pageSize=10"
chk "sys/log/login/list"   "/sys/log/login/list?pageNum=1&pageSize=10"
echo "===== 小区基础 ====="
chk "base/community/list"  "/base/community/list?pageNum=1&pageSize=10"
chk "base/community/options" "/base/community/options"
chk "base/building/list"   "/base/building/list?pageNum=1&pageSize=10&communityId=1"
chk "base/unit/list"       "/base/unit/list?pageNum=1&pageSize=10&communityId=1"
chk "base/room/list"       "/base/room/list?pageNum=1&pageSize=10&communityId=1"
chk "base/owner/list"      "/base/owner/list?pageNum=1&pageSize=10&communityId=1"
chk "base/vehicle/list"    "/base/vehicle/list?pageNum=1&pageSize=10&communityId=1"
echo "===== 设备/视频 ====="
chk "iot/device/list"      "/iot/device/list?pageNum=1&pageSize=10&communityId=1"
chk "iot/device/status"    "/iot/device/status?communityId=1"
chk "iot/device/type-stat" "/iot/device/type-stat?communityId=1"
chk "video/channel/list"   "/video/channel/list?pageNum=1&pageSize=10&communityId=1"
chk "video/channel/tree"   "/video/channel/tree?communityId=1"
chk "video/record/list"    "/video/record/list"
echo "===== 告警/工单 ====="
chk "alarm/event/list"     "/alarm/event/list?pageNum=1&pageSize=10&communityId=1"
chk "alarm/stat/summary"   "/alarm/stat/summary?communityId=1"
chk "alarm/stat/trend"     "/alarm/stat/trend?communityId=1&days=7"
chk "alarm/stat/type"      "/alarm/stat/type?communityId=1"
chk "alarm/rule/list"      "/alarm/rule/list?communityId=1"
chk "workorder/list"       "/workorder/list?pageNum=1&pageSize=10&communityId=1"
chk "workorder/stat/summary" "/workorder/stat/summary?communityId=1"
echo "===== 门禁/访客/车辆 ====="
chk "access/device/list"   "/access/device/list?communityId=1"
chk "access/record/list"   "/access/record/list?pageNum=1&pageSize=10&communityId=1"
chk "access/permission/list" "/access/permission/list?pageNum=1&pageSize=10&communityId=1"
chk "visitor/invite/list"  "/visitor/invite/list?pageNum=1&pageSize=10&communityId=1"
chk "visitor/record/list"  "/visitor/record/list?pageNum=1&pageSize=10&communityId=1"
chk "vehicle/pass/list"    "/vehicle/pass/list?pageNum=1&pageSize=10&communityId=1"
chk "vehicle/stat/daily"   "/vehicle/stat/daily?communityId=1&days=7"
chk "vehicle/gate/list"    "/vehicle/gate/list?communityId=1"
echo "===== 消息/统计/大屏 ====="
chk "msg/notice/list"      "/msg/notice/list?pageNum=1&pageSize=10&communityId=1"
chk "msg/my/list"          "/msg/my/list"
chk "stat/overview"        "/stat/overview?communityId=1"
chk "stat/alarm/trend"     "/stat/alarm/trend?communityId=1&days=7"
chk "stat/access/trend"    "/stat/access/trend?communityId=1&days=7"
chk "stat/vehicle/trend"   "/stat/vehicle/trend?communityId=1&days=7"
chk "stat/workorder/summary" "/stat/workorder/summary?communityId=1"
chk "stat/device/online-rate" "/stat/device/online-rate?communityId=1"
chk "stat/safety-index"    "/stat/safety-index?communityId=1"
chk "screen/realtime"      "/screen/realtime?communityId=1"
chk "screen/alarm/latest"  "/screen/alarm/latest?communityId=1&limit=8"
chk "screen/device/status" "/screen/device/status?communityId=1"
chk "screen/video/rotation" "/screen/video/rotation?communityId=1&size=4"

echo
echo "===== 菜单树完整性 ====="
curl -s -m 8 "$BASE/auth/info" -H "$H" | python3 -c "
import sys,json
def walk(ns,acc):
    for n in ns:
        if n.get('component'): acc.append(n['component'])
        if n.get('children'): walk(n['children'],acc)
    return acc
d=json.load(sys.stdin)['data']
c=walk(d['menus'],[])
print('带组件叶子菜单数:',len(c))
missing=[x for x in ['alarm/list/index','workorder/list/index','iot/device/index','video/live/index','stat/overview/index','screen/index','system/user/index','base/community/index'] if x not in c]
print('关键组件缺失:', missing if missing else '无')
"
echo
echo "PASS=$PASS FAIL=$FAIL"
