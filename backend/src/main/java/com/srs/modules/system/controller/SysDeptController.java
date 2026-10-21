package com.srs.modules.system.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.R;
import com.srs.modules.system.entity.SysDept;
import com.srs.modules.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/v1/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @Operation(summary = "部门列表(树形)")
    @PreAuthorize("@ss.has('sys:dept:list')")
    @GetMapping("/list")
    public R<List<SysDept>> list(@RequestParam(required = false) String deptName,
                                 @RequestParam(required = false) Integer status) {
        return R.data(deptService.listDepts(deptName, status));
    }

    @Operation(summary = "新增部门")
    @PreAuthorize("@ss.has('sys:dept:add')")
    @OperLog(module = "部门管理", businessType = 1)
    @PostMapping
    public R<Void> create(@RequestBody SysDept dept) {
        deptService.createDept(dept);
        return R.ok("新增成功");
    }

    @Operation(summary = "修改部门")
    @PreAuthorize("@ss.has('sys:dept:edit')")
    @OperLog(module = "部门管理", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody SysDept dept) {
        deptService.updateDept(dept);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除部门")
    @PreAuthorize("@ss.has('sys:dept:remove')")
    @OperLog(module = "部门管理", businessType = 3)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deptService.deleteDept(id);
        return R.ok("删除成功");
    }
}
