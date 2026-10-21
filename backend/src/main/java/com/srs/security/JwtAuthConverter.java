package com.srs.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 将 JWT 中自定义的 authorities 声明映射为 Spring Security 权限
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>(defaultConverter.convert(jwt));

        Object claim = jwt.getClaim("authorities");
        if (claim instanceof Collection<?> list) {
            authorities.addAll(list.stream()
                    .map(String::valueOf)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        } else if (claim instanceof String str && !str.isBlank()) {
            for (String s : str.split(",")) {
                authorities.add(new SimpleGrantedAuthority(s.trim()));
            }
        }
        String principal = jwt.getSubject();
        return new JwtAuthenticationToken(jwt, authorities, principal);
    }

    public static List<String> authoritiesOf(Jwt jwt) {
        Object claim = jwt.getClaim("authorities");
        if (claim instanceof Collection<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
