package com.srs.modules.system.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.R;
import com.srs.modules.system.entity.SysMenu;
import com.srs.modules.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/v1/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @Operation(summary = "菜单列表(树形)")
    @PreAuthorize("@ss.has('sys:menu:list')")
    @GetMapping("/list")
    public R<List<SysMenu>> list(@RequestParam(required = false) String menuName,
                                 @RequestParam(required = false) Integer status) {
        return R.data(menuService.listMenus(menuName, status));
    }

    @Operation(summary = "菜单树(权限分配用)")
    @PreAuthorize("@ss.has('sys:menu:list')")
    @GetMapping("/tree")
    public R<List<SysMenu>> tree() {
        return R.data(menuService.listMenus(null, null));
    }

    @Operation(summary = "菜单详情")
    @PreAuthorize("@ss.has('sys:menu:query')")
    @GetMapping("/{id}")
    public R<SysMenu> detail(@PathVariable Long id) {
        return R.data(menuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @PreAuthorize("@ss.has('sys:menu:add')")
    @OperLog(module = "菜单管理", businessType = 1)
    @PostMapping
    public R<Void> create(@RequestBody SysMenu menu) {
        menuService.createMenu(menu);
        return R.ok("新增成功");
    }

    @Operation(summary = "修改菜单")
    @PreAuthorize("@ss.has('sys:menu:edit')")
    @OperLog(module = "菜单管理", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除菜单")
    @PreAuthorize("@ss.has('sys:menu:remove')")
    @OperLog(module = "菜单管理", businessType = 3)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok("删除成功");
    }
}
