package com.srs.modules.base.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.base.entity.BaseBuilding;
import com.srs.modules.base.entity.BaseUnit;
import com.srs.modules.base.service.BaseStructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "楼栋/单元管理")
@RestController
@RequiredArgsConstructor
public class BaseStructureController {

    private final BaseStructureService structureService;

    @Operation(summary = "楼栋列表")
    @PreAuthorize("@ss.has('base:building:list')")
    @GetMapping("/api/v1/base/building/list")
    public R<PageResult<BaseBuilding>> buildingList(PageQuery query,
                                                    @RequestParam(required = false) Long communityId,
                                                    @RequestParam(required = false) String buildingName) {
        return R.data(PageResult.of(structureService.pageBuildings(query, communityId, buildingName)));
    }

    @Operation(summary = "楼栋下拉")
    @PreAuthorize("@ss.has('base:building:list')")
    @GetMapping("/api/v1/base/building/options")
    public R<List<BaseBuilding>> buildingOptions(@RequestParam(required = false) Long communityId) {
        return R.data(structureService.buildingsOf(communityId));
    }

    @Operation(summary = "新增/修改楼栋")
    @PreAuthorize("@ss.has('base:building:add')")
    @OperLog(module = "楼栋管理", businessType = 1)
    @PostMapping("/api/v1/base/building")
    public R<Void> saveBuilding(@RequestBody BaseBuilding entity) {
        structureService.saveBuilding(entity);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除楼栋")
    @PreAuthorize("@ss.has('base:building:remove')")
    @OperLog(module = "楼栋管理", businessType = 3)
    @DeleteMapping("/api/v1/base/building/{ids}")
    public R<Void> removeBuildings(@PathVariable List<Long> ids) {
        structureService.removeBuildings(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "单元列表")
    @PreAuthorize("@ss.has('base:unit:list')")
    @GetMapping("/api/v1/base/unit/list")
    public R<PageResult<BaseUnit>> unitList(PageQuery query,
                                            @RequestParam(required = false) Long communityId,
                                            @RequestParam(required = false) Long buildingId,
                                            @RequestParam(required = false) String unitName) {
        return R.data(PageResult.of(structureService.pageUnits(query, communityId, buildingId, unitName)));
    }

    @Operation(summary = "单元下拉")
    @PreAuthorize("@ss.has('base:unit:list')")
    @GetMapping("/api/v1/base/unit/options")
    public R<List<BaseUnit>> unitOptions(@RequestParam(required = false) Long buildingId) {
        return R.data(structureService.unitsOf(buildingId));
    }

    @Operation(summary = "新增/修改单元")
    @PreAuthorize("@ss.has('base:unit:add')")
    @OperLog(module = "单元管理", businessType = 1)
    @PostMapping("/api/v1/base/unit")
    public R<Void> saveUnit(@RequestBody BaseUnit entity) {
        structureService.saveUnit(entity);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除单元")
    @PreAuthorize("@ss.has('base:unit:remove')")
    @OperLog(module = "单元管理", businessType = 3)
    @DeleteMapping("/api/v1/base/unit/{ids}")
    public R<Void> removeUnits(@PathVariable List<Long> ids) {
        structureService.removeUnits(ids);
        return R.ok("删除成功");
    }
}
