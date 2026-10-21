package com.srs.modules.system.service;

import com.srs.common.exception.BizException;
import com.srs.modules.system.entity.SysMenu;
import com.srs.modules.system.entity.SysRole;
import com.srs.modules.system.entity.SysUser;
import com.srs.modules.system.mapper.SysRbacMapper;
import com.srs.modules.system.mapper.SysRoleMapper;
import com.srs.security.JwtTokenService;
import com.srs.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final SysUserService userService;
    private final SysLogService logService;
    private final SysMenuService menuService;
    private final SysRbacMapper rbacMapper;
    private final SysRoleMapper roleMapper;

    @Value("${srs.security.access-token-ttl:2h}")
    private String accessTokenTtl;

    public Map<String, Object> login(String username, String password, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", jwtTokenService.createAccessToken(loginUser));
            result.put("refreshToken", jwtTokenService.createRefreshToken(loginUser));
            result.put("tokenType", "Bearer");
            result.put("expiresIn", 7200);

            SysUser user = new SysUser();
            user.setId(loginUser.getUserId());
            user.setLoginIp(request == null ? null : com.srs.common.util.IpUtils.getIp(request));
            user.setLoginTime(LocalDateTime.now());
            userService.getBaseMapper().updateById(user);

            logService.recordLogin(username, true, "登录成功", request);
            log.info("[login] {} 登录成功", username);
            return result;
        } catch (Exception e) {
            log.warn("[login] {} 登录失败", username, e);
            logService.recordLogin(username, false, e.getMessage(), request);
            throw BizException.unauthorized("用户名或密码错误");
        }
    }

    /** 刷新令牌: 校验 refresh 类型 JWT 后签发新的访问令牌 */
    public Map<String, Object> refresh(LoginUser loginUser) {
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", jwtTokenService.createAccessToken(loginUser));
        result.put("tokenType", "Bearer");
        result.put("expiresIn", 7200);
        return result;
    }

    public Map<String, Object> info(LoginUser loginUser) {
        SysUser user = userService.getById(loginUser.getUserId());
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roles", loginUser.getRoles());
        data.put("permissions", loginUser.getPermissions());
        data.put("dataScope", loginUser.getDataScope());
        data.put("communities", loginUser.getCommunityIds());

        List<SysMenu> menus = menuService.currentUserMenus(loginUser.getUserId());
        data.put("menus", menus);
        data.put("roleNames", roleNames(loginUser.getUserId()));
        return data;
    }

    private List<String> roleNames(Long userId) {
        List<String> names = new ArrayList<>();
        for (Long roleId : rbacMapper.selectRoleIdsByUserId(userId)) {
            SysRole role = roleMapper.selectById(roleId);
            if (role != null) {
                names.add(role.getRoleName());
            }
        }
        return names;
    }

    @Data
    public static class LoginBody {
        private String username;
        private String password;
    }
}
