package com.srs.modules.patrol.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.patrol.entity.PatPoint;
import com.srs.modules.patrol.service.PatPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "巡更点位")
@RestController
@RequestMapping("/api/v1/patrol/point")
@RequiredArgsConstructor
public class PatPointController {

    private final PatPointService pointService;

    @Operation(summary = "巡更点位列表")
    @PreAuthorize("@ss.has('patrol:point:list')")
    @GetMapping("/list")
    public R<PageResult<PatPoint>> list(PageQuery query,
                                        @RequestParam(required = false) Long communityId,
                                        @RequestParam(required = false) String pointName) {
        return R.data(PageResult.of(pointService.page(query, communityId, pointName)));
    }

    @Operation(summary = "巡更点位下拉")
    @PreAuthorize("@ss.has('patrol:point:list')")
    @GetMapping("/options")
    public R<List<PatPoint>> options(@RequestParam(required = false) Long communityId) {
        return R.data(pointService.options(communityId));
    }

    @Operation(summary = "新增巡更点位")
    @PreAuthorize("@ss.has('patrol:point:add')")
    @OperLog(module = "巡更点位", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody PatPoint point) {
        point.setId(null);
        pointService.savePoint(point);
        return R.ok("保存成功");
    }

    @Operation(summary = "修改巡更点位")
    @PreAuthorize("@ss.has('patrol:point:edit')")
    @OperLog(module = "巡更点位", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody PatPoint point) {
        if (point.getId() == null) {
            return R.fail(400, "点位ID不能为空");
        }
        pointService.savePoint(point);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除巡更点位")
    @PreAuthorize("@ss.has('patrol:point:remove')")
    @OperLog(module = "巡更点位", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        pointService.removePoints(ids);
        return R.ok("删除成功");
    }
}
