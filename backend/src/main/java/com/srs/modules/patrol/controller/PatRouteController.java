package com.srs.modules.patrol.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.patrol.entity.PatRoute;
import com.srs.modules.patrol.service.PatRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "巡更路线")
@RestController
@RequestMapping("/api/v1/patrol/route")
@RequiredArgsConstructor
public class PatRouteController {

    private final PatRouteService routeService;

    @Operation(summary = "巡更路线列表")
    @PreAuthorize("@ss.has('patrol:route:list')")
    @GetMapping("/list")
    public R<PageResult<PatRoute>> list(PageQuery query,
                                        @RequestParam(required = false) Long communityId,
                                        @RequestParam(required = false) String routeName) {
        return R.data(PageResult.of(routeService.page(query, communityId, routeName)));
    }

    @Operation(summary = "巡更路线下拉")
    @PreAuthorize("@ss.has('patrol:route:list')")
    @GetMapping("/options")
    public R<List<PatRoute>> options(@RequestParam(required = false) Long communityId) {
        return R.data(routeService.options(communityId));
    }

    @Operation(summary = "巡更路线详情(含点位顺序)")
    @PreAuthorize("@ss.has('patrol:route:list')")
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.data(routeService.detail(id));
    }

    @Operation(summary = "新增巡更路线")
    @PreAuthorize("@ss.has('patrol:route:add')")
    @OperLog(module = "巡更路线", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody RouteSaveRequest request) {
        if (request.route() == null) {
            return R.fail(400, "路线信息不能为空");
        }
        request.route().setId(null);
        routeService.saveRoute(request.route(), request.pointIds());
        return R.ok("保存成功");
    }

    @Operation(summary = "修改巡更路线")
    @PreAuthorize("@ss.has('patrol:route:edit')")
    @OperLog(module = "巡更路线", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody RouteSaveRequest request) {
        if (request.route() == null || request.route().getId() == null) {
            return R.fail(400, "路线ID不能为空");
        }
        routeService.saveRoute(request.route(), request.pointIds());
        return R.ok("保存成功");
    }

    @Operation(summary = "删除巡更路线")
    @PreAuthorize("@ss.has('patrol:route:remove')")
    @OperLog(module = "巡更路线", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        routeService.removeRoutes(ids);
        return R.ok("删除成功");
    }

    /** 路线保存请求: 路线信息 + 按顺序排列的点位ID */
    public record RouteSaveRequest(PatRoute route, List<Long> pointIds) {
    }
}
