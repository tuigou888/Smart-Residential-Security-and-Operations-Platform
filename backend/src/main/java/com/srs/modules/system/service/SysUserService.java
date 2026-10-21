package com.srs.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.system.entity.SysDept;
import com.srs.modules.system.entity.SysUser;
import com.srs.modules.system.entity.SysUserRole;
import com.srs.modules.system.mapper.SysDeptMapper;
import com.srs.modules.system.mapper.SysRbacMapper;
import com.srs.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysRbacMapper rbacMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    public IPage<SysUser> pageUsers(PageQuery q, String username, String phone, Integer status, Long deptId) {
        Page<SysUser> page = new Page<>(q.getPageNum(), q.getPageSize());
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(phone), SysUser::getPhone, phone)
                .eq(status != null, SysUser::getStatus, status)
                .eq(deptId != null, SysUser::getDeptId, deptId);
        if (StringUtils.hasText(q.getOrderBy())) {
            boolean asc = !"asc".equalsIgnoreCase(q.getOrderDir());
            w.orderBy(true, asc, SysUser::getCreateTime);
        } else {
            w.orderByDesc(SysUser::getId);
        }
        IPage<SysUser> result = baseMapper.selectPage(page, w);
        for (SysUser u : result.getRecords()) {
            if (u.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(u.getDeptId());
                if (dept != null) {
                    u.setDeptName(dept.getDeptName());
                }
            }
        }
        return result;
    }

    public SysUser detail(Long id) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) {
            throw BizException.notFound("用户不存在: " + id);
        }
        user.setRoleIds(rbacMapper.selectRoleIdsByUserId(id));
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUser user) {
        if (baseMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername())) > 0) {
            throw BizException.conflict("用户名已存在: " + user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(
                org.springframework.util.StringUtils.hasText(user.getPassword()) ? user.getPassword() : "admin123"));
        baseMapper.insert(user);
        saveUserRoles(user.getId(), user.getRoleIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        if (user.getId() == null) {
            throw BizException.badRequest("用户ID不能为空");
        }
        SysUser exist = baseMapper.selectById(user.getId());
        if (exist == null) {
            throw BizException.notFound("用户不存在: " + user.getId());
        }
        user.setPassword(null);
        baseMapper.updateById(user);
        saveUserRoles(user.getId(), user.getRoleIds());
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        rbacMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            rbacMapper.insert(ur);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        if (ids.contains(1L)) {
            throw BizException.badRequest("超级管理员账号不允许删除");
        }
        for (Long id : ids) {
            rbacMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        }
        baseMapper.deleteByIds(ids);
    }

    public void changeStatus(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        baseMapper.updateById(user);
    }

    public void resetPwd(Long id, String password) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(
                org.springframework.util.StringUtils.hasText(password) ? password : "admin123"));
        baseMapper.updateById(user);
    }

    public SysUser findByUsername(String username) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    public Set<String> roleKeys(Long userId) {
        return Set.copyOf(rbacMapper.selectRoleKeysByUserId(userId));
    }
}
