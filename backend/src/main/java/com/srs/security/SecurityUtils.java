package com.srs.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 当前登录用户上下文工具
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /** 当前用户ID, 未登录返回 null */
    public static Long getUserId() {
        Object p = principal();
        if (p instanceof Jwt jwt) {
            return longVal(jwt.getClaim("userId"));
        }
        return null;
    }

    public static String getUsername() {
        Authentication auth = getAuthentication();
        if (auth == null) {
            return null;
        }
        if (auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return auth.getName();
    }

    public static String getRealName() {
        Object p = principal();
        if (p instanceof Jwt jwt) {
            return jwt.getClaim("realName");
        }
        return getUsername();
    }

    public static Integer getDataScope() {
        Object p = principal();
        if (p instanceof Jwt jwt) {
            Integer v = jwt.getClaim("dataScope");
            return v == null ? 1 : v;
        }
        return 1;
    }

    public static boolean isAdmin() {
        Object p = principal();
        if (p instanceof Jwt jwt) {
            Object roles = jwt.getClaim("roles");
            return roles != null && String.valueOf(roles).contains("admin");
        }
        return false;
    }

    /** 数据权限可见的小区ID集合, 为空表示不限制 */
    public static Set<Long> getCommunityIds() {
        Object p = principal();
        if (p instanceof Jwt jwt) {
            String v = jwt.getClaim("communityIds");
            if (v == null || v.isBlank()) {
                return Set.of();
            }
            Set<Long> ids = new HashSet<>();
            for (String s : v.split(",")) {
                if (!s.isBlank()) {
                    try {
                        ids.add(Long.parseLong(s.trim()));
                    } catch (NumberFormatException ignored) {
                        // ignore
                    }
                }
            }
            return ids;
        }
        return Set.of();
    }

    public static Long getDeptId() {
        Object p = principal();
        if (p instanceof Jwt jwt) {
            return longVal(jwt.getClaim("deptId"));
        }
        return null;
    }

    public static boolean hasAuthority(String perm) {
        Authentication auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(perm));
    }

    private static Object principal() {
        Authentication auth = getAuthentication();
        if (auth instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        return null;
    }

    private static Long longVal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        return Optional.of(String.valueOf(v)).filter(s -> !s.isBlank()).map(Long::parseLong).orElse(null);
    }
}
