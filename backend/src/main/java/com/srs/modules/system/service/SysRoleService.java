package com.srs.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.system.entity.SysRole;
import com.srs.modules.system.entity.SysRoleMenu;
import com.srs.modules.system.mapper.SysRoleMapper;
import com.srs.modules.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRoleMenuMapper roleMenuMapper;

    public IPage<SysRole> pageRoles(PageQuery q, String roleName, String roleKey, Integer status) {
        Page<SysRole> page = new Page<>(q.getPageNum(), q.getPageSize());
        return baseMapper.selectPage(page, new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .like(StringUtils.hasText(roleKey), SysRole::getRoleKey, roleKey)
                .eq(status != null, SysRole::getStatus, status)
                .orderByAsc(SysRole::getRoleSort));
    }

    public List<Long> menuIdsOfRole(Long roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void createRole(SysRole role) {
        if (baseMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, role.getRoleKey())) > 0) {
            throw BizException.conflict("角色标识已存在: " + role.getRoleKey());
        }
        baseMapper.insert(role);
        saveRoleMenus(role.getId(), role.getMenuIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRole role) {
        baseMapper.updateById(role);
        if (role.getMenuIds() != null) {
            saveRoleMenus(role.getId(), role.getMenuIds());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        saveRoleMenus(roleId, menuIds);
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRoles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        }
        baseMapper.deleteByIds(ids);
    }
}
