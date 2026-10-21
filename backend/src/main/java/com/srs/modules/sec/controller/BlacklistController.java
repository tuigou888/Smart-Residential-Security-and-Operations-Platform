package com.srs.modules.sec.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.sec.entity.SecBlacklist;
import com.srs.modules.sec.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "黑名单管理")
@RestController
@RequestMapping("/api/v1/access/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    @Operation(summary = "黑名单列表")
    @PreAuthorize("@ss.has('access:blacklist:list')")
    @GetMapping("/list")
    public R<PageResult<SecBlacklist>> list(PageQuery query,
                                            @RequestParam(required = false) Long communityId,
                                            @RequestParam(required = false) Integer blacklistType,
                                            @RequestParam(required = false) String keyword) {
        return R.data(PageResult.of(blacklistService.page(query, communityId, blacklistType, keyword)));
    }

    @Operation(summary = "新增黑名单")
    @PreAuthorize("@ss.has('access:blacklist:add')")
    @OperLog(module = "黑名单管理", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody SecBlacklist item) {
        item.setId(null);
        blacklistService.saveBlacklist(item);
        return R.ok("保存成功");
    }

    @Operation(summary = "修改黑名单")
    @PreAuthorize("@ss.has('access:blacklist:edit')")
    @OperLog(module = "黑名单管理", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody SecBlacklist item) {
        if (item.getId() == null) {
            return R.fail(400, "记录ID不能为空");
        }
        blacklistService.saveBlacklist(item);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除黑名单")
    @PreAuthorize("@ss.has('access:blacklist:remove')")
    @OperLog(module = "黑名单管理", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        blacklistService.removeBlacklist(ids);
        return R.ok("删除成功");
    }
}
