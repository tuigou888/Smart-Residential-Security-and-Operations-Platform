package com.srs.security;

import com.srs.modules.system.entity.SysRole;
import com.srs.modules.system.entity.SysUser;
import com.srs.modules.system.mapper.SysRbacMapper;
import com.srs.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 从数据库加载用户及其权限/数据范围
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService userService;
    private final SysRbacMapper rbacMapper;
    private final com.srs.modules.system.mapper.SysRoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new UsernameNotFoundException("账号已停用: " + username);
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setRealName(user.getRealName());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUserType(user.getUserType());
        loginUser.setRoles(new HashSet<>(rbacMapper.selectRoleKeysByUserId(user.getId())));
        loginUser.setPermissions(new HashSet<>(rbacMapper.selectPermsByUserId(user.getId())));
        loginUser.setCommunityIds(new HashSet<>(rbacMapper.selectCommunityIdsByUserId(user.getId())));
        loginUser.setDataScope(resolveDataScope(user.getId(), loginUser.getRoles()));
        loginUser.setEnabled(user.getStatus() == null || user.getStatus() == 1);
        return loginUser;
    }

    /** 多角色时取最宽松的数据范围(值越小范围越大) */
    private Integer resolveDataScope(Long userId, Set<String> roles) {
        if (roles.contains("admin")) {
            return 1;
        }
        List<Long> roleIds = rbacMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return 4;
        }
        int scope = 4;
        for (Long roleId : roleIds) {
            SysRole role = roleMapper.selectById(roleId);
            if (role != null && role.getDataScope() != null && role.getDataScope() < scope) {
                scope = role.getDataScope();
            }
        }
        return scope;
    }
}
