package com.srs.modules.base.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.base.entity.BaseVehicle;
import com.srs.modules.base.service.BaseVehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "车辆档案")
@RestController
@RequestMapping("/api/v1/base/vehicle")
@RequiredArgsConstructor
public class BaseVehicleController {

    private final BaseVehicleService vehicleService;

    @Operation(summary = "车辆列表")
    @PreAuthorize("@ss.has('base:vehicle:list')")
    @GetMapping("/list")
    public R<PageResult<BaseVehicle>> list(PageQuery query,
                                           @RequestParam(required = false) Long communityId,
                                           @RequestParam(required = false) String plateNo,
                                           @RequestParam(required = false) Integer vehicleType,
                                           @RequestParam(required = false) Integer status) {
        return R.data(PageResult.of(vehicleService.pageVehicles(query, communityId, plateNo, vehicleType, status)));
    }

    @Operation(summary = "车辆详情")
    @PreAuthorize("@ss.has('base:vehicle:list')")
    @GetMapping("/{id}")
    public R<BaseVehicle> detail(@PathVariable Long id) {
        return R.data(vehicleService.getById(id));
    }

    @Operation(summary = "新增/修改车辆")
    @PreAuthorize("@ss.has('base:vehicle:add')")
    @OperLog(module = "车辆档案", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody BaseVehicle entity) {
        vehicleService.saveVehicle(entity);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除车辆")
    @PreAuthorize("@ss.has('base:vehicle:remove')")
    @OperLog(module = "车辆档案", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        vehicleService.remove(ids);
        return R.ok("删除成功");
    }
}
