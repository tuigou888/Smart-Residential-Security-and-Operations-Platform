package com.srs.modules.sec.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.sec.entity.SecAlarmEvent;
import com.srs.modules.sec.entity.SecAlarmRule;
import com.srs.modules.sec.service.AlarmService;
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

@Tag(name = "告警中心")
@RestController
@RequestMapping("/api/v1/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "告警列表")
    @PreAuthorize("@ss.has('alarm:event:list')")
    @GetMapping("/event/list")
    public R<PageResult<SecAlarmEvent>> list(PageQuery query,
                                             @RequestParam(required = false) Long communityId,
                                             @RequestParam(required = false) String alarmType,
                                             @RequestParam(required = false) Integer alarmLevel,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) Integer sourceType,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime begin,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return R.data(PageResult.of(
                alarmService.pageAlarms(query, communityId, alarmType, alarmLevel, status, sourceType, begin, end)));
    }

    @Operation(summary = "告警详情(含处置记录)")
    @PreAuthorize("@ss.has('alarm:event:query')")
    @GetMapping("/event/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.data(alarmService.detail(id));
    }

    @Operation(summary = "告警接入(设备/AI/人工上报)")
    @PreAuthorize("@ss.has('alarm:event:add')")
    @OperLog(module = "告警中心", businessType = 1)
    @PostMapping("/event")
    public R<SecAlarmEvent> receive(@RequestBody SecAlarmEvent event) {
        return R.data(alarmService.receive(event));
    }

    @Operation(summary = "处理告警")
    @PreAuthorize("@ss.has('alarm:event:handle')")
    @OperLog(module = "告警中心", businessType = 2)
    @PutMapping("/event/handle")
    public R<Void> handle(@RequestBody HandleBody body) {
        alarmService.handle(body.getAlarmId(), body.getHandleType(), body.getHandleResult(),
                body.getAttachments(), body.isFinish());
        return R.ok("处置已提交");
    }

    @Operation(summary = "标记误报")
    @PreAuthorize("@ss.has('alarm:event:handle')")
    @OperLog(module = "告警中心", businessType = 2)
    @PutMapping("/event/false")
    public R<Void> markFalse(@RequestBody HandleBody body) {
        alarmService.markFalse(body.getAlarmId(), body.getReason());
        return R.ok("已标记误报");
    }

    @Operation(summary = "告警统计")
    @PreAuthorize("@ss.has('alarm:stat:view')")
    @GetMapping("/stat/summary")
    public R<Map<String, Object>> summary(@RequestParam(required = false) Long communityId) {
        return R.data(alarmService.summary(communityId));
    }

    @Operation(summary = "告警趋势")
    @PreAuthorize("@ss.has('alarm:stat:view')")
    @GetMapping("/stat/trend")
    public R<Map<String, Object>> trend(@RequestParam(required = false) Long communityId,
                                        @RequestParam(defaultValue = "7") int days) {
        return R.data(alarmService.trend(communityId, days));
    }

    @Operation(summary = "告警类型分布")
    @PreAuthorize("@ss.has('alarm:stat:view')")
    @GetMapping("/stat/type")
    public R<List<Map<String, Object>>> typeStat(@RequestParam(required = false) Long communityId) {
        return R.data(alarmService.typeDistribution(communityId));
    }

    @Operation(summary = "告警规则列表")
    @PreAuthorize("@ss.has('alarm:rule:add')")
    @GetMapping("/rule/list")
    public R<List<SecAlarmRule>> rules(@RequestParam(required = false) Long communityId) {
        return R.data(alarmService.rules(communityId));
    }

    @Operation(summary = "新增/修改告警规则")
    @PreAuthorize("@ss.has('alarm:rule:add')")
    @OperLog(module = "告警中心", businessType = 1)
    @PostMapping("/rule")
    public R<Void> saveRule(@RequestBody SecAlarmRule rule) {
        alarmService.saveRule(rule);
        return R.ok("保存成功");
    }

    @Operation(summary = "修改告警规则")
    @PreAuthorize("@ss.has('alarm:rule:edit')")
    @PutMapping("/rule")
    public R<Void> updateRule(@RequestBody SecAlarmRule rule) {
        alarmService.saveRule(rule);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除告警规则")
    @PreAuthorize("@ss.has('alarm:rule:edit')")
    @DeleteMapping("/rule/{id}")
    public R<Void> removeRule(@PathVariable Long id) {
        alarmService.removeRule(id);
        return R.ok("删除成功");
    }

    @Data
    public static class HandleBody {
        private Long alarmId;
        private Integer handleType;
        private String handleResult;
        private String attachments;
        private boolean finish;
        private String reason;
    }
}
