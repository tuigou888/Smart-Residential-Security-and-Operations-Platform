package com.srs.modules.stat.controller;

import com.srs.common.core.R;
import com.srs.modules.stat.service.StatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/api/v1/stat")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @Operation(summary = "综合总览")
    @PreAuthorize("@ss.has('stat:overview:view')")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview(@RequestParam(required = false) Long communityId) {
        return R.data(statService.overview(communityId));
    }

    @Operation(summary = "告警趋势")
    @PreAuthorize("@ss.has('stat:alarm:view')")
    @GetMapping("/alarm/trend")
    public R<Map<String, Object>> alarmTrend(@RequestParam(required = false) Long communityId,
                                             @RequestParam(defaultValue = "7") int days) {
        return R.data(statService.alarmTrend(communityId, days));
    }

    @Operation(summary = "通行趋势")
    @PreAuthorize("@ss.has('stat:access:view')")
    @GetMapping("/access/trend")
    public R<Map<String, Object>> accessTrend(@RequestParam(required = false) Long communityId,
                                              @RequestParam(defaultValue = "7") int days) {
        return R.data(statService.accessTrend(communityId, days));
    }

    @Operation(summary = "车辆趋势")
    @PreAuthorize("@ss.has('stat:vehicle:view')")
    @GetMapping("/vehicle/trend")
    public R<Map<String, Object>> vehicleTrend(@RequestParam(required = false) Long communityId,
                                               @RequestParam(defaultValue = "7") int days) {
        return R.data(statService.vehicleTrend(communityId, days));
    }

    @Operation(summary = "工单统计")
    @PreAuthorize("@ss.has('stat:workorder:view')")
    @GetMapping("/workorder/summary")
    public R<Map<String, Object>> workOrderSummary(@RequestParam(required = false) Long communityId) {
        return R.data(statService.workOrderSummary(communityId));
    }

    @Operation(summary = "设备在线率与分布")
    @PreAuthorize("@ss.has('stat:device:view')")
    @GetMapping("/device/online-rate")
    public R<Map<String, Object>> deviceStat(@RequestParam(required = false) Long communityId) {
        return R.data(statService.deviceStat(communityId));
    }

    @Operation(summary = "小区安全指数")
    @PreAuthorize("@ss.has('stat:safety:view')")
    @GetMapping("/safety-index")
    public R<Map<String, Object>> safetyIndex(@RequestParam(required = false) Long communityId) {
        return R.data(statService.safetyIndex(communityId));
    }
}
