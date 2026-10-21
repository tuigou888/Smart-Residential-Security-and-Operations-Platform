package com.srs.modules.patrol.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.patrol.entity.PatPlan;
import com.srs.modules.patrol.service.PatPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "巡更计划")
@RestController
@RequestMapping("/api/v1/patrol/plan")
@RequiredArgsConstructor
public class PatPlanController {

    private final PatPlanService planService;

    @Operation(summary = "巡更计划列表")
    @PreAuthorize("@ss.has('patrol:plan:list')")
    @GetMapping("/list")
    public R<PageResult<PatPlan>> list(PageQuery query,
                                       @RequestParam(required = false) Long communityId,
                                       @RequestParam(required = false) String planName) {
        return R.data(PageResult.of(planService.page(query, communityId, planName)));
    }

    @Operation(summary = "新增巡更计划")
    @PreAuthorize("@ss.has('patrol:plan:add')")
    @OperLog(module = "巡更计划", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody PatPlan plan) {
        plan.setId(null);
        planService.savePlan(plan);
        return R.ok("保存成功");
    }

    @Operation(summary = "修改巡更计划")
    @PreAuthorize("@ss.has('patrol:plan:edit')")
    @OperLog(module = "巡更计划", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody PatPlan plan) {
        if (plan.getId() == null) {
            return R.fail(400, "计划ID不能为空");
        }
        planService.savePlan(plan);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除巡更计划")
    @PreAuthorize("@ss.has('patrol:plan:remove')")
    @OperLog(module = "巡更计划", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        planService.removePlans(ids);
        return R.ok("删除成功");
    }
}
