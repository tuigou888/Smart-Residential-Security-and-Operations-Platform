package com.srs.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.exception.BizException;
import com.srs.modules.system.entity.SysMenu;
import com.srs.modules.system.mapper.SysMenuMapper;
import com.srs.modules.system.mapper.SysRbacMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    private final SysRbacMapper rbacMapper;

    public List<SysMenu> listMenus(String menuName, Integer status) {
        List<SysMenu> all = baseMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .like(menuName != null && !menuName.isBlank(), SysMenu::getMenuName, menuName)
                .eq(status != null, SysMenu::getStatus, status)
                .orderByAsc(SysMenu::getOrderNum));
        return buildTree(all);
    }

    /** 当前登录用户可见菜单(树形) */
    public List<SysMenu> currentUserMenus(Long userId) {
        if (isAdminUser(userId)) {
            return buildTree(baseMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getMenuType, List.of("M", "C"))
                    .eq(SysMenu::getVisible, 1)
                    .orderByAsc(SysMenu::getOrderNum)));
        }
        return buildTree(rbacMapper.selectMenusByUserId(userId));
    }

    public boolean isAdminUser(Long userId) {
        return rbacMapper.selectRoleKeysByUserId(userId).contains("admin");
    }

    public List<SysMenu> buildTree(List<SysMenu> menus) {
        Map<Long, SysMenu> map = new LinkedHashMap<>();
        for (SysMenu m : menus) {
            map.put(m.getId(), m);
        }
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu m : menus) {
            Long pid = m.getParentId() == null ? 0L : m.getParentId();
            SysMenu parent = map.get(pid);
            if (parent == null || parent.getId().equals(m.getId())) {
                roots.add(m);
            } else {
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(m);
            }
        }
        return roots;
    }

    public void createMenu(SysMenu menu) {
        baseMapper.insert(menu);
    }

    public void updateMenu(SysMenu menu) {
        baseMapper.updateById(menu);
    }

    public void deleteMenu(Long id) {
        long childCount = baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw BizException.badRequest("存在子菜单, 不允许删除");
        }
        baseMapper.deleteById(id);
    }
}
