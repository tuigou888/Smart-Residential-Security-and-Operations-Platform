package com.srs.modules.iot.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.iot.entity.IotDeviceStatusLog;
import com.srs.modules.iot.entity.IotDeviceType;
import com.srs.modules.iot.mapper.IotDeviceTypeMapper;
import com.srs.modules.iot.service.IotDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "设备管理")
@RestController
@RequestMapping("/api/v1/iot")
@RequiredArgsConstructor
public class IotDeviceController {

    private final IotDeviceService deviceService;
    private final IotDeviceTypeMapper deviceTypeMapper;

    @Operation(summary = "设备列表")
    @PreAuthorize("@ss.has('iot:device:list')")
    @GetMapping("/device/list")
    public R<PageResult<IotDevice>> list(PageQuery query,
                                         @RequestParam(required = false) Long communityId,
                                         @RequestParam(required = false) String deviceType,
                                         @RequestParam(required = false) String deviceName,
                                         @RequestParam(required = false) Integer onlineStatus,
                                         @RequestParam(required = false) Integer runStatus) {
        return R.data(PageResult.of(
                deviceService.pageDevices(query, communityId, deviceType, deviceName, onlineStatus, runStatus)));
    }

    @Operation(summary = "设备详情")
    @PreAuthorize("@ss.has('iot:device:query')")
    @GetMapping("/device/{id}")
    public R<IotDevice> detail(@PathVariable Long id) {
        return R.data(deviceService.getById(id));
    }

    @Operation(summary = "新增/修改设备")
    @PreAuthorize("@ss.has('iot:device:add')")
    @OperLog(module = "设备管理", businessType = 1)
    @PostMapping("/device")
    public R<Void> save(@RequestBody IotDevice entity) {
        deviceService.saveDevice(entity);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除设备")
    @PreAuthorize("@ss.has('iot:device:remove')")
    @OperLog(module = "设备管理", businessType = 3)
    @DeleteMapping("/device/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        deviceService.removeDevices(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "设备状态统计")
    @PreAuthorize("@ss.has('iot:device:list')")
    @GetMapping("/device/status")
    public R<Map<String, Object>> status(@RequestParam(required = false) Long communityId) {
        return R.data(deviceService.statusStat(communityId));
    }

    @Operation(summary = "设备类型分布")
    @PreAuthorize("@ss.has('iot:device:list')")
    @GetMapping("/device/type-stat")
    public R<List<Map<String, Object>>> typeStat(@RequestParam(required = false) Long communityId) {
        return R.data(deviceService.typeStat(communityId));
    }

    @Operation(summary = "设备在线率")
    @PreAuthorize("@ss.has('iot:device:list')")
    @GetMapping("/device/online-rate")
    public R<Map<String, Object>> onlineRate(@RequestParam(required = false) Long communityId) {
        return R.data(deviceService.statusStat(communityId));
    }

    @Operation(summary = "远程重启设备")
    @PreAuthorize("@ss.has('iot:device:control')")
    @OperLog(module = "设备管理", businessType = 2)
    @PostMapping("/device/restart")
    public R<Void> restart(@RequestBody Map<String, Object> body) {
        deviceService.restart(Long.valueOf(String.valueOf(body.get("id"))));
        return R.ok("重启指令已下发");
    }

    @Operation(summary = "设备状态上报(接入层/联调入口)")
    @PreAuthorize("@ss.has('iot:device:control')")
    @PostMapping("/device/report")
    public R<Void> report(@RequestBody Map<String, Object> body) {
        deviceService.reportStatus(
                String.valueOf(body.get("deviceCode")),
                body.get("onlineStatus") == null ? null : Integer.valueOf(String.valueOf(body.get("onlineStatus"))),
                body.get("runStatus") == null ? null : Integer.valueOf(String.valueOf(body.get("runStatus"))),
                (String) body.get("extra"));
        return R.ok("状态已上报");
    }

    @Operation(summary = "设备状态日志")
    @PreAuthorize("@ss.has('iot:device:list')")
    @GetMapping("/device/log")
    public R<List<IotDeviceStatusLog>> logs(@RequestParam(required = false) Long deviceId,
                                            @RequestParam(required = false) Integer eventType) {
        return R.data(deviceService.statusLogs(deviceId, eventType));
    }

    @Operation(summary = "设备类型列表")
    @PreAuthorize("@ss.has('iot:deviceType:list')")
    @GetMapping("/device/type/list")
    public R<List<IotDeviceType>> types() {
        return R.data(deviceTypeMapper.selectList(null));
    }
}
