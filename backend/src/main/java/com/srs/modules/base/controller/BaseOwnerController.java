package com.srs.modules.base.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.base.entity.BaseOwner;
import com.srs.modules.base.service.BaseOwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "业主管理")
@RestController
@RequestMapping("/api/v1/base/owner")
@RequiredArgsConstructor
public class BaseOwnerController {

    private final BaseOwnerService ownerService;

    @Operation(summary = "业主列表")
    @PreAuthorize("@ss.has('base:owner:list')")
    @GetMapping("/list")
    public R<PageResult<BaseOwner>> list(PageQuery query,
                                         @RequestParam(required = false) String ownerName,
                                         @RequestParam(required = false) String phone,
                                         @RequestParam(required = false) Integer userType,
                                         @RequestParam(required = false) Integer authStatus,
                                         @RequestParam(required = false) Long communityId) {
        return R.data(PageResult.of(
                ownerService.pageOwners(query, ownerName, phone, userType, authStatus, communityId)));
    }

    @Operation(summary = "业主详情(手机号/身份证脱敏)")
    @PreAuthorize("@ss.has('base:owner:query')")
    @GetMapping("/{id}")
    public R<BaseOwner> detail(@PathVariable Long id) {
        return R.data(ownerService.detail(id));
    }

    @Operation(summary = "新增/修改业主")
    @PreAuthorize("@ss.has('base:owner:add')")
    @OperLog(module = "业主管理", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody BaseOwner entity) {
        ownerService.saveOwner(entity);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除业主")
    @PreAuthorize("@ss.has('base:owner:remove')")
    @OperLog(module = "业主管理", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        ownerService.remove(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "绑定房屋")
    @PreAuthorize("@ss.has('base:owner:bindRoom')")
    @OperLog(module = "业主管理", businessType = 2)
    @PostMapping("/bind-room")
    public R<Void> bindRoom(@RequestBody BindBody body) {
        ownerService.bindRoom(body.getOwnerId(), body.getRoomId(), body.getCommunityId(), body.getRelation());
        return R.ok("绑定成功");
    }

    @Operation(summary = "解绑房屋")
    @PreAuthorize("@ss.has('base:owner:unbindRoom')")
    @OperLog(module = "业主管理", businessType = 2)
    @PostMapping("/unbind-room")
    public R<Void> unbindRoom(@RequestBody BindBody body) {
        ownerService.unbindRoom(body.getOwnerId(), body.getRoomId());
        return R.ok("解绑成功");
    }

    @Operation(summary = "人脸登记(保存人脸库标识/照片URL)")
    @PreAuthorize("@ss.has('base:owner:faceUpload')")
    @OperLog(module = "业主管理", businessType = 2)
    @PostMapping("/face-upload")
    public R<Void> faceUpload(@RequestBody Map<String, Object> body) {
        Long ownerId = Long.valueOf(String.valueOf(body.get("ownerId")));
        BaseOwner owner = new BaseOwner();
        owner.setId(ownerId);
        owner.setFaceToken((String) body.get("faceToken"));
        owner.setFaceImg((String) body.get("faceImg"));
        ownerService.updateById(owner);
        return R.ok("人脸已登记");
    }

    @Data
    public static class BindBody {
        private Long ownerId;
        private Long roomId;
        private Long communityId;
        private Integer relation;
    }
}
