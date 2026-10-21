package com.srs.security;

import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 方法级权限校验辅助(SPEL 引用名为 ss 的 Bean)
 * <p>
 * 超管(dataScope=1 或角色含 admin)拥有全部权限; 其余用户按 authorities 校验。
 */
@Component("ss")
public class SecurityService {

    public boolean has(String... perms) {
        if (SecurityUtils.isAdmin()) {
            return true;
        }
        if (perms == null || perms.length == 0) {
            return true;
        }
        return Arrays.stream(perms).anyMatch(SecurityUtils::hasAuthority);
    }
}
