package com.srs.modules.base.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.base.entity.BaseCommunity;
import com.srs.modules.base.service.BaseCommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "小区管理")
@RestController
@RequestMapping("/api/v1/base/community")
@RequiredArgsConstructor
public class BaseDataController {

    private final BaseCommunityService communityService;

    @Operation(summary = "小区列表(分页)")
    @PreAuthorize("@ss.has('base:community:list')")
    @GetMapping("/list")
    public R<PageResult<BaseCommunity>> list(PageQuery query,
                                             @RequestParam(required = false) String communityName,
                                             @RequestParam(required = false) String communityCode,
                                             @RequestParam(required = false) Integer status) {
        return R.data(PageResult.of(communityService.pageCommunities(query, communityName, communityCode, status)));
    }

    @Operation(summary = "小区下拉选项(受数据权限控制)")
    @PreAuthorize("@ss.has('base:community:list')")
    @GetMapping("/options")
    public R<List<BaseCommunity>> options() {
        return R.data(communityService.options());
    }

    @Operation(summary = "小区详情")
    @PreAuthorize("@ss.has('base:community:query')")
    @GetMapping("/{id}")
    public R<BaseCommunity> detail(@PathVariable Long id) {
        return R.data(communityService.getById(id));
    }

    @Operation(summary = "新增小区")
    @PreAuthorize("@ss.has('base:community:add')")
    @OperLog(module = "小区管理", businessType = 1)
    @PostMapping
    public R<Void> create(@RequestBody BaseCommunity entity) {
        communityService.create(entity);
        return R.ok("新增成功");
    }

    @Operation(summary = "修改小区")
    @PreAuthorize("@ss.has('base:community:edit')")
    @OperLog(module = "小区管理", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody BaseCommunity entity) {
        communityService.modify(entity);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除小区")
    @PreAuthorize("@ss.has('base:community:remove')")
    @OperLog(module = "小区管理", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> delete(@PathVariable List<Long> ids) {
        communityService.remove(ids);
        return R.ok("删除成功");
    }
}
