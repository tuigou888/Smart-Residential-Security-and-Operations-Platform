package com.srs.modules.system.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.R;
import com.srs.modules.system.service.AuthService;
import com.srs.security.LoginUser;
import com.srs.security.SecurityUtils;
import com.srs.security.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtDecoder jwtDecoder;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginBody body, HttpServletRequest request) {
        return R.data(authService.login(body.getUsername(), body.getPassword(), request));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok("已退出登录");
    }

    @Operation(summary = "获取当前用户信息(角色/权限/菜单)")
    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        String username = SecurityUtils.getUsername();
        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(username);
        return R.data(authService.info(loginUser));
    }

    @Operation(summary = "刷新访问令牌")
    @PostMapping("/refresh")
    public R<Map<String, Object>> refresh(@RequestBody RefreshBody body) {
        if (body.getRefreshToken() == null || body.getRefreshToken().isBlank()) {
            return R.fail(400, "refreshToken 不能为空");
        }
        Jwt jwt = jwtDecoder.decode(body.getRefreshToken());
        if (!"refresh".equals(jwt.getClaim("type"))) {
            return R.fail(400, "令牌类型错误");
        }
        LoginUser user = new LoginUser();
        user.setUsername(jwt.getSubject());
        user.setUserId(Long.valueOf(String.valueOf(jwt.getClaim("userId"))));
        user.setRealName(jwt.getClaim("realName"));
        user.setDeptId(jwt.getClaim("deptId") == null ? null
                : Long.valueOf(String.valueOf(jwt.getClaim("deptId"))));
        user.setUserType(jwt.getClaim("userType"));
        user.setDataScope(jwt.getClaim("dataScope"));
        String roles = jwt.getClaim("roles");
        user.setRoles(roles == null || roles.isBlank() ? new HashSet<>()
                : new HashSet<>(List.of(roles.split(","))));
        String communities = jwt.getClaim("communityIds");
        HashSet<Long> communityIds = new HashSet<>();
        if (communities != null && !communities.isBlank()) {
            for (String s : communities.split(",")) {
                communityIds.add(Long.valueOf(s));
            }
        }
        user.setCommunityIds(communityIds);
        return R.data(authService.refresh(user));
    }

    /** 校验访问令牌是否有效(用于前端探活) */
    @Operation(summary = "令牌校验")
    @GetMapping("/check")
    public R<Map<String, Object>> check() {
        var auth = SecurityUtils.getAuthentication();
        boolean valid = auth instanceof JwtAuthenticationToken token && token.isAuthenticated();
        Map<String, Object> data = Map.of(
                "valid", valid,
                "username", SecurityUtils.getUsername(),
                "userId", SecurityUtils.getUserId() == null ? 0L : SecurityUtils.getUserId());
        return R.data(data);
    }

    @Data
    public static class LoginBody {
        private String username;
        private String password;
    }

    @Data
    public static class RefreshBody {
        private String refreshToken;
    }
}
