package com.srs.modules.system.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.system.entity.SysRole;
import com.srs.modules.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/v1/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "角色列表(分页)")
    @PreAuthorize("@ss.has('sys:role:list')")
    @GetMapping("/list")
    public R<PageResult<SysRole>> list(PageQuery query,
                                       @RequestParam(required = false) String roleName,
                                       @RequestParam(required = false) String roleKey,
                                       @RequestParam(required = false) Integer status) {
        return R.data(PageResult.of(roleService.pageRoles(query, roleName, roleKey, status)));
    }

    @Operation(summary = "角色详情")
    @PreAuthorize("@ss.has('sys:role:query')")
    @GetMapping("/{id}")
    public R<SysRole> detail(@PathVariable Long id) {
        SysRole role = roleService.getById(id);
        if (role != null) {
            role.setMenuIds(roleService.menuIdsOfRole(id));
        }
        return R.data(role);
    }

    @Operation(summary = "新增角色")
    @PreAuthorize("@ss.has('sys:role:add')")
    @OperLog(module = "角色管理", businessType = 1)
    @PostMapping
    public R<Void> create(@RequestBody SysRole role) {
        roleService.createRole(role);
        return R.ok("新增成功");
    }

    @Operation(summary = "修改角色")
    @PreAuthorize("@ss.has('sys:role:edit')")
    @OperLog(module = "角色管理", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody SysRole role) {
        roleService.updateRole(role);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除角色")
    @PreAuthorize("@ss.has('sys:role:remove')")
    @OperLog(module = "角色管理", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> delete(@PathVariable List<Long> ids) {
        roleService.deleteRoles(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "分配菜单权限")
    @PreAuthorize("@ss.has('sys:role:edit')")
    @OperLog(module = "角色管理", businessType = 2)
    @PutMapping("/menu")
    public R<Void> assignMenus(@RequestBody Map<String, Object> body) {
        Long roleId = Long.valueOf(String.valueOf(body.get("roleId")));
        @SuppressWarnings("unchecked")
        List<Long> menuIds = ((List<Object>) body.get("menuIds")).stream()
                .map(o -> Long.valueOf(String.valueOf(o))).toList();
        roleService.assignMenus(roleId, menuIds);
        return R.ok("授权成功");
    }
}
