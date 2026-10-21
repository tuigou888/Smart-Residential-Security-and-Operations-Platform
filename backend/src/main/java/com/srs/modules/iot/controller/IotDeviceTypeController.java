package com.srs.modules.iot.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.iot.entity.IotDeviceType;
import com.srs.modules.iot.service.IotDeviceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备类型 CRUD
 */
@Tag(name = "设备类型")
@RestController
@RequestMapping("/api/v1/iot/device-type")
@RequiredArgsConstructor
public class IotDeviceTypeController {

    private final IotDeviceTypeService deviceTypeService;

    @Operation(summary = "设备类型列表")
    @PreAuthorize("@ss.has('iot:deviceType:list')")
    @GetMapping("/list")
    public R<PageResult<IotDeviceType>> list(PageQuery query,
                                             @RequestParam(required = false) Long communityId,
                                             @RequestParam(required = false) String typeName) {
        return R.data(PageResult.of(deviceTypeService.page(query, communityId, typeName)));
    }

    @Operation(summary = "设备类型下拉")
    @PreAuthorize("@ss.has('iot:deviceType:list')")
    @GetMapping("/options")
    public R<List<IotDeviceType>> options() {
        return R.data(deviceTypeService.options());
    }

    @Operation(summary = "新增设备类型")
    @PreAuthorize("@ss.has('iot:deviceType:add')")
    @OperLog(module = "设备类型", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody IotDeviceType entity) {
        entity.setId(null);
        deviceTypeService.saveType(entity);
        return R.ok("保存成功");
    }

    @Operation(summary = "修改设备类型")
    @PreAuthorize("@ss.has('iot:deviceType:edit')")
    @OperLog(module = "设备类型", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody IotDeviceType entity) {
        if (entity.getId() == null) {
            return R.fail(400, "类型ID不能为空");
        }
        deviceTypeService.saveType(entity);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除设备类型")
    @PreAuthorize("@ss.has('iot:deviceType:remove')")
    @OperLog(module = "设备类型", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        deviceTypeService.removeTypes(ids);
        return R.ok("删除成功");
    }
}
