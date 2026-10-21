package com.srs.modules.iot.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.iot.entity.IotDeviceEvent;
import com.srs.modules.iot.service.IotDeviceEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "设备事件")
@RestController
@RequestMapping("/api/v1/iot/device-event")
@RequiredArgsConstructor
public class IotDeviceEventController {

    private final IotDeviceEventService eventService;

    @Operation(summary = "设备事件列表")
    @PreAuthorize("@ss.has('iot:deviceEvent:list')")
    @GetMapping("/list")
    public R<PageResult<IotDeviceEvent>> list(PageQuery query,
                                              @RequestParam(required = false) Long communityId,
                                              @RequestParam(required = false) Long deviceId,
                                              @RequestParam(required = false) String eventCode,
                                              @RequestParam(required = false) Integer handleStatus) {
        return R.data(PageResult.of(eventService.page(query, communityId, deviceId, eventCode, handleStatus)));
    }

    @Operation(summary = "设备事件上报(接入层入口)")
    @PreAuthorize("@ss.has('iot:deviceEvent:control')")
    @PostMapping("/report")
    public R<IotDeviceEvent> report(@RequestBody IotDeviceEvent event) {
        event.setId(null);
        return R.data("事件已上报", eventService.report(event));
    }

    @Operation(summary = "处置设备事件")
    @PreAuthorize("@ss.has('iot:deviceEvent:control')")
    @OperLog(module = "设备事件", businessType = 2)
    @PutMapping("/handle/{id}")
    public R<Void> handle(@PathVariable Long id) {
        eventService.handle(id);
        return R.ok("处置成功");
    }
}
