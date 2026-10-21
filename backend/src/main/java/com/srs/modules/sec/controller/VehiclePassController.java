package com.srs.modules.sec.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.sec.entity.SecVehiclePass;
import com.srs.modules.sec.service.VehiclePassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "车辆通行")
@RestController
@RequestMapping("/api/v1/vehicle")
@RequiredArgsConstructor
public class VehiclePassController {

    private final VehiclePassService vehiclePassService;

    @Operation(summary = "车辆通行记录")
    @PreAuthorize("@ss.has('vehicle:pass:list')")
    @GetMapping("/pass/list")
    public R<PageResult<SecVehiclePass>> list(PageQuery query,
                                              @RequestParam(required = false) Long communityId,
                                              @RequestParam(required = false) String plateNo,
                                              @RequestParam(required = false) Integer direction,
                                              @RequestParam(required = false) Integer vehicleType,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime begin,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return R.data(PageResult.of(vehiclePassService.pagePass(query, communityId, plateNo, direction,
                vehicleType, begin, end)));
    }

    @Operation(summary = "车辆每日统计")
    @PreAuthorize("@ss.has('vehicle:stat:view')")
    @GetMapping("/stat/daily")
    public R<Map<String, Object>> daily(@RequestParam(required = false) Long communityId,
                                        @RequestParam(defaultValue = "7") int days) {
        return R.data(vehiclePassService.dailyStat(communityId, days));
    }

    @Operation(summary = "手动抬杆")
    @PreAuthorize("@ss.has('vehicle:gate:open')")
    @OperLog(module = "车辆通行", businessType = 2)
    @PostMapping("/gate/open")
    public R<Void> openGate(@RequestBody GateBody body) {
        vehiclePassService.openGate(body.getGateDeviceId(), body.getPlateNo(), body.getReason());
        return R.ok("抬杆指令已下发");
    }

    @Operation(summary = "车牌入场识别(白名单自动放行)")
    @PreAuthorize("@ss.has('vehicle:pass:list')")
    @PostMapping("/enter")
    public R<Map<String, Object>> enter(@RequestBody GateBody body) {
        return R.data(vehiclePassService.enter(body.getGateDeviceId(), body.getPlateNo()));
    }

    @Operation(summary = "道闸设备列表")
    @PreAuthorize("@ss.has('vehicle:pass:list')")
    @GetMapping("/gate/list")
    public R<List<IotDevice>> gates(@RequestParam(required = false) Long communityId) {
        return R.data(vehiclePassService.gates(communityId));
    }

    @Data
    public static class GateBody {
        private Long gateDeviceId;
        private String plateNo;
        private String reason;
    }
}
