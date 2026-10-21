package com.srs.modules.system.controller;

import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.system.entity.SysConfig;
import com.srs.modules.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "参数配置")
@RestController
@RequestMapping("/api/v1/sys/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    @Operation(summary = "参数列表")
    @PreAuthorize("@ss.has('sys:config:list')")
    @GetMapping("/list")
    public R<PageResult<SysConfig>> list(PageQuery query,
                                         @RequestParam(required = false) String configName,
                                         @RequestParam(required = false) String configKey) {
        return R.data(PageResult.of(configService.pageConfigs(query, configName, configKey)));
    }

    @Operation(summary = "按Key获取参数值")
    @PreAuthorize("@ss.has('sys:config:query')")
    @GetMapping("/key/{key}")
    public R<String> byKey(@PathVariable String key) {
        return R.data(configService.valueByKey(key));
    }

    @Operation(summary = "新增参数")
    @PreAuthorize("@ss.has('sys:config:add')")
    @PostMapping
    public R<Void> create(@RequestBody SysConfig config) {
        configService.save(config);
        return R.ok("新增成功");
    }

    @Operation(summary = "修改参数")
    @PreAuthorize("@ss.has('sys:config:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysConfig config) {
        configService.updateById(config);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除参数")
    @PreAuthorize("@ss.has('sys:config:remove')")
    @DeleteMapping("/{ids}")
    public R<Void> delete(@PathVariable java.util.List<Long> ids) {
        configService.removeByIds(ids);
        return R.ok("删除成功");
    }
}
