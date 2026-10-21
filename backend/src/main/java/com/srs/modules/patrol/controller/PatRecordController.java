package com.srs.modules.patrol.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.patrol.entity.PatRecord;
import com.srs.modules.patrol.service.PatRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "巡更记录")
@RestController
@RequestMapping("/api/v1/patrol/record")
@RequiredArgsConstructor
public class PatRecordController {

    private final PatRecordService recordService;

    @Operation(summary = "巡更记录列表")
    @PreAuthorize("@ss.has('patrol:record:list')")
    @GetMapping("/list")
    public R<PageResult<PatRecord>> list(PageQuery query,
                                         @RequestParam(required = false) Long communityId,
                                         @RequestParam(required = false) Long planId,
                                         @RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) Integer status) {
        return R.data(PageResult.of(recordService.page(query, communityId, planId, userId, status)));
    }

    @Operation(summary = "巡更打卡")
    @PreAuthorize("@ss.has('patrol:record:add')")
    @OperLog(module = "巡更记录", businessType = 1)
    @PostMapping("/check-in")
    public R<PatRecord> checkIn(@RequestBody PatRecord record) {
        return R.data("打卡成功", recordService.checkIn(record));
    }

    @Operation(summary = "巡更统计")
    @PreAuthorize("@ss.has('patrol:record:list')")
    @GetMapping("/stat")
    public R<Map<String, Object>> stat(@RequestParam(required = false) Long communityId,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.data(recordService.stat(communityId, date));
    }

    @Operation(summary = "手动执行漏巡判定(运维入口)")
    @PreAuthorize("@ss.has('patrol:record:add')")
    @OperLog(module = "巡更记录", businessType = 1)
    @PostMapping("/mark-missing")
    public R<Integer> markMissing(@RequestParam
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.data(recordService.markMissing(date == null ? LocalDate.now().minusDays(1) : date));
    }
}
