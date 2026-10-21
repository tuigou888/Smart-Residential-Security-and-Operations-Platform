package com.srs.modules.stat.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.stat.entity.StatDailySummary;
import com.srs.modules.stat.service.DailySummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "日统计汇总")
@RestController
@RequestMapping("/api/v1/stat/daily")
@RequiredArgsConstructor
public class DailySummaryController {

    private final DailySummaryService summaryService;

    @Operation(summary = "日汇总列表")
    @PreAuthorize("@ss.has('stat:daily:view')")
    @GetMapping("/list")
    public R<PageResult<StatDailySummary>> list(PageQuery query,
                                                @RequestParam(required = false) Long communityId,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return R.data(PageResult.of(summaryService.page(query, communityId, begin, end)));
    }

    @Operation(summary = "生成日汇总")
    @PreAuthorize("@ss.has('stat:daily:generate')")
    @OperLog(module = "日统计汇总", businessType = 1)
    @PostMapping("/generate")
    public R<StatDailySummary> generate(@RequestBody Map<String, Object> body) {
        Long communityId = Long.valueOf(String.valueOf(body.get("communityId")));
        LocalDate date = body.get("date") == null ? LocalDate.now().minusDays(1)
                : LocalDate.parse(String.valueOf(body.get("date")));
        return R.data("生成成功", summaryService.generate(communityId, date));
    }
}
