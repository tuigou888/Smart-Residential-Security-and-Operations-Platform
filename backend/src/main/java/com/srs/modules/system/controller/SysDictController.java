package com.srs.modules.system.controller;

import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.system.entity.SysDictData;
import com.srs.modules.system.entity.SysDictType;
import com.srs.modules.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典管理")
@RestController
@RequestMapping("/api/v1/sys/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService dictService;

    @Operation(summary = "字典类型列表")
    @PreAuthorize("@ss.has('sys:dict:list')")
    @GetMapping("/type/list")
    public R<PageResult<SysDictType>> typeList(PageQuery query,
                                               @RequestParam(required = false) String dictName,
                                               @RequestParam(required = false) String dictType) {
        return R.data(PageResult.of(dictService.pageTypes(query, dictName, dictType)));
    }

    @Operation(summary = "字典数据列表")
    @PreAuthorize("@ss.has('sys:dict:list')")
    @GetMapping("/data/list")
    public R<PageResult<SysDictData>> dataList(PageQuery query,
                                               @RequestParam(required = false) String dictType,
                                               @RequestParam(required = false) String dictLabel) {
        return R.data(PageResult.of(dictService.pageData(query, dictType, dictLabel)));
    }

    @Operation(summary = "按类型查询字典数据(下拉用)")
    @PreAuthorize("@ss.has('sys:dict:list')")
    @GetMapping("/data/type/{dictType}")
    public R<List<SysDictData>> dataByType(@PathVariable String dictType) {
        return R.data(dictService.dataByType(dictType));
    }

    @Operation(summary = "新增字典类型")
    @PreAuthorize("@ss.has('sys:dict:add')")
    @PostMapping("/type")
    public R<Void> createType(@RequestBody SysDictType type) {
        dictService.saveOrUpdate(type);
        return R.ok("新增成功");
    }

    @Operation(summary = "修改字典类型")
    @PreAuthorize("@ss.has('sys:dict:edit')")
    @PutMapping("/type")
    public R<Void> updateType(@RequestBody SysDictType type) {
        dictService.saveOrUpdate(type);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除字典类型")
    @PreAuthorize("@ss.has('sys:dict:remove')")
    @DeleteMapping("/type/{ids}")
    public R<Void> deleteType(@PathVariable List<Long> ids) {
        dictService.removeByIds(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "新增字典数据")
    @PreAuthorize("@ss.has('sys:dict:add')")
    @PostMapping("/data")
    public R<Void> createData(@RequestBody SysDictData data) {
        dictService.saveData(data);
        return R.ok("新增成功");
    }

    @Operation(summary = "修改字典数据")
    @PreAuthorize("@ss.has('sys:dict:edit')")
    @PutMapping("/data")
    public R<Void> updateData(@RequestBody SysDictData data) {
        dictService.saveData(data);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除字典数据")
    @PreAuthorize("@ss.has('sys:dict:remove')")
    @DeleteMapping("/data/{id}")
    public R<Void> deleteData(@PathVariable Long id) {
        dictService.removeData(id);
        return R.ok("删除成功");
    }
}
