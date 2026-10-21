package com.srs.modules.system.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.system.entity.SysUser;
import com.srs.modules.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/v1/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "用户列表(分页)")
    @PreAuthorize("@ss.has('sys:user:list')")
    @GetMapping("/list")
    public R<PageResult<SysUser>> list(PageQuery query,
                                       @RequestParam(required = false) String username,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Long deptId) {
        return R.data(PageResult.of(userService.pageUsers(query, username, phone, status, deptId)));
    }

    @Operation(summary = "用户详情")
    @PreAuthorize("@ss.has('sys:user:query')")
    @GetMapping("/{id}")
    public R<SysUser> detail(@PathVariable Long id) {
        return R.data(userService.detail(id));
    }

    @Operation(summary = "新增用户")
    @PreAuthorize("@ss.has('sys:user:add')")
    @OperLog(module = "用户管理", businessType = 1)
    @PostMapping
    public R<Void> create(@RequestBody SysUser user) {
        userService.createUser(user);
        return R.ok("新增成功");
    }

    @Operation(summary = "修改用户")
    @PreAuthorize("@ss.has('sys:user:edit')")
    @OperLog(module = "用户管理", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody SysUser user) {
        userService.updateUser(user);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除用户")
    @PreAuthorize("@ss.has('sys:user:remove')")
    @OperLog(module = "用户管理", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> delete(@PathVariable List<Long> ids) {
        userService.deleteUsers(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "修改用户状态")
    @PreAuthorize("@ss.has('sys:user:edit')")
    @OperLog(module = "用户管理", businessType = 2)
    @PutMapping("/status")
    public R<Void> changeStatus(@RequestParam Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return R.ok("状态已更新");
    }

    @Operation(summary = "重置密码")
    @PreAuthorize("@ss.has('sys:user:resetPwd')")
    @OperLog(module = "用户管理", businessType = 2)
    @PutMapping("/reset-pwd")
    public R<Void> resetPwd(@RequestBody SysUser user) {
        userService.resetPwd(user.getId(), user.getPassword());
        return R.ok("密码已重置");
    }
}
