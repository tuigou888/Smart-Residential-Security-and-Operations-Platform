package com.srs.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户主体(同时作为 Spring Security UserDetails 与 JWT Claims 来源)
 */
@Data
public class LoginUser implements UserDetails {

    private Long userId;

    private String username;

    private String password;

    private String realName;

    private Long deptId;

    private Integer userType;

    /** 数据范围: 1全部 2自定义 3本部门 4本小区 */
    private Integer dataScope;

    /** 可访问的小区ID集合(数据权限) */
    private Set<Long> communityIds;

    /** 角色标识集合 */
    private Set<String> roles;

    /** 权限标识集合 */
    private Set<String> permissions;

    private boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null) {
            return List.of();
        }
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public boolean isAdmin() {
        return roles != null && roles.contains("admin");
    }
}
