package com.srs.modules.sec.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.sec.entity.SecAccessPermission;
import com.srs.modules.sec.entity.SecAccessRecord;
import com.srs.modules.sec.service.AccessService;
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

@Tag(name = "门禁管理")
@RestController
@RequestMapping("/api/v1/access")
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;

    @Operation(summary = "门禁设备列表")
    @PreAuthorize("@ss.has('access:device:list')")
    @GetMapping("/device/list")
    public R<List<IotDevice>> devices(@RequestParam(required = false) Long communityId) {
        return R.data(accessService.accessDevices(communityId));
    }

    @Operation(summary = "通行记录")
    @PreAuthorize("@ss.has('access:record:list')")
    @GetMapping("/record/list")
    public R<PageResult<SecAccessRecord>> records(PageQuery query,
                                                  @RequestParam(required = false) Long communityId,
                                                  @RequestParam(required = false) Long deviceId,
                                                  @RequestParam(required = false) Long ownerId,
                                                  @RequestParam(required = false) String personName,
                                                  @RequestParam(required = false) Integer openMethod,
                                                  @RequestParam(required = false) Integer passResult,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                  LocalDateTime begin,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                  LocalDateTime end) {
        return R.data(PageResult.of(accessService.pageRecords(query, communityId, deviceId, ownerId,
                personName, openMethod, passResult, begin, end)));
    }

    @Operation(summary = "远程开门")
    @PreAuthorize("@ss.has('access:remote:open')")
    @OperLog(module = "门禁管理", businessType = 2)
    @PostMapping("/remote-open")
    public R<Void> remoteOpen(@RequestBody OpenBody body) {
        accessService.remoteOpen(body.getDeviceId(), body.getCommunityId(), body.getReason(),
                body.getTargetType(), body.getTargetId());
        return R.ok("开门指令已下发");
    }

    @Operation(summary = "门禁授权列表")
    @PreAuthorize("@ss.has('access:permission:list')")
    @GetMapping("/permission/list")
    public R<List<SecAccessPermission>> permissions(@RequestParam(required = false) Long deviceId,
                                                    @RequestParam(required = false) Long ownerId,
                                                    @RequestParam(required = false) Long communityId) {
        return R.data(accessService.permissions(deviceId, ownerId, communityId));
    }

    @Operation(summary = "门禁授权")
    @PreAuthorize("@ss.has('access:permission:grant')")
    @OperLog(module = "门禁管理", businessType = 1)
    @PostMapping("/permission/grant")
    public R<Void> grant(@RequestBody SecAccessPermission permission) {
        accessService.grant(permission);
        return R.ok("授权成功");
    }

    @Operation(summary = "撤销授权")
    @PreAuthorize("@ss.has('access:permission:revoke')")
    @OperLog(module = "门禁管理", businessType = 3)
    @PostMapping("/permission/revoke")
    public R<Void> revoke(@RequestBody Map<String, Object> body) {
        accessService.revoke(Long.valueOf(String.valueOf(body.get("deviceId"))),
                Long.valueOf(String.valueOf(body.get("ownerId"))));
        return R.ok("已撤销授权");
    }

    @Operation(summary = "通行记录上报(门禁设备接入入口)")
    @PreAuthorize("@ss.has('access:device:list')")
    @PostMapping("/record")
    public R<Void> record(@RequestBody SecAccessRecord record) {
        accessService.record(record);
        return R.ok("已记录");
    }

    @Data
    public static class OpenBody {
        private Long deviceId;
        private Long communityId;
        private String reason;
        /** 黑名单校验目标类型: 1人员 2车辆 */
        private Integer targetType;
        /** 身份证/车牌号, 为空则不校验黑名单 */
        private String targetId;
    }
}
