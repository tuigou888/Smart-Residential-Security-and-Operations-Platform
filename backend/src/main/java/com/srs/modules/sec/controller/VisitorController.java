package com.srs.modules.sec.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.sec.entity.SecVisitorInvite;
import com.srs.modules.sec.service.VisitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "访客管理")
@RestController
@RequestMapping("/api/v1/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @Operation(summary = "访客邀请列表")
    @PreAuthorize("@ss.has('visitor:invite:list')")
    @GetMapping("/invite/list")
    public R<PageResult<SecVisitorInvite>> list(PageQuery query,
                                                @RequestParam(required = false) Long communityId,
                                                @RequestParam(required = false) String visitorName,
                                                @RequestParam(required = false) Long inviterId,
                                                @RequestParam(required = false) Integer status) {
        return R.data(PageResult.of(
                visitorService.pageInvites(query, communityId, visitorName, inviterId, status)));
    }

    @Operation(summary = "创建访客邀请(生成二维码)")
    @PreAuthorize("@ss.has('visitor:invite:add')")
    @OperLog(module = "访客管理", businessType = 1)
    @PostMapping("/invite")
    public R<SecVisitorInvite> create(@RequestBody SecVisitorInvite invite) {
        return R.data(visitorService.create(invite));
    }

    @Operation(summary = "修改访客邀请")
    @PreAuthorize("@ss.has('visitor:invite:edit')")
    @OperLog(module = "访客管理", businessType = 2)
    @PutMapping("/invite/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SecVisitorInvite invite) {
        invite.setId(id);
        visitorService.modify(invite);
        return R.ok("修改成功");
    }

    @Operation(summary = "撤销访客邀请")
    @PreAuthorize("@ss.has('visitor:invite:remove')")
    @OperLog(module = "访客管理", businessType = 3)
    @DeleteMapping("/invite/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        visitorService.cancel(id);
        return R.ok("已撤销");
    }

    @Operation(summary = "访客到访记录(按状态查询)")
    @PreAuthorize("@ss.has('visitor:record:list')")
    @GetMapping("/record/list")
    public R<PageResult<SecVisitorInvite>> records(PageQuery query,
                                                   @RequestParam(required = false) Long communityId,
                                                   @RequestParam(required = false) String visitorName,
                                                   @RequestParam(required = false) Integer status) {
        return R.data(PageResult.of(
                visitorService.pageInvites(query, communityId, visitorName, null, status)));
    }

    @Operation(summary = "访客扫码签到")
    @PreAuthorize("@ss.has('visitor:checkin')")
    @OperLog(module = "访客管理", businessType = 2)
    @PostMapping("/check-in")
    public R<SecVisitorInvite> checkIn(@RequestBody CheckInBody body) {
        return R.data(visitorService.checkIn(body.getQrCode()));
    }

    @Operation(summary = "批量置过期邀请")
    @PreAuthorize("@ss.has('visitor:invite:edit')")
    @PostMapping("/expire")
    public R<Map<String, Object>> expire() {
        return R.data(Map.of("expired", visitorService.expireOverdue()));
    }

    @Data
    public static class CheckInBody {
        private String qrCode;
    }
}
